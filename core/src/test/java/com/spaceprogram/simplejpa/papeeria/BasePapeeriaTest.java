package com.spaceprogram.simplejpa.papeeria;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;
import com.spaceprogram.simplejpa.EntityManagerSimpleJPA;
import junit.framework.TestCase;
import org.junit.Ignore;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gkalabin@papeeria.com
 */
@Ignore
public class BasePapeeriaTest extends TestCase {
    public static final String PERSISTENCE_UNIT_NAME = "papeeriatestunit";
    private EntityManagerFactoryImpl myEntityManagerFactory;

    protected static final List<Class<?>> ourTestClasses = new ArrayList<Class<?>>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myEntityManagerFactory = new EntityManagerFactoryImpl(PERSISTENCE_UNIT_NAME, null, null, getStringClassNames());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        EntityManagerSimpleJPA em = (EntityManagerSimpleJPA) myEntityManagerFactory.createEntityManager();
        for (Class<?> aClass : ourTestClasses) {
            AmazonSimpleDB db = em.getSimpleDb();
            String domainName = em.getDomainName(aClass);

            System.out.println("deleting domain: " + domainName);
            DeleteDomainRequest deleteDomainRequest = new DeleteDomainRequest(domainName);
            db.deleteDomain(deleteDomainRequest);
        }
        em.close();
    }

    protected Set<String> getStringClassNames() {
        Set<String> classNames = new HashSet<String>(ourTestClasses.size());
        for (Class aClass : ourTestClasses) {
            classNames.add(aClass.getName());
        }
        return classNames;
    }

    protected EntityManager getEntityManager() {
        return myEntityManagerFactory.createEntityManager();
    }
}
