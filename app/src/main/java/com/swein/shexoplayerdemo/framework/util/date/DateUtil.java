package com.swein.shexoplayerdemo.framework.util.date;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DateUtil
{

    public static String getDateFromMilliSeconds(long milliSeconds)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        String s = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));

        return s;
    }

}
