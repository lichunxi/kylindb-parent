/**
 * Created:2017年9月18日 下午2:51:08
 * Author:lichunxi
 * <http://www.kylinyun.com> ®All Rights Reserved
 */
package net.kylindb.metricWriter.mq;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import net.kylindb.coder.ValueCoder;
import net.kylindb.metricWriter.mq.MetricData.MetricDataRequest;
import net.kylindb.metricWriter.mq.MetricData.MetricDataRequest.MetricDatum;
import net.kylindb.metricWriter.mq.MetricData.MetricDataRequest.MetricDatum.MapFieldEntry;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;


/**
 * @author lichunxi
 *
 */
public class MetricGenerator {
	public static Logger LOG = LoggerFactory
			.getLogger(MetricGenerator.class);
	private static final String namespace = "IOE.electricity";
	private static final String [] pointIds = new String[] {"21218335351398119","32383551089889","86322458679","327351089209","2652458979","427286389287","2623458975","472862392657", "9612458972","1272638420"}; 
	private static final String startTime = "2017/10/01 00:00:00";  //yyyy/MM/dd-HH:mm:ss
	private static final String endTime = "2017/10/05 00:00:00";  //yyyy/MM/dd-HH:mm:ss
	//间隔周期，单位：毫秒
	private static final Long interval = 60 * 1000L; 
	
	private static Producer<String, byte[]> producer = null;
	private static ExecutorService workers = Executors.newFixedThreadPool(2);
	
	public static void main(String[] args){
		
		Properties props = new Properties();
		props.put("bootstrap.servers", "hzzh-dev-72.ynycloud.com:9092,hzzh-dev-73.ynycloud.com:9092,hzzh-dev-74.ynycloud.com:9092");  
		props.put("acks", "all");
		props.put("retries", 3);
		props.put("batch.size", 4096); // 4K
		props.put("linger.ms", 10); // 10毫秒
		props.put("buffer.memory", 134217728); // 128M
		props.put("key.serializer",
				"org.apache.kafka.common.serialization.ByteArraySerializer");
		props.put("value.serializer",
				"org.apache.kafka.common.serialization.ByteArraySerializer");

		producer = new KafkaProducer<String, byte[]>(props);

		generate();
		
		try{
			Thread.sleep(100 * 1000L);
		} catch (Exception e){
			LOG.error("thread sleep except", e);
		}
	
		LOG.info("complete, try to close producer");
		producer.close();
	}
	
	public static void generate() {
		LOG.info("start to generate message.");
		DateFormat timeformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Long startTimestamp=0L;
		Long endTimestamp=0L;
		try {
			startTimestamp = timeformat.parse(startTime).getTime();
			endTimestamp = timeformat.parse(endTime).getTime();
			System.out.println("startTime:" + startTimestamp);
			System.out.println("endTime:" + endTimestamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		Random random = new Random();
		Long startTime = System.currentTimeMillis();
		for (long time = startTimestamp; time < endTimestamp; time=time+interval){
			for (Long i = 10000000L; i < 10050000; i=i+50){
				// 每50条拼装一条protobuf消息
				List<MetricDataRequest.MetricDatum> dataList = new ArrayList<MetricDataRequest.MetricDatum>();
				Long start = i;
				Long end = i + 50;
				for (Long pointId = start; pointId < end; pointId++){
					MetricDataRequest.MetricDatum.Builder dataBuilder = MetricDataRequest.MetricDatum.newBuilder();
					dataBuilder.setPointId(pointId)
						.setTimestamp(time)
						.setValue(ByteString.copyFrom(ValueCoder.encode(random.nextInt(600))));
					MapFieldEntry.Builder maxTimeBuilder = MapFieldEntry.newBuilder();
					maxTimeBuilder.setKey("maxTime");
					maxTimeBuilder.setValue(String.valueOf(time));
					dataBuilder.addNotes(maxTimeBuilder.build());
					MapFieldEntry.Builder actionBuilder = MapFieldEntry.newBuilder();
					actionBuilder.setKey("action");
					actionBuilder.setValue("lichunxi99");
					dataBuilder.addNotes(actionBuilder.build());
					dataList.add(dataBuilder.build());
				}
				MetricDataRequest.Builder requestBuilder = MetricDataRequest.newBuilder();
				requestBuilder.addAllMetricData(dataList);
				send(generateTopic(namespace), requestBuilder.build().toByteArray());
				if (i % 5 == 0){
					try{
						Thread.sleep(10L);
					} catch (Exception e){
						LOG.error("thread sleep except", e);
					}
				}
			}
		}
		
		LOG.info("generate message in {} milliseconds", System.currentTimeMillis() - startTime);
	}
	
	public static void send(String topic, byte[] message) {
		workers.execute(() -> {
			LOG.debug("send to kafka: topic:{}, count:{}", topic, 50);
			producer.send(new ProducerRecord<String, byte[]>(topic, message),
					new Callback() {
						public void onCompletion(RecordMetadata metadata,
								Exception e) {
							if (e != null) {
								LOG.error("send kafka fail", e);
							}
						}
					});
		});
	}
	
	private static final String generateTopic(String namespace) {
		StringBuilder buf = new StringBuilder("metrics_");
		buf.append(namespace);
		return buf.toString();
	}
}
