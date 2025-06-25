package com.sean.lightrpc.fault.tolerant;

import com.sean.lightrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class FailBackTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.error("FailBackTolerantStrategy take charge caused by exception: {}", e.getMessage());

        // TODO: do retry or downgrade service

        return null;
    }
}
