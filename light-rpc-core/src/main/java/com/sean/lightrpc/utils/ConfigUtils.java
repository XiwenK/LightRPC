package com.sean.lightrpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 *  Read Configuration files to load config into RpcConfig instance
 */
public class ConfigUtils {

    /**
     * Use hutool to load config into RpcConfig instance by filename (application.properties) and prefix (rpc)
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");

        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");

        Props props = new Props(configFileBuilder.toString());

        return props.toBean(tClass, prefix);
    }
}
