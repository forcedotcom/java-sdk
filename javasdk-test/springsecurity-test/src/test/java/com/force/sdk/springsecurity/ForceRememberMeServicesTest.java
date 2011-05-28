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

import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.*;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.oauth.context.*;
import com.force.sdk.oauth.context.store.*;

/**
 *
 * This {@code ForceRememberMeServicesTest} tests {@code ForceRememberMeServices} and
 * {@code SecurityContextServiceImpl}. It verifies:
 * <ul>
 *  <li>loginsuccess stores session
 *  <li>autologin uses the stored session
 *  <li>setSecurityContextToSession
 *  <li>clearSecurityContext
 *  </ul>
 *
 * @author John Simone
 * @author Nawab Iqbal
 */
public class ForceRememberMeServicesTest {

    //these are the starting values for a valid security context
    public static final String VALID_SFDC_ENDPOINT = "http://test.url.login.com/services/Soap/u/21";
    public static final String VALID_SFDC_SID = "thisisavalidsfdcsessionid";
    public static final String ORIGINAL_ORG_ID = "org_id";
    public static final String ORIGINAL_USER_ID = "user_id";
    public static final String ORIGINAL_USER_NAME = "user_name";
    public static final String ORIGINAL_USER_LANGUAGE = "user_language";
    public static final String ORIGINAL_USER_LOCALE = "US";
    public static final String ORIGINAL_USER_TIMEZONE = "GMT";
    public static final String ROLE = "ROLE_USER";
    private SecurityContext securityContext;
    private boolean setSecurityContextCalled;
    
    @BeforeClass
    public void init() {
        securityContext = new ForceSecurityContext();
        
        securityContext.setEndPoint(VALID_SFDC_ENDPOINT);
        securityContext.setSessionId(VALID_SFDC_SID);
        securityContext.setOrgId(ORIGINAL_ORG_ID);
        securityContext.setUserId(ORIGINAL_USER_ID);
        securityContext.setUserName(ORIGINAL_USER_NAME);
        securityContext.setLanguage(ORIGINAL_USER_LANGUAGE);
        securityContext.setRole(ROLE);
    }
    
    /**
     *
     * This {@code TestSecurityContextServiceReturnSession} is injected in {@code ForceRememberMeServices} to verify
     * that certain methods are called with the expected parameters.
     *
     * @author John Simone
     * @author Nawab Iqbal
     */
    private class TestSecurityContextServiceReturnSession implements SecurityContextService {
        @Override
        public void setSecurityContextToSession(HttpServletRequest request, HttpServletResponse response,
                SecurityContext sc) {
            setSecurityContextCalled = true;
            assertSecurityContextsAreEqual(securityContext, sc,
                            "The stored security context should match that which was passed in the authentication object");
        }

        @Override
        public SecurityContext getSecurityContextFromSession(HttpServletRequest request) {
            return securityContext;
        }

        @Override
        public SecurityContext verifyAndRefreshSecurityContext(SecurityContext sc, HttpServletRequest request) {
            Assert.fail("verifyAndRefreshSecurityContext should not be called by ForceRememberMeServices");
            return null;
        }

        @Override
        public void clearSecurityContext(HttpServletRequest request, HttpServletResponse response) {
            Assert.fail("clearSecurityContext should not be called by ForceRememberMeServices");
        }

        @Override
        public SecretKeySpec getSecretKey() throws ForceEncryptionException {
            return null;
        }
    }
    
    /**
     *
     * This {@code TestSecurityContextServiceNoSession} is injected in {@code ForceRememberMeServices} to verify
     * the behavior when session does not have {@code SecurityContext}.
     *
     * @author John Simone
     * @author Nawab Iqbal
     */
    private class TestSecurityContextServiceNoSession implements SecurityContextService {

        @Override
        public void setSecurityContextToSession(HttpServletRequest request, HttpServletResponse response,
                SecurityContext sc) {
            setSecurityContextCalled = true;
        }

        @Override
        public SecurityContext getSecurityContextFromSession(HttpServletRequest request) {
            return null;
        }

        @Override
        public SecurityContext verifyAndRefreshSecurityContext(SecurityContext sc, HttpServletRequest request) {
            Assert.fail("verifyAndRefreshSecurityContext should not be called by ForceRememberMeServices");
            return null;
        }

        @Override
        public void clearSecurityContext(HttpServletRequest request, HttpServletResponse response) {
            Assert.fail("clearSecurityContext should not be called by ForceRememberMeServices");
        }

