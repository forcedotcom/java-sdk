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

import static org.testng.Assert.*;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URL;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectorConfig;

/**
 * Tests for the ForceServiceConnector cache.
 * 
 * @author Tim Kral
 */
public class ForceServiceConnectorCacheTest extends BaseForceServiceConnectorTest {

    @Test
    public void testCacheWithConfig() throws Exception {
        ForceConnectorConfig config = createConfig();
        config.setReadTimeout(1234);

        ForceServiceConnector connector = new ForceServiceConnector(config);
        connector.getConnection();

        config.setReadTimeout(2345);
        connector = new ForceServiceConnector(config);

        // We should hit the cache here and retrieve the original read timeout
        PartnerConnection conn = connector.getConnection();
        assertEquals(conn.getConfig().getReadTimeout(), 1234);
    }

    @Test
    public void testCacheWithConnectionName() throws Exception {
        String connUrl = createConnectionUrl();
        try {
            System.setProperty("force.testCacheWithConnectionName.url", connUrl + ";timeout=1234");

            ForceServiceConnector connector = new ForceServiceConnector("testCacheWithConnectionName");
            connector.getConnection();

            System.setProperty("force.testCacheWithConnectionName.url", connUrl + ";timeout=2345");
            connector = new ForceServiceConnector("testCacheWithConnectionName");

            // We should hit the cache here and retrieve the original read timeout
            PartnerConnection conn = connector.getConnection();
            assertEquals(conn.getConfig().getReadTimeout(), 1234);
        } finally {
            System.clearProperty("force.testCacheWithConnectionName.url");
        }
    }

    @Test
    public void testSessionIdIsCached() throws Exception {
        ForceConnectorConfig config = createConfig();
        ForceServiceConnector connector = new ForceServiceConnector(config);
        connector.getConnection();

        String cacheId = config.getCacheId();
        ForceConnectorConfig cachedConfig = ForceServiceConnector.getCachedConfig(cacheId);

        assertNotNull(cachedConfig, "Expected config to be cached but it was not found in the cache.");
        assertNotNull(cachedConfig.getSessionId(), "SessionId was not cached in the ForceServiceConnector cache.");
    }

    @Test
    public void testSkipCacheExplicit() throws Exception {
        ForceConnectorConfig config = createConfig();
        ForceServiceConnector connector = new ForceServiceConnector(config);
        connector.setSkipCache(true); // Explicitly skip cache reads and writes
        connector.getConnection();

        String cacheId = config.getCacheId();
        assertNotNull(cacheId, "Expected cacheId to be non-null");

        // The config should not be cached because we explicitly asked
        // to skip cache reads and writes
        ForceConnectorConfig cachedConfig = ForceServiceConnector.getCachedConfig(cacheId);
        assertNull(cachedConfig, "Unexpected cached config found.");
    }

    @Test
    public void testSkipCacheImplicit() throws Exception {
        ConnectorConfig nativeConfig = new ConnectorConfig();
        nativeConfig.setAuthEndpoint(userInfo.getServerEndpoint());
        nativeConfig.setUsername(userInfo.getUserName());
        nativeConfig.setPassword(userInfo.getPassword());

        // Login outside of the ForceServiceConnector
        PartnerConnection conn = Connector.newConnection(nativeConfig);

        // A session id can represent a username and password
        // so this is still a valid config
        ForceConnectorConfig config = createConfig();
        config.setUsername(null);
        config.setPassword(null);
        config.setSessionId(conn.getSessionHeader().getSessionId());

        ForceServiceConnector connector = new ForceServiceConnector(config);
        connector.getConnection();

        // Since we don't have a username and password, we can't generate
        // a cache id for the config. Verify this is true. That's ok, because 
        // we're supplying a session id so we'll skip login anyway. If we
        // tried to cache this, we would have gotten an NPE.
        assertNull(config.getCacheId(), "Expected cacheId to be null");
    }
    
    public static int getProcessId() {
        String beanName = ManagementFactory.getRuntimeMXBean().getName();
        String[] split = beanName.split("@");
        return Integer.parseInt(split[0]);
    }
    
    public static int executeIt(String cmd) throws IOException  {
        int count = 0;
        Process lsProc = null;

        InputStream lsIn = null;
        try {
            lsProc = Runtime.getRuntime().exec(cmd);
            lsIn  = new BufferedInputStream(lsProc.getInputStream());
            
            byte[] buffer = new byte[1024];
            int readChars = 0;
            while ((readChars = lsIn.read(buffer)) != -1) {
                for (int i = 0; i < readChars; i++) {
                    if (buffer[i] == '\n')
                        count++;
                }
            }
        } catch (IOException e1) {
            // Ignored
        } finally {
            if (lsIn != null) lsIn.close();
            
            if (lsProc != null) {
                lsProc.getInputStream().close();
                lsProc.getOutputStream().close();
                lsProc.getErrorStream().close();
            }
        }

        return count;
    }
    
    // This test is linux specific and will not run on windows. 
    @Test
    public void testLoadFromCache() throws Exception {
        URL propsFileUrl = ForceServiceConnectorCacheTest.class.getResource("/funcconnuserinfo.properties");
        String path = propsFileUrl.getPath();
        String commandFormat = "lsof %1$s -p %2$s "; // list open handles for this file.
        int pid = getProcessId();
        String cmd = String.format(commandFormat, path, pid);

        int handleCount = executeIt(cmd);
        
        for (int i = 0; i < 10; i++) {
            ForceConnectorUtils.loadConnectorPropsFromFile(propsFileUrl);
        }
        
        int handleCount2 = executeIt(cmd);
        Assert.assertEquals(handleCount2, handleCount, "Handle count has increased after the test. ");
    }
}
