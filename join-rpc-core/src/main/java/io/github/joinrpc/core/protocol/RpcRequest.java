package io.github.joinrpc.core.protocol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 调用请求。参数类型用类名传输，避免依赖序列化框架对 Class 的支持。
 */
public class RpcRequest {

    /** 接口全限定名，如 com.xxx.HelloService */
    private String interfaceName;

    private String methodName;

    /** 参数类型全限定名，用于在服务端精确定位重载方法 */
    private String[] parameterTypes;

    private Object[] arguments;

    /** 透传的附加信息，如链路追踪 id */
    private Map<String, String> attachments = new HashMap<>();

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return interfaceName + "#" + methodName + Arrays.toString(parameterTypes);
    }
}
