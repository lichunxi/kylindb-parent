/**
 * 
 */
package net.kylindb.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 性能日志工具类
 * 
 * @author beny
 *
 */
public class PerformanceLog {
	private static final String PERF_LOG = "PERF_LOG";
	private static final Logger LOG = LoggerFactory.getLogger(PERF_LOG);

	/**
	 * 记录性能日志
	 * 
	 * @param jvmInfo
	 *            JVM虚拟机相关监控数据
	 * @param metrics
	 *            业务相关监控统计数据
	 */
	public static void log(String jvmInfo, String metrics) {
		StringBuilder text = new StringBuilder();
		text.append("jvmInfo:\n");
		text.append(jvmInfo);
		text.append("\n");
		text.append("metrics:\n");
		text.append(metrics);
		LOG.info(text.toString());
	}
}
