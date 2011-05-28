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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.sforce.ws.ConnectionException;

/**
 * Negative tests for ForceServiceConnector constructors.
 *
 * @author Tim Kral
 */
public class NegativeForceServiceConnectorConstructorTest {

    @Test
    public void testBadConnectorConfig() throws Exception {
        try {
            new ForceServiceConnector(new ForceConnectorConfig());
            fail("ForceServiceConnector constructor should have failed due to imcomplete ConnectorConfig");
        } catch (ConnectionException expected) {
            assertTrue(expected.getMessage().contains("ForceConnectorConfig must have an AuthEndpoint"));
        }
    }
    
    @Test
    public void testNullConnectorConfig() throws Exception {
        try {
            new ForceServiceConnector((ForceConnectorConfig) null);
            fail("ForceServiceConnector constructor should have failed because ConnectorConfig is null");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Cannot construct ForceServiceConnector with null ConnectorConfig."));
        }
    }
    
}
