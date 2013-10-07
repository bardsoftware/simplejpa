package com.spaceprogram.simplejpa.cache;

/**
 * User: treeder
 * Date: Jun 7, 2008
 * Time: 4:47:01 PM
 */
public class NoopCache implements Cache {
    public Object get(Object o) {
        return null;
    }

    public void put(Object o, Object o1) {

    }

    public boolean remove(Object o) {
        return true;
    }
}
