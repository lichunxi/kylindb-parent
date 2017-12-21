package net.kylindb.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Result implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6866413938357794696L;
	
	public static final String SUCCESS = "success";

	/**
	 * 请求消息的唯一标识
	 */
	private Long requestId = 0L;

	/**
	 * 错误码，如果为success，说明成功
	 */
	private String code = SUCCESS;

	/**
	 * 错误码对应的错误描述
	 */
	private String message = null;
	
	/**
	 * 错误描述中的参数列表
	 */
	private Map<String, Object> params = null;

	public Result() {
		super();
	}
	
	/**
	 * @param requestId
	 * @param code
	 * @param message
	 */
	public Result(Long requestId, String code, String message) {
		super();
		this.requestId = requestId;
		this.code = code;
		this.message = message;
	}
	
	/**
	 * @param requestId
	 * @param code
	 * @param message
	 * @param params
	 */
	public Result(Long requestId, String code, String message,
			Map<String, Object> params) {
		super();
		this.requestId = requestId;
		this.code = code;
		this.message = message;
		this.params = params;
	}

	/**
	 * @return the requestId
	 */
	public Long getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the params
	 */
	public Map<String, Object> getParams() {
		return params;
	}
	
	public void addParam(String key, Object value){
		if (null == params){
			params = new HashMap<String, Object>();
		}
		this.params.put(key, value);
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

}
