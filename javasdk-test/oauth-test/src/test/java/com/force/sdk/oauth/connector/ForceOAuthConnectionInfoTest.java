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

package com.force.sdk.oauth.connector;

import static org.testng.Assert.*;

import java.util.Properties;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.qa.util.PropsUtil;

/**
 * Unit Tests for ForceOAuthConnectionInfo.
 *
 * @author Tim Kral
 */
public class ForceOAuthConnectionInfoTest {
       
    @Test
    public void testAppendOauthKeyParam() throws Exception {
        StringBuffer url = new StringBuffer("url?params");
        
        ForceOAuthConnectionInfo connInfo = new ForceOAuthConnectionInfo();
        connInfo.setOauthKey("ABCDEF");
        
        connInfo.appendOauthKeyParam(url);
        assertTrue(url.toString().endsWith("&client_id=ABCDEF"));
    }

    @Test
    public void testAppendOauthSecretParam() throws Exception {
        StringBuffer url = new StringBuffer("url?params");
        
        ForceOAuthConnectionInfo connInfo = new ForceOAuthConnectionInfo();
        connInfo.setOauthSecret("123456");
        
        connInfo.appendOauthSecretParam(url);
        assertTrue(url.toString().endsWith("&client_secret=123456"));
    }
    
    @DataProvider
    protected Object[][] propertyFileConnNameProvider() {
        // Test property files defined in /src/test/resources 
        return new Object[][] {
            {"unitconnurl"},
            {"unitconnoauthinfo"},
        };
    }
    
    @Test(dataProvider = "propertyFileConnNameProvider")
    public void testLoadFromClasspathPropertyFile(String connectionName) throws Exception {
        ForceOAuthConnectionInfo connInfo = ForceOAuthConnectionInfo.loadFromName(connectionName);
        assertEquals(connInfo.getEndpoint(), "url");
        assertEquals(connInfo.getOauthKey(), "ABCDEF");
        assertEquals(connInfo.getOauthSecret(), "123456");
    }
    
    @DataProvider
    protected Object[][] envVariableConnNameProvider() {
        // FORCE_CONNURLENVVAR_URL is set in pom file        
        return new Object[][] {
            {"CONNURLENVVAR"},
            {"cOnNuRlEnVvAr"},
            {"connurlenvvar"},
        };
    }
    
    // NOTE: This is not going to pass in STS.  You have to execute from the command line.
    @Test(dataProvider = "envVariableConnNameProvider")
    public void testLoadFromEnvVariable(String connectionName) throws Exception {
        Properties props = PropsUtil.load("funcconnoauthinfo.properties");
        
        ForceOAuthConnectionInfo connInfo = ForceOAuthConnectionInfo.loadFromName(connectionName);
        assertNotNull(connInfo);
        
        // ForceOAuthConnectionInfo won't have a protocol from a connection url
        // So strip off any protocol from the endpoint property so we can compare them
        String[] parsedEndpoint = ((String) props.get("endpoint")).split("://");
        String expectedEndpoint = parsedEndpoint[parsedEndpoint.length - 1];
        
        assertEquals(connInfo.getEndpoint(), expectedEndpoint);
        assertEquals(connInfo.getOauthKey(), props.get("oauth_key"));
        assertEquals(connInfo.getOauthSecret(), props.get("oauth_secret"));
    }
    
    @DataProvider
    protected Object[][] javaPropertyProvider() {
        return new Object[][] {
            {"force.xyz.url", "xyz"},
            {"force.xYz.url", "xYz"},
            {"force.XYZ.url", "XYZ"},
            {"force.xyz1.url", "xyz1"},
            {"force.xyz1.url", "xyz1"},
            {"force.xyz-1.url", "xyz-1"},
            {"force.xyz_1.url", "xyz_1"},
        };
    }
    
    @Test(dataProvider = "javaPropertyProvider")
    public void testLoadFromJavaProperty(String propName, String connectionName) throws Exception {
        try {
            System.setProperty(propName, "force://url;oauth_key=ABCDEF;oauth_secret=123456");
            ForceOAuthConnectionInfo connInfo = ForceOAuthConnectionInfo.loadFromName(connectionName);
            assertNotNull(connInfo);
            
            assertEquals(connInfo.getEndpoint(), "url");
            assertEquals(connInfo.getOauthKey(), "ABCDEF");
            assertEquals(connInfo.getOauthSecret(), "123456");
        } finally {
            System.clearProperty(propName);
        }
    }
    
