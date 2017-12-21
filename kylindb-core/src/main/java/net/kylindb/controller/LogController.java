/**
 * 
 */
package net.kylindb.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.kylindb.log.service.LogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 日志专用入口
 * <p>
 * 
 * @author beny
 *
 */
@RequestMapping(value = "/log")
@Controller
public class LogController {
	private static final Logger LOG = LoggerFactory
			.getLogger(LogController.class);

	private LogService logServer = new LogService();

	/**
	 * 设置日志的级别
	 * */
	@RequestMapping(value = "/setLevel")
	@ResponseBody
	public void setLogLevel(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "level", required = false) String level) {
		logServer.setLoggerLevel(name, level);
		LOG.info("set {} log level to {}", name, level);
	}

	/**
	 * 查询日志的级别
	 * */
	@RequestMapping(value = "/getLevel")
	@ResponseBody
	public Object getLogLevel(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "name", required = false) String name) {
		String level = logServer.getLoggerLevel(name);
		// LOG.info("get {} log level, result={}", name, level);
		return level;
	}

	/**
	 * 查询日志的有效级别
	 * */
	@RequestMapping(value = "/getEffectiveLevel")
	@ResponseBody
	public Object getLogEffectiveLevel(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "name", required = false) String name) {
		String level = logServer.getLoggerEffectiveLevel(name);
		// LOG.info("get {} log effective level, result={}", name, level);
		return level;
	}

	/**
	 * 查询日志名称列表
	 * */
	@RequestMapping(value = "/getLoggerList")
	@ResponseBody
	public Object getLoggerList(HttpServletRequest request,
			HttpServletResponse response) {
		return logServer.getLoggerList();
	}

	/**
	 * 开启性能日志输出
	 * @return
	 */
	@RequestMapping("/enablePerformanceLog")
	@ResponseBody
	public Object enablePerformanceLog(){
		LogService.enablePerformanceLog();
		return "OK";
	}

	/**
	 * 关闭性能日志输出
	 * @return
	 */
	@RequestMapping("/disablePerformanceLog")
	@ResponseBody
	public Object disablePerformanceLog(){
		LogService.disablePerformanceLog();
		return "OK";
	}
}
