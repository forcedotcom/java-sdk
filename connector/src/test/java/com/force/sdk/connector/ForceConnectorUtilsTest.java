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

import static org.testng.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit Tests for ForceConnectorUtils.
 *
 * @author Tim Kral
 */
public class ForceConnectorUtilsTest {
    
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
}
