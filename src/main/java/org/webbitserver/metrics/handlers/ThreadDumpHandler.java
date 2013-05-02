package org.webbitserver.metrics.handlers;

import com.codahale.metrics.jvm.ThreadDump;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;

public class ThreadDumpHandler implements HttpHandler {

    ThreadDump dumper;

    public ThreadDumpHandler() {
        this.dumper = new ThreadDump(ManagementFactory.getThreadMXBean());
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        response.header("Cache-Control", "must-revalidate,no-cache,no-store")
                .header("Content-Type", "text/plain")
                .status(200)
                .content(dump())
                .end();
    }

    private byte[] dump() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        dumper.dump(stream);
        return stream.toByteArray();
    }
}
