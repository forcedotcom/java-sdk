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

import java.util.List;

import org.testng.annotations.*;

import com.force.sdk.jpa.query.entities.DataTypesFTestEntity;
import com.force.sdk.qa.util.jpa.BaseJPAFTest;
import com.google.common.collect.Lists;

/**
 * 
 * Functional tests related to the ORDER BY clause in JPQL select statements.
 *
 * @author Dirk Hain, Tim Kral
 */
public class QueryOrderByFTest extends BaseJPAFTest {

    private static final int DATA_SET_SIZE = 4;
    private static final String QUERY_BASE = "select o from " + DataTypesFTestEntity.class.getSimpleName() + " o "
                                             + "where o.name like 'QueryOrderByFTest%' ";
    
    @BeforeClass(dependsOnMethods = "initialize")
    void initTestData() {
        DataTypesFTestEntity entity1 = new DataTypesFTestEntity();
        entity1.setName("QueryOrderByFTest1");
        entity1.setIntegerObject(1);
        entity1.setLongObject(1L);
        
        DataTypesFTestEntity entity2 = new DataTypesFTestEntity();
        entity2.setName("QueryOrderByFTesty2");
        entity2.setIntegerObject(2);
        entity2.setLongObject(2L);
        
        DataTypesFTestEntity entity3 = new DataTypesFTestEntity();
        entity3.setName("QueryOrderByFTest3");
        entity3.setIntegerObject(3);
        entity3.setLongObject(3L);
        
        DataTypesFTestEntity entity4 = new DataTypesFTestEntity();
        entity4.setName("QueryOrderByFTest4");
        entity4.setIntegerObject(4);
        entity4.setLongObject(1L);
        
        addTestDataInTx(Lists.newArrayList(entity1, entity2, entity3, entity4));
    }
    
    @AfterClass
    public void classTearDown() {
        deleteAll(DataTypesFTestEntity.class.getSimpleName() + " o ", " where name like 'QueryOrderByFTest%'");
    }
    
    /**
     * Test Order By Query Ascending.
     * Execute a JPQL query with an order by clause in ascending order on an entity's integer field.
     * @expectedResults The query results should be sorted in ascending order based on the integer field values.
     * @hierarchy 
     * @userStory 
     */
    @Test
    public void testOrderByQueryAsc() {
        String query = QUERY_BASE + "order by o.integerObject asc";
        List<DataTypesFTestEntity> results = em.createQuery(query, DataTypesFTestEntity.class).getResultList();
        assertEquals(results.size(), DATA_SET_SIZE, "Unexpected number of results for query " + query);
        
        assertEquals(getResultOrder(results), "[ 1 2 3 4 ]", "Unexpected order of results for query " + query);
    }
    
    /**
     * Test Order By Query Descending.
     * Execute a JPQL query with an order by clause in descending order on an entity's integer field.
     * @expectedResults The query results should be sorted in descending order based on the integer field values.
     * @hierarchy 
     * @userStory 
     */
    @Test
    public void testOrderByQueryDesc() {
        String query = QUERY_BASE + "order by o.integerObject desc";
        List<DataTypesFTestEntity> results = em.createQuery(query, DataTypesFTestEntity.class).getResultList();
        assertEquals(results.size(), DATA_SET_SIZE, "Unexpected number of results for query " + query);
        
        assertEquals(getResultOrder(results), "[ 4 3 2 1 ]", "Unexpected order of results for query " + query);
    }
    
    /**
     * Test Order By Query With No Order.
     * Execute a JPQL query with an order by clause without specified order on an entity's integer field.
     * @expectedResults The query results should be sorted in ascending order based on the integer field values.
     * @hierarchy 
     * @userStory 
     */
    @Test
    public void testOrderByQueryWithNoOrder() {
        String query = QUERY_BASE + "order by o.integerObject";
        List<DataTypesFTestEntity> results = em.createQuery(query, DataTypesFTestEntity.class).getResultList();
        assertEquals(results.size(), DATA_SET_SIZE, "Unexpected number of results for query " + query);
        
        assertEquals(getResultOrder(results), "[ 1 2 3 4 ]", "Unexpected order of results for query " + query);
    }
    
    /**
     * Test Order By Query With Multiple Fields.
     * Execute a JPQL query with an order by clause in ascending order on an entity's long field and descending
     * order on an entity's integer field.
     * @expectedResults The query results should be sorted in ascending order based on the long field values, 
     * and then in descending order based on the integer field values.
     * @hierarchy 
     * @userStory 
     */
    @Test
    public void testOrderByQueryWithMultipleFields() {
        String query = QUERY_BASE + "order by o.longObject asc, o.integerObject desc";
        List<DataTypesFTestEntity> results = em.createQuery(query, DataTypesFTestEntity.class).getResultList();
        assertEquals(results.size(), DATA_SET_SIZE, "Unexpected number of results for query " + query);
        
        assertEquals(getResultOrder(results), "[ 4 1 2 3 ]", "Unexpected order of results for query " + query);
    }
    
    private String getResultOrder(List<DataTypesFTestEntity> results) {
        StringBuffer resultOrder = new StringBuffer("[ ");
        for (int i = 0; i < DATA_SET_SIZE; i++) {
            resultOrder.append(results.get(i).getIntegerObject()).append(" ");
        }
        resultOrder.append("]");
        
        return resultOrder.toString();
    }
    
}
