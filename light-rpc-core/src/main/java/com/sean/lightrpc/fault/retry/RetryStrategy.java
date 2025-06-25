package com.sean.lightrpc.fault.retry;

import com.sean.lightrpc.model.RpcResponse;

import java.util.concurrent.Callable;

public interface RetryStrategy {

    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
