package com.spaceprogram.simplejpa.papeeria;

import com.spaceprogram.simplejpa.papeeria.models.BooleanBox;

import javax.persistence.EntityManager;

/**
 * @author gkalabin@papeeria.com
 */
public class BooleanObjectBuilderTest extends BasePapeeriaTest {
  static {
    ourTestClasses.add(BooleanBox.class);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    EntityManager em = getEntityManager();
    {
      // create initial structure
      em.persist(new BooleanBox("t1", true));
      em.persist(new BooleanBox("t2", true));
      em.close();
    }
  }

  public void testSameObjects() {
    BooleanBox t1, anotherT1;
    {
      EntityManager em = getEntityManager();
      t1 = em.find(BooleanBox.class, "t1");
      em.close();
    }
    {
      EntityManager em = getEntityManager();
      anotherT1 = em.find(BooleanBox.class, "t1");
      em.close();
    }
    assertTrue(t1.getBooleanObject());
    assertTrue(anotherT1.getBooleanObject());
    assertTrue(t1.getBooleanObject() == anotherT1.getBooleanObject());
  }

  public void testSameObjectFields() {
    BooleanBox t1, t2;
    {
      EntityManager em = getEntityManager();
      t1 = em.find(BooleanBox.class, "t1");
      em.close();
    }
    {
      EntityManager em = getEntityManager();
      t2 = em.find(BooleanBox.class, "t2");
      em.close();
    }
    assertTrue(t1.getBooleanObject());
    assertTrue(t2.getBooleanObject());
    assertTrue(t1.getBooleanObject() == t2.getBooleanObject());
  }
}
