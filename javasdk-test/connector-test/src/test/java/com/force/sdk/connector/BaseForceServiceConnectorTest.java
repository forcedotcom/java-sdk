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

package com.force.sdk.connector;

import static com.force.sdk.connector.ForceServiceConnector.DESCRIBE_METADATA_VERSION;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.*;

import com.force.sdk.test.util.PropsUtil;
import com.force.sdk.test.util.UserInfo;
import com.sforce.async.*;
import com.sforce.soap.metadata.DescribeMetadataResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

/**
 * Base class for ForceServiceConnector functional tests.
 *
 * @author Tim Kral
 */
public abstract class BaseForceServiceConnectorTest {
    
    private String testOrgNamespace;
    
    protected UserInfo userInfo;
    
    @BeforeClass
    public void classSetUp() throws Exception {
        
        testOrgNamespace = System.getProperty("force.namespace");
        if (testOrgNamespace == null) {
            // Try to load the namespace from the force-sdk-test.properties file on the classpath
            testOrgNamespace = PropsUtil.load(PropsUtil.FORCE_SDK_TEST_PROPS).getProperty("force.namespace");
        }
        
        // Mimic namespace processing in ForceServiceConnector.getNamespace
        if (testOrgNamespace != null && testOrgNamespace.length() == 0) {
            testOrgNamespace = null;
        }
        
        
        // We will use this to create connectors and verify their connections are valid
        userInfo = UserInfo.loadFromPropertyFile(PropsUtil.FORCE_SDK_TEST_NAME);
    }
    
    @BeforeMethod
    public void methodSetUp() {
        // Make sure each test has a clean cache
        ForceServiceConnector.clearCache();
    }
    
    // See testGetConnectionFromPropertyFile in ForceConnectorConstructorTest.
    // and ForceConnectorSetterTest
    @DataProvider
    protected Object[][] propertyFileConnNameProvider() {
        // Test property files defined in /src/test/resources 
        return new Object[][] {
            {"funcconnurl"},
            {"funcconnuserinfo"},
        };
    }
    
    protected ForceConnectorConfig createConfig() {
        ForceConnectorConfig config = new ForceConnectorConfig();
        config.setAuthEndpoint(userInfo.getServerEndpoint());
        config.setUsername(userInfo.getUserName());
        config.setPassword(userInfo.getPassword());
        
        return config;
    }
    
    protected String createConnectionUrl() {
        // Strip out protocol, if it exists
        String[] parsedEndPoint = userInfo.getServerEndpoint().split("://");
        
        StringBuffer sb = new StringBuffer();
        sb.append("force://").append(parsedEndPoint[parsedEndPoint.length - 1])
            .append(";user=").append(userInfo.getUserName())
            .append(";password=").append(userInfo.getPassword());
        
        return sb.toString();
    }
    
    protected void verifyConnection(PartnerConnection conn) throws ConnectionException {
        GetUserInfoResult userInfoResult = conn.getUserInfo();
        assertNotNull(userInfoResult);
        
        assertEquals(userInfoResult.getOrganizationId(), userInfo.getOrgId());
        assertEquals(userInfoResult.getUserId(), userInfo.getUserId());
        assertEquals(userInfoResult.getUserName(), userInfo.getUserName());
    }
    
    protected void verifyConnection(MetadataConnection conn) throws ConnectionException {
        DescribeMetadataResult descMetadataResult = conn.describeMetadata(DESCRIBE_METADATA_VERSION);
        assertNotNull(descMetadataResult);
        
        // Mimic namespace processing in ForceServiceConnector.getNamespace
        String namespace = descMetadataResult.getOrganizationNamespace();
        verifyNamespace(namespace != null && namespace.length() > 0 ? namespace : null);
    }
    
    protected void verifyConnection(BulkConnection conn) throws AsyncApiException, ConnectionException {
        try {
            // Create an invalid JobInfo
            conn.createJob(new JobInfo());
        } catch (AsyncApiException expected) {
            // We'll expect the job to fail with the exception code and message
            // below.  This means that the job at least made it to the Force.com
            // service.  If something else goes wrong (e.g. wrong code, wrong message,
            // or a different exception) then there's something wrong with the
            // BulkConnection.
            if (expected.getExceptionCode() == AsyncExceptionCode.InvalidJob) {
                assertEquals(expected.getExceptionMessage(), "Operation not specified",
                        "Unexpected exception message for invalid job " + expected.getExceptionMessage());
            } else {
                throw expected;
            }
        }
    }
    
    protected void verifyNamespace(String namespace) {
        assertEquals(namespace, this.testOrgNamespace);
    }
}
