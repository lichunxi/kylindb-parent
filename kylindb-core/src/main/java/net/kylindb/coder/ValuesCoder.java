/**
 * Created:2017年11月13日 下午1:52:56
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.coder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.kylindb.util.Constant;

/**
 * @author lichunxi
 *
 */
public class ValuesCoder {
	
	public static byte[] encode(Object[] values) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (Object value : values){
			out.write(ValueCoder.encode(value));
		}
		return out.toByteArray();
	}

	public static Object[] decode(byte[] bytes) {
		List<Object> objectArray = new ArrayList<Object>();
		int size = 0;
		for (int i = 0; i < bytes.length; i = i + size){
			if (((bytes[i] & 0x80) >>> 7) == 0){
				int type = bytes[i] & 0x07;
				if (0 == type) {
					// 000 Double
					size = Constant.DOUBLE_LENGTH + Constant.VALUE_TYPE_LENGTH;
				} else if (1 == type) {
					// 001 Float
					size = Constant.FLOAT_LENGTH + Constant.VALUE_TYPE_LENGTH;
				} else if (2 == type
						 || 3 == type
						 || 4 == type) {
					// 010 Integer
					// 011 Long
					// 100 Boolean
					size = 1;
					int k = i;
					while(k < bytes.length -1 
							&& ((bytes[k + 1] & 0x80) >>> 7) == 1){
						size++;
						k++;
					}
				} else if (5 == type) {
					// 101 String
					size = Constant.VALUE_TYPE_LENGTH;
					int pos = 1;
					int k = i;
					while(k < bytes.length - 2
							&& ((bytes[k + 1] & 0x80) >>> 7) == 1){
						pos++;
						k++;
					}
					size += pos;
					byte[] lengthBytes = new byte[pos];
					System.arraycopy(bytes, i + 1, lengthBytes, 0, pos);
					Integer length = ValueCoder.decodeVarInt(lengthBytes, 1);
					size +=length;
				} else {
					throw new IllegalArgumentException(
							"unknow type, only support: Double, Float, Integer, Long, Boolean, String");
				}
				byte[] itemBytes = new byte[size];
				System.arraycopy(bytes, i, itemBytes, 0, size);
				objectArray.add(ValueCoder.decode(itemBytes));
			} else {
				throw new IllegalArgumentException(
						"should not be here.");
			}
		}
		return objectArray.toArray(new Object[]{});
	}
}
