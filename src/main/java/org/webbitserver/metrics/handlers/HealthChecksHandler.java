package org.webbitserver.metrics.handlers;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.json.HealthCheckModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;

public class HealthChecksHandler implements HttpHandler {

    private HealthCheckRegistry registry;
    private ExecutorService executor;
    private ObjectMapper mapper;

    public HealthChecksHandler(HealthCheckRegistry registry) {
        this(registry, null);
    }

    public HealthChecksHandler(HealthCheckRegistry registry, ExecutorService executor) {
        this.registry = registry;
        this.executor = executor;
        this.mapper = new ObjectMapper().registerModule(new HealthCheckModule());
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        SortedMap<String, HealthCheck.Result> results = runHealthChecks();

        response.header("Content-Type", "application/json")
                .header("Cache-Control", "must-revalidate,no-cache,no-store");

        // check to see if we have any healthchecks returned
        if (results.isEmpty()) {
            response.status(501);
        } else {
            // Check if all the healthchecks were healthy
            if (isAllHealthy(results)) {
                response.status(200);
            } else {
                response.status(500);
            }
        }

        String json = mapper.writeValueAsString(results);

        response.content(json).end();
    }

    private SortedMap<String, HealthCheck.Result> runHealthChecks(){
        if(executor == null){
            return registry.runHealthChecks();
        }

        return registry.runHealthChecks(executor);
    }

    private static boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
        for (HealthCheck.Result result : results.values()) {
            if (!result.isHealthy()) {
                return false;
            }
        }
        return true;
    }
}
