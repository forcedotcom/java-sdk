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

import com.force.sdk.connector.ForceConnectorTestUtils;
import org.testng.annotations.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Collections;
import java.util.Map;

/**
 * Basic tests for JPA connections.
 * Tests that the Force.com JPA layer can get connections with various configurations.
 * 
 * @author Tim Kral
 */
public class BasicJPAConnectionTest extends BaseJPAConnectionTest {

    // NOTE: This test is not going to pass in STS.  You have to execute from the command line.
    @Test
    public void testConnFromConnUrlEnvironmentVariable() throws Exception {
        // FORCE_CONNURLENVVAR_URL is defined in pom.xml
        // See connUrlEnvVar persistence-unit in persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("connUrlEnvVar");
        verifyEntityManager(emf.createEntityManager());
    }
    
    // NOTE: This test is not going to pass in STS.  You have to execute from the command line.
    @Test
    public void testConnFromCustomEnvironmentVariable() throws Exception {
        // FORCE_CONNURLENVVAR_URL is defined in pom.xml
        // See connUrlEnvVar2 persistence-unit in persistence.xml. The config is read as : ${FORCE_CONNURLENVVAR_URL}
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("connUrlEnvVar2");
        verifyEntityManager(emf.createEntityManager());
    }

    @Test
    public void testConnFromConnUrlJavaProperty() throws Exception {
        // See connUrlJavaProp persistence-unit in persistence.xml
        try {
            System.setProperty("force.connUrlJavaProp.url", createConnectionUrl());
            
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("connUrlJavaProp");
            verifyEntityManager(emf.createEntityManager());
        } finally {
            System.clearProperty("force.connUrlJavaProp.url");
        }
    }
    
    @Test
    public void testConnFromCustomJavaProperty() throws Exception {
        // See connUrlJavaProp persistence-unit in persistence.xml
        try {
            System.setProperty("custom.url", createConnectionUrl());

            EntityManagerFactory emf = Persistence.createEntityManagerFactory("connUrlJavaProp2");
            verifyEntityManager(emf.createEntityManager());
        } finally {
            System.clearProperty("custom.url");
        }
    }

    @Test
    public void testConnFromConnUrlPersistenceProperty() throws Exception {
        // See connUrlPersistenceProp persistence-unit in persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("connUrlPersistenceProp");
        verifyEntityManager(emf.createEntityManager());
    }

    @Test
    public void testConnFromConnUrlClasspathPropertyFile() throws Exception {
        // See connUrlPropFile persistence-unit in persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("connUrlPropFile");
        verifyEntityManager(emf.createEntityManager());
    }
    
    @Test
    public void testConnFromConnUrlCliforcePropertyFile() throws Exception {
        // See connUrlCliforcePropFile persistence-unit in persistence.xml
        ForceConnectorTestUtils.createCliforceConn("connUrlCliforcePropFile", createConnectionUrl());
            
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("connUrlCliforcePropFile");
        verifyEntityManager(emf.createEntityManager());
    }
    
    @Test
    public void testConnFromFullConnUrlPersistenceProperty() throws Exception {
        // See connUrlPersistenceProp persistence-unit in persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("fullConnUrlPersistenceProp");
        verifyEntityManager(emf.createEntityManager());
    }

    @Test
    public void testConnFromUserInfoPersistenceProperty() throws Exception {
        // See userInfoPersistenceProp persistence-unit in persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("userInfoPersistenceProp");
        verifyEntityManager(emf.createEntityManager());
    }

    @Test
    public void testConnFromUserInfoClasspathPropertyFile() throws Exception {
        // See userInfoPropFile persistence-unit in persistence.xml
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("userInfoPropFile");
        verifyEntityManager(emf.createEntityManager());
    }
    
    @Test
    public void testCustomConnectionName() throws Exception {
        Map<String, String> customConnNameMap = Collections.singletonMap("force.ConnectionName", "userInfoPropFile");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("badUserInfoPropFile", customConnNameMap);
        
        // We've provided a custom connection name that should lookup the good
        // configuration in userInfoPropFile.properties and not use the bad
        // configuration in badUserInfoPropFile.properties
        verifyEntityManager(emf.createEntityManager());
    }
}
