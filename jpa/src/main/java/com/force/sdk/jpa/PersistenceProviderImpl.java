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

package com.force.sdk.jpa;

import static javax.jdo.Constants.PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS;

import java.util.*;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.datanucleus.jpa.EntityManagerFactoryImpl;
import org.datanucleus.jpa.exceptions.*;

/**
 * 
 * This class allows us to provide the ForceEntityManagerFactory.
 *
 * @author Jill Wetzler
 */
public class PersistenceProviderImpl extends org.datanucleus.jpa.PersistenceProviderImpl {
    
    private static final String PERSISTENCE_PROVIDER_PROPERTY = "javax.persistence.provider";

    /**
     * Method to create an EntityManagerFactory when running in J2EE.
     * The container will have parsed the persistence.xml files to provide this PersistenceUnitInfo.
     * @param unitInfo The "persistence-unit"
     * @param properties EntityManagerFactory properties to override those in the persistence unit
     * @return The EntityManagerFactory
     */
    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo unitInfo, Map properties) {
        try {
            EntityManagerFactoryImpl emf = new ForceEntityManagerFactory(unitInfo, getOverrideMap(properties));
            return emf;
        } catch (NotProviderException npe) {
            return null;
        } catch (NoPersistenceUnitException npue) {
            return null;
        } catch (NoPersistenceXmlException npxe) {
            return null;
        }
    }

    /**
     * Method to create an EntityManagerFactory when running in J2SE.
     * @param unitName Name of the "persistence-unit"
     * @param properties EntityManagerFactory properties to override those in the persistence unit
     * @return The EntityManagerFactory
     */
    @Override
    public EntityManagerFactory createEntityManagerFactory(String unitName, Map properties) {
        try {
            EntityManagerFactoryImpl emf = new ForceEntityManagerFactory(unitName, getOverrideMap(properties));
            return emf;
        } catch (NotProviderException npe) {
            return null;
        } catch (NoPersistenceUnitException npue) {
            return null;
        } catch (NoPersistenceXmlException npxe) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map getOverrideMap(Map properties) {
        Map<Object, Object> m = new HashMap<Object, Object>(properties != null ? properties : Collections.EMPTY_MAP);
        m.put(PERSISTENCE_PROVIDER_PROPERTY, org.datanucleus.jpa.PersistenceProviderImpl.class.getName());
        m.put(PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS, "com.force.sdk.jdo.ForceJDOPersistenceManagerFactory");
        return m;
    }
}
