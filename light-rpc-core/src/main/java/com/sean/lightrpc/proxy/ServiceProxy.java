package com.sean.lightrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.sean.lightrpc.RpcApplication;
import com.sean.lightrpc.config.RpcConfig;
import com.sean.lightrpc.constant.RpcConstant;
import com.sean.lightrpc.model.RpcRequest;
import com.sean.lightrpc.model.RpcResponse;
import com.sean.lightrpc.model.ServiceMetaInfo;
import com.sean.lightrpc.registry.Registry;
import com.sean.lightrpc.registry.RegistryFactory;
import com.sean.lightrpc.serializer.JdkSerializer;
import com.sean.lightrpc.serializer.Serializer;
import com.sean.lightrpc.serializer.SerializerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

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

        // TODO: Load Balancer to select service provider node
        String serviceAddress = serviceMetaInfoList.get(0).getServiceAddress();
        if (StrUtil.isBlank(serviceAddress)) {
            log.info("Service provider currently not available for {}", serviceName);
            return null;
        }

        // Get serializer
        Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());
        byte[] bodyBytes = serializer.serialize(rpcRequest);

        /* Serialize RpcRequest body and send HTTP Request
           Receive HTTP response and deserialize RpcResponse body
         */
        try (HttpResponse httpResponse = HttpRequest.post(serviceAddress)
                .body(bodyBytes)
                .execute())
        {
            byte[] result = httpResponse.bodyBytes();
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return rpcResponse.getData();
        } catch (IOException e) {
            log.info("RpcRequest error:{}", e.getMessage());
        }

        return null;
    }
}
