package com.bvb.spring.jms.listener.config;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Objects;

/**
 * Configuration provided by a client of whether consumption of messages should be delayed for a period and whether the
 * throttling of the consumption should be enabled.  Use the {@link PauseConfigBuilder} to construct instances.
 */
public class PauseConfig
{
    public static final long THROTTLE_DELIVERY_PERIOD_10_MINS = TimeUnit.MINUTES.toMillis(10);
    public static final long THROTTLE_RELAX_MIN_INTERVAL = TimeUnit.MINUTES.toMillis(1);
    public static final int THROTTLE_MIN_CONCURRENT_COUNT = 1;
    
    private final Long throttleDeliveryForMs;
    private final Long throttleRelaxEveryMs;
    private final Long delayConsumptionForMs;
    private final Integer throttleMaxConcurrent;
    
    public PauseConfig()
    {
        this(null, null, null, null);
    }
    
    PauseConfig(Long delayConsumptionForMs, Long throttleDeliveryForPeriodMs, Long throttleRelaxIntervalMs,
            Integer throttleMaxConcurrent)
    {
        this.throttleDeliveryForMs = throttleDeliveryForPeriodMs;
        this.throttleRelaxEveryMs = throttleRelaxIntervalMs;
        this.throttleMaxConcurrent = throttleMaxConcurrent;
        this.delayConsumptionForMs = delayConsumptionForMs;
    }
    
    /**
     * Construct a configuration based on another configuration, but limiting by the maximum concurrent provided.  If the
     * max concurrent in the other configuration to copy if larger than that provided in the constructor call, the value
     * provided in the constructor is used.
     * @param maxConcurrentAllowed the max the concurrent can be.
     * @param other the pause config to copy from.
     */
    public PauseConfig(int maxConcurrentAllowed, PauseConfig other)
    {
        this.throttleDeliveryForMs = other.throttleDeliveryForMs;
        this.throttleRelaxEveryMs = other.throttleRelaxEveryMs;
        this.delayConsumptionForMs = other.delayConsumptionForMs;
        this.throttleMaxConcurrent = (maxConcurrentAllowed < other.getThrottleMaxConcurrent()) ? maxConcurrentAllowed : 
            other.getThrottleMaxConcurrent();
    }

    /**
     * Get the period to throttle delivery for in milliseconds.
     * @return the period to throttle delivery of messages.
     */
    public long getThrottleDeliveryForPeriodMs()
    {
        long throttlePeriod = generateThrottleDeliveryForPeriodMs();
        long throttleRelaxInterval = getThrottleRelaxIntervalMs();
        return throttlePeriod < throttleRelaxInterval ? throttleRelaxInterval : throttlePeriod;
    }
    
    private long generateThrottleDeliveryForPeriodMs()
    {
        return throttleDeliveryForMs == null ? THROTTLE_DELIVERY_PERIOD_10_MINS : throttleDeliveryForMs;
    }

    /**
     * Get the interval between runs of the relaxer, which lessens the throttling before bringing the concurrent consumption
     * back to the original maximum before throttling began.
     * @return the interval between runs in milliseconds.
     */
    public long getThrottleRelaxIntervalMs()
    {
        return throttleRelaxEveryMs == null ? THROTTLE_RELAX_MIN_INTERVAL : throttleRelaxEveryMs;
    }

    /**
     * Get the number of max concurrent consumers to use when throttling.  This is the value used for the  initial phase of
     * throttling.
     * @return the number of max concurrent consumers to use during throttling.
     */
    public int getThrottleMaxConcurrent()
    {
        return throttleMaxConcurrent == null ? THROTTLE_MIN_CONCURRENT_COUNT : throttleMaxConcurrent;
    }
    
    /**
     * Identify how long to delay consumption by.  Consumption of any messages by the DMLC will not take place until this
     * interval has elapsed in milliseconds.
     * @return the interval before restarting consumption in milliseconds.
     */
    public Long getDelayConsumptionForMs()
    {
        return delayConsumptionForMs;
    }
    
    /**
     * Are any of the throttling parameters set.  If not then throttling will be skipped but it is still possible for the
     * delay consumption value to be set.
     * @return {@code true} if throttling is enabled, otherwise {@code false}.
     */
    public boolean isThrottled()
    {
        return throttleDeliveryForMs != null || throttleMaxConcurrent != null || throttleRelaxEveryMs != null;
    }
    
    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }
        else if (!(object instanceof PauseConfig))
        {
            return false;
        }
        PauseConfig other = (PauseConfig) object;
        
        return Objects.equal(getDelayConsumptionForMs(), other.getDelayConsumptionForMs()) &&
                Objects.equal(getThrottleDeliveryForPeriodMs(), other.getThrottleDeliveryForPeriodMs()) &&
                Objects.equal(getThrottleMaxConcurrent(), other.getThrottleMaxConcurrent()) &&
                Objects.equal(getThrottleRelaxIntervalMs(), other.getThrottleRelaxIntervalMs());
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hashCode(getDelayConsumptionForMs(), getThrottleDeliveryForPeriodMs(), getThrottleMaxConcurrent(),
            getThrottleRelaxIntervalMs());
    }
    
    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).add("throttleDeliveryForMs", throttleDeliveryForMs)
                .add("throttleRelaxEveryMs", throttleRelaxEveryMs).add("throttleMaxConcurrent", throttleMaxConcurrent)
                .add("delayConsumptionForMs", delayConsumptionForMs).toString();
    }
}
