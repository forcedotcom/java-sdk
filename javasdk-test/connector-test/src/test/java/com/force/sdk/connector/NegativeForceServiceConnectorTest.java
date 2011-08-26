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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sforce.ws.ConnectionException;

/**
 * Negative functional tests for ForceServiceConnector.
 *
 * @author Tim Kral
 */
public class NegativeForceServiceConnectorTest extends BaseForceServiceConnectorTest {
    
    @Test
    public void testCannotResetConnectorAfterGetConnection() throws Exception {
        ForceConnectorConfig config = createConfig();
        
        ForceServiceConnector connector = new ForceServiceConnector(config);
        verifyConnection(connector.getConnection());
        
        // Once we've gotten a connection, we cannot reset
        // the connector without closing first.
        
        // Add an invalid ConnectorConfig.
        connector.setConnectorConfig(new ForceConnectorConfig());
        
        // The connector still gets valid connections
        // because it hasn't been reset.
        verifyConnection(connector.getConnection());
    }
    
    @Test
    public void testEqualsSignInConnectionUrl() throws Exception {
        try {
            System.setProperty("force.testMissingStateInConnectionUrl.url", "force://url?useruser&password=password");
            
            ForceServiceConnector connector = new ForceServiceConnector("testMissingStateInConnectionUrl");
            connector.getConnection();
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("The ForceConnectionProperty (user) must have a value"));
        } finally {
            System.clearProperty("force.testMissingStateInConnectionUrl.url");
        }
    }

    @DataProvider
    protected Object[][] badPropertyFileProvider() {
        // Test property files defined in /src/test/resources 
        return new Object[][] {
            {"funcconnbadtimeout", "timeout", "abc"},
            {"funcconnmissingendpoint", "endpoint", null},
        };
    }

    @Test(dataProvider = "badPropertyFileProvider")
    public void testGetConnectionWithBadPropertyFile(String connectionName, String badProperty, String badValue)
    throws Exception {
        try {
            ForceServiceConnector connector = new ForceServiceConnector(connectionName);
            connector.getConnection();
            fail("ForceServiceConnector.getConnection should have failed because property (" + badProperty + ") is bad");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains(badProperty));
            if (badValue != null) {
                assertTrue(expected.getMessage().contains(badValue));
            }
        }
    }

    @Test
    public void testNoEnvVariableOrJavaProperty() throws Exception {
        // No environment variable or java property with this name
        ForceServiceConnector connector = new ForceServiceConnector("testNoEnvVariableOrJavaProperty");
        
        try {
            connector.getConnection();
            fail("ForceServiceConnector.getConnection should have failed because"
                    + " there are no registered env variables or java properties");
        } catch (ConnectionException expected) {
            assertTrue(expected.getMessage()
                    .contains("Or create a classpath properties file, environment variable or java property"
                                + " for the name 'testNoEnvVariableOrJavaProperty'"));
        }
    }
}
