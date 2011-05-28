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

package com.force.sdk.springsecurity.config;

import static mockit.Deencapsulation.invoke;
import static org.testng.Assert.*;

import java.net.MalformedURLException;
import java.util.*;

import javax.servlet.Filter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.oauth.connector.ForceOAuthConnectionInfo;
import com.force.sdk.oauth.connector.ForceOAuthConnector;
import com.force.sdk.oauth.context.store.AESUtil;
import com.force.sdk.oauth.userdata.UserDataRetrievalService;
import com.force.sdk.springsecurity.AuthenticationProcessingFilter;
import com.force.sdk.springsecurity.ForceConnectionStorageFilter;

/**
 * Unit tests for the OAuthBeanDefinitionParser.
 *
 * @author Tim Kral, John Simone
 * 
 */
public class OAuthBeanDefinitionParserTest {

    @DataProvider
    public Object[][] fssOAuthConfigFiles() throws NumberFormatException, MalformedURLException {
        Object [][] params = new Object[][]{
                {"security-config-ns-oauthInfo.xml"},
                {"security-config-ns-oauthInfo-securekey.xml"},
        };
        
        return params;
    }
    
    @Test(dataProvider = "fssOAuthConfigFiles")
    public void testParseOAuthInfo(String configLocation) {
        if (AESUtil.class.getResource("/" + configLocation) == null) {
            throw new SkipException(configLocation + " is not inaccessible or doesn't exist on the path.");
        }
        
        ApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
        assertTrue(context.containsBean("oauthConnectionInfo"),
                    "Could not find oauthConnectionInfo bean after parsing " + configLocation);
        assertTrue(context.containsBean("oauthConnector"),
                    "Could not find oauthConnector bean after parsing " + configLocation);
        assertTrue(context.containsBean("forceRememberMeServices"),
                    "Could not find forceRememberMeServices bean after parsing " + configLocation);
        assertTrue(context.containsBean("rememberMeFilter"),
                    "Could not find rememberMeFilter bean after parsing " + configLocation);
        assertTrue(context.containsBean("connectionStorageFilter"),
                    "Could not find connectionStorageFilter bean after parsing " + configLocation);
        
        ForceOAuthConnectionInfo connInfo = context.getBean("oauthConnectionInfo", ForceOAuthConnectionInfo.class);
        ForceOAuthConnector connector = context.getBean("oauthConnector", ForceOAuthConnector.class);
        
        validateConnectionInfo(connInfo, "Parsing " + configLocation + ", checking ForceOAuthConnectionInfo");

        ForceOAuthConnectionInfo connInfoOnConnector = invoke(connector, "getConnInfo");
        validateConnectionInfo(connInfoOnConnector,
                "Parsing " + configLocation + ", checking ForceOAuthConnectionInfo on ForceOAuthConnector");
        
        verifyAllFiltersArePresent(context);
    }
    
    @Test
    public void testParseConnectionUrl() {
        String configLocation = "security-config-ns-connUrl.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
        assertTrue(context.containsBean("oauthConnectionInfo"),
                    "Could not find oauthConnectionInfo bean after parsing " + configLocation);
        assertTrue(context.containsBean("oauthConnector"),
                    "Could not find oauthConnector bean after parsing " + configLocation);
        assertTrue(context.containsBean("forceRememberMeServices"),
                    "Could not find forceRememberMeServices bean after parsing " + configLocation);
        assertTrue(context.containsBean("rememberMeFilter"),
                    "Could not find rememberMeFilter bean after parsing " + configLocation);
        assertTrue(context.containsBean("connectionStorageFilter"),
                    "Could not find connectionStorageFilter bean after parsing " + configLocation);
        
        ForceOAuthConnectionInfo connInfo = context.getBean("oauthConnectionInfo", ForceOAuthConnectionInfo.class);
        ForceOAuthConnector connector = context.getBean("oauthConnector", ForceOAuthConnector.class);
        UserDataRetrievalService userDataRetrievalService =
            context.getBean("userDataRetrievalService", UserDataRetrievalService.class);
        
        assertTrue(userDataRetrievalService.isStoreUsername(),
                "store-user-name was set to true so the storeUsername value "
                    + "on the user data retrieval service should be true");
        validateConnectionInfo(connInfo, "Parsing " + configLocation + ", checking ForceOAuthConnectionInfo");
        
        ForceOAuthConnectionInfo connInfoOnConnector = invoke(connector, "getConnInfo");
        validateConnectionInfo(connInfoOnConnector,
                "Parsing " + configLocation + ", checking ForceOAuthConnectionInfo on ForceOAuthConnector");
        
        verifyAllFiltersArePresent(context);
    }
    
    @Test
    public void testParseConnectionName() {
        String configLocation = "security-config-ns-connName.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
        assertFalse(context.containsBean("oauthConnectionInfo"),
                    "Should not find oauthConnectionInfo bean after parsing " + configLocation);
        assertTrue(context.containsBean("oauthConnector"),
                    "Could not find oauthConnector bean after parsing " + configLocation);
        assertTrue(context.containsBean("forceRememberMeServices"),
                    "Could not find forceRememberMeServices bean after parsing " + configLocation);
        assertTrue(context.containsBean("rememberMeFilter"),
                    "Could not find rememberMeFilter bean after parsing " + configLocation);
        assertTrue(context.containsBean("connectionStorageFilter"),
                    "Could not find connectionStorageFilter bean after parsing " + configLocation);
        
        ForceOAuthConnector connector = context.getBean("oauthConnector", ForceOAuthConnector.class);
        UserDataRetrievalService userDataRetrievalService =
            context.getBean("userDataRetrievalService", UserDataRetrievalService.class);
        
        assertTrue(userDataRetrievalService.isStoreUsername(),
                    "store-user-name not set so the storeUsername value "
                        + "on the user data retrieval service should default to true");
        // For expected values, see security-config-ns-connName.xml
        // connector property is loaded through a non-public getter
        assertEquals(invoke(connector, "getConnectionName"), "connName",
                "Unexpected connection name after parsing " + configLocation);
        
        verifyAllFiltersArePresent(context);
    }
    
