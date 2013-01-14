package com.spaceprogram.simplejpa.papeeria;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.google.common.collect.ImmutableList;
import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;
import com.spaceprogram.simplejpa.EntityManagerSimpleJPA;
import com.spaceprogram.simplejpa.papeeria.models.PapeeriaTestObject;
import com.spaceprogram.simplejpa.papeeria.models.PapeeriaTestSubObject1;
import com.spaceprogram.simplejpa.papeeria.models.PapeeriaTestSubObject2;
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
            PapeeriaTestSubObject1.class,
            PapeeriaTestSubObject2.class
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
        obj.getObjects().add(new PapeeriaTestSubObject1("o1_1", new byte[]{1, 1, 2, 3, 5, 8}, obj));
        obj.getObjects().add(new PapeeriaTestSubObject1("o1_2", new byte[]{1, 1, 2, 3, 5, 8, 13, 21}, obj));
        obj.getObjects().add(new PapeeriaTestSubObject1("o1_3", new byte[]{1, 1, 2, 3, 5, 8, 13, 21, 34, 55}, obj));
        obj.getAnotherObjects().add(new PapeeriaTestSubObject2("o2_1", obj));
        obj.getAnotherObjects().add(new PapeeriaTestSubObject2("o2_2", obj));
        obj.getAnotherObjects().add(new PapeeriaTestSubObject2("o2_3", obj));
        obj.getAnotherObjects().add(new PapeeriaTestSubObject2("o2_4", obj));
        obj.getAnotherObjects().add(new PapeeriaTestSubObject2("o2_5", obj));
        em.close();
        em.persist(obj);

        // find it - everything is ok
        em = myEntityManagerFactory.createEntityManager();
        PapeeriaTestObject obj2 = em.find(PapeeriaTestObject.class, "foo");
        em.close();
        assertEquals(3, obj.getObjects().size());
        assertEquals(5, obj.getAnotherObjects().size());

        // add one more sub objects
        em = myEntityManagerFactory.createEntityManager();
        PapeeriaTestSubObject1 subObject1Added = new PapeeriaTestSubObject1("o1_4", new byte[]{1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 127}, obj);
        obj.getObjects().clear();
        obj.getObjects().add(subObject1Added);
        em.persist(subObject1Added);
        PapeeriaTestSubObject2 subObject2Added = new PapeeriaTestSubObject2("o2_6", obj);
        obj.getAnotherObjects().clear();
        obj.getAnotherObjects().add(subObject2Added);
        em.persist(subObject2Added);
        em.persist(obj);
        em.close();

        // find it - fail
        em = myEntityManagerFactory.createEntityManager();
        PapeeriaTestObject obj3 = em.find(PapeeriaTestObject.class, "foo");
        em.close();
        assertEquals(4, obj3.getObjects().size());
        assertEquals(6, obj3.getAnotherObjects().size());
    }
}
