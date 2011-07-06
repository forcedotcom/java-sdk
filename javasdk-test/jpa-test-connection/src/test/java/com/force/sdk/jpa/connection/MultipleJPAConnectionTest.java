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

package com.force.sdk.jpa.connection;

import com.force.sdk.connector.ForceConnectionProperty;
import com.force.sdk.connector.ForceConnectorUtils;
import com.force.sdk.connector.ForceServiceConnectorCacheTestUtil;
import mockit.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.Persistence;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Tests for multiple connections through JPA persistence units.
 *
 * @author Naaman Newbold
 */
public class MultipleJPAConnectionTest {

    @Mocked(methods = { "loadConnectorPropsFromName" })
    ForceConnectorUtils unused; // allow jmockit to count

    Map<ForceConnectionProperty, String> connectorProps = new HashMap<ForceConnectionProperty, String>();

    @BeforeClass
    public void setupConnectorPropsForConnectorUtilsMock() throws IOException {
        Properties props = new Properties();
        props.load(this.getClass().getResource("/cachePropFile.properties").openStream());
        connectorProps.put(ForceConnectionProperty.USER, props.getProperty(ForceConnectionProperty.USER.getPropertyName()));
        connectorProps.put(ForceConnectionProperty.PASSWORD,
                props.getProperty(ForceConnectionProperty.PASSWORD.getPropertyName()));
        connectorProps.put(ForceConnectionProperty.ENDPOINT,
                props.getProperty(ForceConnectionProperty.ENDPOINT.getPropertyName()));
    }

    @BeforeMethod
    public void clearForceServiceConnectorCache() {
        // ensure cache is cleared before each test run to ensure accurate counts
        ForceServiceConnectorCacheTestUtil.clearForceServiceConnectorCache();
        Assert.assertEquals(ForceServiceConnectorCacheTestUtil.forceServiceConnectorCachedConfigCount(), 0);
    }

    @Test
    public void testCreateEntityManagerThenQueryOnlyLoadsPropertiesOnce() throws IOException {
        new NonStrictExpectations() { {
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 1;
        } };
        Persistence.createEntityManagerFactory("connectionCacheTest").createEntityManager().
                createNativeQuery("Select id From User").getResultList();
    }

    @Test
    public void testCreatingMultipleEntityManagersWithTwoPersistenceUnitsLoadsPropertiesOnlyTwice() throws IOException {
        new NonStrictExpectations() { {
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 2;
        } };
        Persistence.createEntityManagerFactory("connectionCacheTest");
        Persistence.createEntityManagerFactory("connectionCacheTest2");
        Persistence.createEntityManagerFactory("connectionCacheTest");
        Persistence.createEntityManagerFactory("connectionCacheTest2");
    }

    @Test
    public void testLoadingTwoEntityManagersWithTheSamePersistenceUnitNameOnlyLoadsPropsOnce() throws IOException {
        new NonStrictExpectations() { {
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 1;
        } };
        Persistence.createEntityManagerFactory("connectionCacheTest");
        Persistence.createEntityManagerFactory("connectionCacheTest");
    }

    @Test
    public void testLoadingOneEntityManagerFactoryLoadsPropsOnce() throws IOException {
        new NonStrictExpectations() { {
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 1;
        } };
        Persistence.createEntityManagerFactory("connectionCacheTest");
    }

    @Test
    public void testLoadingOneEntityManagerLoadsPropsOnce() throws IOException {
        new NonStrictExpectations() { {
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 1;
        } };
        Persistence.createEntityManagerFactory("connectionCacheTest").createEntityManager();
    }

}
