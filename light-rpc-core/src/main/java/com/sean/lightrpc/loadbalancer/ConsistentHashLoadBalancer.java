package com.sean.lightrpc.loadbalancer;

import com.sean.lightrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * Consistent Hash Circle used to store virtual nodes
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    /**
     * Number of virtual nodes
     */
    private static final int VIRTUAL_NODE_NUM = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) return null;

        // Construct circle of virtual nodes
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i ++) {
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }

        // Get the hash value of request params
        int hash = getHash(requestParams);

        // Select virtual node with the smallest hash value no smaller than the target hash value
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);

        // If not have such virtual node then return the first entry
        if (entry == null) entry = virtualNodes.firstEntry();

        return entry.getValue();
    }


    /**
     * Hash Algo (can also self-implemented)
     */
    private int getHash(Object key) {
        return key.hashCode();
    }
}
