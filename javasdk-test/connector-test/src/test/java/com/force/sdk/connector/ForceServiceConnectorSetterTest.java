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

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests for ForceServiceConnector setters.
 *
 * @author Tim Kral
 */
public class ForceServiceConnectorSetterTest extends BaseForceServiceConnectorTest {

    @Test(dataProvider = "propertyFileConnNameProvider")
    public void testGetConnectionFromPropertyFile(String connectionName) throws Exception {
        ForceServiceConnector connector = new ForceServiceConnector();
        connector.setConnectionName(connectionName);
        verifyConnection(connector.getConnection());
    }
    
    @Test
    public void testGetConnectionWithConfig() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector();
        connector.setConnectorConfig(config);
        verifyConnection(connector.getConnection());
    }
    
    @Test
    public void testGetConnectionWithEnvVarConnUrl() throws Exception {
        // FORCE_ENVVARCONN_URL is defined in pom file
        ForceServiceConnector connector = new ForceServiceConnector();
        connector.setConnectionName("ENVVARCONN");
        verifyConnection(connector.getConnection());
    }
    
    @Test
    public void testGetConnectionWithJavaPropConnUrl() throws Exception {
        String connectionName = "testGetConnectionWithJavaPropConnUrl";
        try {
            System.setProperty("force." + connectionName + ".url", createConnectionUrl());
            
            ForceServiceConnector connector = new ForceServiceConnector();
            connector.setConnectionName(connectionName);
            verifyConnection(connector.getConnection());
        } finally {
            System.clearProperty("force." + connectionName + ".url");
        }
    }
    
    @Test
    public void testSetClientId() throws Exception {
        ForceConnectorConfig config = createConfig();

        ForceServiceConnector connector = new ForceServiceConnector(config);
        connector.setClientId("testSetClientId");

        PartnerConnection conn = connector.getConnection();
        assertEquals(conn.getCallOptions().getClient(), "testSetClientId");

        MetadataConnection mdConn = connector.getMetadataConnection();
        assertEquals(mdConn.getCallOptions().getClient(), "testSetClientId");
    }

    @Test
    public void testSetTimeout() throws Exception {
        ForceConnectorConfig config = createConfig();

        ForceServiceConnector connector = new ForceServiceConnector(config);
        connector.setTimeout(1000);

        PartnerConnection conn = connector.getConnection();
        assertEquals(conn.getConfig().getReadTimeout(), 1000);
    }

    @Test
    public void testUseClientIdOnConnector() throws Exception {
        ForceConnectorConfig config = createConfig();
        config.setClientId("testUseLatestClientId1");

        ForceServiceConnector connector = new ForceServiceConnector();
        connector.setConnectorConfig(config);
        connector.setClientId("testUseLatestClientId2");

        PartnerConnection conn = connector.getConnection();
        assertEquals(conn.getCallOptions().getClient(), "testUseLatestClientId2");

        MetadataConnection mdConn = connector.getMetadataConnection();
        assertEquals(mdConn.getCallOptions().getClient(), "testUseLatestClientId2");
    }
    
    @Test
    public void testUseTimeoutOnConnector() throws Exception {
        ForceConnectorConfig config = createConfig();
        config.setReadTimeout(1000);
        
        ForceServiceConnector connector = new ForceServiceConnector();
        connector.setConnectorConfig(config);
        connector.setTimeout(2000);
        
        PartnerConnection conn = connector.getConnection();
        assertEquals(conn.getConfig().getReadTimeout(), 2000);
    }
}
