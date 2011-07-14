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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import org.testng.annotations.Test;

import com.google.inject.internal.Lists;
import com.sforce.soap.partner.DescribeSObjectResult;

/**
 * Unit tests for {@link ObjectNameFilter}.
 *
 * @author Tim Kral
 */
public class ObjectNameFilterTest {

    @Test
    public void testBasicFilter() {
        DescribeSObjectResult dsrIn = new DescribeSObjectResult();
        dsrIn.setName("Object_Name_In__c");
        
        DescribeSObjectResult dsrOut = new DescribeSObjectResult();
        dsrOut.setName("Object_Name_Out__c");
        
        List<DescribeSObjectResult> dsrs =
            new ObjectNameFilter("Object_Name_In__c").filter(Lists.newArrayList(dsrIn, dsrOut));
        
        assertNotNull(dsrs, "An object name filter of a non-null value should be non-null");
        assertEquals(dsrs.size(), 1, "Unexpected number of DescribeSObjectResults after object name filter");
        assertEquals(dsrs.get(0).getName(), "Object_Name_In__c", "Unexpected DescribeSObjectResult after object name filter");
    }
}
