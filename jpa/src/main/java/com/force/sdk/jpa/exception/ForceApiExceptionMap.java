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

package com.force.sdk.jpa.exception;

import org.datanucleus.exceptions.*;
import org.datanucleus.store.query.NoQueryResultsException;

import com.sforce.soap.partner.fault.ApiFault;

/**
 * Maps Force.com API Exceptions to appropriate JPA Exceptions.
 *
 * @author Tim Kral
 */
public final class ForceApiExceptionMap {

    private ForceApiExceptionMap() {  }
    
    /**
     * Method to map specific api exception codes to the proper DataNucleus
     * exception.
     * 
     * @param af  the Force.com ApiFault exception to map
     * @param isQuery whether the exception happened during a query or not
     * @param isOptimistic whether JPA transactions are in optimistic mode
     * @return the properly mapped NucleusException
     */
    public static NucleusException mapToNucleusException(ApiFault af, boolean isQuery, boolean isOptimistic) {
        switch (af.getExceptionCode()) {
        case INVALID_QUERY_FILTER_OPERATOR:
            if (isQuery) {
                // For queries with an invalid ID field message, treat them
                // just like any other query for which we have no results.
                String exceptionMsg = af.getExceptionMessage();
                if (exceptionMsg != null && exceptionMsg.contains("invalid ID field")) {
                    return new NoQueryResultsException("invalid ID field");
                }
                
                return new NucleusUserException(af.toString(), af);
            } else {
                // For non-queries (e.g. em.find), the SOQL query is pre-defined
                // which means we likely have an invalid ID field.
                // So treat this case as if we found no data. 
                return new NucleusObjectNotFoundException();
            }
        case INVALID_SOAP_HEADER:
            if (isOptimistic) {
                return new NucleusDataStoreException("Your organization does not have the necessary permissions "
                    + "to use optimistic transactions. Please contact salesforce.com to have this permission enabled.");
            }
            // fall through to default
        default:
            return new NucleusDataStoreException(af.toString(), af);
        }
    }
}
