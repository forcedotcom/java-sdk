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

import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.QueryTestEntity;

/**
 * Tests for Force.com JPA query ORDER BY clause.
 * 
 * @author Tim Kral
 */
public class QueryOrderByTest extends BaseJPAQueryTest {

    private static final String QUERY_BASE = "select o.id from " + QueryTestEntity.class.getSimpleName() + " o ";
    private static final String EXPECTED_QUERY_BASE = "select o.Id from querytestentity__c o ";
    
    @Test
    public void testOrderByQueryAsc() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " order by o.number__c ASC ");
        em.createQuery(QUERY_BASE + "order by o.number asc").getResultList();
    }

    @Test
    public void testOrderByQueryDesc() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " order by o.number__c DESC ");
        em.createQuery(QUERY_BASE + "order by o.number desc").getResultList();
    }
    
    @Test
    public void testOrderByQueryWithNoOrder() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " order by o.number__c ASC ");
        em.createQuery(QUERY_BASE + "order by o.number").getResultList();
    }
    
    @Test
    public void testOrderByQueryWithMultipleFields() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE
                + " order by o.Name ASC , o.number__c ASC , o.entityType__c DESC ");
        em.createQuery(QUERY_BASE + "order by o.name, o.number asc, o.entityType desc").getResultList();
    }
}
