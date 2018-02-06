/**
 * Created:2018年1月26日 上午11:32:02
 * Author:lichunxi
 * All Rights Reserved
 */
package net.kylindb.timer;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 时间轮上的桶，每个桶中放置多个Timer，每个timer内部有remainingCycle来计算是否到期<p>
 * 
 * @author lichunxi
 *
 */
public class Bucket {
	// 仅存放timerId，真实的timer对象在WheelTimer对象的timerMap中
	private Map<Long, Long> timerIdSet = new ConcurrentHashMap<Long, Long>();
	
	public void addTimer(Long timerId){
		timerIdSet.put(timerId, timerId);
	}
	
	public void deleteTimer(Long timerId){
		timerIdSet.remove(timerId);
	}
	
	public Iterator<Long> iterator(){
		return timerIdSet.keySet().iterator();
	}
}
