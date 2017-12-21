/**
 * Created:2017年11月20日 上午10:29:11
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.client;

import java.util.List;

import com.stumbleupon.async.Deferred;

import net.kylindb.client.domain.DataPoint;

/**
 * @author lichunxi
 *
 */
public interface IMetricDatumReader {
	
	/**
	 * 按照pointId和时间段查询历史数据。时间段为：start<=time<end
	 * @param pointIds pointId列表
	 * @param start 开始时间戳，秒或毫秒
	 * @param end 结束时间戳
	 * @param withNotes 是否把pointId相关的note信息查询出来，默认为否false
	 * @return 符合要求的数据集
	 */
	Deferred<List<DataPoint>> queryMetricData(Long[] pointIds, Long start, Long end, boolean withNotes);

	/**
	 * 按照pointId查询最新的数据
	 * @param pointIds pointId列表
     * @param withNotes 是否把pointId相关的note信息查询出来，默认为否false
	 * @return 符合要求的数据集
	 */
	Deferred<List<DataPoint>> queryLatestMetricData(Long[] pointIds, boolean withNotes);
}
