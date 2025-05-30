package com.sean.lightrpc.registry;

import com.sean.lightrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Cache for service info in registry
 *   - support multiple service discoveries at same time
 */
public class RegistryServiceMultiCache {

    Map<String, List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    void writeCache(String serviceKey, List<ServiceMetaInfo> newServiceCache) {
        this.serviceCache.put(serviceKey, newServiceCache);
    }

    List<ServiceMetaInfo> readCache(String serviceKey) {
        return this.serviceCache.get(serviceKey);
    }

    void clearCache(String serviceKey) {
        this.serviceCache.remove(serviceKey);
    }
}
