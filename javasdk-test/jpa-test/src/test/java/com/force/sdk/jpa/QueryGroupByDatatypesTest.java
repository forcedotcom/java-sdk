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
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.jpa.JPATestUtils.Digit;
import com.force.sdk.jpa.entities.AnnotatedEntity;
import com.force.sdk.jpa.entities.TestEntity;


/**
 * 
 * Test related to the GROUP BY clause in JPQL select statements.
 *
 * @author Dirk Hain
 */
public class QueryGroupByDatatypesTest extends DatatypesBaseTest {

    static final String ALL_TYPES = "AllTypes";
    static final String GROUPABLE_TYPES = "GroupableTypes";
    static final String NON_GROUP_TYPES = "NonGroupTypes";

    
    
    static final String TEST_ENTITY_SELECT_ITEMS_ALL =
        "o.boolType, o.byteType, o.shortType, o.intType, o.longType, o.doubleType, o.floatType, o.charType, "
            + "o.booleanObject, o.byteObject, o.shortObject, o.integerObject, o.longObject, o.doubleObject, "
            + "o.floatObject, o.characterObject, o.bigDecimalObject, o.bigIntegerObject, o.stringObject, o.url, "
            + "o.phone, o.email, o.percent, o.date, o.dateTimeCal, o.dateTimeGCal, o.dateTemporal, "
            + "o.dateTimeTemporal, o.lastModifiedDate";
    
    
    @DataProvider
    public Object[][] groupbyData(Method test) throws NumberFormatException, MalformedURLException {
        
        Calendar cal = JPATestUtils.getCalendar(false);
        Calendar.getInstance();
        cal.set(2010, 1, 1, Digit.AZERO.value, Digit.AZERO.value, Digit.AZERO.value);
        cal.set(Calendar.MILLISECOND, Digit.AZERO.value);
        //final Date d = cal.getTime();

        Object [][] groupableVals = new Object[][]{
                {new TestEntity(), "byteType",      (byte) 0,               new Byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}},
                {new TestEntity(), "byteObject",    Byte.valueOf((byte) 0),  new Byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}},
//check in once W-854513 is fixed
//                {new TestEntity(), "boolType",      Boolean.TRUE,           new Boolean[]{true}},
//                {new TestEntity(), "booleanObject", Boolean.FALSE,          new Boolean[]{true}},
//                {new TestEntity(), "date",          getCal(false, Digit.AZERO, false).getTime(), new Date[]{d}},
//                {new TestEntity(), "dateTemporal",  getCal(false, Digit.AZERO, false).getTime(), new Date[]{d}},
                {new TestEntity(), "charType",          'A',                new Character[]{'A', 'E', 'F', 'N', 'O', 'S', 'T'}},
                {new TestEntity(), "characterObject",   'A',                new Character[]{'A', 'E', 'F', 'N', 'O', 'S', 'T'}},
        };
        
        Object [][] nonGroupableVals = new Object[][]{
                {new TestEntity(), "shortType",     (short) 0,                new Object[]{}},
                {new TestEntity(), "intType",       0,                        new Object[]{}},
                {new TestEntity(), "longType",      (long) 0,                 new Object[]{}},
                {new TestEntity(), "floatType",     (float) 0,                new Object[]{}},
                {new TestEntity(), "doubleType",    (double) 0,               new Object[]{}},
                {new TestEntity(), "shortObject",   Short.valueOf((short) 0), new Object[]{}},
                {new TestEntity(), "integerObject", Integer.valueOf(0),       new Object[]{}},
                {new TestEntity(), "longObject",    Long.valueOf(0),          new Object[]{}},
                {new TestEntity(), "floatObject",   new Float(0),             new Object[]{}},
                {new TestEntity(), "doubleObject",  new Double(0),            new Object[]{}},
                {new TestEntity(), "bigDecimalObject", new BigDecimal(0),     new Object[]{}},
                {new TestEntity(), "bigIntegerObject", BigInteger.valueOf(0),   new Object[]{}},
                {new TestEntity(), "percent",        0,                         new Object[]{}},
                {new TestEntity(), "dateTimeCal",   getCal(false, Digit.AZERO, true),   new Object[]{}},
                {new TestEntity(), "dateTimeGCal",  getCal(true, Digit.AZERO, true),    new Object[]{}},
        };
        
        final String em = "foobar@salesforce.com";
        final String ph = "415-123-";
        final String ur = "http://localhost:";
        Object [][] stringVals = new Object[][]{
                {new TestEntity(), "stringObject",   Digit.AZERO.toString(),
                    new String[]{"AZERO", "EIGHT", "FIVE", "FOUR", "NINE", "ONE", "SEVEN", "SIX", "THREE", "TWO"}},
                {new TestEntity(), "phone", "415-123-0000",
                    new String[]{ph + "0000", ph + "1111", ph + "2222", ph + "3333", ph + "4444", ph + "5555",
                                    ph + "6666", ph + "7777", ph + "8888", ph + "9999"}},
                {new TestEntity(), "email", "0foobar@salesforce.com",
                    new String[]{"0" + em, "1" + em, "2" + em, "3" + em, "4" + em, "5" + em, "6" + em,
                                    "7" + em, "8" + em, "9" + em}},
                {new TestEntity(), "url",   new URL("http://localhost:0000"),
                    new URL[]{new URL(ur + "0000"), new URL(ur + "1111"), new URL(ur + "2222"), new URL(ur + "3333"),
                              new URL(ur + "4444"), new URL(ur + "5555"), new URL(ur + "6666"), new URL(ur + "7777"),
                              new URL(ur + "8888"), new URL(ur + "9999")}},
        };
        
        
        if (test.getName().contains(ALL_TYPES)) {
            return concat(groupableVals, nonGroupableVals, stringVals);
        } else if (test.getName().contains(NON_GROUP_TYPES)) {
            return nonGroupableVals;
        } else if (test.getName().contains(GROUPABLE_TYPES)) {
            return concat(groupableVals, stringVals);
        } else {
            return null;
        }
    }
    

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "groupbyData")
    /**
     * JPQL group by test.
     * Tests group by clause in JPQL query. This test is data driven and runs for most types defined by the sdk.
     * @hierarchy javasdk
     * @userStory xyz
     */    
    public <T extends AnnotatedEntity, E, F> void testSimpleGroupbyGroupableTypes(T queryObj, String group,
            E value, F[] expected) {
        String queryBase = "SELECT o." + group
                           + " FROM " + queryObj.getClass().getSimpleName() + " o "
                           + "GROUP BY o." + group
                           + " ORDER BY o." + group + " ASC";
        List<Object> result = em.createQuery(queryBase).getResultList();
        Assert.assertEquals(result.size(), expected.length, "Wrong number of groups for grouping on " + group);
        //assert groupings
        //verify every result element is in the expected list
        int i = 0;
        for (Object res : result) {
            Assert.assertEquals(res, expected[i++], "JPQL GroupBy: Wrong groups returned for grouping on " + group);
        }
    }

    
    @Test(dataProvider = "groupbyData")
    /**
     * JPQL group by negative tests.
     * Tests assert the correct exception for non-groupable types. The test is data driven.
     * @hierarchy javasdk
     * @userStory xyz
     */        
    public <T extends AnnotatedEntity, E, F> void testGroupbyNegNonGroupTypes(T queryObj, String group, E value, F[] expected) {
        final Pattern exceptionMessagePattern =
            Pattern.compile("field '[a-zA-Z0-9_$]*" + group + "__c' can not be grouped in a query call");
        String queryBase = "SELECT o." + group + " FROM " + queryObj.getClass().getSimpleName() + " o GROUP BY o." + group;
        try {
            em.createQuery(queryBase).getResultList();
            Assert.fail("GROUP BY query on non groupable type should have caused an exception.");
        } catch (PersistenceException pe) {
            boolean messagesMatch = exceptionMessagePattern.matcher(pe.getMessage()).find();
            Assert.assertTrue(messagesMatch, "Wrong exception message for group by query on non groupable type " + group);
        }
    }

    
    @SuppressWarnings("unchecked")
    @Test(dataProvider = "groupbyData")
    /**
     * JPQL group by clause with where condition and aggregate.
     * Runs a JPQL query containing group by clause, where condition 
     * and aggregate functions (AVG, SUM, MIN, MAX). The test is data driven.
     * @hierarchy javasdk
     * @userStory xyz
     */            
    public <T extends AnnotatedEntity, E, F> void testGroupbyGroupableTypes(T queryObj, String group, E value, F[] expected) {
        String queryBase = "SELECT o." + group + ", AVG(o.longObject), SUM(o.floatType), "
                           + "MIN(o.percent), MAX(o.bigIntegerObject) "
                           + " FROM " + queryObj.getClass().getSimpleName() + " o "
                           + "WHERE o." + group + "<>:typeval "
                           + "GROUP BY o." + group
                           + " ORDER BY o." + group + " ASC";
        Query q = em.createQuery(queryBase + ":typeval").setParameter("typeval", value);
        List<Object[]> result = q.getResultList();
        Assert.assertEquals(result.size(), expected.length - 1, "Grouping on " + group);
        int i = 1; // skip the first expected result as it is excluded by the where clause
        for (Object[] res : result) {
            Assert.assertEquals(res[0], expected[i++],
                    "JPQL GroupBy, AGGR, Where: Wrong groups returned for grouping on " + group);
        }
    }
    
    
    @Test
    /**
     * Negative test for count() predicate in group by clause.
     * Count is not supported by the sdk. This test asserts an error is logged when a query with count is found.
     * @hierarchy javasdk
     * @userStory xyz
     */                
    public void testGroupbyCountNeg() {
        final Pattern exceptionMessagePattern = Pattern.compile("ERROR at Row:\\d+:Column:\\d+\nunexpected token: '\\)''");
        String queryBase = "SELECT o.charType, count(o) "
                           + " FROM TestEntity o "
                           + "GROUP BY o.charType";
        try {
            em.createQuery(queryBase).getResultList();
        } catch (PersistenceException pe) {
            boolean messagesMatch = exceptionMessagePattern.matcher(pe.getMessage()).find();
            Assert.assertTrue(messagesMatch, "Wrong exception message for group by with count");
        }
    }
    
}
