package me.fullpage.manticlib.utils;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class Utils {


    public static boolean isNullOrEmpty(String value) {
        return (value == null) || (value.trim().length() == 0);
    }

    public static boolean isNullOrEmpty(Object value) {
        if (value instanceof Collection) {
            return isNullOrEmpty((Collection<?>) value);
        }
        return value == null;
    }

    public static <T> boolean isNullOrEmpty(Collection<T> collection) {
        return (collection == null) || (collection.isEmpty());
    }

    public static boolean isNullOrEmpty(Number number) {
        return (number == null) || (!(number.doubleValue() > 0));
    }

    public static boolean isNullOrEmpty(Date data) {
        return data == null;
    }

    public static <T> boolean isNullOrEmpty(Map<T, T> map) {
        return (map == null) || (map.isEmpty());
    }

    public static boolean isNullOrEmpty(File file) {
        return isNull(file) || file.length() == 0;
    }

    public static boolean isNullOrEmpty(Object[] array) {
        return (array == null) || (array.length == 0);
    }

    public static boolean isNull(Object value) {

        return value == null;
    }

}
