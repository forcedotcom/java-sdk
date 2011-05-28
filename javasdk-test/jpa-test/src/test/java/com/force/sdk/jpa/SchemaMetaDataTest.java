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
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.*;

import org.datanucleus.ObjectManagerImpl;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.*;
import com.force.sdk.jpa.entities.FolderEntity.FolderType;
import com.force.sdk.jpa.query.QueryHints;
import com.force.sdk.jpa.sample.Employee;
import com.force.sdk.jpa.schema.ForceStoreSchemaHandler;
import com.force.sdk.jpa.table.TableImpl;
import com.force.sdk.test.util.BaseMultiEntityManagerJPAFTest;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.fault.InvalidSObjectFault;

/**
 * 
 * This class tests @CustomObject and @CustomField features.
 *
 * @author Jill Wetzler
 */
public class SchemaMetaDataTest extends BaseMultiEntityManagerJPAFTest {
    
    @Test
    public void testEnableFeedsAnnotation() throws Exception {
        TestEntity entity = new TestEntity();
        JPATestUtils.initializeTestEntity(entity);
        ParentTestEntity parentMD = JPATestUtils.setMasterDetailRelationship(entity);
        entity.setParent(parentMD);
        
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(parentMD);
            em.persist(entity);
            tx.commit();
        } catch (Exception ex) {
            tx.rollback();
            Assert.fail(ex.getMessage());
        }
        
        tx = em.getTransaction();
        try {
            tx.begin();
            ParentTestEntity parentEntityFromDb = em.find(ParentTestEntity.class, parentMD.getId());
            parentEntityFromDb.setTextField("new value");
            em.merge(parentEntityFromDb);
            tx.commit();
        } catch (Exception ex) {
            tx.rollback();
            Assert.fail(ex.getMessage());
        }
        
        QueryResult result =
            service.query("select id, type from " + getTableName(em, ParentTestEntity.class).replace("__c", "__Feed")
                            + " where parentId = '" + parentMD.getId() + "'");
        assert (result.getSize() == 2); //one for creating the item, one for updating the tracked field
        boolean exceptionThrown = false;
        try {
            service.query("select id, type, feedpost.body from " + getTableName(em, TestEntity.class).replace("__c", "__Feed")
                            + " where parentId = '" + parentMD.getId() + "'");
        } catch (InvalidSObjectFault e) {
            //we should expect this message since tracking is not enabled for this object
            exceptionThrown = true;
        } finally {
            deleteAll(ParentTestEntity.class);
        }
        
