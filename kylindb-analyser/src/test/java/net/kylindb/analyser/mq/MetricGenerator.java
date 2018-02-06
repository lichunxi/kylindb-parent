/**
 * Created:2017年9月18日 下午2:51:08
 * Author:lichunxi
 * <http://www.kylinyun.com> ®All Rights Reserved
 */
package net.kylindb.analyser.mq;

import java.util.Properties;
import java.util.Random;

import net.kylindb.analyser.mq.MetricDatum.MetricDatumRequest;
import net.kylindb.coder.ValueCoder;

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
	public static Logger LOG = LoggerFactory.getLogger(MetricGenerator.class);

	private static Producer<String, byte[]> producer = null;

	public static void main(String[] args) {

		Properties props = new Properties();
		props.put("bootstrap.servers",
				"hzzh-dev-72.ynycloud.com:9092,hzzh-dev-73.ynycloud.com:9092,hzzh-dev-74.ynycloud.com:9092");
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

		generate();

		LOG.info("complete, try to close producer");
		producer.close();
	}

	public static void generate() {
		LOG.info("start to generate event message.");
		Random random = new Random();

		for (Long pointId = 100000000L; pointId < 100000100L; pointId++) {
			Long time = System.currentTimeMillis();
			MetricDatumRequest.Builder requestBuilder = MetricDatumRequest
					.newBuilder();
			requestBuilder.setPointId(pointId)
					.setTimestamp(time)
					.setValue(ByteString.copyFrom(ValueCoder.encode(random.nextInt() > 0 ? 1 : 0)));
			System.out.println("===========time: " + time);
			send("events_connection", String.valueOf(pointId), requestBuilder.build().toByteArray());
		}
	}

	public static void send(String topic, String key, byte[] message) {
		LOG.debug("send to kafka: topic:{}, count:{}", topic, 50);
		producer.send(new ProducerRecord<String, byte[]>(topic, key, message),
				new Callback() {
					public void onCompletion(RecordMetadata metadata,
							Exception e) {
						if (e != null) {
							LOG.error("send kafka fail", e);
						}
					}
				});
	}

}
