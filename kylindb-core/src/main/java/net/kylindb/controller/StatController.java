/**
 * 
 */
package net.kylindb.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.kylindb.stat.service.RequestMetricsService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统计数据专用入口
 * 
 * @author beny
 *
 */
@RequestMapping(value = "/stat")
@Controller
public class StatController {
//	private static final Logger LOG = LoggerFactory
//			.getLogger(StatController.class);

	/**
	 * 输出JSON格式监控统计信息
	 * */
	@RequestMapping(value = "/json", produces = "application/json;charset=UTF-8")
	@ResponseBody
	public String queryStatJSON(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "name", required = false) String name) {
		if (null != name) {
			return RequestMetricsService.queryRequestMetricDataJSON(name);
		} else {
			return RequestMetricsService.queryAllRequestMetricDataJSON();
		}
	}

	/**
	 * 输出JSON格式监控统计信息
	 * */
	@RequestMapping(value = "/line", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public String queryStatLine(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "name", required = false) String name) {
		if (null != name) {
			return RequestMetricsService.queryRequestMetricDataLine(name);
		} else {
			return RequestMetricsService.queryAllRequestMetricDataLine();
		}
	}
	
	/**
	 * 重置统计数据
	 * @param request
	 * @param response
	 * @param name  接口名称，如果不填写则重置该服务下所有的接口请求统计数据
	 */
	@RequestMapping(value = "/reset")
	public void reset(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "name", required = false) String name) {
		if (null != name) {
			RequestMetricsService.reset(name);
		} else {
			RequestMetricsService.resetAll();
		}
	}
	
	/**
	 * 打开统计数据开关
	 * @param request
	 * @param response
	 * @param name  接口名称，如果不填写则表示对服务下的所有接口进行操作
	 */
	@RequestMapping(value = "/enable")
	public void enable(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "name", required = false) String name) {
		if (null != name) {
			RequestMetricsService.enable(name);
		} else {
			RequestMetricsService.enableAll();
		}
	}
	
	/**
	 * 关闭统计数据开关
	 * @param request
	 * @param response
	 * @param name  接口名称，如果不填写则表示对服务下的所有接口进行操作
	 */
	@RequestMapping(value = "/disable")
	public void disable(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "name", required = false) String name) {
		if (null != name) {
			RequestMetricsService.disable(name);
		} else {
			RequestMetricsService.disableAll();
		}
	}
}
