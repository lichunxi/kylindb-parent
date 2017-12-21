/**
 * 
 */
package net.kylindb.stat.domain;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 针对接口的统计项列表
 * <p>
 * 适用于各种接口调用时，统计成功次数、失败次数、请求时延等数据
 * 
 * @author beny
 *
 */
public class RequestMetrics {
	public static final String INTERFACE_NAME = "name"; /* 接口或函数名称 */
	public static final String[] TAG_KEYS = { INTERFACE_NAME };
	private static final String SPLITER = " ";
	private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("######0.00");   
	
	/**
	 * 统计开关 true-打开， false-关闭
	 */
	private AtomicBoolean metricEnable = new AtomicBoolean(true);

	/**
	 * 多线程时，请求都有一个startTime
	 */
	private ThreadLocal<Long> startTime = new ThreadLocal<Long>();

	/**
	 * end和fail函数调用互斥，调用过end函数后，不应该再调用fail函数，反之亦然
	 * <p>
	 * 此处封装该逻辑主要是为了业务使用简单，不需要在调用fail后必须记得写return
	 */
	private ThreadLocal<Boolean> flag = new ThreadLocal<Boolean>();

	/**
	 * 翻转时间戳，用于计算当前时刻距离上次翻转的时间长度
	 */
	private Long latestFlipTime = System.currentTimeMillis();
	
	/**
	 * 收到的总请求数
	 * <p>
	 * 总请求消息数=成功总数+失败总数+并发总数
	 */
	public Metric<AtomicLong> totalRequest = new Metric<AtomicLong>(
			"TotalRequest", "总请求数", "Count", TAG_KEYS, new AtomicLong(0), new AtomicLong(0));

	/**
	 * 处理成功请求数
	 */
	public Metric<AtomicLong> successRequest = new Metric<AtomicLong>(
			"SuccessRequest", "成功请求数", "Count", TAG_KEYS, new AtomicLong(0), new AtomicLong(0));

	/**
	 * 处理失败的请求数
	 */
	public Metric<AtomicLong> failRequest = new Metric<AtomicLong>(
			"FailRequest", "失败请求数", "Count", TAG_KEYS, new AtomicLong(0), new AtomicLong(0));

	/**
	 * 正在处理的请求数（既未成功，也未失败），即并发总数
	 */
	public Metric<AtomicLong> concurrentRequest = new Metric<AtomicLong>(
			"ConcurrentRequest", "并发请求数", "Count", TAG_KEYS, new AtomicLong(0), new AtomicLong(0));

	/**
	 * 所有请求的总响应时延，单位为毫秒
	 * <p>
	 * 每个请求的平均时延=totalLatency/(successRequest+failRequest)
	 */
	public Metric<AtomicLong> totalLatency = new Metric<AtomicLong>(
			"TotalLatency", "总时延", "Milliseconds", TAG_KEYS, new AtomicLong(0), new AtomicLong(0));

	/**
	 * 周期内最大时延，单位为毫秒
	 */
	public Metric<AtomicLong> maxLatency = new Metric<AtomicLong>("MaxLatency",
			"最大时延", "Milliseconds", TAG_KEYS, new AtomicLong(0), new AtomicLong(0));

	/**
	 * 周期内最小时延，单位为毫秒
	 */
	public Metric<AtomicLong> minLatency = new Metric<AtomicLong>("MinLatency",
			"最小时延", "Milliseconds", TAG_KEYS, new AtomicLong(0), new AtomicLong(0));

	/**
	 * 导出量，根据基础监控数据计算得到的监控数据
	 */
	public DerivedRequestMetrics derivedMetrics = new DerivedRequestMetrics();

	@Override()
	public String toString() {
		calculate();
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		buf.append(totalRequest.toString());
		buf.append(",");
		buf.append(successRequest.toString());
		buf.append(",");
		buf.append(failRequest.toString());
		buf.append(",");
		buf.append(concurrentRequest.toString());
		buf.append(",");
		buf.append(totalLatency.toString());
		buf.append(",");
		buf.append(maxLatency.toString());
		buf.append(",");
		buf.append(minLatency.toString());
		buf.append("]");
		return buf.toString();
	}
	
