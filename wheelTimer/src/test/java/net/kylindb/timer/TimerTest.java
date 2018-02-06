/**
 * 
 */
package net.kylindb.timer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.kylindb.timer.Task;
import net.kylindb.timer.Timer;
import net.kylindb.timer.WheelTimer;


/**
 * @author lichunxi
 *
 */
public class TimerTest {
	public static class DefaultListener implements Task{

		private int timeout;
		
		private long startTime;
		
		/**
		 * @param timeout
		 */
		public DefaultListener(long startTime, int timeout) {
			super();
			this.startTime = startTime;
			this.timeout = timeout;
		}

		/* (non-Javadoc)
		 * @see net.kylindb.analyser.timer.timer.Task#run()
		 */
		@Override
		public void run() {
			long end = System.currentTimeMillis();
			// 做点无用功
			int count = 0;
			for (int i = 0; i < 10000; i++){
				count = count + i;
			}
			System.out.println("start Listener:" + timeout + ", elapse:" + (end - startTime));
		}
		
	}
	
	public static void main (String[] args){
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		for (int k = 0; k < 50; k++){
			executorService.execute(new Runnable(){

				@Override
				public void run() {
					test();
				}
				
			});
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Thread.yield();
	}
	
	private static void test(){
//		System.out.println(System.currentTimeMillis());
		WheelTimer wheelTimer = WheelTimer.getInstance();
		wheelTimer.appendTimer(10, new DefaultListener(System.currentTimeMillis(), 10)).appendTimer(15, new DefaultListener(System.currentTimeMillis(), 15));
//		try {
//			Thread.sleep(618);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		try {
//			Thread.sleep(55 * 1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		long timerId60 = wheelTimer.addTimer(60, new DefaultListener(System.currentTimeMillis(), 60));
		long timerId90 = wheelTimer.addTimer(90, new DefaultListener(System.currentTimeMillis(), 90));
		
//		for (int i=0; i < 5; i++){
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			wheelTimer.updateTimer(timerId60, 0);
//		}
//		wheelTimer.expireTimer(timerId90);
//		wheelTimer.deleteTimer(timerId90);
		wheelTimer.addTimer(12, new DefaultListener(System.currentTimeMillis(), 12));
		wheelTimer.addTimer(35, new DefaultListener(System.currentTimeMillis(), 35));
		try {
			Thread.sleep(300000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Map<Long, Timer> map = wheelTimer.stop();
//		System.out.println("map size:" + map.size());
	}

}
