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

import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceServiceConnector;
import org.datanucleus.OMFContext;
import org.datanucleus.store.connection.AbstractConnectionFactory;
import org.datanucleus.store.connection.ManagedConnection;

import java.util.Map;

/**
 * 
 * Factory for creating connections to the Force.com service.
 *
 * @author Fiaz Hossain
 */
public class ForceConnectionFactory extends AbstractConnectionFactory {
    /**
     * Factory constructor for Force.com connections.
     * 
     * @param omfContext The OMF context
     * @param resourceType Type of resource (tx, nontx)
     */
    public ForceConnectionFactory(OMFContext omfContext, String resourceType) {
        super(omfContext, resourceType);
    }

    /**
     * Instantiates the connections to the Force.com service. It either reads the configs
     * from thread local (which would have been set from OAuth) or it gets the config from
     * the store manager.
     * {@inheritDoc}
     */
    @Override
    public ManagedConnection createManagedConnection(Object poolKey, Map transactionOptions) {
        ForceStoreManager storeManager = (ForceStoreManager) omfContext.getStoreManager();
            
        ForceServiceConnector connector = new ForceServiceConnector();
        
        // A ConnectorConfig might have been set via OAuth in which case we should be
        // using that.
        ForceConnectorConfig tlConfig;
        if ((tlConfig = ForceServiceConnector.getThreadLocalConnectorConfig()) != null) {
            connector.setConnectorConfig(tlConfig);
        } else {
            connector.setConnectorConfig(storeManager.getConfig());
        }
        
        connector.setConnectionName(omfContext.getPersistenceConfiguration().getStringProperty("force.ConnectionName"));
        connector.setClientId(ForceServiceConnector.API_USER_AGENT);
        connector.setTimeout(omfContext.getPersistenceConfiguration().getIntProperty("datanucleus.datastoreReadTimeout"));
        connector.setSkipCache(omfContext.getPersistenceConfiguration()
                                            .getBooleanProperty("force.skipConfigCache", false /* resultIfNotSet */));
        
        return new ForceManagedConnection(connector);
    }
}
