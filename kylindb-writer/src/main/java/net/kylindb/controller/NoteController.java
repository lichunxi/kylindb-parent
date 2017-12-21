/**
 * Created:2017年11月20日 下午8:20:37
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.kylindb.client.INoteDatumWriter;
import net.kylindb.stat.domain.RequestMetrics;
import net.kylindb.stat.service.RequestMetricsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author lichunxi
 *
 */
@RequestMapping(value = "/note")
@Controller
public class NoteController {
	private static final Logger LOG = LoggerFactory
			.getLogger(NoteController.class);
	
	@Resource
	private INoteDatumWriter writer;
	
	@RequestMapping(value = "/addNote")
	@ResponseBody
	public Object addNote(HttpServletRequest request,
			HttpServletResponse response,
			Long pointId, Long timestamp, String notes) {
		RequestMetrics stat = RequestMetricsService.getInstance("addNote");
		stat.start();
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, String> notesMap = mapper.readValue(notes, new TypeReference<Map<String, String>>(){});
			Object result = writer.updateNoteDatum(pointId, timestamp, notesMap).joinUninterruptibly(10*1000L);
			stat.end();
			return result;
		} catch (Exception e) {
			LOG.error("put note except.", e);
			stat.fail();
			return "";
		}
	}
}
