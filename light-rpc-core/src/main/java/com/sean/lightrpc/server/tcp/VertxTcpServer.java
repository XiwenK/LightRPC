package com.sean.lightrpc.server.tcp;

import com.sean.lightrpc.server.RpcServer;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxTcpServer implements RpcServer {

    @Override
    public void doStart(int port) {
        // create vertx instance
        Vertx vertx = Vertx.vertx();

        // create TCP server instance
        NetServer server = vertx.createNetServer();

        // Binding TCP connection handler
        server.connectHandler(new TcpServerHandler());

        // start HTTP server and listen specified port
        server.listen(port, res -> {
            if (res.succeeded()) {
                log.info("Vertx HTTP server started on port {}", port);
            } else {
                log.error("Vertx HTTP server start error", res.cause());
            }
        });

        // Gracefully close vertx when JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));
    }
}
