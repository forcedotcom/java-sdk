package com.force.sdk.jpa.connection;

import com.force.sdk.connector.ForceConnectionProperty;
import com.force.sdk.connector.ForceConnector;
import com.force.sdk.connector.ForceConnectorUtils;
import mockit.*;
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
//@UsingMocksAndStubs(MultipleJPAConnectionTest.MockForceConnectorUtils.class)
public final class MultipleJPAConnectionTest {

    @Mocked(methods = {"loadConnectorPropsFromName"})
    ForceConnectorUtils unused;

    Map<ForceConnectionProperty, String> connectorProps = new HashMap<ForceConnectionProperty, String>();

    @Test
    public void testCreateEntityManagerThenQueryOnlyLoadsPropertiesOnce() throws IOException {
        setupConnector("userInfoPropFile");
        new NonStrictExpectations() {
            {
                ForceConnectorUtils.loadConnectorPropsFromName(anyString); result = connectorProps; times = 1;
            }
        };

        Persistence.createEntityManagerFactory("connectionCacheTest").createEntityManager().createNativeQuery("Select id From Account").getResultList();
    }

    private void setupConnector(String resource) throws IOException {
        Properties props = new Properties();
        props.load(this.getClass().getResource("/" + resource + ".properties").openStream());
        if (props.containsKey("url"))
            connectorProps = ForceConnectorUtils.loadConnectorPropsFromUrl(props.getProperty("url"));
        else {
            connectorProps.put(ForceConnectionProperty.USER, props.getProperty(ForceConnectionProperty.USER.getPropertyName()));
            connectorProps.put(ForceConnectionProperty.PASSWORD, props.getProperty(ForceConnectionProperty.PASSWORD.getPropertyName()));
            connectorProps.put(ForceConnectionProperty.ENDPOINT, props.getProperty(ForceConnectionProperty.ENDPOINT.getPropertyName()));
        }
    }

}
