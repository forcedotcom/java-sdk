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

package com.force.sdk.jdo;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.jdo.JDOPersistenceManager;
import org.datanucleus.util.NucleusLogger;

/**
 * 
 * Factory for the JDOPersistenceManager.  Overriding so we can provide our own PersistenceManager
 *
 * @author Fiaz Hossain
 */
public class ForceJDOPersistenceManagerFactory extends org.datanucleus.jdo.JDOPersistenceManagerFactory {

    /** Cache of PMF keyed by the name. Only used when having single-PMF property enabled. */
    private static Map<String, ForceJDOPersistenceManagerFactory> pmfByName;
    
    /**
     * Default factory constructor.
     */
    public ForceJDOPersistenceManagerFactory() {
    }

    /**
     * 
     * Factory constructor that takes in a set of overriding properties.
     * 
     * @param props  Persistent properties
     */
    public ForceJDOPersistenceManagerFactory(Map props) {
        super(props);
    }

    /**
     * Return a new PersistenceManagerFactory with options set according to the given Properties.
     * @param overridingProps The Map of properties to initialize the PersistenceManagerFactory with.
     * @return A PersistenceManagerFactory with options set according to the given Properties.
     * @see javax.jdo.JDOHelper#getPersistenceManagerFactory(java.util.Map)
     */
    public static synchronized PersistenceManagerFactory getPersistenceManagerFactory(Map overridingProps) {
        // Extract the properties into a Map allowing for a Properties object being used
        Map overridingMap = null;
        if (overridingProps instanceof Properties) {
            // Make sure we handle default properties too (SUN Properties class oddness)
            overridingMap = new HashMap();
            for (Enumeration e = ((Properties) overridingProps).propertyNames(); e.hasMoreElements();) {
                String param = (String) e.nextElement();
                overridingMap.put(param, ((Properties) overridingProps).getProperty(param));
            }
        } else {
            overridingMap = overridingProps;
        }

        return createPersistenceManagerFactory(overridingMap);
    }
    
    /**
     * Convenience method to create the PMF, check whether we should hand out a singleton, and if all
     * ok then freeze it for use.
     * @param props The properties
     * @return The PMF to use
     */
    protected static synchronized ForceJDOPersistenceManagerFactory createPersistenceManagerFactory(Map props) {
        // Create the PMF and freeze it (JDO spec $11.7)
        final ForceJDOPersistenceManagerFactory pmf = new ForceJDOPersistenceManagerFactory(props);

        Boolean singleton =
            pmf.getOMFContext().getPersistenceConfiguration().getBooleanObjectProperty("datanucleus.singletonPMFForName");
        if (singleton != null && singleton) {
            // Check on singleton pattern. Would be nice to know the name of the PMF before creation
            // but not possible without restructuring parse code, so leave as is for now
            if (pmfByName == null) {
                pmfByName = new HashMap<String, ForceJDOPersistenceManagerFactory>();
            }
            String name = pmf.getName();
            if (name == null) {
                name = pmf.getPersistenceUnitName();
            }
            if (name != null) {
                if (pmfByName.containsKey(name)) {
                    pmf.close();
                    NucleusLogger.PERSISTENCE.warn("Requested PMF of name \"" + name
                        + "\" but already exists and using singleton pattern, so returning existing PMF");
                    return pmfByName.get(name);
                }
            }
            pmfByName.put(name, pmf);
        }

        // Freeze the PMF for use (establishes connection to datastore etc)
        pmf.freezeConfiguration();

        return pmf;
    }
    
    /**
     * Construct a {@link JDOPersistenceManager}.  Override if you want to construct a subclass instead.
     */
    @Override
    protected JDOPersistenceManager newPM(org.datanucleus.jdo.JDOPersistenceManagerFactory jdoPmf,
            String userName, String password) {
        
        return new com.force.sdk.jdo.ForceJDOPersistenceManager(jdoPmf, userName, password);
    }
}
