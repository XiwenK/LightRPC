package com.sean.example.provider;

import com.sean.example.common.service.UserService;
import com.sean.lightrpc.RpcApplication;
import com.sean.lightrpc.config.RpcConfig;
import com.sean.lightrpc.model.ServiceMetaInfo;
import com.sean.lightrpc.model.ServiceRegisterInfo;
import com.sean.lightrpc.registry.LocalRegistry;
import com.sean.lightrpc.registry.Registry;
import com.sean.lightrpc.registry.RegistryFactory;
import com.sean.lightrpc.server.HttpServer;
import com.sean.lightrpc.server.VertxHttpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 *  Example Provider for easy RPC call implementation
 */
@Slf4j
public class ExampleProvider {

    public static void main(String[] args) {
        // Load configuration and registry
        RpcApplication.init();
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // Local registry
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // Distributed Registry
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(UserService.class.getName());
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            log.info("Register service {} error: {}", serviceMetaInfo.getServiceKey(), e.getMessage());
        }

        // Start vertx HTTP server
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(rpcConfig.getServerPort());
    }
}
