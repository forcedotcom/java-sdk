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

package com.force.sample.springsecurity;

import com.force.sdk.oauth.context.SecurityContextUtil;
import mockit.Mockit;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Controller for all of the pages in this application.  The different pages are used by the 
 * implementation of spring security in this application.
 *
 * @author Jeff Lai
 * @author Nawab Iqbal
 *
 */
@Controller
public class TestController {
    /**
     * springsecurity-integration tests will set mockapi=true; when mock server is used.
     * For using mock server, while running from command-line, set -Dmockapi=true
     */
    static {
        System.out.println(" ------------------------------------------------------------------------");
        System.out.println("mockapi: " + System.getProperty("mockapi"));
        if (Boolean.getBoolean("mockapi")) {
            System.out.println("Mock has been setup.");
            Mockit.setUpMock(SecurityContextUtil.class, MockSecurityContextUtil.class);
        }
        System.out.println(" ------------------------------------------------------------------------");
    }

    /**
     * Controller method for page_with_login_link.html.
     * @return new ModelAndView object
     */
    @RequestMapping("page_with_login_link.html")
    public ModelAndView pageWithLoginLink() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }

    /**
     * Controller method for page_with_logout_link.html.
     * @return new ModelAndView object
     */
    @RequestMapping("page_with_logout_link.html")
    public ModelAndView pageWithLogoutLink() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }
    
    /**
     * Controller method for secured_page.html.
     * @return new ModelAndView object
     */
    @RequestMapping("secured_page.html")
    public ModelAndView securedPage() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }
    
    /**
     * Controller method for login_success.html.
     * @return new ModelAndView object
     */  
    @RequestMapping("login_success.html")
    public ModelAndView loginSuccess() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }
    
    /**
     * Controller method for logout_success.html.
     * @return new ModelAndView object
     */  
    @RequestMapping("logout_success.html")
    public ModelAndView logoutSuccess() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }

}
