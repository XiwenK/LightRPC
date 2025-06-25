package com.sean.lightrpc.fault.retry;

import com.sean.lightrpc.model.RpcResponse;

import java.util.concurrent.Callable;

public class NoRetryStrategy implements RetryStrategy {

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
