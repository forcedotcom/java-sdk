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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.DataTypesTestEntity;
import com.force.sdk.jpa.entities.DataTypesTestEntity.PickValues;
import com.google.inject.internal.Lists;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.types.Time;

/**
 * Tests for Force.com JPA query return types.
 *
 * @author Tim Kral
 */
public class QueryReturnTypeTest extends BaseJPAQueryTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBooleanReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("booleanType__c", "true");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.booleanType__c from datatypestestentity__c o ");
        
        String query = "select o.booleanType from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Boolean.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), true, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBooleanObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("booleanObject__c", "true");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.booleanObject__c from datatypestestentity__c o ");
        
        String query = "select o.booleanObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Boolean.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), true, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testByteReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("byteType__c", "1");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.byteType__c from datatypestestentity__c o ");
        
        String query = "select o.byteType from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Byte.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), (byte) 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testByteObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("byteObject__c", "1");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.byteObject__c from datatypestestentity__c o ");
        
        String query = "select o.byteObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Byte.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), (byte) 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBigDecimalObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("bigDecimalObject__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.bigDecimalObject__c from datatypestestentity__c o ");
        
        String query = "select o.bigDecimalObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), BigDecimal.class, "Unexpected return type for query: " + query);
        
        BigDecimal expectedReturnValue = new BigDecimal(1.00d).setScale(2);
        assertEquals(results.get(0), expectedReturnValue, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBigIntegerObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("bigIntegerObject__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.bigIntegerObject__c from datatypestestentity__c o ");
        
        String query = "select o.bigIntegerObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), BigInteger.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), BigInteger.valueOf(1L), "Unexpected return value for query: " + query);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCalendarReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("dateTimeCal__c", "1970-01-01T00:00:00.000Z");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.dateTimeCal__c from datatypestestentity__c o ");
        
        String query = "select o.dateTimeCal from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertTrue(results.get(0) instanceof Calendar, "Unexpected return type for query: " + query);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(0);
        assertEquals(results.get(0), cal, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCharReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("charType__c", "a");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.charType__c from datatypestestentity__c o ");
        
        String query = "select o.charType from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Character.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 'a', "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCharacterObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("characterObject__c", "a");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.characterObject__c from datatypestestentity__c o ");
        
        String query = "select o.characterObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Character.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 'a', "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testDateReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("date__c", "1970-01-01");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.date__c from datatypestestentity__c o ");
        
        String query = "select o.date from " + DataTypesTestEntity.class.getSimpleName() + " o";
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
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("doubleType__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.doubleType__c from datatypestestentity__c o ");
        
        String query = "select o.doubleType from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Double.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1.0d, "Unexpected return value for query: " + query);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDoubleObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("doubleObject__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.doubleObject__c from datatypestestentity__c o ");
        
        String query = "select o.doubleObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Double.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1.0d, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testFloatReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("floatType__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.floatType__c from datatypestestentity__c o ");
        
        String query = "select o.floatType from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Float.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1.0f, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testFloatObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("floatObject__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.floatObject__c from datatypestestentity__c o ");
        
        String query = "select o.floatObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Float.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1.0f, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testGregorianCalendarReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("dateTimeGCal__c", "1970-01-01T00:00:00.000Z");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.dateTimeGCal__c from datatypestestentity__c o ");
        
        String query = "select o.dateTimeGCal from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertTrue(results.get(0) instanceof Calendar, "Unexpected return type for query: " + query);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(0);
        assertEquals(results.get(0), cal, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testIntReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("intType__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.intType__c from datatypestestentity__c o ");
        
        String query = "select o.intType from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Integer.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testIntegerObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("integerObject__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.integerObject__c from datatypestestentity__c o ");
        
        String query = "select o.integerObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Integer.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLongReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("longType__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.longType__c from datatypestestentity__c o ");
        
        String query = "select o.longType from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Long.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1L, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testLongObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("longObject__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.longObject__c from datatypestestentity__c o ");
        
        String query = "select o.longObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Long.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), 1L, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testPicklistValueReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("pickValue__c", "ONE");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.pickValue__c from datatypestestentity__c o ");
        
        String query = "select o.pickValue from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), PickValues.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), PickValues.ONE, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testShortReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("shortType__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.shortType__c from datatypestestentity__c o ");
        
        String query = "select o.shortType from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Short.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), (short) 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testShortObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("shortObject__c", "1.0");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.shortObject__c from datatypestestentity__c o ");
        
        String query = "select o.shortObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Short.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), (short) 1, "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testStringObjectReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("stringObject__c", "deadbeef");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.stringObject__c from datatypestestentity__c o ");
        
        String query = "select o.stringObject from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), String.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), "deadbeef", "Unexpected return value for query: " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testTimeReturnType() {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        // Time objects stored as FieldType.datetime (see PersistenceUtils.setFieldType)
        sobject.setField("time__c", "1970-01-01T00:00:00.000Z");
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.time__c from datatypestestentity__c o ");
        
        String query = "select o.time from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), Time.class, "Unexpected return type for query: " + query);
        
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(0);
        assertEquals(results.get(0), new Time(cal), "Unexpected return value for query: " + query);
    }
    
    @DataProvider
    protected Object[][] urlProvider() {
        return new Object[][] {
                {"http://www.deadbeef.com", "http://www.deadbeef.com"},
                {"https://www.deadbeef.com", "https://www.deadbeef.com"},
                {"www.deadbeef.com", "http://www.deadbeef.com"},
                {"://www.deadbeef.com", "http://www.deadbeef.com"},
                {"/www.deadbeef.com", "http://www.deadbeef.com"},
        };
    }
    
    @SuppressWarnings("unchecked")
    @Test(dataProvider = "urlProvider")
    public void testURLReturnType(String url, String expectedUrl) throws MalformedURLException {
        SObject sobject = createSObject("DataTypesTestEntity__c");
        sobject.setField("url__c", url);
        
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.url__c from datatypestestentity__c o ");
        
        String query = "select o.url from " + DataTypesTestEntity.class.getSimpleName() + " o";
        List<Object> results = em.createQuery(query).getResultList();
        assertEquals(results.get(0).getClass(), URL.class, "Unexpected return type for query: " + query);
        assertEquals(results.get(0), new URL(expectedUrl), "Unexpected return value for query: " + query);
    }
    
}
