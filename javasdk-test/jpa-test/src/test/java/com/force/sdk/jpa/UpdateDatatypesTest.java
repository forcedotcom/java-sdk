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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import javax.persistence.PersistenceException;

import junit.framework.Assert;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.ParentTestEntity;
import com.force.sdk.jpa.entities.TestEntity;

/**
 * 
 * Tests for UPDATE queries using all supported data types.
 *
 * @author Dirk Hain
 */
public class UpdateDatatypesTest extends DatatypesBaseTest {

    /**
     * Modifying operations require setting up data after every method call.
     */
    @BeforeMethod
    public void reloadTestData() {
        final String queryBase = "select o from " + TestEntity.class.getSimpleName() + " o ";
        final String queryBaseParent = "select o from " + ParentTestEntity.class.getSimpleName() + " o ";
        if (em.createQuery(queryBase).getResultList().size() != 10
                || em.createQuery(queryBaseParent).getResultList().size() != 2) {
            initTestData();
        }
    }
    
    /**
     * Modifying operations require cleaning the org after every method call.
     */
    @AfterMethod
    @Override
    public void classTearDown() {
        super.classTearDown();
    }
    
    @DataProvider
    public Object[][] updateData() throws NumberFormatException, MalformedURLException {
        Object [][] entities = new Object[][]{
                {new TestEntity(), "shortType",     (short) 0, (short) 77},
                {new TestEntity(), "intType",       0, 77},
                {new TestEntity(), "longType",      (long) 0, (long) 77},
                {new TestEntity(), "doubleType",    (double) 0, (double) 77},
                {new TestEntity(), "floatType",     (float) 0, (float) 77},
                {new TestEntity(), "charType",    'A', '7'},
                
                {new TestEntity(), "shortObject",       Short.valueOf((short) 0), Short.valueOf((short) 77)},
                {new TestEntity(), "integerObject",     Integer.valueOf(0), Integer.valueOf(77)},
                {new TestEntity(), "longObject",        Long.valueOf(0), Long.valueOf(77)},
                {new TestEntity(), "doubleObject",      new Double(0), new Double(77)},
                {new TestEntity(), "floatObject",       new Float(0), new Float(77)},
                {new TestEntity(), "characterObject",   'A', '7'},
                {new TestEntity(), "bigDecimalObject",  new BigDecimal(0), new BigDecimal(77)},
                {new TestEntity(), "bigIntegerObject",  BigInteger.valueOf(0), BigInteger.valueOf(77)},
                {new TestEntity(), "percent",           0, (77)},
                {new TestEntity(), "stringObject",      "0", "77"},
                {new TestEntity(), "phone",             "415-123-0000", "415-123-0007"},
                {new TestEntity(), "email",             "0foobar@salesforce.com", "77foobar@salesforce.com"},
                {new TestEntity(), "url",               new URL("http://localhost:0000"), new URL("http://localhost:7000")},
                {new TestEntity(), "characterObject",   'A', '7'},
                {new TestEntity(), "booleanObject",     Boolean.TRUE, Boolean.FALSE},
                
                {new ParentTestEntity(), "name",        "parent entity", "updated name"},
        };
        return entities;
    }
        
    @Test
    /**
     * Basic bulk UPDATE JPQL query test.
     * Bulk update is currently not supported by the javasdk and this test asserts the exception.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testUpdateAll() {
        String updateBase = "update TestEntity t SET t.boolType='false'";
        try {
            em.createQuery(updateBase).executeUpdate();
            Assert.fail("Bulk update should have caused an exception.");
        } catch (PersistenceException pe) {
            Assert.assertTrue("Exception message was wrong.", pe.getMessage().contains("Bulk Update is not yet supported"));
        }
        
    }
    
    //Bulk update is not yet supported by the sdk.
//    @Test(dataProvider="updateData")
    @SuppressWarnings("unchecked")
    /**
     * Update test for all datatypes.
     * Tests for JPQL updates of all data types.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public <T, P> void testUpdateWhere(T updateObj, String typename, P whereVal, P updateVal)
    throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        String updateObjName = updateObj.getClass().getSimpleName();
        String updateBase = "UPDATE " + updateObjName + " o "
                            + "SET o." + typename + "='" + updateVal + "' "
                            + "WHERE o." + typename + "='" + whereVal + "'";
        Assert.assertEquals("Updated wrong number of entities", 1, em.createQuery(updateBase).executeUpdate());
        
        String selectBase = "select o from " + updateObjName + " o where o." + typename + "=" + updateVal; //whereVal;
        Assert.assertEquals("Update was not successful", 1, em.createQuery(selectBase).getResultList().size());
        T updatedEntity = (T) em.createQuery(selectBase).getResultList().get(0);
        Method getter =
            updatedEntity.getClass().getMethod("get" + typename.substring(0, 1).toUpperCase() + typename.substring(1));
        Assert.assertEquals("Property was not updated correctly.", updateVal, getter.invoke(updatedEntity, new Object[0]));
    }
    
}
