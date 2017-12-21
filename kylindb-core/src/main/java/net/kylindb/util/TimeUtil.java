/**
 * Created:2017年10月31日 下午5:52:49
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.util;

/**
 * @author lichunxi
 *
 */
public class TimeUtil {

	public static int getBaseTime(long timestamp){
		long baseTime;
		if ((timestamp & Constant.SECOND_MASK) != 0) {
			// 如果是毫秒，取到小时
			baseTime = ((timestamp / 1000) - ((timestamp / 1000) % Constant.MAX_TIMESPAN));
		} else {
			baseTime = (timestamp - (timestamp % Constant.MAX_TIMESPAN));
		}
		return (int)baseTime;
	}
	
	// 获取毫秒
	public static long getMilisecond(long timestamp){
		if ((timestamp & Constant.SECOND_MASK) != 0) {
			// 如果是毫秒直接返回
			return timestamp;
		} else {
			return timestamp * 1000;
		}
	}
}
