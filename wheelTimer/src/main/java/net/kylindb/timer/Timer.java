/**
 * Created:2018年1月26日 上午11:24:02
 * Author:lichunxi
 */
package net.kylindb.timer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 每个bucket中包含多个timer，到timer到期后，会触发下面的task（允许多个，逐一触发）
 * 
 * @author lichunxi
 *
 */
public class Timer {
	/**
	 * 用于产生timerId随机数
	 */
	private static Random rand = new Random();
	
	/**
	 * 唯一标识
	 */
	private Long timerId;
	
	/**
	 * 超时时间
	 */
	private Integer timeout;
	
	/**
	 * 剩余的圈数，每一圈代表时间 = wheelSize * period
	 * 0表示时间到期
	 */
	private Integer remainingCycle = 0;
	
	/**
	 * 归属哪个bucket
	 */
	private Integer bucketIndex = 0;
	
	/**
	 * 任务列表
	 */
	private List<Task> taskList = null;
	
	/**
	 * 保证一个timer只能被一个线程执行，第一个进来的线程把该字段赋值为true，
	 * 第二个线程（例如调用expireTimer函数的线程）根据该字段值判断已经被执行过了，不应该再次执行
	 */
	private AtomicBoolean expiring = new AtomicBoolean(false);
	
	/**
	 * 创建定时器
	 * @param timerId 用户自定义timerId
	 * @param timeout 定时器超时时间，单位：秒
	 * @param remainingCycle 需要旋转的圈数
	 * @param bucketIndex 所属的bucket索引
	 * @param tasks 到期后要执行的任务列表
	 */
	public Timer(Long timerId, Integer timeout, Integer remainingCycle, Integer bucketIndex, List<Task> tasks) {
		super();
		this.timerId = timerId;
		this.timeout = timeout;
		this.remainingCycle = remainingCycle;
		this.bucketIndex = bucketIndex;
		this.taskList = tasks;
	}

	/**
	 * @return the timerId
	 */
	public Long getTimerId() {
		return timerId;
	}
	
	/**
	 * @return the timeout
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * @return the bucketIndex
	 */
	public Integer getBucketIndex() {
		return bucketIndex;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	/**
	 * @param bucketIndex the bucketIndex to set
	 */
	public void setBucketIndex(Integer bucketIndex) {
		this.bucketIndex = bucketIndex;
	}

	/**
	 * 获得随机的timerId
	 * @return
	 */
	public static Long generateId() {
		// timerId由时间戳（毫秒） + 4byte随机数组成
		// 在同一毫秒内，id出现重复的概率小于百万分之八十（实测，大约）
		long timestamp = System.currentTimeMillis();
		int randInt = rand.nextInt();
		return (timestamp << 32) | randInt;
	}

	public void decreaseRemainingCycle(){
		remainingCycle--;
	}

	public Integer getRemainingCycle(){
		return remainingCycle;
	}
	
	public void setRemainingCycle(Integer remainingCycle) {
		this.remainingCycle = remainingCycle;
	}

	public List<Task> getTaskList(){
		return taskList;
	}
	
	/**
	 * 获取第一个task，适用于只有一个task的情况
	 * @return Task
	 */
	public Task getTask(){
		return taskList.get(0);
	}
	
	public void addTask(Task task){
		this.taskList.add(task);
	}

	/**
	 * @return the expiring
	 */
	public boolean getExpiring() {
		return expiring.get();
	}

	/**
	 * @param expiring the expiring to set
	 */
	public void setExpiring(boolean expiring) {
		this.expiring.set(expiring);
	}
	
}
