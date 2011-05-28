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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import javax.persistence.Query;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.JPATestUtils.Digit;
import com.force.sdk.jpa.entities.ParentTestEntity;
import com.force.sdk.jpa.entities.TestEntity;

/**
 * 
 * Tests for DELETE queries using all supported data types.
 *
 * @author Dirk Hain
 */
public class DeleteDatatypesTest extends DatatypesBaseTest {
    
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
    
    @DataProvider
    public Object[][] deleteData() throws NumberFormatException, MalformedURLException {
        Object [][] entities = new Object[][]{
                {new TestEntity(), 10, 0},
                {new ParentTestEntity(), 2, 0},
        };
        return entities;
    }
    
    @DataProvider
    public Object[][] deleteWhereData(Method test) throws NumberFormatException, MalformedURLException {
        Object [][] primitiveVals = new Object[][]{
                {new TestEntity(), "boolType",  Boolean.TRUE, 10, 0},
        };
        Object [][] primitiveNumberVals = new Object[][]{
                {new TestEntity(), "shortType",  (short) 0, 10, 9},
                {new TestEntity(), "intType",    0, 10, 9},
                {new TestEntity(), "longType",   (long) 0, 10, 9},
                {new TestEntity(), "doubleType", (double) 0, 10, 9},
                {new TestEntity(), "floatType",  (float) 0, 10, 9},
                {new TestEntity(), "charType",   'A', 10, 9},
        };
        
        Object[][] objectNumberVals = new Object[][]{
                {new TestEntity(), "shortObject",    Short.valueOf((short) 0), 10, 9},
                {new TestEntity(), "integerObject",  Integer.valueOf(0), 10, 9},
                {new TestEntity(), "longObject",     Long.valueOf(0), 10, 9},
                {new TestEntity(), "doubleObject",   new Double(0), 10, 9},
                {new TestEntity(), "floatObject",    new Float(0), 10, 9},
                {new TestEntity(), "characterObject", 'A', 10, 9},
                {new TestEntity(), "bigDecimalObject", new BigDecimal(0), 10, 9},
                {new TestEntity(), "bigIntegerObject", BigInteger.valueOf(0), 10, 9},
                {new TestEntity(), "percent",        0, 10, 9},
        };
        
        Object [][] stringVals = new Object[][]{
                {new TestEntity(), "stringObject",   Digit.AZERO.toString(), 10, 9},
                {new TestEntity(), "phone",          "415-123-0000", 10, 9},
                {new TestEntity(), "email",          "0foobar@salesforce.com", 10, 9},
                {new TestEntity(), "url",            new URL("http://localhost:0000"), 10, 9},
                {new TestEntity(), "characterObject", 'A', 10, 9},
        };
        
        Object [][] objectVals = new Object[][]{
//TODO: verify what the behavior for Master objects should be                
//                {new TestEntity(), "parent",              parent, 10, 0},
//                {new TestEntity(), "parentMasterDetail",  parentMD, 10, 0},
                {new TestEntity(), "booleanObject",  Boolean.FALSE, 10, 10},
                {new TestEntity(), "byteObject",     Byte.valueOf("0"), 10, 9},
                {new TestEntity(), "date",           getCal(false, Digit.AZERO, false).getTime(), 10, 0},
                {new TestEntity(), "dateTimeCal",    getCal(false, Digit.AZERO, true), 10, 9},
                {new TestEntity(), "dateTimeGCal",   getCal(true, Digit.AZERO, true), 10, 9},
                {new TestEntity(), "dateTemporal",   getCal(false, Digit.AZERO, false).getTime(), 10, 0},
        };
        
        if (test.getName().contains("AllTypes")) {
            return concat(objectVals, primitiveVals, primitiveNumberVals, objectNumberVals, stringVals);
        } else {
            return null;
        }
    }
    
    
    @Test(dataProvider = "deleteData")
    /**
     * Parameterized test case to delete all objects of specified type.
     * Using JPQL query, delete all objects of the type (sent as parameter) and verify that no instance is left.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults No objects of type TestEntity and ParentTestEntity are left in store.
     */
    public <T> void testDeleteAll(T queryObj, int before, int after) {
        
        verifyTestSetup(queryObj.getClass().getSimpleName(), before);
        
        String querySelect = "select o from " + queryObj.getClass().getSimpleName() + " o";
        Assert.assertEquals(em.createQuery(querySelect).getResultList().size(), before, "Test setup incorrect ");
        
        String queryDeleteAll = "delete from " + queryObj.getClass().getSimpleName();
        Assert.assertEquals(em.createQuery(queryDeleteAll).executeUpdate(), before - after,
                "Number of modified entities does not match ");
        Assert.assertEquals(em.createQuery(querySelect).getResultList().size(), after, "Remaining entity number incorrect ");
    }
    
