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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.oauth.context.SecurityContextUtil;
import com.force.sdk.oauth.context.store.ForceEncryptionException;
import com.force.sdk.oauth.context.store.SecurityContextCookieStore;
import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

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

    @DataProvider
    protected Object[][] securedPageProvider() {
        
        return new Object[][] {
                {"/secured_page.html"},
                {"/secured_page_no_session.html"}
           };
    }

    
    @Test(dataProvider = "securedPageProvider")
    public void testSecuredPage(String securePageLocation)
            throws FailingHttpStatusCodeException, IOException, ForceEncryptionException, ClassNotFoundException {

        getWebClient().setRedirectEnabled(false);
        HtmlPage loginPage = null;
        try {
            loginPage = getWebClient().getPage(appEndpoint + "/login");
            Assert.fail("Login page should redirect.");
        } catch (FailingHttpStatusCodeException e) {
            Assert.assertEquals(e.getResponse().getStatusCode(), 302, "Redirect code 302 was expected.");
        }

        // enabling redirection so that login page redirects to SFDC and we can logon.
        getWebClient().setRedirectEnabled(true);
        loginPage = getWebClient().getPage(appEndpoint + "/login");
        fillOutCredentialsAndLogin(loginPage);

        // note when we try to access the secured page we are already logged in because HtmlUnit goes into an infinite loop 
        // while trying to execute javascript if you try to access the secured page and then login
        HtmlPage securedPage = getWebClient().getPage(appEndpoint + securePageLocation);

        Assert.assertEquals(securedPage.getTitleText(), "Secured page");
        Assert.assertEquals(securedPage.getUrl().toString(), appEndpoint + securePageLocation);
    }
    
    @Test(dataProvider = "securedPageProvider")
    public void testSecuredPageCookieDeletion(String securePageLocation)
            throws FailingHttpStatusCodeException, IOException, ForceEncryptionException, ClassNotFoundException {

        // enabling redirection so that login page redirects to SFDC and we can logon.
        getWebClient().setRedirectEnabled(true);
        HtmlPage loginPage = getWebClient().getPage(appEndpoint + "/login");
        fillOutCredentialsAndLogin(loginPage);
        
        //remove the sdk cookies. This should effectively log the user out.
        removeSdkCookies();

        //attempt the get the secured page and ensure that we are redirected
        getWebClient().setRedirectEnabled(false);
        try {
            getWebClient().getPage(appEndpoint + securePageLocation);
            Assert.fail("Login page should redirect.");
        } catch (FailingHttpStatusCodeException e) {
            Assert.assertEquals(e.getResponse().getStatusCode(), 302, "Redirect code 302 was expected.");
        }
    }

    @Test(dataProvider = "securedPageProvider")
    public void testSecuredPageCookieCorruption(String securePageLocation)
            throws FailingHttpStatusCodeException, IOException, ForceEncryptionException, ClassNotFoundException {

        // enabling redirection so that login page redirects to SFDC and we can logon.
        getWebClient().setRedirectEnabled(true);
        HtmlPage loginPage = getWebClient().getPage(appEndpoint + "/login");
        fillOutCredentialsAndLogin(loginPage);
        
        //corrupt the security context cookie
        corruptSecurityContextCookie();

        HtmlPage securedPage = getWebClient().getPage(appEndpoint + securePageLocation);

        Assert.assertEquals(securedPage.getTitleText(), "Secured page");
        Assert.assertEquals(securedPage.getUrl().toString(), appEndpoint + securePageLocation);
    }
    
    /**
     * Remove the sdk cookies from the client so that we can test that login is forced.
     */
    private void removeSdkCookies() {
        CookieManager cookieManager = getWebClient().getCookieManager();
        Cookie securityContextCookie =
            cookieManager.getCookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME);
        Cookie endpointCookie = cookieManager.getCookie(SecurityContextUtil.FORCE_FORCE_ENDPOINT);
        Cookie sidCookie = cookieManager.getCookie(SecurityContextUtil.FORCE_FORCE_SESSION);
        
        cookieManager.removeCookie(securityContextCookie);
        cookieManager.removeCookie(endpointCookie);
        cookieManager.removeCookie(sidCookie);
    }
    
    private void corruptSecurityContextCookie() {
        CookieManager cookieManager = getWebClient().getCookieManager();
        Cookie securityContextCookie =
            cookieManager.getCookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME);

        cookieManager.removeCookie(securityContextCookie);

        securityContextCookie =
            new Cookie(SecurityContextCookieStore.SECURITY_CONTEXT_COOKIE_NAME,
                    securityContextCookie.getValue().substring(5, 30));
        
        cookieManager.addCookie(securityContextCookie);
    }

    @Test
    public void testLoginRedirectToSecuredPage() throws FailingHttpStatusCodeException, IOException {
        HtmlPage securedPage = getWebClient().getPage(appEndpoint + "/secured_page.html");
        Assert.assertEquals(securedPage.getTitleText(), "salesforce.com - Customer Secure Login Page");
    }
    
}
