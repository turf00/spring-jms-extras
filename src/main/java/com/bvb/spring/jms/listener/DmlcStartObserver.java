package com.bvb.spring.jms.listener;

import com.bvb.spring.jms.listener.config.PauseConfig;

/**
 * Observers interested in the status of a BackoffDmlc can register to receive notifications on status changes.
 */
public interface DmlcStartObserver
{
    /**
     * Received when the BackoffDmlc is running and ready to consume messages.
     */
    void running();
    /**
     * Received when the BackoffDmlc has stopped with no PauseConfiguration.
     */
    void stopped();
    /**
     * Received when the BackoffDmlc has stopped with a config.
     * @param config the configuration received on how long before retry, etc.
     */
    void stopped(PauseConfig config);
}
