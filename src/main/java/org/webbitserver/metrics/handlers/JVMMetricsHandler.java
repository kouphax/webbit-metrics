package org.webbitserver.metrics.handlers;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.json.MetricsModule;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import static java.util.concurrent.TimeUnit.SECONDS;

public class JVMMetricsHandler implements HttpHandler {

    private final MetricRegistry registry;
    private final MBeanServer server;
    private final ObjectMapper mapper;

    public JVMMetricsHandler() {
        this.registry = new MetricRegistry();
        this.server = MBeanServerFactory.createMBeanServer();

        // As of right now we just pass in default values for the sampling and
        // duration of the Json Module, later we should make this configurable
        this.mapper = new ObjectMapper().registerModule(new MetricsModule(SECONDS, SECONDS, true));

        registry.registerAll(new BufferPoolMetricSet(this.server));
        registry.registerAll(new MemoryUsageGaugeSet());
        registry.registerAll(new ThreadStatesGaugeSet());
        registry.registerAll(new GarbageCollectorMetricSet());
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
