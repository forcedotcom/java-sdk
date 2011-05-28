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

package com.force.mockoauthserver.controller;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import com.force.mockoauthserver.model.AuthResponsePayload;

/**
 * 
 * The methods in this class mock out response from the SFDC Core App's OAuth Authentication server.
 * More information about the SFDC OAuth flow can be found at 
 * {@link http://wiki.developerforce.com/index.php/Digging_Deeper_into_OAuth_2.0_on_Force.com}
 * @author Jeff Lai
 */
@Path("/token")
public class MockAuthService {
    
    private static final long MILLISEC_PER_SEC = 1000L;
    
    /**
     * This method mocks the authorization request from SFDC core to an OAuth enabled client application.
     * @param code The value returned by the authorization server
     * @param grantType Set this to authorization_code
     * @param clientId  Your application's client identifier
     * @param clientSecret Your application's client secret (consumer secret in Remote Access Detail)
     * @param redirectUri callback url for client application
     * @return JSON response
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeClient(@FormParam("code") String code, @FormParam("grant_type") String grantType,
            @FormParam("client_id") String clientId, @FormParam("client_secret") String clientSecret,
            @FormParam("redirect_uri") String redirectUri) {

        AuthResponsePayload payload = new AuthResponsePayload();
        payload.setId("https://login.salesforce.com/id/00D50000000IZ3ZEAW/00550000001fg5OAAQ");
        payload.setAccessToken("222");
        payload.setInstanceUrl("https://na1.salesforce.com");
        payload.setIssuedAt(String.valueOf(System.currentTimeMillis() / MILLISEC_PER_SEC));
        payload.setRefreshToken("333");
        payload.setSignature("555");
        
        return Response.ok(payload).build();
    }

}
