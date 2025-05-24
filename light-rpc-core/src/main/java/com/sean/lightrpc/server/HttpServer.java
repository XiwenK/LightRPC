package com.sean.lightrpc.server;

/**
 *  Service Provider Side Package
 */
public interface HttpServer {

    /**
     * Start server
     *
     * @param port port
     */
    void doStart(int port);
}
