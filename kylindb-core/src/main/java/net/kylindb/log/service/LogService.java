/**
 * 
 */
package net.kylindb.log.service;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import net.kylindb.log.StatLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * 
 * @author beny
 *
 */
public class LogService {

	private static final Logger LOG = LoggerFactory
			.getLogger(LogService.class);
	private static final String DOMAIN_NAME = "net.kylindb.log";
	private static final String JMX_NAME = "logbackJmx";
	private MBeanServer mBeanServer = null;
	private JMXConfigurator reloadConfig = null;

	public LogService() {
		try {
			mBeanServer = ManagementFactory.getPlatformMBeanServer();
			// 注册服务
			ObjectName name = new ObjectName(DOMAIN_NAME + ":name=" + JMX_NAME);
			reloadConfig = new JMXConfigurator(
					(LoggerContext) LoggerFactory.getILoggerFactory(),
					mBeanServer, name);
		} catch (Exception e) {
			LOG.error("init LogService fail.", e);
		}
	}

	public void setLoggerLevel(String loggerName, String level) {
		reloadConfig.setLoggerLevel(loggerName, level);
	}

	public String getLoggerLevel(String loggerName) {
		return reloadConfig.getLoggerLevel(loggerName);
	}

	public String getLoggerEffectiveLevel(String loggerName) {
		return reloadConfig.getLoggerEffectiveLevel(loggerName);
	}

	public List<String> getLoggerList() {
		return reloadConfig.getLoggerList();
	}

	public static void enablePerformanceLog() {
		if(!StatLogger.getStart()){
			StatLogger.init();
			LOG.info("set performance log enable!");
		}
	}

	public static void disablePerformanceLog(){
		StatLogger.setStart(false);
		LOG.info("set performance log disable!");
	}
}
