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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.force.sdk.connector.ForceServiceConnector;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Controller for all of the pages in this application.  The different pages are used by the 
 * implementation of spring security in this application.
 *
 * @author Jeff Lai
 *
 */
@Controller
public class TestController {


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
     * @throws ConnectionException if an error occurs while connecting to the Force.com store (organization).
     */
    @RequestMapping("secured_page.html")
    public ModelAndView securedPage() throws ConnectionException {
        return createSecuredPageModel();
    }
    
    /**
     * Controller method for secured_page.html.
     * @return new ModelAndView object
     * @throws ConnectionException if an error occurs while connecting to the Force.com store (organization).
     */
    @RequestMapping("secured_page_no_session.html")
    public ModelAndView securedPageNoSession() throws ConnectionException {
        return createSecuredPageModel();
    }
    
    /**
     * Create the model and view that both secured pages will use
     * @return new ModelAndView object for secured pages
     */
    private ModelAndView createSecuredPageModel() throws ConnectionException{
        ModelAndView mav = new ModelAndView();

        // This will instantiate a ForceSeviceConnector with the given connectionName.
        // ForceServiceConnector f = new ForceServiceConnector("integrationserver");

        // This will use ForceServiceConnector assigned to the ThreadLocal.
        ForceServiceConnector f = new ForceServiceConnector();
        PartnerConnection conn = f.getConnection();


        GetUserInfoResult userInfoResult = conn.getUserInfo();

        StringBuffer value = new StringBuffer();
        value.append("[");
        value.append("{" + userInfoResult.getUserName() + "},");
        value.append("]");
        mav.addObject("userinfo", userInfoResult.getUserName());
        mav.addObject("moreinfo", value.toString());

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
