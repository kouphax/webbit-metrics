package org.webbitserver.metrics;

import org.webbitserver.metrics.handlers.PingHandler;
import org.webbitserver.netty.NettyWebServer;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;

public class PingWebServer extends NettyWebServer {

    public PingWebServer(int port) {
        super(port);
        init();
    }

    public PingWebServer(Executor executor, int port) {
        super(executor, port);
        init();
    }

    public PingWebServer(Executor executor, SocketAddress socketAddress, URI publicUri) {
        super(executor, socketAddress, publicUri);
        init();
    }

    private void init() {
        add(new PingHandler());
    }
}
