package org.webbitserver.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.metrics.handlers.HealthChecksHandler;
import org.webbitserver.metrics.handlers.MetricsHandler;
import org.webbitserver.metrics.handlers.PingHandler;
import org.webbitserver.metrics.handlers.ThreadDumpHandler;
import org.webbitserver.netty.NettyWebServer;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminWebServer extends NettyWebServer {

    private static final String HOME_PAGE = "" +
            "<!doctype html>\n" +
            "<html lang='en-GB'>\n" +
            "<head>\n" +
            "    <meta charset='UTF-8'>\n" +
            "    <title></title>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <h1>webbit-admin</h1>\n" +
            "    <ul>\n" +
            "        <li><a href='/ping'>Ping</a></li>\n" +
            "        <li><a href='/metrics'>Metrics</a></li>\n" +
            "        <li><a href='/healthchecks'>Health Checks</a></li>\n" +
            "        <li><a href='/dump'>Thread Dump</a></li>\n" +
            "    </ul>\n" +
            "</body>\n" +
            "</html>";
    private HealthCheckRegistry healthchecks;
    private MetricRegistry metrics;

    public AdminWebServer(int port) {
        this(Executors.newCachedThreadPool(), port, new HealthCheckRegistry());
    }

    public AdminWebServer(Executor executor, int port) {
        this(executor, port, new HealthCheckRegistry());
    }

    public AdminWebServer(Executor executor, SocketAddress socketAddress, URI publicUri) {
        this(executor, socketAddress, publicUri, new HealthCheckRegistry());
    }

    public AdminWebServer(int port, HealthCheckRegistry healthchecks) {
        this(Executors.newCachedThreadPool(), port, healthchecks, new MetricRegistry());
    }

    public AdminWebServer(Executor executor, int port, HealthCheckRegistry healthchecks) {
        this(executor, port, healthchecks, new MetricRegistry());
    }

    public AdminWebServer(Executor executor, SocketAddress socketAddress, URI publicUri, HealthCheckRegistry healthchecks) {
        this(executor, socketAddress, publicUri, healthchecks, new MetricRegistry());
    }

    public AdminWebServer(int port, MetricRegistry metrics) {
        this(Executors.newCachedThreadPool(), port, new HealthCheckRegistry(), metrics);
    }

    public AdminWebServer(Executor executor, int port, MetricRegistry metrics) {
        this(executor, port, new HealthCheckRegistry(), metrics);
    }

    public AdminWebServer(Executor executor, SocketAddress socketAddress, URI publicUri, MetricRegistry metrics) {
        this(executor, socketAddress, publicUri, new HealthCheckRegistry(), metrics);
    }

    public AdminWebServer(int port, HealthCheckRegistry healthchecks, MetricRegistry metrics) {
        super(Executors.newCachedThreadPool(), port);
        this.healthchecks = healthchecks;
        this.metrics = metrics;
        init();
    }

    public AdminWebServer(Executor executor, int port, HealthCheckRegistry healthchecks, MetricRegistry metrics) {
        super(executor, port);
        this.healthchecks = healthchecks;
        this.metrics = metrics;
        init();
    }

    public AdminWebServer(Executor executor, SocketAddress socketAddress, URI publicUri, HealthCheckRegistry healthchecks, MetricRegistry metrics) {
        super(executor, socketAddress, publicUri);
        this.healthchecks = healthchecks;
        this.metrics = metrics;
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

        // this handler will return a simple root view for the admin
        add("/", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                response.status(200)
                        .content(HOME_PAGE)
                        .end();
            }
        });
        add("/ping", new PingHandler());
        add("/dump", new ThreadDumpHandler());
        add("/healthchecks", new HealthChecksHandler(healthchecks, service));
        add("/metrics", new MetricsHandler(metrics, service));
    }
}
