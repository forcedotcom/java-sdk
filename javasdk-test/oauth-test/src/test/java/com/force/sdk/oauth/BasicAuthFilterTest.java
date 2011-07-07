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

import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.oauth.context.ForceSecurityContextHolder;
import com.force.sdk.oauth.context.SecurityContext;
import com.force.sdk.oauth.context.SecurityContextUtil;
import com.sforce.soap.partner.Connector;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

import static org.testng.Assert.*;

/**
 * Basic functional tests for the OAuth handshake with Force.com.
 *
 * @author Tim Kral
 * @author Nawab Iqbal
 */
public class BasicAuthFilterTest extends BaseOAuthTest {

    @Test
    public void testNoInfiniteLoop() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AuthFilter.FILTER_ALREADY_VISITED, Boolean.TRUE);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        AuthFilter filter = new AuthFilter();
        filter.doFilter(request, response, filterChain);
        
        // Assert that we haven't redirected or forwarded
        assertNull(response.getRedirectedUrl(), "AuthFilter should not redirect if it has been already visited.");
        assertNull(response.getForwardedUrl(), "AuthFilter should not forward if it has been already visited.");
        
        // Assert that we haven't wrapped the request
        assertTrue(filterChain.getRequest() instanceof MockHttpServletRequest,
                "AuthFilter should not wrap request if it has been already visited.");
    }
    
    @Test
    public void testOAuthLoginRedirectWithOAuthInfo() throws Exception {
        // Initialize the filter with oauth info
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("endpoint", endpoint);
        filterConfig.addInitParameter("oauthKey", oauthKey);
        filterConfig.addInitParameter("oauthSecret", oauthSecret);
    
        testOAuthLoginRedirectInternal(filterConfig);
    }

    @Test
    public void testOAuthLoginRedirectWithConnUrl() throws Exception {
        // Initialize the filter with a connection url
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("url", createConnectionUrl());

        testOAuthLoginRedirectInternal(filterConfig);
    }
    
    @Test
    public void testOAuthLoginRedirectWithEnvVariable() throws Exception {
        // Initialize the filter with an environment variable name
        MockFilterConfig filterConfig = new MockFilterConfig();
        filterConfig.addInitParameter("connectionName", "CONNURLENVVAR"); // FORCE_CONNURLENVVAR_URL is set in pom file
        
        testOAuthLoginRedirectInternal(filterConfig);
    }
    
    @Test
    public void testOAuthLoginRedirectWithJavaProperty() throws Exception {
        try {
            System.setProperty("force.filterWithConnUrlJavaProperty.url", createConnectionUrl());
            
            // Initialize the filter with a Java property name
            MockFilterConfig filterConfig = new MockFilterConfig();
            filterConfig.addInitParameter("connectionName", "filterWithConnUrlJavaProperty");

            testOAuthLoginRedirectInternal(filterConfig);
        } finally {
            System.clearProperty("force.filterWithConnUrlJavaProperty.url");
        }
    }

    /**
     * Enum for testing different request url options.
     */
    private enum RequestOption {
        NONE,
        PATH_INFO,
        QUERY_PARAM,
        PATH_INFO_AND_QUERY_PARAM,
    }

    @DataProvider
    protected Object[][] requestOptions() {
        return new Object[][] {
                {RequestOption.NONE},
                {RequestOption.PATH_INFO},
                {RequestOption.QUERY_PARAM},
                {RequestOption.PATH_INFO_AND_QUERY_PARAM}
        };
    }

    @Test(dataProvider = "requestOptions")
    public void testOAuthLoginRedirectWithPropertyFile(RequestOption option) throws Exception {
        // Initialize the filter with a Java property name
        MockFilterConfig filterConfig = new MockFilterConfig();
        // funcconnoauthinfo.properties defined in /src/test/resources
        filterConfig.addInitParameter("connectionName", "funcconnoauthinfo");

        testOAuthLoginRedirectInternal(filterConfig, option);
    }
    
    @Test
    public void testOAuthInfoIsFirst() throws Exception {
        MockFilterConfig filterConfig = new MockFilterConfig();
        
        // Add good OAuth info
        filterConfig.addInitParameter("endpoint", endpoint);
        filterConfig.addInitParameter("oauthKey", oauthKey);
        filterConfig.addInitParameter("oauthSecret", oauthSecret);
        filterConfig.addInitParameter("storeUsername", "false");
        
        // Add a bad connection url
        filterConfig.addInitParameter("url", "force://url;oauth_key=ABCDEF;oauth_secret=123456");
        
        // Add a bad connection name
        filterConfig.addInitParameter("connectionName", "badConnectionName");
        
        // This test should still pass because we use OAuth info first
        testOAuthLoginRedirectInternal(filterConfig);
    }
    
    @Test
    public void testConnectionUrlIsSecond() throws Exception {
        MockFilterConfig filterConfig = new MockFilterConfig();
        
        // Add a good connection url
        filterConfig.addInitParameter("url", createConnectionUrl());
        
        // Add a bad connection name
        filterConfig.addInitParameter("connectionName", "badConnectionName");
        
        // This test should still pass because we use 
        // a connection url before a connection name
        testOAuthLoginRedirectInternal(filterConfig);
    }

    private void testOAuthLoginRedirectInternal(FilterConfig filterConfig) throws Exception {
        testOAuthLoginRedirectInternal(filterConfig, RequestOption.NONE);
    }

    private void testOAuthLoginRedirectInternal(FilterConfig filterConfig, RequestOption option) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", "host");
        request.setContextPath("/contextpath");
        request.setServletPath("/servletpath");

        String state = "https%3A%2F%2Fhost%2Fcontextpath%2Fservletpath";

        switch (option) {
            case PATH_INFO:
                request.setPathInfo("/pathInfo");
                state += "%2FpathInfo";
                break;
            case QUERY_PARAM:
                request.setQueryString("var1=val1&var2=val2");
                state += "%3Fvar1%3Dval1%26var2%3Dval2";
                break;
            case PATH_INFO_AND_QUERY_PARAM:
                request.setPathInfo("/pathInfo");
                request.setQueryString("var1=val1&var2=val2");
                state += "%2FpathInfo%3Fvar1%3Dval1%26var2%3Dval2";
                break;
            default:
                break;
        }

        MockHttpServletResponse response = processRequest(filterConfig, request);

        // Filter should redirect us to OAuth login page
        assertEquals(response.getRedirectedUrl(),
                endpoint + "/services/oauth2/authorize?response_type=code&"
                         + "redirect_uri=https%3A%2F%2Fhost%2Fcontextpath%2F_auth&"
                         + "state=" + state + "&"
                         + "client_id=" + oauthKey,
                "Expected OAuth login redirect.");
    }

    private MockHttpServletResponse processRequest(FilterConfig filterConfig, MockHttpServletRequest request)
            throws ServletException, IOException {
        AuthFilter filter = new AuthFilter();
        filter.init(filterConfig);

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        filter.doFilter(request, response, filterChain);
        return response;
    }

    @Test
    public void testLoginWithSessionIdAndEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ForceServiceConnector connector = new ForceServiceConnector("userInfo");
        
        // Add session id and endpoint cookies to the request
        Cookie sidCookie =
            new Cookie(SecurityContextUtil.FORCE_FORCE_SESSION, connector.getConnection().getSessionHeader().getSessionId());
        Cookie endpointCookie =
            new Cookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT, connector.getConnection().getConfig().getServiceEndpoint());
        request.setCookies(sidCookie, endpointCookie);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        try {
            
            AuthFilter filter = new AuthFilter();
            MockFilterConfig filterConfig = new MockFilterConfig();
            
            // Add good OAuth info
            filterConfig.addInitParameter("endpoint", endpoint);
            filterConfig.addInitParameter("oauthKey", oauthKey);
            filterConfig.addInitParameter("oauthSecret", oauthSecret);
            filter.init(filterConfig);
            
            // Assert application security state within the LoginWithSessionIdAndEndpointFilterChain
            filter.doFilter(request, response, new LoginWithSessionIdAndEndpointFilterChain());
        } finally {
            System.clearProperty("java.security.auth.login.config");
        }
    }
    
    @Test
    public void testLoginWithSessionIdAndEndpointNoUsername() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ForceServiceConnector connector = new ForceServiceConnector("userInfo");
        
        // Add session id and endpoint cookies to the request
        Cookie sidCookie =
            new Cookie(SecurityContextUtil.FORCE_FORCE_SESSION, connector.getConnection().getSessionHeader().getSessionId());
        Cookie endpointCookie =
            new Cookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT, connector.getConnection().getConfig().getServiceEndpoint());
        request.setCookies(sidCookie, endpointCookie);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        try {
            
            AuthFilter filter = new AuthFilter();
            MockFilterConfig filterConfig = new MockFilterConfig();
            
            // Add good OAuth info
            filterConfig.addInitParameter("endpoint", endpoint);
            filterConfig.addInitParameter("oauthKey", oauthKey);
            filterConfig.addInitParameter("oauthSecret", oauthSecret);
            filterConfig.addInitParameter("storeUsername", "false");

            filter.init(filterConfig);
            request.setAttribute("nullUsername", Boolean.valueOf(true));
            
            // Assert application security state within the LoginWithSessionIdAndEndpointFilterChain
            filter.doFilter(request, response, new LoginWithSessionIdAndEndpointFilterChain());
        } finally {
            System.clearProperty("java.security.auth.login.config");
        }
    }
    
    /**
     * Mock filter chain for verifying login with session id and endpoint. 
     * 
     * @author Tim Kral
     */
    private class LoginWithSessionIdAndEndpointFilterChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest req, ServletResponse res) throws IOException {
            assertTrue(req instanceof HttpServletRequestWrapper, "Request should have been wrapped as HttpServletRequestWrapper");
            HttpServletRequestWrapper request = (HttpServletRequestWrapper) req;
            
            assertNotNull(request.getAttribute(AuthFilter.FILTER_ALREADY_VISITED), "AuthFilter should have been marked visited");
            
            if (request.getAttribute("nullUsername") != null && (Boolean) request.getAttribute("nullUsername")) {
                assertNull(request.getRemoteUser(), "Username should be null when configured to not store username");
            } else {
                assertTrue(request.getRemoteUser().startsWith(userInfo.getUserName()),
                        "Unexpected remote user. Expected remote user (" + request.getRemoteUser() + ") "
                            + "to start with " + endpoint);
            }
            assertTrue(request.isUserInRole("System Administrator"), "User should be in the default role: ROLE_USER");
            
            SecurityContext sc = ForceSecurityContextHolder.get();
            assertNotNull(sc, "SecurityContext thread local should have been set");
            assertNotNull(sc.getSessionId(), "SecurityContext session id should have been set");
            assertNotNull(sc.getEndPoint(), "SecurityContext end point should have been set");
            assertNotNull(sc.getEndPointHost(), "SecurityContext end point host should have been set");
            
            if (request.getAttribute("nullUsername") != null && (Boolean) request.getAttribute("nullUsername")) {
                assertNull(sc.getUserName(), "Username should be null when configured to not store username");
            }
            
            ConnectorConfig config = new ConnectorConfig();
            config.setSessionId(sc.getSessionId());
            config.setServiceEndpoint(sc.getEndPoint());
            
            try {
                sc.init(Connector.newConnection(config).getUserInfo());
            } catch (ConnectionException e) {
                throw new IOException(e);
            }
            
            verifySecurityContext(sc, true /* checkApiEndpoint*/, false /* checkRefreshToken*/);
        }
    }
}
