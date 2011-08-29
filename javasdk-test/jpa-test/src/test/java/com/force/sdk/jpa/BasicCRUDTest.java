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

import java.lang.reflect.Constructor;
import java.util.*;

import javax.persistence.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.*;
import com.force.sdk.qa.util.TestContext;
import com.force.sdk.qa.util.jpa.BaseMultiEntityManagerJPAFTest;
import com.force.sdk.qa.util.logging.ForceLogAppenderValidator;
import com.force.test.model.JarEntity;

/**
 * 
 * Smoke tests for basic JPA operations including logging.
 *
 * @author Dirk Hain
 */
public class BasicCRUDTest extends BaseMultiEntityManagerJPAFTest {
    
    @Test
    public void testBasicPersist() {
        TestEntity entity = new TestEntity();
        JPATestUtils.initializeTestEntity(entity);
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            Assert.assertTrue(em.contains(entity), "The entity was not stored to the database.");
            em.remove(entity);
            Assert.assertFalse(em.contains(entity), "The entity was not deleted from the database");
        } catch (PersistenceException pex) {
            pex.printStackTrace();
        } finally {
            tx.rollback();
        }
    }
    
    /**
     * Tests the persistence of an entity that is found in a jar file.
     */
    @Test
    public void testBasicPersistJarEntity() {
        JarEntity entity = new JarEntity();
        entity.setName("jar entity");
        EntityTransaction tx = em4.getTransaction();
        try {
            tx.begin();
            em4.persist(entity);
            Assert.assertTrue(em4.contains(entity), "The entity was not stored to the database.");
            em4.remove(entity);
            Assert.assertFalse(em4.contains(entity), "The entity was not deleted from the database");
        } catch (PersistenceException pex) {
            pex.printStackTrace();
        } finally {
            tx.rollback();
        }
    }
    
    private static final String[] TEST_BASIC_JPA_LOGGING_LOGS = {
        "JPA Persist - entity: com.force.sdk.jpa.entities.ParentTestEntity@",
        "JPA Persist - entity: com.force.sdk.jpa.entities.TestEntity@",
        "JPA Lock - entity: com.force.sdk.jpa.entities.TestEntity@[0-9A-Fa-f]+ lock: READ",
        "JPA Flush ignored",
        "Creating object: [a-zA-Z0-9_$]*ParentTestEntity__c",
        "Created object id: [a-zA-Z0-9]{18}",
        "Creating object: [a-zA-Z0-9_$]*TestEntity__c",
        "Created object id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Detach - entity: com.force.sdk.jpa.entities.TestEntity@",
        "JPA Merge: - entity: com.force.sdk.jpa.entities.TestEntity@",
        "JPA Remove - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Deleting object: [a-zA-Z0-9]{18}"
    };
    
    @Test
    public void testBasicJPALogging() {
        Logger logger = Logger.getLogger("com.force.sdk.jpa");
        Level origLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        ForceLogAppenderValidator appender = new ForceLogAppenderValidator(TEST_BASIC_JPA_LOGGING_LOGS);
        try {
            logger.addAppender(appender);

            TestEntity entity = new TestEntity();
            JPATestUtils.initializeTestEntity(entity);
            ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(parent);
            em.persist(entity);
            em.lock(entity, LockModeType.READ);
            em.flush();
            tx.commit();
            tx.begin();
            entity = em.find(entity.getClass(), entity.getId());
            em.detach(entity);
            entity = em.merge(entity);
            em.remove(entity);
            tx.commit();
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(origLevel);
        }
    }
    
    @Test
    public void testNegativeCRUD() {
        TestEntity entity = new TestEntity();
        JPATestUtils.initializeTestEntity(entity);
        ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(parent);
        em.persist(entity);
        em.persist(entity); // This is a no-op
        em.detach(entity);
        try {
            em.persist(entity);
            Assert.fail("Persisting detached entity should fail");
        } catch (EntityExistsException pe) {
            Assert.assertTrue(pe.getMessage().contains("Entity already exists. Use merge to save changes."),
                    "Unexpected message: " + pe.getMessage());
        }
        try {
            em.merge(entity);
            Assert.fail("Detached entity with null id cannot be merged.");
        } catch (IllegalArgumentException pe) {
            Assert.assertTrue(pe.getMessage().contains("Detached entity with null id cannot be merged."),
                    "Unexpected message: " + pe.getMessage());
        }
        try {
            tx.commit();
            Assert.fail("Transaction should have been marked for rollback-only");
        } catch (RollbackException re) {
            tx.rollback();
        }
        Assert.assertNull(entity.getId(), "Entity got unexpected id");
        
        // Abandon old entities as they are not usable so start over again
        entity = new TestEntity();
        JPATestUtils.initializeTestEntity(entity);
        parent = JPATestUtils.setMasterDetailRelationship(entity);
        tx = em.getTransaction();
        tx.begin();
        em.persist(parent);
        em.persist(entity);
        tx.commit();
        
        final String someName = "some odd name testNegativeCRUD";
        entity = em.find(entity.getClass(), entity.getId());
        entity.setName(someName);
        tx.begin();
        em.detach(entity);
        em.merge(entity);
        tx.commit();
        entity = em.find(entity.getClass(), entity.getId());
        Assert.assertEquals(entity.getName(), someName, "Changes did not get saved");
    }
    
    @Test
    public void testNoTXCRUD() {
        EntityManager emm = emfac.createEntityManager();
        TestEntity entity = new TestEntity();
        JPATestUtils.initializeTestEntity(entity);
        ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
        emm.persist(parent);
        Assert.assertNotNull(parent.getId(), "Entity should have been persisted to db and got an id");
        emm.persist(entity);
        Assert.assertNotNull(entity.getId(), "Entity should have been persisted to db and got an id");
        try {
            em.persist(entity); // since entity is always detached because of no tx
            Assert.fail("Persisting detached entity should fail");
        } catch (EntityExistsException pe) {
            Assert.assertTrue(pe.getMessage().contains("Entity already exists. Use merge to save changes."),
                    "Unexpected message: " + pe.getMessage());
        }
        final String someName = "some odd name testNoTXCRUD";
        entity.setName(someName);
        emm.merge(entity);
        entity = emm.find(entity.getClass(), entity.getId());
        Assert.assertEquals(entity.getName(), someName, "Changes did not get saved");
    }
    
    @Test
    public void testDateConversion() {
        TimeZone origTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"));
            TestEntity entity = new TestEntity();
            JPATestUtils.initializeTestEntity(entity);
            ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
            Calendar date = entity.getDateTimeCal();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                em.persist(parent);
                em.persist(entity);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                Assert.fail(e.getMessage());
            }
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Istanbul"));
            entity = em.find(TestEntity.class, entity.getId());
            Calendar dateOut = entity.getDateTimeCal();
            Assert.assertEquals(dateOut.getTimeInMillis(), date.getTimeInMillis());
            Assert.assertEquals(dateOut.getTimeZone(), TimeZone.getTimeZone("GMT"));
        } finally {
            TimeZone.setDefault(origTimeZone);
        }
    }

    @Test
    public void testDateAndCalendarUpdate() {
        TestEntity entity = new TestEntity();
        JPATestUtils.initializeTestEntity(entity);
        ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
        EntityTransaction tx = em.getTransaction();
        Date oldDate = entity.getDate();
        Calendar oldCal = entity.getDateTimeCal();
        GregorianCalendar oldGCal = entity.getDateTimeGCal();
        tx.begin();
        try {
            em.persist(parent);
            em.persist(entity);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            Assert.fail(e.getMessage());
        }
        // Now update Date, Calendar and GregorianCalendar
        entity = em.find(TestEntity.class, entity.getId());
        
        //Set it to 10 days off for Date and 10 hours off for Cals
        entity.getDate().setTime(oldDate.getTime() + 10 * 24 * 3600 * 1000);
        entity.getDateTimeCal().add(Calendar.HOUR_OF_DAY, 10);
        entity.getDateTimeGCal().add(Calendar.HOUR_OF_DAY, 10);
        
        tx.begin();
        try {
            em.merge(entity);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            Assert.fail(e.getMessage());
        }
        // Read back
        entity = em.find(TestEntity.class, entity.getId());
        
        Assert.assertEquals(entity.getDate().getTime(), oldDate.getTime() + 10 * 24 * 3600 * 1000);
        Assert.assertEquals(entity.getDateTimeCal().getTimeInMillis(), oldCal.getTimeInMillis() + 10 * 3600 * 1000);
        Assert.assertEquals(entity.getDateTimeGCal().getTimeInMillis(), oldGCal.getTimeInMillis() + 10 * 3600 * 1000);
    }
    
    @Test
    public void testPersistLifecycle() {
        testPersistLifecycleInternal(em, false);
        testPersistLifecycleInternal(em, true);
    }
    
    @Test
    public void testPersistLifecycleOptimistic() {
        testPersistLifecycleInternal(em2, false);
        testPersistLifecycleInternal(em2, true);
    }
    
    @Test
    public void testPersistLifecycleOptimisticAllOrNothing() {
       testPersistLifecycleInternal(em3, false);
       testPersistLifecycleInternal(em3, true);
    }
    
    public void testPersistLifecycleInternal(EntityManager emm, boolean useTransientMerge) {
        // entity is transient here
        TestEntity entity = new TestEntity();
        if (useTransientMerge) BaseEntity.initialiseForTransientMerge(emm, entity);
        JPATestUtils.initializeTestEntity(entity);
        ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
        EntityTransaction tx = emm.getTransaction();
        tx.begin();
        entity.setStringObject("abcd123");
        emm.persist(parent);
        // entity has become persistent 
        emm.persist(entity);
        tx.commit();
        // entity has become detached
        
        // Now read back the object
        entity = emm.find(TestEntity.class, entity.getId());
        Assert.assertEquals(entity.getStringObject(), "abcd123");
        
        // After commit entity object is detached so we need to merge it again
        tx = emm.getTransaction();
        tx.begin();
        entity.setIntType(111);
        entity.setStringObject("abcd321");
        entity = emm.merge(entity);
        // entity is now persistent
        
        // We can even change the object here but we can't do that in the case of newly created object since there was no id there
        entity.setStringObject("abcd");
        tx.commit();
        // entity is detached
        
        // Now read back the object
        entity = emm.find(TestEntity.class, entity.getId());
        Assert.assertEquals(entity.getStringObject(), "abcd");
        Assert.assertTrue(111 == entity.getIntType());
    }
    
    @Test
    public void testPersistLifecycleUpdateExistingTransientObject() {
        testPersistLifecycleUpdateExistingTransientObjectInternal(em, false);
        testPersistLifecycleUpdateExistingTransientObjectInternal(em, true);
    }
    
    @Test
    public void testPersistLifecycleUpdateExistingTransientObjectOptimistic() {
        testPersistLifecycleUpdateExistingTransientObjectInternal(em2, false);
        testPersistLifecycleUpdateExistingTransientObjectInternal(em2, true);
    }
    
    @Test
    public void testPersistLifecycleUpdateExistingTransientObjectOptimisticAllOrNothing() {
        testPersistLifecycleUpdateExistingTransientObjectInternal(em3, false);
        testPersistLifecycleUpdateExistingTransientObjectInternal(em3, true);
    }
    
    private void testPersistLifecycleUpdateExistingTransientObjectInternal(EntityManager emm, boolean useTransientMerge) {
        // entity is transient here
        TestEntity entity = new TestEntity();
        if (useTransientMerge) BaseEntity.initialiseForTransientMerge(emm, entity);
        JPATestUtils.initializeTestEntity(entity);
        ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
        EntityTransaction tx = emm.getTransaction();
        tx.begin();
        entity.setStringObject("abcd123");
        emm.persist(parent);
        // entity has become persistent 
        emm.persist(entity);
        tx.commit();
        // entity has become detached
        
        // After commit entity object is detached so we need to merge it again but we need to read back version so that
        // we create them again with that version
        entity = emm.find(TestEntity.class, entity.getId());
        String id = entity.getId();
        Calendar version = entity.getLastModifiedDate();
        
        tx = emm.getTransaction();
        tx.begin();
        entity = new TestEntity();
        BaseEntity.initialiseForTransientMerge(emm, entity);
        /**
         *  lastModified needs to be set just like ID in the case of transient merge
         */
        entity.setId(id);
        entity.getLastModifiedDate().setTime(version.getTime());

        entity.setIntType(111);
        entity.setStringObject("abcd321");
        entity = emm.merge(entity);
        // entity is now persistent
        
        // We can even change the object here but we can't do that in the case of newly created object since there was no id there
        entity.setStringObject("abcd");
        tx.commit();
        // entity is detached
        
        // Now read back the object
        TestEntity entity2 = emm.find(TestEntity.class, entity.getId());
        Assert.assertEquals("abcd", entity2.getStringObject());
        Assert.assertTrue(111 == entity2.getIntType());
    }
    
    @Test
    public void testSimpleRelationship() throws Exception {
        for (int i = 0; i < 2; i++) {
            SimpleLookupChild entity = new SimpleLookupChild();
            if (i % 2 != 0) entity = BaseEntity.<SimpleLookupChild>initialiseForTransientMerge(em, entity);
            entity.name = "blah";
            SimpleLookupParent parent = new SimpleLookupParent();
            entity.parent = parent;
            testSimpleRelationshipInternal(entity, SimpleLookupChild.class, parent, em);
        }
    }
    
    private void testSimpleRelationshipInternal(SimpleLookupChild entity, Class<?> clazz, SimpleLookupParent parent,
            EntityManager emm) throws Exception {
        EntityTransaction tx = emm.getTransaction();
        try {
            tx.begin();
            emm.persist(parent);
            emm.persist(entity);
            Assert.assertTrue(emm.contains(parent), "The parent entity was not stored to the database.");
            Assert.assertTrue(emm.contains(entity), "The entity was not stored to the database.");
        } catch (PersistenceException pex) {
            tx.rollback();
            throw pex;
        }
        // This will flush the data to db
        tx.commit();
    }
    
    private static final String[] TEST_BASIC_PERSIST_GOES_TO_DB_LOGS = {
        "JPA Persist - entity: com.force.sdk.jpa.entities.ParentTestEntity@",
        "JPA Persist - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Creating object: [a-zA-Z0-9_$]*ParentTestEntity__c",
        "Created object id: [a-zA-Z0-9]{18}",
        "Creating object: [a-zA-Z0-9_$]*TestEntity__c",
        "Created object id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.TestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.ParentTestEntity id: [a-zA-Z0-9]{18} lock: null",
        "JPA Merge: - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Updating object: [a-zA-Z0-9_$]*TestEntity__c id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.TestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Refresh - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.ParentTestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: ParentTestEntity id: [a-zA-Z0-9]{18}",
        "JPA Remove - entity: com.force.sdk.jpa.entities.ParentTestEntity@",
        "Fetch object: ParentTestEntity id: [a-zA-Z0-9]{18}",
        "Deleting object: [a-zA-Z0-9]{18}",
    };
    
    @Test
    public void testBasicPersistGoesToDb() throws Exception {
        for (int i = 0; i < 2; i++) {
            TestEntity entity = new TestEntity();
            if (i % 2 != 0) entity = BaseEntity.initialiseForTransientMerge(em, entity);
            JPATestUtils.initializeTestEntity(entity);
            ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
            testBasicPersistGoesToDbInternal(entity, TestEntity.class, parent, em, TEST_BASIC_PERSIST_GOES_TO_DB_LOGS);
        }
    }
    
    @Test
    public void testBasicPersistGoesToDbWithMethodAnnotations() throws Exception {
        for (int i = 0; i < 2; i++) {
            TestEntityMethodAnnotations entity = new TestEntityMethodAnnotations();
            if (i % 2 != 0) entity = BaseEntity.initialiseForTransientMerge(em, entity);
            JPATestUtils.initializeTestEntity(entity);
            ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
            testBasicPersistGoesToDbInternal(entity, TestEntityMethodAnnotations.class, parent, em, null);
        }
    }
    
    private static final String[] TEST_BASIC_PERSIST_GOES_TO_DB_WITH_OPTIMISTIC_TX_LOGS = {
        "JPA Persist - entity: com.force.sdk.jpa.entities.ParentTestEntity@",
        "JPA Persist - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Creating object: [a-zA-Z0-9_$]*ParentTestEntity__c",
        "Created object id: [a-zA-Z0-9]{18}",
        "Creating object: [a-zA-Z0-9_$]*TestEntity__c",
        "Created object id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.TestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.ParentTestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: ParentTestEntity id: [a-zA-Z0-9]{18}",
        "JPA Merge: - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Conditional header set to: \\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z",
        "Updating object: [a-zA-Z0-9_$]*TestEntity__c id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.TestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Refresh - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.ParentTestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: ParentTestEntity id: [a-zA-Z0-9]{18}",
        "JPA Remove - entity: com.force.sdk.jpa.entities.ParentTestEntity@",
        "Fetch object: ParentTestEntity id: [a-zA-Z0-9]{18}",
        "Deleting object: [a-zA-Z0-9]{18}"
    };
    
    @Test
    public void testBasicPersistGoesToDbWithOptimisticTransaction() throws Exception {
        for (int i = 0; i < 2; i++) {
            TestEntity entity = new TestEntity();
            if (i % 2 != 0) entity = BaseEntity.initialiseForTransientMerge(em2, entity);
            JPATestUtils.initializeTestEntity(entity);
            ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
            testBasicPersistGoesToDbInternal(entity, TestEntity.class, parent, em2,
                    TEST_BASIC_PERSIST_GOES_TO_DB_WITH_OPTIMISTIC_TX_LOGS);
        }
    }
    
    private static final String[] TEST_BASIC_PERSIST_GOES_TO_DB_WITH_OPTIMISTIC_TX_ALL_OR_NOTHING_LOGS = {
        "JPA Persist - entity: com.force.sdk.jpa.entities.ParentTestEntity@",
        "JPA Persist - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Queuing for A-O-N create object: [a-zA-Z0-9_$]*ParentTestEntity__c",
        "Queuing for A-O-N create object: [a-zA-Z0-9_$]*TestEntity__c",
        "Creating objects: \\[entity: [a-zA-Z0-9_$]*ParentTestEntity__c, entity: [a-zA-Z0-9_$]*TestEntity__c\\]",
        "Created objects: \\[[a-zA-Z0-9]{18}, [a-zA-Z0-9]{18}\\]",
        "JPA Find - entity: com.force.sdk.jpa.entities.TestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.ParentTestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: ParentTestEntity id: [a-zA-Z0-9]{18}",
        "JPA Merge: - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Queuing for A-O-N update object: [a-zA-Z0-9_$]*TestEntity__c id: [a-zA-Z0-9]{18}",
        "Updating objects: \\[entity: [a-zA-Z0-9_$]*TestEntity__c id: [a-zA-Z0-9]{18}\\]",
        "Conditional header set to: \\[\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z\\]",
        "JPA Find - entity: com.force.sdk.jpa.entities.TestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Refresh - entity: com.force.sdk.jpa.entities.TestEntity@",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "Fetch object: TestEntity id: [a-zA-Z0-9]{18}",
        "JPA Find - entity: com.force.sdk.jpa.entities.ParentTestEntity id: [a-zA-Z0-9]{18} lock: null",
        "Fetch object: ParentTestEntity id: [a-zA-Z0-9]{18}",
        "JPA Remove - entity: com.force.sdk.jpa.entities.ParentTestEntity@",
        "Fetch object: ParentTestEntity id: [a-zA-Z0-9]{18}",
        "Queuing for A-O-N delete object: [a-zA-Z0-9]{18}",
        "Deleting objects: \\[[a-zA-Z0-9]{18}\\]"
    };
    
    @Test
    public void testBasicPersistGoesToDbWithOptimisticTransactionAllOrNothing() throws Exception {
        for (int i = 0; i < 2; i++) {
            TestEntity entity = new TestEntity();
            if (i % 2 != 0) entity = BaseEntity.initialiseForTransientMerge(em3, entity);
            JPATestUtils.initializeTestEntity(entity);
            ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
            testBasicPersistGoesToDbInternal(entity, TestEntity.class, parent, em3,
                    TEST_BASIC_PERSIST_GOES_TO_DB_WITH_OPTIMISTIC_TX_ALL_OR_NOTHING_LOGS);
        }
    }
    
    public void testBasicPersistGoesToDbInternal(AnnotatedEntity entity, Class<? extends AnnotatedEntity> clazz,
            ParentTestEntity parent, EntityManager emm, String[] expectedLogs) throws Exception {
        Logger logger = Logger.getLogger("com.force.sdk.jpa");
        Level origLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        ForceLogAppenderValidator appender = new ForceLogAppenderValidator(expectedLogs);
        try {
            logger.addAppender(appender);
            
            EntityTransaction tx = emm.getTransaction();
            try {
                tx.begin();
                emm.persist(parent);
                emm.persist(entity);
                Assert.assertTrue(emm.contains(parent), "The parent entity was not stored to the database.");
                Assert.assertTrue(emm.contains(entity), "The entity was not stored to the database.");
            } catch (PersistenceException pex) {
                tx.rollback();
                throw pex;
            }
            // This will flush the data to db
            tx.commit();
    
            tx = emm.getTransaction();
            tx.begin();
            Assert.assertTrue(entity.getId() != null, "The entity was not stored to the database.");

            // Read data back from db
            AnnotatedEntity entityFromDb1 = null;
            ParentTestEntity entityFromDb2 = null;
            try {
                entityFromDb1 = emm.find(clazz, entity.getId());
                Assert.assertEquals(entityFromDb1.getId(), entity.getId());
                // since autonumber starts above 100 this should be true always
                Assert.assertTrue(entityFromDb1.getAutoNum() > 99L);
                Assert.assertTrue(JPATestUtils.annotatedEntityEqual(entity, entityFromDb1),
                        "Retrieved entity is different from inserted entity.");
                
                entityFromDb2 = emm.find(ParentTestEntity.class, parent.getId());
                Assert.assertEquals(entityFromDb2.getId(), parent.getId());
            } finally {
                tx.commit();
            }
            
            // Update some data on the object
            String newString = "My not so random string";
            tx.begin();
            try {
                entityFromDb1.setStringObject(newString);
                emm.merge(entityFromDb1);
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
            tx.commit();
            
            // Readback from db and validate data
            entityFromDb1 = emm.find(clazz, entityFromDb1.getId());
            entityFromDb1.setStringObject("some junk text");
            emm.refresh(entityFromDb1);
            // The object should be overwritten from db and the junk text should be overwritten with the newString
            Assert.assertEquals(entityFromDb1.getStringObject(), newString);
            deleteObject(parent.getClass(), parent.getId(), emm);
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(origLevel);
        }
    }
    
    @Test
    /**
     * Test that schema creation uses describeSObjects (instead of describeSObject).
     * 1. Acquire jpa schema logger and append ForceLogAppenderValidator
     * 2. Create schema via createEntityManagerFactory call.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Log validator should receive a log with "DescribeSObjects:".
     */
    public void testDescribeSObjectsCall() {
        String[] expected = {"DescribeSObjects: *"};
        Logger logger = Logger.getLogger("com.force.sdk.jpa.schema");
        Level origLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        ForceLogAppenderValidator appender = new ForceLogAppenderValidator(expected);
        try {
            logger.addAppender(appender);
            //now create an EMF which should trigger the createSObjects call
            Persistence.createEntityManagerFactory(TestContext.get().getPersistenceUnitName());
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(origLevel);
        }
    }
    
    
    @Test
    public void testRelationshipPersistence() throws Exception {
        testRelationshipPersistenceInternal(em, false);
        testRelationshipPersistenceInternal(em, true);
    }
    
    @Test
    public void testRelationshipPersistenceOptimistic() throws Exception {
        testRelationshipPersistenceInternal(em2, false);
        testRelationshipPersistenceInternal(em2, true);
    }
    
    @Test
    public void testRelationshipPersistenceOptimisticAllOrNothing() throws Exception {
        testRelationshipPersistenceInternal(em3, false);
        testRelationshipPersistenceInternal(em3, true);
    }
    
    public void testRelationshipPersistenceInternal(EntityManager emm, boolean useTransientMerge) throws Exception {
        ParentTestEntity parent = new ParentTestEntity();
        parent.init();
        TestEntity entity = new TestEntity();
        if (useTransientMerge) BaseEntity.initialiseForTransientMerge(emm, entity);
        JPATestUtils.initializeTestEntity(entity);
        entity.setParent(parent);
        ParentTestEntity parentMD = JPATestUtils.setMasterDetailRelationship(entity);
        
        //set the user
        String userPropsQuery = "SELECT u FROM User u WHERE u.username = '" + TestContext.get().getUserInfo().getUserName() + "'";
        User u = (User) emm.createQuery(userPropsQuery).getSingleResult();
        entity.setUserLookUp(u);
        
        EntityTransaction tx = emm.getTransaction();
        try {
            tx.begin();
            emm.persist(parent);
            emm.persist(parentMD);
            emm.persist(entity);
            Assert.assertTrue(emm.contains(parent), "The parent entity was not stored to the database.");
            Assert.assertTrue(emm.contains(parentMD), "The parent MD entity was not stored to the database.");
            Assert.assertTrue(emm.contains(entity), "The entity was not stored to the database.");

            tx.commit();
            // Read data back from db
            TestEntity entityFromDb1 = null;
            ParentTestEntity entityFromDb2 = null;
            ParentTestEntity entityFromDb3 = null;
            
            entityFromDb1 = emm.find(TestEntity.class, entity.getId());
            Assert.assertEquals(entityFromDb1.getId(), entity.getId());
            
            entityFromDb2 = emm.find(ParentTestEntity.class, parent.getId());
            Assert.assertEquals(entityFromDb2.getId(), parent.getId());
            
            entityFromDb3 = emm.find(ParentTestEntity.class, parentMD.getId());
            Assert.assertEquals(entityFromDb3.getId(), parentMD.getId());
        } catch (Exception ex) {
            tx.rollback();
            Assert.fail(ex.getMessage());
        }
        deleteObject(parent.getClass(), parent.getId(), emm);
        deleteObject(parentMD.getClass(), parentMD.getId(), emm);
        
        //deleting the master object should cause the child object to get deleted.
        Assert.assertNull(emm.find(TestEntity.class, entity.getId()));
    }
    
    @Test
    /**
     * Optimistic transaction test with non-repeatable read.
     * 1. Create one TestEntity and initialize it.
     * 2. Create another TestEntity and initialize it with transient merge.
     * 3. Make a change to the entity and merge it with the EntityManager.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Initial merge should fail with optimistic transaction violation.
     */
    public void testOptimisticTransaction() throws Exception {
        for (int i = 0; i < 2; i++) {
            TestEntity entity = new TestEntity();
            if (i % 2 != 0) BaseEntity.initialiseForTransientMerge(em, entity);

            JPATestUtils.initializeTestEntity(entity);
            ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
            testOptimisticTransactionInternal(entity, TestEntity.class, parent, em2, i % 2 != 0);
        }
    }
    
    @Test
    /**
     * Optimistic transaction with non-repeatable read with method annotations.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testOptimisticTransactionWithMethodAnnotations() throws Exception {
        for (int i = 0; i < 2; i++) {
            TestEntityMethodAnnotations entity = new TestEntityMethodAnnotations();
            if (i % 2 != 0) BaseEntity.initialiseForTransientMerge(em, entity);

            JPATestUtils.initializeTestEntity(entity);
            ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
            testOptimisticTransactionInternal(entity, TestEntityMethodAnnotations.class, parent, em2, i % 2 != 0);
        }
    }
    
    @Test
    /**
     * Optimistic transaction with non-repeatable read in all-or-nothing mode.
     * Test optimistic transaction with non-repeatable read with all-or-nothing. The 
     * initial merge attempt should fail and only succeed upon re-reading the object from DB.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults {@see testOptimisticTransaction}
     */
    public void testOptimisticTransactionAllOrNothing() throws Exception {
        for (int i = 0; i < 2; i++) {
            TestEntity entity = new TestEntity();
            if (i % 2 != 0) BaseEntity.initialiseForTransientMerge(em, entity);

            JPATestUtils.initializeTestEntity(entity);
            ParentTestEntity parent = JPATestUtils.setMasterDetailRelationship(entity);
            testOptimisticTransactionInternal(entity, TestEntity.class, parent, em3, i % 2 != 0);
        }
    }
    
    public void testOptimisticTransactionInternal(AnnotatedEntity entity, Class<? extends AnnotatedEntity> clazz,
            ParentTestEntity parent, EntityManager emm, boolean useTransientMerge) throws Exception {
        saveObject(em, parent, entity, true);
        Assert.assertTrue(entity.getId() != null, "The entity was not stored to the database.");
        entity = em.find(clazz, entity.getId());

        EntityTransaction tx = emm.getTransaction();
        boolean gotExpectedFailure = false;
        tx.begin();

        // Read data back from db over optimistic connection
        AnnotatedEntity entityFromDb1 = null;
        try {
            entityFromDb1 = emm.find(clazz, entity.getId());
            Assert.assertEquals(entityFromDb1.getId(), entity.getId());
            if (useTransientMerge) {
                Constructor<? extends AnnotatedEntity> c = clazz.getConstructor();
                entityFromDb1 = c.newInstance();
                BaseEntity.initialiseForTransientMerge(emm, entityFromDb1);
                entityFromDb1.setId(entity.getId());
                entityFromDb1.getLastModifiedDate().setTime((entity.getLastModifiedDate().getTime()));
            }
            
            // Now change the object via another connection but too bad we need to have one second delay
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                // Ignore
            }
            entity.setName("blahblah");
            saveObject(em, entity, null, false);
            
            entityFromDb1.setName("some other name");
            try {
                emm.merge(entityFromDb1);
                tx.commit();
                // expect save to fail
                Assert.fail("Should not be able to save the same object");
            } catch (RollbackException oe) {
                Assert.assertTrue(oe.getCause().getMessage()
                        .contains("Some instances failed to flush successfully due to optimistic verification problems"),
                        "Error message not found");
                gotExpectedFailure = true;
            }
        } finally {
            if (!gotExpectedFailure) {
                if (tx.isActive()) {
                    tx.rollback();
                }
            }
        }
        
        // Reread the object and you can save again
        entityFromDb1 = emm.find(clazz, entity.getId());
        Assert.assertEquals(entityFromDb1.getId(), entity.getId());
        
        entityFromDb1.setName("some other name");
        saveObject(emm, entityFromDb1, null, false);

        // wait one more second and then write back the old object
        tx = em.getTransaction();
        tx.begin();
        try {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                // Ignore
            }
            // Ensure em has no problems since it does not use optimistic transactions
            em.merge(entity);
        } finally {
            tx.commit();
        }
    }

    @Test
    public void testFlushIsNoop() {
        AccountEntity testEntity = new AccountEntity();
        testEntity.setName("BasicCRUDTest.testFlushIsNoop");

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(testEntity);
        em.flush();

        Assert.assertNull(testEntity.getId(), "expected id for testEntity to be null, but it was: " + testEntity.getId());
        Assert.assertTrue(em.contains(testEntity),
                "testEntity was persisted and flush is a noop, but the testEntity is not managed.");
        tx.rollback();
    }


    private void saveObject(EntityManager entityManager, Object entity1, Object entity2, boolean isTransient) throws Exception {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        if (isTransient) {
            entityManager.persist(entity1);
            if (entity2 != null) {
                entityManager.persist(entity2);
            }
        } else {
            entityManager.merge(entity1);
            if (entity2 != null) {
               entityManager.merge(entity2);
            }
        }
        tx.commit();
    }
    
    @SuppressWarnings("unchecked")
    private void deleteObject(Class clazz, String id, EntityManager emm) throws Exception {
        EntityTransaction tx = emm.getTransaction();
        tx.begin();
        try {
            emm.remove(emm.find(clazz, id));
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
}
