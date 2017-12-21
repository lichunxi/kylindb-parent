/**
 * Created:2017年9月13日 下午4:01:50
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricWriter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.kylindb.coder.QualifierCoder;
import net.kylindb.coder.RowKeyCoder;
import net.kylindb.coder.ValueCoder;
import net.kylindb.client.IMetricDatumWriter;
import net.kylindb.client.INoteDatumWriter;
import net.kylindb.client.domain.DataPoint;
import net.kylindb.orm.IDatumDao;
import net.kylindb.stat.domain.RequestMetrics;
import net.kylindb.stat.service.RequestMetricsService;
import net.kylindb.util.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.stumbleupon.async.Deferred;


/**
 * 数据写服务<br>
 * 本类中不处理数据写入失败的逻辑。写入失败应该由外部调用者进行处理，请参照kafka消费者中的逻辑
 * @author lichunxi
 *
 */
@Service
public class DefaultMetricDatumWriter implements IMetricDatumWriter {

	public static Logger LOG = LoggerFactory
			.getLogger(DefaultMetricDatumWriter.class);
	
	@Resource(name="metricDao")
	private IDatumDao<DataPoint> metricSaver;
	
	@Resource
	private INoteDatumWriter noteSaver;
	
	@Resource
	private IOffsetDatumWriter offsetSaver;
	
	/* (non-Javadoc)
	 * @see net.kylindb.metricDataWriter.service.IMetricDatumWriter#putMetricDatum(java.lang.Long, java.lang.Long, java.lang.String)
	 */
	@Override
	public Deferred<Object> putMetricDatum(Long pointId, Long timestamp, Object value, Map<String, String> notes) {
		RequestMetrics stat = RequestMetricsService.getInstance("putMetricDatum");
		stat.start();
		try{
			int baseTime = TimeUtil.getBaseTime(timestamp);
			
			// 拼装rowkey
			byte[] rowKeyBytes = RowKeyCoder.encode(pointId, baseTime);
			
			// 拼装Qualifier
			byte[] qualifierBytes = QualifierCoder.encode(timestamp, baseTime);
			
			// value编码
			byte[] valueBytes = ValueCoder.encode(value);
			
			List<Deferred<Object>> deferredList = new ArrayList<Deferred<Object>>();
			deferredList.add(metricSaver.put(rowKeyBytes, qualifierBytes, valueBytes));
			deferredList.add(offsetSaver.putOffsetDatum(pointId, timestamp));
			if (null != notes && 0 < notes.size()){
				deferredList.add(noteSaver.putNoteDatum(pointId, timestamp, notes));
			}
			Deferred<Object> putResult = Deferred.group(deferredList).addErrback((Exception eb) -> {
				LOG.warn("putMetricDatum fail.", eb);
				return eb;
			}).addCallback((ArrayList<Object> result) ->{
				return 1;   // 全部成功，说明成功插入1条
			});
			stat.end();
			return putResult;
		} catch(Exception e){
			LOG.error("putMetricDatum except.", e);
			stat.fail();
			return Deferred.fromError(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.kylindb.metricDataWriter.service.IMetricDatumWriter#putMetricData(java.util.List)
	 */
	@Override
	public Deferred<Object> putMetricData(List<DataPoint> points) {
		RequestMetrics stat = RequestMetricsService.getInstance("putMetricDataList");
		stat.start();
		List<Deferred<Object>> deferredList = new ArrayList<Deferred<Object>>();
		for (DataPoint point : points){
			deferredList.add(putMetricDatum(point.getPointId(), point.getTimestamp(), point.getValue(), point.getNotes()));
		}
		Deferred<Object> putResult = Deferred.group(deferredList).addCallback((ArrayList<Object> result) ->{
			return result.size();
		});
		stat.end();
		return putResult;
	}

}
