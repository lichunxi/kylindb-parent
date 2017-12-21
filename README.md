# kylindb-parent
百亿级别时序数据库，支持水平扩展，存储采用hbase集群，消息总线支持kafka

参考opentsdb的模型，使用了其中的异步hbase客户端和Deferred异步模型。
### 特点：
1. 读写分离，可以分别部署，水平扩展和收缩
2. 围绕kafka进行消息处理，时序数据发送到kafka，然后由writer模块进行消费
3. 底层数据存储支持double、float、int、long、bool、string字符类型，并采用protobuf类似的编解码方式，使得存储最小
4. 领域模型突破监控项限制，而是以point为核心，一个point能唯一确定被监控对象、监控项名称等等，该映射关系不保存在时序数据库内部，而是作为业务字段另行保存，从而使kylindb做到尽可能业务无关

--------------

## 总体架构

![架构图](http://github-lichunxi.oss-cn-beijing.aliyuncs.com/1513847579673.jpg)


## 存储模型

一共3张表：metrics、notes、offsets
### metrics表
用于存储时序数据。
表结构和opentsdb类似：
![metrics表结构](http://github-lichunxi.oss-cn-beijing.aliyuncs.com/1513849523842.jpg)

#### rowkey由salt+pointId+baseTime组成，pointId为8byte的long值，basetime为距离1970年1月1日零点的秒数，salt用于改善写性能，对pointId进行hash产生的数值。
#### qualifier为2byte（秒）或3byte（毫秒），其中前2bit位时间单位，01为秒，10位毫秒，对于秒，后12bit存储距离basetime的秒数，剩余2bit保留；对于毫秒，剩余的22bit用于保留距离basetime的毫秒数。
#### value字段支持double、float、int、long、bool、string字符类型，并采用Varint进行编码（可参考protobuf），规则如下：
 * 每个字节（8bit）的第一位bit作为flag标识，1表示下一个Byte和自己是一起的；0表示到本byte结束，下一个byte是新的数字开始。剩下7bit用于存储真实的数值对应的bits数值。flag标识为0的byte，其中最后3bit作为数据类型type
 ##### 数字
 * double type+8byte type占用3bit，值为000
 * float type+4byte type占用3bit，值为001
 * int type+varint type占用3bit，值为010，，采用zigzag编码
 * long type+varint type占用3bit，值为011，，采用zigzag编码
 ##### 布尔值
 * bool type +varint type占用3bit，值为100
 ##### 字符串
 * string type+length+value type占用3bit，值为101。 length字段采用type为010的varint表示

### notes表
用于metric表中每一条数据的备注信息，例如标记某个point在某个时刻的数据为最大值，某个时刻发生了什么事情。
该表结构和metric类似，但是value字段是一个map结构，可以增加各种key-value的标注信息（采用protobuf编码）

### offsets表
保存每个pointId最新的时间戳，用于辅助查询pointId的最新数据。


