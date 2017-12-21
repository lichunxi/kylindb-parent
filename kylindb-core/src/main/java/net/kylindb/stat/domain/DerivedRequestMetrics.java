/**
 * 
 */
package net.kylindb.stat.domain;

/**
 * 根据原始统计数据生成的导出量
 * 
 * @author beny
 *
 */
public class DerivedRequestMetrics {
	/**
	 * 请求成功率
	 * <p>
	 * 成功率=成功总数/(成功总数+失败总数)*100%
	 */
	public Metric<Double> successRatio = new Metric<Double>("SuccessRatio",
			"请求成功率", "Percent", RequestMetrics.TAG_KEYS, 0D, 0D);

	/**
	 * 平均请求时延
	 * <p>
	 * 平均时延=总时延/(成功总数+失败总数)
	 */
	public Metric<Double> latency = new Metric<Double>("Latency", "平均请求时延",
			"Milliseconds", RequestMetrics.TAG_KEYS, 0D, 0D);

	/**
	 * TPS平均每秒请求数
	 * <p>
	 * tps=总请求数/时间长度
	 */
	public Metric<Double> tps = new Metric<Double>("tps", "平均每秒请求数",
			"Count/Second", RequestMetrics.TAG_KEYS, 0D, 0D);
}
