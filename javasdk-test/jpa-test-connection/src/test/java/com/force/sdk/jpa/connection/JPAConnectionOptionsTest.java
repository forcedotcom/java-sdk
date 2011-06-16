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

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import javax.persistence.*;

import org.testng.annotations.Test;

/**
 * Tests setting various options on JPA connections.
 *
 * @author Tim Kral
 */
public class JPAConnectionOptionsTest extends BaseJPAConnectionTest {

    @Test
    public void testSetOptionsFromConnUrlPropertyFile() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("connUrlWithOptionsPropFile");
        EntityManager em = emf.createEntityManager();
        
        verifyEntityManager(em);
        verifyClientId(em);
        verifyTimeout(em, 10000);
    }
    
    @Test
    public void testSetOptionsFromUserInfoPropertyFile() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("userInfoWithOptionsPropFile");
        EntityManager em = emf.createEntityManager();
        
        verifyEntityManager(em);
        verifyClientId(em);
        verifyTimeout(em, 10000);
    }
    
    @Test
    public void testSetTimeoutFromPersistenceProperty() throws Exception {
        Map<String, Integer> timeoutMap = Collections.singletonMap("datanucleus.datastoreReadTimeout", 10000);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("userInfoPersistenceProp", timeoutMap);
        EntityManager em = emf.createEntityManager();
        
        verifyEntityManager(em);
        verifyTimeout(em, 10000);
    }
    
    @Test
    public void testUseTimeoutFromPersistencePropertyBeforePropertyFile() throws Exception {
        Map<String, Integer> timeoutMap = Collections.singletonMap("datanucleus.datastoreReadTimeout", 20000);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("userInfoWithOptionsPropFile", timeoutMap);
        EntityManager em = emf.createEntityManager();
        
        verifyEntityManager(em);
        verifyTimeout(em, 20000); // JPA should use the property in persistence.xml before the properties file
    }
    
    private void verifyClientId(EntityManager em) throws Exception {
        Properties projectProps = new Properties();
        projectProps.load(ClassLoader.getSystemResource("sdk.properties").openStream());
        String sdkVersion = projectProps.getProperty("force.sdk.version");
        String clientId = String.format("javasdk-%s", sdkVersion);
        assertEquals(getPartnerConnection(em).getCallOptions().getClient(), clientId);
        assertEquals(getMetadataConnection(em).getCallOptions().getClient(), clientId);
    }
    
    private void verifyTimeout(EntityManager em, int expectedTimeout) {
        assertEquals(getPartnerConnection(em).getConfig().getReadTimeout(), expectedTimeout);
    }
}