        @Override
        public SecretKeySpec getSecretKey() throws ForceEncryptionException {
            return null;
        }
    }
    
    @Test
    public void testAutoLoginContextInSession() {
        SecurityContextService securityContextService = new TestSecurityContextServiceReturnSession();
        ForceRememberMeServices rememberMeServices = new ForceRememberMeServices();
        rememberMeServices.setSecurityContextService(securityContextService);
        
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        
        Authentication authentication = rememberMeServices.autoLogin(request, response);
        SecurityContext sc = (SecurityContext) authentication.getDetails();
        assertSecurityContextsAreEqual(securityContext, sc,
                "loginSuccess should return an Authentication object with the SecurityContext from the session");
    }
    
    @Test
    public void testAutoLoginNoContextInSession() {
        SecurityContextService securityContextService = new TestSecurityContextServiceNoSession();
        ForceRememberMeServices rememberMeServices = new ForceRememberMeServices();
        rememberMeServices.setSecurityContextService(securityContextService);
        
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Assert.assertNull(rememberMeServices.autoLogin(request, response),
                "loginSuccess should return null if there is no valid security context in the session");
    }
    
    @Test
    public void testLoginSuccess() {
        setSecurityContextCalled = false;

        SecurityContextService securityContextService = new TestSecurityContextServiceReturnSession();
        ForceRememberMeServices rememberMeServices = new ForceRememberMeServices();
        rememberMeServices.setSecurityContextService(securityContextService);
        
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl(ROLE));
        OAuthAuthenticationToken newAuthToken = new OAuthAuthenticationToken(null, null, authorities);
        newAuthToken.setDetails(securityContext);

        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        
        rememberMeServices.loginSuccess(request, response, newAuthToken);
        
        Assert.assertTrue(setSecurityContextCalled, "SecurityContext was not remembered.");
    }
    
    @DataProvider(name = "storeTypes")
    public Object[][] appCommandExpectedInput() {
        return new Object[][]{
                {SecurityContextCookieStore.class, new String[] {SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME,
                    SecurityContextUtil.FORCE_FORCE_ENDPOINT,
                    SecurityContextUtil.FORCE_FORCE_SESSION}} ,
                {SecurityContextSessionStore.class, new String[] {SecurityContextUtil.FORCE_FORCE_ENDPOINT,
                    SecurityContextUtil.FORCE_FORCE_SESSION}}
        };
    }

    @Test (dataProvider = "storeTypes")
    public void testClearSecurityContext(Class storeClass, String [] cookieNames)
    throws InstantiationException, IllegalAccessException {
        SecurityContextServiceImpl securityContextService = new SecurityContextServiceImpl();
        securityContextService.setSecurityContextStorageService((SecurityContextStorageService) storeClass.newInstance());
        
        HttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Assert.assertEquals(response.getCookies().length, 0);
        securityContextService.clearSecurityContext(request, response);
        Assert.assertEquals(response.getCookies().length, cookieNames.length);
        
        for (int i = 0; i < cookieNames.length; i++) {
            verifyEmptyCookie(response, cookieNames[i]);
        }
    }

    private void verifyEmptyCookie(MockHttpServletResponse response, String cookieName) {
        Cookie c = response.getCookie(cookieName);
        Assert.assertNotNull(c);
        Assert.assertEquals(c.getValue(), "");
        Assert.assertEquals(c.getMaxAge(), 0);
    }
    
    /**
     * Do a deep compare of the two security context instances while asserting each value.
     * 
     * @param expectedSc
     * @param actualSc
     * @param message
     */
    protected void assertSecurityContextsAreEqual(SecurityContext expectedSc, SecurityContext actualSc, String message) {
        
        Assert.assertEquals(expectedSc.getEndPoint(), actualSc.getEndPoint(), message + " - field: endpoint - ");
        Assert.assertEquals(expectedSc.getSessionId(), actualSc.getSessionId(), message + " - field: session id - ");
        Assert.assertEquals(expectedSc.getUserId(), actualSc.getUserId(), message + " - field: user id - ");
        Assert.assertEquals(expectedSc.getUserName(), actualSc.getUserName(), message + " - field: user name - ");
        Assert.assertEquals(expectedSc.getOrgId(), actualSc.getOrgId(), message + " - field: org id - ");
        Assert.assertEquals(expectedSc.getLanguage(), actualSc.getLanguage(), message + " - field: language - ");
        
    }
    
}
