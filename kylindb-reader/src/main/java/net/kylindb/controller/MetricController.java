/**
 * Created:2017年11月20日 下午8:20:37
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.kylindb.client.domain.DataPoint;
import net.kylindb.client.IMetricDatumReader;
import net.kylindb.stat.domain.RequestMetrics;
import net.kylindb.stat.service.RequestMetricsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	private static ThreadLocal<DateFormat> threadLocal = new ThreadLocal<DateFormat>();
	
	@Resource
	private IMetricDatumReader reader;
	
	private static DateFormat getDateFormat(){
		DateFormat dateFormat = threadLocal.get();
		if (null == dateFormat){
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			threadLocal.set(dateFormat);
		}
		return dateFormat;
	}
	
	@RequestMapping(value = "/queryMetric")
	@ResponseBody
	public Object queryMetric(HttpServletRequest request,
			HttpServletResponse response,
			String pointIds,
			String start,
			String end,
			@RequestParam(name="withNotes", required= false, defaultValue="false") Boolean withNotes) {
		RequestMetrics stat = RequestMetricsService.getInstance("queryMetric");
		stat.start();
		try {
			if (null == pointIds || pointIds.trim().length() <= 0){
				throw new IllegalArgumentException("pointIds can't be null or empty");
			}
			if (null == start || start.trim().length() <= 0){
				throw new IllegalArgumentException("start can't be null or empty");
			}
			if (null == end || end.trim().length() <= 0){
				throw new IllegalArgumentException("end can't be null or empty");
			}
			
			String[] pointIdStrs = pointIds.split(",");
			Long[] pointIdList = new Long[pointIdStrs.length];
			for (int i=0; i<pointIdStrs.length; i++){
				pointIdList[i] = Long.valueOf(pointIdStrs[i]);
			}
			
			Long startTimestamp=0L;
			Long endTimestamp=0L;
			try {
				startTimestamp = getDateFormat().parse(start).getTime();
				endTimestamp = getDateFormat().parse(end).getTime();
			} catch (ParseException e) {
				throw new IllegalArgumentException("invalid startTime or endTime.should be yyyy/MM/dd");
			}
			
			// 最多10秒超时
			List<DataPoint> dataList = reader.queryMetricData(pointIdList, startTimestamp, endTimestamp, withNotes).addErrback((Exception e) ->{
				LOG.error("queryMetricData fail.", e);
				return null;
			}).joinUninterruptibly(10*1000L);
			stat.end();
			return dataList;
		} catch (Exception e) {
			LOG.error("query metric except.", e);
			stat.fail();
			return null;
		}
	}
	
	@RequestMapping(value = "/queryLatestMetric")
	@ResponseBody
	public Object queryLatestMetric(HttpServletRequest request,
			HttpServletResponse response,
			String pointIds,
			@RequestParam(name="withNotes", required= false, defaultValue="false") Boolean withNotes) {
		RequestMetrics stat = RequestMetricsService.getInstance("queryLatestMetric");
		stat.start();
		try {
			String[] pointIdStrs = pointIds.split(",");
			Long[] pointIdArray = new Long[pointIdStrs.length];
			for (int i=0; i<pointIdStrs.length; i++){
				pointIdArray[i] = Long.valueOf(pointIdStrs[i]);
			}
			// 最多10秒超时
			List<DataPoint> dataList = reader.queryLatestMetricData(pointIdArray, withNotes).addErrback((Exception e) ->{
				LOG.error("queryLatestMetric fail.", e);
				return null;
			}).joinUninterruptibly(10*1000L);
			stat.end();
			return dataList;
		} catch (Exception e) {
			LOG.error("query latest metric except.", e);
			stat.fail();
			return null;
		}
	}
}