    @Test
    public void testLoadFromJavaPropertyIsCaseSensitive() throws Exception {
        try {
            System.setProperty("force.xyz.url", "force://url;oauth_key=ABCDEF;oauth_secret=123456");
            assertNull(ForceOAuthConnectionInfo.loadFromName("XYZ"));
        } finally {
            System.clearProperty("force.xyz.url");
        }
    }
    
    // NOTE: This is not going to pass in STS.  You have to execute from the command line.
    @Test
    public void testLoadEnvVariableBeforeJavaProperty() throws Exception {
        try {
            // Set a Java property that conflicts with the environment variable set in the pom
            System.setProperty("force.connurlenvvar.url", "force://javapropurl;oauth_key=javapropkey;oauth_secret=7891011");
            
            // Try loading from the environment variable.
            // The assertions in that test should still work.
            testLoadFromEnvVariable("connurlenvvar");
        } finally {
            System.clearProperty("force.envvarconn.url");
        }
    }
    
    @Test
    public void testLoadJavaPropertyBeforePropertyFile() throws Exception {
        try {
            // Set a Java property that conficts with the unitconnurl properties file
            System.setProperty("force.unitconnurl.url", "force://javapropurl;oauth_key=javapropkey;oauth_secret=7891011");
            
            ForceOAuthConnectionInfo connInfo = ForceOAuthConnectionInfo.loadFromName("unitconnurl");
            assertNotNull(connInfo);
            
            // The loaded connection info should have used the connection url
            // from the Java property
            assertEquals(connInfo.getEndpoint(), "javapropurl");
            assertEquals(connInfo.getOauthKey(), "javapropkey");
            assertEquals(connInfo.getOauthSecret(), "7891011");
        } finally {
            System.clearProperty("force.unitconnurl.url");
        }
    }

    @Test
    public void testLoadWithNothingSet() throws Exception {
        assertNull(ForceOAuthConnectionInfo.loadFromName("xyz"));
    }
    
    @DataProvider
    protected Object[][] badConnectionUrlProvider() {
        return new Object[][] {
            {null},
            {""},
            {"url"},
            {"url;oauth_key=ABCDEF;oauth_secret=123456"},
            {"force://"},
            {"force://url"},
            {"force://url;oauth_key=ABCDEF"},
        };
    }
    
    @Test(dataProvider = "badConnectionUrlProvider")
    public void testParseBadConnectionUrl(String connectionUrl) throws Exception {
        ForceOAuthConnectionInfo connInfo = new ForceOAuthConnectionInfo();
        try {
            // Parse the connectionUrl in the setter
            connInfo.setConnectionUrl(connectionUrl);
            fail("ForceOAuthConnectionInfo.setConnectionUrl should have failed with bad connection url.");
        } catch (IllegalArgumentException expected) {
            if (connectionUrl != null) {
                assertTrue(expected.getMessage().contains(connectionUrl));
            } else {
                assertTrue(expected.getMessage().contains("null"));
            }
        }
    }

    @DataProvider
    protected Object[][] connectionUrlWithBadPropertyProvider() {
        return new Object[][] {
            {"force://;oauth_key=ABCDEF;oauth_secret=123456", "endpoint", null},
            {"force://url;oauth_key=;oauth_secret=123456", "oauth_key", null},
            {"force://url;oauth_key=ABCDEF;oauth_secret=", "oauth_secret", null},
            {"force://url/a;oauth_key=ABCDEF;oauth_secret=", "endpoint", "url/a"},
            {"force://url;oauth_key=ABCDEF;oauth_secret=abc", "oauth_secret", "abc"},
        };
    }
    
