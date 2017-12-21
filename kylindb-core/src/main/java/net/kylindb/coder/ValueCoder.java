/**
 * Created:2017年10月31日 下午8:15:46
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.coder;

import java.nio.charset.Charset;

import net.kylindb.util.Bytes;
import net.kylindb.util.Constant;

/**
 * 值的编码方式如下；<br>
 *
 * 采用Varint<br>
 * 每个字节的第一位bit作为flag标识，1表示下一个Byte和自己是一起的，0表示到本byte结束，下一个byte是新的数字开始。<br>
 * 剩下7bit用于存储真实的数值对应的bits数值<br>
 * flag标识为0的byte，其中最后3bit作为数据类型type<br>
 * <br>
 * 数字<br>
 * double type+8byte type占用3bit，值为000<br>
 * float type+4byte type占用3bit，值为001<br>
 * int type+varint type占用3bit，值为010，，采用zigzag编码<br>
 * long type+varint type占用3bit，值为011，，采用zigzag编码<br>
 * <br>
 * 布尔值<br>
 * bool type +varint type占用3bit，值为100<br>
 * <br>
 * 字符串<br>
 * string type+length+value type占用3bit，值为101。 length字段采用type为010的varint表示<br>
 * 
 * @author lichunxi
 *
 */
public class ValueCoder {
	public static byte[] encode(Object value) {
		if (value instanceof Integer || value instanceof Long
				|| value instanceof Boolean) {
			return encodeVarInt(value, 0);
		} else if (value instanceof Double) {
			// 固定8byte+1Byte
			Double val = (Double) value;
			long longBits = Double.doubleToLongBits(val);
			byte type = 0; // 000
			int length = Constant.DOUBLE_LENGTH + Constant.VALUE_TYPE_LENGTH;

			byte[] fixBytes = new byte[length];
			byte[] typeBytes = new byte[] { type };
			System.arraycopy(typeBytes, 0, fixBytes, 0,
					Constant.VALUE_TYPE_LENGTH);
			System.arraycopy(Bytes.fromLong(longBits), 0, fixBytes,
					Constant.VALUE_TYPE_LENGTH, Constant.DOUBLE_LENGTH);
			return fixBytes;
		} else if (value instanceof Float) {
			// 固定4byte+1Byte
			Float val = (Float) value;
			int intBits = Float.floatToIntBits(val);
			byte type = 1; // 001
			int length = Constant.FLOAT_LENGTH + Constant.VALUE_TYPE_LENGTH;

			byte[] fixBytes = new byte[length];
			byte[] typeBytes = new byte[] { type };
			System.arraycopy(typeBytes, 0, fixBytes, 0,
					Constant.VALUE_TYPE_LENGTH);
			System.arraycopy(Bytes.fromInt(intBits), 0, fixBytes,
					Constant.VALUE_TYPE_LENGTH, Constant.FLOAT_LENGTH);
			return fixBytes;
		} else if (value instanceof String) {
			String str = (String) value;
			// 采用TLV方式，tag表示类型为字符串，占用1Byte，Value为字符串的UTF-8编码值,
			// Length表示字符串进行UTF-8编码之后占用的长度，Length用大端表示法
			byte type = 5; // 101
			byte[] valueBytes = str.getBytes(Charset.forName("UTF-8"));
			byte[] lengthBytes = encodeVarInt(valueBytes.length, 1); // 采用varInt编码

			byte[] stringBytes = new byte[Constant.VALUE_TYPE_LENGTH
					+ lengthBytes.length + valueBytes.length];
			byte[] typeBytes = new byte[] { type };
			System.arraycopy(typeBytes, 0, stringBytes, 0,
					Constant.VALUE_TYPE_LENGTH);
			System.arraycopy(lengthBytes, 0, stringBytes,
					Constant.VALUE_TYPE_LENGTH, lengthBytes.length);
			System.arraycopy(valueBytes, 0, stringBytes,
					Constant.VALUE_TYPE_LENGTH + lengthBytes.length,
					valueBytes.length);
			return stringBytes;
		} else {
			throw new IllegalArgumentException(
					"Not support type, only support: Double, Float, Integer, Long, Boolean, String");
		}
	}

	private static byte[] encodeVarInt(Object value, int order) {
		// 采用zigzag编码，用无符号数来表示有符号数，可以节省空间
		// zigzag中0表示0，1表示-1，2表示1，3表示-2，4表示2，……4294967294表示2147483647（2^31-1），4294967295（2^32-1）表示-2147483648
		Long val;
		if (value instanceof Integer) {
			Integer i = (Integer) value;
			val = encodeZigZag64(i); // 需要使用64bit的long来保存，否则可能溢出为负数，导致保存时占用的空间太多
		} else if (value instanceof Boolean) {
			Boolean b = (Boolean) value;
			if (b) {
				val = 1L;
			} else {
				val = 0L;
			}
		} else {
			Long l = (Long) value;
			val = encodeZigZag64(l);
		}

		byte type = 2;
		if (value instanceof Integer) {
			type = 2; // 010
		} else if (value instanceof Long) {
			type = 3; // 011
		} else if (value instanceof Boolean) {
			type = 4; // 100
		}

		byte[] varIntBytes;
		int numberOfLeadingZeros = Long.numberOfLeadingZeros(val);
		int remainBits = 64 - numberOfLeadingZeros;
		int length = 1;
		if (remainBits <= 4) {
			length = 1;
			varIntBytes = getVarIntBytes(val, length, type, order);
		} else if (remainBits <= 7 + 4) { // 11
			length = 2;
			varIntBytes = getVarIntBytes(val, length, type, order);
		} else if (remainBits <= 7 * 2 + 4) {// 18
			length = 3;
			varIntBytes = getVarIntBytes(val, length, type, order);
		} else if (remainBits <= 7 * 3 + 4) {// 25
			length = 4;
			varIntBytes = getVarIntBytes(val, length, type, order);
		} else if (remainBits <= 7 * 4 + 4) {// 32
			length = 5;
			varIntBytes = getVarIntBytes(val, length, type, order);
		} else if (remainBits <= 7 * 5 + 4) {// 39
			length = 6;
			varIntBytes = getVarIntBytes(val, length, type, order);
		} else if (remainBits <= 7 * 6 + 4) {// 46
			length = 7;
			varIntBytes = getVarIntBytes(val, length, type, order);
		} else if (remainBits <= 7 * 7 + 4) {// 53
			length = 8;
			varIntBytes = getVarIntBytes(val, length, type, order);
		} else if (remainBits <= 7 * 8 + 4) {// 60
			length = 9;
			varIntBytes = getVarIntBytes(val, length, type, order);
		} else {
			length = 10;
			varIntBytes = getVarIntBytes(val, length, type, order);
		}
		return varIntBytes;
	}

