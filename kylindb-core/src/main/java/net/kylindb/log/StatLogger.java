/**
 * 
 */
package net.kylindb.log;


import net.kylindb.stat.service.RequestMetricsService;
import net.kylindb.stat.util.JvmUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 定期输出性能统计日志
 * 
 * @author beny
 *
 */
public class StatLogger {
	private static final Logger LOG = LoggerFactory.getLogger(StatLogger.class);
	/**
	 * 线程是否启动标识位
	 */
	private static AtomicBoolean start = new AtomicBoolean(false);

	/**
	 * 定时任务周期，5分钟
	 */
	private static final Long INTERVAL = 5 * 60 * 1000L;
	private static ExecutorService executorService = Executors
			.newSingleThreadExecutor();
	private static Lock lock = new ReentrantLock();

	public static void init() {
		if (!start.get()) {
			lock.lock();
			try {
				if (!start.get()) {
					start.set(true);
					executorService.execute(new Runnable() {

						@Override
						public void run() {
							try {
								while (start.get()) {
									Thread.sleep(INTERVAL);
									// 输出性能日志
									PerformanceLog.log(JvmUtil.getJvmInfo(),
											RequestMetricsService.queryAllRequestMetricDataLine());
									RequestMetricsService.flipAll();
								}
							} catch (InterruptedException e) {
								LOG.error("thread interrupted.", e);
							}
						}

					});
				}
			} finally {
				lock.unlock();
			}
		}
	}

//	static {
//		StatLogger.init();	//默认开启性能日志输出
//	}

	public static Boolean getStart() {
		return start.get();
	}

	public static void setStart(Boolean bStart){
		start.set(bStart);
	}

}
