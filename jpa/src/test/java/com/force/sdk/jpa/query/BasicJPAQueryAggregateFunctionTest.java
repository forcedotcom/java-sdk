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
 * Basic tests for Force.com JPA queries with aggregate functions.
 * <p>
 * For testing strategy see {@link BaseJPAQueryTest}.
 *
 * @author Tim Kral
 */
public class BasicJPAQueryAggregateFunctionTest extends BaseJPAQueryTest {

    @Test
    public void testAvgQuery() {
        mockQueryConn.setExpectedSoqlQuery("select  AVG(o.number__c)  from querytestentity__c o ");
        em.createQuery("select avg(o.number) from " + QueryTestEntity.class.getSimpleName() + " o").getResultList();
    }

    @Test
    public void testCountQuery() {
        mockQueryConn.setExpectedSoqlQuery("select  COUNT() from querytestentity__c o ");
        em.createQuery("select count(o) from " + QueryTestEntity.class.getSimpleName() + " o").getSingleResult();
    }
    
    @Test
    public void testMaxQuery() {
        mockQueryConn.setExpectedSoqlQuery("select  MAX(o.number__c)  from querytestentity__c o ");
        em.createQuery("select max(o.number) from " + QueryTestEntity.class.getSimpleName() + " o").getResultList();
    }
    
    @Test
    public void testMinQuery() {
        mockQueryConn.setExpectedSoqlQuery("select  MIN(o.number__c)  from querytestentity__c o ");
        em.createQuery("select min(o.number) from " + QueryTestEntity.class.getSimpleName() + " o").getResultList();
    }
    
    @Test
    public void testSumQuery() {
        mockQueryConn.setExpectedSoqlQuery("select  SUM(o.number__c)  from querytestentity__c o ");
        em.createQuery("select sum(o.number) from " + QueryTestEntity.class.getSimpleName() + " o").getResultList();
    }
}
