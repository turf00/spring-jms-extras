package com.bvb.spring.jms.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bvb.spring.jms.listener.keepalive.KeepAliveResponse;
import com.bvb.spring.jms.listener.keepalive.KeepAliveService;

public class KeepAliveMutable extends AbstractKeepAlive implements KeepAliveService
{
    private volatile KeepAliveResponse response;
    
    public KeepAliveMutable()
    {
        response = new KeepAliveResponse(true);
    }
    
    public KeepAliveMutable(KeepAliveResponse response)
    {
        this.response = response;
    }

    @Override
    public KeepAliveResponse keepAlive()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        log("Keep alive called: [" + Thread.currentThread().getName() + "]" + formatter.format(new Date()));
        return response;
    }
    
    public void setResponse(KeepAliveResponse response)
    {
        this.response = response;
    }

}
