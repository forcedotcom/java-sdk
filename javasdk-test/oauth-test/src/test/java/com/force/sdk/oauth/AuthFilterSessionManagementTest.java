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

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.mock.web.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.oauth.context.*;
import com.force.sdk.oauth.context.store.SecurityContextSessionStore;
import com.force.sdk.oauth.exception.ForceOAuthSessionExpirationException;

/**
 * This class tests the session management of the AuthFilter. All tests assume that a user has already authenticated via
 * OAuth so this state is initialized ahead of time. With each test we are simulating whether you have a session with
 * your security context on the server you hit or not. Also whether the data in that security context may be stale.
 * 
 * @author John Simone
 */
public class AuthFilterSessionManagementTest extends BaseMockedPartnerConnectionTest {

    public static final String SECURITY_CONTEXT_TO_VERIFY_KEY = "securityContextToVerify";

    MockHttpServletRequest request = null;
    MockHttpServletResponse response = null;
    MockHttpSession session = null;
    AuthFilter filter = null;

    /**
     * This will be set as the next filter in the chain so that values can be verified
     * Describe your class here.
     *
     * @author John Simone
     */
    private class PostAuthFilterChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest req, ServletResponse res) throws IOException {
            HttpSession reqSession = ((HttpServletRequest) req).getSession();
            SecurityContext sc = (SecurityContext) reqSession
                    .getAttribute(SecurityContextSessionStore.SECURITY_CONTEXT_SESSION_KEY);
            SecurityContext verificationSc = (SecurityContext) reqSession.getAttribute(SECURITY_CONTEXT_TO_VERIFY_KEY);

            Assert.assertNotNull(sc, "The security context in the session should not be null");
            assertSecurityContextsAreEqual(verificationSc, sc, "Security contexts are not equal");
            ForceConnectorConfig connectorConfig = ForceServiceConnector.getThreadLocalConnectorConfig();

            Assert.assertNotNull(connectorConfig,
                    "The ForceConnectorConfig stored in the thread local should not be null when a user has logged in");
            Assert.assertTrue(connectorConfig.getSessionRenewer() instanceof AuthFilter,
                    "An AuthFilter should be the session renewer.");
            Assert.assertEquals(connectorConfig.getSessionId(), verificationSc.getSessionId(),
                    "Session id in the security context should match that in the connector config.");
            Assert.assertEquals(connectorConfig.getServiceEndpoint(), verificationSc.getEndPoint(),
                    "Endpoint in the security context should match that in the connector config.");
        }
    }

    /**
     * This will be set as the next filter in the chain.
     * Throws a ForceOAuthSessionExpirationException to simulate an expired
     * session id in client code.
     * 
     * @author John Simone
     */
    private static class ExceptionThrowingFilterChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest req, ServletResponse res) throws IOException {
            throw new ForceOAuthSessionExpirationException();
        }
    }

    @BeforeMethod
    public void init() throws ServletException {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        session = new MockHttpSession();

        // Add session id and endpoint cookies to the request
        Cookie sidCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_SESSION, VALID_SFDC_SID);
        Cookie endpointCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT, VALID_SFDC_ENDPOINT);
        request.setCookies(sidCookie, endpointCookie);

        filter = new AuthFilter();

        MockFilterConfig filterConfig = new MockFilterConfig();

        // Add good OAuth info
        filterConfig.addInitParameter("endpoint", VALID_SFDC_ENDPOINT);
        filterConfig.addInitParameter("oauthKey", CONSUMER_KEY);
        filterConfig.addInitParameter("oauthSecret", CONSUMER_SECRET);
        filterConfig.addInitParameter("securityContextStorageMethod", "session");
        filter.init(filterConfig);

    }

    /**
     * User is authenticated and they visit an instance that has their java session. Here we set up a request that has
     * the sfdc sid and endpoint cookies. A security context is also put into the session. We test that the context in
     * the session at the end is the same as what we started with. there should be no call to the partner api.
     */
    @Test
    public void testValidSessionData() throws ServletException, IOException {

        session.setAttribute(SecurityContextSessionStore.SECURITY_CONTEXT_SESSION_KEY, originalSc);
        session.setAttribute(SECURITY_CONTEXT_TO_VERIFY_KEY, originalSc);
        request.setSession(session);

        filter.doFilter(request, response, new PostAuthFilterChain());
    }

    /**
     * User appears to be authenticated, but when the API is used a sessioned timed out exception gets thrown. This
     * causes a session renewal to be attempted which should result in an redirect to the OAuth authentication URL.
     */
    @Test
    public void testInvalidSessionException() throws ServletException, IOException {

        session.setAttribute(SecurityContextSessionStore.SECURITY_CONTEXT_SESSION_KEY, originalSc);
        request.setSession(session);

        filter.doFilter(request, response, new ExceptionThrowingFilterChain());

        Assert.assertNotNull(response.getRedirectedUrl(), "Should send redirect url on a session renewal attempt.");
        Assert.assertEquals(
                response.getRedirectedUrl(),
                VALID_SFDC_ENDPOINT
                + "/services/oauth2/authorize?response_type=code&scope=api+refresh_token&redirect_uri=https%3A%2F%2Flocalhost%3A8443%2F_auth&"
                + "state=https%3A%2F%2Flocalhost%3A8443&client_id="
                + CONSUMER_KEY,
                "Should send redirect url on a session renewal attempt. Url is not correct.");
    }

    /**
     * User is authenticated and they visit an instance that has their java session, but the sfdc session id in the
     * Security Context doesn't match the one in their cookie (stale data). Here we set up a request that has the sfdc
     * sid and endpoint cookies. A security context is also put into the session, but it has a different session id in
     * it. This simulates stale data in the session. We test that the context in the session at the end came from the
     * partner api because we expect a call to refresh the data.
     */
    @Test
    public void testInvalidSFDCSessionId() throws ServletException, IOException {

        SecurityContext sc = new ForceSecurityContext();

        sc.setEndPoint(originalSc.getEndPoint());
        sc.setSessionId(INVALID_SFDC_SID);
        sc.setOrgId(originalSc.getOrgId());
        sc.setUserId(originalSc.getUserId());
        sc.setUserName(originalSc.getUserName());
        sc.setLanguage(originalSc.getLanguage());

        session.setAttribute(SecurityContextSessionStore.SECURITY_CONTEXT_SESSION_KEY, sc);
        session.setAttribute(SECURITY_CONTEXT_TO_VERIFY_KEY, partnerSc);
        request.setSession(session);

        filter.doFilter(request, response, new PostAuthFilterChain());
    }

    /**
     * User is authenticated and they visit an instance that does not have their Java session. Here we set up a request
     * that has the sfdc sid and endpoint cookies. No security context is put into the session to simulate hitting an
     * instance that you don't have a session with. We test that the context in the session at the end came from the
     * partner api because we expect a call to get the data.
     */
    @Test
    public void testNoDataInSession() throws ServletException, IOException {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute(SECURITY_CONTEXT_TO_VERIFY_KEY, partnerSc);
        request.setSession(mockSession);

        filter.doFilter(request, response, new PostAuthFilterChain());
    }
}
