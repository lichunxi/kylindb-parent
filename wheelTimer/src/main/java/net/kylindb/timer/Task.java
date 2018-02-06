/**
 * 
 */
package net.kylindb.timer;

/**
 * 定时器到期后触发的任务
 * 
 * @author lichunxi
 *
 */
public interface Task {
	
	/**
	 * 到期后触发的函数
	 */
	void run();
}
