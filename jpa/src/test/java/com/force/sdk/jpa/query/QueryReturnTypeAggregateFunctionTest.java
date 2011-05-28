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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.DataTypesTestEntity;
import com.google.inject.internal.Lists;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Tests for Force.com JPA query return types with aggregate functions.
 *
 * @author Nawab Iqbal
 * @author Tim Kral
 */
public class QueryReturnTypeAggregateFunctionTest extends BaseJPAQueryTest {

    @DataProvider
    public Object[][] avgFunctionProvider() {
        return new Object[][]{
                {"avg(o.intType)", Double.class},
                {"avg(o.shortType)", Double.class},
                {"avg(o.longType)", Double.class},
                {"avg(o.floatType)", Double.class},
                {"avg(o.doubleType)", Double.class},
                {"avg(o.bigIntegerObject)", Double.class},
                {"avg(o.bigDecimalObject)", Double.class},
        };
    }
    
    @Test(dataProvider = "avgFunctionProvider")
    public void testAvgReturnType(String avgFunction, Class<Object> expectedReturnType) {
        SObject sobject = createSObject("AggregateResult");
        sobject.setField("expr0", 1.0d);
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        
        String query = "select " + avgFunction + " from " + DataTypesTestEntity.class.getSimpleName() + " o";
        Object result = em.createQuery(query).getSingleResult();
        assertEquals(result.getClass(), expectedReturnType, "Unexpected return type for query: " + query);
    }
    
    @DataProvider
    public Object[][] maxFunctionProvider() {
        return new Object[][]{
                {"max(o.intType)", Integer.class},
                {"max(o.shortType)", Short.class},
                {"max(o.longType)", Long.class},
                {"max(o.floatType)", Float.class},
                {"max(o.doubleType)", Double.class},
                {"max(o.bigIntegerObject)", BigInteger.class},
                {"max(o.bigDecimalObject)", BigDecimal.class},
        };
    }
    
    @Test(dataProvider = "maxFunctionProvider")
    public void testMaxReturnType(String maxFunction, Class<Object> expectedReturnType) {
        SObject sobject = createSObject("AggregateResult");
        sobject.setField("expr0", 1.0d);
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        
        String query = "select " + maxFunction + " from " + DataTypesTestEntity.class.getSimpleName() + " o";
        Object result = em.createQuery(query).getSingleResult();
        assertEquals(result.getClass(), expectedReturnType, "Unexpected return type for query: " + query);
    }
    
    @DataProvider
    public Object[][] minFunctionProvider() {
        return new Object[][]{
                {"min(o.intType)", Integer.class},
                {"min(o.shortType)", Short.class},
                {"min(o.longType)", Long.class},
                {"min(o.floatType)", Float.class},
                {"min(o.doubleType)", Double.class},
                {"min(o.bigIntegerObject)", BigInteger.class},
                {"min(o.bigDecimalObject)", BigDecimal.class},
        };
    }
    
    @Test(dataProvider = "minFunctionProvider")
    public void testMinReturnType(String minFunction, Class<Object> expectedReturnType) {
        SObject sobject = createSObject("AggregateResult");
        sobject.setField("expr0", 1.0d);
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        
        String query = "select " + minFunction + " from " + DataTypesTestEntity.class.getSimpleName() + " o";
        Object result = em.createQuery(query).getSingleResult();
        assertEquals(result.getClass(), expectedReturnType, "Unexpected return type for query: " + query);
    }
    
    @DataProvider
    public Object[][] sumFunctionProvider() {
        return new Object[][]{
                {"sum(o.intType)", Long.class},
                {"sum(o.shortType)", Long.class},
                {"sum(o.longType)", Long.class},
                {"sum(o.floatType)", Double.class},
                {"sum(o.doubleType)", Double.class},
                {"sum(o.bigIntegerObject)", BigInteger.class},
                {"sum(o.bigDecimalObject)", BigDecimal.class},
        };
    }
    
    @Test(dataProvider = "sumFunctionProvider")
    public void testSumReturnType(String sumFunction, Class<Object> expectedReturnType) {
        SObject sobject = createSObject("AggregateResult");
        sobject.setField("expr0", 1.0d);
                
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        
        String query = "select " + sumFunction + " from " + DataTypesTestEntity.class.getSimpleName() + " o";
        Object result = em.createQuery(query).getSingleResult();
        assertEquals(result.getClass(), expectedReturnType, "Unexpected return type for query: " + query);
    }
    
    @DataProvider
    public Object[][] countFunctionProvider() {
        return new Object[][]{
                {"count(o.intType)", Long.class},
                {"count(o.integerObject)", Long.class},
        };
    }
    
    @Test(dataProvider = "countFunctionProvider")
    public void testCountReturnType(String countFunction, Class<Object> expectedReturnType) {
        QueryResult qr = new QueryResult();
        qr.setDone(true);
        qr.setSize(2); // Count queries return a size, but no results
        
        mockQueryConn.setReturnedQueryResult(qr);
        
        String query = "select " + countFunction + " from " + DataTypesTestEntity.class.getSimpleName() + " o";
        Object result = em.createQuery(query).getSingleResult();
        assertEquals(result.getClass(), expectedReturnType, "Unexpected return type for query: " + query);
    }
}
