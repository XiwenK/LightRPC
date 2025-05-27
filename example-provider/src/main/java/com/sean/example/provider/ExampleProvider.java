package com.sean.example.provider;

import com.sean.example.common.service.UserService;
import com.sean.lightrpc.RpcApplication;
import com.sean.lightrpc.config.RpcConfig;
import com.sean.lightrpc.registry.LocalRegistry;
import com.sean.lightrpc.server.HttpServer;
import com.sean.lightrpc.server.VertxHttpServer;

/**
 *  Example Provider for easy RPC call implementation
 */
public class ExampleProvider {

    public static void main(String[] args) {
        // Load Configuration and Register Services
        RpcApplication.init();
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // Start vertx HTTP server
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(rpcConfig.getServerPort());
    }
}
