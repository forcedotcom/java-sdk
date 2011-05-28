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

package com.force.sdk.jpa.mock;

import com.force.sdk.jpa.ForceManagedConnection;
import com.sforce.async.AsyncApiException;
import com.sforce.async.BulkConnection;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import mockit.Mock;
import mockit.MockClass;

/**
 * A mock ForceManagedConnection class.
 * <p>
 * This class will return non-null connections.
 *
 * @author Tim Kral
 */
@MockClass(realClass = ForceManagedConnection.class)
public final class MockForceManagedConnection {
    
    @Mock
    public Object getConnection() {
        try {
            ConnectorConfig config = new ConnectorConfig();
            config.setManualLogin(true);
            
            // Just return a non-null PartnerConnection
            return new PartnerConnection(config);
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Mock
    public MetadataConnection getMetadataConnection() throws ConnectionException {
        // Just return a non-null MetadataConnection
        return new MetadataConnection(new ConnectorConfig());
    }
    
    @Mock
    public BulkConnection getBulkConnection() throws ConnectionException, AsyncApiException {
        // Just return a non-null BulkConnection
        return new BulkConnection(new ConnectorConfig());
    }
    
    @Mock
    public String getNamespace() throws ConnectionException {
        return null;
    }
    
}
