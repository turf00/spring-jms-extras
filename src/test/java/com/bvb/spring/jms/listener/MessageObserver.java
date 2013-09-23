package com.bvb.spring.jms.listener;

public interface MessageObserver
{
    public void update(String message);
}
