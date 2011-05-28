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

package com.force.sdk.connector;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * Basic functional tests for ForceServiceConnector.
 *
 * @author Tim Kral
 */
public class BasicForceServiceConnectorTest extends BaseForceServiceConnectorTest {

    @Test
    public void testConnectorClose() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector(config);
        connector.close();
        try {
            connector.getConnection();
            fail("ForceServiceConnector.getConnection should have failed because the connector is closed.");
        } catch (ConnectionException expected) {
            assertTrue(expected.getMessage().contains("No state was found to construct a connection."));
        }
    }
    
    @Test
    public void testGetConnection() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector(config);
        verifyConnection(connector.getConnection());
    }
    
    @Test
    public void testGetMetadataConnection() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector(config);
        verifyConnection(connector.getMetadataConnection());
    }
    
    @Test
    public void testGetNamespace() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector(config);
        verifyNamespace(connector.getNamespace());
    }
    
    @Test
    public void testGetBulkConnection() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector(config);
        verifyConnection(connector.getBulkConnection());
    }
    
    @Test
    public void testRenewSession() throws Exception {
        ConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector();
        connector.renewSession(config);
        verifyConnection(connector.getConnection());
    }
    
    @Test
    public void testResetConnector() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector(config);
        verifyConnection(connector.getConnection());
        
        // After closing, we can no longer get connections (see testConnectorClose)
        connector.close();
        
        // We can reset the connector and get valid connections again
        connector.setConnectorConfig(config);
        verifyConnection(connector.getConnection());
    }
}
