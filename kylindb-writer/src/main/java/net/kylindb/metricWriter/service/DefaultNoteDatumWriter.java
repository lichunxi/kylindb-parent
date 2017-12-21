/**
 * Created:2017年11月22日 下午3:03:37
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.metricWriter.service;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import net.kylindb.client.INoteDatumWriter;
import net.kylindb.coder.NotesCoder;
import net.kylindb.coder.QualifierCoder;
import net.kylindb.coder.RowKeyCoder;
import net.kylindb.client.domain.Note;
import net.kylindb.orm.IDatumDao;
import net.kylindb.stat.domain.RequestMetrics;
import net.kylindb.stat.service.RequestMetricsService;
import net.kylindb.util.TimeUtil;

import com.stumbleupon.async.Deferred;

/**
 * @author lichunxi
 *
 */
@Service
public class DefaultNoteDatumWriter implements INoteDatumWriter {
	public static Logger LOG = LoggerFactory
			.getLogger(DefaultNoteDatumWriter.class);
	
	@Resource(name="noteDao")
	private IDatumDao<Note> noteDao;
	
	@Override
	public Deferred<Object> putNoteDatum(Long pointId, Long timestamp,
			Map<String, String> notes) {
		RequestMetrics stat = RequestMetricsService.getInstance("putNoteDatum");
		stat.start();
		try{
			int baseTime = TimeUtil.getBaseTime(timestamp);
			
			// 拼装rowkey
			byte[] rowKeyBytes = RowKeyCoder.encode(pointId, baseTime);
			
			// 拼装Qualifier
			byte[] qualifierBytes = QualifierCoder.encode(timestamp, baseTime);
	
			byte[] noteBytes = NotesCoder.encode(notes);
			Deferred<Object> result = noteDao.put(rowKeyBytes, qualifierBytes, noteBytes);
			stat.end();
			return result;
		} catch(Exception e){
			LOG.error("putNoteDatum fail.", e);
			stat.fail();
			return Deferred.fromResult(0);
		}
	}
	
	@Override
	public Deferred<Object> updateNoteDatum(Long pointId, Long timestamp,
			Map<String, String> notes) {
		RequestMetrics stat = RequestMetricsService.getInstance("updateNoteDatum");
		stat.start();
		try{
			Deferred<Object> result = noteDao.get(pointId, timestamp).addCallback((Note note) ->{
				// 如果数据库中存在旧值，则更新
				if (null != note){
					Map<String, String> oldNotes = note.getNotes();
					if (null !=oldNotes && oldNotes.size() > 0){
						for (Entry<String, String> entry : oldNotes.entrySet()){
							// 老数据中存在，新数据中不存在，则保留
							// 老数据中不存在，新数据中存在，则增加
							notes.putIfAbsent(entry.getKey(), entry.getValue());
						}
					}
				}
				int baseTime = TimeUtil.getBaseTime(timestamp);
				
				// 拼装rowkey
				byte[] rowKeyBytes = RowKeyCoder.encode(pointId, baseTime);
				
				// 拼装Qualifier
				byte[] qualifierBytes = QualifierCoder.encode(timestamp, baseTime);
		
				byte[] noteBytes = NotesCoder.encode(notes);
				return noteDao.put(rowKeyBytes, qualifierBytes, noteBytes).addErrback((Exception e) ->{
					LOG.error("updateNoteDatum except.", e);
					return e;
				});
			});
			stat.end();
			return result;
		} catch(Exception e){
			LOG.error("updateNoteDatum fail.", e);
			stat.fail();
			return Deferred.fromResult(0);
		}
	}

}
