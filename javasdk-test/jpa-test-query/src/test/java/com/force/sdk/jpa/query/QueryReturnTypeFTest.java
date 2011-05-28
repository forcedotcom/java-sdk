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

package com.force.sdk.jpa.query;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import org.testng.annotations.*;

import com.force.sdk.jpa.query.entities.DataTypesFTestEntity;
import com.force.sdk.jpa.query.entities.DataTypesFTestEntity.PickValues;
import com.force.sdk.test.util.BaseJPAFTest;
import com.google.inject.internal.Lists;

/**
 * Tests for Force.com JPA query return types.
 * Also see mocking test in QueryReturnTypeTest.
 *
 * @author Tim Kral
 */
public class QueryReturnTypeFTest extends BaseJPAFTest {
    
    @BeforeClass(dependsOnMethods = "initialize")
    void initTestData() {
        DataTypesFTestEntity entity = new DataTypesFTestEntity();
        entity.setName("QueryReturnTypeFTest");
        
        entity.setBooleanType(true);
        entity.setBooleanObject(true);
        
        entity.setByteType((byte) 1);
        entity.setByteObject((byte) 1);
        
        entity.setBigDecimalObject(new BigDecimal(1.0));
        
        entity.setBigIntegerObject(new BigInteger("1"));

        
        entity.setCharType('a');
        entity.setCharacterObject('a');
        
        
        entity.setDoubleType(1.0d);
        entity.setDoubleObject(1.0d);
        
        entity.setFloatType(1.0f);
        entity.setFloatObject(1.0f);
        
        entity.setIntType(1);
        entity.setIntegerObject(1);
        
        entity.setLongType(1L);
        entity.setLongObject(1L);
        
        entity.setPickValue(PickValues.ONE);
        
        entity.setShortType((short) 1);
        entity.setShortObject((short) 1);
        
        entity.setStringObject("deadbeef");
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(0);
        GregorianCalendar gcal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        gcal.setTimeInMillis(0);
        entity.setDate(cal.getTime());
        entity.setDateTimeCal(cal);
        entity.setDateTimeGCal(gcal);
        // This is not currently supported by the JPA layer
//        entity.setTime(new Time(0));
        
        addTestDataInTx(Lists.newArrayList(entity));
    }
    
    @AfterClass
    public void classTearDown() {
        deleteAll(DataTypesFTestEntity.class.getSimpleName() + " o ", " where name='QueryReturnTypeFTest'");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBooleanReturnType() {
        String query = "select o.booleanType from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Boolean.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), true, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBooleanObjectReturnType() {
        String query = "select o.booleanObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Boolean.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), true, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testByteReturnType() {
        String query = "select o.byteType from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Byte.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), (byte) 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testByteObjectReturnType() {
        String query = "select o.byteObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Byte.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), (byte) 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBigDecimalObjectReturnType() {
        String query = "select o.bigDecimalObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), BigDecimal.class, "Unexpected return type for query: " + query);
        
        BigDecimal expectedReturnValue = new BigDecimal(1.00d).setScale(2);
        assertEquals(results.get(0), expectedReturnValue, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBigIntegerObjectReturnType() {
        String query = "select o.bigIntegerObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), BigInteger.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), BigInteger.valueOf(1L), "Unexpected return value for query: " + query);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCalendarReturnType() {
        String query = "select o.dateTimeCal from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertTrue(results.get(0) instanceof Calendar, "Unexpected return type for query: " + query);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(0);
        assertEquals(results.get(0), cal, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCharReturnType() {
        String query = "select o.charType from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Character.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 'a', "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCharacterObjectReturnType() {
        String query = "select o.characterObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Character.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 'a', "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testDateReturnType() {
        String query = "select o.date from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Date.class, "Unexpected return type for query: " + query);
        
        // Date is always formatted on local time
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0 - TimeZone.getDefault().getRawOffset()); // this is always set in UTC
        assertEquals(results.get(0), cal.getTime(), "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testDoubleReturnType() {
        String query = "select o.doubleType from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Double.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1.0d, "Unexpected return value for query: " + query);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDoubleObjectReturnType() {
        String query = "select o.doubleObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Double.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1.0d, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testFloatReturnType() {
        String query = "select o.floatType from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Float.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1.0f, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testFloatObjectReturnType() {
        String query = "select o.floatObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Float.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1.0f, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testGregorianCalendarReturnType() {
        String query = "select o.dateTimeGCal from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertTrue(results.get(0) instanceof Calendar, "Unexpected return type for query: " + query);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(0);
        assertEquals(results.get(0), cal, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testIntReturnType() {
        String query = "select o.intType from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Integer.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testIntegerObjectReturnType() {
        String query = "select o.integerObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Integer.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLongReturnType() {
        String query = "select o.longType from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Long.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1L, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLongObjectReturnType() {
        String query = "select o.longObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Long.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1L, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testPicklistValueReturnType() {
        String query = "select o.pickValue from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), PickValues.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), PickValues.ONE, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testShortReturnType() {
        String query = "select o.shortType from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Short.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), (short) 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testShortObjectReturnType() {
        String query = "select o.shortObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Short.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), (short) 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testStringObjectReturnType() {
        String query = "select o.stringObject from " + DataTypesFTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), String.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), "deadbeef", "Unexpected return value for query: " + query);
    }
    
    // This is not currently supported by the JPA layer
//    @SuppressWarnings("unchecked")
//    @Test
//    public void testTimeReturnType() {
//        String query = "select o.time from " + DataTypesFTestEntity.class.getSimpleName() + " o";
//        List<Object> results = em.createQuery(query).getResultList();
//        assertEquals(results.get(0).getClass(), Time.class, "Unexpected return type for query: " + query);
//        assertEquals(results.get(0), new Time(0), "Unexpected return value for query: " + query);
//    }
}
