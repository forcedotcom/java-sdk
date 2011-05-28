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

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.DataTypesTestEntity;
import com.force.sdk.jpa.entities.QueryTestEntity;
import com.google.inject.internal.Lists;
import com.sforce.soap.partner.fault.ExceptionCode;
import com.sforce.soap.partner.fault.UnexpectedErrorFault;
import com.sforce.soap.partner.sobject.SObject;

/**
 * Negative tests for Force.com JPA queries.
 *
 * @author Tim Kral
 */
public class NegativeJPAQueryTest extends BaseJPAQueryTest {

    @Test
    public void testCurrentTimeQueryUnsupported() {
        try {
            String query = "select o from " + QueryTestEntity.class.getSimpleName() + " o where o.date < CURRENT_TIME";
            em.createQuery(query).getResultList();
            fail("'" + query + "' should have failed because CURRENT_TIME is not supported.");
        } catch (PersistenceException pe) {
            assertTrue(pe.getMessage().contains("CURRENT_TIMESTAMP or CURRENT_TIME is not supported by Force.com datastore"));
        }
    }
    
    @Test
    public void testSelectDistinctQueryUnsupported() {
        try {
            String query = "select distinct o from " + QueryTestEntity.class.getSimpleName() + " o ";
            em.createQuery(query).getResultList();
            fail("'" + query + "' should have failed because Select Distinct is not supported");
        } catch (PersistenceException pe) {
            assertTrue(pe.getMessage().contains("select distinct not supported by force.com datastore"),
                    "Exception contains unexpected message");
        }
    }
    
    @Test
    public void testQueryWithUnAliasedField() {
        SObject sobject = createSObject("QueryTestEntity__c");
        sobject.setField("id", "00ax000deadbeefAAA");
        mockQueryConn.setSObjectsForQueryResult(Lists.newArrayList(sobject));
        
        try {
            // Query should be: select o.id from QueryTestEntity o
            String query = "select id from " + QueryTestEntity.class.getSimpleName() + " o ";
            em.createQuery(query).getResultList();
            fail("'" + query + "' should have failed because the id field is unaliased");
        } catch (PersistenceException expected) {
            assertTrue(expected.getMessage().contains("Could not find alias for field: id"));
        }
    }
    
    @Test
    public void testWhereQueryWithNoResult() {
        mockQueryConn.setExpectedSoqlQuery("select id, date__c, entityType__c, Name, number__c from querytestentity__c qte ");
        String query = "Select qte From " + QueryTestEntity.class.getSimpleName() + " qte";
        try {
            em.createQuery(query).getSingleResult();
            Assert.fail("Expected query to fail because we call getSingleResult with no results. Query: " + query);
        } catch (NoResultException expected) {
            // Exception expected
        }
        
        Assert.assertEquals(em.createQuery(query).getResultList().size(), 0, "Did not expected any results for query: " + query);
    }
    
    @Test
    public void testWhereQueryWithNoUniqueResult() {
        mockQueryConn.setExpectedSoqlQuery("select id, date__c, entityType__c, Name, number__c from querytestentity__c qte ");
        String query = "Select qte From " + QueryTestEntity.class.getSimpleName() + " qte";
        Query q = em.createQuery(query);
        
        try {
            q.getSingleResult();
            fail("getSingleResult should have thrown an exception because we have an invalid id for query " + query);
        } catch (NoResultException expected) {
            // Exception expected
        }

        // Since we've tried to get a unique result (SingleResult) on this query,
        // DataNucleus will mark it as a unique query.  This means a call to getResultList
        // with no results will return null.
        assertNull(q.getResultList(), "Expected null ResultList for query with no unique results " + query);
    }
    
    @Test
    public void testWhereQueryWithInvalidId() {
        mockQueryConn.setExpectedSoqlQuery("select id, date__c, entityType__c, Name, number__c from querytestentity__c qte"
                                            + "  where (qte.Id = 'deadbeef')");
                
        // Throw the exception that we'd get from Force.com
        UnexpectedErrorFault apiFault = new UnexpectedErrorFault();
        apiFault.setExceptionCode(ExceptionCode.INVALID_QUERY_FILTER_OPERATOR);
        apiFault.setExceptionMessage("querytestentity__c qte where (id = 'deadbeef')\n"
                                   + "                              ^               \n"
                                   + "ERROR at Row:1:Column:65\n"
                                   + "invalid ID field: deadbeef");
        
        mockQueryConn.setThrownConnectionException(apiFault);
        
        Query q = em.createQuery("Select qte From " + QueryTestEntity.class.getSimpleName() + " qte Where qte.id='deadbeef'");
        
        try {
            q.getSingleResult();
            fail("getSingleResult should have thrown an exception because we have an invalid id.");
        } catch (NoResultException expected) {
            // Exception expected
        }

        // We are expecting a unique result from the query (since we are using the Id field)
        // This means that if no results are found, we'll get a null value back.
        assertNull(q.getResultList());
    }
    
    @Test
    public void testWhereHavingNoGroupBy() {
        try {
            String query = "select o from " + DataTypesTestEntity.class.getSimpleName() + " o "
                            + "where o.booleanType=true having o.booleanType IN true";
            em.createQuery(query).getResultList();
            fail("'" + query + "' should have failed because there is a HAVING clause with no GROUP BY");
        } catch (PersistenceException expected) {
            assertTrue(expected.getMessage().contains("Queries specifying a HAVING clause must also specify a GROUP BY clause"));
        }
    }
    
    @Test
    public void testWhereHavingNoAggregate() {
        try {
            String query = "select o from " + DataTypesTestEntity.class.getSimpleName() + " o "
                            + "group by o.booleanType having o.booleanType = true";
            em.createQuery(query).getResultList();
            fail("'" + query + "' should have failed because the HAVING clause does not reference an aggregate function");
        } catch (PersistenceException expected) {
            assertTrue(expected.getMessage().contains("HAVING clauses must reference an aggregate function"));
        }
    }
    
    @Test
    public void testMixedParamKeyTypes() {
        try {
            String query = "select o.id from " + QueryTestEntity.class.getSimpleName() + " o where o.name=:one or o.name=?2";
            em.createQuery(query).setParameter("one", "one").setParameter(2, "two").getResultList();
            fail("'" + query + "' should have failed because it is using named and numbered parameters");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Query is using named parameters yet also has \"?2\""),
                    "Exception contains unexpected message");
        }
    }
    
//    W-884381
//    @Test
    public void testJDOQLQuery() {
        try {
            PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("testDNJdo");
            PersistenceManager pm = pmf.getPersistenceManager();
            javax.jdo.Query q = pm.newQuery(QueryTestEntity.class);
            q.compile();
            q.execute();
        } catch (JDODataStoreException jdoe) {
            assertTrue(jdoe.getMessage().contains("SOME GOOD ERROR MESSAGE HERE"),
                    "Received wrong exception on JDOQL query."); //TODO: W-884381
        }
    }
}
