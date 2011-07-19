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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * A {@link FieldFilter} which filters out reference fields.
 * 
 * @author Tim Kral
 */
public class FieldReferenceFilter implements FieldFilter {

    private final boolean include;
    private final Set<String> referenceObjectNames;
    
    /**
     * Initializes an {@code FieldReferenceFilter} with a set of
     * Force.com schema object names. Any reference field to one of
     * these object will be filtered.
     * 
     * @param include whether this {@code FieldReferenceFilter} is
     *                an include filter or exclude filter
     * @param referenceObjectNames a {@code java.util.Set} of exact object
     *                             names that are to be filtered in
     */
    public FieldReferenceFilter(boolean include, Set<String> referenceObjectNames) {
        this.include = include;
        this.referenceObjectNames = referenceObjectNames;
    }
    
    /**
     * Initializes an {@code FieldReferenceFilter} with a set of
     * Force.com schema object names. Any reference field to one of
     * these object will be filtered.
     * <p>
     * Note that duplicates within the given array will be eliminated.
     * 
     * @param include whether this {@code FieldReferenceFilter} is
     *                an include filter or exclude filter
     * @param referenceObjectNames an {@code Array} of exact object
     *                             names that are to be filtered
     */
    public FieldReferenceFilter(boolean include, String... referenceObjectNames) {
        this.include = include;
        this.referenceObjectNames = new HashSet<String>();
        this.referenceObjectNames.addAll(Arrays.asList(referenceObjectNames));
    }
 
    @Override
    public List<Field> filter(DescribeSObjectResult dsr) {
        List<Field> fieldList = Lists.newArrayList(dsr.getFields());
        Iterator<Field> fieldIter = fieldList.iterator();
        
        while (fieldIter.hasNext()) {
            Field field = fieldIter.next();
            
            if (field.getType() == FieldType.reference) {
                for (String referenceTo : field.getReferenceTo()) {
                    // Remove if there exists a reference to object for which:
                    //
                    // include and referenceObjectNames does not contain name
                    // OR
                    // exclude and referenceObjectNames contains name
                    if (include != referenceObjectNames.contains(referenceTo)) {
                        fieldIter.remove();
                        break;
                    }
                }
            }
        }
        
        return fieldList;
    }
    
    /**
     * Returns the {@code java.util.Set} of schema object names
     * for which this {@code FieldReferenceFilter} will filter
     * field references.
     * 
     * @return a non {@code null} {@code java.util.Set} that
     *         contains the schema object names for which this
     *         {@code FieldReferenceFilter} will filter field
     *         references
     */
    public Set<String> getReferenceObjectNames() {
        return referenceObjectNames;
    }
    
    /**
     * Returns whether this {@code FieldReferenceFilter} is an
     * include filter or exclude filter.
     * 
     * @return true if and only if this {@code FieldReferenceFilter}
     *         is an include filter
     */
    public boolean isInclude() {
        return include;
    }
}
