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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.QueryTestEntity;
import com.google.inject.internal.Lists;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Tests for Force.com JPA query results.
 *
 * @author Tim Kral
 */
public class QueryResultTest extends BaseJPAQueryTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCustomResultClass() {
        SObject sobject = createSObject("QueryTestEntity__c");
        sobject.setField("entityType__c", "AAA");
        sobject.setField("number__c", "1.0");
        
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        mockQueryConn.setExpectedSoqlQuery("select o.entityType__c, o.number__c from querytestentity__c o ");
        
        // Write the query to include a custom query result class
        String query = "select new " + CustomQueryResult.class.getName() + "(o.entityType, o.number) "
                         + "from " + QueryTestEntity.class.getSimpleName() + " o";
        
        List<CustomQueryResult> results = em.createQuery(query).getResultList();
        assertEquals(results.size(), 1, "Unexpected number of results for query " + query);
        assertEquals(results.get(0).entityType, "AAA", "Unexpected first record entityType for query " + query);
        assertEquals(results.get(0).number, 1, "Unexpected first record number for query " + query);
    }
    
    @Test
    public void testCreateQueryWithCustomResultClass() {
        SObject sobject = createSObject("QueryTestEntity__c");
        sobject.setField("entityType__c", "AAA");
        sobject.setField("number__c", "1.0");
        
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        
        String query = "select o.entityType, o.number from " + QueryTestEntity.class.getSimpleName() + " o";
        
        // Create the query with a custom query result class
        List<CustomQueryResult> results = em.createQuery(query, CustomQueryResult.class).getResultList();
        assertEquals(results.size(), 1, "Unexpected number of results for query " + query);
        assertEquals(results.get(0).entityType, "AAA", "Unexpected first record entityType for query " + query);
        assertEquals(results.get(0).number, 1, "Unexpected first record number for query " + query);
        assertEquals(results.get(0).discriminator, "non-native", "Unexpected first record discriminator for query " + query);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNativeQueryWithCustomResultClass() {
        SObject sobject = createSObject("QueryTestEntity__c");
        sobject.setField("entityType__c", "AAA");
        sobject.setField("number__c", "1.0");
        sobject.setField("discriminator__c", "native");
        
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));

        String nativeQuery = "select entityType__c, number__c, discriminator__c from querytestentity__c";
        
        /**
         * Our contract with native query is the application handles all data conversion. So, all return values will
         * be Strings and therefore we need a constructor with 3 strings here.
         */
        List<CustomQueryResult> results = em.createNativeQuery(nativeQuery, CustomQueryResult.class).getResultList();
        assertEquals(results.size(), 1, "Unexpected number of results for native query " + nativeQuery);
        assertEquals(results.get(0).entityType, "AAA", "Unexpected first record entityType for native query " + nativeQuery);
        assertEquals(results.get(0).number, 1, "Unexpected first record number for native query " + nativeQuery);
        assertEquals(results.get(0).discriminator, "native",
                        "Unexpected first record discriminator for native query " + nativeQuery);
    }
    
    /**
     * Custom container for query results.
     *
     * @author Fiaz Hossain
     */
    public static class CustomQueryResult {
        public String entityType;
        public int number;
        public String discriminator;
        
        public CustomQueryResult(String entityType, Integer number) {
            this.entityType = entityType;
            this.number = number;
            this.discriminator = "non-native";
        }
        
        public CustomQueryResult(String entityType, String number, String discriminator) {
            this.entityType = entityType;
            this.number = Double.valueOf(number).intValue();
            this.discriminator = discriminator;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNativeQueryWithSObjectResult() {
        SObject sobject = createSObject("QueryTestEntity__c", "a00000000000000AAA");
        sobject.setField("Name", "QueryTestEntity1");
        sobject.setField("entityType__c", "AAA");
        sobject.setField("number__c", "1.0");
        
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        
        String nativeQuery = "select id, name, entityType__c, number__c from querytestentity__c";
        
        // retrieve with limit set get raw sobject back
        List<SObject> results = em.createNativeQuery(nativeQuery).getResultList();
        Assert.assertEquals(results.size(), 1, "Unexpected number of results for native query " + nativeQuery);
        Assert.assertEquals(results.get(0).getField("entityType__c"), "AAA",
                "Unexpected first record entityType for native query " + nativeQuery);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNativeQueryWithResultSetMapping() {
        SObject sobject = createSObject("QueryTestEntity__c", "a00000000000000AAA");
        sobject.setField("Name", "QueryTestEntity1");
        sobject.setField("entityType__c", "AAA");
        sobject.setField("number__c", "1.0");
        
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        
        String nativeQuery = "select id, name, entityType__c, number__c from querytestentity__c";
        String resultSetMapping = "QueryTestMapping"; // See @SqlResultSetMapping 'QueryTestMapping' in QueryTestEntity
        
        List<Object[]> results = em.createNativeQuery(nativeQuery, resultSetMapping).getResultList();
        assertEquals(results.size(), 1, "Unexpected number of results for native query " + nativeQuery);
        
        // Assert the first part of the mapped result is a QueryTestEntity
        assertEquals(results.get(0)[0].getClass(), QueryTestEntity.class,
                "Unexpected @EntityResult type for @SqlResultSetMapping " + resultSetMapping + " "
                + "for native query " + nativeQuery);
        
        QueryTestEntity qteResult = (QueryTestEntity) results.get(0)[0];
        assertEquals(qteResult.getEntityType(), "AAA",
                "Unexpected @EntityResult result for @SqlResultMapping " + resultSetMapping + " "
                + "for native query " + nativeQuery);
        
        // Assert the second part of the mapped result is a String scalar
        assertEquals(results.get(0)[1].getClass(), String.class,
                "Unexpected @ColumnResult type for @SqlResultSetMapping " + resultSetMapping + " "
                + "for native query " + nativeQuery);
        
        // The scalar column maps the name field
        String scalarResult = (String) results.get(0)[1];
        assertEquals(scalarResult, "QueryTestEntity1",
                "Unexpected @ColumnResult result for @SqlResultMapping " + resultSetMapping + " "
                + "for native query " + nativeQuery);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNamedNativeQueryWithResultSetMapping() {
        SObject sobject = createSObject("QueryTestEntity__c", "a00000000000000AAA");
        sobject.setField("Name", "QueryTestEntity1");
        sobject.setField("entityType__c", "AAA");
        sobject.setField("number__c", "1.0");
        
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        
        // See @NamedNativeQuery 'QueryNativeWithResultSetMapping' on QueryTestEntity
        String namedNativeQuery = "QueryNativeWithResultSetMapping";
        List<Object[]> results = em.createNamedQuery(namedNativeQuery).getResultList();
        assertEquals(results.size(), 1, "Unexpected number of results for named native query " + namedNativeQuery);
        
        // Assert the first part of the mapped result is a QueryTestEntity
        assertEquals(results.get(0)[0].getClass(), QueryTestEntity.class,
                "Unexpected @EntityResult type for named native query " + namedNativeQuery);
        
        QueryTestEntity qteResult = (QueryTestEntity) results.get(0)[0];
        assertEquals(qteResult.getEntityType(), "AAA",
                "Unexpected @EntityResult result for named native query " + namedNativeQuery);
        
        // Assert the second part of the mapped result is a String scalar
        assertEquals(results.get(0)[1].getClass(), String.class,
                "Unexpected @ColumnResult type for named native query " + namedNativeQuery);
        
        // The scalar column maps the name field
        String scalarResult = (String) results.get(0)[1];
        assertEquals(scalarResult, "QueryTestEntity1",
                "Unexpected @ColumnResult result for named native query " + namedNativeQuery);
    }
    
    @Test
    public void testFirstResult() {
        ArrayList<SObject> sobjects =
            Lists.newArrayList(
                createSObject("QueryTestEntity__c", "a00000000000000AAA"),
                createSObject("QueryTestEntity__c", "a00000000000001AAA"),
                createSObject("QueryTestEntity__c", "a00000000000002AAA"));
        
        mockQueryConn.setSObjectsForQueryResult(sobjects);
        
        String query = "select o from " + QueryTestEntity.class.getSimpleName() + " o";
        TypedQuery<QueryTestEntity> q = em.createQuery(query, QueryTestEntity.class).setFirstResult(1);
        
        List<QueryTestEntity> results = q.getResultList();
        assertEquals(results.size(), 2, "Unexpected number of results for setFirstResult");
        assertEquals(results.get(0).getId(), "a00000000000001AAA", "Unexpected first result for setFirstResult");
    }

    @Test
    public void testMaxResult() {
        // Max result is controlled at the SOQL query level
        mockQueryConn.setExpectedSoqlQuery("select id, date__c, entityType__c, Name from querytestentity__c o  limit 2");
        
        String query = "select o from " + QueryTestEntity.class.getSimpleName() + " o";
        em.createQuery(query, QueryTestEntity.class).setMaxResults(2);
    }
}
