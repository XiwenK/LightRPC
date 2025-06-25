package com.sean.lightrpc.fault.tolerant;

import com.sean.lightrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FailFastTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.error("FailFastTolerantStrategy take charge caused by exception: {}", e.getMessage());
        throw new RuntimeException("Service Error", e);
    }
}
