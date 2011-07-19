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

package com.force.utils;

import com.force.sdk.oauth.context.SecurityContext;
import com.force.sdk.oauth.context.SecurityContextUtil;
import com.sforce.ws.ConnectionException;
import mockit.Instantiation;
import mockit.Mock;
import mockit.MockClass;

/**
 * This class mocks the {@code SecurityContextUtil}::initializeSecurityContextFromApi, when a mock oauth server is
 * used to replace actual SFDC url.
 *
 * @author Nawab Iqbal
 */

@MockClass(realClass = SecurityContextUtil.class, instantiation = Instantiation.PerMockSetup)
public final class MockSecurityContextUtil {

    private MockSecurityContextUtil() { }

    /**
     * Fills mocked values into security context object; so that we don't need to do an api call.
     * @param securityContext  The mocked values are added to this object.
     * @throws com.sforce.ws.ConnectionException Not being used. It is only to keep the signature consistent with actual method.
     */
    @Mock
    public static void initializeSecurityContextFromApi(SecurityContext securityContext) throws ConnectionException  {
        System.out.println("Getting mocked security context.");
        securityContext.setOrgId("dummy Org");
        securityContext.setUserId("mock userId");
        securityContext.setUserName("mock un");
        securityContext.setLanguage("mock l");
        securityContext.setLocale("mock L");
        securityContext.setTimeZone("mock tz");
        securityContext.setRole(SecurityContextUtil.DEFAULT_ROLE);
    }
}
