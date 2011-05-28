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

package com.force.sdk.jpa.query;

import java.util.*;

/**
 * Use this class to encapsulate collection parameters. The toString() method is overridden to 
 * return a properly serialized String representation of the collection, appropriate for SOQL
 * queries.
 * 
 * String collections get all members wrapped. So [AAA,BBB] -->  ('AAA', 'BBB')
 * Date/Calendar collections get serialized according to SOQLDateFormatUtil formatting methods.
 * All other collection types rely on their respective toString() methods. So [1,2,3] --> (1,2,3)
 * 
 * @author Saptarshi Roy
 *
 */
public class CollectionParameter {
    private Collection<?> collection;

    /**
     * 
     * Stores off the given collection for use with the toString() method later.
     * 
     * @param collection  the collection that will be formatted as a String for SOQL queries
     */
    public CollectionParameter(Collection<?> collection) {
        this.collection = collection;
    }
    
    /**
     * Given a collection of unknown type, return a serialized String representation of the collection
     * appropriate to inclusion in SOQL queries.
     * 
     * Empty arrays return the value NULL, resulting in queries like '... WHERE Id IN (NULL). This works in SOQL
     * independent of type.
     * 
     * Strings are wrapped in single quotes.
     * All other types are returned as simple comma separated lists.
     * 
     * @return A serialized String representation of the collection appropriate for SOQL IN clause queries.
     */
    @Override
    public String toString() {

        if (collection.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("(");

            Iterator<?> paramIter = collection.iterator();
            
            boolean pastFirst = false;
            while (paramIter.hasNext()) {
                Object paramValue = paramIter.next();
                
                if (pastFirst) {
                    sb.append(",");
                }
                
                if (paramValue instanceof String) {
                    sb.append("'" + paramValue + "'");
                } else if (paramValue instanceof Calendar) {
                    sb.append(SOQLDateFormatUtil.getSOQLFormat((Calendar) paramValue));
                }  else if (paramValue instanceof Date) {
                    sb.append(SOQLDateFormatUtil.getSOQLFormat((Date) paramValue));
                } else {
                    sb.append(paramValue);
                }
                
                pastFirst = true;
            }
            
            sb.append(")");
            return sb.toString();
        } else {
            // This is a type neutral syntax for an empty list in SOQL.
            return "(NULL)";
        }
    }
    
}
