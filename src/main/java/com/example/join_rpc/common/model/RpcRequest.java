package com.example.join_rpc.common.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RpcRequest {

    //唯一id
    private String requestId;

    //节点服务名字(提供者信息) 如: com.xxx.xxx.xxxService
    private String serviceName;


    private String serviceMethod;

    private String group;


    private Map<String, String> headers = new HashMap<>();

    private Class<?>[] parametersTypes;

    private Object[] parameters;

    private Integer a;

}
