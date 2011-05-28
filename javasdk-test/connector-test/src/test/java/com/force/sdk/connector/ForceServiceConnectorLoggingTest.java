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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sforce.async.AsyncApiException;
import com.sforce.ws.ConnectionException;

/**
 * Test for connector loglines.
 *
 * @author Fiaz Hossain
 */
public class ForceServiceConnectorLoggingTest extends BaseForceServiceConnectorTest {
    
    @Test
    public void testApiTraceLogging() throws ConnectionException {
        Logger logger = Logger.getLogger("com.force.sdk.connector");
        Level oldLevel = logger.getLevel();
        
        String expectedLogLine = "WSC: Creating a new connection to";
        MockAppender mockAppender = new MockAppender(expectedLogLine);
        logger.addAppender(mockAppender);
        try {
            logger.setLevel(Level.TRACE);
            ForceConnectorConfig config = createConfig();
            ForceServiceConnector connector = new ForceServiceConnector(config);
            connector.getConnection();
        } finally {
            logger.setLevel(oldLevel);
            logger.removeAppender(mockAppender);
        }
        
        assertTrue(mockAppender.receivedLogLine(), "Did not receive expected log line: " + expectedLogLine);
    }
    
    // NOTE: This is not going to pass in STS.  You have to execute from the command line.
    @Test
    public void testLogConnectorLoadFromEnvVar() throws ConnectionException {
        Logger logger = Logger.getLogger("com.force.sdk.connector");
        
        // FORCE_ENVVARCONN_URL is set in pom file
        String expectedLogLine = "Connection : Creating envvarconn from environment variable";
        MockAppender mockAppender = new MockAppender(expectedLogLine);
        logger.addAppender(mockAppender);
        
        try {
            ForceServiceConnector connector = new ForceServiceConnector("envvarconn");
            connector.getConnection();
        } finally {
            logger.removeAppender(mockAppender);
        }
        
        assertTrue(mockAppender.receivedLogLine(), "Did not receive expected log line: " + expectedLogLine);
    }
    
    @Test
    public void testLogConnectorLoadFromJavaProp() throws ConnectionException {
        Logger logger = Logger.getLogger("com.force.sdk.connector");
        
        String expectedLogLine = "Connection : Creating testLogConnectorLoadFromJavaProp from Java system property";
        MockAppender mockAppender = new MockAppender(expectedLogLine);
        logger.addAppender(mockAppender);
        
        try {
            System.setProperty("force.testLogConnectorLoadFromJavaProp.url", createConnectionUrl());
            
            ForceServiceConnector connector = new ForceServiceConnector("testLogConnectorLoadFromJavaProp");
            connector.getConnection();
        } finally {
            System.clearProperty("force.testLogConnectorLoadFromJavaProp.url");
            logger.removeAppender(mockAppender);
        }
        
        assertTrue(mockAppender.receivedLogLine(), "Did not receive expected log line: " + expectedLogLine);
    }
    
    @Test
    public void testLogConnectorLoadFromClasspathPropFile() throws ConnectionException {
        Logger logger = Logger.getLogger("com.force.sdk.connector");
        
        String expectedLogLine = "Connection : Creating funcconnuserinfo from classpath properties file";
        MockAppender mockAppender = new MockAppender(expectedLogLine);
        logger.addAppender(mockAppender);
        
        try {
            ForceServiceConnector connector = new ForceServiceConnector("funcconnuserinfo");
            connector.getConnection();
        } finally {
            logger.removeAppender(mockAppender);
        }
        
        assertTrue(mockAppender.receivedLogLine(), "Did not receive expected log line: " + expectedLogLine);
    }
    
    @Test
    public void testLogConnectorLoadFromCliforcePropFile() throws ConnectionException, IOException {
        Logger logger = Logger.getLogger("com.force.sdk.connector");
        
        String connectionName = "ForceServiceConnectorLoggingTest.testLogConnectorLoadFromCliforcePropFile";
        String expectedLogLine = "Connection : Creating " + connectionName + " from cliforce connections file";
        MockAppender mockAppender = new MockAppender(expectedLogLine);
        logger.addAppender(mockAppender);
        
        try {
            ForceConnectorTestUtils.createCliforceConn(connectionName, createConnectionUrl());
            
            ForceServiceConnector connector = new ForceServiceConnector(connectionName);
            connector.getConnection();
        } finally {
            logger.removeAppender(mockAppender);
        }
        
        assertTrue(mockAppender.receivedLogLine(), "Did not receive expected log line: " + expectedLogLine);
    }
    
