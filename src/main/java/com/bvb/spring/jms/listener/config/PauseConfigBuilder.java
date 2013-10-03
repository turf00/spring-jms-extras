package com.bvb.spring.jms.listener.config;

import java.util.concurrent.TimeUnit;

/**
 * Build a {@link PauseConfig}.
 */
public class PauseConfigBuilder
{
    private Long throttleDeliveryForMs;
    private Long throttleRelaxEveryMs;
    private Long delayConsumptionForMs;
    private Integer throttleMaxConcurrent;

    /**
     * Get a new instance of the builder to use.
     * @return the builder.
     */
    public static PauseConfigBuilder newBuilder()
    {
        return new PauseConfigBuilder();
    }
    
    /**
     * Throttle delivery for a period in seconds.
     * @param delaySeconds the length of time to throttle by.  
     * @return the builder.
     */
    public PauseConfigBuilder withThrottleDeliveryForXSeconds(long delaySeconds)
    {
        this.throttleDeliveryForMs = TimeUnit.SECONDS.toMillis(delaySeconds);
        return this;
    }
    
    /**
     * Throttle delivery for a period in minutes.
     * @param delayMinutes the length of time in minutes to throttle by.
     * @return the builder.
     */
    public PauseConfigBuilder withThrottleDeliveryForXMinutes(int delayMinutes)
    {
        this.throttleDeliveryForMs = TimeUnit.MINUTES.toMillis(delayMinutes);
        return this;
    }
    
    /**
     * Throttle delivery for a period in milliseconds.
     * @param delayMs the length of time in milliseconds to throttle by.
     * @return the builder.
     */
    public PauseConfigBuilder withThrottleDeliveryForXMs(long delayMs)
    {
        this.throttleDeliveryForMs = delayMs;
        return this;
    }

    /**
     * The throttling will be relaxed gradually, specify how often this should occur.  The minimum value allowed here is
     * 1 minute.
     * @param relaxSeconds the number of seconds between relaxing the throttle. 
     * @return the builder.
     */
    public PauseConfigBuilder withThrottleRelaxEveryXSeconds(long relaxSeconds)
    {
        this.throttleRelaxEveryMs = TimeUnit.SECONDS.toMillis(relaxSeconds);
        return this;
    }
    
    /**
     * The throttling will be relaxed gradually, specify how often this should occur.  The minimum value allowed here is
     * 1 minute.
     * @param relaxSeconds the number of seconds between relaxing the throttle. 
     * @return the builder.
     */
    public PauseConfigBuilder withThrottleRelaxEveryXMinutes(long relaxMinutes)
    {
        this.throttleRelaxEveryMs = TimeUnit.MINUTES.toMillis(relaxMinutes);
        return this;
    }
    
    /**
     * Set the max concurrent consumers to use when throttling.  This is the initial throttled value and then the
     * throttling is relaxed gradually.
     * @param maxConcurrent the max number of concurrent values to use.
     * @return the builder.
     */
    public PauseConfigBuilder withThrottleMaxConcurrency(int maxConcurrent)
    {
        this.throttleMaxConcurrent = maxConcurrent;
        return this;
    }
    
    /**
     * Delay consumption of messages for this length of time in milliseconds.
     * @param delayConsumptionForMs delay for these milliseconds.
     * @return the builder.
     */
    public PauseConfigBuilder withDelayConsumptionForXMs(long delayConsumptionForMs)
    {
        this.delayConsumptionForMs = delayConsumptionForMs;
        return this;
    }
    
    /**
     * Delay consumption of messages for this length of time in seconds.
     * @param delaySeconds the number of seconds to delay by.
     * @return the builder.
     */
    public PauseConfigBuilder withDelayConsumptionForXSeconds(long delaySeconds)
    {
        this.delayConsumptionForMs = TimeUnit.SECONDS.toMillis(delaySeconds);
        return this;
    }
    
    /**
     * Delay consumption of messages for a period of minutes.
     * @param delayMinutes the number of minutes to delay consumption by.
     * @return the builder.
     */
    public PauseConfigBuilder withDelayConsumptionForXMinutes(int delayMinutes)
    {
        this.delayConsumptionForMs = TimeUnit.MINUTES.toMillis(delayMinutes);
        return this;
    }
    
    
    public PauseConfig build()
    {
        return new PauseConfig(delayConsumptionForMs, throttleDeliveryForMs, throttleRelaxEveryMs, throttleMaxConcurrent);
    }
    
}
