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

package com.force.sdk.oauth.userdata;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import mockit.*;

import org.springframework.mock.web.*;
import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.oauth.AuthFilter;
import com.force.sdk.oauth.context.*;
import com.force.sdk.oauth.context.store.SecurityContextSessionStore;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.ws.ConnectionException;

/**
 * Tests the user data retrieval service logic. 
 * These tests will verify that the correct logic is executed in both the 
 * standard and custom cases.
 * 
 * @author John Simone
 *
 */
public class UserDataRetrievalServiceTest {

    private static final String TEST_VALUE = "testvalue";
    private static final String SESSION_ID = "session_id";
    private static final String BAD_SESSION_ID = "bad_session_id";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String ENDPOINT = "endpoint";
    private static final String USERNAME = "username";
    private static final String ORG_ID = "org_id";
    private static final String USER_ID  = "user_id";
    private static final String USER_LANGUAGE = "user_language";
    private static final String USER_LOCALE = "user_locale";
    private static final String USER_TIMEZONE = "user_timezone";
    private static final String USER_ROLE = "role";
    
    /**
     * Test custom security context for the custom data retrieval tests.
     * 
     * @author John Simone
     */
    public static class TestSecurityContext extends CustomSecurityContext {
        
        private String testValue;

        public String getTestValue() {
            return testValue;
        }

        public void setTestValue(String testValue) {
            this.testValue = testValue;
        }
    }
    
    /**
     * Test custom data retriever for the custom data retrieval tests.
     * 
     * @author John Simone
     */
    public static class TestUserDataRetriever extends CustomUserDataRetriever<TestSecurityContext> {

        @Override
        public TestSecurityContext retrieveUserData() {
            TestSecurityContext tsc = new TestSecurityContext();
            tsc.setTestValue(TEST_VALUE);
            return tsc;
        }
        
    }
    
    /**
     * Mock of ForceUserDataRetriever to help isolate the service.
     * 
     * @author John Simone
     */
    @MockClass(realClass = ForceUserDataRetriever.class, instantiation = Instantiation.PerMockSetup)
    protected static class MockForceDataRetriever {
        
        public boolean assertRefreshToken = true;
        boolean badSession = false;
        
        @Mock
        public SecurityContext retrieveUserData()
            throws ConnectionException {
            
            if (badSession) {
                badSession = false;
                throw new ConnectionException();
            }
            
            SecurityContext sc = new ForceSecurityContext();
            
            sc.setSessionId(SESSION_ID);
            sc.setEndPoint(ENDPOINT);
            sc.setRefreshToken(REFRESH_TOKEN);
            sc.setUserName(USERNAME);
            
            return sc;
        }
        
        @Mock
        public void setSessionId(String sessionId) {
            
            if (sessionId.equals(BAD_SESSION_ID)) {
                badSession = true;
                return;
            }
            
            Assert.assertEquals(
                    sessionId, SESSION_ID,
                    "UserDataRetrievalService passed the incorrect session id to the ForceUserDataRetriever");
        }

        @Mock
        public void setEndpoint(String endpoint) {
            Assert.assertEquals(
                    endpoint, ENDPOINT,
                    "UserDataRetrievalService passed the incorrect endpoint to the ForceUserDataRetriever");
        }

        @Mock
        public void setRefreshToken(String refreshToken) {
            if (assertRefreshToken) {
                Assert.assertEquals(
                        refreshToken, REFRESH_TOKEN,
                        "UserDataRetrievalService passed the incorrect refresh token to the ForceUserDataRetriever");
            }
        }
    }
    
    private MockForceDataRetriever mockDataRetriever;
    
    @BeforeClass
    public void registerMocks() {
        mockDataRetriever = new MockForceDataRetriever();
        Mockit.setUpMock(ForceUserDataRetriever.class, mockDataRetriever);
    }
    
    @AfterClass
    public void unregisterMocks() {
        Mockit.tearDownMocks(ForceUserDataRetriever.class);
    }
    
    @Test
    public void testUserDataService() throws ConnectionException {
        UserDataRetrievalService userDataRetrievalService =
            new UserDataRetrievalService();
        
        SecurityContext sc =
            userDataRetrievalService.retrieveUserData(SESSION_ID, ENDPOINT, REFRESH_TOKEN);
        verifyForceSecurityContext(sc);
    }
    
    @Test
    public void testCustomUserDataService() throws ConnectionException {
        UserDataRetrievalService userDataRetrievalService =
            new CustomUserDataRetrievalService(new TestUserDataRetriever());
        
        SecurityContext sc =
            userDataRetrievalService.retrieveUserData(SESSION_ID, ENDPOINT, REFRESH_TOKEN);
        verifyForceSecurityContext(sc);
        verifyTestSecurityContext(sc);
    }
    
    @Test
    public void testCustomSecurityContext() throws ConnectionException {
        TestSecurityContext tsc = new TestSecurityContext();
        tsc.setForceSecurityContext(new ForceSecurityContext());
        GetUserInfoResult userInfo = new GetUserInfoResult();
        userInfo.setOrganizationId(ORG_ID);
        userInfo.setUserId(USER_ID);
        userInfo.setUserName(USERNAME);
        userInfo.setUserLanguage(USER_LANGUAGE);
        userInfo.setUserLocale(USER_LOCALE);
        userInfo.setUserTimeZone(USER_TIMEZONE);
        tsc.init(userInfo);
        
        Assert.assertEquals(tsc.getOrgId(), ORG_ID);
        Assert.assertEquals(tsc.getUserId(), USER_ID);
        Assert.assertEquals(tsc.getUserName(), USERNAME);
        Assert.assertEquals(tsc.getLanguage(), USER_LANGUAGE);
        Assert.assertEquals(tsc.getLocale(), USER_LOCALE);
        Assert.assertEquals(tsc.getTimeZone(), USER_TIMEZONE);
    }
    
