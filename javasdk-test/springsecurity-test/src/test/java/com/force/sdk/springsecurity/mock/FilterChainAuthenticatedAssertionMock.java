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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.testng.Assert;

import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.oauth.context.ForceSecurityContextHolder;
import com.force.sdk.oauth.context.SecurityContext;
import com.force.sdk.springsecurity.data.SpringSecurityTestData;
import com.sforce.ws.ConnectorConfig;

/**
 *
 * This {@code FilterChainAuthenticatedAssertionMock} is used to assert correct values in
 * {@code ForceSecurityContextHolder}, {@code SecurityContext} and {@code ThreadLocalCache}.
 *
 * @author John Simone
 */
public class FilterChainAuthenticatedAssertionMock implements FilterChain {

    @Override
    /**
     * This method is called from the authentication processing filter test and is meant to simulate the 
     * downstream servlet filter chain that would normally be called once authentication is 
     * completed and an authorized resource is being served. Since the authorized resource 
     * itself is not relevant for the test the only action here is to fire
     * assertions to ensure that the authorization context was properly created by the
     * filter.
     * 
     */
    public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        Assert.assertNotNull(ForceSecurityContextHolder.get());
        Assert.assertNotNull(ForceServiceConnector.getThreadLocalConnectorConfig());
        SecurityContext sc = ForceSecurityContextHolder.get();
        Assert.assertEquals(sc.getUserName(), SpringSecurityTestData.SFDC_USERNAME);
        Assert.assertEquals(sc.getEndPoint(), SpringSecurityTestData.SFDC_ENDPOINT);
        Assert.assertEquals(sc.getSessionId(), SpringSecurityTestData.SFDC_SESSION_ID);
        ConnectorConfig cc = ForceServiceConnector.getThreadLocalConnectorConfig();
        Assert.assertEquals(cc.getServiceEndpoint(), SpringSecurityTestData.SFDC_ENDPOINT);
        Assert.assertNotNull(cc.getSessionRenewer());
    }

}
