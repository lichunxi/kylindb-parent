/**
 * 
 */
package net.kylindb.stat.service;

import net.kylindb.stat.domain.RequestMetrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 专用于接口或消息统计的工具类
 * <p>
 * 传入接口名称，获得该接口名称对应的统计对象，统计对象中包含对该接口的请求总数、成功数、失败数、时延等信息
 * 
 * @author beny
 *
 */
public class RequestMetricsService {
	private static final Logger LOG = LoggerFactory.getLogger(RequestMetricsService.class);

	/**
	 * 接口对应的统计数据，key为接口名称
	 */
	private static final Map<String, RequestMetrics> requestMetrics = new ConcurrentHashMap<String, RequestMetrics>();

	private static Lock lock = new ReentrantLock();

	public static RequestMetrics getInstance(String interfaceName) {
		if (!requestMetrics.containsKey(interfaceName)) {
			lock.lock();
			try {
				if (!requestMetrics.containsKey(interfaceName)) {
					RequestMetrics metrics = new RequestMetrics();
					requestMetrics.put(interfaceName, metrics);
					return metrics;
				}
			} finally {
				lock.unlock();
			}
		}
		return requestMetrics.get(interfaceName);
	}

	public static void enable(String interfaceName) {
		RequestMetrics metrics = getInstance(interfaceName);
		if (null != metrics) {
			metrics.setMetricEnable(true);
			LOG.info("set metric enable, name={}", interfaceName);
		}
	}

	public static void enableAll() {
		for (Entry<String, RequestMetrics> entry : requestMetrics.entrySet()) {
			RequestMetrics metrics = entry.getValue();
			if (null != metrics) {
				metrics.setMetricEnable(true);
				LOG.info("set metric enable, name={}", entry.getKey());
			}
		}
	}

	public static void disable(String interfaceName) {
		RequestMetrics metrics = getInstance(interfaceName);
		if (null != metrics) {
			metrics.setMetricEnable(false);
			LOG.info("set metric disable, name={}", interfaceName);
		}
	}

	public static void disableAll() {
		for (Entry<String, RequestMetrics> entry : requestMetrics.entrySet()) {
			RequestMetrics metrics = entry.getValue();
			if (null != metrics) {
				metrics.setMetricEnable(false);
				LOG.info("set metric disable, name={}", entry.getKey());
			}
		}
	}

	public static String queryRequestMetricDataLine(String interfaceName) {
		RequestMetrics metrics = requestMetrics.get(interfaceName);
		if (null != metrics) {
			Long time = System.currentTimeMillis();
			return metrics.getDataLine(time, interfaceName);
		}
		return "NULL";
	}

	public static String queryRequestMetricDataJSON(String interfaceName) {
		RequestMetrics metrics = requestMetrics.get(interfaceName);
		if (null != metrics) {
			Long timestamp = System.currentTimeMillis();
			return metrics.getDataJson(timestamp, interfaceName);
		}
		return "{}";
	}

	public static String queryAllRequestMetricDataLine() {
		if (requestMetrics.size() > 0) {
			Long time = System.currentTimeMillis();
			StringBuilder buf = new StringBuilder();
			for (Entry<String, RequestMetrics> entry : requestMetrics
					.entrySet()) {
				buf.append(entry.getValue().getDataLine(time, entry.getKey()));
				buf.append("\n");
			}
			buf.deleteCharAt(buf.length() - 1);
			return buf.toString();
		}
		return "NULL";
	}

	public static String queryAllRequestMetricDataJSON() {
		Long timestamp = System.currentTimeMillis();
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		if (requestMetrics.size() > 0) {
			for (Entry<String, RequestMetrics> entry : requestMetrics
					.entrySet()) {
				String interfaceName = entry.getKey();
				RequestMetrics metrics = entry.getValue();
				if (null != metrics) {
					buf.append(metrics.getDataJson(timestamp, interfaceName));
					buf.append(",");
				}
			}
			buf.deleteCharAt(buf.length() - 1);
		}
		buf.append("]");
		return buf.toString();
	}

	public static void resetAll() {
		if (requestMetrics.size() > 0) {
			for (Entry<String, RequestMetrics> entry : requestMetrics
					.entrySet()) {
				entry.getValue().clear();
				LOG.info("reset metric, name={}", entry.getKey());
			}
		}
	}

	public static void reset(String interfaceName) {
		RequestMetrics metrics = getInstance(interfaceName);
		if (null != metrics) {
			metrics.clear();
			LOG.info("reset metric, name={}", interfaceName);
		}
	}
	
	public static void flipAll() {
		if (requestMetrics.size() > 0) {
			for (Entry<String, RequestMetrics> entry : requestMetrics
					.entrySet()) {
				entry.getValue().flip();
				LOG.debug("flip metric, name={}", entry.getKey());
			}
		}
	}

	public static void flip(String interfaceName) {
		RequestMetrics metrics = getInstance(interfaceName);
		if (null != metrics) {
			metrics.flip();
			LOG.debug("flip metric, name={}", interfaceName);
		}
	}
}
