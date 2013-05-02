package org.webbitserver.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import org.webbitserver.metrics.handlers.HealthChecksHandler;
import org.webbitserver.netty.NettyWebServer;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HealthCheckWebServer extends NettyWebServer {

    private HealthCheckRegistry registry;

    public HealthCheckWebServer(int port) {
        this(Executors.newCachedThreadPool(), port);
    }

    public HealthCheckWebServer(Executor executor, int port) {
        super(executor, port);
        init();
    }

    public HealthCheckWebServer(Executor executor, SocketAddress socketAddress, URI publicUri) {
        super(executor, socketAddress, publicUri);
        init();
    }

    public HealthCheckWebServer(int port, HealthCheckRegistry registry) {
        super(port);
        this.registry = registry;
        init();
    }

    public HealthCheckWebServer(Executor executor, int port, HealthCheckRegistry registry) {
        super(executor, port);
        this.registry = registry;
        init();
    }

    public HealthCheckWebServer(Executor executor, SocketAddress socketAddress, URI publicUri, HealthCheckRegistry registry) {
        super(executor, socketAddress, publicUri);
        this.registry = registry;
        init();
    }

    public HealthCheckRegistry registry(){
        return this.registry;
    }

    private void init(){

        // instantiate a new registry if we haven't set one yet.
        if(registry == null){
            registry = new HealthCheckRegistry();
        }

        Executor executor = this.getExecutor();

        // metrics-healthchecks can be passed and executor service but this is
        // a subclass of the potential executor that can be used with Webbit
        // rather than constrain this, if a consumer of this library wants to
        // manage the execution of this service then they can pass in an
        // {@code ExecutorService}
        if(executor instanceof ExecutorService){
            add(new HealthChecksHandler(registry, (ExecutorService) executor));
        } else {
            add(new HealthChecksHandler(registry));
        }

    }

}
