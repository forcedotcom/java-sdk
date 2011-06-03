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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
 * The methods in this class mock out response from the SFDC Core App's OAuth Authentication server.
 * More information about the SFDC OAuth flow can be found at 
 * {@link http://wiki.developerforce.com/index.php/Digging_Deeper_into_OAuth_2.0_on_Force.com}
 *
 * Sample Force Url:
 * force://localhost:9966/force-mock-oauth-server-app;user=username;password=pwd;oauth_key=key;oauth_secret=secret"
 *
 * @author Jeff Lai
 * @author Nawab Iqbal
 */
@Path("/oauth2/authorize")
public class MockAuthorizePath {
    
    private static final long MILLISEC_PER_SEC = 1000L;
    
    /**
     * This method mocks the authorization request from SFDC core to an OAuth enabled client application.
     * @param responseType The value returned by the authorization server.
     * @param clientId  Your application's client identifier.
     * @param redirectUri callback url for client application.
     * @param state any state sent from the app.
     * @return JSON response.
     *
     * @throws URISyntaxException if the redirect_uri or state parameters are not in proper format.
     */
    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeUser(@QueryParam("response_type") String responseType,
                                    @QueryParam("client_id") String clientId,
                                    @QueryParam("redirect_uri") String redirectUri,
                                    @QueryParam("state") String state) throws URISyntaxException {
        URI u = new URI(redirectUri
                + "?code=mytoken&"
                + "state=" + state);

        return    Response.temporaryRedirect(u).status(302).build();
    }

}
