package com.alteredworlds.taptap.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by twcgilbert on 14/12/2015.
 */
public class DateHelper {
    public static final String AW_UTC_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String formattedDateLocal(Date date) {
        if (null == date) {
            return "";
        } else {
            // formats date using supplied format string
            // expressed in system default Locale, TimeZone
            final SimpleDateFormat sdf = new SimpleDateFormat(
                    AW_UTC_DATE_TIME_FORMAT,
                    Locale.getDefault());
            return sdf.format(date);
        }
    }

    public static Date getStartOfToday() {
        // start of today in UTC
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        return getStartOfDay(cal);
    }

    public static Date getStartOfYesterday() {
        // start of yesterday in UTC
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.DAY_OF_MONTH, -1);  // set to yesterday
        return getStartOfDay(cal);
    }

    public static Date getStartOfDay(GregorianCalendar cal) {
        // set to first millisecond of day
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 1);
        return cal.getTime();
    }
}
