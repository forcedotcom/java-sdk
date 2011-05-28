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

package com.force.sdk.springsecurity.endtoend;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * 
 * Tests in this class hit the salesforce core app authentication for login.
 *
 * @author Jeff Lai
 */
public class LoginEndToEndTest extends BaseEndToEndTest {
    
    public LoginEndToEndTest() throws IOException {
        super();
    }

    @Test
    public void testLoginRedirectToDefaultTargetUrl() throws FailingHttpStatusCodeException, IOException {
        HtmlPage loginPage = getWebClient().getPage(appEndpoint + "/login");
        HtmlPage redirectPage = fillOutCredentialsAndLogin(loginPage);
        Assert.assertEquals(redirectPage.getTitleText(), "Login success");
        Assert.assertEquals(redirectPage.getUrl().toString(), appEndpoint + "/login_success.html");
    }
    
    @Test
    public void testLoginRedirectToUrl() throws FailingHttpStatusCodeException, IOException {
        HtmlPage pageWithLoginLink = getWebClient().getPage(appEndpoint + "/page_with_login_link.html");
        HtmlAnchor loginLink = pageWithLoginLink.getAnchorByText("login");
        HtmlPage loginPage = loginLink.click();
        HtmlPage redirectPage = fillOutCredentialsAndLogin(loginPage);
        Assert.assertEquals(redirectPage.getTitleText(), "Page with login link");
        Assert.assertEquals(redirectPage.getUrl().toString(), appEndpoint + "/page_with_login_link.html");
    }
    
    @Test
    public void testSecuredPage() throws FailingHttpStatusCodeException, IOException {
        HtmlPage loginPage = getWebClient().getPage(appEndpoint + "/login");
        fillOutCredentialsAndLogin(loginPage);
        // note when we try to access the secured page we are already logged in because HtmlUnit goes into an infinite loop 
        // while trying to execute javascript if you try to access the secured page and then login
        HtmlPage securedPage = getWebClient().getPage(appEndpoint + "/secured_page.html");
        Assert.assertEquals(securedPage.getTitleText(), "Secured page");
        Assert.assertEquals(securedPage.getUrl().toString(), appEndpoint + "/secured_page.html");
    }
    
    @Test
    public void testLoginRedirectToSecuredPage() throws FailingHttpStatusCodeException, IOException {
        HtmlPage securedPage = getWebClient().getPage(appEndpoint + "/secured_page.html");
        Assert.assertEquals(securedPage.getTitleText(), "salesforce.com - Customer Secure Login Page");
    }
    
}
