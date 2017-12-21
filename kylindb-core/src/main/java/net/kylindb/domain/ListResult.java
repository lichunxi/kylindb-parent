/**
 * 创建:  2015年9月9日
 * 作者:  beny
 * 杭州中恒云能源互联网技术有限公司
 */
package net.kylindb.domain;

import java.util.List;

/**
 * @author beny
 *
 */
public class ListResult<T> extends Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = 924734425329604463L;
	
	/**
	 * 记录集合
	 */
	private List<T> dataList;

	public List<T> getDataList() {
		return dataList;
	}

	public void setDataList(List<T> dataList) {
		this.dataList = dataList;
	}

}
