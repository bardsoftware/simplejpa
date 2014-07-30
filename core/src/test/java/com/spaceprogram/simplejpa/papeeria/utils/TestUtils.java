package com.spaceprogram.simplejpa.papeeria.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author gkalabin@papeeria.com
 */
public class TestUtils {
  public static <T> void collectionEquals(Collection<T> expected, Collection<T> actual) {
    assertEquals(expected.size(), actual.size());
    for (T t : expected) {
      if (!actual.contains(t)) {
        fail(String.format("Actual doesn't contain %s", t));
      }
    }
  }

  public static <T> void collectionEquals(Collection<T> actual, T... expected) {
    List<T> expectedList = Arrays.asList(expected);
    collectionEquals(expectedList, actual);
  }
}
