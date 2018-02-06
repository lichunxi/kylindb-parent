/**
 * Created:2018年1月31日 下午5:01:40
 * Author:lichunxi
 * All Rights Reserved
 */
package net.kylindb.analyser;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import net.kylindb.analyser.mq.KafkaSender;
import net.kylindb.analyser.mq.MetricDatum.MetricDatumRequest;
import net.kylindb.timer.Task;
import net.kylindb.coder.ValueCoder;

/**
 * @author lichunxi
 *
 */
public class JudgementTask implements Task {
	public static Logger LOG = LoggerFactory
			.getLogger(JudgementTask.class);
	
	/**
	 * 缓存5分钟内的数据，等超时后进行判断
	 * ：TODO
	 * 为了防止本地崩溃，应该放在共享的存储中，如redis
	 */
	private List<Event> data = new ArrayList<Event>(5);

	/* (non-Javadoc)
	 * @see net.kylindb.analyser.timer.Task#run()
	 */
	@Override
	public void run() {
		// 断连value为0，连接value为1
		if (data.size() > 0){
			// 仅根据最后一条数据进行判断
			Event event = data.get(data.size() - 1);
			if (null != event){
				if (0 == event.value){
					// 产生断连告警事件
					generateAndSendEvent(event.pointId, 0);
				} else if (1 == getLastValue()){
					// 产生连接通知事件
					generateAndSendEvent(event.pointId, 1);
				} else {
					// 不应该到此处
					LOG.error("unknow value:{}", event.value);
				}
			}
		}
	}
	
	/**
	 * @param pointId
	 * @param i
	 */
	private void generateAndSendEvent(Long pointId, Integer value) {
		Long time = System.currentTimeMillis();
		MetricDatumRequest.Builder requestBuilder = MetricDatumRequest
				.newBuilder();
		requestBuilder.setPointId(pointId)
				.setTimestamp(time)
				.setValue(ByteString.copyFrom(ValueCoder.encode(value)));
		// 向新的topic发送消息
		KafkaSender.send("events_connection_judgement", String.valueOf(pointId), requestBuilder.build().toByteArray());
	}

	public Integer getLastValue(){
		if (data.size() > 0){
			Event event = data.get(data.size() - 1);
			if (null != event){
				return event.value;
			}
		}
		return null;
	}
	
	public void addEvent(Long pointId, Long timestamp, Integer value){
		data.add(new Event(pointId, timestamp, value));
	}

	public class Event{
		Long pointId;
		Long timestamp;
		Integer value;
		
		/**
		 * @param pointId
		 * @param timestamp
		 * @param value
		 */
		public Event(Long pointId, Long timestamp, Integer value) {
			super();
			this.pointId = pointId;
			this.timestamp = timestamp;
			this.value = value;
		}
	}
}
