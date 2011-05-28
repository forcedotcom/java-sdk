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

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.ParentTestEntity;
import com.force.sdk.jpa.entities.TestEntity;
import com.force.sdk.test.util.BaseTransactionalSpringContextJPAFTest;

/**
 * 
 * Basic Create/Read/Update/Delete test using Spring.
 *
 * @author Dirk Hain
 */
public class SpringBasicCRUDTest extends BaseTransactionalSpringContextJPAFTest {

    private TestEntity entity = null;
    PlatformTransactionManager txMgr = null;

    @Test
    @Transactional
    /**
     * Spring entity persist test.
     * This test creates and initializes an entity and tries to persist it. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testBasicPersist() {
        txMgr = (PlatformTransactionManager) applicationContext.getBean("transactionManager");
        entity = new TestEntity();
        JPATestUtils.initializeTestEntity(entity);
        ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
        Assert.assertFalse(entityManager.contains(entity) || entityManager.contains(parent), "Entity is not transient.");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txMgr.getTransaction(def);
        entityManager.persist(parent);
        entityManager.persist(entity);
        Assert.assertTrue((entityManager.contains(entity) && entityManager.contains(parent)), " Entities are not managed.");
        txMgr.commit(txStatus);
        Assert.assertNotNull(entity.getId(), entity.getClass().getName() + " ID was not generated.");
        Assert.assertFalse(entityManager.contains(entity), "Entity is still managed but should be detached.");
        Assert.assertNotNull(entityManager.find(TestEntity.class, entity.getId()),
                "The entity was not stored to the database.");
    }
    
    @Test(dependsOnMethods = "testBasicPersist")
    /**
     * Spring entity retrieve test.
     * This test retrieves an entity created by {@see testBasicPersist}. The test dependency is defined via TestNG.
     * @hierarchy javasdk
     * @userStory xyz
     */    
    public void testBasicRetrieve() throws Exception {
        TestEntity retrieved = entityManager.find(TestEntity.class, entity.getId());
        Assert.assertTrue(JPATestUtils.annotatedEntityEqual(entity, retrieved),
                "Retrieved entity is different from inserted entity.");
        Assert.assertTrue(entityManager.contains(retrieved), "Entity is not managed.");
    }

    
    @Test(dependsOnMethods = "testBasicRetrieve")
    @Transactional
    @Rollback(false)
    /**
     * Spring entity update test.
     * This test retrieves and updates an entity created by {@see testBasicPersist}. The test dependency is defined via TestNG.
     * @hierarchy javasdk
     * @userStory xyz
     */    
    public void testBasicUpdate() throws Exception {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txMgr.getTransaction(def);
        TestEntity retrieved = entityManager.find(TestEntity.class, entity.getId());
        retrieved.setBigDecimalObject(BigDecimal.valueOf(100, 2));
        retrieved.setBoolType(false);
        retrieved.setIntType(10000);
        retrieved.setLongType(1000000);
        retrieved.setStringObject("update");
        entityManager.merge(retrieved);
        txMgr.commit(txStatus);
        entityManager.clear();
        TestEntity updated = entityManager.find(TestEntity.class, retrieved.getId());
        Assert.assertEquals(updated.getBigDecimalObject(), retrieved.getBigDecimalObject(), "Entity was not updated correctly.");
        Assert.assertEquals(updated.getBoolType(), retrieved.getBoolType(), "Entity was not updated correctly.");
        Assert.assertEquals(updated.getIntType(), retrieved.getIntType(), "Entity was not updated correctly.");
        Assert.assertEquals(updated.getLongType(), retrieved.getLongType(), "Entity was not updated correctly.");
        Assert.assertEquals(updated.getStringObject(), retrieved.getStringObject(), "Entity was not updated correctly.");
    }
    
    
    @Test(dependsOnMethods = "testBasicUpdate")
    @Transactional
    @Rollback(false)
    /**
     * Spring entity delete test.
     * This test deletes an entity created by {@see testBasicPersist}. The test dependency is defined via TestNG.
     * @hierarchy javasdk
     * @userStory xyz
     */    
    public void testBasicRemove() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txMgr.getTransaction(def);
        entity = entityManager.find(TestEntity.class, entity.getId());
        String eid = entity.getId();
        entityManager.remove(entity);
        Assert.assertFalse(entityManager.contains(entity), "Entity is still managed but should be removed.");
        txMgr.commit(txStatus);
        Assert.assertNull(entityManager.find(TestEntity.class, eid), "The entity was not deleted from the database");
    }

    @Test
    @Transactional
    @Rollback(true)
    /**
     * Spring persistence context test.
     * Test verifies that entities move through the specified persistence context lifecycle stages when running in a 
     * container managed persistence context (i.e. managed by spring). Upon transaction rollback entities should become detached.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults entities should be detached after rollback.
     */
    public void testPersistenceContextRollback() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txMgr.getTransaction(def);
        TestEntity t = new TestEntity();
        ParentTestEntity p = JPATestUtils.setMasterDetailRelationship(t);
        Assert.assertFalse(entityManager.contains(t) || entityManager.contains(p), "Entities are not transient.");
        entityManager.persist(p);
        entityManager.persist(t);
        Assert.assertTrue(entityManager.contains(t) && entityManager.contains(p), "Entities are not managed.");
        txMgr.rollback(txStatus);
        Assert.assertFalse(entityManager.contains(t) || entityManager.contains(p),
                "Entities are still managed but should be detached.");
    }
    
    @Test
    @Transactional
    @Rollback(true)
    /**
     * Initializes all entity fields with null (where possible).
     * This test initializes an entity with null for all possible fields and then persists the entity. 
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Entity should be persisted successfully.
     */
    public void testCreateNullEntity() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txMgr.getTransaction(def);
        TestEntity testEntity = new TestEntity();
        JPATestUtils.initializeNullTestEntity(testEntity);
        ParentTestEntity parent = new ParentTestEntity();
        parent.init();
        testEntity.setParentMasterDetail(parent);
        entityManager.persist(parent);
        entityManager.persist(testEntity);
        txMgr.commit(txStatus);
        Assert.assertNotNull(testEntity.getId(), testEntity.getClass().getName() + " ID was not generated.");
        Assert.assertFalse(entityManager.contains(testEntity), "Entity is still managed but should be detached.");
        Assert.assertNotNull(entityManager.find(TestEntity.class, testEntity.getId()),
                "The entity was not stored to the database.");
    }
    
    
    @Test(dependsOnMethods = "testBasicRemove")
    @Transactional
    @Rollback(false)
    /**
     * This is not a test.
     * Workaround to cleanup after all tests while the spring transaction is still active. 
     * Unfortunately, there is no adequate annotation for this timing that would allow 
     * this method to only run once after all tests.
     */    
    public void cleanup() {
        Metamodel m = entityManager.getMetamodel();
        EntityType<TestEntity> etype = m.entity(TestEntity.class); //deliberately a little verbose here
        Query q = entityManager.createQuery("Select t From " + etype.getName() + " t");
        @SuppressWarnings("rawtypes")
        List results = q.getResultList();
        for (Object e : results) {
            entityManager.remove(e);
        }
    }

}
