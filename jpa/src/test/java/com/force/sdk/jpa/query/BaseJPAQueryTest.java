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

import java.io.IOException;
import java.util.List;

import javax.persistence.*;

import mockit.*;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.force.sdk.jpa.mock.*;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

/**
 * Base class for Force.com JPA query tests.
 * <p>
 * For these tests, we stub out all schema creation 
 * and loading (see MockPersistenceUtils class). Each
 * test has a mocked PartnerConnection which expects a
 * particular SOQL query. We assert that the given JPA
 * query results in the expected SOQL query. 
 *
 * @author Tim Kral
 */
public abstract class BaseJPAQueryTest {
    
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
        private QueryResult returnedQueryResult;
        private ConnectionException thrownConnectionException;
        
        @Mock
        public QueryResult query(String queryString) throws ConnectionException {
            // Assert any expected SOQL query
            if (expectedSoqlQuery != null) {
                assertEquals(queryString, this.expectedSoqlQuery);
            }
            
            // Throw any registered exceptions
            if (thrownConnectionException != null) {
                throw thrownConnectionException;
            }
            
            // Return any registered results
            if (returnedQueryResult != null) return returnedQueryResult;
            return new QueryResult();
        }
        
        public void setExpectedSoqlQuery(String expectedSoqlQuery) {
            this.expectedSoqlQuery = expectedSoqlQuery;
        }
        
        // Convenience method that allows a test to construct
        // a QueryResult based on the SObjects that are to be returned
        public void setSObjectsForQueryResult(List<SObject> sobjects) {
            returnedQueryResult = new QueryResult();
            returnedQueryResult.setDone(true);
            returnedQueryResult.setRecords(sobjects.toArray(new SObject[sobjects.size()]));
            returnedQueryResult.setSize(sobjects.size());
        }
        
        public void setReturnedQueryResult(QueryResult returnedQueryResult) {
            this.returnedQueryResult = returnedQueryResult;
        }
        
        public void setThrownConnectionException(ConnectionException thrownConnectionException) {
            this.thrownConnectionException = thrownConnectionException;
        }
    }
    
    // The EntityManager used to execute Force.com JPA queries
    protected EntityManager em;
    
    // Schema handler which stores table and field names
    protected MockForceStoreSchemaHandler mockSchemaHandler;
    
    // The mocked connection which can verify the build SOQL query
    // see MockPartnerConnection class above
    protected MockQueryPartnerConnection mockQueryConn;
    
    @BeforeClass
    public void classSetUp() throws IOException, ClassNotFoundException {
        // Register all of our JPA entities.
        // This would normally be done by PersistenceUtils in createSchema or loadSchema
        mockSchemaHandler = new MockForceStoreSchemaHandler();
        mockSchemaHandler.registerAllTables();
        
        Mockit.setUpMocks(MockForceTableMetaData.class, MockForceColumnMetaData.class,
                            MockForceManagedConnection.class, mockSchemaHandler, MockForceSchemaWriter.class);

        // Setup the EntityManager just like normal
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpaQueryUnitTest");
        em = emf.createEntityManager();
    }
    
    @AfterClass(alwaysRun = true)
    public void classTearDown() {
        Mockit.tearDownMocks();
    }
    
    @BeforeMethod
    public void methodSetUp() {
        // Each test method gets its own mocked PartnerConnection
        mockQueryConn = new MockQueryPartnerConnection();
        
        Mockit.setUpMock(PartnerConnection.class, mockQueryConn);
    }
    
    // Convenience methods to help construct query return results
    protected SObject createSObject(String type) {
        return createSObject(type, null);
    }
    
    protected SObject createSObject(String type, String id) {
        // Type must go first and then Id second (see ForceQUeryUtils.getFieldNameList)
        SObject sobject = new SObject();
        sobject.setType(type);
        sobject.setId(id);
        
        return sobject;
    }
}
