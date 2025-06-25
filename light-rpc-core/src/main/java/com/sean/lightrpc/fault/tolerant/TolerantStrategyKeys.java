package com.sean.lightrpc.fault.tolerant;

public interface TolerantStrategyKeys {

    /**
     *  FailBack: service auto recovery (Retry / Downgrade)
     */
    String FAIL_BACK = "failBack";

    /**
     *  FailFast: report exception and error immediately
     */
    String FAIL_FAST = "failFast";

    /**
     *  FailOver: switch to another service provider
     */
    String FAIL_OVER = "failOver";

    /**
     *  FailSafe: ignore the error
     */
    String FAIL_SAFE = "failSafe";
}
