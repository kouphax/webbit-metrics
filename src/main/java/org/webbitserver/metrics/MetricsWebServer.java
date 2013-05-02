package org.webbitserver.metrics;

import com.codahale.metrics.MetricRegistry;
import org.webbitserver.metrics.handlers.MetricsHandler;
import org.webbitserver.netty.NettyWebServer;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class MetricsWebServer extends NettyWebServer {

    MetricRegistry registry;

    public MetricsWebServer(int port) {
        super(port);
        init();
    }

    public MetricsWebServer(Executor executor, int port) {
        super(executor, port);
        init();
    }

    public MetricsWebServer(Executor executor, SocketAddress socketAddress, URI publicUri) {
        super(executor, socketAddress, publicUri);
        init();
    }

    public MetricsWebServer(int port, MetricRegistry registry) {
        super(port);
        this.registry = registry;
        init();
    }

    public MetricsWebServer(Executor executor, int port, MetricRegistry registry) {
        super(executor, port);
        this.registry = registry;
        init();
    }

    public MetricsWebServer(Executor executor, SocketAddress socketAddress, URI publicUri, MetricRegistry registry) {
        super(executor, socketAddress, publicUri);
        this.registry = registry;
        init();
    }

    public MetricRegistry registry(){
        return this.registry;
    }

    private void init(){

        // instantiate a new registry if we haven't set one yet.
        if(registry == null){
            registry = new MetricRegistry();
        }

        Executor executor = this.getExecutor();

        // metrics can be passed and executor service but this is
        // a subclass of the potential executor that can be used with Webbit
        // rather than constrain this, if a consumer of this library wants to
        // manage the execution of this service then they can pass in an
        // {@code ExecutorService}
        if(executor instanceof ExecutorService){
            add(new MetricsHandler(registry, (ExecutorService) executor));
        } else {
            add(new MetricsHandler(registry));
        }

    }

}
