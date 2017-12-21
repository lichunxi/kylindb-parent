/**
 * Created:2017年11月20日 上午11:30:18
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricReader;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.stumbleupon.async.Deferred;

import net.kylindb.client.IMetricDatumReader;
import net.kylindb.client.INoteDatumReader;
import net.kylindb.client.domain.DataPoint;
import net.kylindb.client.domain.Note;
import net.kylindb.orm.IDatumDao;
import net.kylindb.orm.OffsetDatumDao;

/**
 * @author lichunxi
 *
 */
@Service
public class DefaultMetricDatumReader implements IMetricDatumReader {
	public static Logger LOG = LoggerFactory
			.getLogger(DefaultMetricDatumReader.class);
	
	@Resource(name="metricDao")
	private IDatumDao<DataPoint> metricReader;
	
	@Resource
	private OffsetDatumDao offsetReader;
	
	@Resource
	private INoteDatumReader noteReader;

	/* (non-Javadoc)
	 * @see net.kylindb.metricReader.IMetricDatumReader#queryMetricData(java.lang.Long[], java.lang.Long, java.lang.Long)
	 */
	@Override
	public Deferred<List<DataPoint>> queryMetricData(Long[] pointIds,
			Long start, Long end, boolean withNotes) {
		try{
			if (withNotes) {
				List<DataPoint> dataList = queryMetricDataOnly(pointIds, start, end).joinUninterruptibly(5 * 1000L);
			
				// 查询notes，并附加到point上
				List<Note> noteList = noteReader.queryNoteData(pointIds, start, end).joinUninterruptibly(5 * 1000L);
				if (null != noteList) {
					for (Note note : noteList) {
						for (DataPoint point : dataList) {
							if (note.getPointId().equals(point.getPointId())
									&& note.getTimestamp().equals(point.getTimestamp())) {
								point.setNotes(note.getNotes());
								continue; // 一个note只会对应一个point，找到了之后就不用往后找了
							}
						}
					}
					return Deferred.fromResult(dataList);
				}
				return Deferred.fromResult(dataList);
			} else {
				return queryMetricDataOnly(pointIds, start, end);
			}
		} catch(Exception e){
			LOG.error("query metricData except:", e);
			return Deferred.fromResult(null);
		}
	}
	
	private Deferred<List<DataPoint>> queryMetricDataOnly(Long[] pointIds, Long start, Long end) {
		try{
			List<Deferred<List<DataPoint>>> results = new ArrayList<Deferred<List<DataPoint>>>();
			for (Long pointId : pointIds){
				results.add(metricReader.scan(pointId, start, end));
			}
			
			Deferred<List<DataPoint>> deferred = Deferred.group(results).addCallback((ArrayList<List<DataPoint>> resultList) ->{
				List<DataPoint> dataList = new ArrayList<DataPoint>();
				for (List<DataPoint> pointList : resultList){
					dataList.addAll(pointList);
				}
				return dataList;
			}).addErrback((Exception e) -> {
				LOG.error("query metricData only fail.", e);
				return e;
			});
			return deferred;
		} catch(Exception e){
			LOG.error("query metricData only except:", e);
			return Deferred.fromResult(null);
		}
	}

	@Override
	public Deferred<List<DataPoint>> queryLatestMetricData(Long[] pointIds, boolean withNotes) {
		try{
			List<DataPoint> dataPointList = new ArrayList<DataPoint>();
			for (Long pointId : pointIds){
				// 先查找该pointId对应的最新时间戳，再根据时间戳获取数据
				long timestamp = offsetReader.get(pointId).joinUninterruptibly(5*1000L);
				DataPoint point = metricReader.get(pointId, timestamp).joinUninterruptibly(5*1000L);
				if (withNotes) {
					Note note = noteReader.queryNoteDatum(pointId, timestamp).joinUninterruptibly(5 * 1000L);
					if (null != note){
						point.setNotes(note.getNotes());
					}
				}
				dataPointList.add(point);
			}
			return Deferred.fromResult(dataPointList);
		} catch (Exception e){
			LOG.error("query latest metric except:", e);
			return Deferred.fromResult(null);
		}
	}
}
