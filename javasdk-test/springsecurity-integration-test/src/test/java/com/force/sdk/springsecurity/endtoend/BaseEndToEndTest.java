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

import com.force.sdk.qa.util.integration.BaseSecurityIntegrationTest;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * 
 * This is the base class for End To End Spring Security integration tests that hit the salesforce core app for authentication.
 *
 * @author Jeff Lai
 * 
 */
public abstract class BaseEndToEndTest extends BaseSecurityIntegrationTest {

    private WebClient webClient;
    
    @BeforeMethod
    public void methodSetup() throws FailingHttpStatusCodeException, IOException {
        webClient = new WebClient();
    }
    
    @AfterMethod(alwaysRun = true)
    public void methodTeardown() {
        webClient.closeAllWindows();
    }
    
    public WebClient getWebClient() {
        return webClient;
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
