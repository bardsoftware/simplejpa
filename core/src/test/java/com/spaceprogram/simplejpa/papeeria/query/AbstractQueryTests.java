package com.spaceprogram.simplejpa.papeeria.query;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;
import com.spaceprogram.simplejpa.EntityManagerSimpleJPA;
import com.spaceprogram.simplejpa.papeeria.models.PrimitiveBox;
import junit.framework.TestCase;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gkalabin@bardsoftware.com
 */
public class AbstractQueryTests extends TestCase {
  public static final String PERSISTENCE_UNIT_NAME = "papeeriatestunit";
  private EntityManagerFactoryImpl myEntityManagerFactory;

  private static final List<Class<?>> CLASSES = new ArrayList<Class<?>>();

  static {
    CLASSES.add(PrimitiveBox.class);
  }


  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myEntityManagerFactory = new EntityManagerFactoryImpl(PERSISTENCE_UNIT_NAME, null, null, getStringClassNames());

    EntityManager em = myEntityManagerFactory.createEntityManager();
    {
      // create initial structure
      PrimitiveBox obj = new PrimitiveBox("foo", 42, 115L, .5d);
      PrimitiveBox obj2 = new PrimitiveBox("bar", 24, 511L, 5.5d);
      em.persist(obj);
      em.persist(obj2);
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

  public void testQueryWithIntParam() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
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
    EntityManager em = myEntityManagerFactory.createEntityManager();
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
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("SELECT b FROM PrimitiveBox b WHERE b.double=:double");
    query.setParameter("double", .5d);
    List<PrimitiveBox> resultList = query.getResultList();
    assertEquals(1, resultList.size());
    assertEquals(42, resultList.get(0).getInt());
    assertEquals(115L, resultList.get(0).getLong());
    assertEquals(.5d, resultList.get(0).getDouble());
    em.close();
  }

  private Set<String> getStringClassNames() {
    Set<String> classNames = new HashSet<String>(CLASSES.size());
    for (Class aClass : CLASSES) {
      classNames.add(aClass.getName());
    }
    return classNames;
  }
}
