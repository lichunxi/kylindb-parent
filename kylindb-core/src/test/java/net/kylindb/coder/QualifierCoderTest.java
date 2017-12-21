/**
 * Created:2017年10月31日 下午7:33:38
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.coder;

import static org.junit.Assert.*;
import net.kylindb.coder.QualifierCoder;
import net.kylindb.domain.Qualifier;

import org.hbase.async.Bytes;
import org.junit.Test;

/**
 * @author lichunxi
 *
 */
public class QualifierCoderTest {

	// ////123
	/**
	 * Test method for
	 * {@link net.kylindb.coder.QualifierCoder#encode(java.lang.Long, java.lang.Integer)}
	 * .
	 */
	@Test
	public void testEncode() {
		byte[] a = { 65, (byte) 236 };
		byte[] b = QualifierCoder.encode(1506790923L, 1506790800);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testDecode() {
		byte[] a = { 65, (byte) 236 };
		Qualifier q = QualifierCoder.decode(a);
		assertTrue(1 == q.getType() && 123 == q.getDelta());
	}

	// ////0
	@Test
	public void testEncode0() {
		byte[] a = { 64, 0 };
		byte[] b = QualifierCoder.encode(1506790800L, 1506790800);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testDecode0() {
		byte[] a = { 64, 0 };
		Qualifier q = QualifierCoder.decode(a);
		assertTrue(1 == q.getType() && 0 == q.getDelta());
	}

	// ////3600
	@Test
	public void testEncode1() {
		byte[] a = { (byte) 120, 64 };
		byte[] b = QualifierCoder.encode(1506794400L, 1506790800);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testDecode1() {
		byte[] a = { (byte) 120, 64 };
		Qualifier q = QualifierCoder.decode(a);
		assertTrue(1 == q.getType() && 3600 == q.getDelta());
	}

	// ////3605
	@Test
	public void testEncode2() {
		byte[] a = { (byte) 120, 84 };
		byte[] b = QualifierCoder.encode(1506794405L, 1506790800);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testDecode2() {
		byte[] a = { (byte) 120, 84 };
		Qualifier q = QualifierCoder.decode(a);
		assertTrue(1 == q.getType() && 3605 == q.getDelta());
	}

	// ////123000
	@Test
	public void testEncodeMilli() {
		byte[] a = { (byte) 129, (byte) 224, (byte) 121 };
		byte[] b = QualifierCoder.encode(1506790923001L, 1506790800);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testDecodeMilli() {
		byte[] a = { (byte) 129, (byte) 224, (byte) 121 };
		Qualifier q = QualifierCoder.decode(a);
		assertTrue(2 == q.getType() && 123001 == q.getDelta());
	}

	// ////0
	@Test
	public void testEncodeMilli0() {
		byte[] a = { (byte) 128, 0, 0 };
		byte[] b = QualifierCoder.encode(1506790800000L, 1506790800);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testDecodeMilli0() {
		byte[] a = { (byte) 128, 0, 0 };
		Qualifier q = QualifierCoder.decode(a);
		assertTrue(2 == q.getType() && 0 == q.getDelta());
	}

	// ////3600000
	@Test
	public void testEncodeMilli1() {
		byte[] a = { (byte) 182, (byte) 238, (byte) 128 };
		byte[] b = QualifierCoder.encode(1506794400000L, 1506790800);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testDecodeMilli1() {
		byte[] a = { (byte) 182, (byte) 238, (byte) 128 };
		Qualifier q = QualifierCoder.decode(a);
		assertTrue(2 == q.getType() && 3600000 == q.getDelta());
	}

	// ////3605000
	@Test
	public void testEncodeMilli2() {
		byte[] a = { (byte) 183, 2, 8 };
		byte[] b = QualifierCoder.encode(1506794405000L, 1506790800);
		if (Bytes.equals(a, b)) {
			assertTrue(true);
		} else {
			assertTrue(false);
		}
	}

	@Test
	public void testDecodeMilli2() {
		byte[] a = { (byte) 183, 2, 8 };
		Qualifier q = QualifierCoder.decode(a);
		assertTrue(2 == q.getType() && 3605000 == q.getDelta());
	}
}
