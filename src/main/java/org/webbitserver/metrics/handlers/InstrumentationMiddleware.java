package org.webbitserver.metrics.handlers;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InstrumentationMiddleware implements HttpHandler {

    private static final int OK = 200;
    private static final int CREATED = 201;
    private static final int NO_CONTENT = 204;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;
    private static final int SERVER_ERROR = 500;
    private static final Map<Integer, String> statusCodeMeterNames;

    static {
        statusCodeMeterNames = new HashMap<>(6);
        statusCodeMeterNames.put(OK, "status.ok");
        statusCodeMeterNames.put(CREATED, "status.created");
        statusCodeMeterNames.put(NO_CONTENT, "status.notContent");
        statusCodeMeterNames.put(BAD_REQUEST, "status.badRequest");
        statusCodeMeterNames.put(NOT_FOUND, "status.notFound");
        statusCodeMeterNames.put(SERVER_ERROR, "status.serverError");
    }

    private final ConcurrentMap<Integer, Meter> statusCodeMeters;
    private final Counter activeRequests;
    private final Timer requestTimer;
    private final Meter otherMeter;
    private final String handlerId;

    protected final MetricRegistry registry;

    public InstrumentationMiddleware(MetricRegistry registry){
        this("", registry);
    }

    public InstrumentationMiddleware(String handlerId, MetricRegistry registry){
        this.registry = registry;
        this.handlerId = handlerId;

        String prefix = handlerId.isEmpty() ? "" : handlerId.concat(".");

        this.activeRequests = registry.counter(prefix.concat("active-requests"));
        this.requestTimer = registry.timer(prefix.concat("request"));
        this.statusCodeMeters = new ConcurrentHashMap<>(6);
        this.otherMeter = registry.meter(prefix.concat("status.other"));

        for(Map.Entry<Integer, String> entry : statusCodeMeterNames.entrySet()){
            String qualifiedMeterName = prefix.concat(entry.getValue());
            statusCodeMeters.put(entry.getKey(), registry.meter(qualifiedMeterName));
        }
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        activeRequests.inc();
        Timer.Context context = requestTimer.time();
        try{
            // TODO Do we really want to pass control in here?
            beforeHandle(request, response, control);

            control.nextHandler();
        } finally {

            // TODO Do we really want to pass control in here?
            afterHandle(request, response, control);

            context.stop();
            activeRequests.dec();

            Meter statusCodeMeter= statusCodeMeters.get(response.status());
            if(statusCodeMeter != null) {
                statusCodeMeter.mark();
            } else {
                otherMeter.mark();
            }
        }
    }

    public void beforeHandle(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {

    }

    public void afterHandle(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {

    }
}