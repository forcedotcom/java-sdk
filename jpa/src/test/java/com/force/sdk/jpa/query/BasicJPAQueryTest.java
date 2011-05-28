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
import java.util.List;

import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.QueryTestEntity;
import com.google.inject.internal.Lists;

/**
 * Basic tests for Force.com JPA queries.
 * <p>
 * For testing strategy see {@link BaseJPAQueryTest}.
 *
 * @author Tim Kral
 */
public class BasicJPAQueryTest extends BaseJPAQueryTest {
    
    private static final String QUERY_BASE = "select o from " + QueryTestEntity.class.getSimpleName() + " o ";
    private static final String EXPECTED_QUERY_BASE =
        "select id, date__c, entityType__c, Name, number__c from querytestentity__c o ";
    
    @Test
    public void testBasicQuery() throws Exception {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE);
        em.createQuery(QUERY_BASE).getResultList();
    }
    
    @Test
    public void testSelectIdQuery() {
        mockQueryConn.setExpectedSoqlQuery("select o.Id from querytestentity__c o ");
        em.createQuery("select o.id from " + QueryTestEntity.class.getSimpleName() + " o").getResultList();
    }
    
    @Test
    public void testWhereQueryWithNameField() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.Name = 'five')");
        em.createQuery(QUERY_BASE + "where o.name='five'").getResultList();
    }
    
    @Test
    public void testWhereQueryWithCustomField() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.number__c = 5)");
        em.createQuery(QUERY_BASE + "where o.number=5").getResultList();
    }
    
    @Test
    public void testWhereQueryWithNamedParam() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.Name = 'five')");
        em.createQuery(QUERY_BASE + "where o.name=:nameValue").setParameter("nameValue", "five").getResultList();
    }
    
    @Test
    public void testWhereQueryWithNumberedParam() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.Name = 'five')");
        em.createQuery(QUERY_BASE + "where o.name=?1").setParameter(1, "five").getResultList();
    }
    
    @Test
    public void testWhereLikeQuery() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (Name like 't%')");
        em.createQuery(QUERY_BASE + "where name like 't%'").getResultList();
    }
    
    @Test
    public void testWhereNotLikeQuery() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (NOT Name like 't%')");
        em.createQuery(QUERY_BASE + "where name not like 't%'").getResultList();
    }

    @Test
    public void testWhereLikeQueryWithParamCharBind() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (Name like '%')");
        
        char p = '%';
        em.createQuery(QUERY_BASE + "where name like :param").setParameter("param", p).getResultList();
    }
    
    @Test
    public void testWhereLikeQueryWithParamCharacterBind() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (Name like '%')");
        em.createQuery(QUERY_BASE + "where name like :param").setParameter("param", '%').getResultList();
    }
    
    @Test
    public void testOrderByQuery() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " order by o.Name ASC ");
        em.createQuery(QUERY_BASE + "order by o.name").getResultList();
    }

    @Test
    public void testGroupByQueryWithOneField() {
        mockQueryConn.setExpectedSoqlQuery("select o.entityType__c from querytestentity__c o  group by o.entityType__c");
        em.createQuery("select o.entityType from " + QueryTestEntity.class.getSimpleName()
                        + " o group by o.entityType").getResultList();
    }
    
    @Test
    public void testGroupByQueryWithMultipleFields() {
        mockQueryConn.setExpectedSoqlQuery("select o.Name,  MAX(o.number__c)  from querytestentity__c o  group by o.Name");
        em.createQuery("select o.name, max(o.number) from " + QueryTestEntity.class.getSimpleName()
                        + " o group by o.name").getResultList();
    }
    
    @Test
    public void testGroupByQueryWithOrderBy() {
        mockQueryConn.setExpectedSoqlQuery(
                "select o.Name,  MAX(o.number__c)  from querytestentity__c o  where (o.Name like 't%')"
                + " group by o.Name order by o.Name ASC ");
        
        em.createQuery("select o.name, max(o.number) from " + QueryTestEntity.class.getSimpleName() + " o "
                       + "where o.name like 't%' group by o.name order by o.name").getResultList();
    }

    @Test
    public void testHavingQuery() {
        mockQueryConn.setExpectedSoqlQuery(
                "select o.entityType__c,  MAX(o.number__c)  from querytestentity__c o"
                + "  group by o.entityType__c having  MAX(o.number__c)  = 9 order by o.entityType__c ASC ");
        
        em.createQuery("select o.entityType, max(o.number) from " + QueryTestEntity.class.getSimpleName() + " o "
                       + "group by o.entityType having max(o.number)=9 order by o.entityType").getResultList();
    }
    
    @Test
    public void testInQuery() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE
                + " where (( o.entityType__c = 'AAA' ) OR ( o.entityType__c = 'DDD' ))");
        em.createQuery(QUERY_BASE + "where o.entityType in ('AAA', 'DDD')").getResultList();
    }
    
    @Test
    public void testInQueryWithParamEmptyListBind() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.entityType__c IN (NULL))");
        
        List<String> emptyList = new ArrayList<String>();
        em.createQuery(QUERY_BASE + "where o.entityType in ?1").setParameter(1, emptyList).getResultList();
    }
    
    @Test
    public void testInQueryWithParamIntegerListBind() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.number__c IN (1,2))");
        
        List<Integer> intList =  Lists.<Integer>newArrayList(1, 2);
        em.createQuery(QUERY_BASE + "where o.number in ?1").setParameter(1, intList).getResultList();
    }
    
    @Test
    public void testInQueryWithParamStringListBind() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.entityType__c IN ('AAA','DDD'))");
        
        List<String> stringList = Lists.<String>newArrayList("AAA", "DDD");
        em.createQuery(QUERY_BASE + "where o.entityType in ?1").setParameter(1, stringList).getResultList();
    }
    
    @Test
    public void testNotInQuery() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE
                + " where (( o.entityType__c <> 'AAA' ) AND ( o.entityType__c <> 'BBB' ))");
        em.createQuery(QUERY_BASE + "where o.entityType not in ('AAA', 'BBB')").getResultList();
    }
    
    @Test
    public void testNotInQueryWithParams() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE
                + " where (( o.entityType__c <> 'AAA' ) AND ( o.entityType__c <> 'BBB' ))");
        em.createQuery(QUERY_BASE + "where o.entityType not in (:first, :second)")
            .setParameter("first", "AAA").setParameter("second", "BBB").getResultList();
    }
    
    @Test
    public void testCurrentDateQuery() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.date__c >=  YESTERDAY)");
        em.createQuery(QUERY_BASE + "where o.date >= CURRENT_DATE").setHint(QueryHints.CURRENT_DATE, "YESTERDAY").getResultList();
    }
    
    @Test
    public void testNamedJPQLQuery() {
        mockQueryConn.setExpectedSoqlQuery(EXPECTED_QUERY_BASE + " where (o.Name = 'five')");
        
        // See @NamedQuery 'BasicJPQLQuery' on QueryTestEntity
        em.createNamedQuery("BasicJPQLQuery").getResultList();
    }
    
    @Test
    public void testNativeQuery() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, name, entityType__c, number__c from querytestentity__c "
                + "where entityType__c not in ('FFF') order by entityType__c limit 3");
        
        em.createNativeQuery("select id, name, entityType__c, number__c from querytestentity__c "
                             + "where entityType__c not in ('FFF') order by entityType__c limit 3").getResultList();
    }
    
    @Test
    public void testNamedNativeQuery() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, name, entityType__c, number__c from QueryTestEntity__c\n"
                + "where entityType__c not in ('AAA') order by entityType__c limit 3");
        
        // See @NamedNativeQuery 'QueryNativeBasic' on QueryTestEntity
        em.createNamedQuery("QueryNativeBasic").getResultList();
    }
}
