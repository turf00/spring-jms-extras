package com.bvb.spring.jms.listener.keepalive;

import java.util.concurrent.TimeUnit;

import com.bvb.spring.jms.listener.BackoffDefaultMessageListeningContainer;
import com.bvb.spring.jms.listener.config.PauseConfig;
import com.bvb.spring.jms.listener.config.PauseConfigBuilder;
import com.bvb.spring.jms.listener.keepalive.KeepAliveResponse;

/**
 * Provide the ability to create the KeepAlive config that the {@link BackoffDefaultMessageListeningContainer} requires
 * in order to throttle message consumption or return it to normal after a successful keep alive response.
 */
public class KeepAliveConfig
{
    private long keepAliveIntervalSecSuccess;
    private long keepAliveIntervalSecFailed;
    private long keepAliveIntervalMsFailed;
    private int maxConsumersThrottled;
    private long throttlingDeliveryPeriodSec;
    private KeepAliveResponse success;
    private KeepAliveResponse failed;
    
    public KeepAliveResponse getSuccess()
    {
        return success;
    }
    
    public KeepAliveResponse getFailed()
    {
        return failed;
    }
    
    /**
     * Get a keep alive response, providing the delay before the next keep alive is tried.
     * @param nextSendIntervalInMs the delay to make the next keep alive response.
     * @return the keep alive response built.
     */
    public KeepAliveResponse getThrottledForMs(Long nextSendIntervalInMs)
    {
        long delayMs = (nextSendIntervalInMs == null) ? keepAliveIntervalMsFailed : nextSendIntervalInMs;
        return new KeepAliveResponse(false, getConfigThrottledForMs(delayMs));
    }
    
    /**
     * Get a PauseConfig by providing the next interval in seconds to delay until re-starting consumption and sending of
     * messages.  If the interval provided is smaller than the {@link #keepAliveIntervalSecFailed} then the
     * {@link #keepAliveIntervalSecFailed} will be used.
     * @param nextSendIntervalInSecs the interval in seconds to pause before attempting to restart delivery.
     * @return the pause config containing the throttling and delay for attempting consumption.
     */
    public PauseConfig getConfigThrottledForSecs(long nextSendIntervalInSecs)
    {
        long delayToUseSecs = (nextSendIntervalInSecs < keepAliveIntervalSecFailed) ? keepAliveIntervalSecFailed :
            nextSendIntervalInSecs;

        return PauseConfigBuilder.newBuilder()
        .withThrottleMaxConcurrency(maxConsumersThrottled).withThrottleDeliveryForXSeconds(throttlingDeliveryPeriodSec)
        .withDelayConsumptionForXSeconds(delayToUseSecs).build();
    }
    
    /**
     * Get a PauseConfig by providing the next interval in milliseconds to delay until re-starting consumption and sending of
     * messages.  If the interval provided is smaller than the {@link #keepAliveIntervalMsFailed} then the
     * {@link #keepAliveIntervalMsFailed} will be used.
     * @param nextSendIntervalInMs the interval in milliseconds to pause before attempting to restart delivery.
     * @return the pause config containing the throttling and delay for attempting consumption.
     */
    public PauseConfig getConfigThrottledForMs(long nextSendIntervalInMs)
    {
        long delayToUseMs = nextSendIntervalInMs < keepAliveIntervalMsFailed ? keepAliveIntervalMsFailed : nextSendIntervalInMs;
        return PauseConfigBuilder.newBuilder().withThrottleMaxConcurrency(maxConsumersThrottled)
                .withThrottleDeliveryForXSeconds(throttlingDeliveryPeriodSec)
                .withDelayConsumptionForXMs(delayToUseMs).build();
    }

    public long getKeepAliveIntervalSecSuccess()
    {
        return keepAliveIntervalSecSuccess;
    }

    public void setKeepAliveIntervalSecSuccess(long keepAliveIntervalSecSuccess)
    {
        this.keepAliveIntervalSecSuccess = keepAliveIntervalSecSuccess;
        success = new KeepAliveResponse(true, 
            PauseConfigBuilder.newBuilder().withDelayConsumptionForXSeconds(keepAliveIntervalSecSuccess).build());
    }
    
    public void setKeepAliveIntervalMsSuccess(long keepAliveIntervalMsSuccess)
    {
        this.keepAliveIntervalSecSuccess = TimeUnit.MILLISECONDS.toSeconds(keepAliveIntervalMsSuccess);
        success = new KeepAliveResponse(true, 
            PauseConfigBuilder.newBuilder().withDelayConsumptionForXMs(keepAliveIntervalMsSuccess).build());
    }

    public long getKeepAliveIntervalSecFailed()
    {
        return keepAliveIntervalSecFailed;
    }

    public void setKeepAliveIntervalSecFailed(long keepAliveIntervalSecFailed)
    {
        this.keepAliveIntervalSecFailed = keepAliveIntervalSecFailed;
        this.keepAliveIntervalMsFailed = TimeUnit.SECONDS.toMillis(keepAliveIntervalSecFailed);
        failed = new KeepAliveResponse(false,
            PauseConfigBuilder.newBuilder().withDelayConsumptionForXSeconds(keepAliveIntervalSecFailed).build());
    }

    public int getMaxConsumersThrottled()
    {
        return maxConsumersThrottled;
    }

    public void setMaxConsumersThrottled(int maxConsumersThrottled)
    {
        this.maxConsumersThrottled = maxConsumersThrottled;
    }

    public long getThrottlingDeliveryPeriodSec()
    {
        return throttlingDeliveryPeriodSec;
    }

    public void setThrottlingDeliveryPeriodSec(long throttlingDeliveryPeriodSec)
    {
        this.throttlingDeliveryPeriodSec = throttlingDeliveryPeriodSec;
    }
    
}
