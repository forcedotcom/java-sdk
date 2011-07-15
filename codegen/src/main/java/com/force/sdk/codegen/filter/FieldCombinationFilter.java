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

import java.util.ArrayList;
import java.util.List;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

/**
 * A {@link FieldFilter} which will apply multiple 
 * {@code FieldFilter}s to a Force.com {@code DescribeSObjectResult} object.
 * 
 * @author Tim Kral
 */
public class FieldCombinationFilter implements FieldFilter {

    List<FieldFilter> filterList = new ArrayList<FieldFilter>();
    
    @Override
    public List<Field> filter(DescribeSObjectResult dsr) {
        List<Field> filteredFields = null;
        for (FieldFilter filter : filterList) {
            filteredFields = filter.filter(dsr);
            dsr.setFields(filteredFields.toArray(new Field[filteredFields.size()]));
        }
        
        return filteredFields;
    }

    /**
     * Add a {@code FieldFilter} to be executed by this {@code FieldCombinationFilter}.
     * 
     * @param fieldFilter a {@code FieldFilter} which is to be executed by
     *                    this {@code FieldCombinationFilter} in the order in which
     *                    it was added
     * @return this {@code FieldCombinationFilter} to ease combination filter
     *         construction
     */
    public FieldCombinationFilter addFilter(FieldFilter fieldFilter) {
        filterList.add(fieldFilter);
        return this;
    }
    
    /**
     * Returns the {@code FieldFilter}s to be executed in order.
     * 
     * @return the list of {@code FieldFilter}s that are to be executed
     *         by thie {@code FieldCombinationFilter}
     */
    public List<FieldFilter> getFilterList() {
        return filterList;
    }
}
