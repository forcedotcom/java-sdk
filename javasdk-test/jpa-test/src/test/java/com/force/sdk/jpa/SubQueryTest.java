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

import com.force.sdk.jpa.entities.AccountEntity;
import com.force.sdk.jpa.entities.OpportunityEntity;
import com.force.sdk.jpa.entities.ParentTestEntity;
import com.force.sdk.jpa.entities.TestEntity;
import com.force.sdk.jpa.entities.generated.*;
import com.force.sdk.jpa.query.QueryHints;
import com.force.sdk.qa.util.BaseJPAFTest;
import com.google.inject.internal.Lists;
import org.datanucleus.jpa.EntityManagerImpl;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.lang.reflect.Method;
import java.util.*;

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
    public void testFetchDepthError() throws Exception {
        em.getTransaction().begin();
        String errorMessage = "Max fetch depth cannot be greater than 5.";
        try {
            em.createQuery("select o from Entity3 o)", Entity3.class)
                        .setHint(QueryHints.MAX_FETCH_DEPTH, 7).getResultList();
            Assert.fail("Exception was not thrown");
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), errorMessage);
        }

        try {
            em.find(Entity3.class, "xxxxx", Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object) 9));
            Assert.fail("Exception was not thrown");
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), errorMessage);
        }

        try {
            EntityManagerFactory badEMF
                    = Persistence.createEntityManagerFactory("testFetchDepthTooHigh");
            EntityManager badEM = badEMF.createEntityManager();
            badEM.createQuery("select o from Entity3 o)", Entity3.class).getResultList();
            Assert.fail("Exception was not thrown");
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), errorMessage);
        }
        em.getTransaction().commit();
    }

    @Test
    public void testLazyFetchingPastFetchDepth() throws Exception {
        deleteAll(Entity0.class);
        deleteAll(Entity1.class);
        deleteAll(Entity2.class);
        deleteAll(Entity3.class);
        deleteAll(Entity4.class);
        deleteAll(Entity5.class);
        deleteAll(Entity6.class);

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
        Entity4 e4 = new Entity4();
        e4.setEntity3(e3);
        e4.setName("e4");
        Entity5 e5 = new Entity5();
        e5.setEntity4(e4);
        e5.setName("e5");
        Entity6 e6 = new Entity6();
        e6.setEntity5(e5);
        e6.setName("e6");
        addTestDataInTx(Lists.newArrayList(e0, e1, e2, e3, e4, e5, e6));

        for (int i = 1; i <= 5; i++) {
            List<Entity0> entities;
            // Test that we can also set the value using EntityManagerImpl.getFetchPlan().setMaxFetchDepth()
            int oldDepth = ((EntityManagerImpl) em).getFetchPlan().getMaxFetchDepth();
            try {
                ((EntityManagerImpl) em).getFetchPlan().setMaxFetchDepth(i);
                entities = em.createQuery("select o from Entity0 o)", Entity0.class).getResultList();
                verifyLazyFetchingOfEntities(entities);

                // Test the same with find
                e0 = em.find(Entity0.class, entities.get(0).getId());
                verifyLazyFetchingOfEntities(Collections.singletonList(e0));
            } finally {
                ((EntityManagerImpl) em).getFetchPlan().setMaxFetchDepth(oldDepth);
            }

            em.clear();

            entities = em.createQuery("select o from Entity0 o)", Entity0.class)
                        .setHint(QueryHints.MAX_FETCH_DEPTH, i).getResultList();
            verifyLazyFetchingOfEntities(entities);

            EntityManager fem = emfac.createEntityManager();
            e0 = fem.find(Entity0.class, e0.id, Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object) i));
            verifyLazyFetchingOfEntities(Collections.singletonList(e0));


        }
    }

    private void verifyLazyFetchingOfEntities(List<Entity0> entities) {
        Assert.assertEquals(1, entities.size());
        Entity0 e0 = entities.get(0);
        Assert.assertEquals(1, e0.getEntity1s().size());
        Collection<Entity2> e2s = e0.getEntity1s().iterator().next().getEntity2s();
        Assert.assertEquals(1, e2s.size());
        Collection<Entity3> e3s = e2s.iterator().next().getEntity3s();
        Assert.assertEquals(1, e3s.size());
        Collection<Entity4> e4s = e3s.iterator().next().getEntity4s();
        Assert.assertEquals(1, e4s.size());
        Collection<Entity5> e5s = e4s.iterator().next().getEntity5s();
        Assert.assertEquals(1, e5s.size());
        Collection<Entity6> e6s = e5s.iterator().next().getEntity6s();
        Assert.assertEquals(1, e6s.size());

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

    @Test
    public void testManyToOneSubquery() {
        deleteAll(Entity0.class);
        deleteAll(Entity1.class);
        deleteAll(Entity2.class);

        //create the main entity
        Entity1 e1 = new Entity1();
        e1.setName("testManyToOneSubquery");

        //create two entity2s, give each their own entity0 and the same entity1
        Entity2 firstChild = new Entity2();
        firstChild.setName("entity2_1");
        firstChild.setEntity1(e1);
        Entity0 entityForFirst = new Entity0();
        entityForFirst.setName("entity0_1");
        firstChild.setEntity0(entityForFirst);

        Entity2 secondChild = new Entity2();
        secondChild.setName("entity2_2");
        secondChild.setEntity1(e1);
        Entity0 entityForSecond = new Entity0();
        entityForSecond.setName("entity0_2");
        secondChild.setEntity0(entityForSecond);

        addTestDataInTx(Lists.newArrayList(e1, entityForFirst, entityForSecond, firstChild, secondChild));

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Entity1 retrievedEntity = em.find(Entity1.class, e1.getId(),
                    Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object) 3));
            for (Entity2 child : retrievedEntity.getEntity2s()) {
                child.getEntity0();
                System.out.println("e2: " + child.getId() + " --> e2.e0: "
                       + child.getEntity0() + " --> e2.e1: " + child.getEntity1());
            }

            tx.commit();
            tx = null;
        } finally {
            if (tx != null) {
                tx.rollback();
            }
        }
    }
}
