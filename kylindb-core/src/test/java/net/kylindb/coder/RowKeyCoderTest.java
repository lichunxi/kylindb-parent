/**
 * Created:2017年10月31日 下午6:01:38
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.coder;

import static org.junit.Assert.*;
import net.kylindb.coder.RowKeyCoder;
import net.kylindb.domain.RowKey;

import org.hbase.async.Bytes;
import org.junit.Test;

/**
 * @author lichunxi
 *
 */
public class RowKeyCoderTest {

	/**
	 * Test method for {@link net.kylindb.coder.RowKeyCoder#encode(java.lang.Long, java.lang.Integer)}.
	 */
	@Test
	public void testEncode() {
		byte[] a = {'6',0,0,0,0,0,(byte)188,97,78,89,(byte)207,(byte)205,(byte)144};
		byte[] b = RowKeyCoder.encode(12345678L, 1506790800);
		if (Bytes.equals(a, b)){
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}
	
	@Test
	public void testDecode() {
		byte[] a = {'6',0,0,0,0,0,(byte)188,97,78,89,(byte)207,(byte)205,(byte)144};
		RowKey r = RowKeyCoder.decode(a);
		assertTrue((12345678L==r.getPointId()) 
				&& (1506790800 == r.getBaseTime()));
	}

}
