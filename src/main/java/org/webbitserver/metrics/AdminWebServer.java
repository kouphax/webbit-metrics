package org.webbitserver.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.webbitserver.*;
import org.webbitserver.metrics.registries.TaskRegistry;
import org.webbitserver.netty.NettyWebServer;

import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Netty based Webbit WebServer that is intended to act as a companion server
 * to another (potentially) Webbit server (though this isn't entire necessary)
 *
 * You instantiate the server alongside your service
 *
 * <pre>
 * {@code
 * WebServer server = new NettyWebServer(9996);
 * WebServer admin = new AdminWebServer(9997);
 * }
 * </pre>
 */
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
        "        <li><a href='/tasks'>Tasks</a></li>\n" +
        "        <li><a href='/dump'>Thread Dump</a></li>\n" +
        "        <li><a href='/jvm'>JVM Metrics</a></li>\n" +
        "    </ul>\n" +
        "</body>\n" +
        "</html>";

    public final HealthCheckRegistry healthchecks;
    public final MetricRegistry metrics;
    public final TaskRegistry tasks;

    private final MetricRegistry jvmMetrics;

    public AdminWebServer(int port) {
        this(Executors.newCachedThreadPool(), port);
    }

    public AdminWebServer(Executor executor, int port) {
        super(executor, port);
        this.healthchecks = new HealthCheckRegistry();
        this.metrics = new MetricRegistry();
        this.jvmMetrics = new MetricRegistry();
        this.tasks = new TaskRegistry();
    }

    public AdminWebServer(Executor executor, SocketAddress socketAddress, URI publicUri) {
        super(executor, socketAddress, publicUri);
        this.healthchecks = new HealthCheckRegistry();
        this.metrics = new MetricRegistry();
        this.jvmMetrics = new MetricRegistry();
        this.tasks = new TaskRegistry();
    }

    @Override
    public Future<NettyWebServer> start() {
        this.buildService();
        return super.start();
    }

    private void buildService(){

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
        add("/jvm", new JVMMetricsHandler());
        add("/tasks", new TasksHandler(tasks));

        ExecutorService service = this.getExecutorService();

        if(service == null){
            add("/healthchecks", new HealthChecksHandler(healthchecks));
            add("/metrics", new MetricsHandler(metrics));
        } else {
            add("/healthchecks", new HealthChecksHandler(healthchecks, service));
            add("/metrics", new MetricsHandler(metrics, service));
        }
    }

    private ExecutorService getExecutorService() {
        Executor executor = this.getExecutor();
        ExecutorService service = null;
        if(executor instanceof ExecutorService){
            service = (ExecutorService) executor;
        }
        return service;
    }
}
