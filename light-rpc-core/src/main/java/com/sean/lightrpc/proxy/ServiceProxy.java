package com.sean.lightrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.sean.lightrpc.RpcApplication;
import com.sean.lightrpc.config.RpcConfig;
import com.sean.lightrpc.constant.RpcConstant;
import com.sean.lightrpc.fault.retry.RetryStrategy;
import com.sean.lightrpc.fault.retry.RetryStrategyFactory;
import com.sean.lightrpc.fault.tolerant.TolerantStrategy;
import com.sean.lightrpc.fault.tolerant.TolerantStrategyFactory;
import com.sean.lightrpc.loadbalancer.LoadBalancer;
import com.sean.lightrpc.loadbalancer.LoadBalancerFactory;
import com.sean.lightrpc.model.RpcRequest;
import com.sean.lightrpc.model.RpcResponse;
import com.sean.lightrpc.model.ServiceMetaInfo;
import com.sean.lightrpc.registry.Registry;
import com.sean.lightrpc.registry.RegistryFactory;
import com.sean.lightrpc.serializer.Serializer;
import com.sean.lightrpc.serializer.SerializerFactory;
import com.sean.lightrpc.server.tcp.VertxTcpClient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Service Proxy (Dynamic Proxy)
 *   - create proxy during runtime based on
 */
@Slf4j
public class ServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String serviceName = method.getDeclaringClass().getName();

        // Build RpcRequest body
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .params(args)
                .build();

        // Get registry instance from configs
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());

        // Construct serviceMetaInfo
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);

        // Call service discovery
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            log.info("No available service provider nodes for {}", serviceName);
            return null;
        }

        // Get loadBalancer instance to select service provider
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());

        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());

        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);
        log.info("Selected service meta info: {}", selectedServiceMetaInfo.toString());

        /* Used for HTTP Request
         *  Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
         *  byte[] bodyBytes = serializer.serialize(rpcRequest);
         */

        RpcResponse rpcResponse;
        try {
            // Set retry strategy, after which tolerant strategy will be triggered
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());

            // Do HTTP Request
            // rpcResponse = retryStrategy.doRetry(() -> doHttpRequest(selectedServiceMetaInfo, bodyBytes, serializer));

            // Do TCP Request
            rpcResponse = retryStrategy.doRetry(() ->
                    VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );
        } catch (Exception e) {
            // Trigger tolerant strategy
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }

        return rpcResponse.getData();
    }

    /**
     * Do HTTP Request
     */
    private static RpcResponse doHttpRequest(ServiceMetaInfo selectedServiceMetaInfo, byte[] bodyBytes, Serializer serializer) throws IOException {
        String serviceAddress = selectedServiceMetaInfo.getServiceAddress();

        if (StrUtil.isBlank(serviceAddress)) {
            log.info("Service provider currently not available for {}", selectedServiceMetaInfo);
            throw new RuntimeException("Service provider currently not available for: " + selectedServiceMetaInfo);
        }

        // Send HTTP Request
        try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                .body(bodyBytes)
                .execute()) {
            byte[] result = httpResponse.bodyBytes();
            return serializer.deserialize(result, RpcResponse.class);
        }
    }
}
