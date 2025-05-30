package com.sean.lightrpc.model;

import cn.hutool.core.util.StrUtil;
import com.sean.lightrpc.constant.RpcConstant;
import lombok.Data;

@Data
public class ServiceMetaInfo {

    private String serviceName;

    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

    private String serviceHost;

    private int servicePort;

    /**
     *  TO BE IMPLEMENTED
     */
    private String serviceGroup = "default";

    /**
     *  Service info
     *  Return serviceName:serviceVersion
     */
    public String getServiceKey() {
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     *  Calling URL
     *  Return (http://)serviceHost:servicePort
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }

    /**
     *  Full meta info string (service info + node info)
     *  Return serviceName:serviceVersion/serviceHost:servicePort
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }
}
