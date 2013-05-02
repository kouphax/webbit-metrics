package org.webbitserver.metrics.handlers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class PingHandlerTest {

    @Test
    public void willReturnPongForAllRequests() throws Exception {
        PingHandler handler = new PingHandler();
        StubHttpResponse response = new StubHttpResponse();

        handler.handleHttpRequest(new StubHttpRequest(), response, new StubHttpControl());

        assertEquals("pong", response.contentsString());
        assertEquals(200, response.status());
    }
}
