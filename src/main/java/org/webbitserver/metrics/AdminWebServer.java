package org.webbitserver.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.webbitserver.metrics.handlers.HealthChecksHandler;
import org.webbitserver.metrics.handlers.MetricsHandler;
import org.webbitserver.metrics.handlers.PingHandler;
import org.webbitserver.metrics.handlers.ThreadDumpHandler;
import org.webbitserver.netty.NettyWebServer;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

public class AdminWebServer extends NettyWebServer {

    private HealthCheckRegistry healthchecks;
    private MetricRegistry metrics;

    public AdminWebServer(int port) {
        this(port, new HealthCheckRegistry());
    }

    public AdminWebServer(Executor executor, int port) {
        this(executor, port, new HealthCheckRegistry());
    }

    public AdminWebServer(Executor executor, SocketAddress socketAddress, URI publicUri) {
        this(executor, socketAddress, publicUri, new HealthCheckRegistry());
    }

    public AdminWebServer(int port, HealthCheckRegistry healthchecks) {
        super(port);
        this.healthchecks = healthchecks;
        init();
    }

    public AdminWebServer(Executor executor, int port, HealthCheckRegistry healthchecks) {
        super(executor, port);
        this.healthchecks = healthchecks;
        init();
    }

    public AdminWebServer(Executor executor, SocketAddress socketAddress, URI publicUri, HealthCheckRegistry healthchecks) {
        super(executor, socketAddress, publicUri);
        this.healthchecks = healthchecks;
        init();
    }

    public HealthCheckRegistry healthchecks(){
        return this.healthchecks;
    }

    public MetricRegistry metrics(){
        return this.metrics;
    }

    private void init(){

        Executor executor = this.getExecutor();
        ExecutorService service = null;
        if(executor instanceof ExecutorService){
            service = (ExecutorService) executor;
        }

        add("/ping", new PingHandler());
        add("/dump", new ThreadDumpHandler());
        add("/healthchecks", new HealthChecksHandler(healthchecks, service));
        add("/metrics", new MetricsHandler(metrics, service));
    }
}
