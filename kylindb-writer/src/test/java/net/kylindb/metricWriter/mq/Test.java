/**
 * Created:2017年11月23日 下午6:50:10
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricWriter.mq;

import org.hbase.async.Bytes;


/**
 * @author lichunxi
 *
 */
public class Test {

	public static void main(String[] args){
		// 4750679015621132288, 4750679015621132288
		String value = "4000000512";
//		Double d = 4000000016.8D;
		Double d = Double.parseDouble(value);
		Float f = d.floatValue();
		System.out.println(Double.parseDouble(value));
		System.out.println(Double.doubleToRawLongBits(d));
		System.out.println(Bytes.pretty(Bytes.fromLong(Double.doubleToRawLongBits(d))));
		Double a = Double.longBitsToDouble(Bytes.getLong(new byte[]{65, -19, -51, 101, 55, 25, -103, -103}));
		System.out.println(a);
	}
	
}
