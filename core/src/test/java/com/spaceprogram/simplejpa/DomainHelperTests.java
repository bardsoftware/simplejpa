package com.spaceprogram.simplejpa;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectResult;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.PersistenceException;
import java.util.List;

/**
 * Must enable 2nd level cache to make this tests useful.
 * <p/>
 * User: normj
 * Date: Sept 1, 2010
 * Time: 12:56:00 PM
 */
public class DomainHelperTests extends BaseTestClass {
    private static final String DOMAIN_NAME = "simplejpa-domainhelper-tests";
    private AmazonSimpleDB mySdbClient;

    @Before
    public void setUp() {
        EntityManagerSimpleJPA em = (EntityManagerSimpleJPA) factory.createEntityManager();
        mySdbClient = em.getSimpleDb();
        mySdbClient.createDomain(new CreateDomainRequest().withDomainName(DOMAIN_NAME));
    }

    @After
    public void tearDown() {
        mySdbClient.deleteDomain(new DeleteDomainRequest().withDomainName(DOMAIN_NAME));
    }

    @Test
    public void findByIdTest() {
        Assert.assertNull(DomainHelper.findItemById(mySdbClient, DOMAIN_NAME, "noexist"));

        mySdbClient.putAttributes(new PutAttributesRequest()
                .withItemName("exist")
                .withDomainName(DOMAIN_NAME)
                .withAttributes(new ReplaceableAttribute("name", "value", true)));
        Assert.assertNotNull(DomainHelper.findItemById(mySdbClient, DOMAIN_NAME, "exist"));
        Assert.assertNull(DomainHelper.findItemById(mySdbClient, DOMAIN_NAME, ""));
    }

    @Test
    public void listAllItemsTests() {
        for (int i = 0; i < 3; i++) {
            mySdbClient.putAttributes(
                    new PutAttributesRequest()
                            .withItemName("thing" + i)
                            .withDomainName(DOMAIN_NAME)
                            .withAttributes(new ReplaceableAttribute("name", "value", true)));
        }

        List<Item> items = DomainHelper.listAllItems(mySdbClient, DOMAIN_NAME);
        Assert.assertEquals(3, items.size());
    }

    @Test
    public void selectItemsTests() {
        for (int i = 0; i < 10; i++) {
            mySdbClient.putAttributes(
                    new PutAttributesRequest()
                            .withItemName("thing" + i)
                            .withDomainName(DOMAIN_NAME)
                            .withAttributes(new ReplaceableAttribute("name", "value", true)));
        }

        SelectResult result = DomainHelper.selectItems(mySdbClient, String.format("select * from `%s` LIMIT 3", DOMAIN_NAME), null);
        Assert.assertEquals(3, result.getItems().size());
        Assert.assertNotNull(result.getNextToken());

        result = DomainHelper.selectItems(mySdbClient, String.format("select * from `%s` LIMIT 3", DOMAIN_NAME), result.getNextToken());
        Assert.assertEquals(3, result.getItems().size());
        Assert.assertNotNull(result.getNextToken());

        result = DomainHelper.selectItems(mySdbClient, String.format("select * from `%s` LIMIT 3", DOMAIN_NAME), result.getNextToken());
        Assert.assertEquals(3, result.getItems().size());
        Assert.assertNotNull(result.getNextToken());

        result = DomainHelper.selectItems(mySdbClient, String.format("select * from `%s` LIMIT 3", DOMAIN_NAME), result.getNextToken());
        Assert.assertEquals(1, result.getItems().size());
        Assert.assertNull(result.getNextToken());
    }

    @Test
    public void selectItemsWithWhereTests() {
        for (int i = 0; i < 10; i++) {
            mySdbClient.putAttributes(
                    new PutAttributesRequest()
                            .withItemName("thing" + i)
                            .withDomainName(DOMAIN_NAME)
                            .withAttributes(new ReplaceableAttribute("name", "value", true)));
        }

        SelectResult result = DomainHelper.selectItems(mySdbClient, DOMAIN_NAME, "name = 'value' LIMIT 3", null);
        Assert.assertEquals(3, result.getItems().size());
        Assert.assertNotNull(result.getNextToken());

        result = DomainHelper.selectItems(mySdbClient, DOMAIN_NAME, "name = 'value' LIMIT 3", result.getNextToken());
        Assert.assertEquals(3, result.getItems().size());
        Assert.assertNotNull(result.getNextToken());

        result = DomainHelper.selectItems(mySdbClient, DOMAIN_NAME, "name = 'value' LIMIT 3", result.getNextToken());
        Assert.assertEquals(3, result.getItems().size());
        Assert.assertNotNull(result.getNextToken());

        result = DomainHelper.selectItems(mySdbClient, DOMAIN_NAME, "name = 'value' LIMIT 3", result.getNextToken());
        Assert.assertEquals(1, result.getItems().size());
        Assert.assertNull(result.getNextToken());
    }
}
