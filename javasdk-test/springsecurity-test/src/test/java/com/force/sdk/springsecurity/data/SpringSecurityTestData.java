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

package com.force.sdk.springsecurity.data;

/**
 *
 * This class has the constants used for testing oauth and spring security flows.
 *
 * @author Fiaz Hossain
 * @author John Simone
 */
public final class SpringSecurityTestData {
    
    private SpringSecurityTestData() {  }
    
    public static final String FILTER_PROCESSING_URL = "login_url";
    public static final String REDIRECT_URL = "redirect_url";
    public static final String REFERER_URL = "referer_url";
    public static final String CONTEXT_PATH = "context_path/";
    public static final String OAUTH_CALLBACK_URL = "/_auth";
    public static final String SECURE_URL = "/secure.htm";
    
    public static final String OAUTH_ACCESS_CODE = "oauth_access_code";
    public static final String OAUTH_REDIRECT_URI = "oauth_redirect_uri";
    public static final String OAUTH_REFRESH_TOKEN = "oauth_refresh_token";
    public static final String SFDC_SESSION_ID = "sfdc_session_id";
    public static final String SFDC_SESSION_ID_2 = "sfdc_session_id_2";
    public static final String SFDC_ENDPOINT = "sfdc_endpoint";
    public static final String SFDC_USERNAME = "sfdc_username";
    public static final String DEFAULT_ROLE = "ROLE_USER";

}
