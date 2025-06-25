package com.sean.lightrpc.fault.tolerant;

import com.sean.lightrpc.model.RpcResponse;

import java.util.Map;

public interface TolerantStrategy {

    RpcResponse doTolerant(Map<String, Object> context, Exception e);
}
