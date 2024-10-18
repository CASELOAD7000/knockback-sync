package me.caseload.knockbacksync;

import me.caseload.knockbacksync.util.NumberConversions;

import java.util.HashMap;
import java.util.Map;

public class ConfigWrapper {
    private final Map<String, Object> configMap;

    public ConfigWrapper(Map<String, Object> configMap) {
        this.configMap = configMap;
    }

    public String getString(String path, String def) {
        Object value = getValue(path);
        return value instanceof String ? (String) value : def;
    }

    public boolean getBoolean(String path, boolean def) {
        Object value = getValue(path);
        return value instanceof Boolean ? (Boolean) value : def;
    }

    public int getInt(String path, int def) {
        Object value = getValue(path);
        return NumberConversions.toInt(value, def);
    }

    public long getLong(String path, long def) {
        Object value = getValue(path);
        return NumberConversions.toLong(value, def);
    }

    public void set(String path, Object value) {
        setValue(path, value);
    }

    private Object getValue(String path) {
        String[] parts = path.split("\\.");
        Object current = configMap;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null; // Path not found
            }
        }
        return current;
    }

    private void setValue(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = configMap;
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.containsKey(part) || !(current.get(part) instanceof Map)) {
                current.put(part, new HashMap<>());
            }
            current = (Map<String, Object>) current.get(part);
        }
        current.put(parts[parts.length - 1], value);
    }
}