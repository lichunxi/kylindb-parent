/**
 * Created:2018年1月15日 下午2:29:06
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.analyser.mq;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import net.kylindb.analyser.mq.MetricDatum.MetricDatumRequest;
import net.kylindb.coder.ValueCoder;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author lichunxi
 *
 */
public class MetricConsumer {
	public static Logger LOG = LoggerFactory
			.getLogger(MetricConsumer.class);
	
	private static KafkaConsumer<byte[], byte[]> consumer = null;
    
	public static void main(String[] args){
		Properties props = new Properties();
        props.put("bootstrap.servers", "hzzh-dev-72.ynycloud.com:9092,hzzh-dev-73.ynycloud.com:9092,hzzh-dev-74.ynycloud.com:9092"); // kafka集群
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
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
        Pattern pattern = Pattern.compile("^events_connection.*");

        consumer.subscribe(pattern, new NoOpConsumerRebalanceListener());

        while (true) {
			try{
				ConsumerRecords<byte[], byte[]> records = consumer.poll(1000L);
				if (!records.isEmpty()){
					try {
						for (ConsumerRecord<byte[], byte[]> record : records) {
							MetricDatumRequest request = MetricDatumRequest.parseFrom(record.value());
							System.out.println("================pointId:" + request.getPointId());
							System.out.println("================timestamp:" + request.getTimestamp());
							System.out.println("================value:" + ValueCoder.decode(request.getValue().toByteArray()));
						}
					} catch (Exception e) {
						LOG.error("except in consume.", e);
					}
				}
			} catch (Exception e) {
				LOG.warn("except in consume loop.", e);
			}
		}
	}
}
