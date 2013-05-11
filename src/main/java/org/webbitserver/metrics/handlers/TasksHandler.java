package org.webbitserver.metrics.handlers;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.metrics.registries.TaskRegistry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.NoSuchElementException;

public class TasksHandler implements HttpHandler {

    private final TaskRegistry registry;

    public TasksHandler(TaskRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
        String name = request.queryParam("name");

        // attempt to stop caching
        response.header("Cache-Control", "must-revalidate,no-cache,no-store");

        if(name == null) {
            renderTaskList(response);
        } else {
            executeTask(name, response);
        }
    }

    private void executeTask(String name, HttpResponse response) {

        String body;
        try {
            registry.runTask(name);
            body = name + " ran without complaint.";
        } catch(NoSuchElementException nsee) {
            body = nsee.getMessage();
        } catch(Exception e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer, true));
            body = writer.toString();
        }

        response.header("Content-Type", "text/plain")
                .content(body)
                .end();
    }


    private void renderTaskList(HttpResponse response) {

        StringBuilder tasks = new StringBuilder("<h1>Tasks</h1>");
        tasks.append("<ul>");
        for(String taskName : registry.getNames()){
            tasks.append("<li>")
                 .append("  <a href='/tasks?name=" + taskName + "'>")
                 .append(taskName)
                 .append("  </a>")
                 .append("</li>");
        }
        tasks.append("</ul>");

        response.header("Content-Type", "text/html")
                .content(tasks.toString())
                .end();
    }
}
