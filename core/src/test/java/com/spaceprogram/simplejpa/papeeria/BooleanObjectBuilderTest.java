package com.spaceprogram.simplejpa.papeeria;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;
import com.spaceprogram.simplejpa.EntityManagerSimpleJPA;
import com.spaceprogram.simplejpa.papeeria.models.BooleanBox;
import junit.framework.TestCase;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gkalabin@papeeria.com
 */
public class BooleanObjectBuilderTest extends TestCase {
  public static final String PERSISTENCE_UNIT_NAME = "papeeriatestunit";
  private EntityManagerFactoryImpl myEntityManagerFactory;

  private static final List<Class<?>> CLASSES = new ArrayList<Class<?>>();

  static {
    CLASSES.add(BooleanBox.class);
  }


  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myEntityManagerFactory = new EntityManagerFactoryImpl(PERSISTENCE_UNIT_NAME, null, null, getStringClassNames());

    EntityManager em = myEntityManagerFactory.createEntityManager();
    {
      // create initial structure
      em.persist(new BooleanBox("t1", true));
      em.persist(new BooleanBox("t2", true));
      em.close();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    EntityManagerSimpleJPA em = (EntityManagerSimpleJPA) myEntityManagerFactory.createEntityManager();
    for (Class<?> aClass : CLASSES) {
      AmazonSimpleDB db = em.getSimpleDb();
      String domainName = em.getDomainName(aClass);

      System.out.println("deleting domain: " + domainName);
      DeleteDomainRequest deleteDomainRequest = new DeleteDomainRequest(domainName);
      db.deleteDomain(deleteDomainRequest);
    }
    em.close();
  }

  public void testSameObjects() {
    BooleanBox t1, anotherT1;
    {
      EntityManager em = myEntityManagerFactory.createEntityManager();
      t1 = em.find(BooleanBox.class, "t1");
      em.close();
    }
    {
      EntityManager em = myEntityManagerFactory.createEntityManager();
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
      EntityManager em = myEntityManagerFactory.createEntityManager();
      t1 = em.find(BooleanBox.class, "t1");
      em.close();
    }
    {
      EntityManager em = myEntityManagerFactory.createEntityManager();
      t2 = em.find(BooleanBox.class, "t2");
      em.close();
    }
    assertTrue(t1.getBooleanObject());
    assertTrue(t2.getBooleanObject());
    assertTrue(t1.getBooleanObject() == t2.getBooleanObject());
  }

  private Set<String> getStringClassNames() {
    Set<String> classNames = new HashSet<String>(CLASSES.size());
    for (Class aClass : CLASSES) {
      classNames.add(aClass.getName());
    }
    return classNames;
  }
}
