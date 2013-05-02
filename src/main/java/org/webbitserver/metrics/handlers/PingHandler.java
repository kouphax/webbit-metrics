package org.webbitserver.metrics.handlers;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

public class PingHandler implements HttpHandler {
    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        response.header("Cache-Control", "must-revalidate,no-cache,no-store")
                .header("Content-Type", "text/plain")
                .status(200)
                .content("pong")
                .end();
    }
}
