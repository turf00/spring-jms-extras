package com.bvb.spring.jms.listener.throttler;

import com.bvb.spring.jms.listener.config.PauseConfig;

public class FixedRateThrottlerCounter
{
    private final int endConsumerCount;
    private final int growByEachRun;
    private int current;
    
    public FixedRateThrottlerCounter(int starting, int target, int numberOfRuns)
    {
        int diff = target - starting;
        growByEachRun = (int) Math.ceil((double) diff / numberOfRuns);
        this.endConsumerCount = target;
        this.current = starting;
    }
    
    public FixedRateThrottlerCounter(int target, PauseConfig config)
    {
        this(config.getThrottleMaxConcurrent(), target, 
            (int) (config.getThrottleDeliveryForPeriodMs()  / config.getThrottleRelaxIntervalMs()));
    }

    public int incrementAndGet()
    {
        current += growByEachRun;
        // on the last run we may not have an exact number to increase by
        if (current > endConsumerCount)
        {
            current = endConsumerCount;
        }
        return current;
    }
    
    public boolean isDone()
    {
        return current == endConsumerCount;
    }
    
}
