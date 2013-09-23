package com.bvb.spring.jms.listener.utils;

import java.util.Date;

public class DateUtils
{
    private DateUtils()
    {
        /* No construction */
    }
    
    public static Date getNowPlusMs(long ms)
    {
        Date date = new Date();
        return new Date(date.getTime() + ms);
    }
}
