package com.sean.lightrpc.config;

import com.sean.lightrpc.fault.retry.RetryStrategyKeys;
import com.sean.lightrpc.fault.tolerant.TolerantStrategyKeys;
import com.sean.lightrpc.loadbalancer.LoadBalancerKeys;
import com.sean.lightrpc.serializer.SerializerKeys;
import lombok.Data;

@Data
public class RpcConfig {

    private String name = "light-rpc";

    private String version = "1.0";

    private String serverHost = "localhost";

    private int serverPort = 8080;

    private String serializer = SerializerKeys.JDK;

    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    private String retryStrategy = RetryStrategyKeys.NO;

    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;

    private RegistryConfig registryConfig = new RegistryConfig();

}
