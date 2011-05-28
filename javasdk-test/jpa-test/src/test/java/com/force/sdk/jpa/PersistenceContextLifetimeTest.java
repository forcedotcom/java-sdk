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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.persistence.*;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.entities.ParentTestEntity;
import com.force.sdk.jpa.entities.TestEntity;
import com.force.sdk.test.util.BaseJPAFTest;
import com.force.sdk.test.util.TestContext;

/**
 * 
 * Tests for PersistenceContext lifetime in a not container managed scenario.
 *
 * @author Dirk Hain
 */
public class PersistenceContextLifetimeTest extends BaseJPAFTest {

    protected EntityManagerFactory emfExt;
    protected EntityManager emExt;
    
    @BeforeClass(dependsOnMethods = "initialize")
    protected final void initExtPersCtxt() {
        //dyn org as already been created
        HashMap<String, Object> configOverrides = TestContext.get().getUserInfo().getUserinfoAsPersistenceunitProperties();
        emfExt = Persistence.createEntityManagerFactory("extPersCtxPU", configOverrides);
        emExt = emfExt.createEntityManager();
    }
    
    @BeforeMethod
    protected void clean() {
        deleteAll(TestEntity.class);
        deleteAll(ParentTestEntity.class);
    }
    
    @Test
    /**
     * Application managed extended persistence context test.
     * Tests the correct lifecycle events in a application managed extended persistence context. An extended persistence context
     * can call persist/remove/merge/refresh without a transaction.
     * @hierarchy javasdk
     * @userStory xyz
     */            
    public void testExtendedBehavior() {
        TestEntity t = new TestEntity();
        JPATestUtils.initializeTestEntity(t);
        //new/transient
        ParentTestEntity p = JPATestUtils.setMasterDetailRelationship(t);
        t.setParent(p);
        Assert.assertFalse(emExt.contains(t), "Entity is not transient.");
        emExt.persist(p);
        emExt.persist(t);
//        Assert.assertNull(t.getId(), "Entity was already committed.");//fails
        EntityTransaction tx = emExt.getTransaction();
        tx.begin();
        //managed        
        tx.commit();
        Assert.assertTrue((emExt.contains(t) && emExt.contains(p)), "Entities are not managed.");
        Assert.assertNotNull(t.getId(), "ID was not generated.");
        Assert.assertNotNull(emExt.find(TestEntity.class, t.getId()), "The entity was not stored to the database.");
        String firstTE = t.getId();
        //now remove first TE and replace it with another TE
        emExt.remove(t);
        //another EM should still see the entity
//        Assert.assertNotNull(em.find(TestEntity.class, firstTE), "The entity is already deleted.");//fails
        TestEntity second = new TestEntity();
        JPATestUtils.initializeTestEntity(second);
        second.setParentMasterDetail(p);
        second.setParent(p);
        emExt.persist(second);
        Assert.assertTrue(emExt.contains(second), "Second entity is managed.");
//        Assert.assertNull(second.getId(), "Second was already committed.");//fails
        tx = emExt.getTransaction();
        tx.begin();
        Assert.assertTrue((emExt.contains(second) && emExt.contains(p)), "Entities are not managed.");
//        Assert.assertFalse(emExt.contains(t), "Removed entity is still managed.");//fails
        tx.commit();
        Assert.assertFalse(firstTE.equals(second.getId()), "The first entity was not replaced by a new.");
        //now make changes on TE, then refresh (reverts changes) and merge outside of a tx
        String origName = second.getName();
        second.setName("testExtendedBehavior");
        emExt.refresh(second);
        Assert.assertEquals(second.getName(), origName, "Refresh did not reset the name.");
        second.setName("newname");
//        emExt.merge(second);//should make the new name persistent upon next commit //FAILS due to tx required - this is very odd
        Assert.assertEquals(em.find(TestEntity.class, second.getId()).getName(), origName,
                "Merge instantly committed the changes."); //should still see the old name
        tx = emExt.getTransaction();
        tx.begin();
        tx.rollback();
        Assert.assertEquals(emExt.find(TestEntity.class, second.getId()).getName(), origName, "Name update was not rolled back.");
    }
    
