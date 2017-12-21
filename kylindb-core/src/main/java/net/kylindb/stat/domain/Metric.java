/**
 * 
 */
package net.kylindb.stat.domain;

/**
 * 监控项定义
 * 
 * @author beny
 *
 */
public class Metric<T> {
	/**
	 * 监控项名称
	 */
	private String name;

	/**
	 * 监控项名称别名，一般填写中文名称
	 */
	private String nameAlias;

	/**
	 * 监控项单位，合法值有： Seconds | Microseconds | Milliseconds | Bytes | Kilobytes |
	 * Megabytes | Gigabytes | Terabytes | Bits | Kilobits | Megabits | Gigabits
	 * | Terabits | Percent | Count | Bytes/Second | Kilobytes/Second |
	 * Megabytes/Second | Gigabytes/Second | Terabytes/Second | Bits/Second |
	 * Kilobits/Second | Megabits/Second | Gigabits/Second | Terabits/Second |
	 * Count/Second | None | RMB | USD
	 */
	private String unit;

	/**
	 * 监控项对应的tagk业务字段集合，用于更细粒度划分监控项
	 */
	private String[] tagKeys;

	/**
	 * 该监控项的当前值
	 */
	private T value;
	
	/**
	 * 该监控项的最近flip时的值
	 */
	private T preValue;

	public Metric(String name, String nameAlias, String unit, String[] tagKeys, T value, T preValue) {
		super();
		this.name = name;
		this.nameAlias = nameAlias;
		this.unit = unit;
		this.tagKeys = tagKeys;
		this.value = value;
		this.preValue = preValue;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder("{");
		buf.append("\"name\":\"" + name + "\",");
		buf.append("\"nameAlias\":\"" + nameAlias + "\",");
		buf.append("\"unit\":\"" + unit + "\",");
		buf.append("\"tagKeys\":[");
		for (String key : tagKeys) {
			buf.append("\"" + key + "\",");
		}
		if (tagKeys.length > 0) {
			buf.deleteCharAt(buf.length() - 1);
		}
		buf.append("]");
		buf.append(",\"value\":" + value);
		buf.append(",\"preValue\":" + preValue);
		buf.append("}");
		return buf.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameAlias() {
		return nameAlias;
	}

	public void setNameAlias(String nameAlias) {
		this.nameAlias = nameAlias;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String[] getTagKeys() {
		return tagKeys;
	}

	public void setTagKeys(String[] tagKeys) {
		this.tagKeys = tagKeys;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public T getPreValue() {
		return preValue;
	}
	
	public void setPreValue(T preValue) {
		this.preValue = preValue;
	}

}
