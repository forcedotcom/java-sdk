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

import com.force.sdk.jpa.entities.BasicChildTestEntity;
import com.force.sdk.jpa.entities.BasicParentTestEntity;

/**
 * Basic tests for Force.com JPA query joins.
 * <p>
 * For testing strategy see {@link BaseJPAQueryTest}.
 *
 * @author Tim Kral
 */
public class BasicJPAQueryJoinTest extends BaseJPAQueryTest {
    
    @Test
    public void testBasicSemiJoin() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, (select id, Name from BasicParentTestEntity_basicchildtestenti__r), Name "
                + "from basicparenttestentity__c p  where "
                + "(id in (select BasicParentTestEntity__c from basicchildtestentity__c c  "
                        + "where (( c.Name = 'child1' ) OR ( c.Name = 'child2' ))))");
        
        em.createQuery("select p from " + BasicParentTestEntity.class.getSimpleName() + " p "
                       + "INNER JOIN p.childEntities c "
                       + "where c.name in ('child1', 'child2')").getResultList();
    }
    
    @Test
    public void testSemiJoinWithSubQuery() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, (select id, Name from BasicParentTestEntity_basicchildtestenti__r), Name "
                + "from basicparenttestentity__c p  where "
                + "(Id IN (select c.BasicParentTestEntity__c from basicchildtestentity__c c  "
                        + "where (( c.Name = 'child1' ) OR ( c.Name = 'child2' ))))");
        
        em.createQuery("select p from " + BasicParentTestEntity.class.getSimpleName() + " p "
                       + "where id in "
                        + "(select c.parent from " + BasicChildTestEntity.class.getSimpleName() + " c "
                         + "where c.name in ('child1', 'child2'))").getResultList();
    }
    
    @Test
    public void testSemiJoinWithRelationship() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, Name, BasicParentTestEntity__r.Id, BasicParentTestEntity__r.Name "
                + "from basicchildtestentity__c c  where "
                + "(( c.BasicParentTestEntity__r.Name = 'parent1' ) OR ( c.BasicParentTestEntity__r.Name = 'parent2' ))");
        
        em.createQuery("select c from " + BasicChildTestEntity.class.getSimpleName() + " c "
                       + "where c.parent.name in ('parent1', 'parent2')").getResultList();
    }
    
    @Test
    public void testSemiJoinWithIN() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, (select id, Name from BasicParentTestEntity_basicchildtestenti__r), Name "
                + "from basicparenttestentity__c p  where "
                + "(id in (select BasicParentTestEntity__c from basicchildtestentity__c c  "
                        + "where (( c.Name = 'child1' ) OR ( c.Name = 'child2' ))))");
        
        em.createQuery("select p from " + BasicParentTestEntity.class.getSimpleName() + " p, "
                       + "IN(p.childEntities) c where c.name in ('child1', 'child2')").getResultList();
    }
    
    @Test
    public void testSemiJoinWithNativeQuery() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, name from basicparenttestentity__c where "
                + "id in (select BasicParentTestEntity__c from basicchildtestentity__c "
                       + "where name in ('child1', 'child2'))");
        
        em.createNativeQuery("select id, name from basicparenttestentity__c where "
                             + "id in (select BasicParentTestEntity__c from basicchildtestentity__c "
                                    + "where name in ('child1', 'child2'))").getResultList();
    }
    
    @Test
    public void testBasicAntiJoin() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, (select id, Name from BasicParentTestEntity_basicchildtestenti__r), Name "
                + "from basicparenttestentity__c p  where "
                + "(id in (select BasicParentTestEntity__c from basicchildtestentity__c c  "
                        + "where (c.Name <> 'child2')))");
        
        em.createQuery("select p from " + BasicParentTestEntity.class.getSimpleName() + " p "
                       + "INNER JOIN p.childEntities c "
                       + "where c.name not in ('child2')").getResultList();
    }
    
    @Test
    public void testAntiJoinWithSubQuery() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, (select id, Name from BasicParentTestEntity_basicchildtestenti__r), Name "
                + "from basicparenttestentity__c p  where "
                + "(p.Id NOT IN (select c.BasicParentTestEntity__c from basicchildtestentity__c c  "
                              + "where (( c.Name = 'child1' ) OR ( c.Name = 'child2' ))))");
        
        em.createQuery("select p from " + BasicParentTestEntity.class.getSimpleName() + " p "
                       + "where p.id not in "
                       + "(select c.parent "
                        + "from " + BasicChildTestEntity.class.getSimpleName() + " c "
                        + "where c.name in ('child1', 'child2'))").getResultList();
    }
    
    @Test
    public void testAntiJoinWithRelationship() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, Name, BasicParentTestEntity__r.Id, BasicParentTestEntity__r.Name "
                + "from basicchildtestentity__c c  "
                + "where (( c.BasicParentTestEntity__r.Name <> 'parent1' )"
                        + " AND ( c.BasicParentTestEntity__r.Name <> 'parent2' ))");
        
        em.createQuery("select c from " + BasicChildTestEntity.class.getSimpleName() + " c "
                       + "where c.parent.name not in ('parent1', 'parent2')").getResultList();
    }
    
    @Test
    public void testAntiJoinWithWithIN() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, (select id, Name from BasicParentTestEntity_basicchildtestenti__r), Name "
                + "from basicparenttestentity__c p  where "
                + "(id in (select BasicParentTestEntity__c from basicchildtestentity__c c  "
                        + "where (( c.Name <> 'child1' ) AND ( c.Name <> 'child2' ))))");
        
        em.createQuery("select p from " + BasicParentTestEntity.class.getSimpleName() + " p, "
                       + "IN(p.childEntities) c where c.name not in ('child1', 'child2')").getResultList();
    }

    @Test
    public void testAntiJoinWithNativeQuery() {
        mockQueryConn.setExpectedSoqlQuery(
                "select id, name from basicparenttestentity__c where "
                + "id not in (select BasicParentTestEntity__c from basicchildtestentity__c "
                           + "where name in ('child1'))");
        
        em.createNativeQuery("select id, name from basicparenttestentity__c where "
                             + "id not in (select BasicParentTestEntity__c from basicchildtestentity__c "
                                        + "where name in ('child1'))").getResultList();
    }
}
