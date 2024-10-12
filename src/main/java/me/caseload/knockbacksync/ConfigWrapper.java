package me.caseload.knockbacksync;

import java.util.Map;

public class ConfigWrapper {
    private Map<String, Object> configMap;

    public ConfigWrapper(Map<String, Object> configMap) {
        this.configMap = configMap;
    }

    public String getString(String path, String def) {
        Object value = configMap.get(path);
        return value instanceof String ? (String) value : def;
    }

    public boolean getBoolean(String path, boolean def) {
        Object value = configMap.get(path);
        return value instanceof Boolean ? (Boolean) value : def;
    }

    public int getInt(String path, int def) {
        Object value = configMap.get(path);
        return value instanceof Integer ? (Integer) value : def;
    }

    public long getLong(String path, long def) {
        Object value = configMap.get(path);
        return value instanceof Long ? (Long) value : def;
    }

    public void set(String path, Object value) {
        configMap.put(path, value);
    }

    // Add other methods as needed (getDouble, getList, etc.)
}