package org.webbitserver.metrics.handlers;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * InstrumentedMiddleware is a Webbit handler that provides some basic metrics
 * for requests. Placing it at the head of your middleware stack for all
 * requests in a Webbit will monitor the entire request chain.
 *
 * <pre>
 * {@code
 *      server.add(new InstrumentationMiddleware("my-lovely-greeter-service", admin.metrics));
 *      server.add(...);
 *      server.add(...);
 *      server.add("/", new HttpHandler(){
 *          @Override
 *          public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
 *              response.content("Hello World").end();
 *          }
 *      });
 * }
 * </pre>
 *
 * You can optionally pass in a handlerName string as a first parameter and
 * this will be appended to the instrumented metrics (useful if you have
 * multiple handlers going to the same metrics registry).
 *
 * The following metrics are provided by InstrumentedMiddleware
 *
 * <ul>
 *  <li>{@code active-request} counter - number of currently active requests</li>
 *  <li>Meters for HTTP statuses
 *      <ul>
 *          <li>{@code status.badRequest} (HTTP Status 400)</li>
 *          <li>{@code status.created} (HTTP Status 201)</li>
 *          <li>{@code status.noContent} (HTTP Status 204)</li>
 *          <li>{@code status.notFound} (HTTP Status 404)</li>
 *          <li>{@code status.ok} (HTTP Status 200)</li>
 *          <li>{@code status.serverError} (HTTP Status 500)</li>
 *          <li>{@code status.other} (All other HTTP Statuses)</li>
 *      </ul>
 *  </li>
 *  <li>{@code request} timer - time based stats for request processing time</li>
 * </ul>
 */
public class InstrumentationMiddleware implements HttpHandler {

    private final ConcurrentMap<Integer, Meter> statusCodeMeters;
    private final Counter activeRequests;
    private final Timer requestTimer;
    private final Meter otherMeter;
    private final MetricRegistry registry;

    public InstrumentationMiddleware(MetricRegistry registry){
        this("", registry);
    }

    public InstrumentationMiddleware(String handlerName, MetricRegistry registry){
        this.registry = registry;

        String prefix = handlerName.isEmpty() ? "" : handlerName.concat(".");

        this.activeRequests = registry.counter(prefix.concat("active-requests"));
        this.requestTimer = registry.timer(prefix.concat("request"));
        this.statusCodeMeters = new ConcurrentHashMap<>(6);
        this.otherMeter = registry.meter(prefix.concat("status.other"));

        // create meters for particular statuses so we can monitor them
        statusCodeMeters.put(200, registry.meter(prefix.concat("status.ok")));
        statusCodeMeters.put(201, registry.meter(prefix.concat("status.created")));
        statusCodeMeters.put(204, registry.meter(prefix.concat("status.noContent")));
        statusCodeMeters.put(400, registry.meter(prefix.concat("status.badRequest")));
        statusCodeMeters.put(404, registry.meter(prefix.concat("status.notFound")));
        statusCodeMeters.put(500, registry.meter(prefix.concat("status.serverError")));
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        activeRequests.inc();
        Timer.Context context = requestTimer.time();
        try{
            control.nextHandler();
        } finally {
            context.stop();
            activeRequests.dec();
            getMeterForStatus(response.status()).mark();
        }
    }

    private Meter getMeterForStatus(int status) {
        Meter statusCodeMeter = statusCodeMeters.get(status);
        if(statusCodeMeter != null) {
            return statusCodeMeter;
        } else {
            return otherMeter;
        }
    }
}