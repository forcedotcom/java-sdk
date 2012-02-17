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

import javax.persistence.TypedQuery;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.query.entities.date.DateTestEntity;
import com.force.sdk.qa.util.jpa.BaseJPAFTest;
import com.google.common.collect.Sets;

/**
 * Functional tests for JPA queries with dates.
 *
 * @author Fiaz Hossain
 */
public class QueryDateTest extends BaseJPAFTest {

    @BeforeClass(dependsOnMethods = "initialize")
    public void initTestData() {
        List<DateTestEntity> dateTestData =
            new ArrayList<DateTestEntity>(2);
        
        // To test date functions, create an entity from two days ago and two days from now
        dateTestData.add(DateTestEntity.init("PastEntity", new Date(System.currentTimeMillis() - 2 * 24 * 3600 * 1000)));
        dateTestData.add(DateTestEntity.init("PresentEntity", new Date()));
        dateTestData.add(DateTestEntity.init("FutureEntity", new Date(System.currentTimeMillis() + 2 * 24 * 3600 * 1000)));
        dateTestData.add(DateTestEntity.init("NullEntity", null));
        
        addTestDataInTx(dateTestData);
    }
    
    @AfterClass
    protected void classTearDown() {
        deleteAll(DateTestEntity.class);
    }
    
    @Test
    public void testCurrentDateQueryNoHint() {
        String query = "select o from " + DateTestEntity.class.getSimpleName() + " o where o.date < CURRENT_DATE";
        List<DateTestEntity> results = em.createQuery(query, DateTestEntity.class).getResultList();
        Assert.assertEquals(results.size(), 1, "Unexpected number of results for query " + query);
    }
    
    @Test
    public void testNullDateQueryNoHint() {
    	String query = "select o from " + DateTestEntity.class.getSimpleName() + " o where o.date is null";
    	List<DateTestEntity> results = em.createQuery(query, DateTestEntity.class).getResultList();
    	Assert.assertEquals(results.size(), 1, "Unexpected number of results for query " + query);
    }
    
    @DataProvider
    protected Object[][] currentDateQueryWithHintProvider() {
        String queryBase = "select o.name from " + DateTestEntity.class.getSimpleName() + " o";
        
        return new Object[][] {
            {queryBase + " where o.date <= CURRENT_DATE", "TOMORROW", Sets.newHashSet("PastEntity", "PresentEntity")},
            // TODO: Fix bug for empty query results.  The results are coming back with a 0L  
            {queryBase + " where o.date > CURRENT_DATE", "NEXT_WEEK", Collections.<String>emptySet()},
            {queryBase + " where o.date <= CURRENT_DATE", "NEXT_N_DAYS:3",
                Sets.newHashSet("PastEntity", "PresentEntity", "FutureEntity")},
            {queryBase + " where o.date < CURRENT_DATE AND o.date > CURRENT_DATE", new String[]{"TOMORROW", "YESTERDAY"},
                Sets.newHashSet("PresentEntity")},
        };
    }
    
    @Test(dataProvider = "currentDateQueryWithHintProvider")
    public void testCurrentDateQueryWithHint(String query, Object queryHintValue, Set<String> expectedResults) {
        TypedQuery<DateTestEntity> q = em.createQuery(query, DateTestEntity.class);
        q.setHint(QueryHints.CURRENT_DATE, queryHintValue);
        
        List<DateTestEntity> results = q.getResultList();
        Assert.assertEquals(results.size(), expectedResults.size(),
                "Unexpected number of results for query " + query + " with hint " + queryHintValue);
        
        if (!expectedResults.isEmpty()) {
            for (DateTestEntity dte : results) {
                Assert.assertTrue(expectedResults.contains(dte.getName()),
                        "Unexpected result for query " + query + " with hint " + queryHintValue + ". "
                        + "Results contained value " + dte.getName() + ", but expected results are " + expectedResults);
            }
        }
    }
}
