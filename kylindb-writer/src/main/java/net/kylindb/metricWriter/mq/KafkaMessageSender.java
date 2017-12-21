/**
 * Created:2017年12月6日 下午4:09:08
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricWriter.mq;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.kylindb.coder.ValueCoder;
import net.kylindb.metricWriter.mq.MetricData.MetricDataRequest;
import net.kylindb.metricWriter.mq.MetricData.MetricDataRequest.MetricDatum.MapFieldEntry;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.protobuf.ByteString;

/**
 * @author lichunxi
 *
 */
@Service
public class KafkaMessageSender {
	public static Logger LOG = LoggerFactory
			.getLogger(KafkaMessageSender.class);
	public static final String TOPIC = "metrics_retry";
	
	private static Producer<byte[], byte[]> producer = null;
	
    /**
     * kafka集群地址
     */
    @Value("${kafka.cluster.address}")
    private String kafkaCluster;

    @PostConstruct
    public void init() {
    	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    	scheduler.schedule(new Runnable() {
            @Override
            public void run() {
		    	Properties props = new Properties();
				props.put("bootstrap.servers", kafkaCluster);  
				props.put("acks", "all");
				props.put("retries", 3);
				props.put("batch.size", 4096); // 4K
				props.put("linger.ms", 10); // 10毫秒
				props.put("buffer.memory", 134217728); // 128M
				props.put("key.serializer",
						"org.apache.kafka.common.serialization.ByteArraySerializer");
				props.put("value.serializer",
						"org.apache.kafka.common.serialization.ByteArraySerializer");
		
				producer = new KafkaProducer<byte[], byte[]>(props);
            }
        }, 10, TimeUnit.SECONDS);
    }
    
    @PreDestroy
    public void destroy() {
    	producer.close();
    }
    
	public void send(Long pointId, Long timestamp, Object value,
			Map<String, String> notes) {
		try {
			MetricDataRequest.MetricDatum.Builder dataBuilder = MetricDataRequest.MetricDatum.newBuilder();
			dataBuilder.setPointId(Long.valueOf(pointId)).setTimestamp(timestamp)
					.setValue(ByteString.copyFrom(ValueCoder.encode(value)));
			if (null != notes && notes.size() > 0) {
				for (Entry<String, String> entry : notes.entrySet()) {
					MapFieldEntry.Builder noteBuilder = MapFieldEntry.newBuilder();
					noteBuilder.setKey(entry.getKey());
					noteBuilder.setValue(entry.getValue());
					dataBuilder.addNotes(noteBuilder);
				}
			}
			MetricDataRequest.Builder requestBuilder = MetricDataRequest.newBuilder();
			requestBuilder.addMetricData(dataBuilder.build());
	
			producer.send(new ProducerRecord<byte[], byte[]>(TOPIC, requestBuilder.build().toByteArray()), new Callback() {
				public void onCompletion(RecordMetadata metadata, Exception e) {
					if (e != null) {
						LOG.error("send retry metric to kafka fail", e);
					}
				}
			});
		} catch (Exception e){
			LOG.error("send exception.", e);
		}
	}
    
}
