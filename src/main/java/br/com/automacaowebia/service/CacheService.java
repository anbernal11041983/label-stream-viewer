package br.com.automacaowebia.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheService {

    private static final Map<String, Object> cache = new ConcurrentHashMap<>();

    public static void put(String key, Object value) {
        cache.put(key, value);
    }

    public static Object get(String key) {
        return cache.get(key);
    }

    public static boolean contains(String key) {
        return cache.containsKey(key);
    }

    public static void remove(String key) {
        cache.remove(key);
    }

    public static void clear() {
        cache.clear();
    }
}
