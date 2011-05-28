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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


/**
 * 
 * Tests in this class hit the salesforce core app authentication for logout.
 *
 * @author Jeff Lai
 */
public class LogoutEndToEndTest extends BaseEndToEndTest  {
    
    public LogoutEndToEndTest() throws IOException {
        super();
    }

    @Override
    @BeforeMethod
    public void methodSetup() throws FailingHttpStatusCodeException, IOException {
        super.methodSetup();
        HtmlPage loginPage = getWebClient().getPage(appEndpoint + "/login");
        fillOutCredentialsAndLogin(loginPage);
    }
    
    @Test
    public void testLogoutRedirectToDefaultTargetUrl() throws FailingHttpStatusCodeException, IOException {
        HtmlPage redirectPage = getWebClient().getPage(appEndpoint + "/logout");
        Assert.assertEquals(redirectPage.getTitleText(), "Logout success");
        Assert.assertEquals(redirectPage.getUrl().toString(), appEndpoint + "/logout_success.html");
    }
    
    @Test
    public void testLoginRedirectToUrl() throws FailingHttpStatusCodeException, IOException {
        HtmlPage pageWithLogoutLink = getWebClient().getPage(appEndpoint + "/page_with_logout_link.html");
        HtmlAnchor logoutLink = pageWithLogoutLink.getAnchorByText("logout");
        HtmlPage redirectPage = logoutLink.click();
        Assert.assertEquals(redirectPage.getTitleText(), "Logout success");
        Assert.assertEquals(redirectPage.getUrl().toString(), appEndpoint + "/logout_success.html");
    }

}
