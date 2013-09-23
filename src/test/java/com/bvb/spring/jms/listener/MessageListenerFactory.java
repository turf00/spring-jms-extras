package com.bvb.spring.jms.listener;

import org.springframework.jms.listener.adapter.MessageListenerAdapter;

public class MessageListenerFactory
{
    public static MessageListenerAdapter build(Object delegate, String listenerMethod, String responseDestination)
    {
        MessageListenerAdapter adapter = new MessageListenerAdapter();
        adapter.setDelegate(delegate);
        adapter.setDefaultListenerMethod(listenerMethod);
        adapter.setDefaultResponseQueueName(responseDestination);
        return adapter;
    }
}
