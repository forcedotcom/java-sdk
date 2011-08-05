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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sforce.soap.partner.*;

/**
 * 
 * Unit tests for {@link FieldReferenceFilter}.
 *
 * @author Jeff Lai
 *
 */
public class FieldReferenceFilterTest {
    
    DescribeSObjectResult dsr;
    
    @BeforeClass
    public void setupClass() {
        dsr = new DescribeSObjectResult();
        dsr.setName("Object_Name__c");
        
        Field idField = new Field();
        idField.setName("Id");
        idField.setType(FieldType.id);
        
        Field nameField = new Field();
        nameField.setName("Name");
        nameField.setType(FieldType.string);
        
        Field phoneField = new Field();
        phoneField.setName("Phone");
        phoneField.setType(FieldType.reference);
        phoneField.setReferenceTo(new String[] {"Phone"});
        
        Field acceptedField = new Field();
        acceptedField.setName("Accepted");
        acceptedField.setType(FieldType.reference);
        acceptedField.setReferenceTo(new String[] {"Accepted"});
        
        dsr.setFields(new Field[] {idField, nameField, phoneField, acceptedField});
        
    }
    
    @Test
    public void testExcludeOneField() {
        List<Field> fields = new FieldReferenceFilter(false, Collections.singleton("Phone")).filter(dsr);
        verifyFilteredFields(fields, Arrays.asList(new String[] {"Accepted", "Id", "Name"}));
    }
    
    @Test
    public void testIncludeOneField() {
        List<Field> fields = new FieldReferenceFilter(true, Collections.singleton("Phone")).filter(dsr);
        verifyFilteredFields(fields, Arrays.asList(new String[] {"Id", "Name", "Phone"}));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testExcludeZeroFields() {
        List<Field> fields = new FieldReferenceFilter(false, Collections.EMPTY_SET).filter(dsr);
        verifyFilteredFields(fields, Arrays.asList(new String[] {"Accepted", "Id", "Name", "Phone"}));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testIncludeZeroFields() {
        List<Field> fields = new FieldReferenceFilter(true, Collections.EMPTY_SET).filter(dsr);
        verifyFilteredFields(fields, Arrays.asList(new String[] {"Id", "Name"}));
    }
    
    private void verifyFilteredFields(List<Field> actualFields, List<String> expectedFieldNames) {
        Collections.sort(actualFields, new FieldComparator());
        Collections.sort(expectedFieldNames);
        Assert.assertEquals(actualFields.size(), expectedFieldNames.size(),
                "unexpected number of returned fields after filtering");
        for (int i = 0; i < actualFields.size(); i++) {
            Assert.assertEquals(actualFields.get(i).getName(), expectedFieldNames.get(i), "unexpected field name");
        }
    }
    
    /**
     * 
     * Comparator class for sorting SObjectResult Fields.
     *
     * @author Jeff Lai
     * 
     */
    private static class FieldComparator implements Comparator<Field> {

        @Override
        public int compare(Field field1, Field field2) {
            return field1.getName().compareTo(field2.getName());
        }
        
    }

}
