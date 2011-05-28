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

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import com.force.sdk.oauth.ForceUserPrincipal;
import com.force.sdk.oauth.context.ForceSecurityContext;
import com.force.sdk.oauth.context.SecurityContext;
import com.force.sdk.springsecurity.OAuthAuthenticationToken;
import com.force.sdk.springsecurity.data.SpringSecurityTestData;

/**
 *
 * This {@code AuthenticationProviderMock} mocks {@code AuthenticationProvider} to emulate authenticate(...) method
 * without using {@code ForceOAuthConnector}.
 *
 * @author John Simone
 */
public class OAuthAuthenticationProviderMock  implements AuthenticationProvider {

    /**
     * Create an {@code Authentication} object by using constants defined in {@code SpringSecurityTestData}.
     *
     * @param authentication {@code Authentication}
     * @return {@code Authentication}
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        SecurityContext sc = new ForceSecurityContext();
        sc.setEndPoint(SpringSecurityTestData.SFDC_ENDPOINT);
        sc.setSessionId(SpringSecurityTestData.SFDC_SESSION_ID);
        sc.setRefreshToken(SpringSecurityTestData.OAUTH_REFRESH_TOKEN);
        sc.setUserName(SpringSecurityTestData.SFDC_USERNAME);
        return createAuthentication(sc);
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        // TODO Auto-generated method stub
        return true;
    }
    
    private static Authentication createAuthentication(SecurityContext sc) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        OAuthAuthenticationToken newAuthToken =
            new OAuthAuthenticationToken(new ForceUserPrincipal(sc.getUserName(), sc.getSessionId()), null, authorities);
        newAuthToken.setDetails(sc);
        return newAuthToken;
    }

}
