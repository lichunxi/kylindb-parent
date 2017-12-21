/**
 * Created:2017年11月20日 上午11:57:05
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.orm;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import net.kylindb.coder.QualifierCoder;
import net.kylindb.coder.RowKeyCoder;
import net.kylindb.coder.ValueCoder;
import net.kylindb.client.domain.DataPoint;
import net.kylindb.domain.Qualifier;
import net.kylindb.domain.RowKey;
import net.kylindb.util.Constant;
import net.kylindb.util.TimeUtil;

import org.hbase.async.KeyValue;
import org.hbase.async.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.stumbleupon.async.Callback;
import com.stumbleupon.async.Deferred;

/**
 * @author lichunxi
 *
 */
@Component(value="metricDao")
public class MetricDatumDao implements IDatumDao<DataPoint> {
	/**
	 * scan的超时时间默认为30秒
	 */
	private static final Long TIMEOUT = 30 * 1000L;
	private static final byte[] TABLE = "metrics".getBytes(Charset.forName("UTF-8"));
	private static final byte[] FAMILY = { 'm' };
	
	public static Logger LOG = LoggerFactory
			.getLogger(MetricDatumDao.class);
	
	@Resource
	private HbaseService client;
	
	@Override
	public Deferred<Object> put(byte[] key, byte[] qualifier, byte[] value) {
		return client.put(TABLE, key, FAMILY, qualifier, value);
	}
	
	@Override
	public Deferred<DataPoint> get(long pointId, long timestamp) {
		int baseTime = TimeUtil.getBaseTime(timestamp);
		byte[] key = RowKeyCoder.encode(pointId, baseTime);
		byte[] qualifier = QualifierCoder.encode(timestamp, baseTime);
		return client.get(TABLE, key, FAMILY, qualifier)
				.addCallback((ArrayList<KeyValue> keyValueList) -> {
					for (KeyValue keyValue : keyValueList){
						DataPoint point = convert(keyValue);
						if (point.getTimestamp() == TimeUtil.getMilisecond(timestamp) && point.getPointId() == pointId){
							return point;
						}
					}
					return null;
				});
	}
	
	private DataPoint convert(KeyValue keyValue){
		RowKey rowKey = RowKeyCoder.decode(keyValue.key());
		Long pointId = rowKey.getPointId();
		
		Qualifier qualifier = QualifierCoder.decode(keyValue.qualifier());
		int delta = qualifier.getDelta();
		Long timestamp = 0L;
		// 统一转换为毫秒，这样无论写入时时间单位是秒还是毫秒，在读取时，统一为毫秒
		int type = qualifier.getType();
		if (1 == type) {//秒
			timestamp = (rowKey.getBaseTime() + delta) * 1000L;
		} else{//毫秒
			timestamp = rowKey.getBaseTime() * 1000L + delta * 1L;
		}
		Object value = ValueCoder.decode(keyValue.value());
		return new DataPoint(pointId, timestamp, value);
	}

	@Override
	public Deferred<List<DataPoint>> scan(long pointId, long start, long end) {
		List<DataPoint> dataPointList = new ArrayList<DataPoint>();
		Deferred<List<DataPoint>> results = new Deferred<List<DataPoint>>();
		Long scanStart = System.currentTimeMillis();
		byte[] startRow = RowKeyCoder.encode(pointId, TimeUtil.getBaseTime(start));
		byte[] endRow = RowKeyCoder.encode(pointId, TimeUtil.getBaseTime(end) + Constant.MAX_TIMESPAN);
		
		Scanner scanner = client.getScanner(TABLE, FAMILY, startRow, endRow, 256);
		
		class ScannerCB implements Callback<Object, ArrayList<ArrayList<KeyValue>>>{
			int rowCount = 0;
			int pointCount = 0;
			
			@Override
			public Object call(ArrayList<ArrayList<KeyValue>> rows) throws Exception {
				try {
					if (null == rows) {
						close(null);
						return null;
					}
					
					if (System.currentTimeMillis() - scanStart > TIMEOUT) {
                        throw new InterruptedException(
                                "Query timeout exceeded!");
                    }

					for (final ArrayList<KeyValue> row : rows) {
						rowCount++;
						for (KeyValue keyValue : row){
							pointCount++;
							dataPointList.add(convert(keyValue));
						}
					}
					return scan();
				} catch (Exception e) {
					LOG.error("query metric fail.", e);
					close(e);
					return null;
				}
			}
			
			public Object scan() {
				return scanner.nextRows().addCallback(this)
						.addErrback(new ErrorCB());
			}
			
			void close(final Exception e) {
				if (LOG.isDebugEnabled()){
					LOG.debug("Query {} rows {} points in {} milliseconds.", rowCount, pointCount, System.currentTimeMillis() - scanStart);
				}
				scanner.close();
				if (e != null) {
					results.callback(e);
				} else {
					// 剔除不符合时间范围的数据，最终的时间范围：[start,end)
					int length = dataPointList.size();
					if (length > 0) {
						// 判断并删除小于start的数据
						// 判断并删除大于等于end的数据
						Iterator<DataPoint> iterator = dataPointList.iterator();
						while (iterator.hasNext()) {
							DataPoint point = iterator.next();
							if (point.getTimestamp() < TimeUtil.getMilisecond(start)
									|| point.getTimestamp() >= TimeUtil.getMilisecond(end)) {
								iterator.remove();
							}
						}
					}
					LOG.debug("finally query {} points according to start and end time.", length);
					results.callback(dataPointList);
				}
			}
			
			class ErrorCB implements Callback<List<DataPoint>, Exception> {
				@Override
				public List<DataPoint> call(final Exception e) throws Exception {
					LOG.error("Scanner:" + scanner + " throw an exception, ", e);
					close(e);
					return null;
				}
			}
		}
		
		new ScannerCB().scan();
		return results;
	}

	
}
