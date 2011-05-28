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

package com.force.sdk.oauth;

import static org.testng.Assert.*;

import java.util.Properties;

import org.testng.annotations.BeforeClass;

import com.force.sdk.oauth.context.SecurityContext;
import com.force.sdk.test.util.PropsUtil;
import com.force.sdk.test.util.UserInfo;

/**
 * Base class for Force.com OAuth functional tests.
 *
 * @author Tim Kral
 */
public class BaseOAuthTest {

    protected String endpoint;
    protected String oauthKey;
    protected String oauthSecret;
    
    protected String oauthCallback;
    protected String refreshToken;
    
    protected UserInfo userInfo;
    
    @BeforeClass
    public void classSetUp() throws Exception {
        // Get the oauth properties from the classpath
        String oauthPropertiesFileName = "funcconnoauthinfo.properties";
        Properties oauthProperties = PropsUtil.load(oauthPropertiesFileName);
        
        endpoint = assertAndLoadProperty(oauthProperties, "endpoint", oauthPropertiesFileName);
        oauthKey = assertAndLoadProperty(oauthProperties, "oauth_key", oauthPropertiesFileName);
        oauthSecret = assertAndLoadProperty(oauthProperties, "oauth_secret", oauthPropertiesFileName);
        
        oauthCallback = assertAndLoadProperty(oauthProperties, "oauth_callback", oauthPropertiesFileName);
        refreshToken = assertAndLoadProperty(oauthProperties, "refresh_token", oauthPropertiesFileName);
        
        userInfo = UserInfo.loadFromPropertyFile("userInfo");
    }
    
    private String assertAndLoadProperty(Properties properties, String propertyName, String propertyFileName) {
        assertTrue(properties.containsKey(propertyName),
                propertyFileName + " is missing property " + propertyName);
        return properties.getProperty(propertyName);
    }
    
    protected String createConnectionUrl() {
        // Strip off the protocol if it exists
        String host = this.endpoint.split("://", 2)[1];
        StringBuffer connectionUrl = new StringBuffer("force://")
                .append(host)
                .append(";oauth_key=").append(oauthKey)
                .append(";oauth_secret=").append(oauthSecret);
        
        return connectionUrl.toString();
    }
    
    protected void verifySecurityContext(SecurityContext sc) {
        verifySecurityContext(sc, false /* checkApiEndpoint */, true /* checkRefreshToken */);
    }
    
    protected void verifySecurityContext(SecurityContext sc, boolean checkApiEndpoint, boolean checkRefreshToken) {
    
        assertEquals(sc.getOrgId(), userInfo.getOrgId(),
                "Unexpected organization id in SecurityContext");
        assertEquals(sc.getUserId(), userInfo.getUserId(),
                "Unexpected user id in SecurityContext");
        assertEquals(sc.getUserName(), userInfo.getUserName(),
                "Unexpected user name in SecurityContext");
        
        String expectedEndpoint = userInfo.getServerEndpoint();
        
        // The userInfo will store the api server endpoint, but
        // sometimes we may expect the SecurityContext to have the instance endpoint
        if (!checkApiEndpoint) {
            expectedEndpoint = expectedEndpoint.replaceAll("api-", "");
        }
        
        
        assertTrue(sc.getEndPoint().startsWith(expectedEndpoint),
                "Unexpected endpoint in SecurityContext. Security context endpoint (" + sc.getEndPoint() + "). "
                + "Expected endpoint (" + expectedEndpoint + ")");
        
        if (checkRefreshToken) {
            // The refresh token should not change
            assertEquals(sc.getRefreshToken(), refreshToken, "Refresh token changed");
        } else {
            assertNull(sc.getRefreshToken(), "Unexpected refresh token.");
        }
    }
}
