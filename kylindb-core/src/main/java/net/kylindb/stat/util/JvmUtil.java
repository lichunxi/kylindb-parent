/**
 * 
 */
package net.kylindb.stat.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author beny
 *
 */
public class JvmUtil {
	private static final List<String> YOUNG_GEN_COLLECTOR_NAMES = Arrays
			.asList(new String[] {
					// Oracle (Sun) HotSpot
					// -XX:+UseSerialGC
					"Copy",
					// -XX:+UseParNewGC
					"ParNew",
					// -XX:+UseParallelGC
					"PS Scavenge",
					
					// Oracle (BEA) JRockit
					// -XgcPrio:pausetime
					"Garbage collection optimized for short pausetimes Young Collector",
					// -XgcPrio:throughput
					"Garbage collection optimized for throughput Young Collector",
					// -XgcPrio:deterministic
					"Garbage collection optimized for deterministic pausetimes Young Collector" });

	private static final List<String> OLD_GEN_COLLECTOR_NAMES = Arrays
			.asList(new String[] {
					// Oracle (Sun) HotSpot
					// -XX:+UseSerialGC
					"MarkSweepCompact",
					// -XX:+UseParallelGC and (-XX:+UseParallelOldGC or
					// -XX:+UseParallelOldGCCompacting)
					"PS MarkSweep",
					// -XX:+UseConcMarkSweepGC
					"ConcurrentMarkSweep",

					// Oracle (BEA) JRockit
					// -XgcPrio:pausetime
					"Garbage collection optimized for short pausetimes Old Collector",
					// -XgcPrio:throughput
					"Garbage collection optimized for throughput Old Collector",
					// -XgcPrio:deterministic
					"Garbage collection optimized for deterministic pausetimes Old Collector" });

	public static String getJvmInfo() {
		Runtime runtime = Runtime.getRuntime();
		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long maxMemory = runtime.maxMemory();
		StringBuilder buf = new StringBuilder();
		buf.append("start=");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		buf.append(dateFormat.format(new Date(ManagementFactory
				.getRuntimeMXBean().getStartTime())));
		buf.append(",load=");
		buf.append(ManagementFactory.getOperatingSystemMXBean()
				.getSystemLoadAverage());
		buf.append("\njava_version=");
		buf.append(System.getProperties().getProperty("java.version"));
		buf.append(",java_path=");
		buf.append(System.getProperties().getProperty("java.home"));
		
		// JVM内存
		buf.append("\n");
		buf.append("totalMemory=");
		buf.append(totalMemory);
		buf.append("(" + (totalMemory >> 10 >> 10));
		buf.append("MB),freeMemory=");
		buf.append(freeMemory);
		buf.append("(" + (freeMemory >> 10 >> 10));
		buf.append("MB),maxMemory=");
		buf.append(maxMemory);
		buf.append("(" + (maxMemory >> 10 >> 10));
		buf.append("MB)");
		
		// 内存详情：heap：young(eden + 2survive) + old  no-heap:perm + codeCache
		MemoryUsage memoryUsage = null;
		long used;
		long committed;
		Double usedRatio;
		for (MemoryPoolMXBean memoryPoolMXBean : ManagementFactory
				.getMemoryPoolMXBeans()) {
			memoryUsage = memoryPoolMXBean.getUsage();
			if (null != memoryUsage) {
				used = memoryUsage.getUsed();
				committed = memoryUsage.getCommitted();
				// 保留两位小数
				usedRatio = Math.round(used * 1.0 / committed * 10000) / 100.0;
				buf.append("\n" + memoryPoolMXBean.getType() + "("
						+ memoryPoolMXBean.getName() + "):\n");
				buf.append("used=" + used + "(" + (used >> 10 >> 10) + "MB),");
				buf.append("committed=" + committed + "("
						+ (committed >> 10 >> 10) + "MB),");
				buf.append("usedRatio=" + usedRatio + "%");
			}
		}
		
		// 线程
		buf.append("\n");
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		buf.append("threadCount=" + threadMXBean.getThreadCount());
		buf.append(",peakThreadCount=" + threadMXBean.getPeakThreadCount());
		
		// 内存gc
		for (GarbageCollectorMXBean garbageMXBean : ManagementFactory
				.getGarbageCollectorMXBeans()) {
			String name = garbageMXBean.getName();
			if (YOUNG_GEN_COLLECTOR_NAMES.contains(name)){
				buf.append("\nminor gc(" + name + "):\n");
				buf.append("count=" + garbageMXBean.getCollectionCount());
				buf.append(",time=" + garbageMXBean.getCollectionTime()
						+ "(milliseconds)");
			} else if(OLD_GEN_COLLECTOR_NAMES.contains(name)){
				buf.append("\nfull gc(" + name + "):\n");
				buf.append("count=" + garbageMXBean.getCollectionCount());
				buf.append(",time=" + garbageMXBean.getCollectionTime()
						+ "(milliseconds)");
			} else {
				// 应该不会到此处
				buf.append("\n" + name + ":\n");
				buf.append("count=" + garbageMXBean.getCollectionCount());
				buf.append(",time=" + garbageMXBean.getCollectionTime()
						+ "(milliseconds)");
			}
		}
		return buf.toString();
	}
}
