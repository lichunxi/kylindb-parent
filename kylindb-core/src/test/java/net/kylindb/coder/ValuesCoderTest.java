/**
 * Created:2017年11月13日 下午4:57:49
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.coder;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.kylindb.coder.ValueCoder;
import net.kylindb.coder.ValuesCoder;

import org.hbase.async.Bytes;
import org.junit.Test;

/**
 * @author lichunxi
 *
 */
public class ValuesCoderTest {

	@Test
	public void testEncodeInteger() {
		Integer[] values = new Integer[]{1,2,3};
		byte[] a = {18,34,50};
		try {
			byte[] b = ValuesCoder.encode(values);
			if (Bytes.equals(a, b)) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEncodeInteger1() {
		Integer[] values = new Integer[]{100,2345,3657998};
		byte[] a = {66,(byte)140,18,(byte)165,(byte)130,98,(byte)161,(byte)244,(byte)155};
		try {
			byte[] b = ValuesCoder.encode(values);
			if (Bytes.equals(a, b)) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEncodeLong() {
		Long[] values = new Long[]{234L,98766L,76544472L};
		byte[] a = {35,(byte)157,99,(byte)185,(byte)224,3,(byte)187,(byte)254,(byte)199,(byte)132};
		try {
			byte[] b = ValuesCoder.encode(values);
			if (Bytes.equals(a, b)) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEncodeBoolean() {
		Boolean[] values = new Boolean[]{true,false,true,false};
		byte[] a = {12,4,12,4};
		try {
			byte[] b = ValuesCoder.encode(values);
			if (Bytes.equals(a, b)) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEncodeDouble() {
		Double[] values = new Double[]{2.14D,3.2569D,8D,98.0D};
//		for (Double d : values){
//			System.out.println(Bytes.pretty(ValueCoder.encode(d)));
//	    }
		byte[] a = {0, 64, 1, 30, -72, 81, -21, -123, 31, 0, 64, 10, 14, 33, -106, 82, -67, 60, 0, 64, 32, 0, 0, 0, 0, 0, 0, 0, 64, 88, -128, 0, 0, 0, 0, 0};
		try {
			byte[] b = ValuesCoder.encode(values);
			if (Bytes.equals(a, b)) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEncodeFloat() {
		Float[] values = new Float[]{3.14f,32.12f,3f,76.0f};
		for (Float d : values){
			System.out.println(Bytes.pretty(ValueCoder.encode(d)));
	    }
		byte[] a = {1, 64, 72, -11, -61, 1, 66, 0, 122, -31, 1, 64, 64, 0, 0, 1, 66, -104, 0, 0};
		try {
			byte[] b = ValuesCoder.encode(values);
			if (Bytes.equals(a, b)) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEncodeString() {
		String[] values = new String[]{"12b","中国","ddsfsabcdfdsdgd","hello"};
//		for (String str : values){
//			System.out.println(Bytes.pretty(str.getBytes(Charset.forName("UTF-8"))));
//		}
		
		byte[] a = {5,50,'1','2','b',5,98,-28, -72, -83, -27, -101, -67,5,(byte)129,114,'d','d','s','f','s','a','b','c','d','f','d','s','d','g','d',5,82,'h','e','l','l','o'};
		try {
			byte[] b = ValuesCoder.encode(values);
			if (Bytes.equals(a, b)) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEncodeMix() {
		List<Object> list = new ArrayList<Object>();
		list.add(1876);
		list.add(3.14f);
		list.add(2.14D);
		list.add(true);
		list.add("中国");
		list.add(1);
		
		
//		for (String str : values){
//			System.out.println(Bytes.pretty(str.getBytes(Charset.forName("UTF-8"))));
//		}
		
		byte[] a = {66,-22,-127,1, 64, 72, -11, -61,0, 64, 1, 30, -72, 81, -21, -123, 31,12,5,98,-28, -72, -83, -27, -101, -67, 18};
		try {
			byte[] b = ValuesCoder.encode(list.toArray());
			if (Bytes.equals(a, b)) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Test
	public void testDecodeInteger() {
		Integer[] a = new Integer[]{1,2,3};
		byte[] values = {18,34,50};
		Object[] b = ValuesCoder.decode(values);
		for (int i = 0; i < a.length; i++){
			Integer c = (Integer)b[i];
			if (c != a[i]){
				assertTrue(false);
			}
		}
		assertTrue(true);
	}
	
	@Test
	public void testDecodeInteger1() {
		Integer[] a = new Integer[]{100,2345,3657998};
		byte[] values = {66,(byte)140,18,(byte)165,(byte)130,98,(byte)161,(byte)244,(byte)155};
		Object[] b = ValuesCoder.decode(values);
		for (int i = 0; i < a.length; i++){
			Integer c = (Integer)b[i];
			if (!c.equals(a[i])){
				assertTrue(false);
			}
		}
		assertTrue(true);
	}
	
	@Test
	public void testDecodeLong() {
		Long[] a = new Long[]{234L,98766L,76544472L};
		byte[] values = {35,(byte)157,99,(byte)185,(byte)224,3,(byte)187,(byte)254,(byte)199,(byte)132};
		Object[] b = ValuesCoder.decode(values);
		for (int i = 0; i < a.length; i++){
			Long c = (Long)b[i];
			if (!c.equals(a[i])){
				assertTrue(false);
			}
		}
		assertTrue(true);
	}
	
	@Test
	public void testDecodeBoolean() {
		Boolean[] a = new Boolean[]{true,false,true,false};
		byte[] values = {12,4,12,4};
		Object[] b = ValuesCoder.decode(values);
		for (int i = 0; i < a.length; i++){
			Boolean c = (Boolean)b[i];
			if (c != a[i]){
				assertTrue(false);
			}
		}
		assertTrue(true);
	}
	
	@Test
	public void testDecodeDouble() {
		Double[] a = new Double[]{2.14D,3.2569D,8D,98.0D};
		byte[] values = {0, 64, 1, 30, -72, 81, -21, -123, 31, 0, 64, 10, 14, 33, -106, 82, -67, 60, 0, 64, 32, 0, 0, 0, 0, 0, 0, 0, 64, 88, -128, 0, 0, 0, 0, 0};
		
		Object[] b = ValuesCoder.decode(values);
		for (int i = 0; i < a.length; i++){
			Double c = (Double)b[i];
			if (!c.equals(a[i])){
				assertTrue(false);
			}
		}
		assertTrue(true);
	}
	
	@Test
	public void testDecodeFloat() {
		Float[] a = new Float[]{3.14f,32.12f,3f,76.0f};
		byte[] values = {1, 64, 72, -11, -61, 1, 66, 0, 122, -31, 1, 64, 64, 0, 0, 1, 66, -104, 0, 0};
		Object[] b = ValuesCoder.decode(values);
		for (int i = 0; i < a.length; i++){
			Float c = (Float)b[i];
			if (!c.equals(a[i])){
				assertTrue(false);
			}
		}
		assertTrue(true);
	}
	
	@Test
	public void testDecodeString() {
		String[] a = new String[]{"12b","中国","ddsfsabcdfdsdgd","hello"};
		byte[] values = {5,50,'1','2','b',5,98,-28, -72, -83, -27, -101, -67,5,(byte)129,114,'d','d','s','f','s','a','b','c','d','f','d','s','d','g','d',5,82,'h','e','l','l','o'};
		Object[] b = ValuesCoder.decode(values);
		for (int i = 0; i < a.length; i++){
			String c = (String)b[i];
			if (!c.equals(a[i])){
				assertTrue(false);
			}
		}
		assertTrue(true);
	}
	
	@Test
	public void testDecodeMix() {
		List<Object> list = new ArrayList<Object>();
		list.add(1876);
		list.add(3.14f);
		list.add(2.14D);
		list.add(true);
		list.add("中国");
		list.add(1);
		
//		Object[] a = list.toArray();
		byte[] values = {66,-22,-127,1, 64, 72, -11, -61,0, 64, 1, 30, -72, 81, -21, -123, 31,12,5,98,-28, -72, -83, -27, -101, -67, 18};
		Object[] b = (Object[])ValuesCoder.decode(values);
		if (1876 != (Integer)b[0]){
			assertTrue(false);
		}
		
		if (3.14f != (Float)b[1]){
			assertTrue(false);
		}
		
		if (2.14D != (Double)b[2]){
			assertTrue(false);
		}
		
		if (true != (Boolean)b[3]){
			assertTrue(false);
		}
		
		if (!"中国".equals((String)b[4])){
			assertTrue(false);
		}
		
		if (1 != (Integer)b[5]){
			assertTrue(false);
		}
	}


}
