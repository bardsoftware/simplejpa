package com.spaceprogram.simplejpa.cache;

/**
 * User: treeder
 * Date: Aug 2, 2009
 * Time: 7:07:41 PM
 */
public interface Cache {
    Object get(Object key);

    void put(Object key, Object value);

    boolean remove(Object key);
}
