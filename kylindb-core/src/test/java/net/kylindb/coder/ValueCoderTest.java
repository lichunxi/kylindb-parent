/**
 * Created:2017年11月1日 下午2:24:33
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.coder;

import static org.junit.Assert.*;

import net.kylindb.coder.ValueCoder;

import org.hbase.async.Bytes;
import org.junit.Test;

/**
 * @author lichunxi
 *
 */
public class ValueCoderTest {
	
	/**
	 * Test method for {@link net.kylindb.coder.ValueCoder#encode(java.lang.Object)}.
	 */
	@Test
	public void testIntegerEncode1bit() { 
		int zvalue = 1;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {10};
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testNegativeIntegerEncode1bit() { 
		int value = -5;   //9
		byte[] a = {74};
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode2bit() { 
		int zvalue = 3;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {26};
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode3bit() { 
		int zvalue = 7;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {58};
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode4bit() { 
		int zvalue = 15;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {122};
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode5bit() { 
		int zvalue = 27;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {90,(byte)129};//2
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode6bit() { 
		int zvalue = 48;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {2,(byte)131};//2
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode10bit() { 
		int zvalue = 768;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {2,(byte)176};//2
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode11bit() { 
		int zvalue = 1536;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {2,(byte)224};//2
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode12bit() { 
		int zvalue = 2562;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {18,(byte)160,(byte)129};//3
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testNegativeIntegerEncode12bit() { 
		int value = -3424;
		byte[] a = {122,(byte)171,(byte)131};//3
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode17bit() { 
		int zvalue = 73730;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {18,(byte)128,(byte)164};//3
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	@Test
	public void testIntegerEncode18bit() { 
		int zvalue = 164370;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {18,(byte)161,(byte)208};//3
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	@Test
	public void testIntegerEncode19bit() { 
		int zvalue = 278808;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {66,(byte)145,(byte)136,(byte)129};
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode25bit() { 
		int zvalue = 17842705;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {10,(byte)161,(byte)136,(byte)196}; //4
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode26bit() { 
		int zvalue = 35726355;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {26,(byte)193,(byte)164,(byte)136,(byte)129}; //5 
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerEncode31bit() { 
		int zvalue = 1612726417;
		int value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {10,(byte)137,(byte)136,(byte)136,(byte)176}; //5
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongEncode32bit() { 
		long zvalue = 2433241363L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {27,(byte)145,(byte)138,(byte)194,(byte)200};  //5Byte
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testLongEncode38bit() { 
		long zvalue = 154619872288L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {3,(byte)194,(byte)128,(byte)132,(byte)128,(byte)164};  //6
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongEncode39bit() { 
		long zvalue = 343605773320L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {67,(byte)192,(byte)128,(byte)160,(byte)128,(byte)208}; //6
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongEncode45bit() { 
		long zvalue = 21990232818720L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {3,(byte)194,(byte)128,(byte)129,(byte)128,(byte)128,(byte)168}; //7
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongEncode46bit() { 
		long zvalue = 39582418608128L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {3,(byte)128,(byte)132,(byte)128,(byte)128,(byte)128,(byte)200}; //7
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongEncode52bit() { 
		long zvalue = 2323268069490688L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {3,(byte)128,(byte)128,(byte)128,(byte)128,(byte)128,(byte)130,(byte)161}; //8
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testLongEncode53bit() { 
		long zvalue = 4521191815513089L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {11,(byte)192,(byte)128,(byte)136,(byte)128,(byte)128,(byte)160,(byte)192}; //8
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongEncode59bit() { 
		long zvalue = 306244912101195840L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {3,(byte)132,(byte)128,(byte)132,(byte)128,(byte)160,(byte)128,(byte)128,(byte)162}; //9
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongEncode60bit() { 
		long zvalue = 612489549326581776L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {3,(byte)129,(byte)128,(byte)144,(byte)128,(byte)128,(byte)128,(byte)128,(byte)196}; //9
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongEncode63bit() { 
		long zvalue = 9214365112478990592L;
		long value = (zvalue >>> 1) ^ -(zvalue & 1);
		byte[] a = {3,(byte)144,(byte)128,(byte)132,(byte)128,(byte)192,(byte)128,(byte)128,(byte)255,(byte)135}; //10
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testBooleanEncodeTrue() { 
		boolean value = true;
		byte[] a = {12}; //1
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testBooleanEncodeFalse() { 
		boolean value = false;
		byte[] a = {4}; //1
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testInteger() { 
		int i = 1612726417;
		int a = (i << 1) ^ (i >> 31);
		System.out.println("zigzagInt:" + a);
		int b = (a >>> 1) ^ -(a & 1);
		System.out.println(b);
		
		int j = -806363209;
		int m = (j << 1) ^ (j >> 31);
		System.out.println("zigzagInt:" + m);
		int n = (m >>> 1) ^ -(m & 1);
		System.out.println(n);
	}
	
	@Test
	public void testLong() { 
		long i = 9214365112478990592L;
		long a = (i << 1) ^ (i >> 63);
		System.out.println("zigzagLong:" + a);
		long b = (a >>> 1) ^ -(a & 1);
		System.out.println(b);
	}
	
	
	@Test
	public void testNegative() { 
		int numberOfLeadingZeros = Long.numberOfLeadingZeros(-1);
		System.out.println("zeros:" + numberOfLeadingZeros);
	}
	
	@Test
	public void testDoubleEncode() { 
		double value = 3.1415926D;
//		long longBits = Double.doubleToLongBits(value);
//		System.out.println("longbits " + longBits);
		byte[] a = {0,64,9,33,(byte)251,77,18,(byte)216,74}; //9
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testFloatEncode() { 
		float value = 2.71233f;
//		int intBits = Float.floatToIntBits(value); // 1076729553
//		System.out.println("intbits " + intBits);
		byte[] a = {1,64,45,(byte)150,(byte)209}; //5
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	
	@Test
	public void testStringEncode() { 
		String value = "a";
//		System.out.println("bytes: " + Bytes.pretty(value.getBytes(Charset.forName("UTF-8"))));
		byte[] a = {5,18,'a'}; //3
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testStringEncode1() { 
		String value = "abc";
//		System.out.println("bytes: " + Bytes.pretty(value.getBytes(Charset.forName("UTF-8"))));
		byte[] a = {5,50,'a','b','c'}; //5
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testStringEncode2() { 
		String value = "abc-efg,12";
		byte[] a = {5,(byte)129,34,'a','b','c','-','e','f','g',',','1','2'}; 
		byte[] b = ValueCoder.encode(value);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testStringaa() { 
		Float a = 6039383.0f;
		Float b = 6036487.5f;
		System.out.println("Float:" + (a+b));
		
		Double a1 = 6039383.0d;
		Double b1 = 6036487.5d;
		System.out.println("Double:" + (a1+b1));
	}
	
	@Test
	public void testIntegerDecode4bit() { 
		Integer value = 2;
		byte[] b = ValueCoder.encode(value);
		Integer val = (Integer)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerDecode11bit() { 
		Integer value = 512;
		byte[] b = ValueCoder.encode(value);
		Integer val = (Integer)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerDecode18bit() { 
		Integer value = 73728;
		byte[] b = ValueCoder.encode(value);
		Integer val = (Integer)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerDecode25bit() { 
		Integer value = 8921088;
		byte[] b = ValueCoder.encode(value);
		Integer val = (Integer)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerDecode31bit() { 
		Integer value = 1082662912;
		byte[] b = ValueCoder.encode(value);
		Integer val = (Integer)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testIntegerDecode32bit() { 
		Integer value = 2147483647;
		byte[] b = ValueCoder.encode(value);
		Integer val = (Integer)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode4bit() { 
		Long value = 1L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode11bit() { 
		Long value = 10L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode19bit() { 
		Long value = 139264L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode26bit() { 
		Long value = 18874368L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode33bit() { 
		Long value = 2432696320L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode39bit() { 
		Long value = 171598436352L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode46bit() { 
		Long value = 19791213494784L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode53bit() { 
		Long value = 2251799815786496L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode60bit() { 
		Long value = 576460752378929152L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testLongDecode63bit() { 
		Long value = 5188146770806317056L;
		byte[] b = ValueCoder.encode(value);
		Long val = (Long)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testBooleanDecode() { 
		Boolean value = true;
		byte[] b = ValueCoder.encode(value);
		Boolean val = (Boolean)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testBooleanDecode1() { 
		Boolean value = false;
		byte[] b = ValueCoder.encode(value);
		Boolean val = (Boolean)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testStringDecode1() { 
		String value = "a";
		byte[] b = ValueCoder.encode(value);
		String val = (String)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testStringDecode52() { 
		String value = "System.arraycopy(src, srcPos, dest, destPos, length)";
		byte[] b = ValueCoder.encode(value);
		String val = (String)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testStringDecode1140() { 
		String value = "采用Varint,每个字节的第一位bit作为flag标识，1表示下一个Byte和自己是一起的，0表示到本byte结束，下一个byte是新的数字开始。剩下7bit用于存储真实的数值对应的bits数值,flag标识为0的byte，其中最后3bit作为数据类型type"
				+ "采用Varint,每个字节的第一位bit作为flag标识，1表示下一个Byte和自己是一起的，0表示到本byte结束，下一个byte是新的数字开始。剩下7bit用于存储真实的数值对应的bits数值,flag标识为0的byte，其中最后3bit作为数据类型type"
				+ "采用Varint,每个字节的第一位bit作为flag标识，1表示下一个Byte和自己是一起的，0表示到本byte结束，下一个byte是新的数字开始。剩下7bit用于存储真实的数值对应的bits数值,flag标识为0的byte，其中最后3bit作为数据类型type"
				+ "采用Varint,每个字节的第一位bit作为flag标识，1表示下一个Byte和自己是一起的，0表示到本byte结束，下一个byte是新的数字开始。剩下7bit用于存储真实的数值对应的bits数值,flag标识为0的byte，其中最后3bit作为数据类型type";
		byte[] b = ValueCoder.encode(value);
		String val = (String)ValueCoder.decode(b);
		if (val.equals(value)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
}
