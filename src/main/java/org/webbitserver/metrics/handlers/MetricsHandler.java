package org.webbitserver.metrics.handlers;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.util.concurrent.ExecutorService;
import static java.util.concurrent.TimeUnit.*;

public class MetricsHandler implements HttpHandler {

    private MetricRegistry registry;
    private ExecutorService executor;
    private ObjectMapper mapper;

    public MetricsHandler(MetricRegistry registry) {
        this(registry, null);
    }

    public MetricsHandler(MetricRegistry registry, ExecutorService executor) {
        this.registry = registry;
        this.executor = executor;

        // As of right now we just pass in default values for the sampling and
        // duration of the Json Module, later we should make this configurable
        this.mapper = new ObjectMapper().registerModule(new MetricsModule(SECONDS, SECONDS, true));
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        response.header("Content-Type", "application/json")
                .header("Cache-Control", "must-revalidate,no-cache,no-store")
                .status(200)
                .content(mapper.writeValueAsString(registry))
                .end();
    }
}
