/**
 * Created:2017年10月31日 下午5:44:12
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.coder;

import net.kylindb.domain.Qualifier;
import net.kylindb.util.Constant;

import org.hbase.async.Bytes;

/**
 * @author lichunxi
 *
 */
public class QualifierCoder{

	public static byte[] encode(Long timestamp, Integer baseTime){
		byte[] qualifier;
		if (0 == (timestamp & Constant.SECOND_MASK)) {
			// 秒s：2Byte，其中前面2bit为01，表示单位为秒，后12bit用于存储秒，剩余2bit保留
			int deltaTime = (int)(timestamp - baseTime);
			qualifier = new byte[Constant.QUALIFIER_SECOND_LENGTH];
			short qualifierShort = (short)((deltaTime << 2) | (1 << 14));
			System.arraycopy(Bytes.fromShort(qualifierShort), 0, qualifier, 0, Constant.QUALIFIER_SECOND_LENGTH);
		} else {
			// 毫秒ms：3Byte，其中前面2bit为10，表示单位为毫秒，后22bit用于存储毫秒
			int deltaTime = (int)(timestamp - baseTime * 1000);
			qualifier = new byte[Constant.QUALIFIER_MILLISECOND_LENGTH];
			int qualifierInt = (int)(deltaTime | (2 << 22));
			System.arraycopy(Bytes.fromInt(qualifierInt), 1, qualifier, 0, Constant.QUALIFIER_MILLISECOND_LENGTH);
		}
		return qualifier;
	}
	
	public static Qualifier decode(byte[] bytes){
		Qualifier qualifier = new Qualifier();
		if (2 == bytes.length){
			// 秒
			qualifier.setType(1);
			int delta =  ((bytes[0] & 0x3F) << 8 | (bytes[1] & 0xFC) << 0) >> 2;
			qualifier.setDelta(delta);
		} else {
			// 毫秒
			qualifier.setType(2);
			int delta =  (0 & 0xFF) << 24  | (bytes[0] & 0x3F) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[2] & 0xFF) << 0;
			qualifier.setDelta(delta);
		}
		return qualifier;
	}
}
