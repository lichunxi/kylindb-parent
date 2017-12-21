/**
 * Created:2017年9月4日 下午5:25:19
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricWriter.mq;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import net.kylindb.coder.ValueCoder;
import net.kylindb.metricWriter.mq.MetricData.MetricDataRequest.MetricDatum.MapFieldEntry;
import net.kylindb.client.IMetricDatumWriter;
import net.kylindb.stat.domain.RequestMetrics;
import net.kylindb.stat.service.RequestMetricsService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stumbleupon.async.Deferred;


/**
 * @author lichunxi
 *
 */
@Service
public class KafkaMessageConsumer {
    /**
     * slector线程等待时间间隔，单位：毫秒
     */
    public static final Long TIMEOUT = 1000L;
    
	public static Logger LOG = LoggerFactory
			.getLogger(KafkaMessageConsumer.class);
	
	private static final Long MAX_SLEEP_TIME = 10 * 1000L;
	private static final Long MIN_SLEEP_TIME = 1L;
	private static AtomicLong sleepTime = new AtomicLong(MIN_SLEEP_TIME);
	
	/**
	 * 单个Kafka消费线程
	 */
	private Thread selector = null;
	private UncaughtExceptionHandler uncaughtExceptionHandler = null;
	
	
	private static KafkaConsumer<byte[], byte[]> consumer = null;
	/**
     * 消费线程是否启动
     */
    private AtomicBoolean started = new AtomicBoolean(false);
    
    /**
     * kafka集群地址
     */
    @Value("${kafka.cluster.address}")
    private String kafkaCluster;
    
    @Resource
    private IMetricDatumWriter writer;
    
    @Resource
    private KafkaMessageSender sender;

