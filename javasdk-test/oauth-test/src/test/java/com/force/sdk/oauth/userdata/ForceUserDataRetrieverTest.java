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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.oauth.BaseMockedPartnerConnectionTest;
import com.force.sdk.oauth.context.SecurityContext;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Test the user data retrievers.
 *
 * @author John Simone
 */
public class ForceUserDataRetrieverTest extends BaseMockedPartnerConnectionTest {

    @Test
    public void testRetrieveUserData() throws ConnectionException {
        UserDataRetriever dataRetriever = new ForceUserDataRetriever();
        dataRetriever.setStoreUsername(true);
        dataRetriever.setSessionId(VALID_SFDC_SID);
        dataRetriever.setEndpoint(VALID_SFDC_ENDPOINT);
        SecurityContext sc = dataRetriever.retrieveUserData();
        
        assertSecurityContextsAreEqual(partnerSc, sc,
                "retrieved security context is not equal to that which is returned by the mocked partner connector");
    }
    
    @Test
    public void testRetrieveUserDataNoUsername() throws ConnectionException {
        UserDataRetriever dataRetriever = new ForceUserDataRetriever();
        dataRetriever.setStoreUsername(false);
        dataRetriever.setSessionId(VALID_SFDC_SID);
        dataRetriever.setEndpoint(VALID_SFDC_ENDPOINT);
        SecurityContext sc = dataRetriever.retrieveUserData();
        
        Assert.assertNull(sc.getUserName(), "username should be null when storeUsername is set to false");
        Assert.assertEquals(sc.getUserId(), partnerSc.getUserId(),
                "Security context should match that which the partner connection returned");
        Assert.assertEquals(sc.getSessionId(), partnerSc.getSessionId(),
                "Security context should match that which the partner connection returned");
        Assert.assertEquals(sc.getEndPoint(), partnerSc.getEndPoint(),
                "Security context should match that which the partner connection returned");
        
    }
    
    @Test
    public void testRetrieveUserDataNullSession() throws ConnectionException {
        UserDataRetriever dataRetriever = new ForceUserDataRetriever();
        dataRetriever.setSessionId(null);
        dataRetriever.setEndpoint(VALID_SFDC_ENDPOINT);
        
        try {
            dataRetriever.retrieveUserData();
            Assert.fail("Illegal argument exception should be thrown when retrieveUserData is called with a null session id");
        } catch (IllegalArgumentException e) {
            // Expected
        }

    }
    
    @Test
    public void testRetrieveUserDataNullEndpoint() throws ConnectionException {
        UserDataRetriever dataRetriever = new ForceUserDataRetriever();
        dataRetriever.setSessionId(VALID_SFDC_SID);
        dataRetriever.setEndpoint(null);
        
        try {
            dataRetriever.retrieveUserData();
            Assert.fail("Illegal argument exception should be thrown when retrieveUserData is called with a null endpoint");
        } catch (IllegalArgumentException e) {
            // Expected
        }
        
    }
}
