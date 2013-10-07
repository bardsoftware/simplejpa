package com.spaceprogram.simplejpa.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * User: treeder Date: Aug 2, 2009 Time: 9:44:53 PM
 */
public class EhcacheWrapper implements com.spaceprogram.simplejpa.cache.Cache {
  private Cache cache;

  public EhcacheWrapper(Cache cache) {
    this.cache = cache;
  }

  public void bootstrap() {
    cache.bootstrap();
  }

  public Object get(Object o) {
    Element elem = cache.get(o);
    return elem == null ? null : elem.getObjectValue();
  }

  public void put(Element element) {
    cache.put(element);
  }

  public void put(Object o, Object o1) {
    cache.put(new Element(o, o1));
  }

  public boolean remove(Object o) {
    return cache.remove(o);
  }

  public int hashCode() {
    return cache.hashCode();
  }

  public boolean equals(Object o) {
    return cache.equals(o);
  }

  public String toString() {
    return cache.toString();
  }
}
