package com.bvb.spring.jms.listener.keepalive;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Handles the scheduling of the keep alive service and stopping/starting the keep alive.
 */
public class KeepAliveManager
{
    protected final Log logger = LogFactory.getLog(getClass());
    
    private long keepAliveIntervalMs = 30000;
    private ThreadPoolTaskScheduler scheduler;
    private ScheduledFuture<?> taskKeepAlive;
    private Runnable runnableOnKeepAlive;
    private boolean started = false;
    
    public KeepAliveManager(ThreadPoolTaskScheduler scheduler, Runnable runnableOnKeepAlive, long keepAliveIntervalMs)
    {
        this.scheduler = scheduler;
        this.runnableOnKeepAlive = runnableOnKeepAlive;
        this.keepAliveIntervalMs = keepAliveIntervalMs;
    }
    
    /**
     * Reschedule the keep alive to run and if the current interval is already the same, reschedule to start from the time
     * the request is received.  The first run based on the rescheduling does not take place until now + the interval.
     * @param keepAliveIntervalMs the interval to run the keep alive service.
     */
    public synchronized void rescheduleAlways(Long keepAliveIntervalMs)
    {
        if (keepAliveIntervalMs != null)
        {
            reschedule(keepAliveIntervalMs, true);
        }
    }
    
    /**
     * Reschedule the keep alive but only if the new value is different from the old value.  If the value is different then
     * the first run based on the rescheduling does not take place until the now + the interval.
     * @param keepAliveIntervalMs the interval to run the keep alive service.
     */
    public synchronized void rescheduleIfDifferent(Long keepAliveIntervalMs)
    {
        if (keepAliveIntervalMs != null)
        {
            reschedule(keepAliveIntervalMs, false);
        }
    }
    
    public long getKeepAliveInterval()
    {
        return keepAliveIntervalMs;
    }
    
    private void reschedule(long keepAliveIntervalMs, boolean forceReschedule)
    {
        if (!started || keepAliveIntervalMs < 1)
        {
            logger.debug(String.format("Not rescheduling keep alive, started: [%s], keepAliveIntervalMs: [%d]", started,
                keepAliveIntervalMs));
            return;
        }
        else if (forceReschedule || keepAliveIntervalMs != this.keepAliveIntervalMs)
        {
            this.keepAliveIntervalMs = keepAliveIntervalMs;
            schedule(false);
        }
    }
    
    private void schedule(boolean initial)
    {
        cancel();
        if (initial)
        {
            logger.info(String.format("Rescheduling keep alive for immediate run then: [%d ms]", keepAliveIntervalMs));
            taskKeepAlive = scheduler.scheduleWithFixedDelay(runnableOnKeepAlive, keepAliveIntervalMs);
        }
        else
        {
            logger.info(String.format("Rescheduling keep alive for run in: [%d ms]", keepAliveIntervalMs));
            taskKeepAlive = scheduler.scheduleWithFixedDelay(runnableOnKeepAlive, getDatePlusInterval(), keepAliveIntervalMs);
        }
    }
    
    private Date getDatePlusInterval()
    {
        Date date = new Date();
        return new Date(date.getTime() + keepAliveIntervalMs);
    }
    
    private void cancel()
    {
        if (taskKeepAlive != null)
        {
            taskKeepAlive.cancel(false);
            taskKeepAlive = null;
        }
    }
    
    public synchronized void stop()
    {
        logger.debug("KeepAlive Stop called");
        started = false;
        cancel();
    }
    
    public synchronized void start()
    {
        logger.debug("KeepAlive Start called");
        // if not started, run keep alive immediately
        schedule(!started);
        started = true;
    }
}
