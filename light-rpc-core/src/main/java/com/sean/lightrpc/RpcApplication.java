package com.sean.lightrpc;

import com.sean.lightrpc.config.RpcConfig;
import com.sean.lightrpc.constant.RpcConstant;
import com.sean.lightrpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

import java.rmi.registry.Registry;

/**
 *  Framework Boot Entrance
 *   - Hold global variables (RpcConfig instance)
 *   - Double-Checked Locking Singleton Pattern
 */
@Slf4j
public class RpcApplication {

    /**
     *  volatile:
     *  - avoid instruction reorder (must first instantiate then assign value
     *  - make rpcConfig transient for all threads
     */
    private static volatile RpcConfig rpcConfig;

    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;

        log.info("RPC Init, config = {}", newRpcConfig.toString());
        
        // Registry Config Initialization
        // RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        // Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        // registry.init(registryConfig);
        // log.info("registry init, config = {}", registryConfig);
        // 创建并注册 Shutdown Hook，JVM 退出时执行操作
        // Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    public static void init() {
        RpcConfig newRpcConfig = null;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX, "");
        } catch (Exception e) {
            // load config failed, use default settings
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     *  Double-Checked Locking Singleton Pattern
     *   - First-time check: to avoid multiple competition for lock
     *   - Second-time check: to ensure thread-safety
     */
    public static RpcConfig getRpcConfig() {
        // First Is-Null check
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                // Second Is-Null check
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
