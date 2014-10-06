package com.spaceprogram.simplejpa.papeeria.query;

import com.spaceprogram.simplejpa.papeeria.BasePapeeriaTest;
import com.spaceprogram.simplejpa.papeeria.models.PrimitiveBox;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * @author gkalabin@papeeria.com
 */
public class AbstractQueryTests extends BasePapeeriaTest {
  static {
    ourTestClasses.add(PrimitiveBox.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    EntityManager em = getEntityManager();
    // create initial structure
    PrimitiveBox obj = new PrimitiveBox("foo", 42, 115L, .5d);
    PrimitiveBox obj2 = new PrimitiveBox("bar", 24, 511L, 5.5d);
    em.persist(obj);
    em.persist(obj2);
    em.close();
  }

  public void testQueryWithIntParam() {
    EntityManager em = getEntityManager();
    Query query = em.createQuery("SELECT b FROM PrimitiveBox b WHERE b.int=:int");
    query.setParameter("int", 42);
    List<PrimitiveBox> resultList = query.getResultList();
    assertEquals(1, resultList.size());
    assertEquals(42, resultList.get(0).getInt());
    assertEquals(115L, resultList.get(0).getLong());
    assertEquals(.5d, resultList.get(0).getDouble());
    em.close();
  }

  public void testQueryWithLongParam() {
    EntityManager em = getEntityManager();
    Query query = em.createQuery("SELECT b FROM PrimitiveBox b WHERE b.long<>:long");
    query.setParameter("long", 511L);
    List<PrimitiveBox> resultList = query.getResultList();
    assertEquals(1, resultList.size());
    assertEquals(42, resultList.get(0).getInt());
    assertEquals(115L, resultList.get(0).getLong());
    assertEquals(.5d, resultList.get(0).getDouble());
    em.close();
  }

  public void testQueryWithDoubleParam() {
    EntityManager em = getEntityManager();
    Query query = em.createQuery("SELECT b FROM PrimitiveBox b WHERE b.double=:double");
    query.setParameter("double", .5d);
    List<PrimitiveBox> resultList = query.getResultList();
    assertEquals(1, resultList.size());
    assertEquals(42, resultList.get(0).getInt());
    assertEquals(115L, resultList.get(0).getLong());
    assertEquals(.5d, resultList.get(0).getDouble());
    em.close();
  }
}