    @Test
    public void testParseConnectionNameAndSession() {
        String configLocation = "security-config-ns-connNameAndSessionStorage.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
        assertFalse(context.containsBean("oauthConnectionInfo"),
                    "Should not find oauthConnectionInfo bean after parsing " + configLocation);
        assertTrue(context.containsBean("oauthConnector"),
                    "Could not find oauthConnector bean after parsing " + configLocation);
        assertTrue(context.containsBean("forceRememberMeServices"),
                    "Could not find forceRememberMeServices bean after parsing " + configLocation);
        assertTrue(context.containsBean("rememberMeFilter"),
                    "Could not find rememberMeFilter bean after parsing " + configLocation);
        assertTrue(context.containsBean("connectionStorageFilter"),
                    "Could not find connectionStorageFilter bean after parsing " + configLocation);
        UserDataRetrievalService userDataRetrievalService =
            context.getBean("userDataRetrievalService", UserDataRetrievalService.class);
        
        Assert.assertFalse(userDataRetrievalService.isStoreUsername(),
                    "store-user-name was set to false so the storeUsername value "
                        + "on the user data retrieval service should be false");
        ForceOAuthConnector connector = context.getBean("oauthConnector", ForceOAuthConnector.class);
        
        // For expected values, see security-config-ns-connName.xml
        // connector property is loaded through a non-public getter
        assertEquals(invoke(connector, "getConnectionName"), "connName",
                "Unexpected connection name after parsing " + configLocation);
        
        verifyAllFiltersArePresent(context);
    }
    
    @Test
    public void testParseConnectionNameWithCustomRetriever() {
        String configLocation = "security-config-ns-connNameAndCustomRetriever.xml";
        ApplicationContext context = new ClassPathXmlApplicationContext(configLocation);
        assertFalse(context.containsBean("oauthConnectionInfo"),
                    "Should not find oauthConnectionInfo bean after parsing " + configLocation);
        assertTrue(context.containsBean("oauthConnector"),
                    "Could not find oauthConnector bean after parsing " + configLocation);
        assertTrue(context.containsBean("userDataRetrievalService"),
                    "Could not find customUserDataRetrieverService bean after parsing " + configLocation);
        assertTrue(context.containsBean("testUserDataRetriever"),
                    "Could not find testUserDataRetriever bean after parsing " + configLocation);
        assertTrue(context.containsBean("forceRememberMeServices"),
                    "Could not find forceRememberMeServices bean after parsing " + configLocation);
        assertTrue(context.containsBean("rememberMeFilter"),
                    "Could not find rememberMeFilter bean after parsing " + configLocation);
        assertTrue(context.containsBean("connectionStorageFilter"),
                    "Could not find connectionStorageFilter bean after parsing " + configLocation);
        
        ForceOAuthConnector connector = context.getBean("oauthConnector", ForceOAuthConnector.class);
        
        // For expected values, see security-config-ns-connName.xml
        // connector property is loaded through a non-public getter
        assertEquals(invoke(connector, "getConnectionName"), "connName",
                "Unexpected connection name after parsing " + configLocation);
        
        verifyAllFiltersArePresent(context);
    }
    
    private void validateConnectionInfo(ForceOAuthConnectionInfo connInfo, String extraErrorMessage) {
        // connection info properties are loaded through non-public getters
        assertEquals(invoke(connInfo, "getEndpoint"), "endpoint", "Unexpected oauth endpoint " + extraErrorMessage);
        assertEquals(invoke(connInfo, "getOauthKey"), "key", "Unexpected oauth key " + extraErrorMessage);
        assertEquals(invoke(connInfo, "getOauthSecret"), "123456", "Unexpected oauth secret " + extraErrorMessage);
    }
    
    private static void verifyAllFiltersArePresent(ApplicationContext context) {
        FilterChainProxy filterChainProxy = context.getBean(FilterChainProxy.class);
        assertNotNull(filterChainProxy, "Filter chain proxy should not be null after configuration has been loaded");
        Map<String, List<Filter>> filterChainMap = filterChainProxy.getFilterChainMap();
        assertNotNull(filterChainMap, "Filter chain map should not be null after configuration has been loaded");

         //get the list of registered filters for the default request
         List<Filter> filters = filterChainMap.get("/**");
         List<Class<? extends Filter>> filterClasses = new
         ArrayList<Class<? extends Filter>>();
         for (Filter filter : filters) {
             filterClasses.add(filter.getClass());
         }
                
        assertTrue(filterClasses.contains(AuthenticationProcessingFilter.class),
                    "The Spring Security filter chain should contain an AuthenticationProcessingFilter");
        assertTrue(filterClasses.contains(RememberMeAuthenticationFilter.class),
                    "The Spring Security filter chain should contain a RememberMeAuthenticationFilter");
        assertTrue(filterClasses.contains(ForceConnectionStorageFilter.class),
                    "The Spring Security filter chain should contain a ForceConnectionStorageFilter");
        assertTrue(filterClasses.contains(LogoutFilter.class),
                    "The Spring Security filter chain should contain a LogoutFilter");
    }
}
