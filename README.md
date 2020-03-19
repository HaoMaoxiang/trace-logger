# 基于SpringBoot的日志组件
## 1. 项目内容
### 1.1 日志组件LogUtil
> 输出格式化的日志信息
>
> **使用方法**：log.info(LogUtil.logMsg("_tag", "key1", "value1", "key2", "value2"));
>
> 输出：_tag||trace_id=xxx||span_id=xxx||key1=value1||key2=value2

API

method | desc
---- | ---
LogUtil.logMsg(String, String...) | 输出格式化日志信息，日志内容自动包括traceId以及spanId，方便分布式系统进行日志追踪；
LogMessage.setTraceId(String) |  设置TraceId
LogMessage.getTraceId(String) |  获取TraceId
LogMessage.generateTraceId() |  根据一定的规则生成一个TraceId
LogMessage.setSpanId(String) |  设置SpanId
LogMessage.getSpanId(String) |  获取SpanId
LogMessage.generateSpanId(String) |  生成需要传递给下游系统的SpanId
LogMessage.remove() | 清除ThreadLocal中存放的TraceId以及SpanId信息

### 1.2 WebLogAop
> 拦截controller以及service的public方法，在调用方法前后自动打印日志
> 
> **使用方法**: 在SpringBoot启动类上加上注解``@ComponentScan(basePackages = {"xxx.xxx", "tech.mervyn"})``，其中``xxx.xxx``是你项目中要自动扫描所在的包目录
>

> 日志格式为：

```log
20:30:23.924 [http-nio-8888-exec-1] INFO  tech.mervyn.aop.WebLogAop - _com_request_in||trace_id=7f0001011582374623924100027538||span_id=span-id.0||host=127.0.0.1||method=GET||uri=/user/1||args={"id":["1"]}
20:30:23.935 [http-nio-8888-exec-1] INFO  tech.mervyn.aop.WebLogAop - _com_service_in||trace_id=7f0001011582374623924100027538||span_id=span-id.0||class_name=me.mervyn.service.user.impl.UserServiceImpl||method_name=getById||args=[1]
20:30:24.101 [http-nio-8888-exec-1] INFO  tech.mervyn.aop.WebLogAop - _com_service_out||trace_id=7f0001011582374623924100027538||span_id=span-id.0||class_name=me.mervyn.service.user.impl.UserServiceImpl||method_name=getById||return={"id":1,"name":"Jone","age":18,"email":"test1@baomidou.com"}||proc_time=161
20:30:24.119 [http-nio-8888-exec-1] INFO  tech.mervyn.aop.WebLogAop - _com_request_out||trace_id=7f0001011582374623924100027538||span_id=span-id.0||host=127.0.0.1||method=GET||uri=/user/1||response={"headers":{},"body":{"errNo":0,"errMsg":"success","data":{"id":1,"name":"Jone","age":18,"email":"test1@baomidou.com"},"time":1582374624103},"statusCode":"OK","statusCodeValue":200}||proc_time=192

```

> 需要在项目配置文件中添加下列配置：

```yml
trace-logger:
  # 日志中敏感信息过滤，黑名单列表
  black-list: response, name, age
  # 是否打印controller日志
  controller-log: true
  # 是否打印service日志
  service-log: true
  # 是否打印sql日志
  sql-log: true
  # sql日志中是否打印出参数
  sql-params: true
```

## 3. Maven引用
```xml
<dependency>
    <groupId>tech.mervyn</groupId>
    <artifactId>trace-logger</artifactId>
    <version>0.0.2</version>
</dependency>
```

## 4. 项目环境

中间件 | 版本 | 备注 
:---: | :----: | :----:
Java | 1.8+ | JDK1.8及以上
SpringBoot | 2.2.4.RELEASE | 最新发布稳定版