    @Test
    public void testCustomSecurityContextProperties() throws ConnectionException {
        TestSecurityContext tsc = new TestSecurityContext();
        tsc.setForceSecurityContext(new ForceSecurityContext());
        tsc.setOrgId(ORG_ID);
        tsc.setUserId(USER_ID);
        tsc.setUserName(USERNAME);
        tsc.setLanguage(USER_LANGUAGE);
        tsc.setLocale(USER_LOCALE);
        tsc.setTimeZone(USER_TIMEZONE);
        tsc.setEndPoint(ENDPOINT);
        tsc.setSessionId(SESSION_ID);
        tsc.setRefreshToken(REFRESH_TOKEN);
        tsc.setRole(USER_ROLE);
        
        Assert.assertEquals(tsc.getOrgId(), ORG_ID);
        Assert.assertEquals(tsc.getUserId(), USER_ID);
        Assert.assertEquals(tsc.getUserName(), USERNAME);
        Assert.assertEquals(tsc.getLanguage(), USER_LANGUAGE);
        Assert.assertEquals(tsc.getLocale(), USER_LOCALE);
        Assert.assertEquals(tsc.getTimeZone(), USER_TIMEZONE);
        Assert.assertEquals(tsc.getEndPoint(), ENDPOINT);
        Assert.assertEquals(tsc.getSessionId(), SESSION_ID);
        Assert.assertEquals(tsc.getRefreshToken(), REFRESH_TOKEN);
        Assert.assertEquals(tsc.getRole(), USER_ROLE);
        Assert.assertEquals(tsc.getForceSecurityContext().getOrgId(), ORG_ID);
        Assert.assertEquals(tsc.getForceSecurityContext().getRole(), USER_ROLE);
    }
    
    @Test
    public void testBadSessionId() {
        UserDataRetrievalService userDataRetrievalService =
            new UserDataRetrievalService();
        
        try {
            userDataRetrievalService.retrieveUserData(BAD_SESSION_ID, ENDPOINT, REFRESH_TOKEN);
            Assert.fail("retrieveUserData should bubble up connect exceptions if they are thrown from retriever classes");
        } catch (ConnectionException e) {
            // Expected
        }
    }
    
    @Test
    public void testWithAuthFilter() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
       
        // Add session id and endpoint cookies to the request
        Cookie sidCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_SESSION,  SESSION_ID);
        Cookie endpointCookie = new Cookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT, ENDPOINT);
        request.setCookies(sidCookie, endpointCookie);
        
        AuthFilter filter = new AuthFilter();
        MockFilterConfig filterConfig = new MockFilterConfig();
        
        // Add good OAuth info
        filterConfig.addInitParameter("customDataRetriever", TestUserDataRetriever.class.getName());
        filterConfig.addInitParameter("endpoint", ENDPOINT);
        filterConfig.addInitParameter("oauthKey", "key");
        filterConfig.addInitParameter("oauthSecret", "secret");
        filterConfig.addInitParameter("securityContextStorageMethod", "session");
        
        filter.init(filterConfig);
        
        VerificationFilterChain filterChain = new VerificationFilterChain();
        mockDataRetriever.assertRefreshToken = false;
        try {
            filter.doFilter(request, response, filterChain);
        } finally {
            mockDataRetriever.assertRefreshToken = true;
        }
        
        HttpSession session = request.getSession();
        TestSecurityContext sc =
            (TestSecurityContext) session.getAttribute(SecurityContextSessionStore.SECURITY_CONTEXT_SESSION_KEY);
        verifyForceSecurityContext(sc);
        Assert.assertEquals(sc.getTestValue(), TEST_VALUE);
        
        Assert.assertEquals(response.getCookie(SecurityContextUtil.FORCE_FORCE_SESSION).getValue(), SESSION_ID);
        Assert.assertEquals(response.getCookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT).getValue(), ENDPOINT);
    }
    
    /**
     * Mock filter chain to verify state in auth filter.
     * 
     * @author John Simone
     */
    private static class VerificationFilterChain implements FilterChain {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            Assert.assertTrue((Boolean) request.getAttribute("__force_auth_filter_already_visited"));
            TestSecurityContext tsc = (TestSecurityContext) ForceSecurityContextHolder.get();
            Assert.assertEquals(tsc.getTestValue(), TEST_VALUE);
            UserDataRetrievalServiceTest.verifyForceSecurityContext(tsc);
        }
    }
    
    private static void verifyForceSecurityContext(SecurityContext sc) {
        Assert.assertEquals(sc.getUserName(), USERNAME, "usernames do not match in the security context");
        Assert.assertEquals(sc.getSessionId(), SESSION_ID, "session ids do not match in the security context");
        Assert.assertEquals(sc.getEndPoint(), ENDPOINT, "endpoints do not match in the security context");
        Assert.assertEquals(sc.getRefreshToken(), REFRESH_TOKEN, "refresh tokens do not match in the security context");
    }
    
    private void verifyTestSecurityContext(SecurityContext sc) {
        
        if (!(sc instanceof CustomSecurityContext)) {
            Assert.fail("Security context should be of type CustomSecurityContext");
        }

        if (!(sc instanceof TestSecurityContext)) {
            Assert.fail("Security context should be of type TestSecurityContext");
        }
        
        TestSecurityContext tsc = (TestSecurityContext) sc;
        Assert.assertEquals(tsc.getTestValue(), TEST_VALUE, "field in custom security context is incorrect");
    }
    
}
