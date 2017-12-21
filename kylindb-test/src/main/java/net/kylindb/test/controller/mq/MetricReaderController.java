/**
 * Created:2017年9月18日 下午2:51:08
 * Author:lichunxi
 * <http://www.kylinyun.com> ®All Rights Reserved
 */
package net.kylindb.test.controller.mq;

import java.net.URI;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lichunxi
 *
 */
@RequestMapping("test")
@Controller
public class MetricReaderController {
	public static Logger LOG = LoggerFactory
			.getLogger(MetricReaderController.class);
	
	private AtomicLong count = new AtomicLong(0L);
	private AtomicBoolean start = new AtomicBoolean(false);

	@RequestMapping("queryMetric")
	public void query(int size) {
		LOG.info("start to test query message.");
		// 5个线程
		int threadSize = size;
		Thread[] threads = new Thread[threadSize];
		for (int i = 0; i < threadSize; i++){
			threads[i] = new Thread(new Runnable(){
				public void run(){
					PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
					CloseableHttpClient httpclient = HttpClients.custom()
				            .setConnectionManager(cm)
				            .build();
					ExecutorService querys = Executors.newFixedThreadPool(2);
					Random rnd = new Random();
					
					// 获取1天的数据量
					int times = 0;
					while(times < 100000){
						executeQuery(httpclient, querys, rnd);
						times++;
						try {
							Thread.sleep(10L);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}
		
		for (int i = 0; i < threadSize; i++){
			threads[i].start();
		}
		

		if (!start.get()){
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable(){
				public void run(){
					LOG.info("====count:{}", count.get());
				}
			}, 0, 10, TimeUnit.SECONDS);
			start.set(true);
		}
		
		for (int i = 0; i < threadSize; i++){
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	private String getPointId(Random rnd){
		int delta = rnd.nextInt(100000);
		return String.valueOf(200000000L + delta);
	}
	
	private void executeQuery(CloseableHttpClient httpclient, ExecutorService querys, Random rnd){
			int num=0;
			while(num < 200){
				try {
					final String pointId = getPointId(rnd);
					URI uri = new URIBuilder()
							.setScheme("http")
							.setHost("121.42.144.232")
							.setPort(48088)
							.setPath("/kylindb-reader-1.0.0-SNAPSHOT/metric/queryMetric")
							.setParameter("pointIds", pointId)
							.setParameter("start", "2017-12-01 00:00:00")
							.setParameter("end", "2017-12-02 00:00:00")
							.setParameter("withNotes", "true").build();
					HttpGet httpget = new HttpGet(uri);
					ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
						public String handleResponse(HttpResponse response){
							int status = response.getStatusLine().getStatusCode();
							if (status == 200) {
								count.incrementAndGet();
	//							LOG.info("query success, pointId:{}, count={}", pointId, count.get());
	//							LOG.info("body:{}", EntityUtils.toString(response.getEntity()));
								return null;
							} else {
								LOG.warn("query fail, pointId:{}", pointId);
								return null;
							}
						}
					};
					httpclient.execute(httpget, responseHandler);
				} catch (Exception e) {
					LOG.error("query except.", e);
				}
				num++;
			}
	}

}
