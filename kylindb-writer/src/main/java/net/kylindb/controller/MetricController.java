/**
 * Created:2017年11月20日 下午8:20:37
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.kylindb.client.domain.DataPoint;
import net.kylindb.client.IMetricDatumWriter;
import net.kylindb.stat.domain.RequestMetrics;
import net.kylindb.stat.service.RequestMetricsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author lichunxi
 *
 */
@RequestMapping(value = "/metric")
@Controller
public class MetricController {
	private static final Logger LOG = LoggerFactory
			.getLogger(MetricController.class);
	
	@Resource
	private IMetricDatumWriter writer;
	
	/**
	 * 简易的http格式put接口，数据格式为pointId timestamp value noteKey1=noteValue1 noteKey2=noteValue2 ...
	 * <br>
	 * 每行之间使用","分割，行内字段间用单个空格分割。timestamp为毫秒
	 * @param request
	 * @param response
	 * @param lines
	 * @return
	 */
	@RequestMapping(value = "/putMetricLine")
	@ResponseBody
	public Object queryMetric(HttpServletRequest request,
			HttpServletResponse response,
			String lines) {
		RequestMetrics stat = RequestMetricsService.getInstance("putMetricLine");
		stat.start();
		try {
			String[] metricLines = splitString(lines, ',');
			int count = 0;
			if (metricLines.length > 0){
				List<DataPoint> pointsList = new ArrayList<DataPoint>();
				for (String line : metricLines){
					if (line.trim().length() > 0){
						String[] metric = splitString(line, ' ');
						Long pointId=Long.valueOf(metric[0]);
						Long timestamp=Long.valueOf(metric[1]);
						Object value=metric[2];    //底层支持各种类型，此处直接使用string
						Map<String, String> notes = null;
						if (metric.length > 3){
							notes = new HashMap<String, String>();
							for (int i = 3; i < metric.length; i++){
								String[] noteKv = splitString(metric[i], '=');
								notes.put(noteKv[0], noteKv[1]);
							}
						}
						DataPoint dataPoint = new DataPoint(pointId, timestamp, value);
						if (null != notes){
							dataPoint.setNotes(notes);
						}
						pointsList.add(dataPoint);
					}
				}
				count = (Integer)writer.putMetricData(pointsList).joinUninterruptibly(30 * 1000L);
			}
			stat.end();
			return count;
		} catch (Exception e) {
			LOG.error("put metricLine except.", e);
			stat.fail();
			return 0;
		}
	}
	
	/**
	 * Optimized version of {@code String#split} that doesn't use regexps. This
	 * function works in O(5n) where n is the length of the string to split.
	 * 
	 * @param s
	 *            The string to split.
	 * @param c
	 *            The separator to use to split the string.
	 * @return A non-null, non-empty array.
	 */
	public static String[] splitString(final String s, final char c) {
		final char[] chars = s.toCharArray();
		int num_substrings = 1;
		for (final char x : chars) {
			if (x == c) {
				num_substrings++;
			}
		}
		final String[] result = new String[num_substrings];
		final int len = chars.length;
		int start = 0; // starting index in chars of the current substring.
		int pos = 0; // current index in chars.
		int i = 0; // number of the current substring.
		for (; pos < len; pos++) {
			if (chars[pos] == c) {
				result[i++] = new String(chars, start, pos - start);
				start = pos + 1;
			}
		}
		result[i] = new String(chars, start, pos - start);
		return result;
	}
	
}
