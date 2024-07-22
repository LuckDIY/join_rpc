package com.example.join_rpc.server.register;

import com.example.join_rpc.common.model.Service;
import com.example.join_rpc.common.constants.RpcConstant;
import com.example.join_rpc.server.register.bean.ServiceObject;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * zookeeper 服务注册实现类
 */
@Slf4j
public class ZookeeperServerRegister extends DefaultServerRegister {

    private static final Gson GSON = new Gson();

    private static final int DEFAULT_SESSION_TIMEOUT_MS = Integer.getInteger("curator-default-session-timeout", 60 * 1000);
    private static final int DEFAULT_CONNECTION_TIMEOUT_MS = Integer.getInteger("curator-default-connection-timeout", 15 * 1000);

    private static final ArrayList<String> ExportedServiceURI = new ArrayList<>();


    // 创建连接实例
    private static CuratorFramework zkClient = null;

    public ZookeeperServerRegister(String registerAddress, Integer serverPort, String protocol,
                                   String compress, Integer weight) {

        //重试等待时间1s，重试三次
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        CuratorFramework client = CuratorFrameworkFactory.builder().
                connectString(registerAddress).
                sessionTimeoutMs(DEFAULT_SESSION_TIMEOUT_MS).
                connectionTimeoutMs(DEFAULT_CONNECTION_TIMEOUT_MS).
                retryPolicy(retryPolicy).
                build();
        client.start();
        zkClient = client;

        this.serverPort = serverPort;
        this.protocol = protocol;
        this.compress = compress;
        this.weight = weight;
    }

    @Override
    public void register(ServiceObject so) throws Exception {
        super.register(so);
        Service service = builderService(so);
        //服务远程注册
        exportRegisterService(service);

    }

    @Override
    public void remove() {
        //父类serviceMap中的同一个service可能有不同版本，不同版本的发布到注册中心的URI一样，所以用ExportedServiceURI记录的已发布的URI来避免重复删除
        ExportedServiceURI.forEach(this::removeService);
    }

    /**
     * 服务注册到zk
     *
     * @param serviceResource
     */
    public void exportRegisterService(Service serviceResource) throws Exception {
        String name = serviceResource.getName();
        String uri = GSON.toJson(serviceResource);
        //uri = URLEncoder.encode(uri, StandardCharsets.UTF_8);
        String servicePath = RpcConstant.ZK_SERVICE_PATH + RpcConstant.ZK_PATH_DELIMITER + name + "/service";

        if (!zkExists(zkClient, servicePath)) {
            //没有就注册
            zkClient.create().creatingParentsIfNeeded().forPath(servicePath);
        }
        String uriPath = servicePath + RpcConstant.ZK_PATH_DELIMITER + uri;
        //同一个service可能有多个版本的实现，所以可能已经被注册到注册中心，这样就不需要注册了
        if (!zkExists(zkClient, uriPath)) {
            //创建一个临时节点，会话失效即被清理
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(uriPath);
            //发布的URI记录到map里，方便关机取消注册
            ExportedServiceURI.add(uriPath);
            log.debug("service :{} exported zk", serviceResource);
        }
    }

    public boolean zkExists(CuratorFramework client, String path) {
        Stat stat = null;
        try {
            stat = client.checkExists().forPath(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return stat != null;
    }

    private Service builderService(ServiceObject so) throws UnknownHostException {
        Service service = new Service();
        String host = InetAddress.getLocalHost().getHostAddress();
        String address = host + ":" + this.serverPort;
        service.setAddress(address);
        service.setName(so.getName());
        service.setProtocol(protocol);
        service.setWeight(weight);
        service.setCompress(compress);
        return service;
    }


    private void removeService(String uriPath){
        try {
            zkClient.delete().deletingChildrenIfNeeded().forPath(uriPath);
            log.debug("remove serviceURI :{}", uriPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws Exception {
        //重试等待时间1s，重试三次
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        byte[] bytes;
        try (CuratorFramework client = CuratorFrameworkFactory.builder().
                connectString("192.168.1.2:2181").
                sessionTimeoutMs(DEFAULT_SESSION_TIMEOUT_MS).
                connectionTimeoutMs(DEFAULT_CONNECTION_TIMEOUT_MS).
                retryPolicy(retryPolicy).
                build()) {
            client.start();
            /*bytes = client.getData().forPath("/testNode");
            client.getData().watched().inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                    System.out.println(event.getType());
                    if (event.getType() == CuratorEventType.GET_DATA) {
                        byte[] bytes1 = event.getData();
                        System.out.println(new String(bytes1));
                    }
                }
            }).forPath("/testNode");
            System.out.println(new String(bytes));

            byte[] data = client.getData().usingWatcher(new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    System.out.println("监听器watchedEvent：" + watchedEvent);
                    try {
                        byte[] bytes1 = client.getData().forPath(watchedEvent.getPath());
                        System.out.println("监听到的数据变化" + new String(bytes1));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).forPath("/testNode");
            System.out.println("监听节点内容：" + new String(data));


            while (true) {
                Thread.sleep(1000);
            }*/
            //Stat stat = client.checkExists().forPath("/testNode");

            //String s = client.create().creatingParentsIfNeeded().forPath("/a/b/c","测试".getBytes());
            //System.out.println("sfd  " + s);
            //client.delete().deletingChildrenIfNeeded().forPath("/a");

            byte[] bytes1 = client.getData().forPath("/a/b/c");
            System.out.println("打印："+new String(bytes1));
        }


    }


}
