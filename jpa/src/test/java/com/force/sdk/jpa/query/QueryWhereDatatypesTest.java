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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.DataTypesTestEntity;

/**
 * 
 * Tests around the WHERE clause for queries using all supported data types.
 *
 * @author Dirk Hain
 * @author Tim Kral
 */
public class QueryWhereDatatypesTest extends BaseJPAQueryTest {
    
    private static final String QUERY_BASE = "select o.id from " + DataTypesTestEntity.class.getSimpleName() + " o ";
    private static final String EXPECTED_QUERY_BASE = "select o.Id from datatypestestentity__c o  ";
    
    /*WHERE clause data provider*/
    @DataProvider
    public Object[][] whereData(Method test) throws NumberFormatException, MalformedURLException {
        // Both calendars represent the epoch in GMT
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(0);
        
        GregorianCalendar gcal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        gcal.setTimeInMillis(0);
        
        Calendar localCal = Calendar.getInstance();
        localCal.setTimeInMillis(0 - TimeZone.getDefault().getRawOffset());
        
        // Note: The where clause operator (represented by %s) is replaced in the test methods
        Object [][] primitiveVals = new Object[][] {
                {"booleanType",    Boolean.TRUE,      "where (o.booleanType__c %s true)"},
                {"byteType",       Byte.valueOf("0"), "where (o.byteType__c %s '0')"},
        };
        
        Object [][] primitiveNumberVals = new Object[][] {
                {"shortType",      (short) 0,  "where (o.shortType__c %s 0)"},
                {"intType",        0,          "where (o.intType__c %s 0)"},
                {"longType",       (long) 0,   "where (o.longType__c %s 0)"},
                {"doubleType",     (double) 0, "where (o.doubleType__c %s 0.0)"},
                {"floatType",      (float) 0,  "where (o.floatType__c %s 0.0)"},
                {"charType",       'A',        "where (o.charType__c %s 'A')"},
        };
        
        Object[][] objectNumberVals = new Object[][] {
                {"shortObject",    Short.valueOf((short) 0), "where (o.shortObject__c %s 0)"},
                {"integerObject",  Integer.valueOf(0),       "where (o.integerObject__c %s 0)"},
                {"longObject",     Long.valueOf(0),          "where (o.longObject__c %s 0)"},
                {"doubleObject",   Double.valueOf(0),        "where (o.doubleObject__c %s 0.0)"},
                {"floatObject",    Float.valueOf(0),         "where (o.floatObject__c %s 0.0)"},
                {"characterObject", 'A',                     "where (o.characterObject__c %s 'A')"},
                {"bigDecimalObject", new BigDecimal(0),      "where (o.bigDecimalObject__c %s 0)"},
                {"bigIntegerObject", BigInteger.valueOf(0),  "where (o.bigIntegerObject__c %s 0)"},
        };

        Object [][] stringVals = new Object[][] {
                {"stringObject",   "deadbeef",                       "where (o.stringObject__c %s 'deadbeef')"},
                {"url",            new URL("http://localhost:0000"), "where (o.url__c %s 'http://localhost:0000')"},
                {"characterObject", 'A',                             "where (o.characterObject__c %s 'A')"},
        };
        
        Object [][] objectVals = new Object[][]{
                {"booleanObject",  Boolean.FALSE,     "where (o.booleanObject__c %s false)"},
                {"byteObject",     Byte.valueOf("0"), "where (o.byteObject__c %s '0')"},
                {"date",           localCal.getTime() ,     "where (o.date__c %s 1970-01-01)"},
                {"dateTimeCal",    cal,               "where (o.dateTimeCal__c %s 1970-01-01T00:00:00+00:00)"},
                {"dateTimeGCal",   gcal,              "where (o.dateTimeGCal__c %s 1970-01-01T00:00:00+00:00)"},
        };
        
        if (test.getName().contains("AllTypes")) {
            return concat(primitiveVals, primitiveNumberVals, objectVals, objectNumberVals, stringVals);
        } else if (test.getName().contains("ObjectTypes")) {
            return concat(objectVals, objectNumberVals, stringVals);
        } else if (test.getName().contains("PrimitiveTypes")) {
            return concat(primitiveVals, primitiveNumberVals);
        } else if (test.getName().contains("StringTypes")) {
            return stringVals;
        } else if (test.getName().contains("PrimitiveNumberTypes")) {
            return primitiveNumberVals;
        } else if (test.getName().contains("NumberTypes")) {
            return concat(primitiveNumberVals, objectNumberVals);
        } else {
            return null;
        }
    }
    
