/**
 * Created:2017年11月21日 下午2:29:15
 * Author:lichunxi
 * <http://www.kylindb.net> ®All Rights Reserved
 */
package net.kylindb.orm;

import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.hbase.async.GetRequest;
import org.hbase.async.HBaseClient;
import org.hbase.async.KeyValue;
import org.hbase.async.PutRequest;
import org.hbase.async.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.stumbleupon.async.Deferred;

/**
 * @author lichunxi
 *
 */
@Component
public class HbaseService {
	
	public static Logger LOG = LoggerFactory
			.getLogger(HbaseService.class);

	@Resource
	private HBaseClient client;
	
	public Deferred<Object> put(byte[] table, byte[] key, byte[] family, byte[] qualifier, byte[] value) {
		Deferred<Object> result = client.put(new PutRequest(table, key, family, qualifier, value));
		result.addCallback(object ->{
			return 1;   // client.put中返回的object没有意义，此处使用1来表示成功插入1条记录
		});
		return result;
	}
	
	public Deferred<Object> put(byte[] table, byte[] key, byte[]family, byte[][] qualifiers, byte[][] values) {
		Deferred<Object> result = client.put(new PutRequest(table, key, family, qualifiers, values));
		result.addCallback(object ->{
			return values.length;   // client.put中返回的object没有意义，此处使用values.length来表示成功插入N条记录
		});
		return result;
	}
	
	public Deferred<ArrayList<KeyValue>> get(byte[] table, byte[] key, byte[] family) {
		return client.get(new GetRequest(table, key, family));
	}
	
	public Deferred<ArrayList<KeyValue>> get(byte[] table, byte[] key, byte[] family, byte[] qualifier) {
		return client.get(new GetRequest(table, key, family, qualifier));
	}
	

	public Scanner getScanner(byte[] table, byte[] family, byte[] startRow, byte[] endRow, int maxNumRows){
	    Scanner scanner = client.newScanner(table);
	    scanner.setStartKey(startRow);
	    scanner.setStopKey(endRow);
	    scanner.setFamily(family);
	    scanner.setMaxNumRows(maxNumRows);
	    return scanner;
	}

	@PostConstruct
	public void init() {
		// 检查几个表是否存在
		client.ensureTableExists("metrics".getBytes(Charset.forName("UTF-8")));
		client.ensureTableExists("notes".getBytes(Charset.forName("UTF-8")));
		client.ensureTableExists("offsets".getBytes(Charset.forName("UTF-8")));
	}
	
	@PreDestroy
	public void destroy() {
		// 把内存中的数据全部刷新到表中
		client.flush().addCallback(result -> {
			// 关闭数据库连接
			return client.shutdown().addCallback(r -> {
				LOG.info("shut down success.");
				return Deferred.fromResult(null);
			}).addErrback((Exception e) -> {
				LOG.error("shut down fail", e);
				return Deferred.fromResult(null);
			});
		}).addErrback((Exception e) -> {
			LOG.error("flush fail", e);
			return Deferred.fromResult(null);
		});
	}
}
