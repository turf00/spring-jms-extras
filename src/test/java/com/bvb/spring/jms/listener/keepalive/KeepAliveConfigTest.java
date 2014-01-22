package com.bvb.spring.jms.listener.keepalive;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.bvb.spring.jms.listener.config.PauseConfig;
import com.bvb.spring.jms.listener.config.PauseConfigBuilder;
import com.bvb.spring.jms.listener.keepalive.KeepAliveResponse;

public class KeepAliveConfigTest
{
    private static final long KEEP_ALIVE_FAILED = 10;
    private static final long KEEP_ALIVE_SUCCESS = 30;
    private static final long THROTTLE_DELIVERY_PERIOD = 60;
    private static final int THROTTLE_COUNT = 1;
    
    private KeepAliveConfig config;
    
    @Before
    public void setUp()
    {
        config = build();
    }
    
    @Test
    public void testGettersAndSetters()
    {
        KeepAliveConfig config = build();
        
        assertEquals(KEEP_ALIVE_FAILED, config.getKeepAliveIntervalSecFailed());
        assertEquals(KEEP_ALIVE_SUCCESS, config.getKeepAliveIntervalSecSuccess());
        assertEquals(THROTTLE_COUNT, config.getMaxConsumersThrottled());
        assertEquals(THROTTLE_DELIVERY_PERIOD, config.getThrottlingDeliveryPeriodSec());
    }
    
    @Test
    public void testGivenGetConfigThrottledForPeriodInMsAssertExpectedPauseConfigReturned()
    {
        long interval = KEEP_ALIVE_FAILED + 10;
        PauseConfig expected = PauseConfigBuilder.newBuilder().withDelayConsumptionForXSeconds(interval)
            .withThrottleDeliveryForXSeconds(THROTTLE_DELIVERY_PERIOD).withThrottleMaxConcurrency(1).build();
        PauseConfig found = config.getConfigThrottledForMs(secsToMs(interval));
        
        assertEquals(expected, found);
    }
    
    @Test
    public void testGivenConfigThrottledForPeriodInSecsAssertExpectedPauseConfigReturned()
    {
        long interval = KEEP_ALIVE_FAILED + 10;
        PauseConfig expected = PauseConfigBuilder.newBuilder().withDelayConsumptionForXSeconds(interval)
                .withThrottleDeliveryForXSeconds(THROTTLE_DELIVERY_PERIOD).withThrottleMaxConcurrency(1).build();
        PauseConfig found = config.getConfigThrottledForSecs(interval);
        
        assertEquals(expected, found);
    }
    
    @Test
    public void testGivenKeepAliveFailedGetAssertKeepAliveResponseAsExpected()
    {
        KeepAliveResponse found = config.getFailed();
        PauseConfig config = PauseConfigBuilder.newBuilder().withDelayConsumptionForXSeconds(KEEP_ALIVE_FAILED).build();
        KeepAliveResponse expected = new KeepAliveResponse(false, config);
        
        assertEquals(expected, found);
    }
    
    @Test
    public void testGivenKeepAliveSucceededGetAssertKeepAliveResponseAsExpected()
    {
        KeepAliveResponse found = config.getSuccess();
        PauseConfig config = PauseConfigBuilder.newBuilder().withDelayConsumptionForXSeconds(KEEP_ALIVE_SUCCESS).build();
        KeepAliveResponse expected = new KeepAliveResponse(true, config);
        
        assertEquals(expected, found);
    }
    
    @Test
    public void testGivenKeepAliveSuccessSetWithMsAssertKeepAliveResponseAsExpected()
    {
        KeepAliveConfig config = new KeepAliveConfig();
        config.setKeepAliveIntervalMsSuccess(TimeUnit.SECONDS.toMillis(KEEP_ALIVE_SUCCESS));
        
        assertEquals(KEEP_ALIVE_SUCCESS, config.getKeepAliveIntervalSecSuccess());
    }
    
    private static long secsToMs(long secs)
    {
        return TimeUnit.SECONDS.toMillis(secs);
    }

    private static KeepAliveConfig build()
    {
        KeepAliveConfig config = new KeepAliveConfig();
        config.setKeepAliveIntervalSecFailed(KEEP_ALIVE_FAILED);
        config.setKeepAliveIntervalSecSuccess(KEEP_ALIVE_SUCCESS);
        config.setMaxConsumersThrottled(THROTTLE_COUNT);
        config.setThrottlingDeliveryPeriodSec(THROTTLE_DELIVERY_PERIOD);
        return config;
    }
    
}
