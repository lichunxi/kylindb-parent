/**
 * Created:2017年10月31日 下午5:44:12
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.coder;

import java.util.Arrays;
import org.hbase.async.Bytes;

import net.kylindb.domain.RowKey;
import net.kylindb.util.Constant;

/**
 * @author lichunxi
 *
 */
public class RowKeyCoder{
	// salt用于缓解海量数据写时写热点问题
	public static final int SALT_LENGTH = 1;
	public static final int BUCKETS_SIZE = 256;

	public static byte[] encode(Long pointId, Integer baseTime){
		//PointId用8byte数字表示
		//baseTime用4byte数字表示
		int keySize = SALT_LENGTH + Constant.POINT_LENGTH + Constant.TIMESTAMP_LENGTH;
		byte[] rowKey = new byte[keySize];
		byte[] pointIdBytes = Bytes.fromLong(pointId);
		System.arraycopy(pointIdBytes, 0, rowKey, SALT_LENGTH, Constant.POINT_LENGTH);
		
		byte[] saltBytes = getSalt(pointIdBytes);
		System.arraycopy(saltBytes, 0, rowKey, 0, SALT_LENGTH);
		
		byte[] baseTimeBytes = Bytes.fromInt(baseTime);
		System.arraycopy(baseTimeBytes, 0, rowKey, Constant.POINT_LENGTH + SALT_LENGTH, Constant.TIMESTAMP_LENGTH);
		return rowKey;
	}
	
	public static RowKey decode(byte[] bytes){
		byte[] longBytes = new byte[Constant.POINT_LENGTH];
		System.arraycopy(bytes, SALT_LENGTH, longBytes, 0, Constant.POINT_LENGTH);
		long pointId = Bytes.getLong(longBytes);
		
		byte[] baseTimeBytes = new byte[Constant.TIMESTAMP_LENGTH];
		System.arraycopy(bytes, Constant.POINT_LENGTH + SALT_LENGTH, baseTimeBytes, 0, Constant.TIMESTAMP_LENGTH);
		Integer baseTime = Bytes.getInt(baseTimeBytes);
		
		RowKey rowKey = new RowKey();
		rowKey.setPointId(pointId);
		rowKey.setBaseTime(baseTime);
		return rowKey;
	}
	
	public static byte[] getSalt(byte[] salt_base){
		int modulo = Arrays.hashCode(salt_base) % BUCKETS_SIZE;
		if (modulo < 0) {
			// make sure we return a positive salt.
			modulo = modulo * -1;
		}
		return getSaltBytes(modulo);
	}
	
	private static byte[] getSaltBytes(final int bucket) {
		final byte[] bytes = new byte[SALT_LENGTH];
		int shift = 0;
		for (int i = 1; i <= SALT_LENGTH; i++) {
			bytes[SALT_LENGTH - i] = (byte) (bucket >>> shift);
			shift += 8;
		}
		return bytes;
	}	
}
