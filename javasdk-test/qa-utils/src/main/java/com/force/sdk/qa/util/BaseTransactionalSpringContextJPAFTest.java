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

package com.force.sdk.qa.util;

import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;

import org.datanucleus.ObjectManager;
import org.datanucleus.store.connection.ConnectionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.ITest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;

import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.jpa.ForceManagedConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Base class for tests that use spring to handle persistence and transactions. Depending on test 
 * properties configured the framework will reestablish the context to access an org that is created 
 * for the test. The base class defines the default application context which can be overridden by
 * bean definitions in application contexts of subclasses.  
 *                                                                                   N
 * @see org.springframework.test.context.ContextConfiguration
 * @author Dirk Hain
 */
@ContextConfiguration(locations = { "/**/applicationContext.xml" })
public abstract class BaseTransactionalSpringContextJPAFTest extends
        AbstractTransactionalTestNGSpringContextTests implements ITest {

    protected PartnerConnection service;
    
    @PersistenceContext(name = "testDNJpaPersistence")
    public EntityManager entityManager;
    
    public BaseTransactionalSpringContextJPAFTest() {  }
    
    @Override
    public void setDataSource(DataSource dataSource) {
        // NOPE
    }
    
    @Override
    public String getTestName() {
        return getClass().getName();
    }


    /**
     * Reload the spring context to run against the test org. 
     */
    @BeforeClass(dependsOnMethods = "springTestContextPrepareTestInstance")
    protected void reloadEntityManager() throws Exception {
        BaseJPAFTest.populateTestContext(getTestName(), BaseJPAFTest.getDefaultUserInfoFromContext());
    }
    
    /**
     * Helper to create a service either from a dynamic org in the {@link TestContext} or from the 
     * credentials in force-sdk-test.properties. 
     * @return PartnerConnection to the org defined in the persistence descriptor
     */
    public static PartnerConnection getServiceFromDefaultContext() throws ConnectionException {
        
        String username, password, authEndpoint = null;
        UserInfo uinfo = TestContext.get().getUserInfo();
        if (uinfo == null) {
            Properties props = TestContext.get().getTestProps();
            username = props.getProperty(PropsUtil.FORCE_USER_PROP);
            password = props.getProperty(PropsUtil.FORCE_PWD_PROP);
            authEndpoint = props.getProperty(PropsUtil.FORCE_PROT_PROP) + "://" + props.getProperty(PropsUtil.FORCE_EP_PROP)
                + "/services/Soap/u/" + props.getProperty(PropsUtil.FORCE_APIV_PROP);
        } else {
            username = uinfo.getUserName();
            password = uinfo.getPassword();
            authEndpoint = uinfo.getServerEndpoint();
        }
        
        ForceConnectorConfig conf = new ForceConnectorConfig();
        conf.setUsername(username);
        conf.setPassword(password);
        conf.setAuthEndpoint(authEndpoint);
        ForceServiceConnector conn = new ForceServiceConnector(conf);
        PartnerConnection service = conn.getConnection();
        return service;
    }
    
    /**
     * Helper to retrieve PersistenceUnitInfo from the application context.
     * @param ctx
     * @return PersistenceUnitInfo object of the context
     */
    public static PersistenceUnitInfo getPUInfoFromAppContext(ApplicationContext ctx) {
        Map<String, EntityManagerFactoryInfo> map = ctx.getBeansOfType(EntityManagerFactoryInfo.class);
        if (map == null || map.isEmpty()) {
            return null;
        }
        EntityManagerFactoryInfo emfi = map.get(map.keySet().iterator().next()); // injected EMF
        return emfi.getPersistenceUnitInfo();
    }
        
    /**
     * Retrieve the name of the configured persistence unit in the application context.
     * @param ctx application context to retrieve the persistence unit from
     */
    public static String getPersistenceUnitName(ApplicationContext ctx) {
        Map<String, EntityManagerFactoryInfo> map = ctx.getBeansOfType(EntityManagerFactoryInfo.class);
        if (map.isEmpty()) {
            throw new RuntimeException("Could not retrieve persistence unit name. No suitable EntityManagerFactory was found.");
        }
        EntityManagerFactoryInfo emf = map.get(map.keySet().iterator().next()); // injected EMF

        return emf.getPersistenceUnitName();
    }

    @AfterTest(alwaysRun = true)
    protected void testTeardown() throws Exception {
        cleanSchema();
    }

    protected void cleanSchema() throws Exception {
        ObjectManager om = (ObjectManager) entityManager.getDelegate();
        // Get the EntityManager's PartnerConnection
        ConnectionFactory connFactory = om.getStoreManager().getConnectionManager().lookupConnectionFactory("force");
        ForceManagedConnection mconn = (ForceManagedConnection) connFactory.createManagedConnection(null, null);
        // cleanup schema via destructive changes
        SfdcTestingUtil.cleanSchema(mconn);
    }

}
