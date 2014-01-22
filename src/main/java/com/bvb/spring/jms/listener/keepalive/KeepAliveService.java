package com.bvb.spring.jms.listener.keepalive;

import com.bvb.spring.jms.listener.BackoffDefaultMessageListeningContainer;

/**
 * Clients implement this service in order to provide keep alive functionality to the DMLC.  They take any action they need
 * to on a keep alive, in order to ascertain whether consumption of messages should continue or not.  They then return the
 * appropriate response that the {@link BackoffDefaultMessageListeningContainer} takes action based on.
 */
public interface KeepAliveService
{
    /**
     * The keep alive interval has expired and the client has been called to either keep the connection alive, test if it is
     * alive, or take no action as required.
     * @return the response of the keep alive test and what the DMLC should do.  On a successful response the PauseConfig
     * should include how long to delay the next keep alive test.
     */
    KeepAliveResponse keepAlive();
}
