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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sforce.soap.metadata.*;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Processor for results from Force.com metadata API requests.  Used during schema creation.
 *
 * @author Fiaz Hossain
 */
public abstract class ForceAsyncResultProcessor {
    
    private static final int MAX_ITERATION_WAIT_MS = 30000;
    
    protected static final Logger LOGGER = LoggerFactory.getLogger("com.force.sdk.jpa");

    /**
     * 
     * Checks the results of a Force.com metadata API call and waits until the status
     * indicates the request is finished.  Any errors will throw an exception if throwOnErrors
     * is true
     * 
     * @param service  the metadata API connection
     * @param ar  the result returned from a metadata API call
     * @param throwOnErrors  whether an exception should be thrown when errors are encountered
     * @param metaData  the object passed to the metadata API request
     * @throws RemoteException  thrown if there is a problem completing the request
     * @throws InterruptedException  thrown if the request is interrupted
     */
    protected void waitForAsyncResult(MetadataConnection service, AsyncResult[] ar, boolean throwOnErrors, Object metaData)
        throws RemoteException, InterruptedException {
        
        ArrayList<AsyncResult> arList = new ArrayList<AsyncResult>(Arrays.asList(ar));
        long waitTimeMilliSecs = 500;
        while (true) {
            String[] oids = new String[arList.size()];
            for (int i = 0; i < arList.size(); i++) {
                oids[i] = arList.get(i).getId();
            }
            AsyncResult[] results;
            try {
                results = service.checkStatus(oids);
            } catch (ConnectionException x) {
                throw new RuntimeException(x);
            }

            for (int i = results.length - 1; i >= 0; i--) {
                boolean done = results[i].getDone();
                if (done) {
                    StatusCode sc = results[i].getStatusCode();
                    if (sc != null) {
                        if (throwOnErrors) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Error status code: ").append(sc).append("\n")
                              .append("Error message: ").append(results[i].getMessage());
                            sb.append("\nAttempted object(s):");
                            if (metaData.getClass().isArray()) {
                                for (Object o : (Object[]) metaData) {
                                    sb.append("\n").append(o);
                                }
                            } else {
                                sb.append("\n").append(metaData);
                            }
                            throw new RuntimeException(sb.toString());
                        }
                    }
                    arList.remove(i);
                }
            }
            
            if (arList.size() == 0) break;
            
            Thread.sleep(waitTimeMilliSecs);

            if (waitTimeMilliSecs < MAX_ITERATION_WAIT_MS) {
                // Increase the wait time for the next iteration
                waitTimeMilliSecs *= 1.5;
            }
        }
    }
}
