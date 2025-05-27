package com.sean.lightrpc.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 *  Service Consumer Side Package
 *  Service Proxy Factory Mode
 *   - Proxy handles the logic of sending requests
 *   - Based on rpc service, create corresponding proxy
 */
@Slf4j
public class ServiceProxyFactory {

    /**
     * Create proxy instance by serviceClass (must be interfaces)
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> serviceClass) {
        log.info("Get proxy for {}", serviceClass.toString());
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());
    }
}
