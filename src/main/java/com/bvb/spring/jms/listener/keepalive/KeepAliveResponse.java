package com.bvb.spring.jms.listener.keepalive;

import com.bvb.spring.jms.listener.config.PauseConfig;
import com.google.common.base.Objects;

/**
 * Implementors of the {@link KeepAliveService} return this whenever they are called to indicate whether the service
 * is alive or whether it has failed and no further messages should be consumed.
 */
public final class KeepAliveResponse
{
    
    private final boolean success;
    private final PauseConfig pauseConfig;
    
    /**
     * Create a keep alive providing success/failure and the pause config.
     * @param success {@code true} if the keep alive succeeded, otherwise {@code false}.
     * @param pauseConfig the config to use to pause the DMLC.
     */
    public KeepAliveResponse(boolean success, PauseConfig pauseConfig)
    {
        this.success = success;
        this.pauseConfig = pauseConfig;
    }
    
    /**
     * Create a keep alive response passing only success or failure.
     * @param success {@code true} if the keep alive succeeded, otherwise {@code false}.
     */
    public KeepAliveResponse(boolean success)
    {
        this(success, new PauseConfig());
    }

    /**
     * Return whether this response indicates success or failure.
     * @return {@code true} if the keep alive succeeded, otherwise {@code false}.
     */
    public boolean isSuccess()
    {
        return success;
    }
    
    public PauseConfig getPauseConfig()
    {
        return pauseConfig;
    }
    
    @Override
    public String toString()
    {
        return Objects.toStringHelper(this).add("success", success).add("pauseConfig", pauseConfig).toString();
    }
    
}
