package com.spaceprogram.simplejpa.papeeria.cache;

import com.spaceprogram.simplejpa.cache.Cache;
import com.spaceprogram.simplejpa.cache.CacheFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gkalabin@bardsoftware.com
 */
public class TestCacheFactory implements CacheFactory {
    public static Map<Class, Cache> cacheMap;

    @Override
    public void init(Map properties) {
        System.out.println("[TestCacheFactory] init");
        cacheMap = new HashMap<>();
    }

    @Override
    public Cache createCache(Class name) {
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

        private TestCache(Class aClass) {
            map = new HashMap<>();
            cacheName = aClass.getName();
        }

        @Override
        public Object get(Object o) {
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
    }
}
