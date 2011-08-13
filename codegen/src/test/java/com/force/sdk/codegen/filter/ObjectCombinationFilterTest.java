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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * 
 * Unit tests for {@link ObjectCombinationFilter}.
 *
 * @author Jeff Lai
 * 
 */
public class ObjectCombinationFilterTest {

    List<DescribeSObjectResult> dsrs;
    
    @BeforeClass
    public void classSetup() {
        dsrs = new ArrayList<DescribeSObjectResult>();
        for (int i = 0; i < 4; i++) {
            dsrs.add(new DescribeSObjectResult());
            dsrs.get(i).setName("Object" + i + "__c");
        }
        Field refField = new Field();
        refField.setName("ref__c");
        refField.setType(FieldType.reference);
        refField.setReferenceTo(new String[] {"Object3__c"});
        dsrs.get(1).setFields(new Field[] {refField});
    }
    
    @Test
    public void testOneNameFilter() {
        ObjectNameFilter nameFilter = new ObjectNameFilter(false, "Object0__c");
        List<DescribeSObjectResult> sObjects = new ObjectCombinationFilter().addFilter(nameFilter).filter(dsrs);
        verifyFilteredFields(sObjects, Arrays.asList(new String[] {"Object1__c", "Object2__c", "Object3__c"}));
    }
    
    @Test
    public void testTwoNameFilters() {
        ObjectNameFilter nameFilter0 = new ObjectNameFilter(false, "Object0__c");
        ObjectNameFilter nameFilter1 = new ObjectNameFilter(false, "Object1__c");
        List<DescribeSObjectResult> sObjects = new ObjectCombinationFilter().addFilter(nameFilter0)
            .addFilter(nameFilter1).filter(dsrs);
        verifyFilteredFields(sObjects, Arrays.asList(new String[] {"Object2__c", "Object3__c"}));
    }
    
    @Test
    public void testNameAndNameFieldRefFilters() {
        ObjectNameFilter nameFilter = new ObjectNameFilter(false, "Object0__c");
        ObjectNameWithRefFilter nameWithRefFilter = new ObjectNameWithRefFilter("Object1__c");
        List<DescribeSObjectResult> sObjects = new ObjectCombinationFilter().addFilter(nameFilter)
            .addFilter(nameWithRefFilter).filter(dsrs);
        verifyFilteredFields(sObjects, Arrays.asList(new String[] {"Object1__c", "Object3__c"}));
    }
    
    private void verifyFilteredFields(List<DescribeSObjectResult> actualSObjects, List<String> expectedSObjectNames) {
        Collections.sort(actualSObjects, new DescribeSObjectComparator());
        Collections.sort(expectedSObjectNames);
        Assert.assertEquals(actualSObjects.size(), expectedSObjectNames.size(),
                "unexpected number of returned sObjects after filtering");
        for (int i = 0; i < actualSObjects.size(); i++) {
            Assert.assertEquals(actualSObjects.get(i).getName(), expectedSObjectNames.get(i), "unexpected sObject name");
        }
    }
    
    /**
     * 
     * Comparator class for sorting SObjectResults.
     *
     * @author Jeff Lai
     * 
     */
    private static class DescribeSObjectComparator implements Comparator<DescribeSObjectResult>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(DescribeSObjectResult object1, DescribeSObjectResult object2) {
            return object1.getName().compareTo(object2.getName());
        }

        
    }

}
