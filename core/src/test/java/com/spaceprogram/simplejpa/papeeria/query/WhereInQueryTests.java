package com.spaceprogram.simplejpa.papeeria.query;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;
import com.spaceprogram.simplejpa.EntityManagerSimpleJPA;
import com.spaceprogram.simplejpa.papeeria.models.PlainObject;
import com.spaceprogram.simplejpa.papeeria.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gkalabin@bardsoftware.com
 */
public class WhereInQueryTests {
  private static final String PERSISTENCE_UNIT_NAME = "papeeriatestunit";
  private static final PlainObject FOO_TEST_OBJ = new PlainObject("foo", "FOO");
  private static final PlainObject BAR_TEST_OBJ = new PlainObject("bar", "BAR");
  private static final PlainObject BAZ_TEST_OBJ = new PlainObject("baz", "BAZ");
  
  private EntityManagerFactoryImpl myEntityManagerFactory;

  private static final List<Class<?>> CLASSES = new ArrayList<Class<?>>();

  static {
    CLASSES.add(PlainObject.class);
  }

  @Before
  public void setUp() throws Exception {
    myEntityManagerFactory = new EntityManagerFactoryImpl(PERSISTENCE_UNIT_NAME, null, null, getStringClassNames());
    EntityManager em = myEntityManagerFactory.createEntityManager();
    em.persist(FOO_TEST_OBJ);
    em.persist(BAR_TEST_OBJ);
    em.persist(BAZ_TEST_OBJ);
    em.close();
  }

  @After
  public void tearDown() throws Exception {
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

  @Test
  public void testIdRaw() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("select o from PlainObject o where o.str in ('foo')");
    TestUtils.collectionEquals(query.getResultList(), FOO_TEST_OBJ);
  }

  @Test
  public void testFieldRaw() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("select o from PlainObject o where o.msg in ('BAR')");
    TestUtils.collectionEquals(query.getResultList(), BAR_TEST_OBJ);
  }

  @Test(expected = PersistenceException.class)
  public void testBadRawValue() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("select o from PlainObject o where o.msg in 'foo'");
    query.getResultList();
  }

  @Test
  public void testIdOneParameter() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("select o from PlainObject o where o.str in :list");
    List<String> strs = new ArrayList<String>();
    strs.add("foo");
    query.setParameter("list", strs);
    TestUtils.collectionEquals(query.getResultList(), FOO_TEST_OBJ);
  }

  @Test
  public void testIdTwoValues() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("select o from PlainObject o where o.str in :list");
    List<String> strs = new ArrayList<String>();
    strs.add("foo");
    strs.add("bar");
    query.setParameter("list", strs);
    TestUtils.collectionEquals(query.getResultList(), FOO_TEST_OBJ, BAR_TEST_OBJ);
  }

  @Test
  public void testFieldOneValue() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("select o from PlainObject o where o.msg in :list");
    List<String> strs = new ArrayList<String>();
    strs.add("BAZ");
    query.setParameter("list", strs);
    TestUtils.collectionEquals(query.getResultList(), BAZ_TEST_OBJ);
  }

  @Test
  public void testFieldTwoValues() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("select o from PlainObject o where o.msg in :list");
    List<String> strs = new ArrayList<String>();
    strs.add("FOO");
    strs.add("BAZ");
    query.setParameter("list", strs);
    TestUtils.collectionEquals(query.getResultList(), FOO_TEST_OBJ, BAZ_TEST_OBJ);
  }

  @Test(expected = PersistenceException.class)
  public void testFieldBadValue() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("select o from PlainObject o where o.msg in :list");
    query.setParameter("list", new Date());
    query.getResultList();
  }

  @Test
  public void testValuesInSet() {
    EntityManager em = myEntityManagerFactory.createEntityManager();
    Query query = em.createQuery("select o from PlainObject o where o.msg in :list");
    Collection<String> strs = new HashSet<String>();
    strs.add("BAZ");
    query.setParameter("list", strs);
    TestUtils.collectionEquals(query.getResultList(), BAZ_TEST_OBJ);
  }

  private Set<String> getStringClassNames() {
    Set<String> classNames = new HashSet<String>(CLASSES.size());
    for (Class aClass : CLASSES) {
      classNames.add(aClass.getName());
    }
    return classNames;
  }
}
