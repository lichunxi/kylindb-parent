/**
 * Created:2018年1月11日 下午1:37:21
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.analyser;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

/**
 * @author lichunxi
 *
 */
@Service
public class ConnectionAnalyser {
	public static Logger LOG = LoggerFactory
			.getLogger(ConnectionAnalyser.class);
	/**
	 * 时间窗口大小
	 */
	public static final Integer WINDOW_SIZE = 1;
	
//	private ScheduledExecutorService timerService = Executors.newSingleThreadScheduledExecutor();
	
	/**
     * kafka集群地址
     */
    @Value("${kafka.cluster.address}")
	private String kafkaBrokers;

    private static AtomicBoolean hasStarted = new AtomicBoolean(false);

    @PostConstruct
	public void init() {
		synchronized(ConnectionAnalyser.class){
			if (false == hasStarted.get()){
				LoggerContext loggerContext= (LoggerContext) LoggerFactory.getILoggerFactory();  
		        //设置全局日志级别
		        ch.qos.logback.classic.Logger logger=loggerContext.getLogger("root");  
		        logger.setLevel(Level.toLevel("INFO")); 
				
				final Properties streamsConfiguration = new Properties();
				// 配置信息
				streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "dtu-connect-analyser");
				streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "dtu-connect-analyser-client");
				streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
//						streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "hzzh-dev-72.ynycloud.com:9092,hzzh-dev-73.ynycloud.com:9092,hzzh-dev-74.ynycloud.com:9092");
				// 默认的消息编解码类
				streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
				streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

				// Set up serializers and deserializers, which we will use for
				// overriding the default serdes
				// specified above.
				final Serde<String> stringSerde = Serdes.String();
				final Serde<byte[]> bytesSerde = Serdes.ByteArray();
//				final Serde<Long> longSerde = Serdes.Long();
//				final Serde<Integer> integerSerde = Serdes.Integer();

				// In the subsequent lines we define the processing topology of
				// the Streams application.
				final StreamsBuilder builder = new StreamsBuilder();
				
				// 从topic消费消息
				KStream<String, byte[]> messageStream = builder.stream("events_connection", Consumed.with(stringSerde, bytesSerde));
//				messageStream.foreach((key, value) -> System.out.println(key + "=====>>>>>" + value));
				messageStream.process(() -> new ConnectionEventProcessor());
				
//						.windowedBy(TimeWindows.of(TimeUnit.MINUTES.toMillis(WINDOW_SIZE)).until(TimeUnit.MINUTES.toMillis(WINDOW_SIZE)))
//						.aggregate(
//								() -> 0L,
//								(aggregateKey, message, aggregateValue) -> {
//									// 处理session内消息到来时应该采取的逻辑
//									// aggregateKey为sessionKey
//									// message为session内新来的消息
//									// aggregateValue为session累计的值
//									Integer value = getValue(message);
//									System.out.println("------:" + value);
//									if (null != value){
//										return value + aggregateValue;
//									} else {
//										return aggregateValue;
//									}
//								},
//								(aggregateKey, leftAggregateValue, rightAggregateValue) -> {
//									// 处理session合并时应该采取的逻辑
//									// aggregateKey为sessionKey
//									// leftAggregateValue为前一个session内累计的值
//									// rightAggregateValue为后一个session内累计的值
//									return leftAggregateValue + rightAggregateValue;
//								},
//								Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("aggregated-events-connection-store") /* state store name */
//						        .withValueSerde(Serdes.Long()))
//						        .toStream()
//						        .foreach((key, value) -> System.out.println(key + "=====>>>>>" + value));
//								.to("events_connection_judgement", Produced.with(Serdes.serdeFrom(
//								        new WindowedSerializer<>(stringSerde.serializer()),
//								        new WindowedDeserializer<>(stringSerde.deserializer())), longSerde));
				
				final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);
				
				streams.cleanUp();
			    streams.start();

			    
//				timerService.scheduleAtFixedRate(() -> {
//					try{
//						ReadOnlyWindowStore<String, Long> keyValueStore = waitUntilStoreIsQueryable("aggregated-events-connection-store", QueryableStoreTypes.windowStore(), streams);
//
//						// 每隔5分钟检查时间窗口内的聚合数据是否达到阈值，达到则发出事件
//						Long current = System.currentTimeMillis();
//						WindowStoreIterator<Long> range = keyValueStore.fetch("100000000", 0, current);
//						while (range.hasNext()) {
//							KeyValue<Long, Long> next = range.next();
//							System.out.println("count for " + next.key + ": " + next.value);
////							KafkaSender.send("events_connection_judgement", null, next.value);
//						}
//						System.out.println("李春喜=========== ");
//					} catch (Exception e){
//				    	LOG.error("get store fail.", e);
//				    }
//				}, 1, WINDOW_SIZE, TimeUnit.MINUTES);
			    
			    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
			    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
				hasStarted.set(true);
				LOG.info("init success.......");
			}
		}
	}
	
	public static <T> T waitUntilStoreIsQueryable(final String storeName,
			final QueryableStoreType<T> queryableStoreType,
			final KafkaStreams streams) throws InterruptedException {
		while (true) {
			try {
				return streams.store(storeName, queryableStoreType);
			} catch (InvalidStateStoreException ignored) {
				// store not yet ready for querying
				Thread.sleep(100);
			}
		}
	}
	
//	private Integer getValue(byte[] message){
//		try {
//			MetricDatumRequest request = MetricDatumRequest.parseFrom(message);
//			Integer value = (Integer)ValueCoder.decode(request.getValue().toByteArray());
//			return value;
//		} catch (Exception e) {
//			LOG.error("parse and get value fail", e);
//			return null;
//		}
//	}

	@PreDestroy
    public void destroy() {
		
	}
	
	
}
