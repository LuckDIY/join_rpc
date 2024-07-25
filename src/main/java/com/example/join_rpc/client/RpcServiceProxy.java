package com.example.join_rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RpcServiceProxy implements InvocationHandler {


    private Class<?> clazz;

    public RpcServiceProxy(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName())) {
            return clazz.getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
        }

        if ("hashCode".equals(method.getName())) {
            return 0;
        }

        System.out.println(method.getName());
        Class<?> returnType = method.getReturnType();
        if(returnType.equals(Integer.class)){
            return 666;
        }
        return method.getName();

        /*//1. 获得服务信息
        String serviceName = clazz.getName();
        List<Service> serviceList = getServiceList(serviceName);
        Service service = loadBalance.selectOne(serviceList);
        //2. 构建request对象
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setAsync(async);
        rpcRequest.setServiceName(service.getName());
        rpcRequest.setMethod(method.getName());
        rpcRequest.setGroup(group);
        rpcRequest.setVersion(version);
        rpcRequest.setParameters(args);
        rpcRequest.setParametersTypes(method.getParameterTypes());
        //3. 协议编组
        RpcProtocolEnum messageProtocol = RpcProtocolEnum.getProtocol(service.getProtocol());
        RpcCompressEnum compresser = RpcCompressEnum.getCompress(service.getCompress());
        RpcResponse response = netClient.sendRequest(rpcRequest, service, messageProtocol, compresser);
        if (response == null) {
            throw new RpcException("the response is null");
        }
        if (response.getException() != null) {
            throw response.getException();
        }
        if (!RpcStatusEnum.SUCCESS.equals(response.getRpcStatus())) {
            log.info("the request is not successful, request:{} response:{}", rpcRequest, response);
        }
        return response.getReturnValue();*/
    }
}
