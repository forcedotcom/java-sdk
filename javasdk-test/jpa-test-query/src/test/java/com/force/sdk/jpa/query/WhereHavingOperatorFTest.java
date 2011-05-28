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

import java.util.*;

import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.query.entities.QueryFTestEntity;
import com.force.sdk.jpa.query.entities.QueryFTestEntity.PickValues;
import com.force.sdk.test.util.BaseJPAFTest;
import com.google.inject.internal.Lists;

/**
 * 
 * Functional tests for WHERE and HAVING clauses for Force.com JPA queries.
 * 
 * NOTE: Unit tests are in WhereHavingOperationTest class.
 *
 * @author John Simone
 */
public class WhereHavingOperatorFTest extends BaseJPAFTest {

    private static final String QUERY_BASE = "select o from " + QueryFTestEntity.class.getSimpleName() + " o";
    private static final  String HAVING_QUERY_BASE = "select o.name from " + QueryFTestEntity.class.getSimpleName() + " o";


    private static final String WHERE = "WHERE";
    private static final String FIELD_NUMBER = "o.number";
    private static final String FIELD_ALPHA = "o.name";
    private static final String FIELD_PICKLIST = "o.pickValueMulti";


    @BeforeClass
    void initTestData() {
        // Add all the data necessary for running basic tests
        List<QueryFTestEntity> whereHavingTestEntityData = new ArrayList<QueryFTestEntity>(12);

        QueryFTestEntity testEntityOne = QueryFTestEntity.init("one", 1, "AAA", new Date(), 0,
                                                                    new PickValues[] {PickValues.ONE});
        QueryFTestEntity testEntityTwo = QueryFTestEntity.init("two", 2, "AAA", new Date(), 0,
                                                                    new PickValues[] {PickValues.TWO});
        QueryFTestEntity testEntityThree = QueryFTestEntity.init("three", 3, "AAA", new Date(), 0,
                                                                    new PickValues[] {PickValues.THREE});

        whereHavingTestEntityData.add(testEntityOne);
        whereHavingTestEntityData.add(testEntityTwo);
        whereHavingTestEntityData.add(testEntityThree);
        whereHavingTestEntityData.add(QueryFTestEntity.init("four", 4, "BBB", new Date(), 0,
                                                                new PickValues[] {PickValues.TWO}));
        whereHavingTestEntityData.add(QueryFTestEntity.init("five", 5, "BBB", new Date(), 0,
                                                                new PickValues[] {PickValues.ONE, PickValues.THREE}));
        whereHavingTestEntityData.add(QueryFTestEntity.init("six", 6, "CCC", new Date(), 0,
                                                                new PickValues[] {}));
        whereHavingTestEntityData.add(QueryFTestEntity.init("seven", 7, "CCC", new Date(), 0,
                                                                new PickValues[] {}));
        whereHavingTestEntityData.add(QueryFTestEntity.init("eight", 8, "DDD", new Date(), 0,
                                                                new PickValues[] {PickValues.ONE}));
        whereHavingTestEntityData.add(QueryFTestEntity.init("nine", 9, "EEE", new Date(), 0,
                                                                new PickValues[] {PickValues.ONE}));
        whereHavingTestEntityData.add(QueryFTestEntity.init("ten", 10, "FFF", new Date(), 0, new PickValues[] {PickValues.ONE}));
        whereHavingTestEntityData.add(QueryFTestEntity.init("eleven", 11, "FFF", new Date(), 0,
                                                                new PickValues[] {PickValues.ONE}));
        whereHavingTestEntityData.add(QueryFTestEntity.init("twelve", 12, "FFF", new Date(), 0,
                                                                new PickValues[] {PickValues.ONE}));

        // Add duplicate record for group by and having tests
        whereHavingTestEntityData.add(QueryFTestEntity.init("nine", 9, "GGG", new Date(), 0, new PickValues[] {PickValues.ONE}));

        // To test date functions, create an entity from two days ago and two days from now
        whereHavingTestEntityData.add(QueryFTestEntity.init("Past entity", 100, "XXX",
                                                                new Date(System.currentTimeMillis() - 2 * 24 * 3600 * 1000),
                                                                0, new PickValues[] {PickValues.ONE}));
        whereHavingTestEntityData.add(QueryFTestEntity.init("Future entity", 101, "XXX",
                                                                new Date(System.currentTimeMillis() + 2 * 24 * 3600 * 1000),
                                                                0, new PickValues[] {PickValues.ONE}));

        addTestDataInTx(whereHavingTestEntityData);
    }

