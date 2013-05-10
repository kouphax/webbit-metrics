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

import static org.fest.assertions.api.Assertions.*;

@RunWith(JUnit4.class)
public class HealthCheckHandlerTest {

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
        assertCompleteJSONResponse(501, null);
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
        assertCompleteJSONResponse(200, "{\"always-passes\":{\"healthy\":true}}");
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
        assertCompleteJSONResponse(500, "{\"always-fails\":{\"healthy\":false,\"message\":\"failed\"},\"always-passes\":{\"healthy\":true}}");
    }

    private void assertCompleteJSONResponse(int status, String body) {
        assertThat(response.status()).isEqualTo(status);
        assertThat(response.header("Content-Type")).isEqualTo("application/json");
        assertThat(response.ended()).isTrue();
        if(body != null) {
            assertThat(response.contentsString()).isEqualTo(body);
        }
    }
}
