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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.*;

import javax.persistence.*;

import org.datanucleus.exceptions.NucleusException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sforce.soap.partner.fault.ApiFault;

/**
 * Negative tests for JPA connections. 
 * Tests errors for bad JPA connection configurations.
 *
 * @author Tim Kral
 */
public class NegativeJPAConnectionTest extends BaseJPAConnectionTest {

    @Test
    public void testNoConnConfig() throws Exception {
        
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("noConnConfig");
            getPartnerConnection(emf.createEntityManager());
        } catch (PersistenceException expected) {
            Throwable rootCause = getRootCause(expected);
            assertTrue(rootCause.getMessage().contains("No state was found to construct a connection"),
                    "Unexpected exception message: " + rootCause.getMessage());
            assertTrue(rootCause.getMessage()
                    .contains("create a classpath properties file, environment variable"
                                + " or java property for the name 'noConnConfig'"),
                    "Unexpected exception message: " + rootCause.getMessage());
        }
    }
    
    @DataProvider
    protected Object[][] connUrlWithBadPropertyProvider() {
        return new Object[][] {
            {"force://url;user=;password=password", "user", null},
            {"force://url;user=user;password=", "password", null},
            {"force://url;user=user;password=password;timeout=abc", "timeout", "abc"},
        };
    }
    
    @Test(dataProvider = "connUrlWithBadPropertyProvider")
    public void testConnUrlWithBadProperty(String connectionUrl, String badProperty, String badValue) throws Exception {
        Map<String, String> connUrlMap = Collections.singletonMap("datanucleus.ConnectionUrl", connectionUrl);
        
        try {
            Persistence.createEntityManagerFactory("noConnConfig", connUrlMap);
        } catch (PersistenceException expected) {
            Throwable rootCause = getRootCause(expected);
            assertTrue(rootCause.getMessage().contains(badProperty),
                        "Could not find " + badProperty + " in: " + rootCause.getMessage());
            
            if (badValue != null)
                assertTrue(rootCause.getMessage().contains(badValue),
                            "Could not find " + badValue + " in: " + rootCause.getMessage());
        }
    }
    
//    TODO: Implement me.  If we add a persistence-unit with a blank name
//    to persistence.xml, then this messes up later tests.
//    @Test
//    public void testEmptyPersistenceUnitName() throws Exception {
//        try {
//            Persistence.createEntityManagerFactory("");
//        } catch (PersistenceException expected) {
//            // TODO: Implement me.
//        }
//    }
    
    @Test
    public void testNoPersistenceUnit() throws Exception {
        try {
            Persistence.createEntityManagerFactory("noPersistenceUnit"); // Doesn't exist in persistence.xml
        } catch (PersistenceException expected) {
            assertTrue(expected.getMessage().contains("\"noPersistenceUnit\""),
                    "Unexpected exception messsage: " + expected.getMessage());
            
            Throwable rootCause = getRootCause(expected);
            assertTrue(rootCause instanceof NullPointerException);
        }
    }
    
    @DataProvider
    protected Object[][] missingPersistencePropertyProvider() {
        return new Object[][] {
            {"datanucleus.storeManagerType", "There is no available StoreManager"},
            {"datanucleus.ConnectionUrl", "No state was found to construct a connection"},
            {"datanucleus.ConnectionUserName", "ForceConnectorConfig must have a Username"},
            {"datanucleus.ConnectionPassword", "Invalid username, password, security token; or user locked out."},
        };
    }
    
    @Test(dataProvider = "missingPersistencePropertyProvider")
    public void testMissingUserInfoPersistenceProperty(String missingPersistenceProperty, String expectedMessage) {
        Map<String, String> persistencePropMap = new HashMap<String, String>();
        persistencePropMap.put("datanucleus.storeManagerType", "force");
        persistencePropMap.put("datanucleus.ConnectionUrl", userInfo.getServerEndpoint());
        persistencePropMap.put("datanucleus.ConnectionUserName", userInfo.getUserName());
        persistencePropMap.put("datanucleus.ConnectionPassword", userInfo.getPassword());
        persistencePropMap.put("force.skipConfigCache", "true"); // Make sure we're testing independent of cached state
        
        persistencePropMap.put(missingPersistenceProperty, null);
        
        try {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("noConnConfig", persistencePropMap);
            getPartnerConnection(emf.createEntityManager());
            fail("EntityManagerFactory connection should have failed with message " + expectedMessage);
        } catch (NucleusException expected) {
            Throwable rootCause = getRootCause(expected);
            String exceptionMessage = rootCause.getMessage();
            if (rootCause instanceof ApiFault) exceptionMessage = ((ApiFault) rootCause).getExceptionMessage();
            
            assertTrue(exceptionMessage.contains(expectedMessage), "Unexpected exception message: " + exceptionMessage);
            
        // In some cases we may get a PersistenceException which is fine
        // so long as the exception message matches
        } catch (PersistenceException expected2) {
            Throwable rootCause = getRootCause(expected2);
            String exceptionMessage = rootCause.getMessage();
            if (rootCause instanceof ApiFault) exceptionMessage = ((ApiFault) rootCause).getExceptionMessage();
            
            assertTrue(exceptionMessage.contains(expectedMessage), "Unexpected exception message: " + exceptionMessage);
        }
    }
    
    private Throwable getRootCause(Throwable thrown) {
        if (thrown == null) return null;

        // Go down the stack trace to the last throwable
        Throwable t = thrown;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        
        return t;
    }
}
