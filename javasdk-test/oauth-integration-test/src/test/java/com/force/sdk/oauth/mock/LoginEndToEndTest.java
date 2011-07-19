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

import com.force.sdk.qa.util.BaseSecurityIntegrationTest;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * Integration tests that verify redirected URL after OAuth handshake.
 *
 * @author Nawab Iqbal
 */
public class LoginEndToEndTest extends BaseSecurityIntegrationTest  {

    private WebClient webClient;

    @BeforeMethod
    public void methodSetup() throws FailingHttpStatusCodeException, IOException {
        webClient = new WebClient();
    }

    @AfterMethod(alwaysRun = true)
    public void methodTeardown() {
        webClient.closeAllWindows();
    }

    @Test
    public void testLoginRedirectToTargetUrl() throws IOException {
        HtmlPage page = webClient.getPage(appEndpoint + "/ProjectList");
        String ep = webClient.getCookieManager().getCookie("force_ep").getValue();
        Assert.assertTrue(URLDecoder.decode(ep, "UTF-8").contains("services/Soap/u/"));
        Assert.assertEquals(page.getTitleText(), "Project List");
    }

    @Test
    public void testLoginRedirectToHome() throws IOException {
        HtmlPage page = webClient.getPage(appEndpoint);
        Assert.assertEquals(page.getTitleText(), "Home");
    }
}
