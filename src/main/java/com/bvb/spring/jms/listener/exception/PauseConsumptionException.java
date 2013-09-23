package com.bvb.spring.jms.listener.exception;

import org.springframework.util.Assert;

import com.bvb.spring.jms.listener.BackoffDefaultMessageListeningContainer;
import com.bvb.spring.jms.listener.config.PauseConfig;



/**
 * A listener task for a {@link BackoffDefaultMessageListeningContainer} can throw this exception to indicate that it has
 * not been able to make contact with an upstream service or that it requires the consumption of messages it receives to
 * stop for a period of time.  The period is configurable and will be provided in the exception.  If a keep alive is
 * configured for the DMLC then the keep alive will be fired first and success tested for before consuming any more messages.
 * This exception also allows a listener to indicate that the number of concurrent messages consumers should be throttled for
 * a period.
 */
public class PauseConsumptionException extends RuntimeException
{
    private static final long serialVersionUID = -658143139189477898L;
    
    private final PauseConfig pauseConfig;

    /**
     * Build providing the pause config and the cause of the exception. 
     * @param pauseConfig the config for pausing or throttling the DMLC.
     * @param cause the original cause of the exception.
     * @throws NullPointerException if the pauseConfig is null.
     */
    public PauseConsumptionException(PauseConfig pauseConfig, Throwable cause)
    {
        super(cause);
        Assert.notNull(pauseConfig, "pauseConfig cannot be null");
        this.pauseConfig = pauseConfig;
    }

    /**
     * Get the config supplied with this exception.
     * @return the config.
     */
    public PauseConfig getConfig()
    {
        return pauseConfig;
    }
    
}
