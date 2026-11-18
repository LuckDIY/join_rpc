# join-rpc

基于 Netty 的轻量 Java RPC 框架。通过 Spring Cloud 标准的 `DiscoveryClient` 发现服务提供方，
所以 Nacos、Consul、Eureka、Zookeeper、Kubernetes 都能直接使用，不需要为每个注册中心单独适配。

> 最初参考 [PPPHUANG/rpc-spring-starter](https://github.com/PPPHUANG/rpc-spring-starter) 学习编写，后经重构。

## 模块

| 模块 | 说明 |
|---|---|
| `join-rpc-core` | 协议、编解码、序列化（SPI）、Netty 客户端/服务端、代理、负载均衡，不依赖 Spring |
| `join-rpc-spring-boot-starter` | `@RpcService` / `@RpcReference`、自动配置、Spring Cloud 适配 |
| `join-rpc-examples` | api / provider / consumer 示例（不发布） |

要求 JDK 17+，Spring Boot 3.x。

## 快速开始

```xml
<dependency>
    <groupId>io.github.joinrpc</groupId>
    <artifactId>join-rpc-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

**提供方**：在实现类上加 `@RpcService`

```java
@RpcService
public class HelloServiceImpl implements HelloService { ... }
```

**调用方**：在字段上加 `@RpcReference`，`application` 填提供方在注册中心里的服务名（通常是它的 `spring.application.name`）

```java
@RpcReference(application = "example-provider")
private HelloService helloService;
```

## 服务发现

调用方按下面的顺序查找提供方地址：

1. **直连配置**（适合本地调试）
   ```yaml
   join-rpc:
     client:
       direct:
         example-provider: 127.0.0.1:9999,127.0.0.1:9998
   ```
2. **Spring Cloud `DiscoveryClient`**：classpath 上有任意 Spring Cloud 注册中心 starter 时自动启用。

提供方启动时会把 RPC 端口写进自己注册实例的 metadata：`join-rpc.port=9999`。
调用方读取实例的 `host` 和 metadata 里的 `join-rpc.port`，组成 RPC 地址；没有这个 metadata 的实例（例如普通 HTTP 服务）会被忽略。

以 Nacos 为例，两边都只需要额外引入：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

> 如果某个注册中心实现不允许修改 metadata，启动日志会给出提示，此时手动配置即可，例如
> `spring.cloud.nacos.discovery.metadata.join-rpc.port=9999`。

## 配置项

| 配置 | 默认值 | 说明 |
|---|---|---|
| `join-rpc.enabled` | `true` | 总开关 |
| `join-rpc.serializer` | `protostuff` | 序列化方式 |
| `join-rpc.server.enabled` | `true` | 是否启用服务端（没有 `@RpcService` 时不会监听端口） |
| `join-rpc.server.port` | `9999` | RPC 端口 |
| `join-rpc.server.threads` | `200` | 业务线程数 |
| `join-rpc.server.queue-size` | `1000` | 业务队列长度，满了直接返回 server is busy |
| `join-rpc.client.timeout` | `3s` | 默认调用超时，可用 `@RpcReference(timeout=...)` 覆盖 |
| `join-rpc.client.connect-timeout` | `3s` | 建连超时 |
| `join-rpc.client.load-balancer` | `random` | `random` / `round-robin` |
| `join-rpc.client.discovery-cache-ttl` | `5s` | 注册中心查询结果缓存时间 |
| `join-rpc.client.direct.<app>` | - | 直连地址 |

## 调用语义

- 连接失败（请求一定没发出）时会换一个节点重试，失败的节点会被跳过 5 秒。
- 超时（`RpcTimeoutException`）和业务异常**不重试**，因为请求可能已经执行。
- 服务端抛出的异常以 `RpcRemoteException` 返回，只携带异常类名和消息。
- 接口的 `default` 方法在本地执行，不发起远程调用。
- 停机顺序：Spring Cloud 先从注册中心下线 → Web 服务器优雅停机 → RPC 服务端等待正在处理的请求（最多 10 秒）后关闭。

## 传输对象的要求（protostuff）

- DTO 需要有无参构造器。
- 方法参数和返回值本身可以是任意 JDK 集合（`List.of`、`Collections.unmodifiableList` 等），框架会自动转换。
- 但 DTO **字段**里不要放 `Collections.unmodifiableXxx` / `singletonList` / `emptyList` 这类 JDK 包装集合：
  JDK 17 不允许反射访问它们，除非 JVM 加上 `--add-opens java.base/java.util=ALL-UNNAMED`。

## 运行示例

```bash
./mvnw install -DskipTests

# 直连模式
java -jar join-rpc-examples/example-provider/target/example-provider-0.1.0-SNAPSHOT.jar
java -jar join-rpc-examples/example-consumer/target/example-consumer-0.1.0-SNAPSHOT.jar
curl 'localhost:8080/hello?name=rpc'

# Nacos 模式（需要本地 Nacos 在 8848 端口）
./mvnw -Pnacos -pl join-rpc-examples/example-provider -am install -DskipTests
./mvnw -Pnacos -pl join-rpc-examples/example-consumer -am install -DskipTests
java -jar join-rpc-examples/example-provider/target/example-provider-0.1.0-SNAPSHOT.jar --spring.profiles.active=nacos
java -jar join-rpc-examples/example-consumer/target/example-consumer-0.1.0-SNAPSHOT.jar --spring.profiles.active=nacos
```

## 协议

```
 0      4        5             9      10           11           15
 +------+--------+-------------+------+------------+------------+-----------+
 | magic| version| full length | type | serializer | request id |   body    |
 |  4B  |   1B   |     4B      |  1B  |     1B     |     4B     |  变长     |
 +------+--------+-------------+------+------------+------------+-----------+
```

每个服务端地址只建一条长连接，请求通过 request id 多路复用；客户端空闲 30 秒发心跳，90 秒收不到任何数据就断开。
