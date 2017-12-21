/**
 * Created:2017年11月22日 下午2:15:24
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricReader;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import net.kylindb.client.INoteDatumReader;
import net.kylindb.client.domain.Note;
import net.kylindb.orm.IDatumDao;

import com.stumbleupon.async.Deferred;

/**
 * @author lichunxi
 *
 */
@Service
public class DefaultNoteDatumReader implements INoteDatumReader {

	@Resource(name="noteDao")
	private IDatumDao<Note> noteReader;
	
	/* (non-Javadoc)
	 * @see net.kylindb.metricReader.INoteDatumReader#queryNoteData(java.lang.Long[], java.lang.Long, java.lang.Long)
	 */
	@Override
	public Deferred<List<Note>> queryNoteData(Long[] pointIds, Long start,
			Long end) {
		List<Deferred<List<Note>>> noteResults = new ArrayList<Deferred<List<Note>>>();
		for (Long pointId : pointIds){
			noteResults.add(noteReader.scan(pointId, start, end));
		}
		
		return Deferred.group(noteResults).addCallback((ArrayList<List<Note>> resultList) ->{
			List<Note> dataList = new ArrayList<Note>();
			for (List<Note> noteList : resultList){
				dataList.addAll(noteList);
			}
			return dataList;
		});
	}

	/* (non-Javadoc)
	 * @see net.kylindb.metricReader.INoteDatumReader#queryNoteDatum(java.lang.Long, java.lang.Long)
	 */
	@Override
	public Deferred<Note> queryNoteDatum(Long pointId, Long time) {
		return noteReader.get(pointId, time);
	}

}
