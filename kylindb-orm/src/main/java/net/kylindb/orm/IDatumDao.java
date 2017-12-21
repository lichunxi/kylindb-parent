/**
 * Created:2017年10月27日 下午5:14:15
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.orm;

import java.util.List;

import com.stumbleupon.async.Deferred;

/**
 * 和数据库的操作接口
 * 
 * @author lichunxi
 *
 */
public interface IDatumDao<T> {

	/**
	 * 数据存储
	 * 
	 * @param key
	 *            rowKey
	 * @param qualifier
	 *            column name
	 * @param value
	 *            the value to store
	 * @return A deferred object that indicates the completion of the request.
	 *         This Integer indicate the success records putting into db.
	 */
	Deferred<Object> put(byte[] key, byte[] qualifier, byte[] value);
	
	/**
	 * 根据timestamp采用get方式查询数据
	 * 
	 * @param pointId
	 * @param timestamp  时间，毫秒
	 * 
	 * @return A deferred T that matched the get request.
	 */
	 Deferred<T> get(long pointId, long timestamp);

	 /**
	  * 根据start和end采用scan方式查询数据
	  * 
	  * @param pointId
	  * @param start
	  * @param end
	  * @return A deferred list of T that matched the get request.
	  */
	 Deferred<List<T>> scan(long pointId, long start, long end);
}
