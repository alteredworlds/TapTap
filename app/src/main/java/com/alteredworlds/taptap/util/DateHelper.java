package com.alteredworlds.taptap.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by twcgilbert on 14/12/2015.
 */
public class DateHelper {
    public static final String AW_UTC_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String formattedDateUTC(Date date) {
        if (null == date) {
            return "";
        } else {
            final SimpleDateFormat sdf = new SimpleDateFormat(AW_UTC_DATE_TIME_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.format(date);
        }
    }
}
