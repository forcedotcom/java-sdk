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

import org.testng.annotations.Test;

/**
 * Tests that get a service connection with ForceServiceConnector constructors.
 *
 * @author Tim Kral
 */
public class ForceServiceConnectorConstructorTest extends BaseForceServiceConnectorTest {
    
    @Test
    public void testGetConnectionFromThreadLocal() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        try {
            ForceServiceConnector.setThreadLocalConnectorConfig(config);
            
            ForceServiceConnector connector = new ForceServiceConnector();
            verifyConnection(connector.getConnection());
        } finally {
            ForceServiceConnector.setThreadLocalConnectorConfig(null);
        }
    }
    
    @Test(dataProvider = "propertyFileConnNameProvider")
    public void testGetConnectionFromPropertyFile(String connectionName) throws Exception {
        ForceServiceConnector connector = new ForceServiceConnector(connectionName);
        verifyConnection(connector.getConnection());
    }
    
    @Test
    public void testGetConnectionFromCliforcePropertyFile() throws Exception {
        String connectionName = "ForceServiceConnectorConstructorTest.testGetConnectionFromCliforcePropertyFile";
        ForceConnectorTestUtils.createCliforceConn(connectionName, createConnectionUrl());
        
        ForceServiceConnector connector = new ForceServiceConnector(connectionName);
        verifyConnection(connector.getConnection());
    }

    @Test
    public void testGetConnectionWithConfig() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector(config);
        verifyConnection(connector.getConnection());
    }
    
    @Test
    public void testGetConnectionWithEnvVarConnUrl() throws Exception {
        // FORCE_ENVVARCONN_URL is defined in pom file
        ForceServiceConnector connector = new ForceServiceConnector("ENVVARCONN");
        verifyConnection(connector.getConnection());
    }
    
    @Test
    public void testGetConnectionWithJavaPropConnUrl() throws Exception {
        String connectionName = "testGetConnectionWithJavaPropConnUrl";
        try {
            System.setProperty("force." + connectionName + ".url", createConnectionUrl());
            
            ForceServiceConnector connector = new ForceServiceConnector(connectionName);
            verifyConnection(connector.getConnection());
        } finally {
            System.clearProperty("force." + connectionName + ".url");
        }
    }
}
