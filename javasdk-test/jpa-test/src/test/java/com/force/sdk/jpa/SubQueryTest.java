/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.force.sdk.jpa;

import java.lang.reflect.Method;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.datanucleus.jpa.EntityManagerImpl;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.*;
import com.force.sdk.jpa.entities.generated.*;
import com.force.sdk.jpa.query.QueryHints;
import com.force.sdk.qa.util.BaseJPAFTest;
import com.google.inject.internal.Lists;

/**
 *
 * Test for aggregate SOQL queries. Verifies that the depth limits of the API are respected, and tests both lazy and eager
 * fetch types on relationship fields
 *
 * @author Jill Wetzler
 */
public class SubQueryTest extends BaseJPAFTest {

    private static final int MAX_RELATIONSHIPS = 20; //this will need to be bumped to 25 later

    @AfterMethod
    protected void classTearDown() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEagerSubQueries() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);

        TestEntity entity1 = new TestEntity();
        JPATestUtils.initializeTestEntity(entity1);
        entity1.setName("entity1");
        entity1.setBoolType(true);
        TestEntity entity2 = new TestEntity();
        JPATestUtils.initializeTestEntity(entity2);
        entity2.setName("entity2");
        entity2.setBoolType(false);
        ParentTestEntity parent = new ParentTestEntity();
        parent.init();
        entity1.setParent(parent);
        entity1.setParentMasterDetail(parent);
        entity2.setParent(parent);
        entity2.setParentMasterDetail(parent);

        addTestDataInTx(Lists.newArrayList(parent, entity1, entity2));

        String nativeQuery = "select id, name, "
                                 + "(select id, " + getFieldName(em, TestEntity.class, "boolType")
                                 + " from " + getRelationshipName(em, ParentTestEntity.class, "TestEntities") + ")"
                              + " from " + getTableName(em, ParentTestEntity.class);
        List<ParentTestEntity> result = em.createNativeQuery(nativeQuery, ParentTestEntity.class).getResultList();

        Assert.assertEquals(result.size(), 1, "Unexpected number of results for native query " + nativeQuery);
        ArrayList<TestEntity> testEntities = (ArrayList<TestEntity>) result.get(0).getTestEntities();
        Assert.assertEquals(testEntities.size(), 2, "Unexpected number of child results for native query " + nativeQuery);
        boolean boolType1 = testEntities.get(0).getBoolType();
        Assert.assertNotSame(testEntities.get(1).getBoolType(), boolType1,
                "Unexpected matching bool results for native query " + nativeQuery);

        String jpqlQuery = "select o from " + ParentTestEntity.class.getSimpleName() + " o INNER JOIN o.testEntities)";
        List<ParentTestEntity> jpqlResult = em.createQuery(jpqlQuery, ParentTestEntity.class).getResultList();
        Assert.assertEquals(jpqlResult.size(), 1, "Unexpected number of results for JPQL query " + jpqlQuery);
        testEntities = (ArrayList<TestEntity>) jpqlResult.get(0).getTestEntities();
        Assert.assertEquals(testEntities.size(), 2, "Unexpected number of child results for JPQL query " + jpqlQuery);
        boolType1 = testEntities.get(0).getBoolType();
        Assert.assertNotSame(testEntities.get(1).getBoolType(), boolType1,
                "Unexpected matching bool results for JPQL query " + jpqlQuery);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLazySubQueries() {
        // Make sure we have cleaned out the org for this data
        deleteAll("Opportunity");
        final String accName = "Account for testing subqueries";
        deleteAll("Account", " a where a.name = '" + accName + "'");

        OpportunityEntity opp1 = new OpportunityEntity();
        opp1.setStageName("Prospecting");
        opp1.setCloseDate(Calendar.getInstance().getTime());
        opp1.setName("opp1 subquery");

        OpportunityEntity opp2 = new OpportunityEntity();
        opp2.setStageName("Prospecting");
        opp2.setCloseDate(Calendar.getInstance().getTime());
        opp2.setName("opp2 subquery");

        AccountEntity parentAccount = new AccountEntity();
        parentAccount.setName(accName);
        opp1.setTheAccount(parentAccount);
        opp2.setTheAccount(parentAccount);

        addTestDataInTx(Lists.newArrayList(parentAccount, opp1, opp2));

        List<AccountEntity> result =
            em.createNativeQuery("select id, name, (select id, name, stageName from Opportunities) from Account "
                                    + "where name = '" + accName + "'",
                                 AccountEntity.class).getResultList();

        Assert.assertEquals(result.size(), 1);
        ArrayList<OpportunityEntity> opportunities = (ArrayList<OpportunityEntity>) result.get(0).getChildOpportunities();
        Assert.assertEquals(opportunities.size(), 2);
        String stageName = opportunities.get(0).getStageName();
        Assert.assertEquals(stageName, "Prospecting");

        List<AccountEntity> jpqlResult =
            em.createQuery("select o from Account o INNER JOIN o.childOpportunities "
                            + "where name = '" + accName + "'", AccountEntity.class).getResultList();
        Assert.assertEquals(jpqlResult.size(), 1);
        opportunities = (ArrayList<OpportunityEntity>) jpqlResult.get(0).getChildOpportunities();
        Assert.assertEquals(opportunities.size(), 2);
        stageName = opportunities.get(0).getStageName();
        Assert.assertEquals(stageName, "Prospecting");
    }

    @Test
    public void testMaxFetchDepth() throws Exception {
        deleteAll(Entity0.class);
        deleteAll(Entity1.class);
        deleteAll(Entity2.class);
        deleteAll(Entity3.class);
        deleteAll(Entity23.class);
        deleteAll(Entity24.class);
        deleteAll(Entity25.class);

        Entity0 e0 = new Entity0();
        e0.setName("e0");
        Entity1 e1 = new Entity1();
        e1.setEntity0(e0);
        e1.setName("e1");
        Entity2 e2 = new Entity2();
        e2.setEntity1(e1);
        e2.setName("e2");
        Entity3 e3 = new Entity3();
        e3.setEntity2(e2);
        e3.setName("e3");
        Entity23 e23 = new Entity23();
        e23.setName("e23");
        Entity24 e24 = new Entity24();
        e24.setEntity23(e23);
        e24.setName("e24");
        Entity25 e25 = new Entity25();
        e25.setEntity24(e24);
        e25.setName("e25");
        addTestDataInTx(Lists.newArrayList(e0, e1, e2, e3, e23, e24, e25));

        // Test default fetch depth with aggregate subquery
        List<Entity23> result = em.createQuery("select o from Entity23 o)", Entity23.class).getResultList();
        Assert.assertEquals(result.size(), 1, "Default fetch level 1 works fine");
        assertEntity23IsOneLevel(result.get(0));
        // Test the same with find
        e23 = em.find(Entity23.class, e23.getId());
        assertEntity23IsOneLevel(e23);

        // Clear entity manager so that it does not come from the cache
        em.clear();

        // Test fetch depth > default with aggregate query
        try {
            em.createQuery("select o from Entity23 o)", Entity23.class).setHint(QueryHints.MAX_FETCH_DEPTH, 2).getResultList();
            Assert.fail("Expected exception was not thrown");
        } catch (PersistenceException ex) {
            Assert.assertTrue(ex.getMessage()
                    .contains("SOQL statements cannot query aggregate relationships more than "
                                + "1 level away from the root entity object."));
        }
        // Same test using find()
        try {
            e23 = em.find(Entity23.class, e23.getId(), Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object) 2));
            Assert.fail("Expected exception was not thrown");
        } catch (PersistenceException ex) {
            Assert.assertTrue(ex.getMessage()
                    .contains("SOQL statements cannot query aggregate relationships more than "
                                + "1 level away from the root entity object."));
        }
        // Test that we can also set the value using EntityManagerImpl.getFetchPlan().setMaxFetchDepth()
        int oldDepth = ((EntityManagerImpl) em).getFetchPlan().getMaxFetchDepth();
        try {
            try {
                ((EntityManagerImpl) em).getFetchPlan().setMaxFetchDepth(2);
                em.createQuery("select o from Entity23 o)", Entity23.class).getResultList();
                Assert.fail("Expected exception was not thrown");
            } catch (PersistenceException ex) {
                Assert.assertTrue(ex.getMessage()
                        .contains("SOQL statements cannot query aggregate relationships more than "
                                    + "1 level away from the root entity object."));
            }
            try {
                // Test the same with find
                e23 = em.find(Entity23.class, e23.getId());
                Assert.fail("Expected exception was not thrown");
            } catch (PersistenceException ex) {
                Assert.assertTrue(ex.getMessage()
                        .contains("SOQL statements cannot query aggregate relationships more than "
                                    + "1 level away from the root entity object."));
            }
        } finally {
            ((EntityManagerImpl) em).getFetchPlan().setMaxFetchDepth(oldDepth);
        }

        // Test default fetch depth with relationship queries
        List<Entity3> result3 = em.createQuery("select o from Entity3 o)", Entity3.class).getResultList();
        Assert.assertEquals(result3.size(), 1, "Default fetch level 1 works fine");
        assertEntity3IsOneLevel(result3.get(0));
        // Test the same with find
        e3 = em.find(Entity3.class, e3.getId());
        assertEntity3IsOneLevel(e3);

        // Test fetch depth > default with relationship queries
        result3 = em.createQuery("select o from Entity3 o)", Entity3.class)
                        .setHint(QueryHints.MAX_FETCH_DEPTH, 2).getResultList();
        Assert.assertEquals(result3.size(), 1, "Default fetch level 1 works fine");
        verifyDepth(result3.get(0), 2);

        // This transaction is only for testing find() with query depth.
        EntityManager fem = emfac.createEntityManager();
        //EntityTransaction tx = fem.getTransaction();
        Entity3 e31 = fem.find(Entity3.class, e3.id, Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object) 3));
        verifyDepth(e31, 3);

        // soql query is always going to the database.
        // However, find() first reads the object from the cache if it is present there.
        // detach only changes the persistence context to detached and leaves some trace of the original object in the cache.
        // therefore, within the same transaction we will have to clear()  to really clear the cache.
        em.clear();
        // e3 and result3.get(0) were references to the same object;
        // but detaching them didn't clear the cache of persistence context.
        //em.detach(e3);
        //em.detach(result3.get(0));

        Entity3 e32 = em.find(Entity3.class, e3.id, Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object) 3));
        verifyDepth(e32, 3);

        // Test fetch depth 0
        List<Entity24> result24 = em.createQuery("select o from Entity24 o)", Entity24.class)
                                        .setHint(QueryHints.MAX_FETCH_DEPTH, 0).getResultList();
        Assert.assertEquals(result24.size(), 1, "Default fetch level 1 works fine");
        Assert.assertEquals(result24.get(0).getEntity25s().size(), 0, "Zero level of subquery object fetched");
        Assert.assertNull(result24.get(0).getEntity23(), "Zero level of relationship object fetched");
    }

    private void verifyDepth(Entity3 e3, int depth) {
        if (depth >= 2) {
            Assert.assertNotNull(e3.getEntity2(), "One level of object fetched");
            Assert.assertEquals(e3.getEntity2().getName(), "e2", "Entity name is e2");
            Assert.assertNotNull(e3.getEntity2().getEntity1(), "Second level of object fetched");
            Assert.assertEquals(e3.getEntity2().getEntity1().getName(), "e1", "Entity name is e1");
        }

        if (depth == 2) {
            Assert.assertNull(e3.getEntity2().getEntity1().getEntity0(), "Third level of object not fetched");
        }

        if (depth >= 3) {
            Assert.assertNotNull(e3.getEntity2().getEntity1().getEntity0(), "Third level of object fetched");
        }
    }

    private void assertEntity23IsOneLevel(Entity23 e23) {
        Assert.assertEquals(e23.getName(), "e23");
        Assert.assertEquals(e23.getEntity24s().size(), 1, "One level of object fetched");
        Entity24 e24 = e23.getEntity24s().iterator().next();
        Assert.assertEquals(e24.getName(), "e24");
        Assert.assertEquals(e24.getEntity25s().size(), 0, "Second level of object not fetched");
    }

    private void assertEntity3IsOneLevel(Entity3 e3) {
        Assert.assertEquals(e3.getName(), "e3");
        Assert.assertNotNull(e3.getEntity2(), "One level of object fetched");
        Assert.assertEquals(e3.getEntity2().getName(), "e2");
        Assert.assertNull(e3.getEntity2().getEntity1(), "Second level of object not fetched");
    }

    @Test
    public void testMultipleLevelSubqueries() throws Exception {
        // Delete all entity23
        deleteAll(Entity23.class);

        //test native first, the API will reject this query immediately
        try {
            em.createNativeQuery("select id, name, "
                                    + "(select id, name, "
                                        + "(select id, name from " + getRelationshipName(em, Entity1.class, "entity2s") + ") "
                                    +  "from " + getRelationshipName(em, Entity0.class, "entity1s") + ") "
                                + "from " + getTableName(em, Entity0.class),
                Entity0.class).getResultList();
            Assert.fail("Expected exception was not thrown");
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage()
                    .contains("SOQL statements cannot query aggregate relationships more than "
                                + "1 level away from the root entity object."));
        }

        Entity23 e23 = new Entity23();
        e23.setName("e23");
        addTestDatumInTx(e23);

        // Test a JPQL query that will eventually generate into a query similar to the above native query.
        // the exception is thrown because we ask for entity23 which eager loads entity24 which eager loads entity25
        // Note that all the joins are implicit joins but we have to override default fetch depth. This is same as
        // MaxFetchDepth test.
        try {
            em.createQuery("select o from Entity23 o)", Entity23.class).setHint(QueryHints.MAX_FETCH_DEPTH, 2).getResultList();
            Assert.fail("Expected exception was not thrown");
        } catch (PersistenceException ex) {
            Assert.assertTrue(ex.getMessage()
                    .contains("SOQL statements cannot query aggregate relationships more than "
                                + "1 level away from the root entity object."));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMaxSubqueries() throws Exception {
        // cleanup Entity0
        deleteAll(Entity0.class);

        Entity0 e0 = new Entity0();
        e0.setName("e0");

        List<Object> entities = new ArrayList<Object>();
        entities.add(e0);

        //create one of each entity, with a lookup to the previous
        Object parent = e0;
        for (int i = 1; i < MAX_RELATIONSHIPS + 1; i++) {
            Class c = Class.forName("com.force.sdk.jpa.entities.generated.Entity" + i);
            Object entity = c.getConstructor().newInstance();
            c.getMethod("setName", String.class).invoke(entity, "e" + i);
            String methodName = "setEntity" + (i - 1);
            Method m = c.getMethod(methodName, Class.forName("com.force.sdk.jpa.entities.generated.Entity" + (i - 1)));
            m.invoke(entity, parent);
            entities.add(entity);
            parent = entity;
        }

        addTestDataInTx(entities);

        //native query
        StringBuilder sb = new StringBuilder("select id, name");
        //right now the max is 20 but in the next version of the API it's 25, so this test will need to be changed
        for (int i = 1; i < MAX_RELATIONSHIPS + 1; i++) {
            sb.append(", (select id, name from ").append(getRelationshipName(em, Entity0.class, "entity" + i + "s")).append(")");
        }
        sb.append(" from ").append(getTableName(em, Entity0.class));
        List<Entity0> queryResult = em.createNativeQuery(sb.toString(), Entity0.class).getResultList();
        Assert.assertEquals(queryResult.size(), 1);
        verifySubqueryResult(queryResult.get(0));

        // JPQL query is simply get the root entity which will lazily pull the other entities as needed, 
        // the "JOIN" is implicit here
        sb = new StringBuilder("select o from Entity0 o");
        queryResult = em.createQuery(sb.toString(), Entity0.class).getResultList();
        Assert.assertEquals(queryResult.size(), 1);
        verifySubqueryResult(queryResult.get(0));
    }

    //starting at result, traverse through all of the children and verify their names
    @SuppressWarnings("unchecked")
    private void verifySubqueryResult(Object result) throws Exception {
        for (int i = 0; i < MAX_RELATIONSHIPS; i++) {
            Class c = Class.forName("com.force.sdk.jpa.entities.generated.Entity" + i);
            ArrayList child = (ArrayList) c.getMethod("getEntity" + (i + 1) + "s").invoke(result);
            Assert.assertEquals(1, child.size());
            result = child.get(0);
            Assert.assertEquals("e" + (i + 1), result.getClass().getMethod("getName").invoke(result));
        }
    }
}
