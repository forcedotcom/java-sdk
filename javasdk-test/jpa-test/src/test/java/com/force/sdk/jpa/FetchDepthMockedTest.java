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

package com.force.sdk.jpa;

import com.force.sdk.jpa.entities.generated.Entity6;
import com.force.sdk.jpa.query.QueryHints;
import com.force.sdk.qa.util.BaseJPAFTest;
import com.force.sdk.qa.util.TestContext;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.ws.ConnectionException;
import mockit.Instantiation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.datanucleus.jpa.EntityManagerImpl;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.util.Collections;

import static org.testng.Assert.assertEquals;

/**
 * Fetch depth tests that need the mocking framework to intercept queries.
 *
 * @author Jill Wetzler
 */
public class FetchDepthMockedTest extends BaseJPAFTest {

    /**
     * A mock PartnerConnection class.
     * <p>
     * This class can verify expected SOQL query strings and return
     * SOQL query results.
     *
     * @author Tim Kral
     */
    @MockClass(realClass = PartnerConnection.class, instantiation = Instantiation.PerMockSetup)
    protected static class MockQueryPartnerConnection {

        private String expectedSoqlQuery;

        @Mock
        public QueryResult query(String queryString) throws ConnectionException {
            // Assert any expected SOQL query
            if (expectedSoqlQuery != null) {
                assertEquals(queryString, this.expectedSoqlQuery);
            }

            return new QueryResult();
        }

        public void setExpectedSoqlQuery(String expectedSoqlQuery) {
            this.expectedSoqlQuery = expectedSoqlQuery;
        }
    }

    // The mocked connection which can verify the build SOQL query
    // see MockPartnerConnection class above
    protected MockQueryPartnerConnection mockQueryConn;

    @BeforeClass
    public void classSetUp() throws IOException, ClassNotFoundException {
        //setup the mock connection so we can intercept queries
        mockQueryConn = new MockQueryPartnerConnection();
        Mockit.setUpMock(PartnerConnection.class, mockQueryConn);
    }

    @AfterClass(alwaysRun = true)
    public void classTearDown() {
        Mockit.tearDownMocks();
    }

    @DataProvider
    public Object[][] fetchDepthQueries()  {
        Object [][] params = new Object[][]{
            {1, "select id, entity0__r.Id, entity0__r.Name, entity5__r.Id, entity5__r.Name, Name from Entity6__c"},
            {2, "select id, entity0__r.Id, entity0__r.Name, entity5__r.entity0__r.Id, entity5__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.Id, entity5__r.entity4__r.Name, entity5__r.Id, entity5__r.Name, Name "
                    + "from Entity6__c"},
            {3, "select id, entity0__r.Id, entity0__r.Name, entity5__r.entity0__r.Id, entity5__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity0__r.Id, entity5__r.entity4__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.Id, entity5__r.entity4__r.entity3__r.Name, "
                    + "entity5__r.entity4__r.Id, entity5__r.entity4__r.Name, entity5__r.Id, entity5__r.Name, "
                    + "Name from Entity6__c"},
            {4, "select id, entity0__r.Id, entity0__r.Name, entity5__r.entity0__r.Id, entity5__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity0__r.Id, entity5__r.entity4__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity0__r.Id, entity5__r.entity4__r.entity3__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.Id, entity5__r.entity4__r.entity3__r.entity2__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.Id, entity5__r.entity4__r.entity3__r.Name, "
                    + "entity5__r.entity4__r.Id, entity5__r.entity4__r.Name, entity5__r.Id, entity5__r.Name, "
                    + "Name from Entity6__c"},
            {5, "select id, entity0__r.Id, entity0__r.Name, entity5__r.entity0__r.Id, entity5__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity0__r.Id, entity5__r.entity4__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity0__r.Id, entity5__r.entity4__r.entity3__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.entity0__r.Id, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.entity1__r.Id, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.entity1__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.Id, entity5__r.entity4__r.entity3__r.entity2__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.Id, entity5__r.entity4__r.entity3__r.Name, "
                    + "entity5__r.entity4__r.Id, entity5__r.entity4__r.Name, entity5__r.Id, entity5__r.Name, "
                    + "Name from Entity6__c"},
            {-1, "select id, entity0__r.Id, entity0__r.Name, entity5__r.entity0__r.Id, entity5__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity0__r.Id, entity5__r.entity4__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity0__r.Id, entity5__r.entity4__r.entity3__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.entity0__r.Id, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.entity0__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.entity1__r.Id, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.entity1__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.entity2__r.Id, entity5__r.entity4__r.entity3__r.entity2__r.Name, "
                    + "entity5__r.entity4__r.entity3__r.Id, entity5__r.entity4__r.entity3__r.Name, "
                    + "entity5__r.entity4__r.Id, entity5__r.entity4__r.Name, entity5__r.Id, entity5__r.Name, "
                    + "Name from Entity6__c"}
        };

        return params;
    }

    @Test(dataProvider = "fetchDepthQueries")
    public void testFetchDepthQueries(int fetchDepth, String baseQuery) {
        String entityId = "xxx";
        String expectedQuery = String.format("%s o ", baseQuery);
        String expectedFindQuery = String.format("%s where Id='%s'", baseQuery, entityId);
        mockQueryConn.setExpectedSoqlQuery(expectedQuery);
        int oldDepth = ((EntityManagerImpl) em).getFetchPlan().getMaxFetchDepth();
        try {
            ((EntityManagerImpl) em).getFetchPlan().setMaxFetchDepth(fetchDepth);
            em.createQuery("select o from Entity6 o)", Entity6.class).getResultList();

            mockQueryConn.setExpectedSoqlQuery(expectedFindQuery);
            // Test the same with find
            em.find(Entity6.class, entityId);
        } finally {
            ((EntityManagerImpl) em).getFetchPlan().setMaxFetchDepth(oldDepth);
        }

        em.clear();

        mockQueryConn.setExpectedSoqlQuery(expectedQuery);
        em.createQuery("select o from Entity6 o)", Entity6.class)
                    .setHint(QueryHints.MAX_FETCH_DEPTH, fetchDepth).getResultList();

        EntityManager fem = emfac.createEntityManager();
        mockQueryConn.setExpectedSoqlQuery(expectedFindQuery);
        fem.find(Entity6.class, entityId, Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object) fetchDepth));
    }

    @Test
    public void testFetchDepthFromPersistenceUnit() {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("testFetchDepthInPersistenceUnit");
        EntityManager manager = factory.createEntityManager();
        String baseQuery = "select id, entity0__r.Id, entity0__r.Name, entity5__r.Id, entity5__r.Name, Name from Entity6__c";
        String expectedQuery = String.format("%s o ", baseQuery);
        String expectedFindQuery = String.format("%s where Id='%s'", baseQuery, "xxx");
        mockQueryConn.setExpectedSoqlQuery(expectedQuery);
        manager.createQuery("select o from Entity6 o)", Entity6.class).getResultList();
        mockQueryConn.setExpectedSoqlQuery(expectedFindQuery);
        // Test the same with find
        manager.find(Entity6.class, "xxx");
    }
}
