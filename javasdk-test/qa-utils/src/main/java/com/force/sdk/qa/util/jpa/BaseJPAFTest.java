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

package com.force.sdk.qa.util.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.persistence.*;

import com.force.sdk.connector.ForceConnectorUtils;
import org.datanucleus.ObjectManager;
import org.datanucleus.ObjectManagerImpl;
import org.datanucleus.store.connection.ConnectionFactory;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.*;

import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.jpa.ForceManagedConnection;
import com.force.sdk.jpa.ForceStoreManager;
import com.force.sdk.jpa.schema.ForceStoreSchemaHandler;
import com.force.sdk.jpa.table.TableImpl;
import com.force.sdk.qa.util.*;
import com.google.inject.internal.Lists;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

/**
 * Create a test org and establish the test context based on the test org. This base class
 * uses the standard javax.Persistence implementation to bootstrap the persistence context.
 * 
 * @author Dirk Hain
 */
public abstract class BaseJPAFTest implements ITest {

    public EntityManager em;
    protected EntityManagerFactory emfac;
    protected PartnerConnection service;

    /**
     * Default initializer for Force.com SDK tests. A test context will be established based on 
     * the force-sdk-test.properties file defined by the user.
     * 
     * @throws Exception 
     */
    @BeforeClass
    protected final void initialize() throws Exception {
        System.setProperty("jpaConnection", getConnectionUrl());

        createStaticEntityMangers();
        populateTestContext(getTestName(), UserInfo.loadFromPropertyFile(PropsUtil.FORCE_SDK_TEST_NAME));
        ForceServiceConnector connector = new ForceServiceConnector(PropsUtil.FORCE_SDK_TEST_NAME);
        service = connector.getConnection();
    }

    private String getConnectionUrl() throws IOException {
        URL propsFileUrl = ForceConnectorUtils.class.getResource("/" + "force-sdk-test.properties");
        if (propsFileUrl == null) {
            throw new IllegalArgumentException("force-sdk-test.properties is not on classpath.");
        }

        Properties connectorProps = new Properties();
        InputStream is = null;
        try {
            is = propsFileUrl.openStream();
            connectorProps.load(is);
        } finally {
            if (is != null) is.close();
        }

        return connectorProps.getProperty("url");
    }

    protected void createStaticEntityMangers() throws Exception {
        emfac = Persistence.createEntityManagerFactory(TestContext.get().getPersistenceUnitName());
        em = emfac.createEntityManager();
    }
        
