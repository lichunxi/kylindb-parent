/**
 * Created:2017年10月27日 下午5:53:21
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.orm;

import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.annotation.Resource;

import net.kylindb.util.Bytes;

import org.hbase.async.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.stumbleupon.async.Deferred;

/**
 * 存储每个point对应的最新时间戳，用于查询最新数据，或者计算该point的缺失时间
 * @author lichunxi
 *
 */
@Component(value="offsetDao")
public class OffsetDatumDao {

	private static final byte[] TABLE = "offsets".getBytes(Charset.forName("UTF-8"));
	private static final byte[] FAMILY = { 'o' };
	private static final byte[] QUALIFIER = "ts".getBytes();
	
	public static Logger LOG = LoggerFactory
			.getLogger(OffsetDatumDao.class);
	
	@Resource
	private HbaseService client;
	
	public Deferred<Object> put(byte[] key, byte[] value) {
		return client.put(TABLE, key, FAMILY, QUALIFIER, value);
	}

	public Deferred<Long> get(long pointId) {
		byte[] key = Bytes.fromLong(pointId);
		return client.get(TABLE, key, FAMILY, QUALIFIER)
				.addCallback((ArrayList<KeyValue> keyValueList) -> {
					// 此时keyValueList.size()应该为1
					for (KeyValue keyValue : keyValueList){
						return Bytes.getLong(keyValue.value());
					}
					return null;
				});
	}
}
