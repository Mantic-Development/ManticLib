package de.exlll.configlib;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Internal serialization map. Comments are metadata, never configuration entries. */
public final class CommentedMap extends LinkedHashMap<String, Object> {
    private final Map<String, List<String>> fieldComments = new LinkedHashMap<>();

    public Map<String, List<String>> getFieldComments() {
        return fieldComments;
    }
}
