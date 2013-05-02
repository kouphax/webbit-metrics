package org.webbitserver.metrics;

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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;


@RunWith(JUnit4.class)
public class ThreadDumpWebServerTests {

    private NettyWebServer server;

    @BeforeClass
    public static void beforeAll() {
        port = 9080;
    }

    @Before
    public void beforeEach() throws ExecutionException, InterruptedException, IOException {
        server = new ThreadDumpWebServer(Executors.newSingleThreadScheduledExecutor(), 9080).start().get();
    }

    @Test
    public void respondsToRequestWithPong()  {
        expect().statusCode(equalTo(200))
                .contentType(equalTo("text/plain"))
                .body(not(empty()))
                .when()
                .get("/");
    }

    @Test
    public void ensuresTheResponseIsNotCached() {
        expect().header("Cache-Control", equalTo("must-revalidate,no-cache,no-store"))
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
