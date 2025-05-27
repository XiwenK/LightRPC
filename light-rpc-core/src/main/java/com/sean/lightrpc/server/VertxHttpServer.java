package com.sean.lightrpc.server;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxHttpServer implements HttpServer {

    @Override
    public void doStart(int port) {
        // create vertx instance
        Vertx vertx = Vertx.vertx();

        // create HTTP server instance
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        // Binding request handler
        server.requestHandler(new HttpServerHandler());

        // start HTTP server and listen specified port
        server.listen(port, res -> {
            if (res.succeeded()) {
                log.info("Vertx HTTP server started on port {}", port);
            } else {
                log.error("Vertx HTTP server start error", res.cause());
            }
        });
    }
}
