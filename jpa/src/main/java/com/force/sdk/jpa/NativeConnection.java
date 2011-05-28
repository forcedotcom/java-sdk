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

import org.datanucleus.store.NucleusConnection;
import org.datanucleus.store.connection.ManagedConnection;

import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Connection class that provides connections to each Force.com API 
 * (SOAP, Metadata, and Bulk).
 *
 * @author Fiaz Hossain
 */
public class NativeConnection implements NucleusConnection {

    private ForceManagedConnection connection;
    
    /**
     * Instantiate a Native connection, this must be called with a ForceManagedConnection.
     * 
     * @param connection  The ForceManagedConnection object that will be used for all API connections 
     */
    public NativeConnection(ManagedConnection connection) {
        this.connection = (ForceManagedConnection) connection;
    }
    
    /**
     * Close all API connections and reset configs.
     */
    @Override
    public void close() {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public Object getNativeConnection() {
        return this;
    }

    /**
     * 
     * Returns whether the connection has been instantiated (i.e. is not null)
     * 
     * @return true if the connection is non null
     */
    @Override
    public boolean isAvailable() {
        return connection != null;
    }

    /**
     * @return a connection to the Force.com Partner SOAP API
     */
    public PartnerConnection getPartnerConnection() {
        return (PartnerConnection) connection.getConnection();
    }
    
    /**
     * @return a connection to the Force.com metadata API
     * @throws ConnectionException thrown if a connection cannot be instantiated
     */
    public MetadataConnection getMetadataConnection() throws ConnectionException {
        return connection.getMetadataConnection();
    }
    
    /**
     * @return a connection to the Force.com Bulk API
     * @throws ConnectionException thrown if a connection cannot be instantiated
     * @throws AsyncApiException  thrown if there are problems with the connection config
     */
    public BulkConnection getBulkConnection() throws ConnectionException, AsyncApiException {
        return connection.getBulkConnection();
    }
}
