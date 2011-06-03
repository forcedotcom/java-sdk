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

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.springframework.mock.web.*;
import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.oauth.context.*;
import com.force.sdk.oauth.context.store.*;
import com.sforce.ws.util.Base64;

/**
 * This class tests the session management of the AuthFilter.
 * All tests assume that a user has already authenticated via OAuth
 * so this state is initialized ahead of time. With each test we are
 * simulating whether you have a session with your security context
 * on the server you hit or not. Also whether the data in that security
 * context may be stale.
 * 
 * @author John Simone
 *
 */
public class AuthFilterCookieManagementTest extends BaseMockedPartnerConnectionTest {
    
    public static final String SECURITY_CONTEXT_TO_VERIFY_KEY = "securityContextToVerify";
    
    MockHttpServletRequest request = null;
    MockHttpServletResponse response = null;
    MockHttpSession session = null;
    AuthFilter filter = null;
    SecurityContextService securityContextService = null;
    String keyFile;
    
    @Factory
    public static Object[] createInstances() {
        Object[] testSuites = new Object[2];
        testSuites[0] = new AuthFilterCookieManagementTest(null);
        testSuites[1] = new AuthFilterCookieManagementTest("valid-key-file.properties");
        return testSuites;
    }
    
     public AuthFilterCookieManagementTest(String keyFile) {
        this.keyFile = keyFile;
    }
    
