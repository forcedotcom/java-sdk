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

/**
 * An {@link ObjectFilter} which will apply multiple 
 * {@code ObjectFilter}s to Force.com {@code DescribeSObjectResult} objects.
 * 
 * @author Tim Kral
 */
public class ObjectCombinationFilter implements ObjectFilter {

    List<ObjectFilter> filterList = new ArrayList<ObjectFilter>();
    
    @Override
    public List<DescribeSObjectResult> filter(List<DescribeSObjectResult> dsrs) {
        for (ObjectFilter filter : filterList) {
            dsrs = filter.filter(dsrs);
        }
        
        return dsrs;
    }

    /**
     * Add an {@code ObjectFilter} to be executed by this {@code ObjectCombinationFilter}.
     * 
     * @param objectFilter an {@code ObjectFilter} which is to be executed by
     *                    this {@code ObjectCombinationFilter} in the order in which
     *                    it was added
     * @return this {@code FieldCombinationFilter} to ease combination filter
     *         construction
     */
    public ObjectCombinationFilter addFilter(ObjectFilter objectFilter) {
        filterList.add(objectFilter);
        return this;
    }
    
    /**
     * Returns the {@code ObjectFilter}s to be executed in order.
     * 
     * @return the list of {@code ObjectFilter}s that are to be executed
     *         by thie {@code ObjectCombinationFilter}
     */
    public List<ObjectFilter> getFilterList() {
        return filterList;
    }
}
