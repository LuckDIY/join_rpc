package io.github.joinrpc.core.protocol;

/**
 * 调用响应。异常只传类名和消息，不传异常对象（异常对象跨语言/跨版本反序列化很不可靠）。
 */
public class RpcResponse {

    public static final int STATUS_OK = 0;
    public static final int STATUS_ERROR = 1;

    private int status;

    private Object value;

    private String errorType;

    private String errorMessage;

    public static RpcResponse success(Object value) {
        RpcResponse response = new RpcResponse();
        response.status = STATUS_OK;
        response.value = value;
        return response;
    }

    public static RpcResponse error(String errorType, String errorMessage) {
        RpcResponse response = new RpcResponse();
        response.status = STATUS_ERROR;
        response.errorType = errorType;
        response.errorMessage = errorMessage;
        return response;
    }

    public static RpcResponse error(Throwable t) {
        return error(t.getClass().getName(), t.getMessage());
    }

    public boolean isSuccess() {
        return status == STATUS_OK;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return isSuccess() ? "RpcResponse{ok, value=" + value + '}'
                : "RpcResponse{error, " + errorType + ": " + errorMessage + '}';
    }
}
