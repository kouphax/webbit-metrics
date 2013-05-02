package org.webbitserver.metrics;

import com.codahale.metrics.health.HealthCheck;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.webbitserver.netty.NettyWebServer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.port;
import static org.hamcrest.Matchers.equalTo;

@RunWith(JUnit4.class)
public class HealthCheckWebServerTest {

    private HealthCheckWebServer server;

    @BeforeClass
    public static void beforeAll() {
        port = 9080;
    }

    @Before
    public void beforeEach() throws ExecutionException, InterruptedException, IOException{
        server = (HealthCheckWebServer) new HealthCheckWebServer(9080).start().get();
    }

    @Test
    public void respondsToRequestWithPong()  {
        server.registry().register("always-fails", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.unhealthy("failed");
            }
        });
        server.registry().register("always-passes", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        expect().statusCode(equalTo(500))
                .body(equalTo("{\"always-fails\":{\"healthy\":false,\"message\":\"failed\"},\"always-passes\":{\"healthy\":true}}"))
                .contentType(equalTo("application/json"))
                .when()
                .get("/");
    }

    @After
    public void afterEach() {
        if(server.isRunning()){
            try {
                server.stop().get();
            } catch (Exception e) {
            }
        }
    }

}
