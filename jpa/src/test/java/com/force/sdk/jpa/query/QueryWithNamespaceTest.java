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

import com.force.sdk.jpa.entities.NamespacedTestEntity;

/**
 * Tests for constructing Force.com JPA queries
 * with a namespace.
 * 
 * @author Tim Kral
 */
public class QueryWithNamespaceTest extends BaseJPAQueryTest {

    private static final String QUERY_BASE = "select o from " + NamespacedTestEntity.class.getSimpleName() + " o ";
    private static final String EXPECTED_QUERY_BASE =
        "select id, ns__date__c, ns__entityType__c, Name, ns__number__c from ns__namespacedtestentity__c o ";
    
    @Test
    public void testBasicQueryWithNamespace() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE);
        em.createQuery(QUERY_BASE).getResultList();
    }

    @Test
    public void testWhereQueryWithNamespace() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.ns__number__c = 5)");
        em.createQuery(QUERY_BASE + "where o.number=5").getResultList();
    }

    @Test
    public void testOrderByQueryWithNamespace() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " order by o.ns__number__c ASC ");
        em.createQuery(QUERY_BASE + "order by o.number asc").getResultList();
    }
    
    @Test
    public void testGroupByQueryWithNamespace() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " group by o.ns__entityType__c");
        em.createQuery(QUERY_BASE + "group by o.entityType").getResultList();
    }
    
    @Test
    public void testHavingQueryWithNamespace() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE
                + " group by o.ns__entityType__c having  MAX(o.ns__number__c)  = 9");
        em.createQuery(QUERY_BASE + "group by o.entityType having max(o.number)=9").getResultList();
    }
}
