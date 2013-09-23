package com.bvb.spring.jms.listener.activemq;

import java.io.Serializable;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.DestinationStatistics;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;

public class Broker
{
    private static final String URL = "vm://localhost";
    
    private BrokerService broker;
    private String dataDirectory;
    
    public void start()
    {
        broker = new BrokerService();
        try
        {
            broker.addConnector(URL + "?jms.prefetchPolicy.all=0");
            if (dataDirectory != null)
            {
                broker.setDataDirectory(dataDirectory);
            }
            broker.start();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public void setDataDirectory(String dataDirectory)
    {
        this.dataDirectory = dataDirectory;
    }
    
    public String getConnectionUrl()
    {
        return URL;
    }
    
    public long getMessagePendingCount(String queue)
    {
        DestinationStatistics stats = getStats(queue);
        return stats.getEnqueues().getCount() - stats.getDequeues().getCount();
    }

    private DestinationStatistics getStats(String queue)
    {
        org.apache.activemq.broker.region.Destination destination;
        try
        {
            destination = broker.getDestination(new ActiveMQQueue(queue));
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        DestinationStatistics stats = destination.getDestinationStatistics();
        return stats;
    }
    
    public long getConsumersCount(String queue)
    {
        DestinationStatistics stats = getStats(queue);
        return stats.getConsumers().getCount();
    }
    
    public void stop()
    {
        if (broker != null)
        {
            try
            {
                broker.stop();
            }
            catch (Exception ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
    
    public void clearAllMessages()
    {
        try
        {
            Map<ActiveMQDestination, org.apache.activemq.broker.region.Destination> destinationMap = 
                    broker.getBroker().getDestinationMap();
            for (org.apache.activemq.broker.region.Destination destination : destinationMap.values())
            {
                if (destination instanceof org.apache.activemq.broker.region.Queue)
                {
                    ((org.apache.activemq.broker.region.Queue)destination).purge();
                }
            }
            broker.deleteAllMessages();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public void addMessages(String queue, Serializable... objects)
    {
        try
        {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(getConnectionUrl());
    
            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();
    
            // Create a Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue(queue);
    
            // Create a MessageProducer from the Session to the Topic or Queue
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
    
            for (Serializable object : objects)
            {
                doSendMessage(session, producer, object);
            }
    
            // Clean up
            session.close();
            connection.close();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    private void doSendMessage(Session session, MessageProducer producer, Serializable object)
    {
        try
        {
            ObjectMessage message = session.createObjectMessage(object);
            producer.send(message);
        }
        catch (JMSException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
}
