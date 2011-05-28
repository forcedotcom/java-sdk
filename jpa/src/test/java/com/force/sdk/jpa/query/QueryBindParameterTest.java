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
 * Tests for parameter binding in Force.com JPA queries.
 * 
 * @author Tim Kral
 */
public class QueryBindParameterTest extends BaseJPAQueryTest {

    private static final String QUERY_BASE = "select o.id from " + QueryTestEntity.class.getSimpleName() + " o ";
    private static final String EXPECTED_QUERY_BASE = "select o.Id from querytestentity__c o ";
    
    @Test
    public void testMultiNumberedParams() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (( o.Name = 'one' ) OR ( o.Name = 'two' ))");
        em.createQuery(QUERY_BASE + "where o.name=?1 or o.name=?2")
            .setParameter(1, "one").setParameter(2, "two").getResultList();
    }
    
    @Test
    public void testMultiNamedParams() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (( o.Name = 'one' ) OR ( o.Name = 'two' ))");
        em.createQuery(QUERY_BASE + "where o.name=:one or o.name=:two")
            .setParameter("one", "one").setParameter("two", "two").getResultList();
    }
    
    @Test
    public void testMixedParamValueTypes() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (( o.Name = 'one' ) OR ( o.number__c = 2 ))");
        
        // NOTE: This query does not work with numbered parameters ?1 and ?2.
        // DataNucleus stores parameters in a zero-based position (i.e. positions 0 and 1)
        // and then does a lookup based on this position:
        //         key 1 => position:0, value:"one"
        //         key 2 => position:1, value=2
        // A lookup on key 1 actually yields the second parameter (at position 1)
        // and then there is a value type mismatch error (Integer versus String).
        // The easiest way around this is starting numbered keys at 0.
        em.createQuery(QUERY_BASE + "where o.name=?0 or o.number=?1")
            .setParameter(0, "one").setParameter(1, 2).getResultList();
    }
}
