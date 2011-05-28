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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.oauth.BaseMockedPartnerConnectionTest;
import com.force.sdk.oauth.mock.MockTokenRetrievalService;
import com.force.sdk.oauth.userdata.UserDataRetrievalService;


/**
 * Unit tests for the OAuth connector.
 *
 * @author Tim Kral
 */
public class BasicForceOAuthConnectorTest extends BaseMockedPartnerConnectionTest {

    private static final String REFRESH_TOKEN = "refresh_token";
    
    @Test
    public void testConnectorClose() throws Exception {
        ForceOAuthConnector connector = createConnector();
        connector.close();
        try {
            connector.refreshAccessToken(REFRESH_TOKEN);
            fail("ForceOAuthConnector.refreshAccessToken should have failed because the connector is closed.");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("No state was found to construct an oauth connection."));
        }
    }
    
    @Test
    public void testConnectorWithConnectionInfo() throws Exception {
        ForceOAuthConnector connector = createConnector();
        assertSecurityContextsAreEqual(partnerSc, connector.refreshAccessToken(REFRESH_TOKEN),
                "Security context should match that which the partner api returned");
    }
    
    @Test
    public void testConnectorWithConnectionValues() throws Exception {
        ForceOAuthConnectionInfo connInfo = new ForceOAuthConnectionInfo();
        connInfo.setEndpoint(endpoint);
        connInfo.setOauthKey(oauthKey);
        connInfo.setOauthSecret(oauthSecret);
        
        ForceOAuthConnector connector = new ForceOAuthConnector(new UserDataRetrievalService(true));
        connector.setConnectionInfo(connInfo);

        //add a bad connection in. The values above should take precedence.
        System.setProperty("force.testExternalConnInfoBeforeConnNameLookup.url",
            "force://url;oauth_key=ABCDEF;oauth_secret=123456");
        connector.setConnectionName("testExternalConnInfoBeforeConnNameLookup");
        
        connector.setTokenRetrievalService(new MockTokenRetrievalService(
                partnerSc.getEndPoint(), partnerSc.getSessionId(),
                VALID_SFDC_INSTANCEURL, partnerSc.getRefreshToken(), oauthKey, oauthSecret));
        assertSecurityContextsAreEqual(partnerSc, connector.refreshAccessToken(REFRESH_TOKEN),
        "Security context should match that which the partner api returned");
    }
    
    @Test
    public void testConnectorWithEnvVariable() throws Exception {
        ForceOAuthConnector connector = new ForceOAuthConnector(new UserDataRetrievalService(true));
        connector.setConnectionName("CONNURLENVVAR"); // FORCE_CONNURLENVVAR_URL is set in pom file
        connector.setTokenRetrievalService(
                new MockTokenRetrievalService(
                        partnerSc.getEndPoint(), partnerSc.getSessionId(),
                        VALID_SFDC_INSTANCEURL, partnerSc.getRefreshToken(), oauthKey, oauthSecret));
        assertSecurityContextsAreEqual(partnerSc, connector.refreshAccessToken(REFRESH_TOKEN),
            "Security context should match that which the partner api returned");    }
    
    @Test
    public void testConnectorWithJavaProperty() throws Exception {
        ForceOAuthConnector connector = new ForceOAuthConnector(new UserDataRetrievalService(true));
        try {
            System.setProperty("force.connUrlJavaProperty.url", createConnectionUrl());
            connector.setConnectionName("connUrlJavaProperty");
            connector.setTokenRetrievalService(
                    new MockTokenRetrievalService(
                            partnerSc.getEndPoint(), partnerSc.getSessionId(),
                            VALID_SFDC_INSTANCEURL, partnerSc.getRefreshToken(), oauthKey, oauthSecret));
            assertSecurityContextsAreEqual(partnerSc, connector.refreshAccessToken(REFRESH_TOKEN),
                "Security context should match that which the partner api returned");
        } finally {
            System.clearProperty("force.connUrlJavaProperty.url");
        }
    }
    
    @Test
    public void testConnectorWithOAuthInfoPropertyFile() throws Exception {
        ForceOAuthConnector connector = new ForceOAuthConnector(new UserDataRetrievalService(true));
        connector.setConnectionName("funcconnoauthinfo"); // funcconnoauthinfo.properties defined in /src/test/resources
        connector.setTokenRetrievalService(
                new MockTokenRetrievalService(
                        partnerSc.getEndPoint(), partnerSc.getSessionId(),
                        VALID_SFDC_INSTANCEURL, partnerSc.getRefreshToken(), oauthKey, oauthSecret));
        assertSecurityContextsAreEqual(partnerSc, connector.refreshAccessToken(REFRESH_TOKEN),
            "Security context should match that which the partner api returned");    }
    
    @Test
    public void testConnectorWithConnUrlPropertyFile() throws Exception {
        ForceOAuthConnector connector = new ForceOAuthConnector(new UserDataRetrievalService(true));
        connector.setConnectionName("funcconnurl"); // funcconnoauthinfo.properties defined in /src/test/resources
        connector.setTokenRetrievalService(
                new MockTokenRetrievalService(
                        partnerSc.getEndPoint(), partnerSc.getSessionId(),
                        VALID_SFDC_INSTANCEURL, partnerSc.getRefreshToken(), oauthKey, oauthSecret));
        assertSecurityContextsAreEqual(partnerSc, connector.refreshAccessToken(REFRESH_TOKEN),
            "Security context should match that which the partner api returned");    }
    
    @DataProvider
    protected Object[][] forceLogoutUrlParamProvider() {
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", "host");
        request.setContextPath("/contextpath");
        
        return new Object[][] {
                {request, "forceEndpoint/", null, "forceEndpoint/secur/logout.jsp"},
                {request, "forceEndpoint/forcecontextpath", null, "forceEndpoint/secur/logout.jsp"},
                {request, "forceEndpoint/", "/localLogoutSuccessfulPath",
                    "forceEndpoint/secur/logout.jsp?"
                        + "retUrl=https://host/contextpath%2FlocalLogoutSuccessfulPath&client_id=" + oauthKey},
                {request, "forceEndpoint/forcecontextpath", "/localLogoutSuccessfulPath",
                    "forceEndpoint/secur/logout.jsp?"
                        + "retUrl=https://host/contextpath%2FlocalLogoutSuccessfulPath&client_id=" + oauthKey},
        };
    }
    
    @Test(dataProvider = "forceLogoutUrlParamProvider")
    public void testGetForceLogoutUrl(HttpServletRequest request, String forceEndpoint, String localLogoutSuccessfulPath,
            String expectedForceLogoutUrl) {
        ForceOAuthConnector connector = createConnector();

        assertEquals(connector.getForceLogoutUrl(request, forceEndpoint, localLogoutSuccessfulPath), expectedForceLogoutUrl,
                "Unexpected force logout url for endpoint (" + forceEndpoint + ") "
                    + "and logout successful path (" + localLogoutSuccessfulPath + ")");
    }
    
    @Test
    public void testGetLoginRedirectUrlEndpoint() throws Exception {
        ForceOAuthConnector connector = createConnector();
        
        String loginRedirectUrl = connector.getLoginRedirectUrl(new MockHttpServletRequest());
        assertTrue(loginRedirectUrl.startsWith(endpoint + "/services/oauth2/authorize"),
                "Expected loginRedirectUrl (" + loginRedirectUrl
                + ") to start with " + endpoint + "/services/oauth2/authorize");
    }
    
    @DataProvider
    protected Object[][] loginRedirectUrlParamProvider() {
        
        MockHttpServletRequest basicRequest = new MockHttpServletRequest();
        basicRequest.addHeader("Host", "host");
        basicRequest.setContextPath("/contextpath");
        basicRequest.setServletPath("/servletpath");
        
        MockHttpServletRequest requestWithPathInfo = new MockHttpServletRequest();
        requestWithPathInfo.addHeader("Host", "host");
        requestWithPathInfo.setContextPath("/contextpath");
        requestWithPathInfo.setServletPath("/servletpath");
        requestWithPathInfo.setPathInfo("/pathinfo");

        MockHttpServletRequest requestWithRedirectAttr = new MockHttpServletRequest();
        requestWithRedirectAttr.addHeader("Host", "host");
        requestWithRedirectAttr.setContextPath("/contextpath");
        requestWithRedirectAttr.setServletPath("/servletpath");
        requestWithRedirectAttr.setAttribute(ForceOAuthConnector.LOGIN_REDIRECT_URL_ATTRIBUTE,
                "loginRedirectUrlAttr");
        
        return new Object[][] {
                {basicRequest, "https%3A%2F%2Fhost%2Fcontextpath%2F_auth",
                    "https%3A%2F%2Fhost%2Fcontextpath%2Fservletpath"},
                {requestWithPathInfo, "https%3A%2F%2Fhost%2Fcontextpath%2F_auth",
                    "https%3A%2F%2Fhost%2Fcontextpath%2Fservletpath%2Fpathinfo"},
                {requestWithRedirectAttr, "https%3A%2F%2Fhost%2Fcontextpath%2F_auth",
                    "loginRedirectUrlAttr"},
        };
    }
    
    @Test(dataProvider = "loginRedirectUrlParamProvider")
    public void testGetLoginRedirectUrlParams(HttpServletRequest request, String expectedRedirectUriParam,
            String expectedStateParam) throws Exception {
        
        ForceOAuthConnector connector = createConnector();
        
        String loginRedirectUrl = connector.getLoginRedirectUrl(request);
        String[] parsedLoginRedirectUrl = loginRedirectUrl.split("\\?", 2);
        assertEquals(parsedLoginRedirectUrl.length, 2,
                "Could not find any login redirect url params on " + loginRedirectUrl);
        
        String loginRedirectUrlParams = parsedLoginRedirectUrl[1];
        assertTrue(loginRedirectUrlParams.contains("response_type=code"),
                "Could not find 'response_type=code' within login redirect url (" + loginRedirectUrl + ")");
        assertTrue(loginRedirectUrlParams.contains("client_id=" + oauthKey),
                "Could not find expected client_id param (" + oauthKey + ") "
                + "within login redirect url (" + loginRedirectUrl + ")");
        assertTrue(loginRedirectUrlParams.contains("redirect_uri=" + expectedRedirectUriParam),
                "Could not find expected redirect_uri param (" + expectedRedirectUriParam + ") "
                + "within login redirect url (" + loginRedirectUrl + ")");
        assertTrue(loginRedirectUrlParams.contains("state=" + expectedStateParam),
                "Could not find expected state param (" + expectedStateParam + ") "
                + "within login redirect url (" + loginRedirectUrl + ")");
    }
    
    private ForceOAuthConnector createConnector() {
        ForceOAuthConnectionInfo connInfo = new ForceOAuthConnectionInfo();
        connInfo.setConnectionUrl(createConnectionUrl());
        
        ForceOAuthConnector connector = new ForceOAuthConnector(new UserDataRetrievalService(true));
        connector.setConnectionInfo(connInfo);
        connector.setTokenRetrievalService(new MockTokenRetrievalService(
                partnerSc.getEndPoint(), partnerSc.getSessionId(),
                VALID_SFDC_INSTANCEURL, partnerSc.getRefreshToken(), oauthKey, oauthSecret));
        return connector;
    }
}
