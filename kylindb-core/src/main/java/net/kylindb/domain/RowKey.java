/**
 * Created:2017年10月31日 下午9:23:31
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.domain;

/**
 * @author lichunxi
 *
 */
public class RowKey {
	/**
	 * 测点Id
	 */
	private Long pointId;
	
	/**
	 * 每小时距离1970年1月1日原点的秒数
	 */
	private Integer baseTime;

	/**
	 * @return the pointId
	 */
	public Long getPointId() {
		return pointId;
	}

	/**
	 * @param pointId the pointId to set
	 */
	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}

	/**
	 * @return the baseTime
	 */
	public Integer getBaseTime() {
		return baseTime;
	}

	/**
	 * @param baseTime the baseTime to set
	 */
	public void setBaseTime(Integer baseTime) {
		this.baseTime = baseTime;
	}

}
