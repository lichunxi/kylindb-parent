/**
 * 
 */
package net.kylindb.timer;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 利用环形队列来解决超时、时间窗口等时间到期后触发某项任务的需要<br>
 * 可用于场景：session超时、时间窗口、延迟通知等等<p>
 * <strong>原理：</strong><br>
 * 一个时间轮定时器WheelTimer，这个轮子上有WheelSize个桶Bucket，每个桶内有一个Timer列表，当该Timer到期时，触发对应的Task。
 * <br>
 * 一个线程负责不停地（间隔时间period）扫描wheelTimer，每隔period（一个tick）跳到下一个Bucket，对于其中的Timer，检查remainingCycle是否为0。
 * 如果remainingCycle<=0则说明Timer到期，触发对应的Task，否则remainingCycle--(表示逝去时间 = WheelSize * period)<br>
 * <strong>可以想象为一个表盘，有个秒针在不停的走动，每次走一格（tick），表示1秒钟，走一圈就是1分钟</strong>
 * <p>
 * 注意：该工具类的时间都不是非常精确，按照tick=100ms颗粒来计算的，1秒为10个tick，但是中间任务调度耗费的时间会影响精度，
 * 例如每次处理bucket中的timer耗费5ms，则10次一共耗费50ms，造成偏移。另外，sleep和System.currentTimeMillis()都会造成误差
 * <br>
 * 为了尽可能减少偏移（如果不修正会不断累积），每次移动都要重新计算sleep时间(理论上是tick=PERIOD，实际上应该会稍微小一点)
 * 
 * @see
 * 网上的原理说明：https://www.cnblogs.com/dytl/p/6530422.html
 * @see
 * 也可参照netty类似工具类org.jboss.netty.util.HashedWheelTimer
 * 
 * @author lichunxi
 *
 */
public class WheelTimer {
	public static Logger LOG = LoggerFactory
			.getLogger(WheelTimer.class);
	
	/**
	 * 时间间隔，单位:毫秒
	 */
	private static final int PERIOD = 100;
	
	/**
	 * 轮子上bucket个数，即wheelSize
	 */
	private static final int WHEEL_SIZE = 600;
	
	/**
	 * 定时扫描线程
	 */
	private Thread selector = null;
	private UncaughtExceptionHandler uncaughtExceptionHandler = null;
	
	/**
	 * 扫描线程启动时间
	 */
	private Long start;
	
	private AtomicBoolean isRunning = new AtomicBoolean(true);
	
	/**
	 * bucket列表
	 */
	private Bucket[] buckets = new Bucket[WHEEL_SIZE];
	
	/**
	 * 表示扫描线程已经到达的bucket位置<p>
	 * 由于需要在扫描线程不断运行的过程中动态增加timer，所以需要根据index动态计算timer的bucketIndex
	 */
	private volatile int index = 0;
	
	/**
	 * 扫描线程从启动到当前为止累计的tick数量，每次移动加1
	 */
	private long ticks = 0;
	