    @PostConstruct
    public void init() {
    	// 先把spring环境初始化完毕后，再初始化kafka
    	// 使用定时任务启动kafka初始化
    	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    	scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                Properties props = new Properties();
                props.put("bootstrap.servers", kafkaCluster); // kafka集群
                props.put("group.id", "metricDataWriter");
                props.put("enable.auto.commit", "false");  //手工控制offset
//                props.put("enable.auto.commit", "true");
//                props.put("auto.commit.interval.ms", "1000");
                // http://kafka.apache.org/0102/documentation.html#configuration
				/**
				 * The timeout used to detect consumer failures when using
				 * Kafka's group management facility. The consumer sends
				 * periodic heartbeats to indicate its liveness to the broker.
				 * If no heartbeats are received by the broker before the
				 * expiration of this session timeout, then the broker will
				 * remove this consumer from the group and initiate a rebalance.
				 * Note that the value must be in the allowable range as
				 * configured in the broker configuration by
				 * group.min.session.timeout.ms and
				 * group.max.session.timeout.ms.
				 * group.max.session.timeout.ms默认值为30000
				 */
                props.put("session.timeout.ms", "30000"); 
                
				/**
				 * The configuration controls the maximum amount of time the
				 * client will wait for the response of a request. If the
				 * response is not received before the timeout elapses the
				 * client will resend the request if necessary or fail the
				 * request if retries are exhausted.
				 * request.timeout.ms必须比session.timeout.ms大
				 */
				props.put("request.timeout.ms", "35000");
                
				/**
				 * The expected time between heartbeats to the consumer
				 * coordinator when using Kafka's group management facilities.
				 * Heartbeats are used to ensure that the consumer's session
				 * stays active and to facilitate rebalancing when new consumers
				 * join or leave the group. The value must be set lower than
				 * session.timeout.ms, but typically should be set no higher
				 * than 1/3 of that value. It can be adjusted even lower to
				 * control the expected time for normal rebalances.
				 * 默认3000
				 */
                props.put("heartbeat.interval.ms", "6000"); 
                props.put("key.deserializer",
                        "org.apache.kafka.common.serialization.ByteArrayDeserializer");
                props.put("value.deserializer",
                        "org.apache.kafka.common.serialization.ByteArrayDeserializer");
                consumer = new KafkaConsumer<byte[], byte[]>(props);
                // 订阅所有以metric/开头的topic
                Pattern pattern = Pattern.compile("^metrics_.*");

                consumer.subscribe(pattern, new NoOpConsumerRebalanceListener());

                selector = new Thread(new MessageSelector(), "kafkaConsumer");
                uncaughtExceptionHandler = (Thread t, Throwable e)->{
                	LOG.warn("uncaught exception, restart kafkaConsumer.", e);
                	// 重新启动线程
                	selector = new Thread(new MessageSelector(), "kafkaConsumer");
                	selector.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                	selector.start();
                };
                selector.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                selector.start();
                LOG.info("kafka consumer start successfully.");
            }
        }, 10, TimeUnit.SECONDS);
    }
    
    @PreDestroy
    public void destroy() {
    	started.set(false);
        consumer.close();
        if (null != selector){
        	started.set(false);
        }
    }
	
	public class MessageSelector implements Runnable {
		
		public void run() {
			started.set(true);
			while (started.get()) {
				try{
					ConsumerRecords<byte[], byte[]> records = consumer.poll(TIMEOUT);
					if (!records.isEmpty()){
						try {
							List<Deferred<Object>> deferredList = new ArrayList<Deferred<Object>>();
							
							for (ConsumerRecord<byte[], byte[]> record : records) {
								deferredList.add(onReceive(record.value()));
							}
							Deferred.group(deferredList).addCallback((ArrayList<Object> result) -> {
								// 手工commit
								consumer.commitSync();
								int successNum = 0;
								for (Object num : result){
									successNum +=(Integer)num;
								}
								return successNum;
							}).addErrback((Exception eb) -> {
								LOG.warn("consume fail, try to deal next poll.", eb);
								return 0;
							}).joinUninterruptibly(10 * 1000L);
							//LOG.info("consume {} records successfully at {}.", count, timeformat.format(new Date()));
						} catch (Exception e) {
							LOG.error("except in consume.", e);
						}
					}
					
					Thread.sleep(sleepTime.get());   // 消费速度调节
				} catch (Exception e) {
					LOG.warn("except in consume loop.", e);
				}
			}
		}
	}

	public Deferred<Object> onReceive(byte[] message) {
		RequestMetrics stat = RequestMetricsService.getInstance("onReceive");
		stat.start();
		int count = 0;
		try {
			List<MetricData.MetricDataRequest.MetricDatum> protoMetricDatumList = MetricData.MetricDataRequest.parseFrom(message).getMetricDataList();
			for (MetricData.MetricDataRequest.MetricDatum metricDatum : protoMetricDatumList){
				Long pointId = metricDatum.getPointId();
				Long timestamp = metricDatum.getTimestamp();
				Object value = ValueCoder.decode(metricDatum.getValue().toByteArray());
				Map<String, String> notes = new HashMap<String, String>();
				List<MapFieldEntry> notesList = metricDatum.getNotesList();
				if (null != notesList){
					for (MapFieldEntry entry : notesList){
						notes.put(entry.getKey(), entry.getValue());
					}
				}
				writer.putMetricDatum(pointId, timestamp, value, notes).addErrback((Exception e) ->{
					// 其他原因产生的错误，可能是本节点错误，例如数据库连接问题，应该发送到kafka，后期再次消费（很可能由其他节点处理）
					// 保证消息不丢失。如果发送kafka失败，呵呵呵，不会这么倒霉，认命吧
					handleFail(pointId, timestamp, value, notes);
					return 0;
				}).addCallback(r ->{
					// 应该调节消费速度
					speedup();
					return 1;
				});
				count++;
			}
			stat.end();
			return Deferred.fromResult(count);
		} catch (Exception e){
			// 消息格式错误,解析错误，仅记录日志，该条消息会丢失
			LOG.warn("parse protobuf message fail, please check your data producer", e);
			stat.fail();
			return Deferred.fromResult(count);
		}
	}

	/**
	 * @param pointId
	 * @param timestamp
	 * @param value
	 * @param notes
	 */
	private void handleFail(Long pointId, Long timestamp, Object value,
			Map<String, String> notes) {
		LOG.info("put metric fail, try again, there will be no data missing");
		if (LOG.isDebugEnabled()){
			String notesStr = null;
			if (null != notes && notes.size() > 0){
				StringBuilder buf = new StringBuilder();
				for (Entry<String, String> entry : notes.entrySet()){
					buf.append(entry.getKey());
					buf.append("=");
					buf.append(entry.getValue());
					buf.append(",");
				}
				buf.deleteCharAt(buf.length() - 1);
				notesStr = buf.toString();
			}
			LOG.debug("metric is:{} {} {} {}", pointId, timestamp, value, notesStr);
		}
		// 1:重发
		writer.putMetricDatum(pointId, timestamp, value, notes).addErrback((Exception e) ->{
			// 2:重发不成功，则写入kafka，交与其他正常的节点进行消费
			// 此时应该降低消费的速率
			slowDown();
			LOG.info("try again fail, send to kafka");
			sender.send(pointId, timestamp, value, notes);
			return Deferred.fromResult(0);
		});
	}
	
	private void slowDown(){
		// 最多调节到10240毫秒
		if (sleepTime.get() < MAX_SLEEP_TIME){
			sleepTime.set(sleepTime.get() * 2);
			LOG.info("now sleep time is {} milliseconds", sleepTime.get());
		}
	}
	
	private void speedup(){
		// 最多调节到1毫秒
		if (sleepTime.get() > MIN_SLEEP_TIME){
			sleepTime.set(sleepTime.get() / 2);
			LOG.info("now sleep time is {} milliseconds", sleepTime.get());
		}
	}
}
