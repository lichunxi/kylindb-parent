/**
 * Created:2017年11月13日 下午1:43:20
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.util;


/**
 * @author lichunxi
 *
 */
public class Bytes {
	  /**
	   * Reads a big-endian 4-byte integer from the begining of the given array.
	   * @param b The array to read from.
	   * @return An integer.
	   * @throws IndexOutOfBoundsException if the byte array is too small.
	   */
	  public static int getInt(final byte[] b) {
	    return getInt(b, 0);
	  }

	  /**
	   * Reads a big-endian 4-byte integer from an offset in the given array.
	   * @param b The array to read from.
	   * @param offset The offset in the array to start reading from.
	   * @return An integer.
	   * @throws IndexOutOfBoundsException if the byte array is too small.
	   */
	  public static int getInt(final byte[] b, final int offset) {
	    return (b[offset + 0] & 0xFF) << 24
	         | (b[offset + 1] & 0xFF) << 16
	         | (b[offset + 2] & 0xFF) << 8
	         | (b[offset + 3] & 0xFF) << 0;
	  }
	  
	  /**
	   * Writes a big-endian 4-byte int at the begining of the given array.
	   * @param b The array to write to.
	   * @param n An integer.
	   * @throws IndexOutOfBoundsException if the byte array is too small.
	   */
	  public static void setInt(final byte[] b, final int n) {
	    setInt(b, n, 0);
	  }

	  /**
	   * Writes a big-endian 4-byte int at an offset in the given array.
	   * @param b The array to write to.
	   * @param offset The offset in the array to start writing at.
	   * @throws IndexOutOfBoundsException if the byte array is too small.
	   */
	  public static void setInt(final byte[] b, final int n, final int offset) {
	    b[offset + 0] = (byte) (n >>> 24);
	    b[offset + 1] = (byte) (n >>> 16);
	    b[offset + 2] = (byte) (n >>>  8);
	    b[offset + 3] = (byte) (n >>>  0);
	  }
	  
	  /**
	   * Creates a new byte array containing a big-endian 4-byte integer.
	   * @param n An integer.
	   * @return A new byte array containing the given value.
	   */
	  public static byte[] fromInt(final int n) {
	    final byte[] b = new byte[4];
	    setInt(b, n);
	    return b;
	  }

	  /**
	   * Reads a big-endian 8-byte long from the begining of the given array.
	   * @param b The array to read from.
	   * @return A long integer.
	   * @throws IndexOutOfBoundsException if the byte array is too small.
	   */
	  public static long getLong(final byte[] b) {
	    return getLong(b, 0);
	  }

	  /**
	   * Reads a big-endian 8-byte long from an offset in the given array.
	   * @param b The array to read from.
	   * @param offset The offset in the array to start reading from.
	   * @return A long integer.
	   * @throws IndexOutOfBoundsException if the byte array is too small.
	   */
	  public static long getLong(final byte[] b, final int offset) {
	    return (b[offset + 0] & 0xFFL) << 56
	         | (b[offset + 1] & 0xFFL) << 48
	         | (b[offset + 2] & 0xFFL) << 40
	         | (b[offset + 3] & 0xFFL) << 32
	         | (b[offset + 4] & 0xFFL) << 24
	         | (b[offset + 5] & 0xFFL) << 16
	         | (b[offset + 6] & 0xFFL) << 8
	         | (b[offset + 7] & 0xFFL) << 0;
	  }

	  /**
	   * Writes a big-endian 8-byte long at the begining of the given array.
	   * @param b The array to write to.
	   * @param n A long integer.
	   * @throws IndexOutOfBoundsException if the byte array is too small.
	   */
	  public static void setLong(final byte[] b, final long n) {
	    setLong(b, n, 0);
	  }

	  /**
	   * Writes a big-endian 8-byte long at an offset in the given array.
	   * @param b The array to write to.
	   * @param offset The offset in the array to start writing at.
	   * @throws IndexOutOfBoundsException if the byte array is too small.
	   */
	  public static void setLong(final byte[] b, final long n, final int offset) {
	    b[offset + 0] = (byte) (n >>> 56);
	    b[offset + 1] = (byte) (n >>> 48);
	    b[offset + 2] = (byte) (n >>> 40);
	    b[offset + 3] = (byte) (n >>> 32);
	    b[offset + 4] = (byte) (n >>> 24);
	    b[offset + 5] = (byte) (n >>> 16);
	    b[offset + 6] = (byte) (n >>>  8);
	    b[offset + 7] = (byte) (n >>>  0);
	  }

	  /**
	   * Creates a new byte array containing a big-endian 8-byte long integer.
	   * @param n A long integer.
	   * @return A new byte array containing the given value.
	   */
	  public static byte[] fromLong(final long n) {
	    final byte[] b = new byte[8];
	    setLong(b, n);
	    return b;
	  }
}