	public String getDataJson(Long time, String interfaceName){
		calculate();
		StringBuilder buf = new StringBuilder();
		buf.append("{\"time\":\"" + time);
		buf.append(",\"" + INTERFACE_NAME + "\":\"" + interfaceName + "\", \"metrics\":[");
		buf.append("{\"" + totalRequest.getName() + "\":" + (totalRequest.getValue().get() - totalRequest.getPreValue().get()));
		buf.append("},{\"" + derivedMetrics.tps.getName() + "\":" + DOUBLE_FORMAT.format(derivedMetrics.tps.getValue()));
		buf.append("},{\"" + successRequest.getName() + "\":" + (successRequest.getValue().get() - successRequest.getPreValue().get()));
		buf.append("},{\"" + failRequest.getName() + "\":" + (failRequest.getValue().get() - failRequest.getPreValue().get()));
		buf.append("},{\"" + concurrentRequest.getName() + "\":" + (concurrentRequest.getValue().get() - concurrentRequest.getPreValue().get()));
		buf.append("},{\"" + derivedMetrics.successRatio.getName() + "\":" + derivedMetrics.successRatio.getValue());
		buf.append("},{\"" + totalLatency.getName() + "\":" + (totalLatency.getValue().get() - totalLatency.getPreValue().get()));
		buf.append("},{\"" + maxLatency.getName() + "\":" + maxLatency.getValue().get());
		buf.append("},{\"" + minLatency.getName() + "\":" + minLatency.getValue().get());
		buf.append("},{\"" + derivedMetrics.latency.getName() + "\":" + derivedMetrics.latency.getValue());
		buf.append("}]");
		buf.append("}");
		return buf.toString();
	}

	public String getDataLine(Long time, String interfaceName) {
		calculate();
		StringBuilder buf = new StringBuilder();
		buf.append(totalRequest.getName() + SPLITER + time + SPLITER
				+ (totalRequest.getValue().get() - totalRequest.getPreValue().get()) + SPLITER + INTERFACE_NAME + "="
				+ interfaceName);
		buf.append("\n");
		buf.append(derivedMetrics.tps.getName() + SPLITER + time
				+ SPLITER + DOUBLE_FORMAT.format(derivedMetrics.tps.getValue()) + SPLITER
				+ INTERFACE_NAME + "=" + interfaceName);
		buf.append("\n");
		buf.append(successRequest.getName() + SPLITER + time + SPLITER
				+ (successRequest.getValue().get() - successRequest.getPreValue().get()) + SPLITER + INTERFACE_NAME + "="
				+ interfaceName);
		buf.append("\n");
		buf.append(failRequest.getName() + SPLITER + time + SPLITER
				+ (failRequest.getValue().get() - failRequest.getPreValue().get()) + SPLITER + INTERFACE_NAME + "="
				+ interfaceName);
		buf.append("\n");
		buf.append(concurrentRequest.getName() + SPLITER + time + SPLITER
				+ (concurrentRequest.getValue().get() - concurrentRequest.getPreValue().get()) + SPLITER + INTERFACE_NAME + "="
				+ interfaceName);
		buf.append("\n");
		buf.append(derivedMetrics.successRatio.getName() + SPLITER + time
				+ SPLITER + derivedMetrics.successRatio.getValue() + SPLITER
				+ INTERFACE_NAME + "=" + interfaceName);
		buf.append("\n");
		buf.append(totalLatency.getName() + SPLITER + time + SPLITER
				+ (totalLatency.getValue().get() - totalLatency.getPreValue().get()) + SPLITER + INTERFACE_NAME + "="
				+ interfaceName);
		buf.append("\n");
		buf.append(maxLatency.getName() + SPLITER + time + SPLITER
				+ maxLatency.getValue().get() + SPLITER + INTERFACE_NAME + "="
				+ interfaceName);
		buf.append("\n");
		buf.append(minLatency.getName() + SPLITER + time + SPLITER
				+ minLatency.getValue().get() + SPLITER + INTERFACE_NAME + "="
				+ interfaceName);
		buf.append("\n");
		buf.append(derivedMetrics.latency.getName() + SPLITER + time + SPLITER
				+ derivedMetrics.latency.getValue() + SPLITER + INTERFACE_NAME
				+ "=" + interfaceName);
		return buf.toString();
	}
	
	// 计算导出量
	private void calculate(){
		derivedMetrics.successRatio.setValue(deriveSuccessRatio());
		derivedMetrics.latency.setValue(deriveLatency());
		derivedMetrics.tps.setValue(deriveTps());
	}

