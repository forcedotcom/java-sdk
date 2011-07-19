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

import com.sforce.soap.partner.*;

/**
 * An {@link ObjectFilter} that filters based on Force.com schema object (SObject)
 * names and follows object references to produce a closed set of schema objects.
 * <p>
 * This {@code ObjectFilter} will filter a Force.com schema object if and only if
 * at least one of the two following conditions are met:
 * <p>
 * <ol>
 *   <li>Its name exactly matches one in the {@code ObjectNameWithRefFilter} state</li>
 *   <li>It is referenced by a schema object that has already been filtered</li>
 * </ol>
 * Names in the {@code ObjectNameWithRefFilter} state that don't match schema objects are
 * ignored.
 * 
 * @author Tim Kral
 */
public class ObjectNameWithRefFilter implements ObjectFilter {

    // The object names that we want to filter
    private final Set<String> objectNames;
    
    // Map from object name to object for all known objects
    private final Map<String, DescribeSObjectResult> objectMap =
        new HashMap<String, DescribeSObjectResult>();
    
    // Since we're using recursion to filter object references
    // we'll need to know what we've filtered already
    private Set<String> filteredResultNames = new HashSet<String>();
    private List<DescribeSObjectResult> filteredResult = new ArrayList<DescribeSObjectResult>();
    
    /**
     * Initializes an {@code ObjectNameWithRefFilter} with a set of
     * Force.com schema object names that are to be filtered
     * along with their references.
     * 
     * @param objectNames a {@code java.util.Set} of exact object
     *                    names that are to be filtered along with
     *                    their references
     */
    public ObjectNameWithRefFilter(Set<String> objectNames) {
        this.objectNames = new HashSet<String>(objectNames);
    }
    
    /**
     * Initializes an {@code ObjectNameWithRefFilter} with a set of
     * Force.com schema object names that are to be filtered
     * along with their references.
     * <p>
     * Note that duplicates in the given array will be eliminated.
     * 
     * @param objectNames an {@code Array} of exact object
     *                    names that are to be filtered along with
     *                    their references
     */
    public ObjectNameWithRefFilter(String... objectNames) {
        this.objectNames = new HashSet<String>();
        this.objectNames.addAll(Arrays.asList(objectNames));
    }
    
    @Override
    public List<DescribeSObjectResult> filter(List<DescribeSObjectResult> dsrs) {
        for (DescribeSObjectResult dsr : dsrs) {
            objectMap.put(dsr.getName(), dsr);
        }

        filterObjectAndReferences(objectNames);
        return filteredResult;
    }

    private void filterObjectAndReferences(Set<String> objectNameSet) {
    
        // Subtract any objects that we've already fetched
        objectNameSet.removeAll(filteredResultNames);
        if (objectNameSet.isEmpty()) return;
    
        Set<String> objectReferencesSet = new HashSet<String>();
    
        for (String objectName : objectNameSet) {
            DescribeSObjectResult dsr;
            if ((dsr = objectMap.get(objectName)) != null) {
                
                // Gather all the references for this object
                for (Field field : dsr.getFields()) {
                    if (field.getType() == FieldType.reference) {
                        objectReferencesSet.addAll(Arrays.asList(field.getReferenceTo()));
                    }
                }
                
                filteredResultNames.add(dsr.getName());
                filteredResult.add(dsr);
            }
        }
    
        // Here, we'll recursively fetch all the object references we found.
        filterObjectAndReferences(objectReferencesSet);
    }
}
