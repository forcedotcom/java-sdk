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

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.JPATestUtils.Digit;
import com.force.sdk.jpa.entities.ParentTestEntity;
import com.force.sdk.jpa.entities.TestEntity;
import com.force.sdk.qa.util.BaseJPAFTest;

/**
 * 
 * Tests for querying a basic entity with all datatypes.
 *
 * @author Dirk Hain
 */
public class DatatypesBaseTest extends BaseJPAFTest {
    protected ParentTestEntity parent;
    protected ParentTestEntity parentMD;
    
    @BeforeClass(dependsOnMethods = "initialize")
    protected void init() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);
    }

    @AfterClass
    protected void classTearDown() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);
    }

    @BeforeClass(dependsOnMethods = "init")
    protected void initTestData() {
        classTearDown();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            parent = new ParentTestEntity();
            parent.init();
            em.persist(parent);
            parentMD = new ParentTestEntity();
            parentMD.init();
            em.persist(parentMD);
            for (Digit d : Digit.values()) {
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
    
    @Test
    /**
     * Verifies that test data has been persisted.
     * Queries TestEntity and ParentTestEntity objects and verifies that the count is correct (10 and 2 respectively).
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Count of TestEntity and ParentTestEntity instances should be correct.
     */
    public void testSetup() {
        final String queryBase = "select o from " + TestEntity.class.getSimpleName() + " o ";
        Assert.assertEquals(em.createQuery(queryBase).getResultList().size(), 10, "Setup did not create all TestEntity objects.");
        final String queryBaseParent = "select o from " + ParentTestEntity.class.getSimpleName() + " o ";
        Assert.assertEquals(em.createQuery(queryBaseParent).getResultList().size(), 2, "Wrong number of parent objects");
    }

    
    /**
     * Helper function to generate Calendar objects for query predicates.
     * @param isGregorian boolean indicating if this Calendar should be of type {@link GregorianCalendar}
     * @param d Digit representing the expected initialization value of the Calendar
     * @param time boolean indicating if this Calendar should have a time stamp
     */
    public static Calendar getCal(boolean isGregorian, Digit d, boolean time) {
        Calendar cal;
        if (isGregorian) {
            cal = new GregorianCalendar();
        } else {
            cal = Calendar.getInstance();
        }
        cal.set(2010, 1, 1);
        if (time) {
            cal.set(Calendar.HOUR_OF_DAY, d.value);
            cal.set(Calendar.MINUTE, d.value);
            cal.set(Calendar.SECOND, d.value);
            cal.set(Calendar.MILLISECOND, d.value); // We do not preserve ms resolution
        }
        return cal;
    }

    
    /**
     * Concatenate n two-dimensional object arrays into one two-dimensional object array.
     * @return Object[][] combo
     */
    public static Object[][] concat(Object[][]... arrs) {
        if (arrs == null) {
            throw new IllegalArgumentException("Illigal argument NULL.");
        }
        Object[][] first = arrs[0];
        int rows = 0;
        for (Object[][] array : arrs) {
            if (array == null || array.length == 0 || array[0] == null || array[0].length != first[0].length) {
                throw new IllegalArgumentException("Arrays cannot be null and need to have the same number of columns.");
            }
            rows += array.length;
        }
        Object [][] combo = new Object[rows][first[0].length];
        int index = 0;
        for (Object[][] array : arrs) {
            for (Object[] row : array) {
                System.arraycopy(row, 0, combo[index], 0, row.length);
                index++;
            }
        }
        return combo;
    }

}
