package com.sean.lightrpc.loadbalancer;

import com.sean.lightrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

public interface LoadBalancer {

    ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);
}
