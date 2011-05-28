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

package com.force.sdk.springsecurity.mock;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.testng.Assert;

import com.force.sdk.oauth.connector.ForceOAuthConnector;
import com.force.sdk.oauth.context.ForceSecurityContext;
import com.force.sdk.oauth.context.SecurityContext;
import com.force.sdk.springsecurity.data.SpringSecurityTestData;

/**
 *
 * This {@code OAuthConnectorMock} mocks {@code ForceOAuthConnector} and provides the functionality without calling
 * the oauth api.
 *
 * @author John Simone
 */
public class OAuthConnectorMock extends ForceOAuthConnector {

    @Override
    public OAuthVersion getOAuthVersion() {
        Assert.fail("WARNING: unimplemented mock method being called");
        return super.getOAuthVersion();
    }

    @Override
    public String getHostPort(HttpServletRequest request) {
        Assert.fail("WARNING: unimplemented mock method being called");
        return super.getHostPort(request);
    }

    @Override
    public String getForceLogoutUrl(HttpServletRequest request,
            String forceEndPoint, String localLogoutSuccessfulPath) {
        Assert.fail("WARNING: unimplemented mock method being called");
        return super.getForceLogoutUrl(request, forceEndPoint,
                localLogoutSuccessfulPath);
    }

    @Override
    public String getRedirectUri(HttpServletRequest request) {
        return SpringSecurityTestData.OAUTH_REDIRECT_URI;
    }

    @Override
    public String getLoginRedirectUrl(HttpServletRequest request)
            throws UnsupportedEncodingException {
        return SpringSecurityTestData.REDIRECT_URL;
    }

    @Override
    public String getAccessCode(HttpServletRequest request) {
        return SpringSecurityTestData.OAUTH_ACCESS_CODE;
    }

    @Override
    public SecurityContext refreshAccessToken(String refreshToken) throws IOException {
        SecurityContext securityContext = new ForceSecurityContext();
        securityContext.setEndPoint(SpringSecurityTestData.SFDC_ENDPOINT);
        securityContext.setSessionId(SpringSecurityTestData.SFDC_SESSION_ID_2);
        securityContext.setRefreshToken(SpringSecurityTestData.OAUTH_REFRESH_TOKEN);
        securityContext.setUserName(SpringSecurityTestData.SFDC_USERNAME);
        securityContext.setRole(SpringSecurityTestData.DEFAULT_ROLE);
        return securityContext;
    }

}
