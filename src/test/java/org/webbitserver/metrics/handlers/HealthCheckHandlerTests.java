package org.webbitserver.metrics.handlers;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
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
public class HealthCheckHandlerTests {

    HealthCheckRegistry registry;
    HealthChecksHandler handler;
    StubHttpResponse response;

    @Before
    public void beforeEach(){
        registry = new HealthCheckRegistry();
        handler = new HealthChecksHandler(registry, Executors.newSingleThreadExecutor());
        response = new StubHttpResponse();
    }

    @Test
    public void returnsNotImplementedStatusWhenNoHealthChecksAdded() throws Exception {

        handler.handleHttpRequest(new StubHttpRequest(), response, new StubHttpControl());

        assertEquals(501, response.status());
        assertEquals("application/json", response.header("Content-Type"));
    }

    @Test
    public void returnsSuccessAndResponseBodyWhenAllHealthChecksPass() throws Exception {

        registry.register("always-passes", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        handler.handleHttpRequest(new StubHttpRequest(), response, new StubHttpControl());

        assertEquals(200, response.status());
        assertEquals("application/json", response.header("Content-Type"));
        assertEquals("{\"always-passes\":{\"healthy\":true}}", response.contentsString());
    }

    @Test
    public void returnsServerErrorAndResponseBodyWhenAHealthCheckFails() throws Exception {

        registry.register("always-fails", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.unhealthy("failed");
            }
        });

        registry.register("always-passes", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        handler.handleHttpRequest(new StubHttpRequest(), response, new StubHttpControl());

        assertEquals(500, response.status());
        assertEquals("application/json", response.header("Content-Type"));
        assertEquals("{\"always-fails\":{\"healthy\":false,\"message\":\"failed\"},\"always-passes\":{\"healthy\":true}}", response.contentsString());
    }
}
