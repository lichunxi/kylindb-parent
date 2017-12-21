/**
 * Created:2017年9月13日 下午3:53:34
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricWriter.service;


import com.stumbleupon.async.Deferred;

/**
 * 数据开始写入数据库
 * @author lichunxi
 *
 */
public interface IOffsetDatumWriter {
	/**
	 * 保存Offset数据
	 * @param pointId 测点Id
	 * @param timestamp 最新的时间戳，距离1970年1月1日原点的秒数或毫秒数
	 */
	Deferred<Object> putOffsetDatum(Long pointId, Long timestamp);
}