    /**
     * This will be set as the next filter in the chain so that values can be verified.
     * 
     * @author John Simone
     */
    private class PostAuthFilterChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest req, ServletResponse res) throws IOException {
            HttpSession httpSession = ((HttpServletRequest) req).getSession();
            
            MockHttpServletResponse mockResponse = (MockHttpServletResponse) res;
            SecurityContext sc;
            try {
                //retrieve the security context from the cookie. Verify that the cookie is
                //secure only if the host is not "localhost"
                sc = retreiveSecurityContextFromCookie(mockResponse, !"localhost".equals(req.getLocalName()));
            } catch (Exception e) {
                throw new IOException(e);
            }
            
            //figure out which security context we are expecting for this test
            SecurityContext verificationSc = (SecurityContext) httpSession.getAttribute(SECURITY_CONTEXT_TO_VERIFY_KEY);
            
            Assert.assertNotNull(sc, "The security context in the cookie should not be null");
            assertSecurityContextsAreEqual(verificationSc, sc, "Security contexts are not equal");
        }
    }
    
    @BeforeMethod
    public void init() throws ForceEncryptionException, ServletException {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        session = new MockHttpSession();
        
        // Add session id and endpoint cookies to the request
        Cookie sidCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_SESSION, VALID_SFDC_SID);
        Cookie endpointCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT, VALID_SFDC_ENDPOINT);
        request.setCookies(sidCookie, endpointCookie);
        request.setLocalName("someRemoteServer");
        
        filter = new AuthFilter();
        
        MockFilterConfig filterConfig = new MockFilterConfig();
        
        // Add good OAuth info
        filterConfig.addInitParameter("endpoint", VALID_SFDC_ENDPOINT);
        filterConfig.addInitParameter("oauthKey", CONSUMER_KEY);
        filterConfig.addInitParameter("oauthSecret", CONSUMER_SECRET);
        
        // this will have different values for each run of this test suite.
        filterConfig.addInitParameter("secure-key-file", keyFile);
        
        filter.init(filterConfig);
        
        securityContextService  = filter.getSecurityContextService();
    }
    
    /**
     * User is authenticated and they visit an instance that has
     * their java session.
     * 
     * Here we set up a request that has the sfdc sid and endpoint cookies.
     * A security context is also put into the session. We test that the
     * context in the session at the end is the same as what we started with.
     * there should be no call to the partner api.
     * @throws ContextStoreException 
     */
    @Test
    public void testValidSessionData() throws ServletException, IOException, ContextStoreException {
        
        //store the security context to a cookie, re-add the previous cookies first
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        securityContextService.setSecurityContextToSession(mockRequest, mockResponse, originalSc);
        Cookie sidCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_SESSION, VALID_SFDC_SID);
        Cookie endpointCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT, VALID_SFDC_ENDPOINT);
        request.setCookies(sidCookie, endpointCookie,
                            mockResponse.getCookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME));
        
        session.setAttribute(SECURITY_CONTEXT_TO_VERIFY_KEY, originalSc);
        request.setSession(session);
        
        filter.doFilter(request, response, new PostAuthFilterChain());
    }

    /**
     * User is authenticated and they visit an instance that has
     * their java session, but the sfdc session id in the Security 
     * Context doesn't match the one in their cookie (stale data).
     * 
     * Here we set up a request that has the sfdc sid and endpoint cookies.
     * A security context is also put into the session, but it has a different
     * session id in it. This simulates stale data in the session. We test that the
     * context in the session at the end came from the partner api because we expect
     * a call to refresh the data.
     * @throws ContextStoreException 
     */
    @Test
    public void testInvalidSFDCSessionId() throws ServletException, IOException, ContextStoreException  {
        
        SecurityContext sc = new ForceSecurityContext();
        
        sc.setEndPoint(originalSc.getEndPoint());
        sc.setSessionId(INVALID_SFDC_SID);
        sc.setOrgId(originalSc.getOrgId());
        sc.setUserId(originalSc.getUserId());
        sc.setUserName(originalSc.getUserName());
        sc.setLanguage(originalSc.getLanguage());
        
        //store the security context to a cookie, re-add then previous cookies first
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        securityContextService.setSecurityContextToSession(mockRequest, mockResponse, sc);
        Cookie sidCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_SESSION, VALID_SFDC_SID);
        Cookie endpointCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT, VALID_SFDC_ENDPOINT);
        request.setCookies(sidCookie, endpointCookie,
                            mockResponse.getCookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME));

        session.setAttribute(SECURITY_CONTEXT_TO_VERIFY_KEY, partnerSc);
        request.setSession(session);
        filter.doFilter(request, response, new PostAuthFilterChain());
    }

    /**
     * User is authenticated and they visit an instance that does
     * not have their Java session.
     * 
     * Here we set up a request that has the sfdc sid and endpoint cookies.
     * No security context is put into the session to simulate hitting an instance that 
     * you don't have a session with. We test that the context in the session at the 
     * end came from the partner api because we expect a call to get the data.
     */    
    @Test
    public void testNoDataInSession() throws ServletException, IOException  {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute(SECURITY_CONTEXT_TO_VERIFY_KEY, partnerSc);
        request.setSession(mockSession);
        filter.doFilter(request, response, new PostAuthFilterChain());
    }
    
    /**
     * Same as testNoDataInSession, but we'll use localhost here to verify that the cookies
     * are not set as secure.
     */    
    @Test
    public void testNoDataInSessionLocalhost() throws ServletException, IOException  {
        MockHttpSession mockSession = new MockHttpSession();
        mockSession.setAttribute(SECURITY_CONTEXT_TO_VERIFY_KEY, partnerSc);
        request.setSession(mockSession);
        request.setLocalName("localhost");
        filter.doFilter(request, response, new PostAuthFilterChain());
    }
    
    private SecurityContext retreiveSecurityContextFromCookie(MockHttpServletResponse mockResponse, boolean checkSecurity)
        throws Exception {
        
        Cookie cookie = mockResponse.getCookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME);
        if (checkSecurity) {
            Assert.assertTrue(cookie.getSecure(), "Cookie should be set as secure");
        } else {
            Assert.assertFalse(cookie.getSecure(), "Cookie should not be set as secure for localhost");
        }
        String value = cookie.getValue();
        
        if (value == null) {
            return null;
        }
        
        return deserializeSecurityContext(Base64.decode(value.getBytes()), true);
    }
    
    private SecurityContext deserializeSecurityContext(byte[] securityContextSer, boolean encrypted)
        throws ForceEncryptionException, IOException, ClassNotFoundException {
        securityContextSer =  AESUtil.decrypt(securityContextSer, securityContextService.getSecretKey());
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(securityContextSer));
        return (SecurityContext) in.readObject();
    }
}
