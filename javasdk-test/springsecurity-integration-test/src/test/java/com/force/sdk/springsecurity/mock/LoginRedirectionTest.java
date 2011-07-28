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

import com.force.sdk.qa.util.integration.BaseSecurityIntegrationTest;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Integration tests that verify redirected URL after OAuth handshake.
 * 
 * @author Jeff Lai
 */
public class LoginRedirectionTest extends BaseSecurityIntegrationTest {

    private HttpClient httpClient;
    
    @BeforeMethod
    public void methodSetup() {
        httpClient = new DefaultHttpClient();
    }
    
    @AfterMethod(alwaysRun = true)
    public void methodTeardown() {
        httpClient.getConnectionManager().shutdown();
    }
    
    @Test
    public void testLoginRedirectToSfdcDefaultUrl() throws ParseException, IOException {
        HttpParams params = new BasicHttpParams();
        params.setParameter("http.protocol.handle-redirects", false);
        HttpGet get = new HttpGet(appEndpoint + "/login");
        get.setParams(params);
        HttpResponse response = httpClient.execute(get);
        assertResponseStatus(response, 302, "Moved Temporarily");
        String location = response.getFirstHeader("Location").getValue();
        assertLoginRedirectSfdcLocationHeaderValue(location, mockSfdcEndpoint, mockOauthKey, appEndpoint + "/_auth", "");
    }
    
    @Test
    public void testLoginRedirectToSfdcRefererUrl() throws IOException {
        HttpParams params = new BasicHttpParams();
        params.setParameter("http.protocol.handle-redirects", false);
        HttpGet get = new HttpGet(appEndpoint + "/login");
        get.setParams(params);
        get.addHeader("Referer", appEndpoint + "/page_with_login_link.html");
        HttpResponse response = httpClient.execute(get);
        assertResponseStatus(response, 302, "Moved Temporarily");
        String location = response.getFirstHeader("Location").getValue();
        assertLoginRedirectSfdcLocationHeaderValue(location, mockSfdcEndpoint, mockOauthKey, appEndpoint + "/_auth",
                                                    appEndpoint + "/page_with_login_link.html");
    }
    
    @Test
    public void testLoginRedirectToSfdcRefererUrlAfterSessionExpiry() throws IOException {
        HttpParams params = new BasicHttpParams();
        params.setParameter("http.protocol.handle-redirects", false);
        HttpGet get = new HttpGet(appEndpoint + "/login");
        get.setParams(params);
        get.addHeader("Referer", appEndpoint + "/page_with_login_link.html");
        HttpResponse response = httpClient.execute(get);
        assertResponseStatus(response, 302, "Moved Temporarily");
        String location = response.getFirstHeader("Location").getValue();
        assertLoginRedirectSfdcLocationHeaderValue(location, mockSfdcEndpoint, mockOauthKey, appEndpoint + "/_auth",
                                                    appEndpoint + "/page_with_login_link.html");
    }

    // TODO W-951321 this test is currently failing.  The mock oauth server is not working properly with the spring security app
    @Test(enabled = false)
    public void testLoginRedirectToAppRefererUrl() throws IOException {
        HttpParams params = new BasicHttpParams();
        params.setParameter("http.protocol.handle-redirects", false);
        HttpGet get = new HttpGet(appEndpoint + "/_auth?code=" + mockAuthCode
                                    + "&state=" + URLEncoder.encode(appEndpoint + "/page_with_login_link.html", "UTF-8"));
        get.setParams(params);
        get.addHeader("Referer", "http://" + mockSfdcEndpoint + "/setup/secur/RemoteAccessAuthorizationPage.apexp?source=00");
        get.addHeader("Host", "localhost:8888");
        HttpResponse response = httpClient.execute(get);
        
        assertResponseStatus(response, 302, "Moved Temporarily");
    }
    
    /**
     * Verifies that the login redirect to sfdc location header is correct.
     * 
     * @param location is the location header from the redirect http response
     * @param expectedSfdcEndpoint is the salesforce endpoint
     * @param expectedOauthKey is the oauth key
     * @param expectedCallbackUrl should end in /_auth
     * @param expectedAppRedirectUrl is where the browser is redirected to after the callback url is loaded
     * @throws UnsupportedEncodingException 
     */
    private void assertLoginRedirectSfdcLocationHeaderValue(String location, String expectedSfdcEndpoint,
            String expectedOauthKey, String expectedCallbackUrl, String expectedAppRedirectUrl)
    throws UnsupportedEncodingException {
        Assert.assertTrue(location.contains(expectedSfdcEndpoint + "/services/oauth2/authorize"),
                            "location does not contain sfdc auth endpoint");
        Map<String, String> params = convertUrlParamsToMap(location);
        Assert.assertEquals(params.get("response_type"), "code", "unexpected response type");
        Assert.assertEquals(params.get("client_id"), expectedOauthKey, "unexpected oauth key");
        Assert.assertEquals(URLDecoder.decode(params.get("redirect_uri"), "UTF-8"), expectedCallbackUrl,
                                "unexpected call back url");
        Assert.assertEquals(URLDecoder.decode(params.get("state"), "UTF-8"), expectedAppRedirectUrl,
                                "unexpected app redirect url");
    }
    
}
