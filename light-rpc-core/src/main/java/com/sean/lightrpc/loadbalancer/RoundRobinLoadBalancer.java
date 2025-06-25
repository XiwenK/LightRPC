package com.sean.lightrpc.loadbalancer;

import com.sean.lightrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {

    /**
     *  Current index: use AtomicInteger to ensure thread-safety
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) return null;

        // If only got one service provider
        int size = serviceMetaInfoList.size();
        if (size == 1) return serviceMetaInfoList.get(0);

        // RoundRobin by modulo
        int index = currentIndex.getAndIncrement() % size;
        return serviceMetaInfoList.get(index);
    }
}
