package com.sean.lightrpc.proxy;

import java.lang.reflect.Proxy;

/**
 *  Service Consumer Side Package
 *  Service Proxy Factory Mode
 *   - Proxy handles the logic of sending requests
 *   - Based on rpc service, create corresponding proxy
 */
public class ServiceProxyFactory {

    /**
     * Create proxy instance by serviceClass (must be interfaces)
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceProxy());
    }
}
