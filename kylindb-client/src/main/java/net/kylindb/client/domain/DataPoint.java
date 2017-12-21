/**
 * Created:2017年10月27日 下午3:23:46
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.client.domain;

import java.util.Map;

/**
 * @author lichunxi
 *
 */
public class DataPoint {
	/**
	 * 测点唯一标识
	 */
	private Long pointId;
	
	/**
	 * 时间戳，距离1970年1月1日原点的秒数或毫秒数
	 * 写入的时候，秒或者毫秒都可以，内部会自动识别
	 * 读取的时候，会全部转换为毫秒
	 */
	private Long timestamp;
	
	/**
	 * 该测点在该时间点上的测量值
	 */
	private Object value;
	
	/**
	 * 针对该条数据的一些备注信息，例如最大值产生的时间：maxTime=1509724800，数据异常isInvalid=true，该时间点上的操作action=开机<br>
	 * 不应该把pointId的维度信息放在此字段，例如传感器Id，机器名称等，这些都是pointId的限定信息<br>
	 * notes不应该用于数据过滤，如果要过滤，应该外建索引和pointId关联
	 */
	private Map<String, String> notes;

	/**
	 * @param pointId
	 * @param timestamp
	 * @param value
	 */
	public DataPoint(Long pointId, Long timestamp, Object value) {
		super();
		this.pointId = pointId;
		this.timestamp = timestamp;
		this.value = value;
	}

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
	 * @return the timestamp
	 */
	public Long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the notes
	 */
	public Map<String, String> getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(Map<String, String> notes) {
		this.notes = notes;
	}
	
}
