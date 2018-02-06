/**
 * 
 */
package net.kylindb.timer;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.kylindb.timer.Task;
import net.kylindb.timer.Timer;
import net.kylindb.timer.WheelTimer;


/**
 * 
 * 性能测试，100万timer
 * @author lichunxi
 *
 */
public class TimerPerformanceTest {
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
			int count = 0;
			for (int i = 0; i < 10000; i++){
				count = count + i;
			}
			if (end - startTime - timeout*1000 > 100){ //误差不应该超过1秒
				System.out.println("start Listener:" + timeout + ", elapse:" + (end - startTime));
			}
		}
		
	}
	
	public static void main (String[] args){
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (int k = 0; k < 5000; k++){
			executorService.execute(new Runnable(){

				@Override
				public void run() {
					test();
				}
				
			});
			
			try {
				Thread.sleep(100);   //不要凑到同一毫秒，timerId容易冲突
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		while(true){
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<Long, Timer> map = WheelTimer.getInstance().timerMap;
			System.out.println("===========================================map size:" + map.size());
		}
		
//		Thread.yield();
	}
	
	private static void test(){
		WheelTimer wheelTimer = WheelTimer.getInstance();
		for (int t = 60; t < 1260; t=t+60){
			wheelTimer.appendTimer(t, new DefaultListener(System.currentTimeMillis(), t));
		}
	}

}
