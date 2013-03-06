package com.spaceprogram.simplejpa.papeeria.cache;

import com.spaceprogram.simplejpa.cache.Cache;
import com.spaceprogram.simplejpa.cache.CacheFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gkalabin@bardsoftware.com
 */
public class TestCacheFactory implements CacheFactory {
    public static Map<String, Cache> cacheMap;

    @Override
    public void init(Map properties) {
        System.out.println("[TestCacheFactory] init");
        cacheMap = new HashMap<String, Cache>();
    }

    @Override
    public Cache createCache(String name) {
        if (cacheMap.containsKey(name)) {
            System.out.println("[TestCacheFactory] get cache " + name);
            return cacheMap.get(name);
        }
        System.out.println("[TestCacheFactory] create cache " + name);
        TestCache cache = new TestCache(name);
        cacheMap.put(name, cache);
        return cache;
    }

    @Override
    public void shutdown() {
        System.out.println("[TestCacheFactory] shutdown");
        cacheMap.clear();
        cacheMap = null;
    }

    @Override
    public void clearAll() {
        System.out.println("[TestCacheFactory] clear all");
        cacheMap.clear();
    }

    private static class TestCache implements Cache {
        private final Map<Object, Object> map;
        private final String cacheName;

        private TestCache(String name) {
            map = new HashMap<Object, Object>();
            cacheName = name;
        }

        @Override
        public int size() {
            System.out.println("[TestCache for " + cacheName + "] get size");
            return map.size();
        }

        @Override
        public Object getObj(Object o) {
            System.out.println("[TestCache for " + cacheName + "] get " + o);
            return map.get(o);
        }

        @Override
        public void put(Object o, Object o1) {
            System.out.println("[TestCache for " + cacheName + "] put (k=" + o + "; v=" + o1 + ")");
            map.put(o, o1);
        }

        @Override
        public boolean remove(Object o) {
            System.out.println("[TestCache for " + cacheName + "] remove " + o);
            return map.remove(o) != null;
        }

        @Override
        public void clear() {
            System.out.println("[TestCache for " + cacheName + "] clear");
            map.clear();
        }
    }
}
