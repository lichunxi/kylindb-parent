/**
 * Created:2018年1月17日 下午5:17:45
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.analyser;

import net.kylindb.analyser.mq.MetricDatum.MetricDatumRequest;
import net.kylindb.timer.Timer;
import net.kylindb.timer.WheelTimer;
import net.kylindb.coder.ValueCoder;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义处理连接事件的processor<br>
 * 每当断连或连接消息到来时，针对该pointId建立一个timer(超时时间为5分钟)，后继5分钟内，如果该pointId有新的消息到来，统一更新到该timer内，timer到期触发时，判断是否要产生断连或者连接事件
 * <p>
 * 规则：<br>
 * 1.收到断连消息后持续5分钟内未收到恢复消息（连接消息）则产生断连事件。对应的timer触发后会自动删除<br>
 * 2.收到连接消息后，先查看之前是否收到断连消息（是否已经有timer），如果有则更新到timer内，如果没有，则产生新的timer，如果持续5分钟内未收到断连消息，则产生连接事件<br>
 * @author lichunxi
 *
 */
public class ConnectionEventProcessor implements Processor<String, byte[]> {

	public static Logger LOG = LoggerFactory
			.getLogger(ConnectionEventProcessor.class);
	
	/**
	 * 所有timer的超时时间为300秒
	 */
	private static final int TIMEOUT = 300;
	
	/**
	 * Timer管理器
	 */
	private WheelTimer wheelTimer = WheelTimer.getInstance();
	
	/* (non-Javadoc)
	 * @see org.apache.kafka.streams.processor.Processor#init(org.apache.kafka.streams.processor.ProcessorContext)
	 */
	@Override
	public void init(ProcessorContext context) {
		// 每5分钟commit一次
		context.schedule(300*1000, PunctuationType.WALL_CLOCK_TIME , new Punctuator(){

			@Override
			public void punctuate(long timestamp) {
				context.commit();
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.apache.kafka.streams.processor.Processor#process(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void process(String key, byte[] value) {
		try {
			MetricDatumRequest request = MetricDatumRequest.parseFrom(value);
			Long requestId = request.getPointId();
			Long timestamp = request.getTimestamp();
			Integer pointValue = (Integer)ValueCoder.decode(request.getValue().toByteArray());
			Timer timer = wheelTimer.getTimer(requestId);
			if (null != timer) {
				JudgementTask task = (JudgementTask) timer.getTask();

				// 0表示断开，1表示连接
				if (0 == task.getLastValue()) {
					if (1 == pointValue) {
						// 以前已经有断连消息，如果这次是连接消息，则把timer删除，即在5分钟内断连又重连则不报警
						wheelTimer.deleteTimer(timer.getTimerId());
					} else {
						// 收到多个断连消息，累加
						task.addEvent(requestId, timestamp, pointValue);
					}
				} else if (1 == task.getLastValue()){
					task.addEvent(requestId, timestamp, pointValue);
					if (0 == pointValue) {
						// 如果上一次为连接消息，而这次为断连消息，则重新开始新的timer
						wheelTimer.updateTimer(timer.getTimerId(), null);
					}
				} else {
					// 不应该到这里
					LOG.error("last value is null.");
				}
			} else {
				// 获取数据后新建一个timer，新的数据来了就更新到对应的task中
				JudgementTask task = new JudgementTask();
				task.addEvent(requestId, timestamp, pointValue);
				// 使用requestId充当timerId
				wheelTimer.appendTimer(requestId, TIMEOUT, task);
			}
		} catch (Exception e) {
			LOG.error("process fail.", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.kafka.streams.processor.Processor#punctuate(long)
	 */
	@Override
	public void punctuate(long timestamp) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.apache.kafka.streams.processor.Processor#close()
	 */
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
