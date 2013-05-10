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