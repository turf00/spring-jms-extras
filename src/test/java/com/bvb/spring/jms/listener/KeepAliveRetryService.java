package com.bvb.spring.jms.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bvb.spring.jms.listener.keepalive.KeepAliveResponse;
import com.bvb.spring.jms.listener.keepalive.KeepAliveService;

public class KeepAliveRetryService extends AbstractKeepAlive implements KeepAliveService
{

    private int hit = 2;

    @Override
    public KeepAliveResponse keepAlive()
    {
        if (log)
        {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            log("Keep alive called: " + formatter.format(new Date()));
        }
        if (--hit <= 0)
        {
            log("KeepAlive response good");
            return new KeepAliveResponse(true);
        }
        else
        {
            log("Keep alive response bad");
            return null;
        }
    }

}
