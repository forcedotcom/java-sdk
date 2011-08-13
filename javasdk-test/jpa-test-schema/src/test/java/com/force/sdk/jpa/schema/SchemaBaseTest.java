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

package com.force.sdk.jpa.schema;

import java.lang.reflect.Field;
import java.util.*;

import org.testng.annotations.*;

import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.qa.util.TestContext;
import com.force.sdk.qa.util.UserInfo;
import com.force.sdk.qa.util.jpa.BaseJPAFTest;
import com.sforce.ws.ConnectionException;

/**
 *
 * Tests related to malformed schemas and the exceptions that the force JPA provider throws.
 *
 * @author Dirk Hain
 */
public class SchemaBaseTest extends BaseJPAFTest {

    public static final String NAME_SEPARATOR = "__";

    protected final Map<String, Object> dynamicOrgConfig = new HashMap<String, Object>();

    @AfterMethod(alwaysRun = true)
    @Override
    protected void testCleanup() throws Exception {
        super.testCleanup();
        cleanSchema();
    }

    @AfterTest
    @Override
    protected void testTeardown() throws Exception {
        //don't let parent class cleanSchema, we're doing that in the testCleanup method
    }
    
    @BeforeClass
    public void loadPersistenceUnit() throws Exception {
        UserInfo info = BaseJPAFTest.getDefaultUserInfoFromContext();
        ForceConnectorConfig cc = new ForceConnectorConfig();
        cc.setUsername(info.getUserName());
        cc.setPassword(info.getPassword());
        cc.setAuthEndpoint(info.getServerEndpoint());
        BaseJPAFTest.populateTestContext(getTestName(), info);
    }

    /**
     * Any test unspecific cleaning that needs to be done.
     */
    @AfterClass
    protected void classTearDown() {
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
    
    /**
     * Returns true if the type defines a field with the same name. The comparison is NOT case sensitive.
     * @param type type to analyze
     * @param fieldName field name to look for
     */
    public static <T extends Class> boolean typeDefinesField(T type, String fieldName) {
        Field[] definedFields = type.getDeclaredFields();
        Set<String> fields = new HashSet<String>();
        for (Field f : definedFields) {
            fields.add(f.getName());
        }
        
        return fields.contains(fieldName);
    }
    
    /**
     * Method removes the JDO generated fields and returns the remaining fields in a SET for 
     * comparison.
     * @param <T>
     * @param entity
     * @return
     */
    public static <T extends Class> Set<Field> removeJDOFields(T entity) {
        Set<Field> entityFields = new HashSet<Field>();
        Field[] fields = entity.getDeclaredFields();
        for (Field f : fields) {
            if (!f.getName().contains("jdo")) {
                entityFields.add(f);
            }
        }
        return entityFields;
    }
    
    /**
     * Helper to retrieve the namespace of the org stored in the current test context.
     * @return String containing the namespace
     */
    public static String getNamespaceFromCtx() throws ConnectionException {
        UserInfo info = TestContext.get().getUserInfo();
        ForceConnectorConfig config = new ForceConnectorConfig();
        config.setAuthEndpoint(info.getServerEndpoint());
        config.setUsername(info.getUserName());
        config.setPassword(info.getPassword());
        ForceServiceConnector connector = new ForceServiceConnector(config);
        return connector.getNamespace();
    }
    
    public static String getObjectApiPrefix() throws ConnectionException {
        String namespace = getNamespaceFromCtx();
        if (namespace == null || "".equals(namespace)) return "";
        
        return namespace + NAME_SEPARATOR;
    }
    
    /**
     * Helper to create connection URL (copied from {@link BaseJPAConnectionTest}.
     * @param info userinfo object to create the connection url from
     * @return connection URL
     */
    public static String createConnectionUrl(UserInfo info) {
        // Strip out protocol, if it exists
        String[] parsedEndPoint = info.getServerEndpoint().split("://");
        
        StringBuffer sb = new StringBuffer();
        sb.append("force://").append(parsedEndPoint[parsedEndPoint.length - 1])
            .append(";user=").append(info.getUserName())
            .append(";password=").append(info.getPassword());
        
        return sb.toString();
    }

}
