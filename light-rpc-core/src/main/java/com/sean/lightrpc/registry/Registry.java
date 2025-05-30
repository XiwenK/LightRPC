package com.sean.lightrpc.registry;

import com.sean.lightrpc.config.RegistryConfig;
import com.sean.lightrpc.model.ServiceMetaInfo;

import java.util.List;

public interface Registry {

    void init(RegistryConfig registryConfig);

    /**
     *  Service Registration (Provider Side)
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     *  Service Unregistration (Provider Side)
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo);

    /**
     *  Service Discovery (Consumer Side)
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     *  Heartbeat Health Check (Provider Side)
     */
    void heartBeat();

    void watch(String serviceNodeKey);

    void destroy();
}
