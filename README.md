# webbit-metrics

[![Build Status](https://travis-ci.org/kouphax/webbit-metrics.png?branch=master)](https://travis-ci.org/kouphax/webbit-metrics)

A [Metrics](http://metrics.codahale.com) backed [Webbit](http://webbitserver.org) Server for great good. __webbit-metrics__ acts as a companion server that can be run alongside other simple services.  This is heavily inspired by the approach that [Twitter](http://twitter.com) take with [Finagle](twitter.github.io/finagle/) and [Ostrich](https://github.com/twitter/ostrich)

## Getting Started

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
