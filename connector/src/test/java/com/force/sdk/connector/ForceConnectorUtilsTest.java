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

import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Unit Tests for ForceConnectorUtils.
 *
 * @author Tim Kral
 */
public class ForceConnectorUtilsTest {

    File cachedCliforceConnFile;

    @DataProvider
    protected Object[][] endpointProvider() {
        return new Object[][] {
                {"url", "https", "url", -1},
                {"url:80", "https", "url", 80},
                {"url/", "https", "url", -1},
                {"http://url", "https", "url", -1},
                {"https://url", "https", "url", -1},
                {"http://localhost:8080", "http", "localhost", 8080},
                {"https://localhost:8080", "https", "localhost", 8080},
        };
    }
    
    @Test(dataProvider = "endpointProvider")
    public void testBuildForceApiEndpoint(String endpoint, String expectedProtocol, String expectedHost, int expectedPort)
    throws MalformedURLException {
        URL apiUrl = new URL(ForceConnectorUtils.buildForceApiEndpoint(endpoint));
        assertEquals(apiUrl.getProtocol(), expectedProtocol);
        assertEquals(apiUrl.getHost(), expectedHost);
        assertEquals(apiUrl.getPort(), expectedPort);
        assertEquals(apiUrl.getPath(), new URL(com.sforce.soap.partner.Connector.END_POINT).getPath());
    }

    @AfterMethod
    public void cleanPropertiesCache() {
        ForceConnectorUtils.clearCache();
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.size(), 0);
    }

    @Test
    public void testFilePropertiesCache() throws IOException {
        Map<ForceConnectionProperty, String> props = ForceConnectorUtils.loadConnectorPropsFromName("unitconnurl");

        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.size(), 1);
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.get("unitconnurl"), props);
    }

    @Test
    public void testFilePropertiesCacheWithTwoMatchingFiles() throws IOException {
        final String connName = "unitconnurl";
        Map<ForceConnectionProperty, String> props = ForceConnectorUtils.loadConnectorPropsFromName(connName);
        Map<ForceConnectionProperty, String> duplicateProps = ForceConnectorUtils.loadConnectorPropsFromName(connName);

        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.size(), 1);
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.get(connName), props);
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.get(connName), duplicateProps);
    }

    @Test
    public void testFilePropertiesCacheWithTwoDifferentFiles() throws IOException {
        final String connName = "unitconnurl";
        Map<ForceConnectionProperty, String> props = ForceConnectorUtils.loadConnectorPropsFromName(connName);

        final String secondConnName = "unitconnuserinfo";
        Map<ForceConnectionProperty, String> secondProps = ForceConnectorUtils.loadConnectorPropsFromName(secondConnName);

        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.size(), 2);
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.get(connName), props);
        Assert.assertNotSame(ForceConnectorUtils.PROPERTIES_CACHE.get(connName), secondProps);
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.get(secondConnName), secondProps);
        Assert.assertNotSame(ForceConnectorUtils.PROPERTIES_CACHE.get(secondConnName), props);
    }

    @Test
    public void testCliforcePropsCache() throws IOException, URISyntaxException {
        ForceConnectorUtils.cliforceConnFile = new File(this.getClass().getResource("/cliforce.properties").toURI());
        final String connName = "connA";
        final Map<ForceConnectionProperty, String> cliforceProps = ForceConnectorUtils.loadConnectorPropsFromName(connName);
        final Map<ForceConnectionProperty, String> duplicateCliforceProps =
                ForceConnectorUtils.loadConnectorPropsFromName(connName);

        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.size(), 1);
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.get(connName), cliforceProps);
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.get(connName), duplicateCliforceProps);
    }

    @BeforeClass
    public void cacheCliforceConnFile() {
        cachedCliforceConnFile = ForceConnectorUtils.cliforceConnFile;
    }

    @AfterClass
    public void resetCliforceConnFile() {
        ForceConnectorUtils.cliforceConnFile = cachedCliforceConnFile;
    }

    @Test
    public void testCliforcePropsCacheWithTwoDifferentURLs() throws IOException, URISyntaxException {
        // init cliforce file to a test file
        ForceConnectorUtils.cliforceConnFile = new File(this.getClass().getResource("/cliforce.properties").toURI());
        final String connName = "connA";
        final Map<ForceConnectionProperty, String> cliforceProps = ForceConnectorUtils.loadConnectorPropsFromName(connName);
        final String secondConnName = "connB";
        final Map<ForceConnectionProperty, String> duplicateCliforceProps =
                ForceConnectorUtils.loadConnectorPropsFromName(secondConnName);

        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.size(), 2);
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.get(connName), cliforceProps);
        Assert.assertNotSame(ForceConnectorUtils.PROPERTIES_CACHE.get(connName), duplicateCliforceProps);
        Assert.assertEquals(ForceConnectorUtils.PROPERTIES_CACHE.get(secondConnName), duplicateCliforceProps);
        Assert.assertNotSame(ForceConnectorUtils.PROPERTIES_CACHE.get(secondConnName), cliforceProps);
    }

}
