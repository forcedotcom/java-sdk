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

package com.force.sdk.jpa.schema;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Persistence;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.force.sdk.jpa.mock.MockAsyncResultsProcessor;
import com.force.sdk.jpa.mock.MockForceManagedConnection;
import com.force.sdk.jpa.mock.MockForceStoreSchemaHandler;
import com.force.sdk.jpa.mock.MockTableImpl;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

import mockit.Instantiation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

/**
 * 
 * Unit test for schema deletion. Verify that if the purgeOnDeleteSchema persistence property is set that the metadata deploy
 * options get set as well
 *
 * @author Jill Wetzler
 */
public class SchemaDeletionTest {
    
    MockMetadataConnectionDelete mockMetadataConn;
    
    @BeforeClass
    public void classSetUp() throws IOException, ClassNotFoundException {
        // Register all of our JPA entities.
        // This would normally be done by PersistenceUtils in createSchema or loadSchema
        MockForceStoreSchemaHandler mockSchemaHandler = new MockForceStoreSchemaHandler();
        mockSchemaHandler.registerAllTables();
        
        Mockit.setUpMocks(MockForceManagedConnection.class, mockSchemaHandler, MockAsyncResultsProcessor.class,
                MockTableImpl.class);
    }
    
    @AfterClass(alwaysRun = true)
    public void classTearDown() {
        Mockit.tearDownMocks();
    }
    
    @BeforeMethod
    public void methodSetUp() {
        // Each test method gets its own mocked MetadataConnection
        mockMetadataConn = new MockMetadataConnectionDelete();
        
        Mockit.setUpMock(MetadataConnection.class, mockMetadataConn);
    }
    
    /**
     * 
     * Mock class used to verify that the purgeOnDeleteSchema option is set
     * when we expect it to be set.
     *
     * @author Jill Wetzler
     */
    @MockClass(realClass = MetadataConnection.class, instantiation = Instantiation.PerMockSetup)
    public static class MockMetadataConnectionDelete {

        boolean purgeOnDeleteShouldBeSet;
        
        @Mock
        public AsyncResult deploy(byte[] zipFile, DeployOptions deployOptions) throws ConnectionException {
            Assert.assertEquals(deployOptions.getPurgeOnDelete(), purgeOnDeleteShouldBeSet,
                    "The deploy options have purge on delete as " + deployOptions.getPurgeOnDelete()
                    + " but we expected " + purgeOnDeleteShouldBeSet);
            
            AsyncResult result = new AsyncResult();
            result.setDone(true);
            return result;
        }
        
        @Mock
        public DeployResult checkDeployStatus(String processId) {
            DeployResult result = new DeployResult();
            result.setSuccess(true);
            return result;
        }
        
        public void setExpectedPurgeOnDelete(boolean purgeOnDelete) {
            purgeOnDeleteShouldBeSet = purgeOnDelete;
        }
    }
    
    @Test
    public void testDeleteSchema() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("force.deleteSchema", true);
        
        mockMetadataConn.setExpectedPurgeOnDelete(false);
        Persistence.createEntityManagerFactory("testDeleteSchema", props);

        props.put("force.purgeOnDeleteSchema", true);
        mockMetadataConn.setExpectedPurgeOnDelete(true);
        Persistence.createEntityManagerFactory("testDeleteSchema", props);

    }
}