	/**
	 * 任务调度线程池
	 */
	private ExecutorService taskExecutor = Executors.newFixedThreadPool(5, new ThreadFactory() {
		
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "wheelTimer-task-executor");
		}
	});
	
	/**
	 * timer定时器map，用于单独对指定的timer进行操作
	 * <br>
	 * 以timerId为key，便于查找
	 */
	protected Map<Long, Timer> timerMap = new ConcurrentHashMap<Long, Timer>();

	private static class TaskTimerLazyHolder {
		static final WheelTimer WHEEL_TIMER = new WheelTimer();
	}

	private WheelTimer() {
		//1:启动扫描线程
		selector = new Thread(new TimerSelector(), "wheelTimerSelector");
		// 当jvm抛出无法处理的异常时，需要自行处理，否则会导致线程退出（不能通过exception获取到）
		uncaughtExceptionHandler = new UncaughtExceptionHandler(){
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				LOG.warn("uncaught exception, restart kafkaConsumer.", e);
	        	// 重新启动线程
	        	selector = new Thread(new TimerSelector(), "wheelTimerSelector");
	        	selector.setUncaughtExceptionHandler(uncaughtExceptionHandler);
	        	selector.start();
			}
		};
        selector.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        selector.start();
        start = System.nanoTime();
	}

	public static WheelTimer getInstance() {
		return TaskTimerLazyHolder.WHEEL_TIMER;
	}

	/**
	 * 停止扫描线程，并且返回所有未到期未触发的timer
	 */
	public Map<Long, Timer> stop(){
		isRunning.set(false);
		return timerMap;
	}
	
	/**
	 * 新增定时器, 可用级联的方式。例如：wheelTimer.appendTimer(10, task1)..appendTimer(15, task2)
	 * @param timeout 超时时间，单位：秒
	 * @param task 超时后要触发的任务
	 * @return WheelTimer
	 */
	public WheelTimer appendTimer(Integer timeout, Task task) {
		addTimer(timeout, task);
		return this;
	}
	
	/**
	 * 新增定时器, 可用级联的方式。例如：wheelTimer.appendTimer(100, 10, task1)..appendTimer(101, 15, task2)
	 * @param timerId 自定义TimerId
	 * @param timeout 超时时间，单位：秒
	 * @param task 超时后要触发的任务
	 * @return WheelTimer
	 */
	public WheelTimer appendTimer(Long timerId, Integer timeout, Task task) {
		List<Task> taskList = new ArrayList<Task>();
		taskList.add(task);
		
		addTimer(timerId, timeout, taskList);
		return this;
	}
	
	/**
	 * 新增定时器
	 * @param timeout 超时时间，单位：秒
	 * @param task 超时后要触发的任务
	 * @return timerId timerId的唯一序号，外部可以保存用于再次访问该timer
	 */
	public long addTimer(Integer timeout, Task task) {
		List<Task> taskList = new ArrayList<Task>();
		taskList.add(task);
		
		return addTimer(null, timeout, taskList);
	}
	
	/**
	 * 新增定时器，task按顺序task1、task2执行
	 * @param timeout 超时时间，单位：秒
	 * @param task1 超时后要触发的任务
	 * @param task2 超时后要触发的任务
	 * @return timerId timerId的唯一序号，外部可以保存用于再次访问该timer
	 */
	public long addTimer(Integer timeout, Task task1, Task task2) {
		List<Task> taskList = new ArrayList<Task>();
		taskList.add(task1);
		taskList.add(task2);
		
		return addTimer(null, timeout, taskList);
	}
	
	/**
	 * 新增定时器，task按顺序task1、task2、task3执行
	 * @param timeout 超时时间，单位：秒
	 * @param task1 超时后要触发的任务
	 * @param task2 超时后要触发的任务
	 * @param task3 超时后要触发的任务
	 * @return timerId timerId的唯一序号，外部可以保存用于再次访问该timer
	 */
	public long addTimer(Integer timeout, Task task1, Task task2, Task task3) {
		List<Task> taskList = new ArrayList<Task>();
		taskList.add(task1);
		taskList.add(task2);
		taskList.add(task3);
		
		return addTimer(null, timeout, taskList);
	}
	
	/**
	 * 新增定时器，task按顺序list中顺序执行
	 * @param timerId 自定义TimerId
	 * @param timeout 超时时间，单位：秒
	 * @param tasks 超时后要触发的任务列表
	 * @return timerId timerId的唯一序号，外部可以保存用于再次访问该timer
	 */
	public long addTimer(Long timerId, Integer timeout, List<Task> tasks) {
		if (null == timeout || 0 >= timeout){
			throw new IllegalArgumentException("timeout must be an positive number.");
		}
		
		if (null == tasks || 0 >= tasks.size()){
			throw new IllegalArgumentException("you should add task to do something when timeout expired.");
		}
		
		// 一共要多少ticks才能超时
		Integer needTicks = timeout * 1000 / PERIOD;
		// 要转多少圈
		Integer cycleNum = (needTicks - 1) / WHEEL_SIZE;
		// 还剩余多少ticks，再加上最新index位置，则为该timer的bucketIndex
		Integer bucketIndex = getBucketIndex(needTicks - 1 - cycleNum * WHEEL_SIZE);
		LOG.debug("addTimer, {}, cycleNum:{},bucketIndex:{},needTicks:{},index:{}", 
				System.currentTimeMillis(), cycleNum, bucketIndex, needTicks, index);
		
		if (null == timerId) {
			// 内部自动生成timerId，保证不冲突
			do {
				timerId = Timer.generateId();
			} while (timerMap.containsKey(timerId));
		} else {
			// 检测timerId是否冲突
			if (timerMap.containsKey(timerId)){
				throw new IllegalArgumentException("timerId conflict, you should keep timerId unique.");
			}
		}
		Timer timer = new Timer(timerId, timeout, cycleNum, bucketIndex, tasks);
		addTimerToBucket(bucketIndex, timerId);
		timerMap.put(timerId, timer);
		return timerId;
	}
	
	public Timer getTimer(Long timerId){
		return timerMap.get(timerId);
	}
	
	/**
	 * 删除定时器<br>
	 * 在定时器未到期时，用户可以手动删除定时器<br>
	 * 定时器到期后，wheelTimer会自动删除对应的定时器，不需要用户手动删除
	 * @param timerId 定时器唯一ID，需要外部程序自行存储
	 */
	public void deleteTimer(long timerId){
		Timer timer = timerMap.get(timerId);
		if (null != timer){
			timerMap.remove(timerId);
			buckets[timer.getBucketIndex()].deleteTimer(timerId);
			LOG.debug("delete timer, timerId={}", timerId);
		}
	}
	
	/**
	 * 立即触发定时器<br>
	 * 定时器触发后，wheelTimer会自动删除对应的定时器，不需要用户手动删除
	 * @param timerId 定时器唯一ID，需要外部程序自行存储
	 */
	public void expireTimer(long timerId){
		Timer timer = timerMap.get(timerId);
		if (null != timer){
			expireTimer(timer);
			timerMap.remove(timerId);
			removeTimerFromBucket(timer.getBucketIndex(), timerId);
			LOG.debug("expire timer, timerId={}", timerId);
		}
	}
	
	/**
	 * 更新定时器，例如当收到消息后，设置定时器重新开始
	 * @param timerId 定时器唯一ID
	 * @param newTimeout 新的超时时间，单位：秒，如果timeout<=0或者为null，则采用之前的timeout值
	 */
	public void updateTimer(long timerId, Integer newTimeout) {
		Timer timer = timerMap.get(timerId);
		if (null != timer) {
			// 1:先从原来的bucket中删除
			removeTimerFromBucket(timer.getBucketIndex(), timerId);

			// 2:再增加到新的bucket中，在此过程中不需要重新new timer
			Integer timeout = (null == newTimeout || newTimeout <= 0) ? timer.getTimeout() : newTimeout;
			// 一共要多少ticks才能超时
			Integer needTicks = timeout * 1000 / PERIOD;
			// 要转多少圈
			Integer cycleNum = (needTicks - 1) / WHEEL_SIZE;
			Integer bucketIndex = getBucketIndex(needTicks - 1 - cycleNum * WHEEL_SIZE);
			timer.setRemainingCycle(cycleNum);
			timer.setTimeout(timeout);
			timer.setBucketIndex(bucketIndex);
			addTimerToBucket(bucketIndex, timerId);
			LOG.debug("updateTimer, {}, cycleNum:{},bucketIndex:{},needTicks:{},index:{}", 
					System.currentTimeMillis(), cycleNum, bucketIndex, needTicks, index);
		}
	}

	/**
	 * @param bucketIndex
	 * @param timerId
	 */
	private synchronized void addTimerToBucket(Integer bucketIndex, Long timerId) {
		if (null == buckets[bucketIndex]){
			buckets[bucketIndex] = new Bucket();
		}
		buckets[bucketIndex].addTimer(timerId);
	}
	
	/**
	 * @param bucketIndex
	 * @param timerId
	 */
	private synchronized void removeTimerFromBucket(Integer bucketIndex, long timerId) {
		if (null != buckets[bucketIndex]){
			buckets[bucketIndex].deleteTimer(timerId);
		}
	}

	public class TimerSelector implements Runnable{

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			long duration = PERIOD;
			while (isRunning.get()) {
				try {
					// 1:先sleep，不应该有立即执行的任务
					Thread.sleep(duration);
					
					//2:处理bucket
					processBucket(buckets[index]);
					index = (index >= WHEEL_SIZE - 1) ? 0 : index + 1;
					ticks++;
					
					//3:修正时间偏移
					duration = amendPeriod();
//					LOG.debug("loop duration:{}, ticks:{}", duration, ticks);
				} catch (Exception e) {
					LOG.error("exception in timer selector.", e);
				}
			}
		}

		/**
		 * 修正时间偏移
		 * @return 下次应该sleep的时长，毫秒
		 */
		private long amendPeriod() {
			// 从启动到上一次移动理论耗费的时间：ticks * period
			// 实际耗费时间=当前时间 - start
			// 本次sleep的时间= PERIOD - (实际耗费时间 - 理论耗费时间)
			// 如果本次要sleep的时间小于10ms，则跳过，累计到下次
			final int millisToNano = 1000000;
			long lapseInTheory = ticks * PERIOD * millisToNano;  //统一按照纳秒进行比较
			long lapseInFact = System.nanoTime() - start;
			long duration = PERIOD * millisToNano - (lapseInFact - lapseInTheory);
			// 小于十分之一Period则本次不sleep
			if (duration <= PERIOD / 10 * millisToNano){
				return 0;
			}
			return duration / millisToNano;
		}

		/**
		 * 处理该bucket中所有的timer列表
		 * @param bucket
		 */
		private void processBucket(Bucket bucket) {
			if (null == bucket){
				return;
			}
			Iterator<Long> timerIdIterator = bucket.iterator();
			while(timerIdIterator.hasNext()) {
				Long timerId = timerIdIterator.next();
				Timer timer = timerMap.get(timerId);
				
				if (null != timer){
					if (timer.getRemainingCycle() <= 0) {
						expireTimer(timer);
						
						// 任务触发完毕后，需要把timer移除
						timerIdIterator.remove();
						timerMap.remove(timerId);
					}
					timer.decreaseRemainingCycle();
				} else {
					// 不应该到此处
					LOG.error("timerId:{} exist in bucket, but not in timerMap.", timerId);
				}
			}
		}
	}
	
	private Integer getBucketIndex(int deltaTicks){
		Integer bucketIndex = index + deltaTicks;
		if (bucketIndex >= WHEEL_SIZE){
			bucketIndex = bucketIndex - WHEEL_SIZE;
		}
		return bucketIndex;
	}
	
	private void expireTimer(Timer timer) {
		// 触发任务列表，为了尽可能减少任务执行对定时器的影响，用多线程进行调度，尽量不影响扫描线程
		taskExecutor.execute(new Runnable() {
			public void run() {
				// 一个timer只允许一个线程来执行expire
				if (false == timer.getExpiring()) {
					synchronized (timer) {
						if (false == timer.getExpiring()) {
							timer.setExpiring(true);
							List<? extends Task> taskList = timer.getTaskList();
							if (null != taskList && 0 < taskList.size()) {
								for (Task task : taskList) {
									try {
										task.run();
									} catch (Exception e) {
										LOG.warn("exception in task.", e);
									}
								}
							}
						}
					}
				}
			}
		});
	}
}
