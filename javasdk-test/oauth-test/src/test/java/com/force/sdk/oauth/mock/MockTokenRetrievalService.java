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

package com.force.sdk.oauth.mock;

import com.force.sdk.oauth.connector.ForceOAuthConnectionInfo;
import com.force.sdk.oauth.connector.TokenRetrievalService;
import org.testng.Assert;

import java.io.IOException;

/**
 * 
 * This is a mock of the token retrieval service. Used for testing without
 * making API calls. The values passed in for oauth key and secret will 
 * also be veriefied by this class.
 *
 * @author John Simone
 */
public class MockTokenRetrievalService implements TokenRetrievalService {

    private static final String CORRECT_RESPONSE =
        "{\"id\":\"%1$s\","
        + "\"issued_at\":\"1306442444753\","
        + "\"refresh_token\":\"%2$s\","
        + "\"instance_url\":\"%3$s\","
        + "\"signature\":\"9sR2+n0Yy06NPhWg5ll5WbEY26vcTX6hsaRmIQNXRME=\","
        + "\"access_token\":\"%4$s\"}";
    
    private String endpoint;
    private String refreshToken;
    private String instanceUrl;
    private String accessToken;
    private String oauthKey;
    private String oauthSecret;
    
    public MockTokenRetrievalService(
            String endpoint, String accessToken, String instanceUrl, String refreshToken, String oauthKey, String oauthSecret) {
        this.endpoint = endpoint;
        this.refreshToken = refreshToken;
        this.instanceUrl = instanceUrl;
        this.accessToken = accessToken;
        this.oauthKey = oauthKey;
        this.oauthSecret = oauthSecret;
    }
    
    @Override
    public String retrieveToken(String hostAndPort, String params, String refreshTok,
            ForceOAuthConnectionInfo connInfo) throws IOException {
        String[] paramArray = params.split("&");
        
        boolean foundKey = false;
        boolean foundSecret = false;
        for (int i = 0; i < paramArray.length; i++) {
            if (paramArray[i].startsWith("client_id")) {
                Assert.assertEquals(paramArray[i].substring("client_id=".length()), oauthKey,
                        "Wrong OAuth key used when attempting connection");
                foundKey = true;
            }

            if (paramArray[i].startsWith("client_secret")) {
                Assert.assertEquals(paramArray[i].substring("client_secret=".length()),
                        oauthSecret, "Wrong OAuth key used when attempting connection");
                foundSecret = true;
            }
        }
        
        Assert.assertTrue(foundKey, "No OAuth key in url parameters");
        Assert.assertTrue(foundSecret, "No OAuth secret in url parameters");
        
        return String.format(CORRECT_RESPONSE, endpoint, refreshToken, instanceUrl, accessToken);
    }

}
