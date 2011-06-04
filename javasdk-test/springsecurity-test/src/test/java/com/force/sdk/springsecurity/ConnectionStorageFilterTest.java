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

package com.force.sdk.springsecurity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.*;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.oauth.ForceUserPrincipal;
import com.force.sdk.oauth.context.*;
import com.force.sdk.oauth.exception.ForceOAuthSessionExpirationException;
import com.force.sdk.springsecurity.data.SpringSecurityTestData;

/**
 * Tests the connection storage filter.
 * 
 * @author John Simone
 */
public class ConnectionStorageFilterTest {

    private ForceConnectionStorageFilter filter = null;

    private static final String SESSION_ID = "session_id";
    private static final String ENDPOINT = "endpoint";

    @BeforeClass
    public void init() {
        ClassPathResource resource = new ClassPathResource("security-config-authProcessingFilterTest.xml");
        BeanFactory factory = new XmlBeanFactory(resource);
        filter = (ForceConnectionStorageFilter) factory.getBean("connectionStorageFilter");

    }

    private void setupAuthentication() {
        SecurityContext sc = new ForceSecurityContext();
        sc.setSessionId(SESSION_ID);
        sc.setEndPoint(ENDPOINT);
        sc.setRole("ROLE_USER");
        sc.setUserName("username");
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl(sc.getRole()));
        OAuthAuthenticationToken newAuthToken = new OAuthAuthenticationToken(new ForceUserPrincipal(sc.getUserName(),
                sc.getSessionId()), null, authorities);
        newAuthToken.setDetails(sc.getForceSecurityContext());

        org.springframework.security.core.context.SecurityContext springSecurityContext = new SecurityContextImpl();
        springSecurityContext.setAuthentication(newAuthToken);
        SecurityContextHolder.setContext(springSecurityContext);
    }

    @Test
    public void testConnectionStorageFilter() throws ServletException, IOException {
        setupAuthentication();

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        VerificationFilterChain filterChain = new VerificationFilterChain();

        filter.doFilter(request, response, filterChain);
        
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication(), 
                "Authentication should be null beause it should be cleared after the request");
    }

    @Test
    public void testInvalidSessionRedirect() throws Exception {
        setupAuthentication();
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new ExceptionThrowingFilterChain();
        filter.doFilter(request, response, filterChain);
        Assert.assertNotNull(response.getRedirectedUrl(),
                "Expecting the user to be redirected when they recieve a connection exception");
        Assert.assertEquals(response.getRedirectedUrl(), SpringSecurityTestData.REDIRECT_URL,
                "Incorrect redirect URL. Expecting the user to be redirected to the login page.");
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication(), 
        "Authentication should be null beause it should be cleared after the request");
    }
    
    /**
     * Mock filter chain which verifies session Id and endpoint after the connection storage filter.
     * 
     * @author John Simone
     */
    private static class VerificationFilterChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            ForceConnectorConfig connConfig = ForceServiceConnector.getThreadLocalConnectorConfig();

            Assert.assertEquals(
                    SESSION_ID,
                    connConfig.getSessionId(),
                    "Session id stored in ForceConnectorConfig thread local should equal that "
                    + "which was in session prior to calling the ForceConnectionStorageFilter");
            Assert.assertEquals(
                    ENDPOINT,
                    connConfig.getServiceEndpoint(),
                    "Service endpoing stored in ForceConnectorConfig thread local should equal that "
                    + "which was in session prior to calling the ForceConnectionStorageFilter");
            Assert.assertTrue(connConfig.getSessionRenewer() instanceof ForceConnectionStorageFilter,
                    "Session renewer should be a ForceConnectionStorageFilter.");

            SecurityContext sc = ForceSecurityContextHolder.get();

            Assert.assertEquals(
                    SESSION_ID,
                    sc.getSessionId(),
                    "Session id stored in ForceSecurityContextHolder thread local should equal that "
                    + "which was in session prior to calling the ForceConnectionStorageFilter");
            Assert.assertEquals(
                    ENDPOINT,
                    sc.getEndPoint(),
                    "Endpoing stored in ForceSecurityContextHolder thread local should equal that "
                    + "which was in session prior to calling the ForceConnectionStorageFilter");
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
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            // throw an exception resembling what a client servlet should throw if an api connection fails
            throw new ForceOAuthSessionExpirationException();
        }
    }

}