    public Object[][] concat(Object[][]... arrs) {
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
    
    @Test(dataProvider = "whereData")
//    @QaTestCase(subject = "Query where", description = "Basic where clause JPQL query",
//                hierarchy = "test hierarchy", testCaseId = "qaforce id", userStorySyncIdOrName = "qaforce userstory id")
    public <T> void testWhereBasicAllTypes(String typeName, T value, String expectedSoqlWhereClause) {
        // The expected SOQL where clause should use the = operator (see query below)
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + String.format(expectedSoqlWhereClause, "="));
        
        String query = QUERY_BASE + "where o." + typeName + "= ?1";
        em.createQuery(query).setParameter(1, value).getResultList();
    }
    
    @Test(dataProvider = "whereData")
//    @QaTestCase(subject = "Query where", description = "Where clause with like expression",
//                hierarchy = "test hierarchy", testCaseId = "qaforce id", userStorySyncIdOrName = "qaforce userstory id")
    public <T> void testWhereLikeStringTypes(String typeName, T value, String expectedSoqlWhereClause) {
        // The expected SOQL where clause should use the 'like' keyword (see query below)
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + String.format(expectedSoqlWhereClause, "like"));
        
        String query = QUERY_BASE + "where o." + typeName + " like ?1";
        em.createQuery(query).setParameter(1, value).getResultList();
    }
    
    
    @Test(dataProvider = "whereData")
//  @QaTestCase(subject = "Query where", description = "Where clause with range operator (e.g. <=)",
//              hierarchy = "test hierarchy", testCaseId = "qaforce id", userStorySyncIdOrName = "qaforce userstory id")
    public <T> void testWhereRangequeryNumberTypes(String typeName, T value, String expectedSoqlWhereClause) {
        // The expected SOQL where clause should use the <= operator (see query below)
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + String.format(expectedSoqlWhereClause, "<="));
        
        String query = QUERY_BASE + "where o." + typeName + "<= ?1";
        em.createQuery(query).setParameter(1, value);
    }



    @Test(dataProvider = "whereData")
//    @QaTestCase(subject = "Query where", description = "Where clause with not matching object type",
//                hierarchy = "test hierarchy", testCaseId = "qaforce id", userStorySyncIdOrName = "qaforce userstory id")
    public <T> void testWhereNegativeObjectTypes(String typeName, T value, String expectedSoqlQuery /* ignored */) {
        String exceptionMessage = "Parameter 1 needs to be assignable from " + value.getClass().getName()
                                    + " yet the value is of type java.lang.Object";
        
        try {
            String query = QUERY_BASE + "where o." + typeName + "= ?1";
            em.createQuery(query).setParameter(1, new Object());
            fail("'" + query + "' should have failed because we supplied an Object as the numbered query parameter.");
        } catch (IllegalArgumentException expected) {
            boolean messagesMatch = expected.getMessage().contains(exceptionMessage);
            if (!messagesMatch) {
                messagesMatch =
                    expected.getMessage().contains("Parameter 1 needs to be assignable from java.util.Calendar"
                                                    + " yet the value is of type java.lang.Object");
            }
            
            assertTrue(messagesMatch, "Wrong exception message for numbered query for type " + typeName);
        }
    }
}
