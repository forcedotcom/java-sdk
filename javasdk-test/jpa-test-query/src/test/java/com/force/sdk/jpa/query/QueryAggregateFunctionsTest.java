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

import java.math.*;
import java.net.MalformedURLException;
import java.util.*;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.query.entities.DataTypesFTestEntity;
import com.force.sdk.jpa.query.entities.DataTypesFTestEntity.PickValues;
import com.force.sdk.qa.util.BaseJPAFTest;

/**
 * 
 * Test related to aggregate functions (count, sum, avg, min and max) in JPQL queries. 
 *
 * @author Nawab Iqbal
 */
// TODO: Most of the test cases here are covered in the unit suite
//       by BasicJPAQueryAggregateFunctionTest and QueryReturnTypeAggregateFunctionTest.
//       We can remove most of the cases in this class.
public class QueryAggregateFunctionsTest extends BaseJPAFTest {

    // these numbers aren't actually max and min, number boundaries should be tested separately
    // some values, like long and big integer, get rounded oddly by the API when they are at their max
    // values, so we're leaving them at 16 digits instead of 18
    private static final long MAX_LONG = 9999999999999999L; // 16 digits, 18 causes rounding problems
    private static final long MIN_LONG = -9999999999999999L; // 16 digits, 18 causes rounding problems
    private static final float MAX_FLOAT = 9999999999999.99f; // this is only 15 digits, 16 should work but doesn't
    private static final float MIN_FLOAT = -9999999999999.99f; // this is only 15 digits, 16 should work but doesn't
    private static final double MAX_DOUBLE = 99999999999999.99d;
    private static final double MIN_DOUBLE = -99999999999999.99d;
    private static final String MAX_BIG_INTEGER = "9999999999999999"; //16 digits, 18 causes rounding problems
    private static final String MIN_BIG_INTEGER = "-9999999999999999"; //16 digits, 18 causes rounding problems
    private static final String MAX_BIG_DECIMAL = "99999999999999.99";
    private static final String MIN_BIG_DECIMAL = "-99999999999999.99";

    BigInteger twoBigInteger = new BigInteger("2");
    BigDecimal twoBigDecimal = new BigDecimal("2");
    BigInteger halfMaxBigInteger = new BigInteger(MAX_BIG_INTEGER).divide(twoBigInteger);
    BigInteger halfMinBigInteger = new BigInteger(MIN_BIG_INTEGER).divide(twoBigInteger);
    BigDecimal halfMaxBigDecimal = new BigDecimal(MAX_BIG_DECIMAL).divide(twoBigDecimal).round(MathContext.DECIMAL64);
    BigDecimal halfMinBigDecimal = new BigDecimal(MIN_BIG_DECIMAL).divide(twoBigDecimal).round(MathContext.DECIMAL64);

    Calendar minDate = null;
    Calendar todayDate = null;
    Calendar maxDate = null;

    Calendar minDateTime = null;
    Calendar todayDateTime = null;
    Calendar maxDateTime = null;

    @BeforeClass(dependsOnMethods = "initialize")
    protected void init() {
        deleteAll(DataTypesFTestEntity.class);
        initializeCalendars();
    }

    private void initializeCalendars() {
        // Using GMT: as autobuild machines have GMT timezone. 
        minDateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        minDateTime.clear();
        minDateTime.set(1700, 0, 1, 01, 10, 30);

        minDate = Calendar.getInstance();
        minDate.clear();
        minDate.set(1700, 0, 1);
        
        todayDateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        todayDateTime.clear();
        todayDateTime.set(2011, 1, 10, 1, 29, 55);

        todayDate =  Calendar.getInstance();
        todayDate.clear();
        todayDate.set(2011, 1, 10);

        maxDateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        maxDateTime.clear();
        maxDateTime.set(4000, 11, 30, 23, 59, 40);
        
        maxDate =  Calendar.getInstance();
        maxDate.clear();
        maxDate.set(4000, 11, 30);
    }

    @AfterClass
    protected void classTearDown() {
        deleteAll(DataTypesFTestEntity.class);
    }