    @Test
    public void testLogConnectorCacheCheck() throws ConnectionException {
        Logger logger = Logger.getLogger("com.force.sdk.connector");
        Level oldLevel = logger.getLevel();
        
        String expectedCheckLogLine = "ForceServiceConnector Cache: Checking for id";
        MockAppender mockCheckAppender = new MockAppender(expectedCheckLogLine);
        
        String expectedMissLogLine = "ForceServiceConnector Cache: MISS for id";
        MockAppender mockMissAppender = new MockAppender(expectedMissLogLine);
        
        String expectedHitLogLine = "ForceServiceConnector Cache: HIT for id";
        MockAppender mockHitAppender = new MockAppender(expectedHitLogLine);
        
        logger.addAppender(mockCheckAppender);
        logger.addAppender(mockMissAppender);
        logger.addAppender(mockHitAppender);
        
        try {
            logger.setLevel(Level.TRACE);
            ForceConnectorConfig config = createConfig();
            
            // The first time we connect, we'll miss the cache
            ForceServiceConnector cacheMissConnector = new ForceServiceConnector(config);
            cacheMissConnector.getConnection();
            
            // Subsequent connections should hit the cache
            ForceServiceConnector cacheHitConnector = new ForceServiceConnector(config);
            cacheHitConnector.getConnection();
        } finally {
            logger.setLevel(oldLevel);
            
            logger.removeAppender(mockHitAppender);
            logger.removeAppender(mockMissAppender);
            logger.removeAppender(mockCheckAppender);
        }
        
        assertTrue(mockCheckAppender.receivedLogLine(), "Did not receive expected log line: " + expectedCheckLogLine);
        assertTrue(mockMissAppender.receivedLogLine(), "Did not receive expected log line: " + expectedMissLogLine);
        assertTrue(mockHitAppender.receivedLogLine(), "Did not receive expected log line: " + expectedHitLogLine);
    }
    
    // Test that cache is NOT hit when skipCache flag is set OR username is null.
    @Test
    public void testConnectorSkipCache() throws ConnectionException, AsyncApiException {
        Logger logger = Logger.getLogger("com.force.sdk.connector");
        Level oldLevel = logger.getLevel();
        
        String expectedCheckLogLine = "ForceServiceConnector Cache: Checking for id";
        MockAppender mockCheckAppender = new MockAppender(expectedCheckLogLine);
        
        String expectedMissLogLine = "ForceServiceConnector Cache: MISS for id";
        MockAppender mockMissAppender = new MockAppender(expectedMissLogLine);
        
        String expectedHitLogLine = "ForceServiceConnector Cache: HIT for id";
        MockAppender mockHitAppender = new MockAppender(expectedHitLogLine);
                
        logger.addAppender(mockCheckAppender);
        
        try {
            logger.setLevel(Level.TRACE);
            ForceConnectorConfig config = createConfig();
            ForceServiceConnector connector = new ForceServiceConnector(config);
            
            // Test: skip cache explicitly.
            connector.setSkipCache(true);
            connector.getMetadataConnection();
            connector.getBulkConnection();
            connector.getConnection();
            
            
            // Test: use a config with null username. Implicilty skip cache.
            config = new ForceConnectorConfig();
            config.setSessionId("session Id");
            config.setServiceEndpoint(userInfo.getServerEndpoint());
            connector = new ForceServiceConnector(config);
            connector.setSkipCache(false); // no-op. As, this is default.
            connector.getMetadataConnection();
            connector.getBulkConnection();
            connector.getConnection();
        } finally {
            logger.setLevel(oldLevel);
            logger.removeAppender(mockCheckAppender);
        }
        
        Assert.assertFalse(mockCheckAppender.receivedLogLine(), "The cache should not be checked when skipCache is set.");
        Assert.assertFalse(mockMissAppender.receivedLogLine(), "The cache should not be checked when skipCache is set.");
        Assert.assertFalse(mockHitAppender.receivedLogLine(), "The cache should not be checked when skipCache is set.");
    }
    
    /**
     * Mock log appender which caches log lines received.
     * 
     * @author Tim Kral
     */
    private static class MockAppender extends AppenderSkeleton {
        final AtomicBoolean receivedLogLine = new AtomicBoolean(false);
        final String expectedLogLine;
        
        public MockAppender(String expectedLogLine) {
            this.expectedLogLine = expectedLogLine;
        }
        
        @Override
        public boolean requiresLayout() {
            return false;
        }
        
        @Override
        public void close() {  }
        
        @Override
        protected void append(LoggingEvent event) {
            if (event != null && event.getRenderedMessage().contains(expectedLogLine)) {
                receivedLogLine.set(true);
            }
        }
        
        public boolean receivedLogLine() {
            return receivedLogLine.get();
        }
    };
}
