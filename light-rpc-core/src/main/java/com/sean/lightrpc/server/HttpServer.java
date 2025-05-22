package com.sean.lightrpc.server;

public interface HttpServer {

    /**
     * Start server
     *
     * @param port port
     */
    void doStart(int port);
}
