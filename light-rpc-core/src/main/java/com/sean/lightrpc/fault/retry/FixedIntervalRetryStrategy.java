package com.sean.lightrpc.fault.retry;

import com.github.rholder.retry.*;
import com.sean.lightrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws ExecutionException, RetryException {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)    // Retry condition
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))   // Wait strategy
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))   // Stop strategy
                .withRetryListener(new RetryListener() {    // Monitor retry action to do logging
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("Retry time: {}", attempt.getAttemptNumber());
                    }
                })
                .build();

        return retryer.call(callable);
    }
}