    @BeforeClass(dependsOnMethods = "init")
    protected void initTestData() {
        EntityTransaction tx = em.getTransaction();
        
        Object [][] data = new Object [][] {
                { "max", false, Short.MAX_VALUE, Integer.MAX_VALUE, MAX_LONG, MAX_FLOAT, MAX_DOUBLE, MAX_BIG_INTEGER,
                    MAX_BIG_DECIMAL, maxDate, maxDateTime, PickValues.ONE, "text"},
                { "max", false, Short.MAX_VALUE, Integer.MAX_VALUE, MAX_LONG, MAX_FLOAT, MAX_DOUBLE, MAX_BIG_INTEGER,
                    MAX_BIG_DECIMAL, maxDate, maxDateTime, PickValues.ONE, "text"},
                { "min", false, Short.MIN_VALUE, Integer.MIN_VALUE, MIN_LONG, MIN_FLOAT, MIN_DOUBLE, MIN_BIG_INTEGER,
                    MIN_BIG_DECIMAL, minDate, minDateTime, PickValues.ONE, "text"},
                { "min", false, Short.MIN_VALUE, Integer.MIN_VALUE, MIN_LONG, MIN_FLOAT, MIN_DOUBLE, MIN_BIG_INTEGER,
                    MIN_BIG_DECIMAL, minDate, minDateTime, PickValues.ONE, "text"},
                { "maxMinOnly", false, Short.MAX_VALUE, Integer.MAX_VALUE, MAX_LONG, MAX_FLOAT, MAX_DOUBLE, MAX_BIG_INTEGER,
                    MAX_BIG_DECIMAL, maxDate, maxDateTime, PickValues.THREE, "three"},
                { "maxMinOnly", false, Short.MIN_VALUE, Integer.MIN_VALUE, MIN_LONG, MIN_FLOAT, MIN_DOUBLE, MIN_BIG_INTEGER,
                    MIN_BIG_DECIMAL, minDate, minDateTime, PickValues.TWO, "TWO"},
                { "normal", false, (short) 50, 50, 50L, 50.11f, 50.11d, "50",
                    "50.11", todayDate, todayDateTime, PickValues.ONE, "text"},
                { "normal", false, (short) 21, 21, 21L, 20.99f , 20.99d, "21",
                    "20.99", maxDate, maxDateTime, PickValues.ONE, "text"},
                { "oneNull", false, (short) 50, 50, 50L, 50.11f, 50.11d, "50",
                    "50.11", todayDate, todayDateTime, PickValues.ONE, "text"},
                { "oneNull", true, (short) 0, 0, 0L, 0f, 0d, "0",
                    "0", null , null, null, null},
                { "null", true, (short) 0, 0, 0L, 0f, 0d, "0",
                    "0", null , null, null, null},
                { "null", true, (short) 0, 0, 0L, 0f, 0d, "0",
                    "0", null , null, null, null},
                {"maxSum", false, (short) 0, 0, (long) (MAX_LONG / 2), 0f, MAX_DOUBLE / 2, halfMaxBigInteger.toString(),
                    halfMaxBigDecimal.toString(), todayDate, todayDateTime , PickValues.ONE, "text"},
                {"maxSum", false, (short) 0, 0, (long) (MAX_LONG / 2), 0f, MAX_DOUBLE / 2, halfMaxBigInteger.toString(),
                    halfMaxBigDecimal.toString(), todayDate, todayDateTime , PickValues.ONE, "text"},
                {"minSum", false, (short) 0, 0, (long) (MIN_LONG / 2), 0f, MIN_DOUBLE / 2, halfMinBigInteger.toString(),
                    halfMinBigDecimal.toString(), todayDate, todayDateTime , PickValues.ONE, "text"},
                {"minSum", false, (short) 0, 0, (long) (MIN_LONG  / 2), 0f, MIN_DOUBLE / 2, halfMinBigInteger.toString(),
                    halfMinBigDecimal.toString(), todayDate, todayDateTime , PickValues.ONE, "text"},
                };
        
        try {
            tx.begin();

            for (int i = 0; i < data.length; i++) {
                DataTypesFTestEntity entity = null;
                Object [] row = data[i];
                entity = new DataTypesFTestEntity();
                
                entity.setTextArea((String) row[0]);
                entity.setShortType((Short) row[2]);
                entity.setIntType((Integer) row[3]);
                entity.setLongType((Long) row[4]);
                entity.setFloatType((Float) row[5]);
                entity.setDoubleType((Double) row[6]);
                
                if ((Boolean) row[1]) {
                // This row name is being used for testing null values. 
                    entity.setShortObject(null);
                    entity.setIntegerObject(null);
                    entity.setLongObject(null);
                    entity.setFloatObject(null);
                    entity.setDoubleObject(null);
                    entity.setBigIntegerObject(null);
                    entity.setBigDecimalObject(null);
                    entity.setDate(null);
                    entity.setDateTimeCal(null);
                    entity.setDateTimeGCal(null);
                    entity.setPickValueDef(null);
                    entity.setPickValue(null);
                    entity.setName(null);
                    entity.setStringObject(null);
                } else {
                    entity.setShortObject((Short) row[2]);
                    entity.setIntegerObject((Integer) row[3]);
                    entity.setLongObject((Long) row[4]);
                    entity.setFloatObject((Float) row[5]);
                    entity.setDoubleObject((Double) row[6]);
                    entity.setBigIntegerObject(new BigInteger(row[7].toString()));
                    entity.setBigDecimalObject(new BigDecimal(row[8].toString()));
                    entity.setDate(((Calendar) row[9]).getTime());
                    entity.setDateTimeCal((Calendar) row[10]);
                    entity.setDateTimeGCal((GregorianCalendar) row[10]);
                    entity.setPickValueDef((PickValues) row[11]);
                    entity.setPickValue((PickValues) row[11]);
                    entity.setName((String) row[12]);
                    entity.setStringObject((String) row[12]);
                }
                
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

    @DataProvider
    public Object[][] aggregateMethodsData() throws NumberFormatException, MalformedURLException {
        Object [][] params = new Object[][]{
                {"avg(o.intType)", "normal", 35.5d, Double.class.toString()},
                {"avg(o.shortType)", "normal", 35.5d, Double.class.toString()},
                {"avg(o.longType)", "normal", 35.5d, Double.class.toString()},
                {"avg(o.floatType)", "normal", 35.55d, Double.class.toString()},
                {"avg(o.doubleType)", "normal", 35.55d, Double.class.toString()},
                {"avg(o.bigIntegerObject)", "normal", 35.5d, Double.class.toString()},
                {"avg(o.bigDecimalObject)", "normal", 35.55d, Double.class.toString()},
                {"max(o.intType)", "normal", 50, Integer.class.toString()},
                {"max(o.shortType)", "normal", (short) 50, Short.class.toString()},
                {"max(o.longType)", "normal", 50L, Long.class.toString()},
                {"max(o.floatType)", "normal", 50.11f, Float.class.toString()},
                {"max(o.doubleType)", "normal", 50.11d, Double.class.toString()},
                {"max(o.bigIntegerObject)", "normal", new BigInteger("50"), BigInteger.class.toString()},
                {"max(o.bigDecimalObject)", "normal", new BigDecimal("50.11"), BigDecimal.class.toString()},
                {"min(o.intType)", "normal", 21, Integer.class.toString()},
                {"min(o.shortType)", "normal", (short) 21, Short.class.toString()},
                {"min(o.longType)", "normal", 21L, Long.class.toString()},
                {"min(o.floatType)", "normal", 20.99f, Float.class.toString()},
                {"min(o.doubleType)", "normal", 20.99d, Double.class.toString()},
                {"min(o.bigIntegerObject)", "normal", new BigInteger("21"), BigInteger.class.toString()},
                {"min(o.bigDecimalObject)", "normal", new BigDecimal("20.99"), BigDecimal.class.toString()},
                {"sum(o.intType)", "normal", 71L, Long.class.toString()},
                {"sum(o.shortType)", "normal", 71L, Long.class.toString()},
                {"sum(o.longType)", "normal", 71L, Long.class.toString()},
                {"sum(o.floatType)", "normal", 71.1d, Double.class.toString()},
                {"sum(o.doubleType)", "normal", 71.1d, Double.class.toString()},
                {"sum(o.bigIntegerObject)", "normal", new BigInteger("71"), BigInteger.class.toString()},
                {"sum(o.bigDecimalObject)", "normal", new BigDecimal("71.10"), BigDecimal.class.toString()},
                {"count(o.intType)", "normal", 2L, Long.class.toString()},
                {"count(o.integerObject)", "normal", 2L, Long.class.toString()},
                
                // One-null-value group
                {"avg(o.intType)", "oneNull", 25d, Double.class.toString()},
                {"avg(o.shortType)", "oneNull", 25d, Double.class.toString()},
                {"avg(o.longType)", "oneNull", 25d, Double.class.toString()},
                {"avg(o.floatType)", "oneNull", 25.055d, Double.class.toString()},
                {"avg(o.doubleType)", "oneNull", 25.055d, Double.class.toString()},
                {"avg(o.integerObject)", "oneNull", 50d, Double.class.toString()},
                {"avg(o.shortObject)", "oneNull", 50d, Double.class.toString()},
                {"avg(o.longObject)", "oneNull", 50d, Double.class.toString()},
                {"avg(o.floatObject)", "oneNull", 50.11d, Double.class.toString()},
                {"avg(o.doubleObject)", "oneNull", 50.11d, Double.class.toString()},
                {"avg(o.bigIntegerObject)", "oneNull", 50d, Double.class.toString()},
                {"avg(o.bigDecimalObject)", "oneNull", 50.11d, Double.class.toString()},
                {"max(o.intType)", "oneNull", 50, Integer.class.toString()},
                {"max(o.shortType)", "oneNull", (short) 50, Short.class.toString()},
                {"max(o.longType)", "oneNull", 50L, Long.class.toString()},
                {"max(o.floatType)", "oneNull", 50.11f, Float.class.toString()},
                {"max(o.doubleType)", "oneNull", 50.11d, Double.class.toString()},
                {"max(o.bigIntegerObject)", "oneNull", new BigInteger("50"), BigInteger.class.toString()},
                {"max(o.bigDecimalObject)", "oneNull", new BigDecimal("50.11"), BigDecimal.class.toString()},
                {"max(o.date)", "oneNull", todayDate.getTime(), Date.class.toString()},
                {"max(o.dateTimeCal)", "oneNull", todayDateTime, GregorianCalendar.class.toString()},
                {"max(o.dateTimeGCal)", "oneNull", todayDateTime, GregorianCalendar.class.toString()},
                {"max(o.pickValueDef)", "oneNull", PickValues.ONE, PickValues.class.toString()},
                {"max(o.pickValue)", "oneNull", PickValues.ONE, PickValues.class.toString()},
                {"max(o.stringObject)", "oneNull", "text", String.class.toString()},
                {"min(o.intType)", "oneNull", 0, Integer.class.toString()},
                {"min(o.shortType)", "oneNull", (short) 0, Short.class.toString()},
                {"min(o.longType)", "oneNull", 0L, Long.class.toString()},
                {"min(o.floatType)", "oneNull", 0f, Float.class.toString()},
                {"min(o.doubleType)", "oneNull", 0d, Double.class.toString()},
                {"min(o.integerObject)", "oneNull", 50, Integer.class.toString()},
                {"min(o.shortObject)", "oneNull", (short) 50, Short.class.toString()},
                {"min(o.longObject)", "oneNull", 50L, Long.class.toString()},
                {"min(o.floatObject)", "oneNull", 50.11f, Float.class.toString()},
                {"min(o.doubleObject)", "oneNull", 50.11d, Double.class.toString()},
                {"min(o.bigIntegerObject)", "oneNull", new BigInteger("50"), BigInteger.class.toString()},
                {"min(o.bigDecimalObject)", "oneNull", new BigDecimal("50.11"), BigDecimal.class.toString()},
                {"min(o.date)", "oneNull", todayDate.getTime(), Date.class.toString()},
                {"min(o.dateTimeCal)", "oneNull", todayDateTime, GregorianCalendar.class.toString()},
                {"min(o.dateTimeGCal)", "oneNull", todayDateTime, GregorianCalendar.class.toString()},
                {"min(o.pickValueDef)", "oneNull", null, PickValues.class.toString()},
                {"min(o.pickValue)", "oneNull", null, PickValues.class.toString()},
                {"min(o.stringObject)", "oneNull", "text", String.class.toString()},
                {"sum(o.intType)", "oneNull", 50L, Long.class.toString()},
                {"sum(o.shortType)", "oneNull", 50L, Long.class.toString()},
                {"sum(o.longType)", "oneNull", 50L, Long.class.toString()},
                {"sum(o.floatType)", "oneNull", 50.11d, Double.class.toString()},
                {"sum(o.doubleType)", "oneNull", 50.11d, Double.class.toString()},
                {"sum(o.bigIntegerObject)", "oneNull", new BigInteger("50"), BigInteger.class.toString()},
                {"sum(o.bigDecimalObject)", "oneNull", new BigDecimal("50.11"), BigDecimal.class.toString()},
                {"count(o.intType)", "oneNull", 2L, Long.class.toString()},
                {"count(o.integerObject)", "oneNull", 2L, Long.class.toString()},
                {"count(o.id)", "oneNull", 2L, Long.class.toString()},
                
                
                // Null group   
                {"avg(o.integerObject)", "null", null, null},
                {"avg(o.shortObject)", "null", null, null},
                {"avg(o.longObject)", "null", null, null},
                {"avg(o.floatObject)", "null", null, null},
                {"avg(o.doubleObject)", "null", null, null},
                {"avg(o.bigIntegerObject)", "null", null, null},
                {"avg(o.bigDecimalObject)", "null", null, null},
                {"max(o.integerObject)", "null", null, null},
                {"max(o.shortObject)", "null", null, null},
                {"max(o.longObject)", "null", null, null},
                {"max(o.floatObject)", "null", null, null},
                {"max(o.doubleObject)", "null", null, null},
                {"max(o.bigIntegerObject)", "null", null, null},
                {"max(o.bigDecimalObject)", "null", null, null},
                {"max(o.date)", "null", null, null},
                {"max(o.dateTimeCal)", "null", null, null},
                {"max(o.dateTimeGCal)", "null", null, null},
                {"max(o.pickValueDef)", "null", null, null},
                {"max(o.pickValue)", "null", null, null},
                {"min(o.integerObject)", "null", null, null},
                {"min(o.shortObject)", "null", null, null},
                {"min(o.longObject)", "null", null, null},
                {"min(o.floatObject)", "null", null, null},
                {"min(o.doubleObject)", "null", null, null},
                {"min(o.bigIntegerObject)", "null", null, null},
                {"min(o.bigDecimalObject)", "null", null, null},
                {"min(o.date)", "null", null, null},
                {"min(o.dateTimeCal)", "null", null, null},
                {"min(o.dateTimeGCal)", "null", null, null},
                {"min(o.pickValueDef)", "null", null, null},
                {"min(o.pickValue)", "null", null, null},
                {"min(o.stringObject)", "null", null, null},
                {"sum(o.integerObject)", "null", null, null},
                {"sum(o.shortObject)", "null", null, null},
                {"sum(o.longObject)", "null", null, null},
                {"sum(o.floatObject)", "null", null, null},
                {"sum(o.doubleObject)", "null", null, null},
                {"sum(o.bigIntegerObject)", "null", null, null},
                {"sum(o.bigDecimalObject)", "null", null, null},
                {"count(o.integerObject)", "null", 2L, Long.class.toString()},
                {"count(o.id)", "null", 2L, Long.class.toString()},
                
                // Max group
                {"avg(o.intType)", "max", new Double(Integer.MAX_VALUE), Double.class.toString()},
                {"avg(o.shortType)", "max", new Double(Short.MAX_VALUE), Double.class.toString()},
                {"avg(o.longType)", "max", new Double(MAX_LONG), Double.class.toString()},
                {"avg(o.floatType)", "max", new Double(MAX_FLOAT), Double.class.toString()},
                {"avg(o.doubleType)", "max", MAX_DOUBLE, Double.class.toString()},
                {"avg(o.bigIntegerObject)", "max", new Double(MAX_BIG_INTEGER), Double.class.toString()},
                {"avg(o.bigDecimalObject)", "max", new Double(MAX_BIG_DECIMAL), Double.class.toString()},
                {"sum(o.intType)", "max",  (2 * (long) Integer.MAX_VALUE), Long.class.toString()},
                {"sum(o.shortType)", "max", (2 * (long) Short.MAX_VALUE), Long.class.toString()},
                {"sum(o.longType)", "maxSum", (long) ((long) (MAX_LONG / 2) + (long) (MAX_LONG / 2)), Long.class.toString()},
                {"sum(o.floatType)", "max", new Double(MAX_FLOAT * 2), Double.class.toString()},
                {"sum(o.doubleType)", "maxSum", MAX_DOUBLE, Double.class.toString()},
                {"sum(o.bigIntegerObject)", "maxSum", halfMaxBigInteger.add(halfMaxBigInteger) , BigInteger.class.toString()},
                {"sum(o.bigDecimalObject)", "maxSum", halfMaxBigDecimal.add(halfMaxBigDecimal) , BigDecimal.class.toString()},
                      
                  
                  // Max-Min Group
                  {"max(o.intType)", "maxMinOnly", Integer.MAX_VALUE, Integer.class.toString()},
                {"max(o.shortType)", "maxMinOnly", Short.MAX_VALUE, Short.class.toString()},
                {"max(o.longType)", "maxMinOnly", MAX_LONG, Long.class.toString()},
                {"max(o.floatType)", "maxMinOnly",  MAX_FLOAT, Float.class.toString()},
                {"max(o.doubleType)", "maxMinOnly", MAX_DOUBLE, Double.class.toString()},
                {"max(o.bigIntegerObject)", "maxMinOnly", new BigInteger(MAX_BIG_INTEGER), BigInteger.class.toString()},
                {"max(o.bigDecimalObject)", "maxMinOnly", new BigDecimal(MAX_BIG_DECIMAL).round(MathContext.DECIMAL64),
                        BigDecimal.class.toString()},
                {"max(o.date)", "maxMinOnly", maxDate.getTime(), Date.class.toString()},
                {"max(o.dateTimeCal)", "maxMinOnly", maxDateTime, GregorianCalendar.class.toString()},
                {"max(o.dateTimeGCal)", "maxMinOnly", maxDateTime, GregorianCalendar.class.toString()},
                {"max(o.pickValueDef)", "maxMinOnly", PickValues.THREE, PickValues.class.toString()},
                {"max(o.pickValue)", "maxMinOnly", PickValues.THREE, PickValues.class.toString()},
                {"max(o.name)", "maxMinOnly", "TWO", String.class.toString()},
                
                {"min(o.intType)", "maxMinOnly", Integer.MIN_VALUE, Integer.class.toString()},
                {"min(o.shortType)", "maxMinOnly", Short.MIN_VALUE, Short.class.toString()},
                {"min(o.longType)", "maxMinOnly", MIN_LONG, Long.class.toString()},
                {"min(o.floatType)", "maxMinOnly", MIN_FLOAT, Float.class.toString()},
                {"min(o.doubleType)", "maxMinOnly", MIN_DOUBLE, Double.class.toString()},
                {"min(o.bigIntegerObject)", "maxMinOnly", new BigInteger(MIN_BIG_INTEGER), BigInteger.class.toString()},
                {"min(o.bigDecimalObject)", "maxMinOnly", new BigDecimal(MIN_BIG_DECIMAL), BigDecimal.class.toString()},
                {"min(o.date)", "maxMinOnly", minDate.getTime(), Date.class.toString()},
                {"min(o.dateTimeCal)", "maxMinOnly", minDateTime, GregorianCalendar.class.toString()},
                {"min(o.dateTimeGCal)", "maxMinOnly", minDateTime, GregorianCalendar.class.toString()},
                {"min(o.pickValueDef)", "maxMinOnly", PickValues.TWO, PickValues.class.toString()},
                {"min(o.pickValue)", "maxMinOnly", PickValues.TWO, PickValues.class.toString()},
                {"min(o.name)", "maxMinOnly", "three", String.class.toString()},
                  
                      
                // Min group
                {"avg(o.intType)", "min", new Double(Integer.MIN_VALUE), Double.class.toString()},
                {"avg(o.shortType)", "min", new Double(Short.MIN_VALUE), Double.class.toString()},
                {"avg(o.longType)", "min", new Double(MIN_LONG), Double.class.toString()},
                {"avg(o.floatType)", "min", new Double(MIN_FLOAT), Double.class.toString()},
                {"avg(o.doubleType)", "min", MIN_DOUBLE, Double.class.toString()},
                {"avg(o.bigIntegerObject)", "min", new Double(MIN_BIG_INTEGER), Double.class.toString()},
                {"avg(o.bigDecimalObject)", "min", new Double(MIN_BIG_DECIMAL), Double.class.toString()},
                {"sum(o.intType)", "min", (2 * (long) Integer.MIN_VALUE), Long.class.toString()},
                {"sum(o.shortType)", "min", (2 * (long) Short.MIN_VALUE), Long.class.toString()},
                {"sum(o.longType)", "minSum", (long) (2 * (MIN_LONG / 2)), Long.class.toString()},
                {"sum(o.floatType)", "min", new Double(MIN_FLOAT * 2), Double.class.toString()},
                {"sum(o.doubleType)", "minSum", MIN_DOUBLE, Double.class.toString()},
                {"sum(o.bigIntegerObject)", "minSum", halfMinBigInteger.add(halfMinBigInteger) , BigInteger.class.toString()},
                {"sum(o.bigDecimalObject)", "minSum", halfMinBigDecimal.add(halfMinBigDecimal) , BigDecimal.class.toString()},
        };
        
        return params;
    }
    
    @Test(dataProvider = "aggregateMethodsData")
    /**
     * Test aggregate functions (min, max, avg, sum, count) on different data types (i.e., numeric, date, string).
     * This method tests the functions Min, Max, Avg, Sum and count on different field types and checks for edge case
     * values (minimum int, maximum int and same for other types).
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResult  Value and type of expected result comes from data provider as parameter. These should be same
     * as defined in JPA Spec unless there is an api constraint (e.g., as in case of float).
     */
    public void testAggregateFunctions(String field, String group, Object expectedValue, String expectedClass) {
        String queryFormat = "Select %1$s from DataTypesFTestEntity o where o.textArea = '%2$s'";
        String queryText = String.format(queryFormat, field, group);
        Query query = em.createQuery(queryText);

        Object c = query.getSingleResult();
        /**
         * This is super hackery but comparing floats is problematic due to rounding issues.
         */
        if (c != null && field.contains("float")) {
            double delta;
            if (c instanceof Float) {
                delta = ((Float) c).doubleValue() - ((Float) expectedValue).doubleValue();
            } else {
                delta = ((Double) c).doubleValue() - ((Double) expectedValue).doubleValue();
            }
            Assert.assertTrue(delta < 0001 && delta > -0001, "Values are not equal.");
        } else {
            Assert.assertEquals(c, expectedValue, "Values are not equal.");
        }
        
        if (c != null) {
            Assert.assertEquals(c.getClass().toString(), expectedClass, "Types are not equal.");
        }
    }
}
