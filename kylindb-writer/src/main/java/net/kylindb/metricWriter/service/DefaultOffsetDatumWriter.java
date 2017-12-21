/**
 * Created:2017年11月29日 上午11:05:26
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricWriter.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.kylindb.orm.OffsetDatumDao;
import net.kylindb.stat.domain.RequestMetrics;
import net.kylindb.stat.service.RequestMetricsService;
import net.kylindb.util.Bytes;
import net.kylindb.util.TimeUtil;

import com.stumbleupon.async.Deferred;

/**
 * @author lichunxi
 *
 */
@Service
public class DefaultOffsetDatumWriter implements IOffsetDatumWriter {
	public static Logger LOG = LoggerFactory
			.getLogger(DefaultOffsetDatumWriter.class);
	/**
	 * 为什么是150秒？
	 * 如果上报周期大于1分钟（例如5分钟），那么每次更新的时候先读取，再比较，性能不至于很差，因为周期间隔大
	 * 如果周期为1分钟：
	 *     1）如果只有一个节点，每次都会使用缓存，不查询数据库
	 *     2）如果有两个或更多节点，150秒是为了保证在本节点上数据最多推迟2.5周期到达，在此期限内使用缓存，否则查数据库
	 * 如果周期为秒级，则有更大的概率使用缓存
	 */
	private static final Long CACHE_TIME_MAX_GAP = 150 * 1000L;
	
	// 1小时
	private static final Long OUT_RANGE_MAX_GAP = 1 * 60 * 60 * 1000L;
	
	private Map<Long, Long> timestampCache = new ConcurrentHashMap<Long, Long>();
	
	@Resource
	private OffsetDatumDao offsetSaver;
	
	private Lock lock = new ReentrantLock();
	
	/* (non-Javadoc)
	 * @see net.kylindb.metricWriter.service.IOffsetDatumWriter#putOffsetDatum(java.lang.Long, java.lang.Long)
	 */
	@Override
	public Deferred<Object> putOffsetDatum(Long pointId, Long timestamp) {
		RequestMetrics stat = RequestMetricsService.getInstance("putOffsetDatum");
		stat.start();
		try{
			// 有可能出现历史补传的数据，此时timestamp肯定比实际最近的时间戳小，不应该更新
			// 最保险的办法是每次去数据库查询，然后比较时间戳，只有当前的时间戳大于数据库时间戳时才更新
			// 但是这样耗费性能，折中的办法是通过时间比较和本地缓存尽可能减少性能损耗
			
			// 一共3个时间：本地时间localTime、缓存时间cacheTime、业务时间timestamp
			// localTime和timestamp的比较决定滞后或超前太久的数据是否更新offset
			// localTime和cacheTime的比较决定cacheTime是否可信
			// cacheTime和timestamp的比较决定是否要更新offset
			long pointTime = TimeUtil.getMilisecond(timestamp);
			// 1:比较localTime和timestamp
			long localTime = System.currentTimeMillis();
			if (Math.abs(localTime - pointTime) > OUT_RANGE_MAX_GAP){
				stat.end();
				return Deferred.fromResult(0);
			}
			
			// 如果缓存的timestamp和服务器时间比较相差很小(90秒？)，则认为缓存的timestamp可信
			// 当可信时，再比较当前时间戳和缓存时间戳，如果当前时间戳大则更新
			// 如果不可信，则先查询数据，再比较
			Long cacheTime = timestampCache.get(pointId);
			if (null == cacheTime){
				try{
					lock.lock();
					cacheTime = timestampCache.get(pointId);
					if (null == cacheTime){
						// 本地没有则从数据库获取
						Long latestTime = offsetSaver.get(pointId).joinUninterruptibly(10 * 1000L);
						if (null != latestTime){
							cacheTime = TimeUtil.getMilisecond(latestTime);
							// 把数据库中最新的time缓存在本地，避免频繁查询
							timestampCache.put(pointId, cacheTime);
						} else {
							// 数据库没有则进行存储
							put(pointId, timestamp);
							stat.end();
							return Deferred.fromResult(1);
						}
					}
				} finally {
					lock.unlock();
				}
			} 
			
			if (null != cacheTime && pointTime > cacheTime){
				if (canAccept(cacheTime)){
					// 缓存的时间戳可信
					put(pointId, timestamp);
					stat.end();
					return Deferred.fromResult(1);
				} else {
					// 不可信
					Long latestTime = offsetSaver.get(pointId).joinUninterruptibly(10 * 1000L);
					if (null == latestTime || pointTime > TimeUtil.getMilisecond(latestTime)){
						put(pointId, timestamp);
						stat.end();
						return Deferred.fromResult(1);
					} else {
						cacheTime = TimeUtil.getMilisecond(latestTime);
						// 把数据库中最新的time缓存在本地，避免频繁查询
						timestampCache.put(pointId, cacheTime);
					}
				}
			}
		} catch (Exception e){
			LOG.error("putOffsetDatum fail.", e);
			stat.fail();
			return Deferred.fromResult(0);  // 插入0行
		}
		stat.end();
		return Deferred.fromResult(0);  // 插入0行
	}
	
	private void put(Long pointId, Long timestamp) throws Exception{
		offsetSaver.put(Bytes.fromLong(pointId), Bytes.fromLong(timestamp)).joinUninterruptibly(10 * 1000L);
		timestampCache.put(pointId, TimeUtil.getMilisecond(timestamp));
	}

	/**
	 * @param cacheTime
	 * @return
	 */
	private boolean canAccept(long cacheTime) {
		// 如果出现业务时间戳比服务器时间大，也认为可以接受
		long localTime = System.currentTimeMillis();
		return (localTime - cacheTime > CACHE_TIME_MAX_GAP) ? false : true;
	}

}
