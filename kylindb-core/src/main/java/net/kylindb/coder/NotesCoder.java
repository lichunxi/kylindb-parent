/**
 * Created:2017年11月15日 上午11:51:13
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.coder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.protobuf.InvalidProtocolBufferException;

import net.kylindb.coder.NotesMap.NoteData;
import net.kylindb.coder.NotesMap.NoteData.MapFieldEntry;

/**
 * @author lichunxi
 *
 */
public class NotesCoder {
	
	public static byte[] encode(Map<String, String> values){
		if (null == values){
			return null;
		}
		NoteData.Builder mapBuilder = NoteData.newBuilder();
		for (Entry<String, String> entry : values.entrySet()){
			MapFieldEntry.Builder entryBuilder = MapFieldEntry.newBuilder();
			entryBuilder.setKey(entry.getKey());
			entryBuilder.setValue(entry.getValue());
			mapBuilder.addNotes(entryBuilder.build());
		}
		return mapBuilder.build().toByteArray();
	}

	public static Map<String, String> decode(byte[] bytes) throws InvalidProtocolBufferException{
		NoteData mapData = NoteData.parseFrom(bytes);
		List<MapFieldEntry> notesList = mapData.getNotesList();
		Map<String, String> notes = new HashMap<String, String>();
		if (null != notesList){
			for (MapFieldEntry entry : notesList){
				notes.put(entry.getKey(), entry.getValue());
			}
		}
		return notes;
	}
}
