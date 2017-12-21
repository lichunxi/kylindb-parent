/**
 * Created:2017年9月18日 下午2:51:08
 * Author:lichunxi
 * <http://www.kylinyun.com> ®All Rights Reserved
 */
package net.kylindb.test.controller.mq;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.kylindb.coder.ValueCoder;
import net.kylindb.test.controller.mq.MetricData.MetricDataRequest;
import net.kylindb.test.controller.mq.MetricData.MetricDataRequest.MetricDatum.MapFieldEntry;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.protobuf.ByteString;


/**
 * @author lichunxi
 *
 */
@RequestMapping("test")
@Controller
public class MetricGeneratorController {
	public static Logger LOG = LoggerFactory
			.getLogger(MetricGeneratorController.class);
	private static final String namespace = "IOE.electricity";
	//间隔周期，单位：毫秒
	private static final Long interval = 60 * 1000L; 
	
    /**
     * kafka集群地址
     */
    @Value("${kafka.cluster.address}")
    private String kafkaCluster;
	
	private static Producer<String, byte[]> producer = null;
	private static ExecutorService generators = Executors.newFixedThreadPool(10);
//	private static ExecutorService readers = Executors.newFixedThreadPool(10);
	private static AtomicLong count = new AtomicLong(0L);
	@PostConstruct
	public void init(){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.schedule(new Runnable(){
			public void run(){
				Properties props = new Properties();
				props.put("bootstrap.servers", kafkaCluster);  
				props.put("acks", "1");
				props.put("retries", 3);
				props.put("batch.size", 4096); // 4K
				props.put("linger.ms", 10); // 10毫秒
				props.put("buffer.memory", 134217728); // 128M
				props.put("key.serializer",
						"org.apache.kafka.common.serialization.ByteArraySerializer");
				props.put("value.serializer",
						"org.apache.kafka.common.serialization.ByteArraySerializer");
	
				producer = new KafkaProducer<String, byte[]>(props);
			}
		}, 10, TimeUnit.SECONDS);
	}
	
	@PreDestroy
	public void destroy(){
		LOG.info("complete, try to close producer");
		producer.close();
	}
	
	//yyyy/MM/dd-HH:mm:ss
	@RequestMapping("generateMetric")
	public static void generate(String startTime, String endTime) {
		LOG.info("start to generate message.");
		DateFormat timeformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Long startTimestamp=0L;
		Long endTimestamp=0L;
		try {
			startTimestamp = timeformat.parse(startTime).getTime();
			endTimestamp = timeformat.parse(endTime).getTime();
			LOG.info("startTime:" + startTimestamp);
			LOG.info("endTime:" + endTimestamp);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		for (long time = startTimestamp; time < endTimestamp; time=time+interval){
			generateMessage(time);
		}
	}
	
	/**
	 * @param time
	 */
	private static void generateMessage(final long time) {
		generators.execute(new Runnable(){
			public void run(){
				Random random = new Random();
	//			Long startPos = System.currentTimeMillis();
				for (Long i = 100000000L; i < 100100000; i=i+50){  //10万个pointId
					// 每50条拼装一条protobuf消息
					List<MetricDataRequest.MetricDatum> dataList = new ArrayList<MetricDataRequest.MetricDatum>();
					Long start = i;
					Long end = i + 50;
					for (Long pointId = start; pointId < end; pointId++){
						MetricDataRequest.MetricDatum.Builder dataBuilder = MetricDataRequest.MetricDatum.newBuilder();
						Integer value = random.nextInt(600);
						dataBuilder.setPointId(pointId)
							.setTimestamp(time)
							.setValue(ByteString.copyFrom(ValueCoder.encode(value)));
						if (value > 500){
							MapFieldEntry.Builder maxTimeBuilder = MapFieldEntry.newBuilder();
							maxTimeBuilder.setKey("max");
							maxTimeBuilder.setValue(String.valueOf(time));
							dataBuilder.addNotes(maxTimeBuilder.build());
							MapFieldEntry.Builder actionBuilder = MapFieldEntry.newBuilder();
							actionBuilder.setKey("act");
							actionBuilder.setValue("98888");
							dataBuilder.addNotes(actionBuilder.build());
						}
						dataList.add(dataBuilder.build());
					}
					MetricDataRequest.Builder requestBuilder = MetricDataRequest.newBuilder();
					requestBuilder.addAllMetricData(dataList);
					send(generateTopic(namespace), requestBuilder.build().toByteArray());
					try{
						Thread.sleep(10L);
					} catch (Exception e){
						LOG.error("thread sleep except", e);
					}
				}
				LOG.info("count={}, time={}", count.get(), time);
			}
		});
	}

	public static void send(String topic, byte[] message) {
		LOG.debug("send to kafka: topic:{}, count:{}", topic, 50);
		producer.send(new ProducerRecord<String, byte[]>(topic, message),
				new Callback() {
					public void onCompletion(RecordMetadata metadata,
							Exception e) {
						if (e != null) {
							LOG.error("send kafka fail", e);
						} else {
							count.incrementAndGet();
						}
					}
				});
	}
	
	private static final String generateTopic(String namespace) {
		StringBuilder buf = new StringBuilder("metrics_");
		buf.append(namespace);
		return buf.toString();
	}
}
