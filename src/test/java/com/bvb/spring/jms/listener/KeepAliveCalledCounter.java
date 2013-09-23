package com.bvb.spring.jms.listener;

import com.bvb.spring.jms.listener.keepalive.KeepAliveResponse;
import com.bvb.spring.jms.listener.keepalive.KeepAliveService;

public class KeepAliveCalledCounter extends AbstractKeepAlive implements KeepAliveService
{
    private volatile int count;

    @Override
    public KeepAliveResponse keepAlive()
    {
        count++;
        log("Keep alive called, count=" + count);
        return new KeepAliveResponse(true);
    }
    
    public int getCount()
    {
        return count;
    }
    
    
}
