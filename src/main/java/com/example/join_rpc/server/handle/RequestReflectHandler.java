package com.example.join_rpc.server.handle;

import com.example.join_rpc.annotation.RpcProxyHandleAno;
import com.example.join_rpc.common.constants.RpcConstant;
import com.example.join_rpc.common.constants.RpcStatusEnum;
import com.example.join_rpc.common.model.RpcRequest;
import com.example.join_rpc.common.model.RpcResponse;
import com.example.join_rpc.server.register.bean.ServiceObject;

import java.lang.reflect.Method;

/**
 * 调用处理器 反射 实现类
 */
@RpcProxyHandleAno(RpcConstant.SERVER_PROXY_TYPE_REFLECT)
public class RequestReflectHandler extends RequestBaseHandler {


    @Override
    RpcResponse invoke(ServiceObject serviceObject, RpcRequest request) throws Exception {
        Method method = serviceObject.getClazz().getMethod(request.getServiceMethod(), request.getParametersTypes());
        Object invoke = method.invoke(serviceObject.getObj(), request.getParameters());
        RpcResponse response = new RpcResponse(RpcStatusEnum.SUCCESS);
        response.setReturnValue(invoke);
        return response;
    }
}
