package com.bvb.spring.jms.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.ConnectionFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.listener.adapter.MessageListenerAdapter;

import com.bvb.spring.jms.listener.activemq.Broker;
import com.bvb.spring.jms.listener.activemq.ConnectionFactoryCreator;
import com.bvb.spring.jms.listener.config.PauseConfig;
import com.bvb.spring.jms.listener.config.PauseConfigBuilder;
import com.bvb.spring.jms.listener.exception.PauseConsumptionException;
import com.bvb.spring.jms.listener.keepalive.KeepAliveResponse;

/**
 * Test the {@link BackoffDefaultMessageListeningContainer} by using an embedded ActiveMQ broker.
 */
public class BackoffDefaultMessageListeningContainerTest implements MessageObserver
{
    private static final int DEFAULT_MAX_CONSUMERS = 10;
    private static final int DEFAULT_CONSUMERS = 5;
    private static final String QUEUE1 = "queue1";
    private static final String QUEUE2 = "queue2";
    
    private static Broker broker;
    private static String brokerUrl;
    private static MessageListenerAdapter listener;
    private static Set<String> messagesReceived;
    private static Service service;
    private static CountDownLatch latch;
    private static DmlcFactory dmlcFactory;
    
    private static BackoffDefaultMessageListeningContainer dmlc;
    
    @BeforeClass
    public static void setUpClass()
    {
        service = new Service();
        buildDefaultListener();
        
        broker = new Broker();
        broker.setDataDirectory("target/amq-data");
        broker.start();
        brokerUrl = broker.getConnectionUrl();
        
        ConnectionFactory connectionFactory = ConnectionFactoryCreator.build(brokerUrl);
        dmlcFactory = new DmlcFactory(connectionFactory);
                
        messagesReceived = Collections.synchronizedSet(new HashSet<String>());
        latch = new CountDownLatch(1);
    }
    
    @AfterClass
    public static void tearDownClass()
    {
        dmlc.stop();
        dmlc.shutdown();
        broker.stop();
        broker = null;
    }
    
    @Before
    public void setup()
    {
        messagesReceived.clear();
        
        service.clear();
        service.clearObservers();
        service.addObserver(this);
        
        dmlc = buildDefault();
    }
    
    @After
    public void tearDown()
    {
        dmlc.stop();
        dmlc.shutdown();
        waitFor(2);
        System.out.println("Pending: " + broker.getMessagePendingCount(QUEUE1));
        broker.clearAllMessages();
        System.out.println("Pending after: " + broker.getMessagePendingCount(QUEUE1));
    }
    
    @Test
    public void testGivenStartWithInitiallyNotConnectedAndKeepAliveFailingAssertNoMessagesConsumed() throws Exception
    {
        String id = "001", id2 = "002";
        addMessages(QUEUE1, id, id2);
        
        dmlc.setInitiallyStopped(true);
        dmlc.setKeepAliveInterval(1000);
        dmlc.setKeepAliveService(new KeepAliveMutable(new KeepAliveResponse(false)));
        
        launch(dmlc);
        
        waitOnLatch(5, 2);

        // Ensure that the 2 messages remain on the queue and are not consumed
        assertTrue(messagesReceived.isEmpty());
        assertEquals(2, broker.getMessagePendingCount(QUEUE1));
    }
    
    @Test
    public void testGivenStartConnectedAndKeepAliveSuccessAssertMessagesConsumed() throws Exception
    {
        System.out.println("Running last test!!!");
        String id = "001", id2 = "002";
        Set<String> expected = addMessages(QUEUE1, id, id2);
        
        dmlc.setInitiallyStopped(false);
        dmlc.setKeepAliveInterval(1000);
        dmlc.setKeepAliveService(new KeepAliveMutable());
        
        launch(dmlc);
        
        waitOnLatch(15, 2);
        // Ensure that the 2 messages arrived and are no longer on the queue
        assertEquals(expected, messagesReceived);
        assertEquals(0, broker.getMessagePendingCount(QUEUE1));
    }
    
    @Test
    public void testGivenKeepAliveThrottlingAssertMaxConsumersAreThrottled() throws Exception
    {
        dmlc.setInitiallyStopped(false);
        dmlc.setKeepAliveInterval(10000);
        PauseConfig config = PauseConfigBuilder.newBuilder().withThrottleDeliveryForXMinutes(2)
                .withThrottleMaxConcurrency(1).build();
        KeepAliveMutable keepAlive = new KeepAliveMutable();
        dmlc.setKeepAliveService(keepAlive);
        
        launch(dmlc);
        
        assertDefaultConsumers();
        
        keepAlive.setResponse(new KeepAliveResponse(false, config));
        
        waitFor(10);
        
        assertConsumers(1, 1);
    }
    
