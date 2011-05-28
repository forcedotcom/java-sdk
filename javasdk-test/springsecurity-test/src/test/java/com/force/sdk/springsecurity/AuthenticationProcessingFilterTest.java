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

package com.force.sdk.springsecurity;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.force.sdk.oauth.connector.ForceOAuthConnector;
import com.force.sdk.springsecurity.data.SpringSecurityTestData;

/**
 * {@code AuthenticationProcessingFilterTest} test {@code AuthenticationProcessingFilter} and verifies the
 * properties on {@code MockHttpServletRequest} and {@code MockHttpServletResponse} after doFilter.
 *
 * @author John Simone
 */
public class AuthenticationProcessingFilterTest {

    private AuthenticationProcessingFilter filter;
    
    @BeforeClass
    public void init() {
        ClassPathResource resource = new ClassPathResource("security-config-authProcessingFilterTest.xml");
        BeanFactory factory = new XmlBeanFactory(resource);
        filter = (AuthenticationProcessingFilter) factory.getBean("authenticationFilter");
        
    }

    @Test
    public void testDoFilterForLoginUrlWithReferer() {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;
        FilterChain filterChain = null;
                
        //Request for filter processing url with a referer
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.addHeader("Referer", SpringSecurityTestData.REFERER_URL);
        request.setRequestURI(SpringSecurityTestData.FILTER_PROCESSING_URL);
        
        try {
            filter.doFilter(request, response, filterChain);
            Assert.assertEquals(response.getRedirectedUrl(), SpringSecurityTestData.REDIRECT_URL,
                                    "Redirect URL not set properly.");
            Assert.assertEquals(request.getAttribute(ForceOAuthConnector.LOGIN_REDIRECT_URL_ATTRIBUTE),
                                    SpringSecurityTestData.REFERER_URL, "Referer URL not set properly.");
        } catch (IOException e) {
            Assert.fail("IOException thrown in AuthenticationProcessingFilter.doFilter()");
        } catch (ServletException e) {
            Assert.fail("ServletException thrown in AuthenticationProcessingFilter.doFilter()");
        }
    }
    
    @Test
    public void testDoFilterForLoginUrlNoReferer() {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;
        FilterChain filterChain = null;
        
        //Request for filter processing url without a referer
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRequestURI(SpringSecurityTestData.FILTER_PROCESSING_URL);
        
        try {
            filter.doFilter(request, response, filterChain);
            Assert.assertEquals(response.getRedirectedUrl(), SpringSecurityTestData.REDIRECT_URL,
                                    "Redirect URL not set properly.");
            Assert.assertEquals(request.getAttribute(ForceOAuthConnector.LOGIN_REDIRECT_URL_ATTRIBUTE), "",
                                    "Referer URL should be blank if one is not used.");
        } catch (IOException e) {
            Assert.fail("IOException thrown in AuthenticationProcessingFilter.doFilter()");
        } catch (ServletException e) {
            Assert.fail("ServletException thrown in AuthenticationProcessingFilter.doFilter()");
        }
    }
    
    @Test
    public void testDoFilterForLoginUrlWithContext() {
        MockHttpServletRequest request = null;
        MockHttpServletResponse response = null;
        FilterChain filterChain = null;
        
        //Request for filter processing url without a referer, with a context path
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setRequestURI(SpringSecurityTestData.CONTEXT_PATH + SpringSecurityTestData.FILTER_PROCESSING_URL);
        request.setContextPath(SpringSecurityTestData.CONTEXT_PATH);
        
        try {
            filter.doFilter(request, response, filterChain);
            Assert.assertEquals(response.getRedirectedUrl(), SpringSecurityTestData.REDIRECT_URL,
                                    "Redirect URL not set properly.");
            Assert.assertEquals(request.getAttribute(ForceOAuthConnector.LOGIN_REDIRECT_URL_ATTRIBUTE), "",
                                    "Referer URL should be blank if one is not used.");
        } catch (IOException e) {
            Assert.fail("IOException thrown in AuthenticationProcessingFilter.doFilter()");
        } catch (ServletException e) {
            Assert.fail("ServletException thrown in AuthenticationProcessingFilter.doFilter()");
        }

    }

}
