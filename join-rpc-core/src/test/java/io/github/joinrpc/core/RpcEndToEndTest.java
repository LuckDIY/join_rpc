package io.github.joinrpc.core;

import io.github.joinrpc.core.client.NettyRpcClient;
import io.github.joinrpc.core.cluster.RoundRobinLoadBalancer;
import io.github.joinrpc.core.discovery.StaticServiceDiscovery;
import io.github.joinrpc.core.proxy.RpcInvoker;
import io.github.joinrpc.core.proxy.RpcProxyFactory;
import io.github.joinrpc.core.proxy.RpcReferenceConfig;
import io.github.joinrpc.core.serialize.Serializers;
import io.github.joinrpc.core.server.NettyRpcServer;
import io.github.joinrpc.core.server.ServiceProviderRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RpcEndToEndTest {

    public interface EchoService {
        String echo(String s);

        int add(int a, int b);

        Point move(Point p, int dx);

        List<String> join(String a, String b, String c);

        void fail(String message);

        Object readOnly(String kind);

        int size(List<String> list);

        String slow(long millis) throws InterruptedException;

        String overloaded(String s);

        String overloaded(Integer i);

        default String greet() {
            return "hi from default method";
        }
    }

    public static class Point {
        public int x;
        public int y;

        public Point() {
        }

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class EchoServiceImpl implements EchoService {
        public String echo(String s) {
            return "echo:" + s;
        }

        public int add(int a, int b) {
            return a + b;
        }

        public Point move(Point p, int dx) {
            return new Point(p.x + dx, p.y);
        }

        public List<String> join(String a, String b, String c) {
            return java.util.Arrays.asList(a, b, c);
        }

        public void fail(String message) {
            throw new IllegalStateException(message);
        }

        public int size(List<String> list) {
            return list.size();
        }

        public Object readOnly(String kind) {
            return switch (kind) {
                case "List.of" -> List.of("a", "b", "c");
                case "List.of1" -> List.of("a");
                case "toList" -> java.util.stream.Stream.of("a", "b").toList();
                case "unmodifiableList" -> java.util.Collections.unmodifiableList(new java.util.ArrayList<>(List.of("a")));
                case "singletonList" -> java.util.Collections.singletonList("a");
                case "Set.of" -> java.util.Set.of("a", "b", "c");
                case "Map.of" -> Map.of("k1", 1, "k2", 2);
                case "unmodifiableMap" -> java.util.Collections.unmodifiableMap(new java.util.HashMap<>(Map.of("k", 1)));
                case "nested" -> Map.of("list", java.util.Collections.singletonList(new Point(1, 2)));
                case "emptyList" -> java.util.Collections.emptyList();
                case "sortedSet" -> java.util.Collections.unmodifiableSortedSet(new java.util.TreeSet<>(List.of("b", "a")));
                default -> throw new IllegalArgumentException(kind);
            };
        }

        public String slow(long millis) throws InterruptedException {
            Thread.sleep(millis);
            return "done";
        }

        public String overloaded(String s) {
            return "string";
        }

        public String overloaded(Integer i) {
            return "integer";
        }
    }

    static NettyRpcServer server;
    static NettyRpcClient client;
    static EchoService echo;
    static int deadPort;

    @BeforeAll
    static void start() throws Exception {
        ServiceProviderRegistry registry = new ServiceProviderRegistry();
        registry.register(EchoService.class, new EchoServiceImpl());
        server = new NettyRpcServer(0, registry);
        server.start();

        try (ServerSocket s = new ServerSocket(0)) {
            deadPort = s.getLocalPort();
        }

        client = new NettyRpcClient(Duration.ofSeconds(1));
        // 一个连不上的地址 + 一个正常地址，验证连接失败时会换节点
        StaticServiceDiscovery discovery = new StaticServiceDiscovery(Map.of("echo-app", List.of(
                new RpcAddress("127.0.0.1", deadPort), new RpcAddress("127.0.0.1", server.getPort()))));
        RpcInvoker invoker = new RpcInvoker(client, discovery, new RoundRobinLoadBalancer(),
                Serializers.get("protostuff"), Duration.ofSeconds(2));
        echo = RpcProxyFactory.create(EchoService.class,
                new RpcReferenceConfig(EchoService.class, "echo-app", null), invoker);
    }

    @AfterAll
    static void stop() {
        client.close();
        server.stop();
    }

    @Test
    void basicTypes() {
        assertThat(echo.echo("hello")).isEqualTo("echo:hello");
        assertThat(echo.add(1, 2)).isEqualTo(3);
    }

    @Test
    void pojoAndCollections() {
        Point p = echo.move(new Point(1, 2), 10);
        assertThat(p.x).isEqualTo(11);
        assertThat(p.y).isEqualTo(2);
        assertThat(echo.join("a", "b", "c")).containsExactly("a", "b", "c");
    }

    @Test
    void nullArgumentsKeepPosition() {
        assertThat(echo.echo(null)).isEqualTo("echo:null");
        assertThat(echo.join(null, "b", null)).containsExactly(null, "b", null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void readOnlyJdkCollections() {
        assertThat((List<String>) echo.readOnly("List.of")).containsExactly("a", "b", "c");
        assertThat((List<String>) echo.readOnly("List.of1")).containsExactly("a");
        assertThat((List<String>) echo.readOnly("toList")).containsExactly("a", "b");
        assertThat((List<String>) echo.readOnly("unmodifiableList")).containsExactly("a");
        assertThat((List<String>) echo.readOnly("singletonList")).containsExactly("a");
        assertThat((java.util.Set<String>) echo.readOnly("Set.of")).containsExactlyInAnyOrder("a", "b", "c");
        assertThat((Map<String, Integer>) echo.readOnly("Map.of")).containsEntry("k1", 1).containsEntry("k2", 2);
        assertThat((Map<String, Integer>) echo.readOnly("unmodifiableMap")).containsEntry("k", 1);
        Map<String, List<Point>> nested = (Map<String, List<Point>>) echo.readOnly("nested");
        assertThat(nested.get("list").get(0).y).isEqualTo(2);
        assertThat((List<String>) echo.readOnly("emptyList")).isEmpty();
        assertThat((java.util.Set<String>) echo.readOnly("sortedSet")).containsExactly("a", "b");
        // 参数是只读集合
        assertThat(echo.size(java.util.Collections.unmodifiableList(List.of("x", "y")))).isEqualTo(2);
    }

    @Test
    void overloadResolvedByParameterTypes() {
        assertThat(echo.overloaded("x")).isEqualTo("string");
        assertThat(echo.overloaded(1)).isEqualTo("integer");
    }

    @Test
    void defaultMethodRunsLocally() {
        assertThat(echo.greet()).isEqualTo("hi from default method");
    }

    @Test
    void remoteExceptionIsPropagated() {
        assertThatThrownBy(() -> echo.fail("boom"))
                .isInstanceOf(RpcRemoteException.class)
                .hasMessageContaining("IllegalStateException")
                .hasMessageContaining("boom");
    }

    @Test
    void timeout() {
        assertThatThrownBy(() -> echo.slow(3000))
                .isInstanceOf(RpcTimeoutException.class);
    }

    @Test
    void noProvider() {
        RpcInvoker invoker = new RpcInvoker(client, new StaticServiceDiscovery(Map.of()), new RoundRobinLoadBalancer(),
                Serializers.get("protostuff"), Duration.ofSeconds(1));
        EchoService missing = RpcProxyFactory.create(EchoService.class,
                new RpcReferenceConfig(EchoService.class, "missing-app", null), invoker);
        assertThatThrownBy(() -> missing.echo("x"))
                .isInstanceOf(RpcException.class)
                .hasMessageContaining("missing-app");
    }

    @Test
    void concurrentCallsShareOneConnection() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(16);
        try {
            List<CompletableFuture<Integer>> futures = IntStream.range(0, 500)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> echo.add(i, 1), pool))
                    .toList();
            for (int i = 0; i < futures.size(); i++) {
                assertThat(futures.get(i).get()).isEqualTo(i + 1);
            }
        } finally {
            pool.shutdown();
        }
    }

    @Test
    void objectMethodsDoNotGoRemote() {
        assertThat(echo.toString()).contains("EchoService").contains("echo-app");
        assertThat(echo.equals(echo)).isTrue();
        assertThat(echo.hashCode()).isEqualTo(System.identityHashCode(echo));
    }
}