    @Test
    public void testGivenKeepAliveThrottlingIsRelaxedAfter1MinuteAssertThrottlingRelaxedByExpectedAmount()
    {
        dmlc.setInitiallyStopped(false);
        dmlc.setKeepAliveInterval(1000);
        PauseConfig config = PauseConfigBuilder.newBuilder().withThrottleDeliveryForXMinutes(1)
                .withThrottleRelaxEveryXSeconds(60).withThrottleMaxConcurrency(1).build();
        KeepAliveMutable keepAlive = new KeepAliveMutable();
        dmlc.setKeepAliveService(keepAlive);
        
        launch(dmlc);
        
        assertDefaultConsumers();
        
        // respond that throttling is necessary
        keepAlive.setResponse(new KeepAliveResponse(false, config));
        // add some messages to process
        addMessages(100);
        // pause
        waitFor(1);
        
        // the keep alive will now respond everything is working
        keepAlive.setResponse(new KeepAliveResponse(true));
        
        assertConsumers(1, 1);
        
        waitFor(70);
        
        assertEquals(DEFAULT_MAX_CONSUMERS, dmlc.getMaxConcurrentConsumers());
    }
    
    @Test
    public void testGivenThrottlingMaxLargerThanStandardMaxThrottlingIgnored()
    {
        dmlc.setInitiallyStopped(false);
        dmlc.setKeepAliveInterval(1000);
        dmlc.setConcurrency("1-2");
        KeepAliveMutable keepAlive = new KeepAliveMutable();
        dmlc.setKeepAliveService(keepAlive);
        
        PauseConfig config = PauseConfigBuilder.newBuilder().withThrottleDeliveryForXMinutes(1)
                .withThrottleRelaxEveryXSeconds(60).withThrottleMaxConcurrency(3).build();
        
        launch(dmlc);
        
        // respond that throttling is necessary
        keepAlive.setResponse(new KeepAliveResponse(false, config));
        
        waitFor(2);
        
        assertConsumers(1, 2);
    }
    
    @Test
    public void testGivenListenerExceptionWithDelayedRetryAssertRetryOccursWhenExpected()
    {
        // the listener should throw a single exception to delay consumption for 10 seconds.
        dmlc.setInitiallyStopped(false);
        dmlc.setKeepAliveInterval(1000);
        KeepAliveCalledCounter keepAlive = new KeepAliveCalledCounter();
        KeepAliveCalledCounter.setLogging(true);
        dmlc.setKeepAliveService(keepAlive);
        
        PauseConfig config = PauseConfigBuilder.newBuilder().withDelayConsumptionForXSeconds(15).build();
        
        service.addExceptionToThrow(new PauseConsumptionException(config, new RuntimeException("Error")));
        addMessages(1);
        launch(dmlc);
        while (dmlc.isRunning())
        {
            waitFor(1);
        }
        
        int countBefore = keepAlive.getCount();
        waitFor(20);
        
        int countAfter = keepAlive.getCount();
        assertEquals(1, countAfter - countBefore);
    }

    private void assertDefaultConsumers()
    {
        assertConsumers(DEFAULT_CONSUMERS, DEFAULT_MAX_CONSUMERS);
    }
    
    private void addMessages(int count)
    {
        String allMessages[] = new String[count];
        for (int i = 0; i < count; i++)
        {
            allMessages[i] = String.format("Message-%d", i);
        }
        addMessages(QUEUE1, allMessages);
    }
    
    private void assertConsumers(int consumers, int maxConsumers)
    {
        assertEquals(consumers, dmlc.getConcurrentConsumers());
        assertEquals(maxConsumers, dmlc.getMaxConcurrentConsumers());
    }
    
    private static void launch(DefaultMessageListenerContainer dmlc)
    {
        dmlc.afterPropertiesSet();
        dmlc.start();
    }
    
    private Set<String> addMessages(String queue, String ...messages)
    {
        int count = messages.length;
        broker.addMessages(queue, (Serializable[]) messages);
        assertEquals(count, broker.getMessagePendingCount(QUEUE1));

        Set<String> set = new HashSet<>();
        for (String s : messages)
        {
            set.add(s);
        }
            
        return set;
    }
    
    private void waitFor(long secondsToWait)
    {
        try
        {
            Thread.sleep(TimeUnit.SECONDS.toMillis(secondsToWait));
        }
        catch (InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    private boolean waitOnLatch(long secondsToWait, int count)
    {
        latch = new CountDownLatch(count);
        boolean result = false;
        try
        {
            latch.await(secondsToWait, TimeUnit.SECONDS);
            result = true;
            // give time for the Tx to complete
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            
        }
        return result;
    }

    @Override
    public void update(String message)
    {
        messagesReceived.add(message);
        latch.countDown();
    }
    
    private static void buildDefaultListener()
    {
        listener = buildListener(QUEUE1);
    }
    
    private static MessageListenerAdapter buildListener(String queue)
    {
        return MessageListenerFactory.build(service, "doSomething", queue);
    }
    
    private static BackoffDefaultMessageListeningContainer buildDefault()
    {
        return dmlcFactory.build(DEFAULT_CONSUMERS, DEFAULT_MAX_CONSUMERS, listener, QUEUE1, false);
    }
    
}
