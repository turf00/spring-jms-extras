package com.bvb.spring.jms.listener;

import javax.jms.ConnectionFactory;

public class DmlcFactory
{

    private ConnectionFactory connectionFactory;
    
    public DmlcFactory(ConnectionFactory connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }
    
    public BackoffDefaultMessageListeningContainer build(int startConcurrent, int endConcurrent, Object listener, 
            String destination, boolean initiallyStopped)
    {
        BackoffDefaultMessageListeningContainer result = new BackoffDefaultMessageListeningContainer();
        result.setConnectionFactory(connectionFactory);
        result.setSessionTransacted(true);
        result.setCacheLevelName("CACHE_CONNECTION");
        result.setInitiallyStopped(initiallyStopped);
        result.setBeanName("Dmlc");
        if (startConcurrent == endConcurrent)
        {
            result.setConcurrentConsumers(endConcurrent);
        }
        else
        {
            result.setConcurrency(String.format("%d-%d", startConcurrent, endConcurrent));
        }
        result.setMessageListener(listener);
        result.setDestinationName(destination + "?consumer.prefetchSize=0");
        return result;
    }
}
