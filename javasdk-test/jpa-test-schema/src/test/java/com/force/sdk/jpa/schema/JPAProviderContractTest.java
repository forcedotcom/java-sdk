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

import java.util.*;

import javax.persistence.*;
import javax.persistence.spi.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.schema.entities.ProviderTestEntity;
import com.force.sdk.qa.util.TestContext;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Tests for provider contracts. We test mostly DN but we should have automation for it because we 
 * do change the DN implementation often. These tests only cover bootstrapping in a Java SE 
 * environment.
 *
 * @author Dirk Hain
 */
public class JPAProviderContractTest extends SchemaBaseTest {

    private static final String PUNAME = "ProviderContract";
    private static final String FAULTY_PU = "someFaultyProviderName";
    private static final String INVALID_PROVIDER_EX = "Invalid or inaccessible explicit provider class: " + FAULTY_PU;
    
    @Test
    /**
     * Provider property test.
     * Tests that provider properties can be supplied via a persistence property besides being specified in persistence.xml.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults A test entity should be persisted properly after reading correct provider proerties.
     */
    public void testSupplyProviderProperty() {
        dynamicOrgConfig.put("javax.persistence.provider", FAULTY_PU);
        EntityManagerFactory emf;
        try {
            emf = Persistence.createEntityManagerFactory(PUNAME, dynamicOrgConfig);
            Assert.fail("CreateEMF should have failed.");
        } catch (PersistenceException pe) {
            Assert.assertEquals(pe.getMessage(), INVALID_PROVIDER_EX, "Wrong exception caught.");
        }
        //now remove the faulty PU name from the PU properties which will default to our provider name if no provider is supplied
        dynamicOrgConfig.remove("javax.persistence.provider");
        emf = Persistence.createEntityManagerFactory(PUNAME, dynamicOrgConfig);
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        ProviderTestEntity pte = new ProviderTestEntity();
        em.persist(pte);
        tx.commit();
        Assert.assertNotNull(pte.getId(), "Entity was not persisted.");
    }
    
    @Test
    /**
     * Provider service test.
     * Tests that our provider provides a persistence service which boils down to a file
     * in /META-INF/services that contains our default provider implementation (spec 9.2).
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Exactly one provider implementation is loaded.
     */
    public void testServiceProviderImpl() {
        
        final String svcProvImpl = "com.force.sdk.jpa.PersistenceProviderImpl";

        ServiceLoader<PersistenceProvider> loadr = ServiceLoader.load(PersistenceProvider.class);
        Iterator<PersistenceProvider> iter = loadr.iterator();
        int count = 0;
        boolean foundCorrectProvider = false;
        while (iter.hasNext()) {
            if (iter.next().getClass().getName().equals(svcProvImpl)) {
                count++;
                foundCorrectProvider = true;
            }
        }
        Assert.assertEquals(count, 1, "Found more than one persitence provider.");
        Assert.assertTrue(foundCorrectProvider, "No service matching " + svcProvImpl + " was found.");
    }
    
    @Test
    /**
     * Test available providers and system properties for bootstrapping.
     * Asserts that all specified providers are resolved and accessible through {@link PersistenceProviderResolverHolder}. Also 
     * tests that system properties can be used for bootstrapping.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults A test entity object is correctly loaded after reading 2 provider implementations.
     */
    public void testAvailableProviders() throws ConnectionException {
        PersistenceProviderResolver ppr = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
        List<PersistenceProvider> providers = ppr.getPersistenceProviders();
        
        // DataNucleus and force JPA provider expected
        Assert.assertEquals(providers.size(), 2, "Wrong number of providers were found.");
        
        Iterator<PersistenceProvider> iter = providers.iterator();
        PersistenceProvider toUse = null;
        while (iter.hasNext()) {
            PersistenceProvider pp = iter.next();
            if (!(pp instanceof com.force.sdk.jpa.PersistenceProviderImpl
                    || pp instanceof org.datanucleus.jpa.PersistenceProviderImpl)) {
                Assert.fail("Found provider of type " + pp.getClass().getName());
            }
            if (pp instanceof com.force.sdk.jpa.PersistenceProviderImpl) {
                toUse = pp;
            }
        }

        // use java prop to test without props map
        System.setProperty("force." + PUNAME + ".url", createConnectionUrl(TestContext.get().getUserInfo()));
        
        EntityManagerFactory emf = toUse.createEntityManagerFactory(PUNAME, new HashMap<String, Object>());
        emf = toUse.createEntityManagerFactory(PUNAME, null);
        Map<String, Object> puProps = emf.getProperties();
        String manType = (String) puProps.get("datanucleus.storeManagerType".toLowerCase());
        Assert.assertEquals(manType, "force", "Wrong StoreManagerType was returned.");
        String connName = (String) puProps.get("force.ConnectionName".toLowerCase());
        Assert.assertEquals(connName, "force-sdk-test", "Wrong connection name was returned.");
        
        EntityManager eman = emf.createEntityManager();
        EntityTransaction tx = eman.getTransaction();
        tx.begin();
        ProviderTestEntity pte = new ProviderTestEntity();
        eman.persist(pte);
        Assert.assertTrue(emf.getPersistenceUnitUtil().isLoaded(pte), "Provider did not load entity.");
        tx.rollback();
    }
}
