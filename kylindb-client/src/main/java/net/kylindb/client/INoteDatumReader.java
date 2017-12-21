/**
 * Created:2017年11月20日 上午10:29:11
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.client;

import java.util.List;

import com.stumbleupon.async.Deferred;

import net.kylindb.client.domain.Note;

/**
 * @author lichunxi
 *
 */
public interface INoteDatumReader {
	
	/**
	 * 按照pointId和时间段查询Note数据。时间段为：start<=time<end
	 * @param pointIds pointId列表
	 * @param start 开始时间戳，秒或毫秒
	 * @param end 结束时间戳
	 * @return 符合要求的数据集
	 */
	Deferred<List<Note>> queryNoteData(Long[] pointIds, Long start, Long end);
	
	/**
	 * 按照pointId和时间戳查询Note数据。
	 * @param pointId pointId
	 * @param time 时间戳
	 * @return 符合要求的Note数据
	 */
	Deferred<Note> queryNoteDatum(Long pointId, Long time);
}
