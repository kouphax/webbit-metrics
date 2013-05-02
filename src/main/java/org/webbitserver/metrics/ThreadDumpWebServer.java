package org.webbitserver.metrics;

import org.webbitserver.metrics.handlers.ThreadDumpHandler;
import org.webbitserver.netty.NettyWebServer;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;

public class ThreadDumpWebServer extends NettyWebServer {

    public ThreadDumpWebServer(int port) {
        super(port);
        init();
    }

    public ThreadDumpWebServer(Executor executor, int port) {
        super(executor, port);
        init();
    }

    public ThreadDumpWebServer(Executor executor, SocketAddress socketAddress, URI publicUri) {
        super(executor, socketAddress, publicUri);
        init();
    }

    private void init() {
        add(new ThreadDumpHandler());
    }
}