        if (!exceptionThrown) {
            Assert.fail("Was able to query feed on object without feeds enabled");
        }
    }
    
    @Test
    /**
     * Schema cache test.
     * Test ensures that the internal schema cache is only keeping objects that are used by this application. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testMetadataCache() throws Exception {
        Object delegate = em.getDelegate();
        Assert.assertNotNull(delegate);
        ObjectManagerImpl om = (ObjectManagerImpl) delegate;
        ForceStoreSchemaHandler forceSH = (ForceStoreSchemaHandler) om.getStoreManager().getSchemaHandler();
        // Search for an entity that we know is used by the tests
        Assert.assertNotNull(forceSH.getTable("TestEntity"));
        // Search for an entity that we know is NOT used by the tests
        Assert.assertNull(forceSH.getTable("Task"));
    }
    
    @Test
    /**
     * Test @transient fields.
     * Tests ensures that @Transient field are not created. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testTransientFields() throws Exception {
        Object delegate = em.getDelegate();
        Assert.assertNotNull(delegate);
        ObjectManagerImpl om = (ObjectManagerImpl) delegate;
        ForceStoreSchemaHandler forceSH = (ForceStoreSchemaHandler) om.getStoreManager().getSchemaHandler();
        // Search for TestEntity
        TableImpl table = forceSH.getTable("TestEntity");
        Assert.assertNotNull(table.getColumnByForceApiName("lastModifiedDate"));
        Assert.assertNull(table.getColumnByForceApiName("unused"));
        // Do the same test with TestEntityMethodAnnotations
        table = forceSH.getTable("TestEntityMethodAnnotations");
        Assert.assertNotNull(table.getColumnByForceApiName("lastModifiedDate"));
        Assert.assertNull(table.getColumnByForceApiName("unused"));
        // Now test the same thing with @Embedded object EmploymentPeriod
        table = forceSH.getTable("EmployeeEntity");
        Assert.assertNotNull(table.getColumnByForceApiName("emp_end__c"));
        Assert.assertNull(table.getColumnByForceApiName("duration__c"));
    }
    
    @Test
    /**
     * Schema not-null field test.
     * Test ensures that not-null fields are created as required. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testNotNullFields() throws Exception {
        testNotNullFieldsInternal(em);
    }
    
    @Test
    /**
     * Schema not-null field test for optimistic transactions enabled.
     * Test ensures that not-null fields are created as required. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testNotNullFieldsOptimistic() throws Exception {
        testNotNullFieldsInternal(em2);
    }

    @Test
    /**
     * Schema not-null field test for all-or-nothing enabled.
     * Test ensures that not-null fields are created as required. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testNotNullFieldsAllOrNothing() throws Exception {
        testNotNullFieldsInternal(em3);
    }

    private static final Pattern TEST_NOT_NULL_FIELDS_PAT =
        Pattern.compile("Required fields are missing: \\[[a-zA-Z0-9_$]*requiredName__c\\]");
    
    public void testNotNullFieldsInternal(EntityManager emm) throws Exception {
        deleteAll(RestrictedTestEntity.class);
        
        Object delegate = emm.getDelegate();
        Assert.assertNotNull(delegate);
        ObjectManagerImpl om = (ObjectManagerImpl) delegate;
        // Search for RestrictedTestEntity
        AbstractClassMetaData cmd =
            ((ForceStoreManager) om.getStoreManager()).getMetaDataManager()
                                    .getMetaDataForEntityName(RestrictedTestEntity.class.getSimpleName());
        Assert.assertFalse(cmd.getMetaDataForMember("requiredName").getColumnMetaData()[0].getAllowsNull());
        Assert.assertTrue(cmd.getMetaDataForMember("optionalName").getColumnMetaData()[0].getAllowsNull());
        // Do some CRUD operations
        RestrictedTestEntity entity = new RestrictedTestEntity();
        EntityTransaction tx = emm.getTransaction();
        tx.begin();
        try {
            emm.persist(entity);
            tx.commit();
            Assert.fail("Should not been able to save RestrictedTestEntity with no name");
        } catch (PersistenceException pe) {
            Assert.assertTrue(TEST_NOT_NULL_FIELDS_PAT.matcher(pe.getMessage()).find());
            tx.rollback();
        }
        tx.begin();
        entity.setRequiredName("One");
        emm.persist(entity);
        tx.commit();
    }
    
    @Test
    /**
     * Schema unique field test.
     * Test ensures that unique fields are created as required. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testUniqueFields() throws Exception {
        testUniqueFieldsInternal(em, false);
    }
    
    @Test
    /**
     * {@see testUniqueFields}. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testUniqueFieldsOptimistic() throws Exception {
        testUniqueFieldsInternal(em2, false);
    }
    
    @Test
    /**
     * {@see testUniqueFields}.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testUniqueFieldsAllOrNothing() throws Exception {
        testUniqueFieldsInternal(em3, true);
    }
    
    private static final Pattern TEST_UNIQUE_FIELDS_PAT =
        Pattern.compile("duplicate value found: [a-zA-Z0-9_$]*requiredName__c duplicates value on record with id");

    public void testUniqueFieldsInternal(EntityManager emm, boolean allOrNothing) throws Exception {
        deleteAll(RestrictedTestEntity.class);
        
        Object delegate = emm.getDelegate();
        Assert.assertNotNull(delegate);
        ObjectManagerImpl om = (ObjectManagerImpl) delegate;
        // Search for RestrictedTestEntity
        AbstractClassMetaData cmd =
            ((ForceStoreManager) om.getStoreManager()).getMetaDataManager()
                                    .getMetaDataForEntityName(RestrictedTestEntity.class.getSimpleName());
        Assert.assertTrue(cmd.getMetaDataForMember("requiredName").getColumnMetaData()[0].getUnique());
        Assert.assertFalse(cmd.getMetaDataForMember("optionalName").getColumnMetaData()[0].getUnique());
        // Do some CRUD operations
        RestrictedTestEntity entity1 = new RestrictedTestEntity();
        entity1.setRequiredName("one");
        RestrictedTestEntity entity2 = new RestrictedTestEntity();
        entity2.setRequiredName("one");
        EntityTransaction tx = emm.getTransaction();
        tx.begin();
        try {
            emm.persist(entity1);
            emm.persist(entity2);
            tx.commit();
            Assert.fail("Should not been able to save RestrictedTestEntity with non-unique name");
        } catch (PersistenceException pe) {
            Assert.assertTrue(TEST_UNIQUE_FIELDS_PAT.matcher(pe.getMessage()).find());
            tx.rollback();
        }
        tx.begin();
        entity2.setRequiredName("two");
        if (allOrNothing) {
            /**
             * In allOrNothing the full transaction is rolled back so both entity1 and entity2 need to be re-attempted.
             * In other cases updated objects are not really rolled back and so only the failed one can be inserted
             */
            emm.persist(entity1);
        }
        emm.persist(entity2);
        tx.commit();
    }
    
    @Test
    /**
     * Schema autonumber field test.
     * Test ensures that autonumber fields work as expected. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testAutonumberFields() throws Exception {
        testAutonumberFieldsInternal(em);
    }
    
    @Test
    /**
     * {@see testAutonumberFields}.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testAutonumberFieldsOptimistic() throws Exception {
        testAutonumberFieldsInternal(em2);
    }
    
    @Test
    /**
     * {@see testAutonumberFields}.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testAutonumberFieldsAllOrNothing() throws Exception {
        testAutonumberFieldsInternal(em3);
    }
    
    public void testAutonumberFieldsInternal(EntityManager emm) throws Exception {
        deleteAll(AutoNumberAndFormulaTestEntity.class);
        
        // Do basic CRUD operations
        AutoNumberAndFormulaTestEntity entity1 = new AutoNumberAndFormulaTestEntity();
        AutoNumberAndFormulaTestEntity entity2 = new AutoNumberAndFormulaTestEntity();
        EntityTransaction tx = emm.getTransaction();
        tx.begin();
        emm.persist(entity1);
        emm.persist(entity2);
        tx.commit();

        tx.begin();
        entity1 = emm.find(AutoNumberAndFormulaTestEntity.class, entity1.getId());
        Assert.assertTrue(entity1.getAutoNum1() > 99);
        entity2 = emm.find(AutoNumberAndFormulaTestEntity.class, entity2.getId());
        tx.commit();
        Assert.assertEquals(entity2.getAutoNum1(), entity1.getAutoNum1() + 1);
        Assert.assertEquals(entity2.getAutoNum2(), entity1.getAutoNum2() + 1);
        Assert.assertEquals(entity2.getAutoNum3(), entity1.getAutoNum3() + 1);
        
        // Now make sure changes to this read only field is silently ignored
        tx.begin();
        entity1.setAutoNum1(500);
        emm.merge(entity1);
        tx.commit();
    }
    
    @Test
    /**
     * Schema formula field test.
     * Test ensures that formula fields work as expected. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testFormulaFields() throws Exception {
        testAutonumberFieldsInternal(em);
    }
    
    @Test
    /**
     * {@see testFormulaFields}.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testFormulaFieldsOptimistic() throws Exception {
        testAutonumberFieldsInternal(em2);
    }
    
    @Test
    /**
     * {@see testFormulaFields}.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testFormulaFieldsAllOrNothing() throws Exception {
        testAutonumberFieldsInternal(em3);
    }
    
    public void testFormulaFieldsInternal(EntityManager emm) throws Exception {
        deleteAll(AutoNumberAndFormulaTestEntity.class);
        
        // Do basic CRUD operations
        AutoNumberAndFormulaTestEntity entity1 = new AutoNumberAndFormulaTestEntity();
        AutoNumberAndFormulaTestEntity entity2 = new AutoNumberAndFormulaTestEntity();
        entity1.setName("entity1");
        entity2.setName("entity2");
        EntityTransaction tx = emm.getTransaction();
        tx.begin();
        emm.persist(entity1);
        emm.persist(entity2);
        tx.commit();

        tx.begin();
        entity1 = emm.find(AutoNumberAndFormulaTestEntity.class, entity1.getId());
        Assert.assertTrue(entity1.getAutoNum1() > 99);
        entity2 = emm.find(AutoNumberAndFormulaTestEntity.class, entity2.getId());
        tx.commit();
        Assert.assertEquals(entity2.getAutoNum1(), entity1.getAutoNum1() + 1);
        Assert.assertEquals(entity1.getNameWithNumber(), entity1.getName() + entity1.getAutoNum1());
        Assert.assertEquals(entity2.getNameWithNumber(), entity2.getName() + entity2.getAutoNum1());
        Assert.assertEquals(entity1.getNextAutoNum1(), entity1.getAutoNum1() + 1);
        Assert.assertEquals(entity2.getNextAutoNum1(), entity2.getAutoNum1() + 1);
        Assert.assertTrue(entity1.getTomorrow().after(new Date()));
        Assert.assertTrue(entity1.getNextBonus().compareTo(BigDecimal.valueOf(51)) == 0
                            || entity1.getNextBonus().compareTo(BigDecimal.valueOf(100)) == 0);
        
        // Now make sure changes to this read only field is silently ignored
        tx.begin();
        entity1.setNameWithNumber("blah1234");
        emm.merge(entity1);
        tx.commit();
    }
    
    @Test
    /**
     * Schema base64 field test.
     * Tests ensures that base64 encoded fields work as expected. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testBase64Fields() throws Exception {
        testBase64FieldsInternal(em);
    }
    
    @Test
    /**
     * {@see testBase64Fields}.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testBase64FieldsOptimistic() throws Exception {
        testBase64FieldsInternal(em2);
    }
    
    @Test
    /**
     * {@see testBase64Fields}.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testBase64FieldsAllOrNothing() throws Exception {
        testBase64FieldsInternal(em3);
    }
    
    public void testBase64FieldsInternal(EntityManager emm) throws Exception {
        // Do some cleanup
        final String folderName = "TestBase64Fields";
        final String documentName = "TestBase64Doc";
        EntityTransaction tx = emm.getTransaction();
        tx.begin();
        emm.createQuery("delete from Document d where d.name = '" + documentName + "'")
            .setHint(QueryHints.EMPTY_RECYCLE_BIN, true).executeUpdate();
        emm.createQuery("delete from Folder f where f.name = '" + folderName + "'").executeUpdate();
        tx.commit();
        
        // Do basic CRUD operations
        FolderEntity folder = new FolderEntity();
        folder.setName(folderName);
        folder.setDeveloperName(folderName);
        folder.setAccessType(com.force.sdk.jpa.entities.FolderEntity.AccessType.Public);
        folder.setType(FolderType.Document);
        
        tx.begin();
        emm.persist(folder);
        tx.commit();

        final byte[] documentData =
            { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
        DocumentEntity document = new DocumentEntity();
        document.setFolder(folder);
        document.setName(documentName);
        document.setBody(documentData);
        document.setIsPublic(true);
        document.setType("fh");
        tx.begin();
        emm.persist(document);
        tx.commit();

        tx.begin();
        List<DocumentEntity> docList =
            emm.createQuery("select d from Document d where d.folder = ?1").setParameter(1, folder).getResultList();
        Assert.assertTrue(docList.size() == 1);
        Assert.assertEquals(docList.get(0).getBody(), documentData);
        tx.commit();
    }
    
    @Test
    /**
     * Entity name test.
     * Test ensures entity name is used for JPQL query but table name is used to create schema. 
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testEntityNaming() {
        deleteAll("EmployeeEntity");
        
        Employee emp = new Employee();
        emp.setSalary(Long.valueOf(100));
        final String name = "Employee testEntityNaming";
        emp.setName(name);
        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(emp);
        tx.commit();

        // Validate that the Entity(name="EmployeeEntity") takes effect
        List<Employee> results = em.createQuery("select t from EmployeeEntity t").getResultList();
        Assert.assertEquals(results.get(0).getName(), name, "Name did not match");
        
        // Validate that we use @Table(name = "EMP__C") in native query
        results =
            em.createNativeQuery("select id, " + getFieldName(em, Employee.class, "name")
                                    + " from " + getTableName(em, Employee.class), Employee.class).getResultList();
        Assert.assertEquals(results.get(0).getName(), name, "Name did not match");
    }
}
