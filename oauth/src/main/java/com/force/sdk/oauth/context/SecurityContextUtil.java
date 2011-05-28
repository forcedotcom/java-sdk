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

package com.force.sdk.oauth.context;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.*;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * This utility class is in place to handle all interaction with the browser cookies
 * that will be used to track a user's authentication status. This will include the logic
 * required to refresh the security context if it isn't available in the server side session.
 * 
 * @author John Simone
 *
 */
public final class SecurityContextUtil {

    /**
     * Constant that defines the name of the session id cookie.
     */
    public static final String FORCE_FORCE_SESSION = "force_sid";
    /**
     * Constant that defines the name of the endpoint cookie.
     */
    public static final String FORCE_FORCE_ENDPOINT = "force_ep";
    /**
     * Constant that defines the default role which will be used if no role is available.
     */
    public static final String DEFAULT_ROLE = "ROLE_USER";
    
    private SecurityContextUtil() {  }
    
    /**
     * Get the session id and endpoint out of cookies.
     * 
     * @param request HttpServletRequest
     * @return Map<String, String> - cookie name, cookie value
     */
    public static Map<String, String> getCookieValues(HttpServletRequest request) {
        HashMap<String, String> cookieValueMap = new HashMap<String, String>();
        int totalCookieCount = 2;
        
        //get the session id and endpoint from cookies
        if (request.getCookies() != null) {
            int count = 0;
            for (Cookie cookie : request.getCookies()) {
                if (FORCE_FORCE_SESSION.equals(cookie.getName())) {
                    cookieValueMap.put(FORCE_FORCE_SESSION, cookie.getValue());
                    if (++count == totalCookieCount) break;
                } else if (FORCE_FORCE_ENDPOINT.equals(cookie.getName())) {
                    cookieValueMap.put(FORCE_FORCE_ENDPOINT, cookie.getValue());
                    if (++count == totalCookieCount) break;
                }
            }
        }
        
        return cookieValueMap;
    }
    
    /**
     * Set the session id and endpoint from the security context into cookies.
     * 
     * @param sc SecurityContext
     * @param response HttpServletResponse
     * @param secure Whether or not the cookie should be secure
     */
    public static void setCookieValues(SecurityContext sc, HttpServletResponse response, boolean secure) {
        
        Map<String, String> cookieValueMap = new HashMap<String, String>();
        cookieValueMap.put(FORCE_FORCE_SESSION, sc.getSessionId());
        cookieValueMap.put(FORCE_FORCE_ENDPOINT, sc.getEndPoint());
        setCookieValues(cookieValueMap, response, secure);
        
    }
    
    /**
     * Set the map of cookie names and values into cookies on the response.
     * 
     * @param cookieValueMap Map<String, String> - cookie name, cookie value
     * @param response HttpServletResponse
     * @param secure Whether or not the cookie should be secure
     */
    public static void setCookieValues(Map<String, String> cookieValueMap, HttpServletResponse response, boolean secure) {
        
        for (Map.Entry<String, String> cookieEntry : cookieValueMap.entrySet()) {
            Cookie cookie = new Cookie(cookieEntry.getKey(), cookieEntry.getValue());
            cookie.setSecure(secure);
            response.addCookie(cookie);
        }
        
    }
    
    /**
     * Clear the endpoint and session cookies.
     * 
     * @param response HttpServletResponse
     */
    public static void clearCookieValues(HttpServletResponse response) {
        Cookie endPointClearCookie = new Cookie(FORCE_FORCE_ENDPOINT, "");
        Cookie sessionIdClearCookie = new Cookie(FORCE_FORCE_SESSION, "");
        endPointClearCookie.setMaxAge(0);
        sessionIdClearCookie.setMaxAge(0);
        response.addCookie(endPointClearCookie);
        response.addCookie(sessionIdClearCookie);
    }
    
    /**
     * Initializes the user information into the security context via a call to
     * the partner API.
     * 
     * @param securityContext securityContext
     * @throws ConnectionException ConnectionException
     */
    public static void initializeSecurityContextFromApi(SecurityContext securityContext)
        throws ConnectionException {
        ConnectorConfig config = new ConnectorConfig();
        config.setServiceEndpoint(securityContext.getEndPoint());
        config.setSessionId(securityContext.getSessionId());
        
        GetUserInfoResult userInfoResult = Connector.newConnection(config).getUserInfo();
        
        securityContext.init(userInfoResult);
        
        SObject[] results = Connector.newConnection(config).retrieve("Name", "Profile",
                                                                new String[] {userInfoResult.getProfileId()});
        
        String role = null;
        if (results != null && results.length > 0) {
            SObject result = results[0];
            role = (String) result.getField("Name");
            
            if (role.isEmpty()) {
                role = DEFAULT_ROLE;
            }
        } else {
            role = DEFAULT_ROLE;
        }
        
        securityContext.setRole(role);
    }

}