    @Test(dataProvider = "connectionUrlWithBadPropertyProvider")
    public void testParseConnectionUrlWithBadProperty(String connectionUrl, String badProperty, String badValue)
    throws Exception {
        ForceOAuthConnectionInfo connInfo = new ForceOAuthConnectionInfo();
        try {
            // Parse the connectionUrl in the setter
            connInfo.setConnectionUrl(connectionUrl);
            fail("ForceOAuthConnectionInfo.setConnectionUrl should have failed with missing property (" + badProperty + ")");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains(badProperty));
            if (badValue != null) {
                assertTrue(expected.getMessage().contains(badValue));
            }
        }
    }
    
    @DataProvider
    protected Object[][] goodConnectionUrlProvider() {
        return new Object[][] {
            {"force://url;oauth_key=ABCDEF;oauth_secret=123456", "url", "ABCDEF", "123456"},
            {"force://url;oauth_secret=123456;oauth_key=ABCDEF", "url", "ABCDEF", "123456"},
            {"force://url/;oauth_key=ABCDEF;oauth_secret=123456", "url/", "ABCDEF", "123456"},
            {"force://url/services/Soap/u/0;oauth_key=ABCDEF;oauth_secret=123456", "url/services/Soap/u/0", "ABCDEF", "123456"},
            {"force://url;oauth_key=ABCDEF;oauth_secret=123456;testProp", "url", "ABCDEF", "123456"}, // Ignore unknown props
            {"force://url;oauth_key=ABCDEF;oauth_secret=123456;testProp=", "url", "ABCDEF", "123456"}, // Ignore unknown props
            {"force://url;oauth_key=ABCDEF;oauth_secret=123456;", "url", "ABCDEF", "123456"}, // Trailing ';'
            {"force://url;oauth_secret=123456;oauth_key=ABCDEF=", "url", "ABCDEF=", "123456"}, // Trailing '='
            {" force://url;oauth_key=ABCDEF;oauth_secret=123456", "url", "ABCDEF", "123456"}, // Leading whitespace
            {"force://url; oauth_key=ABCDEF;oauth_secret=123456", "url", "ABCDEF", "123456"}, // Space between url and user
            {"force://url;oauth_key=ABCDEF; oauth_secret=123456", "url", "ABCDEF", "123456"}, // Space between user and password
            {"force://url;oauth_key=ABCDEF;oauth_secret=123456 ", "url", "ABCDEF", "123456"}, // Trailing whitespace
        };
    }
    
    @Test(dataProvider = "goodConnectionUrlProvider")
    public void testParseGoodConnectionUrl(String connectionUrl, String endpoint, String oauthKey, String oauthSecret)
    throws Exception {
        
        // Parse the connectionUrl in the setter
        ForceOAuthConnectionInfo connInfo = new ForceOAuthConnectionInfo();
        connInfo.setConnectionUrl(connectionUrl);
        
        assertEquals(connInfo.getEndpoint(), endpoint);
        assertEquals(connInfo.getOauthKey(), oauthKey);
        assertEquals(connInfo.getOauthSecret(), oauthSecret);
    }
    
    
    @DataProvider
    protected Object[][] badOauthInfoProvider() {
        return new Object[][] {
                {null, "ABCDEF", "123456", "endpoint", null},
                {"", "ABCDEF", "123456", "endpoint", null},
                {"htp://url", "ABCDEF", "123456", "endpoint", "htp://url"},
                {"https://url/a", "ABCDEF", "123456", "endpoint", "https://url/a"},
                {"https://url/services/Soap/u/", "ABCDEF", "123456", "endpoint", "https://url/services/Soap/u/"},
                {"url", null, "123456", "oauth_key", null},
                {"url", "", "123456", "oauth_key", null},
                {"url", "ABCDEF", null, "oauth_secret", null},
                {"url", "ABCDEF", "", "oauth_secret", null},
                {"url", "ABCDEF", "abc", "oauth_secret", "abc"},
        };
    }

    @Test(dataProvider = "badOauthInfoProvider")
    public void testValidateBadOauthInfo(String endpoint, String oauthKey, String oauthSecret,
            String badProperty, String badValue) throws Exception {
        
        ForceOAuthConnectionInfo connInfo = new ForceOAuthConnectionInfo();
        connInfo.setEndpoint(endpoint);
        connInfo.setOauthKey(oauthKey);
        connInfo.setOauthSecret(oauthSecret);
        
        try {
            connInfo.validate();
            fail("ForceOAuthConnectionInfo.validate should have failed due to incomplete state.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains(badProperty));
            if (badValue != null) {
                assertTrue(expected.getMessage().contains(badValue));
            }
        }
    }
    
    @DataProvider
    protected Object[][] goodOauthInfoProvider() {
        return new Object[][] {
                {"url", "ABCDEF", "123456"},
                {"url:0", "ABCDEF", "123456"},
                {"url/", "ABCDEF", "123456"},
                {"url:0/", "ABCDEF", "123456"},
                {"url/services/Soap/u/0", "ABCDEF", "123456"},
                {"url:0/services/Soap/u/0", "ABCDEF", "123456"},
                {"http://url", "ABCDEF", "123456"},
                {"https://url", "ABCDEF", "123456"},
                {"url", "ABCDEF", "123456"},
                {"url", "ABCDEF", "123456"},
        };
    }
    
    @Test(dataProvider = "goodOauthInfoProvider")
    public void testValidateGoodOauthInfo(String endpoint, String oauthKey, String oauthSecret) throws Exception {
        
        ForceOAuthConnectionInfo connInfo = new ForceOAuthConnectionInfo();
        connInfo.setEndpoint(endpoint);
        connInfo.setOauthKey(oauthKey);
        connInfo.setOauthSecret(oauthSecret);
        
        connInfo.validate();
    }
}