    @DataProvider(name = "singleValueComparisons")
    public Object[][] createSingleValueComparisonData() {

        return new Object[][]{
                {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", Integer.valueOf(1)},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'%'", Integer.valueOf(15)},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'%'", Integer.valueOf(0)},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "=", "'one'", Integer.valueOf(1)},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "6 AND 8", Integer.valueOf(3)},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "NOT BETWEEN", "6 AND 8", Integer.valueOf(12)},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "BETWEEN", "'six' AND 'eight'", Integer.valueOf(0)},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT BETWEEN", "'six' AND 'eight'", Integer.valueOf(15)},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "IN", "('six','eight','three')", Integer.valueOf(3)},
                {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT IN", "('six','eight','nine')", Integer.valueOf(11)},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "IN", "(6,8,9)", Integer.valueOf(4)},
                {QUERY_BASE, WHERE, FIELD_NUMBER, "NOT IN", "(6,8,9)", Integer.valueOf(11)},
                {QUERY_BASE, WHERE, "'TWO'", "MEMBER OF", FIELD_PICKLIST, Integer.valueOf(2)},
                {QUERY_BASE, WHERE,  "'ONE'", "NOT MEMBER OF", FIELD_PICKLIST, Integer.valueOf(5)},
                {QUERY_BASE, WHERE, FIELD_PICKLIST, "IS EMPTY", null, Integer.valueOf(2)},
                {QUERY_BASE, WHERE,  FIELD_PICKLIST, "IS NOT EMPTY", null, Integer.valueOf(13)},
        };
    }

    @DataProvider(name = "multiValueComparisons")
    public Object[][] createMultiValueComparisonData() {
        return new Object[][]{
            //> AND others
            {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "AND", "=", "3", Integer.valueOf(0), true},
            //> OR others
            {QUERY_BASE, WHERE, FIELD_NUMBER, ">", "6", "OR", "=", "3", Integer.valueOf(10), true},
            //<= AND others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "AND", "=", "3", Integer.valueOf(1), true},
            //<= OR others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "<=", "6", "OR", "=", "3", Integer.valueOf(6), true},
            //>= AND others
            {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "AND", "=", "3", Integer.valueOf(0), true},
            //>= OR others
            {QUERY_BASE, WHERE, FIELD_NUMBER, ">=", "6", "OR", "=", "3", Integer.valueOf(11), true},
            //< AND others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "AND", "=", "3", Integer.valueOf(1), true},
            //< OR others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "<", "6", "OR", "=", "3", Integer.valueOf(5), true},
            //= AND others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "AND", "<>", "3", Integer.valueOf(1), true},
            //= OR others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "=", "6", "OR", "<>", "3", Integer.valueOf(14), true},
            //<> AND others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "AND", "BETWEEN", "4 AND 9", Integer.valueOf(6), true},
            //<> OR others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "<>", "6", "OR", "BETWEEN", "4 AND 9", Integer.valueOf(15), true},
            //BETWEEN AND others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "BETWEEN", "6 AND 13", "AND", "NOT BETWEEN", "3 AND 8", Integer.valueOf(5), true},
            //IN AND others
            {QUERY_BASE, WHERE, FIELD_NUMBER, "IN", "(6,7,8)", "AND", "NOT IN", "(3,6,7,8,9)", Integer.valueOf(0), true},
            //LIKE AND others
            {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "AND", "=", "'three'", Integer.valueOf(0), true},
            //LIKE OR others
            {QUERY_BASE, WHERE, FIELD_ALPHA, "LIKE", "'six'", "OR", "=", "'three'", Integer.valueOf(2), true},
            //NOT LIKE AND others
            {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "AND", "=", "'three'", Integer.valueOf(1), true},
            //NOT LIKE OR others
            {QUERY_BASE, WHERE, FIELD_ALPHA, "NOT LIKE", "'six'", "OR", "=", "'three'", Integer.valueOf(14), true},
        };
    }

    @DataProvider(name = "singleValueHaving")
    public Object[][] createSingleValueHavingData() {
        return new Object[][]{
                {">", "3", Integer.valueOf(11)},
                {"=", "3", Integer.valueOf(1)},
                {"<", "3", Integer.valueOf(2)},
                {">=", "3", Integer.valueOf(12)},
                {"<=", "3", Integer.valueOf(3)},
                {"<>", "3", Integer.valueOf(13)},
                {"BETWEEN", "3 AND 5", Integer.valueOf(3)},
                {"NOT BETWEEN", "3 AND 5", Integer.valueOf(11)},
                {"IN", "(2,4,6,8,9,18)", Integer.valueOf(5)},
                {"NOT IN", "(2,4,6,8,9,18)", Integer.valueOf(9)},
        };
    }

    @DataProvider(name = "nullChecks")
    public Object[][] createNullCheckData() {
        return new Object[][]{
            { QUERY_BASE, FIELD_ALPHA, "IS NULL", Integer.valueOf(0)},
            { QUERY_BASE, FIELD_ALPHA, "IS NOT NULL", Integer.valueOf(15)},
        };
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "singleValueComparisons")
    public void testSingleValues(
            String queryBase,
            String queryType,
            String field,
            String operator,
            String value,
            Integer count) {
        String query = queryBase + " " + queryType + " " + field + " " + operator + (value != null ? " " + value : "");
        try {
            List<QueryFTestEntity> resultList = em.createQuery(query).getResultList();
            Assert.assertEquals(resultList.size(), count.intValue(), query);
        } catch (RuntimeException e) {
            throw new RuntimeException(query, e);
        }
    }

    @SuppressWarnings("unchecked")
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
            Integer count,
            Boolean runReverse) {
        String query = queryBase + " " + queryType + " " + field + " " + operator1 + " " + value1 + " "
                + logicModifier + " " + field + " " + operator2 + " " + value2;
        List<QueryFTestEntity> resultList = null;
        try {
            resultList = em.createQuery(query).getResultList();
            Assert.assertEquals(resultList.size(), count.intValue(), query);
        } catch (RuntimeException e) {
            throw new RuntimeException(query, e);
        }

        if (runReverse) {
            query = queryBase + " " + queryType + " " + field + " " + operator2 + " " + value2 + " "
                    + logicModifier + " " + field + " " + operator1 + " " + value1;
            try {
                resultList = em.createQuery(query).getResultList();
                Assert.assertEquals(resultList.size(), count.intValue(), query);
            } catch (RuntimeException e) {
                throw new RuntimeException(query, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "singleValueHaving")
    public void testSingleValueHaving(
            String operator,
            String value,
            Integer count) {
        String query = WhereHavingOperatorFTest.HAVING_QUERY_BASE + " GROUP BY o.name HAVING sum(o.number) "
                        + operator + " " + value;
        try {
            List<QueryFTestEntity> resultList = em.createQuery(query).getResultList();
            Assert.assertEquals(resultList.size(), count.intValue(), query);
        } catch (RuntimeException e) {
            throw new RuntimeException(query, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "nullChecks")
    public void testNullCheck(
            String queryBase,
            String field,
            String operator,
            Integer count) {
        String query = queryBase + " where " + field + " " + operator;
        try {
            List<QueryFTestEntity> resultList = em.createQuery(query).getResultList();
            Assert.assertEquals(resultList.size(), count.intValue(), query);
        } catch (RuntimeException e) {
            throw new RuntimeException(query, e);
        }
    }

    @AfterClass
    public void classTearDown() {
        deleteTestDataInTx(Lists.<Class>newArrayList(QueryFTestEntity.class));
    }

    private void deleteTestDataInTx(List<Class> classList) {
        EntityTransaction tx = em.getTransaction();
        if (tx.isActive()) {
            em.joinTransaction();
        } else {
            tx.begin();
        }
        try {
            for (Class clazz : classList) {
                em.createQuery("delete from " + clazz.getSimpleName()).executeUpdate();
            }

            tx.commit();
            tx = null;
        } finally {
            if (tx != null) {
                tx.rollback();
            }
        }
    }

}
