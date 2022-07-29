package me.fullpage.manticlib.string;

import java.util.*;
import java.util.stream.Collectors;

public class Txt {

    public static boolean isNullOrEmpty(String string) {
        return ManticString.isNullOrEmpty(string);
    }

    public static boolean isNullOrEmpty(Object[] objects) {
        return objects == null || objects.length == 0;
    }

    public static boolean isNullOrEmpty(List<?> objects) {
        return objects == null || objects.isEmpty();
    }

    @SafeVarargs
    public static <T> List<T> list(T... items) {
        List<T> temp = new ArrayList<>(items.length);
        Collections.addAll(temp, items);
        return temp;
    }


    @SafeVarargs
    public static <T> Set<T> set(T... items) {
        Set<T> temp = new HashSet<>(items.length);
        Collections.addAll(temp, items);
        return temp;
    }

    public static String parse(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return new ManticString(str).colourise();
    }

    public static String parse(String str, Object... args) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return new ManticString(str).format(args).colourise();
    }

    public static List<String> parse(List<String> str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return str.stream().map(s -> new ManticString(s).colourise()).collect(Collectors.toList());
    }

    public static List<String> parse(List<String> str, Object... args) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return str.stream().map(s -> new ManticString(s).format(args).colourise()).collect(Collectors.toList());
    }

    public static String[] parse(String[] str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return Arrays.stream(str).map(s -> new ManticString(s).colourise()).toArray(String[]::new);
    }

    public static String[] parse(String[] str, Object... args) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return Arrays.stream(str).map(s -> new ManticString(s).format(args).colourise()).toArray(String[]::new);
    }

}
