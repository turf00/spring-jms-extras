package com.bvb.spring.jms.listener;

public abstract class AbstractKeepAlive
{
    protected static boolean log = false;
    
    public static void setLogging(boolean enabled)
    {
        log = enabled;
    }
    
    protected void log(String message)
    {
        if (log)
        {
            System.out.println(message);
        }
    }
}
