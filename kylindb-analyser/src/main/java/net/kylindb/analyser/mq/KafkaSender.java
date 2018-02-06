/**
 * Created:2018年1月17日 上午10:17:34
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.analyser.mq;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * @author lichunxi
 *
 */
@Service
public class KafkaSender {
	public static Logger LOG = LoggerFactory.getLogger(KafkaSender.class);
	
	private static AtomicBoolean hasStarted = new AtomicBoolean(false);
	private static Producer<String, byte[]> producer = null;
	
	/**
     * kafka集群地址
     */
    @Value("${kafka.cluster.address}")
	private String kafkaBrokers;
	
	@PostConstruct
	public void init(){
		synchronized(KafkaSender.class){
			if (false == hasStarted.get()){
				Properties props = new Properties();
				props.put("bootstrap.servers", kafkaBrokers);
				props.put("acks", "all");
				props.put("retries", 3);
				props.put("batch.size", 4096); // 4K
				props.put("linger.ms", 10); // 10毫秒
				props.put("buffer.memory", 134217728); // 128M
				props.put("key.serializer",
						"org.apache.kafka.common.serialization.StringSerializer");
				props.put("value.serializer",
						"org.apache.kafka.common.serialization.ByteArraySerializer");

				producer = new KafkaProducer<String, byte[]>(props);				
				hasStarted.set(true);
			}
		}
	}
	
	public static void send(String topic, String key, byte[] message) {
		producer.send(new ProducerRecord<String, byte[]>(topic, key, message),
				new Callback() {
					public void onCompletion(RecordMetadata metadata,
							Exception e) {
						if (e != null) {
							LOG.error("send message to kafka fail", e);
						}
					}
				});
	}

	@PreDestroy
	public void destroy(){
		hasStarted.set(false);
		producer.close();
	}
}
