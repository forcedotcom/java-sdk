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

package com.force.sdk.oauth.endtoend;

import com.force.sdk.oauth.context.SecurityContextUtil;
import com.force.sdk.oauth.context.store.SecurityContextCookieStore;
import com.force.sdk.qa.util.integration.BaseSecurityIntegrationTest;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;


/**
 *
 * Tests in this class hit the salesforce core app authentication for logout.
 *
 * @author Nawab Iqbal
 */

public class LogoutEndToEndTest extends BaseSecurityIntegrationTest {

    private static String [] sdkCookies = {"force_sid", "force_ep", "security_context"};
    private static String [] forceDotComCookies = {"sid", "sid_Client", "clientSrc"};
    private WebClient webClient;

    public LogoutEndToEndTest() throws IOException {
        super();
    }

    @BeforeMethod
    public void methodSetup() throws FailingHttpStatusCodeException, IOException {
        webClient = new WebClient();
    }

    @AfterMethod(alwaysRun = true)
    public void methodTeardown() {
        webClient.closeAllWindows();
    }

    @DataProvider
    protected Object[][] logoutServlets() {
        return new Object[][] {
                {"logout", "Good bye!", "salesforce.com", true, false},
                {"logoutA", "Good bye!", "salesforce.com", false, false},
                {"logoutB", "Good bye!", "Good bye!", false, true}
        };
    }

    @Test(dataProvider = "logoutServlets")
    public void testLogout(String logoutServlet, String logoutNoLoginMessage,
                           String logoutMessage, boolean hasSecurityContext, boolean hasForceDotComCookies)
            throws FailingHttpStatusCodeException, IOException {
        HtmlPage htmlPage = webClient.getPage(appEndpoint);
        HtmlAnchor logoutAnchor = htmlPage.getAnchorByHref(logoutServlet);
        HtmlPage logoutSuccessPage = logoutAnchor.click();
        Assert.assertEquals(logoutSuccessPage.getTitleText(), logoutNoLoginMessage);

        // access porject-list page and get redirected to sfdc for login.
        htmlPage = webClient.getPage(appEndpoint + "/ProjectList");
        // redirected to project-list
        htmlPage = fillOutCredentialsAndLogin(htmlPage);

        // goto homepage
        htmlPage = htmlPage.getAnchorByHref("index.jsp").click();
        logoutSuccessPage = htmlPage.getAnchorByHref(logoutServlet).click();
        Assert.assertEquals(logoutSuccessPage.getTitleText(), logoutMessage);

        assertNoCookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT);
        assertNoCookie(SecurityContextUtil.FORCE_FORCE_SESSION);

        // A filter with session-store will NOT erase security_context cookie.
        if (hasSecurityContext) {
            assertHasCookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME);
        } else {
            assertNoCookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME);
        }

        // A filter with logoutFromDatabaseDotCom=false will NOT erase force.com cookies.
        if (hasForceDotComCookies) {
            assertHasCookies(forceDotComCookies);
        } else {
            assertNoCookies(forceDotComCookies);
        }
    }

    private void assertNoCookie(String cookieName) {
        Assert.assertNull(webClient.getCookieManager().getCookie(cookieName),
                cookieName + " cookie should have been remove.");
    }

    private void assertNoCookies(String[] cookieNames) {
        for (String cookieName : cookieNames) {
            assertNoCookie(cookieName);
        }
    }

    private void assertHasCookie(String cookieName) {
        Assert.assertNotNull(webClient.getCookieManager().getCookie(cookieName),
                cookieName + " cookie should be present.");
    }

    private void assertHasCookies(String[] cookieNames) {
        for (String cookieName : cookieNames) {
            assertHasCookie(cookieName);
        }
    }

    public HtmlPage fillOutCredentialsAndLogin(HtmlPage page) throws IOException {
       Assert.assertEquals(page.getTitleText(), "salesforce.com - Customer Secure Login Page", "unexpected page");
       HtmlForm form = page.getFormByName("login");
       HtmlSubmitInput button = form.getInputByName("Login");
       HtmlTextInput textFieldUsername = form.getInputByName("username");
       HtmlPasswordInput textFieldPassword = form.getInputByName("pw");
       textFieldUsername.setValueAttribute(username);
       textFieldPassword.setValueAttribute(password);
       return button.click();
    }
}