    /**
     * Any test unspecific cleaning that needs to be done.
     */
    @AfterMethod
    protected void testCleanup() throws Exception {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
    
    @AfterTest(alwaysRun = true)
    protected void testTeardown() throws Exception {
        cleanSchema();
        System.clearProperty("jpaConnection");
    }

    protected void cleanSchema() throws Exception {
        ObjectManager om = (ObjectManager) em.getDelegate();

        // Get the EntityManager's PartnerConnection
        ConnectionFactory connFactory = om.getStoreManager().getConnectionManager().lookupConnectionFactory("force");
        ForceManagedConnection mconn = (ForceManagedConnection) connFactory.createManagedConnection(null, null);
        SfdcSchemaUtil.cleanSchema(mconn);
    }
    
    /**
     * Helper to extract UserInfo from the current test context by either returning the UserInfo
     * object or extracting the data from the test properties that get loaded from force-sdk-test.properties.
     * @return {@link UserInfo}
     */
    public static UserInfo getDefaultUserInfoFromContext() {
        TestContext ctx = TestContext.get();
        if (ctx.getUserInfo() != null) {
            return ctx.getUserInfo();
        }
        String username, password, authEndpoint = null;
        Properties props = ctx.getTestProps();
        username = props.getProperty(PropsUtil.FORCE_USER_PROP);
        password = props.getProperty(PropsUtil.FORCE_PWD_PROP);
        authEndpoint = props.getProperty(PropsUtil.FORCE_PROT_PROP) + "://" + props.getProperty(PropsUtil.FORCE_EP_PROP)
            + "/services/Soap/u/" + props.getProperty(PropsUtil.FORCE_APIV_PROP);
        return new UserInfo(null, null, username, password, authEndpoint);
    }
    
    /**
     * Helper to get a connection using the default test properties in force-sdk-test.properties.
     * 
     * @return a PartnerConnection to the default test org
     */
    public static PartnerConnection getDefaultServiceFromTestProperties() throws ConnectionException {
        
        UserInfo info = getDefaultUserInfoFromContext();
        ForceConnectorConfig conf = new ForceConnectorConfig();
        conf.setUsername(info.getUserName());
        conf.setPassword(info.getPassword());
        conf.setAuthEndpoint(info.getServerEndpoint());
        ForceServiceConnector conn = new ForceServiceConnector(conf);
        PartnerConnection service = conn.getConnection();
        Assert.assertNotNull(service.login(info.getUserName(), info.getPassword()).getSessionId(),
                "Unable to log into default org.");
        return service;
    }
    
    
    /**
     * Helper to obtain a service connection from an existing test context.
     * This method is invoked if multiple tests are run on the same
     * generated org.
     * 
     * @return PartnerConnection to the last generated org
     */
    public static PartnerConnection getServiceFromTestContext(TestContext ctx) throws ConnectionException {
        String username, password, authEndpoint = null;
        username = ctx.getUserInfo().getUserName();
        password = ctx.getUserInfo().getPassword();
        authEndpoint = ctx.getUserInfo().getServerEndpoint();
        ForceConnectorConfig conf = new ForceConnectorConfig();
        conf.setUsername(username);
        conf.setPassword(password);
        conf.setAuthEndpoint(authEndpoint);
        ForceServiceConnector conn = new ForceServiceConnector(conf);
        PartnerConnection service = conn.getConnection();
        Assert.assertNotNull(service.login(username, password).getSessionId(), "Unable to log into default org using "
                + username + "/" + password + " on " + authEndpoint);
        return service;
    }

    
    // Helpers to add test data for JPA tests
    protected void addTestDatumInTx(Object object) {
        addTestDataInTx(Lists.newArrayList(object));
    }

    protected <T> void addTestDataInTx(List<T> objects) {
        // Add some data here
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            for (T obj : objects) {
                em.persist(obj);
                em.flush();
                
            }
            tx.commit();
            tx = null;
        } finally {
            if (tx != null) {
                tx.rollback();
            }
        }
    }
    
    /**
     * Helper to delete all salesforce instances of a specific entity.
     * @param entity entity type to delete
     */
    protected void deleteAll(Class<?> entity) {
        deleteAll(entity.getSimpleName());
    }
    
    protected void deleteAll(String entityName) {
        deleteAll(entityName, "");
    }
    
    protected void deleteAll(String entityName, String where) {
        EntityTransaction tx = em.getTransaction();
        if (tx.isActive()) {
            em.joinTransaction();
        } else {
            tx = em.getTransaction();
            tx.begin();
        }
        try {
            em.createQuery("delete from " + entityName + where).executeUpdate();
            tx.commit();
            tx = null;
        } finally {
            if (tx != null) {
                tx.rollback();
            }
        }
    }
    
    @Override
    public String getTestName() {
        return getClass().getName();
    }

    /**
     * Helper to populate this threads test context.
     * 
     * @param uinfo
     */
    public static void populateTestContext(String testname, UserInfo uinfo) throws IOException {
        TestContext tc = TestContext.get();
        tc.setUserInfo(testname, uinfo);
    }

    private TableImpl getTable(EntityManager emm, Class<?> entity) {
        ForceStoreSchemaHandler schemaHandler =
            (ForceStoreSchemaHandler) ((ObjectManagerImpl) emm.getDelegate()).getStoreManager().getSchemaHandler();
        return schemaHandler.getTable(
                ((ForceStoreManager) schemaHandler.getStoreManager()).getMetaDataManager().getMetaDataForClass(entity, null));
    }
    
    public String getTableName(EntityManager emm, Class<?> entity) {
        return getTable(emm, entity).getTableName().getForceApiName();
    }
    
    public String getFieldName(EntityManager emm, Class<?> entity, String field) {
        return getTable(emm, entity).getColumnByJavaName(field).getFieldName();
    }
    
    public String getRelationshipName(EntityManager emm, Class<?> entity, String field) {
        return getTable(emm, entity).getColumnByJavaName(field).getForceApiRelationshipName();
    }
    
}
