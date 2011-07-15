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

import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;

import com.google.inject.internal.Lists;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * Unit tests for {@link FieldCombinationFilter}.
 *
 * @author Tim Kral
 */
public class FieldCombinationFilterTest {

    @Test
    public void testBasicCombinationFilter() {
        DescribeSObjectResult dsr = new DescribeSObjectResult();
        dsr.setName("Object_Name__c");
        
        Field idField = new Field();
        idField.setName("Id");
        idField.setType(FieldType.id);
        
        Field nameField = new Field();
        nameField.setName("Name");
        nameField.setType(FieldType.string);
        
        Field customField = new Field();
        customField.setName("customField");
        customField.setType(FieldType.string);
        
        dsr.setFields(new Field[] { idField, nameField, customField });
        
        List<Field> filteredFields = new FieldCombinationFilter()
            .addFilter(new FieldNameTestFilter("Name")).addFilter(new FieldNameTestFilter("Id")).filter(dsr);
        
        assertNotNull(filteredFields, "A combination filter of a non-null value should be non-null");
        assertEquals(filteredFields.size(), 1, "Unexpected number of Fields after combination filtering");
        assertEquals(filteredFields.get(0).getName(), "customField", "Unexpected Field after combination filtering");
    }
    
    /**
     * A test {@code FieldFilter} which filters fields based on name.
     * 
     * @author Tim Kral
     */
    private static class FieldNameTestFilter implements FieldFilter {

        private String fieldName;
        
        FieldNameTestFilter(String fieldName) {
            this.fieldName = fieldName;
        }
        
        @Override
        public List<Field> filter(DescribeSObjectResult dsr) {
            List<Field> fieldList = Lists.newArrayList(dsr.getFields());
            Iterator<Field> fieldIter = fieldList.iterator();
            while (fieldIter.hasNext()) {
                Field field = fieldIter.next();
                
                if (fieldName.equals(field.getName())) {
                    fieldIter.remove();
                }
            }
            
            return fieldList;
        }
        
    }
}
