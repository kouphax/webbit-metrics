package org.webbitserver.metrics.handlers;

import com.codahale.metrics.MetricRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class MetricsHandlerTests {

    MetricRegistry registry;
    MetricsHandler handler;
    StubHttpResponse response;

    @Before
    public void beforeEach(){
        registry = new MetricRegistry();
        handler = new MetricsHandler(registry, Executors.newSingleThreadExecutor());
        response = new StubHttpResponse();
    }

    @Test
    public void returnsAJsonResponseEvenIfNothingIsRegistered() throws Exception {

        handler.handleHttpRequest(new StubHttpRequest(), response, new StubHttpControl());

        assertEquals(200, response.status());
        assertEquals("application/json", response.header("Content-Type"));
        assertEquals("{\"version\":\"3.0.0\",\"gauges\":{},\"counters\":{},\"histograms\":{},\"meters\":{},\"timers\":{}}", response.contentsString());
    }


    @Test
    public void returnsValuesOfRegisteredMetricsWhenCalled() throws Exception {

        registry.counter("increments").inc();
        registry.counter("increments").inc();

        handler.handleHttpRequest(new StubHttpRequest(), response, new StubHttpControl());

        assertEquals(200, response.status());
        assertEquals("application/json", response.header("Content-Type"));
        assertEquals("{\"version\":\"3.0.0\",\"gauges\":{},\"counters\":{\"increments\":{\"count\":2}},\"histograms\":{},\"meters\":{},\"timers\":{}}", response.contentsString());
    }

}
