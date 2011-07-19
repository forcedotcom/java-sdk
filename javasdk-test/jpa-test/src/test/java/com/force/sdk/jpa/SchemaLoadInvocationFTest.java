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

package com.force.sdk.jpa;

import java.io.IOException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.qa.util.MockAppender;
import com.force.sdk.qa.util.PropsUtil;
import com.sforce.ws.ConnectionException;

/**
 * One aspect of performance testing that counts
 * certain method invocations during schema loading.
 * 
 * @author Tim Kral
 */
public class SchemaLoadInvocationFTest {
    
    @Test
    public void testSchemaCreateLoginCache() throws ConnectionException, IOException {
        Logger logger = Logger.getLogger("com.force.sdk.connector");
        Level oldLevel = logger.getLevel();
        Properties sdkTestProps = PropsUtil.load(PropsUtil.FORCE_SDK_TEST_PROPS);
        String expectedLogLine = "ForceServiceConnector Cache: MISS for id: "
            + sdkTestProps.getProperty(PropsUtil.FORCE_USER_PROP);
        MockAppender mockAppender = new MockAppender(expectedLogLine);
        logger.addAppender(mockAppender);
        try {
        logger.setLevel(Level.TRACE);
        EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("SchemaLoadInvocationFTest");
        emf.createEntityManager();
        } finally {
            logger.setLevel(oldLevel);
            logger.removeAppender(mockAppender);
        }
        // if this test is run along with the rest of the force-jpa-test suite, the connector will have
        // already been instantiated previously, and the cache will always be hit (expected log line
        // will not appear).  if this test is run by itself, the cache will be missed only once 
        // (expected log line will appear only once).
        Assert.assertTrue(mockAppender.getLogLineTimes() == 0 || mockAppender.getLogLineTimes() == 1,
                "Log line " + expectedLogLine + " appeared unexpected number of times: " + mockAppender.getLogLineTimes());
    }
    
    
}
