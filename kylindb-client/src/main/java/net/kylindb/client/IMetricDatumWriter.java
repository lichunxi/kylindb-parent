/**
 * Created:2017年9月13日 下午3:53:34
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.client;

import java.util.List;
import java.util.Map;

import com.stumbleupon.async.Deferred;

import net.kylindb.client.domain.DataPoint;

/**
 * 数据开始写入数据库
 * @author lichunxi
 *
 */
public interface IMetricDatumWriter {
	/**
	 * 保存监控数据
	 * @param pointId 测点Id
	 * @param timestamp 时间戳，距离1970年1月1日原点的秒数或毫秒数
	 * @param value 该测点Id在该时间点上的测量值
	 * @param notes 针对该条数据的一些备注信息，例如最大值产生的时间：maxTime=1509724800，数据异常isInvalid=true，该时间点上的操作action=开机<br>
	 * 不应该把pointId的维度信息放在此字段，例如传感器Id，机器名称等，这些都是pointId的限定信息
	 */
	Deferred<Object> putMetricDatum(Long pointId, Long timestamp, Object value, Map<String, String> notes);
	
	
	/**
	 * 保存监控数据列表
	 * @param points 数据列表
	 */
	Deferred<Object> putMetricData(List<DataPoint> points);
}
