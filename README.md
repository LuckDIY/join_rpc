# join-rpc

基于 Netty 的轻量级 RPC 框架(Spring Boot 3 starter),由早期学习项目改造而来。

## 特性

- **开箱即用**:引入 starter + 一个注解即可,服务端自动导出、消费端自动生成代理
- **注册中心无关**:基于 Spring Cloud 的 `ServiceRegistry` / `DiscoveryClient` / `LoadBalancerClient` 抽象,
  核心库**不依赖任何具体注册中心**。使用方按需引入 Nacos / Eureka / Consul / Zookeeper 的 Spring Cloud starter 即可
- **无注册中心也能跑**:支持 `rpc.client.direct-address` 直连模式
- **完整调用链**:Netty 客户端连接复用/自动重连、心跳保活、请求超时、多实例故障转移(传输层异常自动尝试下一实例)、
  内置 random / roundRobin 负载均衡(引入 spring-cloud-loadbalancer 后自动升级为 Spring Cloud LoadBalancer)
- **序列化可插拔**:默认 fastjson2 JSON(不开启 autoType,类型按方法签名转换,无反序列化漏洞面),可选 protostuff

## 模块结构

| 模块 | 说明 |
| --- | --- |
| `join-rpc-spring-boot-starter` | 核心库,别人引用这个 jar |
| `join-rpc-example` | 示例:api(共享接口) + provider(服务提供方) + consumer(服务消费方)三个包 |

## 快速开始

### 1. 引入依赖(以 Nacos 为例)

```xml
<dependency>
    <groupId>io.github.chaoleiwang</groupId>
    <artifactId>join-rpc-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<!-- 任选一个注册中心 starter;核心库通过 Spring Cloud 抽象自动适配 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

### 2. 定义接口

```java
@RpcService
public interface DemoService {
    String hello(String name);
}
```

### 3. 服务提供方

```java
@Service
public class DemoServiceImpl implements DemoService {
    public String hello(String name) { return "hello " + name; }
}

@RpcServiceScan("com.example.api")
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
```

启动后自动:导出 `@RpcService` 服务 → 注册到注册中心 → 启动 Netty 服务端(`rpc.server.port`)。

### 4. 服务消费方

```java
@RpcServiceScan("com.example.api")
@SpringBootApplication
public class ConsumerApplication { ... }

@RestController
public class DemoController {
    @Autowired  // 自动注入远程调用代理
    private DemoService demoService;
}
```

消费方没有本地实现类时,`@RpcService` 接口会被扫描注册为代理 Bean(Bean 名为接口全限定名,`@Primary`),
按接口类型注入即走远程调用。

## 配置项

前缀 `rpc`,常用配置:

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `rpc.enabled` | `true` | 总开关 |
| `rpc.serializer` | `json` | 序列化协议:`json` / `protostuff` |
| `rpc.server.enabled` | `true` | 是否启动 RPC 服务端(纯消费方设为 false) |
| `rpc.server.port` | `9999` | RPC 服务端口 |
| `rpc.server.host` | 自动取本机 IP | 注册到注册中心的主机地址 |
| `rpc.server.weight` | `100` | 服务权重(注册中心元数据) |
| `rpc.client.timeout-millis` | `5000` | 调用超时 |
| `rpc.client.load-balance` | `random` | 内置负载均衡:`random` / `roundRobin`(有 spring-cloud-loadbalancer 时自动用 Spring Cloud 的) |
| `rpc.client.direct-address` | 空 | 直连地址 `host:port`,多个逗号分隔;配置后优先于注册中心 |

## 运行示例

```bash
# 1. 安装到本地仓库
./mvnw clean install -DskipTests

# 2. 直连模式(无需注册中心)
./mvnw -pl join-rpc-example spring-boot:run -Dspring-boot.run.profiles=provider          # 启动 provider,监听 9999
./mvnw -pl join-rpc-example -Pconsumer spring-boot:run -Dspring-boot.run.profiles=consumer   # 启动 consumer,监听 8080

curl "http://localhost:8080/hello?name=join-rpc"
curl "http://localhost:8080/hello2?name=join&age=18"

# 3. Nacos 注册中心模式(先启动 Nacos,默认 127.0.0.1:8848)
./mvnw -pl join-rpc-example spring-boot:run -Dspring-boot.run.profiles=provider,nacos
./mvnw -pl join-rpc-example -Pconsumer spring-boot:run -Dspring-boot.run.profiles=consumer,nacos
```

说明:示例模块里有 provider / consumer 两个启动类,`spring-boot:run` 默认启动 provider,
加 `-Pconsumer`(Maven profile,只改主类)启动 consumer;两个应用也可以直接在 IDEA 里分别运行。

## 注册中心适配说明

- **服务注册**(提供方):容器中存在 Spring Cloud `ServiceRegistry` Bean 时自动使用。Nacos(`spring-cloud-starter-alibaba-nacos-discovery`)、
  Eureka 等以通用 `Registration` 实现的注册中心可直接使用;若某个注册中心实现只接受自己的 Registration 子类,
  可自定义 `RpcServiceRegistry` Bean 替换默认实现。
- **服务发现**(消费方):优先 `LoadBalancerClient`(spring-cloud-loadbalancer),回退 `DiscoveryClient` + 内置负载均衡。
  任何实现了 Spring Cloud Discovery 的注册中心都可用。
- **无注册中心**:`rpc.client.direct-address` 直连,或单应用内提供方+消费方共存时直接调自己。

## 发布到 Maven Central(可选)

1. 在 [Sonatype](https://central.sonatype.org/) 注册账号并申请 `io.github.chaoleiwang` 的发布权限
2. 配置 `~/.m2/settings.xml` 的 server 凭据
3. 将版本号从 `-SNAPSHOT` 改为正式版本,执行 `./mvnw clean deploy`(需在 pom 中补充 maven-gpg-plugin / nexus-staging 配置)

## 与早期学习版的差异

- 注册中心从硬编码 Zookeeper(curator)改为 Spring Cloud 抽象,核心 jar 不再携带任何注册中心依赖
- 客户端从 mock 补全为真实调用链:服务发现、负载均衡、连接复用、超时、故障转移
- 服务端 `start()` 不再阻塞 Spring 启动流程;服务端/客户端生命周期接入 Spring 生命周期回调
- 修复原 `spring.factories` 指向不存在类的问题,改用 Boot 3 的 `AutoConfiguration.imports`
- `@RpcServiceScan` 扫描路径不再写死;请求 id 真实生成(原来恒为 0);序列化协议可按配置选择

## 参考

早期版本参考学习自 [rpc-spring-starter](https://github.com/PPPHUANG/rpc-spring-starter)
