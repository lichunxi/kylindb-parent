/**
 * Created:2017年10月31日 下午8:27:34
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.domain;

/**
 * @author lichunxi
 *
 */
public class Qualifier{
	/**
	 * 时间类型：1表示秒，2表示毫秒
	 */
	private int type;
	
	/**
	 * 距离baseTime的秒数或毫秒数。<br>
	 * 当type为秒是，delta为秒；type为毫秒时，delta为毫秒
	 */
	private int delta;

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return the delta
	 */
	public int getDelta() {
		return delta;
	}

	/**
	 * @param delta the delta to set
	 */
	public void setDelta(int delta) {
		this.delta = delta;
	}
	
}