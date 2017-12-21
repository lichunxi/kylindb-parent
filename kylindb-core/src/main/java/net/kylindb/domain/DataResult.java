package net.kylindb.domain;


public class DataResult<T> extends Result {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5789764650870022341L;
	
	private T data;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
