package org.webbitserver.metrics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.webbitserver.stub.StubHttpControl;
import org.webbitserver.stub.StubHttpRequest;
import org.webbitserver.stub.StubHttpResponse;

import static org.fest.assertions.api.Assertions.assertThat;


@RunWith(JUnit4.class)
public class ThreadDumpHandlerTest {

    @Test
    public void willReturnSomethingForAllRequests() throws Exception {
        ThreadDumpHandler handler = new ThreadDumpHandler();
        StubHttpResponse response = new StubHttpResponse();

        handler.handleHttpRequest(new StubHttpRequest(), response, new StubHttpControl());

        assertThat(response.contentsString()).isNotEmpty();
        assertThat(response.status()).isEqualTo(200);
    }
}
