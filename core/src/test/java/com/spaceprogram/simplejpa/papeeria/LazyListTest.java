package com.spaceprogram.simplejpa.papeeria;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;
import com.spaceprogram.simplejpa.EntityManagerSimpleJPA;
import com.spaceprogram.simplejpa.papeeria.models.PapeeriaTestObject;
import com.spaceprogram.simplejpa.papeeria.models.PapeeriaTestSubObject;
import junit.framework.TestCase;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gkalabin@bardsoftware.com
 */
public class LazyListTest extends TestCase {

    private EntityManagerFactoryImpl myEntityManagerFactory;

    private static final List<Class<?>> CLASSES = new ArrayList<Class<?>>();

    static {
        CLASSES.add(PapeeriaTestObject.class);
        CLASSES.add(PapeeriaTestSubObject.class);
    }


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

    public void testDelete() {
        EntityManager em = myEntityManagerFactory.createEntityManager();

        {
            // create initial structure
            PapeeriaTestObject obj = new PapeeriaTestObject("foo", "42");
            obj.getObjects().add(new PapeeriaTestSubObject("o1_1", obj));
            obj.getObjects().add(new PapeeriaTestSubObject("o1_2", obj));
            obj.getObjects().add(new PapeeriaTestSubObject("o1_3", obj));
            em.persist(obj);
            em.close();
        }

        em = myEntityManagerFactory.createEntityManager();
        PapeeriaTestObject obj2 = em.find(PapeeriaTestObject.class, "foo");
        em.close();
        assertEquals(3, obj2.getObjects().size());

        PapeeriaTestSubObject objToDelete = null;
        for (PapeeriaTestSubObject subObject : obj2.getObjects()) {
            if (subObject.getStr().equals("01_2")) {
                objToDelete = subObject;
            }
        }
        obj2.getObjects().remove(objToDelete);
        assertEquals(2, obj2.getObjects().size());
    }
}