    @Test
    /**
     * Application managed extended persistence context transaction behavior.
     * Tests the correct lifecycle behavior of transactions in an application managed extended persistence context.
     * @hierarchy javasdk
     * @userStory xyz
     */            
    public void testTxBehavior() {
        TestEntity t = new TestEntity();
        JPATestUtils.initializeTestEntity(t);
        //new/transient
        ParentTestEntity p = JPATestUtils.setMasterDetailRelationship(t);
        t.setParent(p);
        Assert.assertFalse(em.contains(t), "Entity is not transient.");
        EntityTransaction tx = em.getTransaction();
        if (tx.isActive())tx.commit();
        try {
            em.persist(p);
            //TODO
//            Assert.fail("Persist should require a transaction.");//fails 
        } catch (IllegalStateException ise) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ise.printStackTrace(pw);
            Assert.assertTrue(ise.getMessage()
                    .contains("Transaction is not active. You need to define a transaction around this"),
                    "Caught wrong exception:\n" + sw.toString());
        }
        tx = em.getTransaction();
        tx.begin();
//        em.persist(p);//fails since persist above is successful
        em.persist(t);
        tx.commit();
        t.setName("refresh");
        try {
            em.refresh(t);
            Assert.fail("Refresh should require a transaction.");
        } catch (Exception e) {
            //TODO: this currently throws java.lang.IllegalArgumentException: 
            // Entity "com.force.sdk.jpa.entities.TestEntity@4631f1f8" isnt managed and so you cant use this method
            // We need to change that
//            Assert.assertTrue(e.getMessage()
//                    .contains("Transaction is not active. You need to define a transaction around this"), 
//                    "Caught wrong exception:\n" + e.getStackTrace());            
        }
        t.setName("testTxBehavior");
        try {
            em.merge(t);
            //TODO
//            Assert.fail("Merge should require a transaction.");//fails
        } catch (Exception e) {
            e.printStackTrace();
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Assert.assertTrue(e.getMessage()
                    .contains("Transaction is not active. You need to define a transaction around this"),
                    "Caught wrong exception:\n" + sw.toString());
        }
    }
    
    
    @DataProvider
    public Object[][] persistenceContextData() {
        Object[][] contexts = new Object[][]{
                {emExt, true},
                {em, false},
        };
        return contexts;
    }
    
    @Test(dataProvider = "persistenceContextData")
    /**
     * Persistence context commit behavior for eager types.
     * @hierarchy javasdk
     * @userStory xyz
     */            
    public void testFetchtypeEager(EntityManager e, boolean isExtended) {
        String eId = prepareTest(e, isExtended);
        TestEntity entity = e.find(TestEntity.class, eId);
        String errMsg = "Tx scoped: ";
        if (isExtended) {
            errMsg = "Extended: ";
        }
        Assert.assertTrue(e.contains(entity), errMsg + " Entity is not managed.");
        Assert.assertTrue(e.contains(entity.getParent()), errMsg + " Parent is not managed.");
        ParentTestEntity parent = entity.getParent();
        String pid = parent.getId();
        EntityTransaction tx = e.getTransaction();
        tx.begin();
        tx.commit();
        if (isExtended) {
            Assert.assertTrue(e.contains(entity) && e.contains(parent), errMsg + " Entity is not managed.");
            e.clear(); //detach all
        } else {
            Assert.assertTrue(!e.contains(entity) && !e.contains(parent), errMsg + " Entity is not detached.");
        }
        parent = e.find(ParentTestEntity.class, pid);
        Assert.assertTrue(e.contains(parent), errMsg + " Parent is not managed.");
        //TODO
//        Assert.assertTrue(e.contains(entity), errMsg + " Entity is not managed but should be due to FetchType.EAGER.");//fails
    }
    
    
    @Test(dataProvider = "persistenceContextData")
    /**
     * Persistence context commit behavior.
     * Tests the correct lifecycle changes for application managed persistence contexts.
     * @hierarchy javasdk
     * @userStory xyz
     */            
    public void testPersistenceContextCommit(EntityManager e, boolean isExtended) {
        String eId = prepareTest(e, isExtended);
        String errMsg = "Tx scoped: ";
        if (isExtended) {
            errMsg = "Extended: ";
        }
        TestEntity entity = e.find(TestEntity.class, eId);
        //managed
        Assert.assertTrue(e.contains(entity), errMsg + " Entity is not managed.");
        EntityTransaction tx = e.getTransaction();
        tx.begin();
        tx.commit();
        if (isExtended) {
            Assert.assertTrue(e.contains(entity), errMsg + " Entity is not managed.");
        } else {
            Assert.assertFalse(e.contains(entity), errMsg + " Entity is not detached.");
            entity = e.find(TestEntity.class, eId);
        }
        tx = e.getTransaction();
        tx.begin();
        e.remove(entity);
        //removed
        Assert.assertFalse(e.contains(entity), errMsg + " Entity is not removed.");
        tx.commit();
        //transient
        Assert.assertFalse(e.contains(entity), errMsg + " Entity is not transient.");
    }

    @Test(dataProvider = "persistenceContextData")
    /**
     * Persistence context rollback behavior.
     * Tests the correct lifecycle changes for application managed persistence contexts for rollback.
     * @hierarchy javasdk
     * @userStory xyz
     */            
    public void testTxPersistenceContextRollback(EntityManager e, boolean isExtended) {
        String eId = prepareTest(e, isExtended);
        String errMsg = "Tx scoped: ";
        if (isExtended) {
            errMsg = "Extended: ";
        }
        //now refresh and create an entity for removal
        EntityTransaction tx = e.getTransaction();
        tx.begin();
        TestEntity removed = new TestEntity();
        //new
        JPATestUtils.initializeTestEntity(removed);
        ParentTestEntity parentRemoved = JPATestUtils.setMasterDetailRelationship(removed);
        e.persist(parentRemoved);
        e.persist(removed);
        TestEntity managed = e.find(TestEntity.class, eId);
        //managed
        Assert.assertTrue(e.contains(managed) && e.contains(removed) && e.contains(parentRemoved),
                errMsg + " Not all entities are in managed state.");
        e.remove(removed);
        e.remove(managed);
        //removed
        Assert.assertFalse(e.contains(removed), errMsg + " Removed entity should still be managed.");
        tx.rollback();
        Assert.assertFalse(e.contains(managed), errMsg + " Managed entity was not detached on tx.rollback().");
        Assert.assertFalse(e.contains(removed) || e.contains(parentRemoved),
                errMsg + " Removed entity was not detached on tx.rollback().");
        managed = e.find(TestEntity.class, eId);
        Assert.assertNotNull(managed, "The entity was removed despite the rollback.");
    }

    /**
     * Helper that creates test entities in tx scoped persistence context and verifies correct life cycle.
     */
    private String prepareTest(EntityManager e, boolean isExtended) {
        TestEntity t = new TestEntity();
        JPATestUtils.initializeTestEntity(t);
        //new/transient
        ParentTestEntity p = JPATestUtils.setMasterDetailRelationship(t);
        t.setParent(p);
        EntityTransaction tx = e.getTransaction();
        String errMsg = "Tx scoped: ";
        if (isExtended) {
            errMsg = "Extended: ";
        }
        Assert.assertFalse(e.contains(t), errMsg + "Entity is not transient.");
        tx.begin();
        e.persist(p);
        e.persist(t);
        //managed
        Assert.assertTrue((e.contains(t) && e.contains(p)), errMsg + " Entity is not managed.");
        tx.commit();
        Assert.assertNotNull(t.getId(), errMsg + " ID was not generated.");
        Assert.assertNotNull(e.find(TestEntity.class, t.getId()), errMsg + " The entity was not stored to the database.");
        if (isExtended) {
            //in extended pers. ctx the entity stays managed after commit
            Assert.assertTrue(e.contains(t), errMsg + " Entity is not managed but should be for extended pers. ctx.");
        } else {
            Assert.assertFalse(e.contains(t), errMsg + " Entity is not detached.");
        }
        return t.getId();
    }
    
}
