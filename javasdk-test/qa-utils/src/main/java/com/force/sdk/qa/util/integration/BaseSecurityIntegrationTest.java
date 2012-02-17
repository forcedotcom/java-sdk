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

package com.force.sdk.qa.util.integration;

import java.io.IOException;
import java.util.*;

import org.apache.http.HttpResponse;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.force.sdk.qa.util.*;
import com.force.sdk.qa.util.TestContext.TestType;

/**
 * 
 * This is the base class for all spring security integration tests.  This class will deploy an application that
 * implements spring security to a web container.
 *
 * @author Jeff Lai
 * @author Nawab Iqbal
 *
 */
public abstract class BaseSecurityIntegrationTest extends BaseContainerTest {
    protected final String port = System.getProperty("containerPort");
    protected final String appEndpoint = System.getProperty("appEndpoint");
    protected final String appWarPath = System.getProperty("appWarPath");
    protected final String mockOauthServerWarPath = System.getProperty("mockOauthServerWarPath");
    
    protected final String mockOauthKey = "123";
    protected final String mockOauthSecret = "456";
    protected final String mockSfdcEndpoint = "localhost:" + port + "/force-mock-oauth-server-app";
    protected final String forceUrlPropName = "integrationserver.url";
    protected final String mockAuthCode = "789";
    protected final String useMockApi = "mockapi";

    protected String sfdcEndpoint;
    public String username;
    public String password;
    protected String oauthKey;
    protected String oauthSecret;
    
    @Override
    @BeforeSuite
    public void suiteSetup() throws Exception {
        super.suiteSetup();
        deployWar(appWarPath);
        if (TestContext.get().getTestType() == TestType.MOCK) {
            deployWar(mockOauthServerWarPath);
        }
    }
    
    @BeforeClass
    public void classSetup() throws IOException {
        loadProps();
    }

    @Override
    public String getZipInstallerUrl() {
        return "http://dbdotcom-sdk-test.s3.amazonaws.com/apache-tomcat-6.0.35.zip";
    }

    @Override
    public String getContainerId() {
        return "tomcat6x";
    }

    public String getJMockitPath() {
        String[] paths = System.getProperties().getProperty("java.class.path").split(":");

        for (String path : paths) {
            if (path.contains("jmockit")) {
                return path;
            }
        }

        return null;
    }

    @Override
    public Map<String, String> getConfigProps() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(ServletPropertySet.PORT, port);
        map.put(GeneralPropertySet.LOGGING , "high");

        if (TestContext.get().getTestType() == TestType.MOCK) {
            // JDK 1.6.0_24 (Linux) seems to have some different implementation of Attach API as compared to 1.6.0_16
            // and was failing with exception when setupMock is used in web applications inside the container.
            // Using the following jvm parameter causes the JMockit agent to load without going through Attach API.

            String mockitPath = getJMockitPath();
            System.out.println("JMockit jar path:" + mockitPath);
            map.put(GeneralPropertySet.JVMARGS, "-javaagent:" + mockitPath);
        }

        return map;
    }

    @Override
    public Map<String, String> getContainerProps() throws IOException {
        loadProps();
        Map<String, String> map = new HashMap<String, String>();
        if (TestContext.get().getTestType() == TestType.ENDTOEND) {
            map.put(useMockApi, "false");
            map.put(forceUrlPropName, "force://" + sfdcEndpoint
                    + "?oauth_key=" + oauthKey + "&oauth_secret=" + oauthSecret);
        } else if (TestContext.get().getTestType() == TestType.MOCK) {
            map.put(useMockApi, "true");
            map.put(forceUrlPropName, "force://" + mockSfdcEndpoint
                    + "?oauth_key=" + mockOauthKey + "&oauth_secret=" + mockOauthSecret);
        }

        return map;
    }
    
    protected void loadProps() throws IOException {
        Properties testProperties = PropsUtil.load(PropsUtil.FORCE_SDK_TEST_PROPS);
        
        this.sfdcEndpoint = System.getProperty(PropsUtil.FORCE_EP_PROP) != null
                ? System.getProperty(PropsUtil.FORCE_EP_PROP) : testProperties.getProperty(PropsUtil.FORCE_EP_PROP);
        this.username = System.getProperty(PropsUtil.FORCE_USER_PROP) != null
                ? System.getProperty(PropsUtil.FORCE_USER_PROP) : testProperties.getProperty(PropsUtil.FORCE_USER_PROP);
        this.password = System.getProperty(PropsUtil.FORCE_PWD_PROP) != null
                ? System.getProperty(PropsUtil.FORCE_PWD_PROP) : testProperties.getProperty(PropsUtil.FORCE_PWD_PROP);
        this.oauthKey = System.getProperty("force.test.oauth.key") != null
                ? System.getProperty("force.test.oauth.key") : testProperties.getProperty("force.test.oauth.key");
        this.oauthSecret = System.getProperty("force.test.oauth.secret") != null
                ? System.getProperty("force.test.oauth.secret") : testProperties.getProperty("force.test.oauth.secret");
    }
    
    public void assertResponseStatus(HttpResponse response, int status, String description) {
        Assert.assertEquals(response.getStatusLine().getStatusCode(), status, "unexpected status code");
        Assert.assertEquals(response.getStatusLine().getReasonPhrase(), description, "unexpected status description");
    }
    
    
    /**
     * Convert url parameters into a String Map.
     * @param url
     * @return returns a map of parameters from a url
     */
    public Map<String, String> convertUrlParamsToMap(String url) {
        Map<String, String> params = new HashMap<String, String>();
        String[] keysAndValues = url.split("\\?")[1].split("&");
        for (String pair : keysAndValues) {
            String[] pairSplit = pair.split("=");
            if (pairSplit.length == 1) {
                params.put(pairSplit[0], "");
            } else {
                params.put(pairSplit[0], pairSplit[1]);
            }
        }
        return params;
    }
}
