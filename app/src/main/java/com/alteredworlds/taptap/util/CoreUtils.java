package com.alteredworlds.taptap.util;

import android.text.TextUtils;

/**
 * Created by twcgilbert on 09/12/2015.
 */
public class CoreUtils {
    @SuppressWarnings("unchecked")
    public static <T> T safeCast(Object obj, Class<T> type) {
        if (type.isInstance(obj)) {
            return (T) obj;
        }
        return null;
    }

    public static String emptyStringIfNull(String value) {
        return TextUtils.isEmpty(value) ? "" : value;
    }

    public static boolean stringEquals(String s1, String s2) {
        boolean retVal = s1 == s2; // reference equality or both null
        if (!retVal) {
            if (null != s1) {
                retVal = s1.equals(s2);
            }
        }
        return retVal;
    }
}
