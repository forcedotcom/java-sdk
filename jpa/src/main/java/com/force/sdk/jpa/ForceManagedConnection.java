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

import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.store.connection.AbstractManagedConnection;

import com.force.sdk.connector.ForceServiceConnector;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Managed connection class that delegates to the ForceServiceConnector.
 *
 * @author Fiaz Hossain
 */
public class ForceManagedConnection extends AbstractManagedConnection {

    private final ForceServiceConnector connector;
    
    /**
     * Constructor for a managed connection. 
     * 
     * @param connector  a ForceServiceConnector object configured for the org
     *                   that the application will run against
     */
    public ForceManagedConnection(ForceServiceConnector connector) {
        this.connector = connector;
    }

    /**
     * Closes all API connections (SOAP, metadata, and bulk) and resets configs.
     */
    @Override
    public void close() {
        this.connector.close();
    }

    /**
     * Get the PartnerConnection object, connection will be lazily instantiated.
     * 
     * @return a PartnerConnection to the Force.com SOAP API
     */
    @Override
    public Object getConnection() {
        try {
            return connector.getConnection();
        } catch (ConnectionException ce) {
            throw new NucleusException(ce.getMessage(), ce);
        }
    }
    
    /**
     * Get the MetadataConnection object, connection will be lazily instantiated.
     * 
     * @return MetadataConnection to the Force.com metadata API
     * @throws ConnectionException  thrown if an error occurs during initialization of the connection
     */
    public MetadataConnection getMetadataConnection() throws ConnectionException {
        return connector.getMetadataConnection();
    }
    
    /**
     * Get the BulkConnection object, connection will be lazily instantiated.
     * 
     * @return BulkConnection to the Force.com Bulk API
     * @throws ConnectionException  thrown if an error occurs during initialization of the connection
     * @throws AsyncApiException    thrown if an error occurs during initialization of the connection
     */
    public BulkConnection getBulkConnection() throws ConnectionException, AsyncApiException {
        return connector.getBulkConnection();
    }
    
    /**
     * Retrieve the namespace of the Force.com organization.  Connection will be instantiated
     * if it hasn't been established yet.
     * 
     * @return the SFDC namespace of the organization that is connected to Force.com
     * @throws ConnectionException  thrown if an error occurs during initialization of the connection
     *                              (if the connection has not yet been instantiated this method will
     *                              first instantiate it) 
     */
    public String getNamespace() throws ConnectionException {
        return connector.getNamespace();
    }
}