	private Double deriveSuccessRatio() {
		double success = successRequest.getValue().get() - successRequest.getPreValue().get();
		long fail = failRequest.getValue().get() - failRequest.getPreValue().get();
		Double ration = Math.round(success / (success + fail) * 10000) / 100.0;
		return ration;
	}

	private Double deriveLatency() {
		double total = totalLatency.getValue().get() - totalLatency.getPreValue().get();
		long success = successRequest.getValue().get() - successRequest.getPreValue().get();
		long fail = failRequest.getValue().get() - failRequest.getPreValue().get();
		Double latency = Math.round(total / (success + fail) * 100) / 100.0;
		return latency;
	}
	
	private Double deriveTps() {
		double total = totalRequest.getValue().get() - totalRequest.getPreValue().get();
		long delta = (System.currentTimeMillis() - latestFlipTime) / 1000;
		return delta > 0 ? total / delta : 0D;
	}
	
	public static void main(String[] args){
		DecimalFormat df = new DecimalFormat("######0.00");  
		System.out.println(df.format(30.1455));
		System.out.println(df.format(0.35655));
		
		System.out.println(df.format(9871222232.46087));
		
	}

	/**
	 * 在请求到达时调用该方法，自动累加总请求数、并发数，设置开始时间
	 */
	public void start() {
		if (metricEnable.get()) {
			totalRequest.getValue().incrementAndGet();
			concurrentRequest.getValue().incrementAndGet();
			startTime.set(System.currentTimeMillis());
			flag.set(false);
		}
	}

	/**
	 * 在请求结束时调用该方法，自动累加成功请求数、并发数，时延
	 */
	public void end() {
		if (metricEnable.get() && !flag.get()) {
			concurrentRequest.getValue().decrementAndGet();
			successRequest.getValue().incrementAndGet();
			Long latency = System.currentTimeMillis() - startTime.get();
			totalLatency.getValue().addAndGet(latency);
			if (latency > maxLatency.getValue().get()) {
				maxLatency.getValue().set(latency);
			}
			if (latency < minLatency.getValue().get()
					|| minLatency.getValue().get() <= 0) {
				minLatency.getValue().set(latency);
			}
			flag.set(true);
		}
	}

	/**
	 * 在请求失败时调用该方法，自动累加失败请求数、并发数，时延
	 */
	public void fail() {
		if (metricEnable.get() && !flag.get()) {
			concurrentRequest.getValue().decrementAndGet();
			failRequest.getValue().incrementAndGet();
			Long latency = System.currentTimeMillis() - startTime.get();
			totalLatency.getValue().addAndGet(latency);
			if (latency > maxLatency.getValue().get()) {
				maxLatency.getValue().set(latency);
			}
			if (latency < minLatency.getValue().get()
					|| minLatency.getValue().get() <= 0) {
				minLatency.getValue().set(latency);
			}
			flag.set(true);
		}
	}

	public Boolean getMetricEnable() {
		return metricEnable.get();
	}

	public void setMetricEnable(Boolean metricEnable) {
		this.metricEnable.set(metricEnable);
	}

	/**
	 * 清空所有的计数器
	 */
	public void clear() {
		totalRequest.getValue().set(0L);
		successRequest.getValue().set(0L);
		failRequest.getValue().set(0L);
		concurrentRequest.getValue().set(0L);
		totalLatency.getValue().set(0L);
		maxLatency.getValue().set(0L);
		minLatency.getValue().set(0L);
	}
	
	/**
	 * 用于外部定时翻转，从而计算一段周期内的统计数据，例如5分钟的平均时延
	 * <br>什么时候翻转由外部调用者控制
	 */
	public void flip(){
		totalRequest.getPreValue().set(totalRequest.getValue().get());
		successRequest.getPreValue().set(successRequest.getValue().get());
		failRequest.getPreValue().set(failRequest.getValue().get());
		concurrentRequest.getPreValue().set(concurrentRequest.getValue().get());
		totalLatency.getPreValue().set(totalLatency.getValue().get());
		//对于最值不需要保存上次的值
		maxLatency.getValue().set(0L); 
		minLatency.getValue().set(0L);
		latestFlipTime = System.currentTimeMillis();
	}
}
