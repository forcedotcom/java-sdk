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

package com.force.sdk.codegen.filter;

import java.util.*;

import com.sforce.soap.partner.DescribeSObjectResult;

/**
 * A {@link DataFilter} which filters based on Force.com schema object (SObject)
 * names.
 * <p>
 * This {@code DataFilter} will filter in a Force.com schema object if and only if
 * its name exactly matches one in the {@code ObjectNameDataFilter} state.  Names
 * in the {@code ObjectNameDataFilter} state which match no schema objects are
 * ignored.
 * 
 * @author Tim Kral
 */
public class ObjectNameDataFilter implements DataFilter {

    private final Set<String> objectNames;
    
    /**
     * Initializes a {@code ObjectNameDataFilter} with a set of
     * Force.com schema object names that are to be filtered
     * in.
     * 
     * @param objectNames a {@code java.util.Set} of exact object
     *                    names that are to be filtered in
     */
    public ObjectNameDataFilter(Set<String> objectNames) {
        this.objectNames = objectNames;
    }
    
    /**
     * Initializes a {@code ObjectNameDataFilter} with a set of
     * Force.com schema object names that are to be filtered
     * in.
     * <p>
     * Note that duplicates within the given array with be eliminated.
     * 
     * @param objectNames an {@code Array} of exact object
     *                    names that are to be filtered in
     */
    public ObjectNameDataFilter(String... objectNames) {
        this.objectNames = new HashSet<String>();
        this.objectNames.addAll(Arrays.asList(objectNames));
    }
    
    @Override
    public List<DescribeSObjectResult> filter(List<DescribeSObjectResult> dsrs) {
        List<DescribeSObjectResult> filteredResult = new ArrayList<DescribeSObjectResult>();
        for (DescribeSObjectResult dsr : dsrs) {
            if (objectNames.contains(dsr.getName())) {
                filteredResult.add(dsr);
            }
        }
        
        return filteredResult;
    }

    /**
     * Returns the {@code java.util.Set} of schema object names
     * on which this {@code ObjectNameDataFilter} will filter.
     * 
     * @return a non {@code null} {@code java.util.Set} which
     *         contains the schema object names on which this
     *         {@code ObjectNameDataFilter} will filter
     */
    public Set<String> getObjectNames() {
        return objectNames;
    }
}
