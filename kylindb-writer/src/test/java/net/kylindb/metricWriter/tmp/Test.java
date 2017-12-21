/**
 * Created:2017年12月15日 下午2:13:35
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricWriter.tmp;

/**
 * @author lichunxi
 *
 */
public class Test {

	public static void main(String[] args){
		Thread t = new Thread(()->{
			try {
				Thread.sleep(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("xxxxxx");
		});
		t.start();
	}
}