	// order=0表示小端排序，此时type在前面；order=1表示大端排序，此时type在后面
	private static byte[] getVarIntBytes(Long value, int length, byte type, int order) {
		byte[] varIntBytes = new byte[length];
		int index = length - 1;
		byte v0 = (byte) (((value & 0x000000000000000f) << 3 & 0x0fffffffffffff7fL) | type);
		if (0 == order){
			varIntBytes[0] = v0;
			for (int i = 0; i < index; i++) {
				int shiftBits = i * 7 + 4;
				byte v = (byte) (((value >>> shiftBits) & 0x000000000000007f) | 0x0000000000000080);
				varIntBytes[i + 1] = v;
			}
		} else {
			varIntBytes[index] = v0;
			for (int i = 0; i < index; i++) {
				int shiftBits = i * 7 + 4;
				byte v = (byte) (((value >>> shiftBits) & 0x000000000000007f) | 0x0000000000000080);
				varIntBytes[index - i - 1] = v;
			}
		}
		return varIntBytes;
	}

	public static int encodeZigZag32(final int n) {
		return (n << 1) ^ (n >> 31);
	}

	public static int decodeZigZag32(final int n) {
		return (n >>> 1) ^ -(n & 1);
	}

	public static long encodeZigZag64(final long n) {
		return (n << 1) ^ (n >> 63);
	}

	public static long decodeZigZag64(final long n) {
		return (n >>> 1) ^ -(n & 1);
	}

	public static Object decode(byte[] bytes) {
		// 先获取到第一个Byte，从中得到数据类型
		byte first = bytes[0];
		int type = first & 0x07;
		if (0 == type) {
			// 000 Double
			long longBits = Bytes.getLong(bytes, 1);
			return Double.longBitsToDouble(longBits);
		} else if (1 == type) {
			// 001 Float
			int intBits = Bytes.getInt(bytes, 1);
			return Float.intBitsToFloat(intBits);
		} else if (2 == type) {
			// 010 Integer
			return decodeVarInt(bytes, 0);
		} else if (3 == type) {
			// 011 Long
			return decodeVarLong(bytes);
		} else if (4 == type) {
			// 100 Boolean
			return decodeVarBoolean(bytes);
		} else if (5 == type) {
			// 101 String
			int i = 1;
			while (((bytes[i] & 0x80) >>> 7) > 0){
				i++;
			}
			byte[] lengthBytes = new byte[i];
			System.arraycopy(bytes, 1, lengthBytes, 0, i);
			
			Integer length = decodeVarInt(lengthBytes, 1);
			byte[] strBytes = new byte[length];
			System.arraycopy(bytes, i + 1, strBytes, 0, length);
			return new String(strBytes, Charset.forName("UTF-8"));
		} else {
			throw new IllegalArgumentException(
					"unknow type, only support: Double, Float, Integer, Long, Boolean, String");
		}
	}

	public static Integer decodeVarInt(byte[] bytes, int order) {
		int length = bytes.length;
		if (0 == order){
			int x = (bytes[0] & 0x78) >> 3;
			if (1 == length) {
				return decodeZigZag32(x);
			}
	
			for (int i = 0; i < length - 1; i++) {
				x ^= ((bytes[i + 1] & 0x7f) << (4 + 7 * i));
			}
			return decodeZigZag32(x);
		} else {
			int x = (bytes[length - 1] & 0x78) >> 3;
			if (1 == length) {
				return decodeZigZag32(x);
			}
	
			for (int i = length - 1; i > 0; i--) {
				x ^= ((bytes[i - 1] & 0x7f) << (4 + 7 * (length - i -1)));
			}
			return decodeZigZag32(x);
		}
	}

	private static Long decodeVarLong(byte[] bytes) {
		int length = bytes.length;
		long x = (bytes[0] & 0x78) >> 3;
		if (1 == length) {
			return decodeZigZag64(x);
		}

		for (int i = 0; i < length - 1; i++) {
			x ^= ((long)(bytes[i + 1] & 0x7f) << (4 + 7 * i));
		}
		return decodeZigZag64(x);
	}
	
	private static Boolean decodeVarBoolean(byte[] bytes) {
		int length = bytes.length;
		int x = (bytes[0] & 0x78) >> 3;
		if (1 == length) {
			if (1 == x) {
				return true;
			} else {
				return false;
			}
		} else {
			throw new IllegalArgumentException(
					"error Boolean length, should be 1.");
		}
	}

}
