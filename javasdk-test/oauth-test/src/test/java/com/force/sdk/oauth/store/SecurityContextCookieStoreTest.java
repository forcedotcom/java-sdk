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

package com.force.sdk.oauth.store;

import com.force.sdk.oauth.context.ForceSecurityContext;
import com.force.sdk.oauth.context.SecurityContextUtil;
import com.sforce.ws.ConnectionException;
import mockit.Mockit;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.oauth.BaseMockedPartnerConnectionTest;
import com.force.sdk.oauth.context.SecurityContext;
import com.force.sdk.oauth.context.store.*;

import static org.testng.Assert.assertEquals;

/**
 * 
 * Tests the serialization of security contexts into cookies.
 *
 * @author John Simone
 */
public class SecurityContextCookieStoreTest extends BaseMockedPartnerConnectionTest {

    @Test
    public void testSecurityContextSerialization() throws ContextStoreException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        SecurityContextStorageService storageService = new SecurityContextCookieStore();
        storageService.storeSecurityContext(request, response, originalSc);
        
        Assert.assertNotNull(
                response.getCookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME),
                "Cookie containing the security context should have been set");
        
        request = new MockHttpServletRequest();
        request.setCookies(response.getCookies());
        
        SecurityContext sc = storageService.retreiveSecurityContext(request);
        
        Assert.assertNotNull(sc, "Security context should not be null after retrieval from the cookie store");
        assertSecurityContextsAreEqual(
                originalSc, sc, "Security context should be equal after serialization and deserialization to a cookie");
    }

    @Test
    public void userWithoutProfileAccessShouldHaveDefaultRole() throws ConnectionException {
        try {
            Mockit.setUpMocks(MockInsufficientProfileAccessPartnerConnection.class);
            SecurityContext sc = new ForceSecurityContext();
            sc.setEndPoint(originalSc.getEndPoint());
            sc.setSessionId(VALID_SFDC_SID);
            SecurityContextUtil.initializeSecurityContextFromApi(sc);
            assertEquals(sc.getRole(), SecurityContextUtil.DEFAULT_ROLE);
        } finally {
            Mockit.tearDownMocks(MockInsufficientProfileAccessPartnerConnection.class);
        }
    }
}
