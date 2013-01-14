package com.spaceprogram.simplejpa.papeeria;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.google.common.collect.ImmutableList;
import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;
import com.spaceprogram.simplejpa.EntityManagerSimpleJPA;
import com.spaceprogram.simplejpa.papeeria.models.PapeeriaTestObject;
import com.spaceprogram.simplejpa.papeeria.models.PapeeriaTestSubObject1;
import junit.framework.TestCase;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gkalabin@bardsoftware.com
 */
public class LazyLoadingTest extends TestCase {

    private EntityManagerFactoryImpl myEntityManagerFactory;

    private static final List<Class<?>> CLASSES = ImmutableList.of(
            PapeeriaTestObject.class,
            PapeeriaTestSubObject1.class
    );

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Set<String> classNames = new HashSet<String>(CLASSES.size());
        for (Class aClass : CLASSES) {
            classNames.add(aClass.getName());
        }
        myEntityManagerFactory = new EntityManagerFactoryImpl("papeeriatestunit", null, null, classNames);

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

    public void testLazyLoadingAfterClosed() {
        EntityManager em = myEntityManagerFactory.createEntityManager();

        // create initial structure
        PapeeriaTestObject obj = new PapeeriaTestObject("foo", "42");
        obj.getObjects().add(new PapeeriaTestSubObject1("o1_1", obj));
        obj.getObjects().add(new PapeeriaTestSubObject1("o1_2", obj));
        obj.getObjects().add(new PapeeriaTestSubObject1("o1_3", obj));
        em.close();
        em.persist(obj);

        // find it - everything is ok
        em = myEntityManagerFactory.createEntityManager();
        PapeeriaTestObject obj2 = em.find(PapeeriaTestObject.class, "foo");
        em.close();
        assertEquals(3, obj2.getObjects().size());

        // add one more sub objects
        em = myEntityManagerFactory.createEntityManager();
        PapeeriaTestSubObject1 subObject1Added = new PapeeriaTestSubObject1("o1_4", obj);
        obj.getObjects().clear();
        obj.getObjects().add(subObject1Added);
        em.persist(subObject1Added);
        em.persist(obj);
        em.close();

        // find it - fail
        em = myEntityManagerFactory.createEntityManager();
        PapeeriaTestObject obj3 = em.find(PapeeriaTestObject.class, "foo");
        em.close();
        assertEquals(4, obj3.getObjects().size());
    }
}
