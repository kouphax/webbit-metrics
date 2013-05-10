# webbit-metrics

[![Build Status](https://travis-ci.org/kouphax/webbit-metrics.png?branch=master)](https://travis-ci.org/kouphax/webbit-metrics)

A [Metrics](http://metrics.codahale.com) backed [Webbit](http://webbitserver.org) Server for great good. __webbit-metrics__ acts as a companion server that can be run alongside other simple services.  This is heavily inspired by the approach that [Twitter](http://twitter.com) take with [Finagle](twitter.github.io/finagle/) and [Ostrich](https://github.com/twitter/ostrich)

## AdminWebServer

Lets assume we have a [Webbit](http://webbitserver.org) web server already.  Any sort of JVM based Web Server will do but [Webbit](http://webbitserver.org) ones give you a little bit more as we will see later. 

    import org.webbitserver.*;
    import org.webbitserver.netty.NettyWebServer;
    
    public class MyLovelyGreeterService {
        public static void main(String[] args) throws Exception {
    
            final NettyWebServer server = new NettyWebServer(9996);
    
            server.add("/", new HttpHandler(){
                @Override
                public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
                    response.content("Hello World").end();
                }
            });
    
            server.start().get();
            System.out.println("Server listening on port " + 9996);
        }
    }
    
If we want to monitor or instrument this service we can create an instance of an `AdminWebServer` that can run alongside it and collect metrics that our service generates.

	final AdminWebServer admin = new AdminWebServer(server, 9997);
    admin.start().get();
    
By adding these lines we end up with another server running on port `9997` that gives us,

- Basic monitoring of the JVM & Threads
- Ping service to check our service is reachable
- Ability to `start`, `stop` & `restart` our service (passing the service is optional for non-[Webbit](http://webbitserver.org) services).

This service also exposes its own `HealthCheckRegistry` & `MetricRegistry` that can be used by your service to monitor the health of and collect valuable application & business metrics for your service.

### Metrics

When an `AdminWebServer` is created it will create its own instance of a `MetricRegistry` that can be accessed via `admin.metrics`.  So lets add some metrics to our service.

    server.add("/", new HttpHandler(){
        @Override
        public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
            admin.metrics.counter("hit-count").inc();
            response.content("Hello World").end();
        }
    });
    
So we added `admin.metrics.counter("hit-count").inc();` which will increment the counter everytime someone hits that endpoint.  If we run the servers and go to [`http://localhost:9996`](http://localhost:9996) then [`http://localhost:9997/metrics`](http://127.0.0.1:9997/metrics) we should see a JSON response like this,

    {
    	version: "3.0.0",
    	gauges: { },
    	counters: {
    		hit-count: {
    			count: 1
    		}
    	},
    	histograms: { },
    	meters: { },
    	timers: { }
    }

This is just a raw response from [Metrics](http://metrics.codahale.com), no need for any special sugar or transformation.  The `AdminWebServer` registry is a vanilla [Metrics](http://metrics.codahale.com) `MetricRegistry` and so supports all of [Metrics metrics](http://metrics.codahale.com/manual/core/) like [Gauges](http://metrics.codahale.com/manual/core/#gauges), [Counters](http://metrics.codahale.com/manual/core/#counters), [Histograms](http://metrics.codahale.com/manual/core/#histograms), [Meters](http://metrics.codahale.com/manual/core/#meters) and [Timers](http://metrics.codahale.com/manual/core/#timers).

### Healthchecks

If your service relies on a database connection or some external service to function then `HealthChecks` help monitor that these things are actually reachable and working as expected.  You can add a healthcheck to the registry easily,

    admin.healthchecks.register("randomly-unhealthy", new HealthCheck() {
        @Override
        protected Result check() throws Exception {
            final Boolean unhealthy = Math.random() < 0.5;
            
            if(unhealthy){
                return Result.unhealthy("I've decided to have a sick day");
            }
            
            return Result.healthy();
        }
    });
    
This example is horribly contrived but it serves its purpose.  Hitting [`http://localhost:9997/healthchecks`](http://127.0.0.1:9997/healthchecks) will run all registered healthchecks and return a JSON response,

    {
      randomly-unhealthy: {
        healthy: false,
        message: "I've decided to have a sick day"
      }
    }
    
It will return an approximate HTTP Status code as well (`200 - OK` if everything is healthy, `500 - Internal Server Error` if these is something up and `501 - Not Implemented` if there are no registered healthchecks.

### Ping

Hitting [`http://localhost:9997/ping`](http://127.0.0.1:9997/ping) will simply return a `200 - OK` response with plain text body containing `pong`.  This is useful in situations where you want to know if a machine or service is actually still around and reachable

### Thread Dump & JVM Metrics

[`http://localhost:9997/jvm`](http://127.0.0.1:9997/jvm) & [`http://localhost:9997/dump`](http://127.0.0.1:9997/dump) will give you some JVM metrics and Thread Dumps respectivley.

### Server Control

If you pass in an instance of a [Webbit](http://webbitserver.org) server you have some control over the running of that service. 

- [`/start`](http://127.0.0.1:9997/start) - Starts the service.  Throws if the service is already started.
- [`/stop`](http://127.0.0.1:9997/stop) - Stops the service.
- [`/restart`](http://127.0.0.1:9997/restart) - Stops and then Starts the service.

### Tasks

Tasks are arbitrary bits of code that can be run at any time from your `AdminWebServer` instance.  You can register as `Task` against the `TaskRegistry` instance created along with your `AdminWebServer` instance.

    admin.tasks.register("seed-database", new Task() {
        @Override
        public void execute() throws Exception {
            // seed the database with some data
        }
    });
    
Browsing to [/tasks](http://localhost:9997/tasks) will list available tasks and passing a `name` parameter in the querystring of a task will run that task e.g. [/tasks?name=seed-database](http://localhost:9997/tasks?name=seed-database).

Any exceptions thrown in the course of running a task will be piped out to the browser.

## InstrumentedMiddleware

`InstrumentedMiddleware` is a Webbit handler that provides some basic metrics for requests.  Placing it at the head of your middleware stack for all requests in a [Webbit](http://webbitserver.org) will monitor the entire request chain

    server.add(new InstrumentationMiddleware("my-lovely-greeter-service", admin.metrics));
    server.add(...);
    server.add(...);
    server.add("/", new HttpHandler(){
        @Override
        public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
            response.content("Hello World").end();
        }
    });

You can optionally pass in a `handlerName` string as a first parameter and this will be appended to the instrumented metrics (useful if you have multiple handlers going to the same metrics registry).

The following metrics are provided by `InstrumentedMiddleware`

- `active-request` counter - number of currently active requests
- Meters for HTTP statuses
  - `status.badRequest` (HTTP Status 400)
  - `status.created` (HTTP Status 201)
  - `status.noContent` (HTTP Status 204)
  - `status.notFound` (HTTP Status 404)
  - `status.ok` (HTTP Status 200)
  - `status.serverError` (HTTP Status 500)
  - `status.` (All other HTTP Statuses)
- `request` timer - time based stats for request processing time

These will be available under [/metrics](http://localhost:9996/metrics).
