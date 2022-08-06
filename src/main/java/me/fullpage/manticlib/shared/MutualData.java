package me.fullpage.manticlib.shared;

import java.util.HashMap;

public class MutualData {

    private static MutualData i;

    private static MutualData accessInstance() {
        if (i == null) {
            i = new MutualData();
        }
        return i;
    }

    public static void put(String key, Object value) {
        accessInstance().map.put(key, value);
    }

    public static void remove(String key) {
        accessInstance().map.remove(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) accessInstance().map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T def) {
        Object o = accessInstance().map.get(key);
        return o == null ? def : (T) o;
    }

    private final HashMap<String, Object> map;

    // Prevent instantiation externally
    private MutualData() {
        map = new HashMap<>();
    }


}
