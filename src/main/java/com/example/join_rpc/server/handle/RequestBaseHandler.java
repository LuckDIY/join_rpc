package com.example.join_rpc.server.handle;

import com.example.join_rpc.common.constants.RpcStatusEnum;
import com.example.join_rpc.common.model.RpcRequest;
import com.example.join_rpc.common.model.RpcResponse;
import com.example.join_rpc.server.register.ServerRegister;
import com.example.join_rpc.server.register.bean.ServiceObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public abstract class RequestBaseHandler {

    private ServerRegister serverRegister;


    public RpcResponse handleRequest(RpcRequest request) {
        log.debug("服务端收到的body请求:{}", request);

        //1.查找具体服务
        ServiceObject serviceObject = serverRegister.getServiceObject(request.getServiceName());

        RpcResponse rpcResponse = new RpcResponse();
        if (serviceObject == null) {
            rpcResponse.setRpcStatus(RpcStatusEnum.NOT_FOUND);
        } else {
            try {
                rpcResponse = invoke(serviceObject, request);
            } catch (Exception e) {
                rpcResponse.setException(e);
                rpcResponse.setRpcStatus(RpcStatusEnum.ERROR);
            }
        }

        rpcResponse.setRequestId(request.getRequestId());
        return rpcResponse;
    }

    abstract RpcResponse invoke(ServiceObject serviceObject, RpcRequest request) throws Exception;

}
