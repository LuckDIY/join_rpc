package com.example.join_rpc.common.constants;

import lombok.Getter;

/**
 * 状态枚举
 */
@Getter
public enum RpcStatusEnum {
    /**
     * SUCCESS
     */
    SUCCESS(200, "SUCCESS"),

    /**
     * ERROR
     */
    ERROR(500, "ERROR"),

    /**
     * NOT_FOUND
     */
    NOT_FOUND(404, "NOT FOUND");

    private final int code;

    private final String desc;

    RpcStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
