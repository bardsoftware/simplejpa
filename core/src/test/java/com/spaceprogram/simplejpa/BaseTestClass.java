package com.spaceprogram.simplejpa;


import com.amazonaws.AmazonClientException;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.NoSuchDomainException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: treeder
 * Date: May 14, 2008
 * Time: 3:48:37 PM
 */
public class BaseTestClass {
    static EntityManagerFactoryImpl factory;
    List<String> afterTestLog = new ArrayList<String>();

    @BeforeClass
    public static void setupEntityManagerFactory() throws IOException {
        factory = new EntityManagerFactoryImpl("papeeriatestunit", null);
        
        /*
        This doesn't work when not packaged in jar or something.
        (EntityManagerFactoryImpl) Persistence.createEntityManagerFactory("persistenceSDB");*/
    }

    @AfterClass
    public static void tearDownEntityManagerFactory() {
        drop(MyTestObject.class);
        drop(MyTestObject2.class);
        drop(MyTestObject3.class);
        drop(MyTestObject4.class);
        drop(MyInheritanceObject1.class);
        drop(MyInheritanceObject2.class);
        drop(MyInheritanceObject3.class);
        factory.close();
    }

    /**
     * Deletes domain associated with entity
     *
     * @param classToDelete representing entity
     */
    private static void drop(Class classToDelete) {
        EntityManagerSimpleJPA em = (EntityManagerSimpleJPA) factory.createEntityManager();
        AmazonSimpleDB db = em.getSimpleDb();
        String domainName = em.getDomainName(classToDelete);

        System.out.println("deleting domain: " + domainName);
        DeleteDomainRequest deleteDomainRequest = new DeleteDomainRequest(domainName);
        db.deleteDomain(deleteDomainRequest);
        em.close();
    }

    @Before
    public void cleanupLoggingStuff() {
        afterTestLog.clear();
    }

    @After
    public void deleteAll() throws AmazonClientException {
        printLog();

        // todo: should just delete all items in the domain, would probably be faster
        System.out.println("Deleting all objects created during test...");
        EntityManagerSimpleJPA em = (EntityManagerSimpleJPA) factory.createEntityManager();
        AmazonSimpleDB db = em.getSimpleDb();
        deleteAll(em, db, MyTestObject.class);
        deleteAll(em, db, MyTestObject2.class);
        deleteAll(em, db, MyTestObject3.class);
        deleteAll(em, db, MyTestObject4.class);
        deleteAll(em, db, MyInheritanceObject1.class);
        deleteAll(em, db, MyInheritanceObject2.class);
        deleteAll(em, db, MyInheritanceObject3.class);
        em.close();
    }

    private void deleteAll(EntityManagerSimpleJPA em, AmazonSimpleDB db, Class aClass) throws AmazonClientException {
        String domainName = em.getDomainName(aClass);
        System.out.println("deleting from domain: " + domainName);
        try {
            List<Item> items = DomainHelper.listAllItems(db, domainName);
            deleteAll(db, domainName, items);
        } catch (NoSuchDomainException e) {
        } catch (AmazonClientException e) {
            e.printStackTrace();
        }
    }

    private void deleteAll(AmazonSimpleDB db, String domainName, List<Item> itemList) throws AmazonClientException {
        System.out.println("Deleting " + itemList.size() + " items from domain " + domainName);
        for (Item item : itemList) {

            db.deleteAttributes(new DeleteAttributesRequest()
                    .withDomainName(domainName)
                    .withItemName(item.getName()));
        }
    }

    private void printLog() {
        for (String s : afterTestLog) {
            System.out.println(s);
        }
    }

    protected void printAndLog(String s) {
        System.out.println(s);
        afterTestLog.add(s);
    }

    protected void clearCaches() {
        factory.getCache(MyTestObject.class).clear();
        factory.getCache(MyTestObject2.class).clear();
        factory.getCache(MyTestObject3.class).clear();
        factory.getCache(MyTestObject4.class).clear();
    }
}
