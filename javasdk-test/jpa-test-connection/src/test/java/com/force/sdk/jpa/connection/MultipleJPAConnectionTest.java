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
 * Tests for multiple connections through JPA persistence units
 *
 * @author naamannewbold
 */
public class MultipleJPAConnectionTest {

    @Mocked(methods = {"loadConnectorPropsFromName"})
    ForceConnectorUtils unused; // allow jmockit to count

    Map<ForceConnectionProperty, String> connectorProps = new HashMap<ForceConnectionProperty, String>();

    @BeforeClass
    public void setupConnectorPropsForConnectorUtilsMock() throws IOException {
        Properties props = new Properties();
        props.load(this.getClass().getResource("/cachePropFile.properties").openStream());
        connectorProps.put(ForceConnectionProperty.USER, props.getProperty(ForceConnectionProperty.USER.getPropertyName()));
        connectorProps.put(ForceConnectionProperty.PASSWORD, props.getProperty(ForceConnectionProperty.PASSWORD.getPropertyName()));
        connectorProps.put(ForceConnectionProperty.ENDPOINT, props.getProperty(ForceConnectionProperty.ENDPOINT.getPropertyName()));
    }

    @BeforeMethod
    public void clearForceServiceConnectorCache() {
        // ensure cache is cleared before each test run to ensure accurate counts
        ForceServiceConnectorCacheTestUtil.clearForceServiceConnectorCache();
        Assert.assertEquals(ForceServiceConnectorCacheTestUtil.forceServiceConnectorCachedConfigCount(), 0);
    }

    @Test
    public void testCreateEntityManagerThenQueryOnlyLoadsPropertiesOnce() throws IOException {
        new NonStrictExpectations() {{
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 1;
        }};
        Persistence.createEntityManagerFactory("connectionCacheTest").createEntityManager().createNativeQuery("Select id From User").getResultList();
    }

    @Test
    public void testCreatingMultipleEntityManagersWithTwoPersistenceUnitsLoadsPropertiesOnlyTwice() throws IOException {
        new NonStrictExpectations() {{
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 2;
        }};
        Persistence.createEntityManagerFactory("connectionCacheTest");
        Persistence.createEntityManagerFactory("connectionCacheTest2");
        Persistence.createEntityManagerFactory("connectionCacheTest");
        Persistence.createEntityManagerFactory("connectionCacheTest2");
    }

    @Test
    public void testLoadingTwoEntityManagersWithTheSamePersistenceUnitNameOnlyLoadsPropsOnce() throws IOException {
        new NonStrictExpectations() {{
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 1;
        }};
        Persistence.createEntityManagerFactory("connectionCacheTest");
        Persistence.createEntityManagerFactory("connectionCacheTest");
    }

    @Test
    public void testLoadingOneEntityManagerFactoryLoadsPropsOnce() throws IOException {
        new NonStrictExpectations() {{
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 1;
        }};
        Persistence.createEntityManagerFactory("connectionCacheTest");
    }

    @Test
    public void testLoadingOneEntityManagerLoadsPropsOnce() throws IOException {
        new NonStrictExpectations() {{
            ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 1;
        }};
        Persistence.createEntityManagerFactory("connectionCacheTest").createEntityManager();
    }

}
