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

package com.force.sdk.oauth;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.force.sdk.connector.ForceConnectorUtils;
import com.force.sdk.oauth.context.ForceSecurityContext;
import com.force.sdk.oauth.context.SecurityContext;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import mockit.*;

/**
 * This is the base class to be used for security tests that need to mock the partner API connection.
 * 
 * @author John Simone
 */
public class BaseMockedPartnerConnectionTest extends BaseOAuthTest {
    
    public static final String VALID_SFDC_INSTANCEURL = "https://test.url.login.com";
    public static final String VALID_SFDC_ENDPOINT = ForceConnectorUtils.buildForceApiEndpoint(VALID_SFDC_INSTANCEURL);
    public static final String VALID_SFDC_SID = "thisisavalidsfdcsessionid";
    public static final String INVALID_SFDC_SID = "thisisaninvalidsfdcsessionid";
    public static final String CONSUMER_KEY = "key";
    public static final String CONSUMER_SECRET = "12345";
    
    //these are the starting values. Each test will start this a security context
    //containing these values in its session.
    public static final String ORIGINAL_ORG_ID = "org_id";
    public static final String ORIGINAL_USER_ID = "user_id";
    public static final String ORIGINAL_USER_NAME = "user_name";
    public static final String ORIGINAL_USER_LANGUAGE = "user_language";
    public static final String ORIGINAL_USER_LOCALE = "US";
    public static final String ORIGINAL_USER_TIMEZONE = "GMT";
    
    //these are the values that are returned from our mocked partner connection
    //this is how we'll figure out whether the partner api was called to refresh data or not.
    public static final String PARTNER_CONN_ORG_ID = "partner_org_id";
    public static final String PARTNER_CONN_USER_ID = "partner_user_id";
    public static final String PARTNER_CONN_USER_NAME = "partner_user_name";
    public static final String PARTNER_CONN_USER_LANGUAGE = "partner_user_language";
    public static final String PARTNER_CONN_USER_LOCALE = "US";
    public static final String PARTNER_CONN_USER_TIMEZONE = "GMT";
    
    protected SecurityContext originalSc = null;
    protected SecurityContext partnerSc = null;
    
    /**
* These mock classes allow us to simulate the calls to the partner api
* without actually making them.
*
* @author John Simone
*/
    @MockClass(realClass = Connector.class, instantiation = Instantiation.PerMockSetup)
    protected static final class MockConnector {
        
        private MockConnector() { }
        
        @Mock
        public static PartnerConnection newConnection(ConnectorConfig config)
            throws ConnectionException {
            config = new ConnectorConfig();
            config.setManualLogin(true);
            
            // Just return a non-null PartnerConnection
            return new PartnerConnection(config);
        }
    }

    /**
* These mock classes allow us to simulate the calls to the partner api
* without actually making them.
*
* @author John Simone
*/
    @MockClass(realClass = PartnerConnection.class, instantiation = Instantiation.PerMockSetup)
    protected static class MockQueryPartnerConnection {
        
        @Mock
        public GetUserInfoResult getUserInfo()
            throws com.sforce.ws.ConnectionException {
            
            GetUserInfoResult userInfo = new GetUserInfoResult();
            
            userInfo.setOrganizationId(PARTNER_CONN_ORG_ID);
            userInfo.setUserId(PARTNER_CONN_USER_ID);
            userInfo.setUserName(PARTNER_CONN_USER_NAME);
            userInfo.setUserLanguage(PARTNER_CONN_USER_LANGUAGE);
            userInfo.setUserLocale(PARTNER_CONN_USER_LOCALE);
            userInfo.setUserTimeZone(PARTNER_CONN_USER_TIMEZONE);
            
            return userInfo;
        }
        
        @Mock
        public SObject[] retrieve(String fieldList, String sObjectType, String[] ids)
            throws com.sforce.ws.ConnectionException {
            
            SObject profile = new SObject();
            profile.setField("Name", "ROLE_USER");
            
            return new SObject[] {profile};
            
        }
    }
    
    @BeforeClass
    public void beforeClass() {
        //register jMockit mocks
        Mockit.setUpMocks(
                BaseMockedPartnerConnectionTest.MockConnector.class,
                BaseMockedPartnerConnectionTest.MockQueryPartnerConnection.class);
        
        originalSc = new ForceSecurityContext();
        
        originalSc.setEndPoint(VALID_SFDC_ENDPOINT);
        originalSc.setSessionId(VALID_SFDC_SID);
        originalSc.setOrgId(ORIGINAL_ORG_ID);
        originalSc.setUserId(ORIGINAL_USER_ID);
        originalSc.setUserName(ORIGINAL_USER_NAME);
        originalSc.setLanguage(ORIGINAL_USER_LANGUAGE);
        
        partnerSc = new ForceSecurityContext();
        
        partnerSc.setEndPoint(VALID_SFDC_ENDPOINT);
        partnerSc.setSessionId(VALID_SFDC_SID);
        partnerSc.setOrgId(PARTNER_CONN_ORG_ID);
        partnerSc.setUserId(PARTNER_CONN_USER_ID);
        partnerSc.setUserName(PARTNER_CONN_USER_NAME);
        partnerSc.setLanguage(PARTNER_CONN_USER_LANGUAGE);
    }
    
    @AfterClass
    public void afterClass() {
        Mockit.tearDownMocks(PartnerConnection.class, Connector.class);
    }
    
    /**
* Do a deep compare of the two security context instances while asserting each value.
*
* @param expectedSc
* @param actualSc
* @param message
*/
    protected void assertSecurityContextsAreEqual(SecurityContext expectedSc, SecurityContext actualSc, String message) {
        
        Assert.assertEquals(actualSc.getEndPoint(), expectedSc.getEndPoint(), message + " - field: endpoint - ");
        Assert.assertEquals(actualSc.getSessionId(), expectedSc.getSessionId(), message + " - field: session id - ");
        Assert.assertEquals(actualSc.getUserId(), expectedSc.getUserId(), message + " - field: user id - ");
        Assert.assertEquals(actualSc.getUserName(), expectedSc.getUserName(), message + " - field: user name - ");
        Assert.assertEquals(actualSc.getOrgId(), expectedSc.getOrgId(), message + " - field: org id - ");
        Assert.assertEquals(actualSc.getLanguage(), expectedSc.getLanguage(), message + " - field: language - ");
        
    }
}
