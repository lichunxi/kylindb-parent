/**
 * Created:2017年10月31日 下午4:13:32
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.util;

/**
 * @author lichunxi
 *
 */
public class Constant {
	/**
	 * the length of pointId, it's a long
	 */
	public static final Integer POINT_LENGTH = 8;
	
	/**
	 * the length of timestamp, it's a integer
	 */
	public static final Integer TIMESTAMP_LENGTH = 4;

	/** Mask to verify a timestamp on 4 bytes in seconds */
	public static final long SECOND_MASK = 0xFFFFFFFF00000000L;

	/** Mask to verify a timestamp on 6 bytes in milliseconds */
	public static final long MILLISECOND_MASK = 0xFFFFF00000000000L;

	/** Max time delta (in seconds) we can store in a column qualifier. */
	public static final short MAX_TIMESPAN = 3600;
	
	/**
	 * the length for qualifier
	 */
	public static final short QUALIFIER_SECOND_LENGTH = 2;
	public static final short QUALIFIER_MILLISECOND_LENGTH = 3;
	
	/**
	 * the length for value type
	 */
	public static final short VALUE_TYPE_LENGTH = 1;
	
	/**
	 * length for double, float
	 */
	public static final short DOUBLE_LENGTH = 8;
	public static final short FLOAT_LENGTH = 4;
	
}
