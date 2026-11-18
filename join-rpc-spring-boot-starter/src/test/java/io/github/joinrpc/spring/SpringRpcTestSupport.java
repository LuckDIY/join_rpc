package io.github.joinrpc.spring;

import java.io.IOException;
import java.net.ServerSocket;

final class SpringRpcTestSupport {

    static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private SpringRpcTestSupport() {
    }
}
