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
import com.sforce.soap.partner.*;

/**
 * Unit tests for {@link ObjectNameWithRefDataFilter}.
 *
 * @author Tim Kral
 */
public class ObjectNameWithRefDataFilterTest {

    @Test
    public void testFilterWithNoReferences() {
        DescribeSObjectResult dsrIn = new DescribeSObjectResult();
        dsrIn.setName("Object_Name_In__c");
     
        DescribeSObjectResult dsrOut = new DescribeSObjectResult();
        dsrOut.setName("Object_Name_Out__c");
        
        Field idField = new Field();
        idField.setName("Id");
        idField.setType(FieldType.id);
        
        dsrIn.setFields(new Field[] {idField});
        dsrOut.setFields(new Field[] {idField});
        
        List<DescribeSObjectResult> dsrs =
            new ObjectNameWithRefDataFilter("Object_Name_In__c").filter(Lists.newArrayList(dsrIn, dsrOut));
        
        assertNotNull(dsrs, "An object name with references filter of a non-null value should be non-null");
        assertEquals(dsrs.size(), 1, "Unexpected number of DescribeSObjectResults after object name with references filter");
        assertEquals(dsrs.get(0).getName(), "Object_Name_In__c",
                "Unexpected DescribeSObjectResult after object name with references filter");
    }
    
    @Test
    public void testFilterWithSingleLevelReferences() {
        DescribeSObjectResult dsrIn = new DescribeSObjectResult();
        dsrIn.setName("Object_Name_In__c");
     
        DescribeSObjectResult dsrRef = new DescribeSObjectResult();
        dsrRef.setName("RefObject__c");
        
        Field refField = new Field();
        refField.setName("RefField__c");
        refField.setType(FieldType.reference);
        refField.setReferenceTo(new String[] {"RefObject__c"});
        
        // Add a reference from Object_Name_In__c to RefObject__c
        dsrIn.setFields(new Field[] {refField});
        
        List<DescribeSObjectResult> dsrs =
            new ObjectNameWithRefDataFilter("Object_Name_In__c").filter(Lists.newArrayList(dsrIn, dsrRef));
        
        assertNotNull(dsrs, "An object name with references filter of a non-null value should be non-null");
        assertEquals(dsrs.size(), 2, "Unexpected number of DescribeSObjectResults after object name with references filter");
    }
    
    @Test
    public void testFilterWithMultiLevelReferences() {
        DescribeSObjectResult dsrIn = new DescribeSObjectResult();
        dsrIn.setName("Object_Name_In__c");
     
        DescribeSObjectResult dsrRef1 = new DescribeSObjectResult();
        dsrRef1.setName("FirstLevel_RefObject__c");
        
        DescribeSObjectResult dsrRef2 = new DescribeSObjectResult();
        dsrRef2.setName("SecondLevel_RefObject__c");
        
        Field refField1 = new Field();
        refField1.setName("RefField__c");
        refField1.setType(FieldType.reference);
        refField1.setReferenceTo(new String[] {"FirstLevel_RefObject__c"});
        
        // Add a reference from Object_Name_In__c to FirstLevel_RefObject__c
        dsrIn.setFields(new Field[] {refField1});
        
        Field refField2 = new Field();
        refField2.setName("RefField__c");
        refField2.setType(FieldType.reference);
        refField2.setReferenceTo(new String[] {"SecondLevel_RefObject__c"});
        
        // Add a reference from FirstLevel_RefObject__c to SecondLevel_RefObject__c
        dsrRef1.setFields(new Field[] {refField2});
        
        List<DescribeSObjectResult> dsrs =
            new ObjectNameWithRefDataFilter("Object_Name_In__c").filter(Lists.newArrayList(dsrIn, dsrRef1, dsrRef2));
        
        assertNotNull(dsrs, "An object name with references filter of a non-null value should be non-null");
        assertEquals(dsrs.size(), 3, "Unexpected number of DescribeSObjectResults after object name with references filter");
    }
    
    @Test
    public void testFilterWithCircularReferences() {
        DescribeSObjectResult dsrRef1 = new DescribeSObjectResult();
        dsrRef1.setName("RefObject1__c");
        
        DescribeSObjectResult dsrRef2 = new DescribeSObjectResult();
        dsrRef2.setName("RefObject2__c");
        
        Field refField1 = new Field();
        refField1.setName("RefField1__c");
        refField1.setType(FieldType.reference);
        refField1.setReferenceTo(new String[] {"RefObject1__c"});
        
        // Add a reference from RefObject2__c to RefObject1__c
        dsrRef2.setFields(new Field[] {refField1});
        
        Field refField2 = new Field();
        refField2.setName("RefField2__c");
        refField2.setType(FieldType.reference);
        refField2.setReferenceTo(new String[] {"RefObject2__c"});
        
        // Add a reference from RefObject1__c to RefObject2__c
        dsrRef1.setFields(new Field[] {refField2});
        
        List<DescribeSObjectResult> dsrs =
            new ObjectNameWithRefDataFilter("RefObject1__c").filter(Lists.newArrayList(dsrRef1, dsrRef2));
        
        assertNotNull(dsrs, "An object name with references filter of a non-null value should be non-null");
        assertEquals(dsrs.size(), 2, "Unexpected number of DescribeSObjectResults after object name with references filter");
    }
}
