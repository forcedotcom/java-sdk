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

import java.util.List;

import javax.persistence.*;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.JPATestUtils.Digit;
import com.force.sdk.jpa.entities.*;
import com.force.sdk.qa.util.jpa.BaseJPAFTest;

/**
 * 
 * Tests related to JPQL constructor expression. 
 * Select new com.salesforce.persistance.jpa.TestEntity() FROM TestEntity e
 * 
 * @author Nawab Iqbal
 */

public class ConstructorExpressionTest extends BaseJPAFTest {
    ParentTestEntity parent;
    ParentTestEntity parentMD;

    @BeforeClass(dependsOnMethods = "initialize")
    protected void init() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);
        deleteAll(TestEntitySummary.class);
    }
    
    @AfterClass
    protected void classTearDown() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);
        deleteAll(TestEntitySummary.class);
    }
    
    @BeforeClass(dependsOnMethods = "init")
    protected void initTestData() {
        EntityTransaction tx = em.getTransaction();
        
        try {
            tx.begin();
            parent = new ParentTestEntity();
            parent.init();
            em.persist(parent);
            parentMD = new ParentTestEntity();
            parentMD.init();
            em.persist(parentMD);

            Digit[] digits = {Digit.AZERO, Digit.ONE, Digit.TWO, Digit.THREE, Digit.THREE, Digit.THREE};
            for (Digit d : digits) {
                TestEntity entity = new TestEntity();
                JPATestUtils.initializeTestEntity(entity, d);
                entity.setParent(parent);
                entity.setParentMasterDetail(parentMD);
                em.persist(entity);
            }

            em.flush();
            tx.commit();
            tx = null;
        } finally {
            if (tx != null) {
                tx.rollback();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    /**
     * Test ctor expression on a non-entity object with multiple parameters.
     * beforeClass method creates and persists some TestEntity instances,
     * Create a non-entity object from different fields of the TestEntity object.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Verify the values of populated fields on TestNonEntity object.
     */
    public void testMultiParamConstructor() {
        String queryText =
            "Select new com.force.sdk.jpa.entities.TestNonEntity(o.shortType, o.doubleObject, "
                                                                    + "o.pickValueMultiDef, o.pickValueMulti) "
                + "from TestEntity o where o.name = 'ONE'";
        Query query = em.createQuery(queryText);
        List<TestNonEntity> list = query.getResultList();
        Assert.assertEquals(list.size(), 1, "TestNotEntity instance was not read successfully.");
        TestNonEntity s = list.get(0);
        Assert.assertNotNull(s, "TestNonEntity instance is null.");
        Assert.assertEquals(s.doubleObject, new Double(1), "Double value is not correct.");
        Assert.assertEquals(s.shortType, (short) 1, "short value is not correct.");
        // todo: Asserts for pick values.
    }

    @Test
    /**
     * Negative test for defalut constructor.
     * Test default constructor -- without any parameter --,
     * this will cause exception which is OK as it's meaningless to not select any value into the constructor.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults PersistenceException should be thrown.
     */
    public void testDefaultConstructor() {
        String queryText = "Select new com.force.sdk.jpa.entities.TestNonEntity() from TestEntity o";
        try {
            em.createQuery(queryText).getResultList();
            Assert.fail("Cannot make a query with 0 columns selected");
        } catch (PersistenceException pe) {
            Assert.assertTrue(pe.getMessage().contains("MALFORMED_QUERY"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    /**
     * Test constructor expression which results in multiple rows.
     * beforeClass method persists multiple TestEntity instances,
     * query some of them into an array using constructor expression populating the name only.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Three objects in the retrieved list with correct value for parameter (name).
     */
    public void testArrayConstructor() {
        String queryText = "Select new com.force.sdk.jpa.entities.TestNonEntity(o.name) from TestEntity o where o.name = 'THREE'";
        Query query = em.createQuery(queryText);
        List<TestNonEntity> list = query.getResultList();
        Assert.assertEquals(list.size(), 3, "TestNotEntity instance was not created successfully.");

        for (TestNonEntity s : list) {
            Assert.assertNotNull(s, "TestNonEntity instance is null.");
            Assert.assertEquals(s.name, "THREE", "Name is not correct.");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    /**
     * Verify constructor by passing entity id.
     * Test constructor expression by passing entity id as parameter and select one row into the entity object.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Name and id are valid on the retrieved row.
     */
    public void testEntityConstructorId() {
        String queryText = "Select new com.force.sdk.jpa.entities.TestEntitySummary(o.id) from TestEntity o where o.name = 'ONE'";
        Query query = em.createQuery(queryText);
        List<TestEntitySummary> list = query.getResultList();
        Assert.assertEquals(list.size(), 1, "TestEntitySummary instance was not created.");
        TestEntitySummary s = list.get(0);
        Assert.assertNotNull(s, "TestEntitySummary instance is null.");
        Assert.assertNotNull(s.getName(), "Name is null.");
        Assert.assertEquals(s.getName().length(), 18, "id should be 18 character long.");
    }

    @SuppressWarnings("unchecked")
    @Test
    /**
     * ctor expr with lookup and master-detail parameters.
     * Test constructor expression for TestEntity with masterdetail and lookup parameters,
     * then persist the object without changing the name.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Count of objects with the same name should increment by 1.
     */
    public void testTestEntityConstructorWithAssociation() {

        String queryText =
            "Select new com.force.sdk.jpa.entities.TestEntity(o.name, o.parent, o.parentMasterDetail) "
                + "from TestEntity o where o.name = 'TWO'";
        Query query = em.createQuery(queryText);
        List<TestEntity> list = query.getResultList();
        int originalSize = list.size();
        TestEntity s = list.get(0);
        s.setParentMasterDetail(parentMD);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(s);
        tx.commit();
        
        queryText = "Select o from TestEntity o where o.name = 'TWO'";
        query = em.createQuery(queryText);
        list = query.getResultList();
        
        Assert.assertEquals(list.size(), originalSize + 1, "TestEntity instance was not persisted successfully.");
    }

    @SuppressWarnings("unchecked")
    @Test
    /**
     * ctor expr with string parameter.
     * beforeClass persists TestEntity instances. Query some of them using constructor expression with o.name as parameter.
     * then add the necessary associations and persist. Querying the database again on same name should return one item
     * more than before.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Count of objects with the same name should increment by 1.
     */
    public void testTestEntityConstructor() {

        String queryText = "Select new com.force.sdk.jpa.entities.TestEntity(o.name) from TestEntity o where o.name = 'TWO'";
        Query query = em.createQuery(queryText);
        List<TestEntity> list = query.getResultList();
        int originalSize = list.size();
        TestEntity s = list.get(0);
        s.setParent(parent);
        s.setParentMasterDetail(parentMD);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(s);
        tx.commit();
        
        queryText = "Select o from TestEntity o where o.name = 'TWO'";
        query = em.createQuery(queryText);
        list = query.getResultList();
        
        Assert.assertEquals(list.size(), originalSize + 1, "TestEntity instance was not persisted successfully.");
    }

    @SuppressWarnings("unchecked")
    @Test
    /**
     * ctor expr with int parameter.
     * beforeClass persists some TestEntity instances. Query one of them into TestEntitySummary object using an integer
     * parameter. Persist the object and verify that it is persisted properly.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults A valid TestEntitySummary object should be present in store.
     */
    public void testEntityConstructorAndPersist() {

        String queryText =
            "Select new com.force.sdk.jpa.entities.TestEntitySummary(o.intType) from TestEntity o where o.name = 'ONE'";
        Query query = em.createQuery(queryText);
        List<TestEntitySummary> list = query.getResultList();
        TestEntitySummary s = list.get(0);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(s);
        tx.commit();
                
        queryText = "Select o from TestEntitySummary o where o.intType = 1";
        query = em.createQuery(queryText);
        list = query.getResultList();
        
        Assert.assertEquals(list.size(), 1, "TestEntitySummary instance was not retrieved successfully.");
        s = list.get(0);
        Assert.assertNotNull(s, "TestEntitySummary instance was not persisted correctly.");
        Assert.assertEquals(s.getIntType(), 1, "Integer value is not correct.");
    }
}