    @Test(dataProvider = "deleteWhereData")
    /**
     * Delete objects of specified type and condition (specified in query using named parameter) and verify final count.
     * Using JPQL query and named parameter for specifying condition, delete all objects of the type (sent as parameter)
     * and verify that counts before and after the operation are correct (also specified as parameters before and after).
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Count of objects is correct before and after the delete operation (specified as parameters).
     */
    public <T, W> void testDeleteWhereNamedParamAllTypes(T fromObj, String typeName, W whereObj, int before, int after) {
        String fromObjName = fromObj.getClass().getSimpleName();
        verifyTestSetup(fromObjName, before);
        String selectBase = "select o from " + fromObjName + " o";
        String deleteBase = "delete from " + fromObjName + " o where o." + typeName + "= :typeval";
        //Query select = em.createQuery(selectBase).setParameter("typeval", whereObj);
        Query delete = em.createQuery(deleteBase).setParameter("typeval", whereObj);
        Assert.assertEquals(delete.executeUpdate(), before - after, "Number of deleted entities is incorrect");
        Assert.assertEquals(em.createQuery(selectBase).getResultList().size(), after,
                "Incorrect number of entities after delete");
    }
    
    @Test(dataProvider = "deleteWhereData")
    /**
     * Delete objects of specified type and condition (specified in query using position parameter) and verify count.
     * Using JPQL query and position parameter for specifying condition, delete all objects of the type (sent as parameter)
     * and verify that counts before and after the operation are correct (also specified as parameters before and after).
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Count of objects is correct before and after the delete operation (specified as parameters).
     */
    public <T, W> void testDeleteWherePosParamAllTypes(T fromObj, String typeName, W whereObj, int before, int after) {
        String fromObjName = fromObj.getClass().getSimpleName();
        verifyTestSetup(fromObjName, before);
        String selectBase = "select o from " + fromObjName + " o";
        String deleteBase = "delete from " + fromObjName + " o where o." + typeName + "=?1";
        //Query select = em.createQuery(selectBase).setParameter(1, whereObj);
        Query delete = em.createQuery(deleteBase).setParameter(1, whereObj);
        Assert.assertEquals(delete.executeUpdate(), before - after, "Number of deleted entities is incorrect");
        Assert.assertEquals(em.createQuery(selectBase).getResultList().size(), after,
                "Incorrect number of entities after delete");
    }

    @Test
    /**
     * Delete parent of multiple children and verify that children are not deleted.
     * Delete an object of ParentTestEntity with 10 children, verify that only one object gets deleted and 10 objects of
     * TestEntity remain in store.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults One ParentTestEntity gets deletec, 10 TestEntity objects are still present.
     */
    public void testDeleteParent() {
        //delete parent of multiple children and verify children are NOT deleted.
        String deleteBase = "delete from " + ParentTestEntity.class.getSimpleName() + " p where p.id=\"" + parent.getId() + "\"";
        String selectBase = "select o from " + TestEntity.class.getSimpleName() + " o";
        int delete = em.createQuery(deleteBase).executeUpdate();
        Assert.assertEquals(delete, 1, "Wrong number of delete entities when deleting a parent entity.");
        Assert.assertEquals(em.createQuery(selectBase).getResultList().size(), 10,
                "Child entities were deleted when deleting a parent.");
    }

    @Test
    /**
     * Delete parent of Master-Child children and verify that children are deleted.
     * Delete an object of ParentTestEntity which has Parent-Child children, verify that more than one object gets
     * deleted and no TestEntity is left in store.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults No TestEntity objects are left in store.
     */
    public void testDeleteMaster() {
        //delete master of master-detail and verify children are deleted.
        String deleteBase =
            "delete from " + ParentTestEntity.class.getSimpleName() + " p where p.id=\"" + parentMD.getId() + "\"";
        String selectBase = "select o from " + TestEntity.class.getSimpleName() + " o";
        int delete = em.createQuery(deleteBase).executeUpdate();
        Assert.assertEquals(delete, 1, "Wrong number of delete entities when deleting a parent entity.");
        Assert.assertEquals(em.createQuery(selectBase).getResultList().size(), 0,
                "Detail entities were not deleted when deleting the master.");
    }
    
    private void verifyTestSetup(String fromObj, int expected) {
        String callMethod = Thread.currentThread().getStackTrace()[2].getMethodName();
        String selectBase = "select o from " + fromObj + " o";
        Assert.assertEquals(em.createQuery(selectBase).getResultList().size(), expected,
                "Test setup incorrect for " + callMethod);
    }
        
}
