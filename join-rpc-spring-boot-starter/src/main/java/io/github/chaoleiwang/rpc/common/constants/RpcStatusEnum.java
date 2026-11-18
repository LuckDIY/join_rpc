package io.github.chaoleiwang.rpc.common.constants;

import lombok.Getter;

/**
 * RPC 调用状态
 */
@Getter
public enum RpcStatusEnum {

    /**
     * 成功
     */
    SUCCESS(200, "SUCCESS"),

    /**
     * 服务端执行出错
     */
    ERROR(500, "ERROR"),

    /**
     * 服务未找到
     */
    NOT_FOUND(404, "NOT FOUND");

    private final int code;

    private final String desc;

    RpcStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
