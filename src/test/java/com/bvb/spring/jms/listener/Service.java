package com.bvb.spring.jms.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service that we use as a message listener, it does very little beyond update observers and throw an exception if required.
 *
 */
public class Service
{
    private Set<String> received = new HashSet<>();
    private List<MessageObserver> observers = new ArrayList<>();
    private List<RuntimeException> toThrow = Collections.synchronizedList(new ArrayList<RuntimeException>());
    
    public void doSomething(String message)
    {
        received.add(message);
        //System.out.println("Received: " + message);
        for (MessageObserver observer : observers)
        {
            observer.update(message);
        }
        try
        {
            RuntimeException ex = toThrow.remove(0);
            if (ex != null)
            {
                throw ex;
            }
        }
        catch (IndexOutOfBoundsException ex)
        {
            // ignore
        }
    }
    
    public void clearObservers()
    {
        observers.clear();
    }
    
    public void addObserver(MessageObserver observer)
    {
        observers.add(observer);
    }
    
    public void clear()
    {
        received.clear();
    }
    
    public void addExceptionToThrow(RuntimeException ex)
    {
        toThrow.add(ex);
    }
    
}
