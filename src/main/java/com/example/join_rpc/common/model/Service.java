package com.example.join_rpc.common.model;

import lombok.Data;

@Data
public class Service {

    //节点服务名字(提供者信息) 如: com.xxx.xxx.xxxService
    private String name;

    //地址 服务地址
    private String address;

    //协议
    private String protocol;

    //压缩
    private String compress;

    //服务权重
    private Integer weight;

}
