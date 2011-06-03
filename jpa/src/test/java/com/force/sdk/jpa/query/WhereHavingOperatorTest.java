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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.QueryTestEntity;

/**
 * 
 * Tests for WHERE and HAVING clauses for Force.com JPA queries.
 *
 * @author John Simone
 * @author Tim Kral
 */
public class WhereHavingOperatorTest extends BaseJPAQueryTest {

    private static final String QUERY_BASE = "select o from " + QueryTestEntity.class.getSimpleName() + " o";
    private static final String HAVING_QUERY_BASE = "select o.name from " + QueryTestEntity.class.getSimpleName() + " o";
    
    private static final String EXPECTED_QUERY_BASE =
        "select id, date__c, entityType__c, Name, number__c from querytestentity__c o ";
    private static final String EXPECTED_HAVING_QUERY_BASE =
        "select o.Name from querytestentity__c o  group by o.Name having ";

    private static final String WHERE = "WHERE";
    private static final String FIELD_NUMBER = "o.number";
    private static final String FIELD_ALPHA = "o.name";
    private static final String FIELD_PICKLIST = "o.pickValueMulti";

    @DataProvider(name = "singleValueComparisons")
    public Object[][] createSingleValueComparisonData() {

        return new Object[][]{
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6",
                    EXPECTED_QUERY_BASE + " where (o.number__c > 6)"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6",
                    EXPECTED_QUERY_BASE + " where (o.number__c >= 6)"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6",
                    EXPECTED_QUERY_BASE + " where (o.number__c < 6)"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6",
                    EXPECTED_QUERY_BASE + " where (o.number__c <= 6)"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6",
                    EXPECTED_QUERY_BASE + " where (o.number__c = 6)"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6",
                    EXPECTED_QUERY_BASE + " where (o.number__c <> 6)"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'%'",
                    EXPECTED_QUERY_BASE + " where (o.Name like '%')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'",
                    EXPECTED_QUERY_BASE + " where (NOT o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'%'",
                    EXPECTED_QUERY_BASE + " where (NOT o.Name like '%')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, ">", "'six'",
                    EXPECTED_QUERY_BASE + " where (o.Name > 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "<", "'six'",
                    EXPECTED_QUERY_BASE + " where (o.Name < 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, ">=", "'six'",
                    EXPECTED_QUERY_BASE + " where (o.Name >= 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "<=", "'six'",
                    EXPECTED_QUERY_BASE + " where (o.Name <= 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "=", "'six'",
                    EXPECTED_QUERY_BASE + " where (o.Name = 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "<>", "'six'",
                    EXPECTED_QUERY_BASE + " where (o.Name <> 'six')"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "6 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 8 ) AND ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "NOT BETWEEN", "6 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) OR ( o.number__c > 8 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "8 AND 6",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) AND ( o.number__c >= 8 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "NOT BETWEEN", "8 AND 6",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 8 ) OR ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "BETWEEN", "'six' AND 'eight'",
                    EXPECTED_QUERY_BASE + " where (( o.Name <= 'eight' ) AND ( o.Name >= 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT BETWEEN", "'six' AND 'eight'",
                    EXPECTED_QUERY_BASE + " where (( o.Name < 'six' ) OR ( o.Name > 'eight' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "BETWEEN", "'eight' AND 'six'",
                    EXPECTED_QUERY_BASE + " where (( o.Name <= 'six' ) AND ( o.Name >= 'eight' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT BETWEEN", "'eight' AND 'six'",
                    EXPECTED_QUERY_BASE + " where (( o.Name < 'eight' ) OR ( o.Name > 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "IN", "('six','eight','three')",
                    EXPECTED_QUERY_BASE + " where (( ( o.Name = 'six' ) OR ( o.Name = 'eight' ) ) OR ( o.Name = 'three' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT IN", "('six','eight','nine')",
                    EXPECTED_QUERY_BASE + " where (( ( o.Name <> 'six' ) AND ( o.Name <> 'eight' ) ) AND ( o.Name <> 'nine' ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "IN", "(6,8,9)",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 6 ) OR ( o.number__c = 8 ) ) OR ( o.number__c = 9 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "NOT IN", "(6,8,9)",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 6 ) AND ( o.number__c <> 8 ) ) AND ( o.number__c <> 9 ))"},
                {QUERY_BASE, WHERE, "'TWO'", "MEMBER OF", FIELD_PICKLIST,
                    EXPECTED_QUERY_BASE + " where (o.pickValueMulti__c includes('TWO'))"},
                {QUERY_BASE, WHERE, "'THREE;ONE,TWO'", "MEMBER OF", FIELD_PICKLIST,
                    EXPECTED_QUERY_BASE + " where (o.pickValueMulti__c includes('THREE;ONE','TWO'))"},
                {QUERY_BASE, WHERE, "'ONE'", "NOT MEMBER OF", FIELD_PICKLIST,
                    EXPECTED_QUERY_BASE + " where (pickValueMulti__c excludes('ONE'))"},
                {QUERY_BASE, WHERE, FIELD_PICKLIST, "IS EMPTY", null,
                    EXPECTED_QUERY_BASE + " where (pickValueMulti__c = null)"},
                {QUERY_BASE, WHERE,  FIELD_PICKLIST, "IS NOT EMPTY", null,
                    EXPECTED_QUERY_BASE + " where (pickValueMulti__c != null)"},
        };
    }

    @Test(dataProvider = "singleValueComparisons")
    public void testSingleValues(
            String queryBase,
            String queryType,
            String field,
            String operator,
            String value,
            final String expectedSoqlQuery) {
        
        mockQueryConn.setExpectedSoqlQuery(expectedSoqlQuery);
        
        String query = queryBase + " " + queryType + " " + field + " " + operator + (value != null ? " " + value : "");
        em.createQuery(query).getResultList();
    }
    
    private Object[][] createMultiValueComparisonDuplicateData() {
        return new Object[][] {
                //control cases. Duplicate declarations
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", ">", "6",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( o.number__c > 6 ))",
                    null /* expectedReverseSoqlQuery */},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "AND", "<", "6",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) AND ( o.number__c < 6 ))",
                    null /* expectedReverseSoqlQuery */},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", ">", "6",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( o.number__c > 6 ))",
                    null /* expectedReverseSoqlQuery */},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "OR", "<", "6",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) OR ( o.number__c < 6 ))",
                    null /* expectedReverseSoqlQuery */},
        };
    }
    
    private Object[][] createMultiValueGreaterAndData() {
        return new Object[][] {
                //> AND others
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", "<", "9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( o.number__c < 9 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 9 ) AND ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", "<=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( o.number__c <= 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 3 ) AND ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", ">=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( o.number__c >= 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 3 ) AND ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", "=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( o.number__c = 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 3 ) AND ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) AND ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) AND ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) AND ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ) AND ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) AND ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) AND ( o.number__c > 6 ))"},
        };
    }
    
    private Object[][] createMultiValueGreaterOrData() {
        return new Object[][] {
                //> OR others
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", "<", "9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( o.number__c < 9 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 9 ) OR ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", "<=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( o.number__c <= 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 3 ) OR ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", ">=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( o.number__c >= 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 3 ) OR ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", "=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( o.number__c = 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 3 ) OR ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) OR ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) OR ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) OR ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", "IN", "(3,6)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( ( o.number__c = 3 ) OR ( o.number__c = 6 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 3 ) OR ( o.number__c = 6 ) ) OR ( o.number__c > 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c > 6 ) OR ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) OR ( o.number__c > 6 ))"},
                
        };
    }
    
    private Object[][] createMultiValueLessEqualsAndData() {
        return new Object[][] {
                //<= AND others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "AND", ">=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) AND ( o.number__c >= 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 3 ) AND ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "AND", "<", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) AND ( o.number__c < 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 3 ) AND ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "AND", "=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) AND ( o.number__c = 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 3 ) AND ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "AND", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) AND ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) AND ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "AND", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) AND ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) AND ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "AND", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) AND ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) AND ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "AND", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) AND ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ) AND ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "AND", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) AND ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) AND ( o.number__c <= 6 ))"},
                
        };
    }
    
    private Object[][] createMultiValueLessEqualsOrData() {
        return new Object[][] {
                //<= OR others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "OR", ">=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) OR ( o.number__c >= 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 3 ) OR ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "OR", "<", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) OR ( o.number__c < 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 3 ) OR ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "OR", "=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) OR ( o.number__c = 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 3 ) OR ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "OR", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) OR ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) OR ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "OR", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) OR ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) OR ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "OR", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) OR ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) OR ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "OR", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) OR ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ) OR ( o.number__c <= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "OR", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <= 6 ) OR ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) OR ( o.number__c <= 6 ))"},
        };
    }

    private Object[][] createMultiValueGreaterEqualsAndData() {
        return new Object[][] {
                //>= AND others
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "AND", "<", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) AND ( o.number__c < 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 3 ) AND ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "AND", "=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) AND ( o.number__c = 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 3 ) AND ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "AND", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) AND ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) AND ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "AND", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) AND ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) AND ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "AND", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) AND ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) AND ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "AND", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) AND ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ) AND ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "AND", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) AND ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) AND ( o.number__c >= 6 ))"},
        };
    }

    private Object[][] createMultiValueGreaterEqualsOrData() {
        return new Object[][] {
                //>= OR others
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "OR", "<", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) OR ( o.number__c < 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 3 ) OR ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "OR", "=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) OR ( o.number__c = 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 3 ) OR ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "OR", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) OR ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) OR ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "OR", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) OR ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) OR ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "OR", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) OR ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) OR ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "OR", "IN", "(3,6)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) OR ( ( o.number__c = 3 ) OR ( o.number__c = 6 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 3 ) OR ( o.number__c = 6 ) ) OR ( o.number__c >= 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "OR", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c >= 6 ) OR ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) OR ( o.number__c >= 6 ))"},
        };
    }
    
    private Object[][] createMultiValueLessAndData() {
        return new Object[][] {
                //< AND others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "AND", "=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) AND ( o.number__c = 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 3 ) AND ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "AND", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) AND ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) AND ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "AND", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) AND ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) AND ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "AND", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) AND ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) AND ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "AND", "IN", "(3,5)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) AND ( ( o.number__c = 3 ) OR ( o.number__c = 5 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 3 ) OR ( o.number__c = 5 ) ) AND ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "AND", "NOT IN", "(3,5)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) AND ( ( o.number__c <> 3 ) AND ( o.number__c <> 5 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 3 ) AND ( o.number__c <> 5 ) ) AND ( o.number__c < 6 ))"},
        };
    }

    private Object[][] createMultiValueLessOrData() {
        return new Object[][] {
                //< OR others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "OR", "=", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) OR ( o.number__c = 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 3 ) OR ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "OR", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) OR ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) OR ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "OR", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) OR ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) OR ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "OR", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) OR ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) OR ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "OR", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) OR ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ) OR ( o.number__c < 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "OR", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c < 6 ) OR ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) OR ( o.number__c < 6 ))"},
        };
    }
    
    private Object[][] createMultiValueEqualsAndData() {
        return new Object[][] {
                //= AND others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "AND", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) AND ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) AND ( o.number__c = 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "AND", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) AND ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) AND ( o.number__c = 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "AND", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) AND ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) AND ( o.number__c = 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "AND", "IN", "(3,6)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) AND ( ( o.number__c = 3 ) OR ( o.number__c = 6 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 3 ) OR ( o.number__c = 6 ) ) AND ( o.number__c = 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "AND", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) AND ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) AND ( o.number__c = 6 ))"},
        };
    }
    
    private Object[][] createMultiValueEqualsOrData() {
        return new Object[][] {
                //= OR others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "OR", "<>", "3",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) OR ( o.number__c <> 3 ))",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 3 ) OR ( o.number__c = 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "OR", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) OR ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) OR ( o.number__c = 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "OR", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) OR ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) OR ( o.number__c = 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "OR", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) OR ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ) OR ( o.number__c = 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "OR", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c = 6 ) OR ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) OR ( o.number__c = 6 ))"},
        };
    }
    
    private Object[][] createMultiValueNotEqualsAndData() {
        return new Object[][] {
                //<> AND others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "AND", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 6 ) AND ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) AND ( o.number__c <> 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "AND", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 6 ) AND ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) AND ( o.number__c <> 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "AND", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 6 ) AND ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ) AND ( o.number__c <> 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "AND", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 6 ) AND ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) AND ( o.number__c <> 6 ))"},
        };
    }
    
    private Object[][] createMultiValueNotEqualsOrData() {
        return new Object[][] {
                //<> OR others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "OR", "BETWEEN", "4 AND 9",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 6 ) OR ( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <= 9 ) AND ( o.number__c >= 4 ) ) OR ( o.number__c <> 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "OR", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 6 ) OR ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ) OR ( o.number__c <> 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "OR", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 6 ) OR ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ) OR ( o.number__c <> 6 ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "OR", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE + " where (( o.number__c <> 6 ) OR ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ) OR ( o.number__c <> 6 ))"},
        };
    }
    
    private Object[][] createMultiValueBetweenAndData() {
        return new Object[][] {
                //BETWEEN AND others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "6 AND 13", "AND", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) )"
                                + " AND ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) )"
                                + " AND ( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "6 AND 13", "AND", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) )"
                                + " AND ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) )"
                                + " AND ( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "6 AND 13", "AND", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) )"
                                + " AND ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) )"
                                + " AND ( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) ))"},
        };
    }
    
    private Object[][] createMultiValueBetweenOrData() {
        return new Object[][] {
                //BETWEEN OR others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "6 AND 13", "OR", "NOT BETWEEN", "3 AND 8",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) )"
                                + " OR ( ( o.number__c < 3 ) OR ( o.number__c > 8 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c < 3 ) OR ( o.number__c > 8 ) )"
                                + " OR ( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "6 AND 13", "OR", "IN", "(3,6)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) )"
                                + " OR ( ( o.number__c = 3 ) OR ( o.number__c = 6 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c = 3 ) OR ( o.number__c = 6 ) )"
                                + " OR ( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "6 AND 13", "OR", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) )"
                                + " OR ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) )"
                                + " OR ( ( o.number__c <= 13 ) AND ( o.number__c >= 6 ) ))"},
        };
    }
    
    private Object[][] createMultiValueNotBetweenAndData() {
        return new Object[][] {
                //NOT BETWEEN AND others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "NOT BETWEEN", "6 AND 13", "AND", "IN", "(8,9)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c < 6 ) OR ( o.number__c > 13 ) )"
                                + " AND ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) )"
                                + " AND ( ( o.number__c < 6 ) OR ( o.number__c > 13 ) ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "NOT BETWEEN", "6 AND 13", "AND", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c < 6 ) OR ( o.number__c > 13 ) )"
                                + " AND ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) )"
                                + " AND ( ( o.number__c < 6 ) OR ( o.number__c > 13 ) ))"},
        };
    }
    
    private Object[][] createMultiValueNotBetweenOrData() {
        return new Object[][] {
                //NOT BETWEEN OR others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "NOT BETWEEN", "6 AND 13", "OR", "IN", "(3,6)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c < 6 ) OR ( o.number__c > 13 ) )"
                                + " OR ( ( o.number__c = 3 ) OR ( o.number__c = 6 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c = 3 ) OR ( o.number__c = 6 ) )"
                                + " OR ( ( o.number__c < 6 ) OR ( o.number__c > 13 ) ))"},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "NOT BETWEEN", "6 AND 13", "OR", "NOT IN", "(8,9)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c < 6 ) OR ( o.number__c > 13 ) )"
                                + " OR ( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <> 8 ) AND ( o.number__c <> 9 ) )"
                                + " OR ( ( o.number__c < 6 ) OR ( o.number__c > 13 ) ))"},
        };
    }
    
    private Object[][] createMultiValueInAndData() {
        return new Object[][] {
                //IN AND others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "IN", "(8,9)", "AND", "NOT IN", "(3,6)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) )"
                                + " AND ( ( o.number__c <> 3 ) AND ( o.number__c <> 6 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <> 3 ) AND ( o.number__c <> 6 ) )"
                                + " AND ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))"},
        };
    }

    private Object[][] createMultiValueInOrData() {
        return new Object[][] {
                //IN OR others
                {QUERY_BASE, WHERE, FIELD_NUMBER, "IN", "(8,9)", "OR", "NOT IN", "(3,6)",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c = 8 ) OR ( o.number__c = 9 ) )"
                                + " OR ( ( o.number__c <> 3 ) AND ( o.number__c <> 6 ) ))",
                    EXPECTED_QUERY_BASE
                        + " where (( ( o.number__c <> 3 ) AND ( o.number__c <> 6 ) )"
                                + " OR ( ( o.number__c = 8 ) OR ( o.number__c = 9 ) ))"},
        };
    }

    private Object[][] createMultiValueLikeAndData() {
        return new Object[][] {
                //LIKE AND others
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "AND", "<", "'nine'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' AND ( o.Name < 'nine' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name < 'nine' ) AND o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "AND", "<=", "'three'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' AND ( o.Name <= 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name <= 'three' ) AND o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "AND", ">=", "'three'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' AND ( o.Name >= 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name >= 'three' ) AND o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "AND", "=", "'three'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' AND ( o.Name = 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name = 'three' ) AND o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "AND", "<>", "'three'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' AND ( o.Name <> 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name <> 'three' ) AND o.Name like 'six')"},
        };
    }

    private Object[][] createMultiValueLikeOrData() {
        return new Object[][] {
                //LIKE OR others
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "OR", "<", "'nine'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' OR ( o.Name < 'nine' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name < 'nine' ) OR o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "OR", "<=", "'three'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' OR ( o.Name <= 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name <= 'three' ) OR o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "OR", ">=", "'three'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' OR ( o.Name >= 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name >= 'three' ) OR o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "OR", "=", "'three'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' OR ( o.Name = 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name = 'three' ) OR o.Name like 'six')"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "OR", "<>", "'three'",
                    EXPECTED_QUERY_BASE + " where (o.Name like 'six' OR ( o.Name <> 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name <> 'three' ) OR o.Name like 'six')"},
        };
    }

    private Object[][] createMultiValueNotLikeAndData() {
        return new Object[][] {
                //NOT LIKE AND others
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "AND", "<", "'nine'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) AND ( o.Name < 'nine' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name < 'nine' ) AND ( NOT o.Name like 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "AND", "<=", "'three'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) AND ( o.Name <= 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name <= 'three' ) AND ( NOT o.Name like 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "AND", ">=", "'three'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) AND ( o.Name >= 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name >= 'three' ) AND ( NOT o.Name like 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "AND", "=", "'three'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) AND ( o.Name = 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name = 'three' ) AND ( NOT o.Name like 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "AND", "<>", "'three'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) AND ( o.Name <> 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name <> 'three' ) AND ( NOT o.Name like 'six' ))"},
        };
    }

    private Object[][] createMultiValueNotLikeOrData() {
        return new Object[][] {
                //NOT LIKE OR others
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "OR", "<", "'nine'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) OR ( o.Name < 'nine' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name < 'nine' ) OR ( NOT o.Name like 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "OR", "<=", "'three'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) OR ( o.Name <= 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name <= 'three' ) OR ( NOT o.Name like 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "OR", ">=", "'three'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) OR ( o.Name >= 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name >= 'three' ) OR ( NOT o.Name like 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "OR", "=", "'three'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) OR ( o.Name = 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name = 'three' ) OR ( NOT o.Name like 'six' ))"},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "OR", "<>", "'three'",
                    EXPECTED_QUERY_BASE + " where (( NOT o.Name like 'six' ) OR ( o.Name <> 'three' ))",
                    EXPECTED_QUERY_BASE + " where (( o.Name <> 'three' ) OR ( NOT o.Name like 'six' ))"},
        };
    }
    
    @DataProvider(name = "multiValueComparisons")
    public Object[][] createMultiValueComparisonData() {
        List<Object[]> data = new ArrayList<Object[]>(150);
        data.addAll(Arrays.asList(createMultiValueComparisonDuplicateData()));
        data.addAll(Arrays.asList(createMultiValueGreaterAndData()));
        data.addAll(Arrays.asList(createMultiValueGreaterOrData()));
        data.addAll(Arrays.asList(createMultiValueLessEqualsAndData()));
        data.addAll(Arrays.asList(createMultiValueLessEqualsOrData()));
        data.addAll(Arrays.asList(createMultiValueGreaterEqualsAndData()));
        data.addAll(Arrays.asList(createMultiValueGreaterEqualsOrData()));
        data.addAll(Arrays.asList(createMultiValueLessAndData()));
        data.addAll(Arrays.asList(createMultiValueLessOrData()));
        data.addAll(Arrays.asList(createMultiValueEqualsAndData()));
        data.addAll(Arrays.asList(createMultiValueEqualsOrData()));
        data.addAll(Arrays.asList(createMultiValueNotEqualsAndData()));
        data.addAll(Arrays.asList(createMultiValueNotEqualsOrData()));
        data.addAll(Arrays.asList(createMultiValueBetweenAndData()));
        data.addAll(Arrays.asList(createMultiValueBetweenOrData()));
        data.addAll(Arrays.asList(createMultiValueNotBetweenAndData()));
        data.addAll(Arrays.asList(createMultiValueNotBetweenOrData()));
        data.addAll(Arrays.asList(createMultiValueInAndData()));
        data.addAll(Arrays.asList(createMultiValueInOrData()));
        data.addAll(Arrays.asList(createMultiValueLikeAndData()));
        data.addAll(Arrays.asList(createMultiValueLikeOrData()));
        data.addAll(Arrays.asList(createMultiValueNotLikeAndData()));
        data.addAll(Arrays.asList(createMultiValueNotLikeOrData()));
        
        // 10 is the number of parameters for testMultiValues(...)
        return data.toArray(new Object[data.size()][10]);
    }
    
    @Test(dataProvider = "multiValueComparisons")
    public void testMultiValues(
            String queryBase,
            String queryType,
            String field,
            String operator1,
            String value1,
            String logicModifier,
            String operator2,
            String value2,
            final String expectedSoqlQuery,
            final String expectedReverseSoqlQuery) {
        
        mockQueryConn.setExpectedSoqlQuery(expectedSoqlQuery);
        
        String query = queryBase + " " + queryType + " " + field + " " + operator1 + " " + value1 + " "
                + logicModifier + " " + field + " " + operator2 + " " + value2;
        em.createQuery(query).getResultList();

        // Reverse the query conditions
        if (expectedReverseSoqlQuery != null) {
            
            mockQueryConn.setExpectedSoqlQuery(expectedReverseSoqlQuery);
            
            query = queryBase + " " + queryType + " " + field + " " + operator2 + " " + value2 + " "
                    + logicModifier + " " + field + " " + operator1 + " " + value1;
            em.createQuery(query).getResultList();
        }
    }
    
    @DataProvider(name = "singleValueHaving")
    public Object[][] createSingleValueHavingData() {
        return new Object[][]{
                {">", "3", EXPECTED_HAVING_QUERY_BASE + " SUM(o.number__c)  > 3"},
                {"=", "3", EXPECTED_HAVING_QUERY_BASE + " SUM(o.number__c)  = 3"},
                {"<", "3", EXPECTED_HAVING_QUERY_BASE + " SUM(o.number__c)  < 3"},
                {">=", "3", EXPECTED_HAVING_QUERY_BASE + " SUM(o.number__c)  >= 3"},
                {"<=", "3", EXPECTED_HAVING_QUERY_BASE + " SUM(o.number__c)  <= 3"},
                {"<>", "3", EXPECTED_HAVING_QUERY_BASE + " SUM(o.number__c)  <> 3"},
                {"BETWEEN", "3 AND 5",
                    EXPECTED_HAVING_QUERY_BASE + "(  SUM(o.number__c)  <= 5 ) AND (  SUM(o.number__c)  >= 3 )"},
                {"NOT BETWEEN", "3 AND 5",
                        EXPECTED_HAVING_QUERY_BASE + "(  SUM(o.number__c)  < 3 ) OR (  SUM(o.number__c)  > 5 )"},
                {"IN", "(3,5)",
                        EXPECTED_HAVING_QUERY_BASE + "(  SUM(o.number__c)  = 3 ) OR (  SUM(o.number__c)  = 5 )"},
                {"NOT IN", "(3,5)",
                        EXPECTED_HAVING_QUERY_BASE + "(  SUM(o.number__c)  <> 3 ) AND (  SUM(o.number__c)  <> 5 )"},
        };
    }
    
    @Test(dataProvider = "singleValueHaving")
    public void testSingleValueHaving(
            String operator,
            String value,
            final String expectedSoqlQuery) {
        
        mockQueryConn.setExpectedSoqlQuery(expectedSoqlQuery);
        
        String query = HAVING_QUERY_BASE + " GROUP BY o.name HAVING sum(o.number) " + operator + " " + value;
        em.createQuery(query).getResultList();
    }
    
    @DataProvider(name = "nullChecks")
    public Object[][] createNullCheckData() {
        return new Object[][]{
            { QUERY_BASE, FIELD_ALPHA, "IS NULL",
                EXPECTED_QUERY_BASE + " where (o.Name = 'NULL')"},
            { QUERY_BASE, FIELD_ALPHA, "IS NOT NULL",
                EXPECTED_QUERY_BASE + " where (o.Name <> 'NULL')"},
        };
    }
    
    @Test(dataProvider = "nullChecks")
    public void testNullCheck(
            String queryBase,
            String field,
            String operator,
            final String expectedSoqlQuery) {
        
        mockQueryConn.setExpectedSoqlQuery(expectedSoqlQuery);
        
        String query = queryBase + " where " + field + " " + operator;
        em.createQuery(query).getResultList();
    }
}
