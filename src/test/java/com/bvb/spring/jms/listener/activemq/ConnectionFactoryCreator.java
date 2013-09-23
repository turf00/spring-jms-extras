package com.bvb.spring.jms.listener.activemq;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.connection.CachingConnectionFactory;

public class ConnectionFactoryCreator
{
    public static ConnectionFactory build(String url)
    {
        ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory(url);
        CachingConnectionFactory cachingFactory = new CachingConnectionFactory();
        cachingFactory.setTargetConnectionFactory(amqFactory);
        cachingFactory.setSessionCacheSize(50);
        return cachingFactory;
    }
}
