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

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.google.inject.internal.Lists;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * Unit tests for {@link ForceJPAFieldFilter}.
 *
 * @author Tim Kral
 */
public class ForceJPAFieldFilterTest {
    
    @Test
    public void testFilterStandardObjectFields() {
        DescribeSObjectResult dsr = createDescribeSObjectResult("testSelectStandardObjectFields");
        dsr.setCustom(false);
        
        List<Field> filteredFields = new ForceJPAFieldFilter().filter(dsr);
        assertFilteredFields(filteredFields, Lists.newArrayList("IsDeleted", "OwnerId", "CustomField__c"));
    }
    
    @Test
    public void testSelectStandardObjectWithAllCommonFields() {
        Field idField = new Field();
        idField.setName("Id");
        idField.setType(FieldType.id);
        
        Field nameField = new Field();
        nameField.setName("Name");
        nameField.setType(FieldType.string);
        
        Field deletedField = new Field();
        deletedField.setName("IsDeleted");
        deletedField.setType(FieldType._boolean);
        
        Field ownerField = new Field();
        ownerField.setName("OwnerId");
        ownerField.setType(FieldType.reference);
        ownerField.setReferenceTo(new String[] { "User" });
        
        Field createdByIdField = new Field();
        createdByIdField.setName("CreatedById");
        createdByIdField.setType(FieldType.reference);
        createdByIdField.setReferenceTo(new String[] { "User" });
        
        Field createdDateField = new Field();
        createdDateField.setName("CreatedDate");
        createdDateField.setType(FieldType.datetime);
        
        Field lastModifiedByIdField = new Field();
        lastModifiedByIdField.setName("LastModifiedById");
        lastModifiedByIdField.setType(FieldType.reference);
        lastModifiedByIdField.setReferenceTo(new String[] { "User" });
        
        Field lastModifiedDateField = new Field();
        lastModifiedDateField.setName("LastModifiedDate");
        lastModifiedDateField.setType(FieldType.datetime);
        
        Field systemModstampField = new Field();
        systemModstampField.setName("SystemModstamp");
        systemModstampField.setType(FieldType.datetime);
        
        Field customField = new Field();
        customField.setName("CustomField__c");
        customField.setType(FieldType.string);
        
        // Object with all fields common to Force.com standard objects
        DescribeSObjectResult dsr =
            createDescribeSObjectResult("testSelectStandardObjectWithAllCommonFields", idField, nameField,
                    deletedField, ownerField, createdByIdField, createdDateField, lastModifiedByIdField,
                    lastModifiedDateField, systemModstampField, customField);
        dsr.setCustom(false);

        List<Field> filteredFields = new ForceJPAFieldFilter().filter(dsr);
        assertFilteredFields(filteredFields,
                Lists.newArrayList("IsDeleted", "CreatedById", "LastModifiedById", "CustomField__c"));
    }
    
    @Test
    public void testSelectCustomObjectFields() {
        DescribeSObjectResult dsr = createDescribeSObjectResult("testSelectCustomObjectFields");
        dsr.setCustom(true);
        
        List<Field> filteredFields = new ForceJPAFieldFilter().filter(dsr);
        assertFilteredFields(filteredFields, Lists.newArrayList("CustomField__c"));
    }
    
    @Test
    public void testConflictJavaFieldNames() {
        Field stdField = new Field();
        stdField.setName("Field");
        stdField.setType(FieldType.string);
        
        Field customField = new Field();
        customField.setName("Field__c");
        customField.setType(FieldType.string);
        customField.setCustom(true);
        
        Field refField = new Field();
        refField.setName("Field__c");
        refField.setType(FieldType.reference);
        refField.setCustom(true);
        refField.setRelationshipName("Field__r");
        refField.setReferenceTo(new String[] { "Relationship" });
        
        DescribeSObjectResult dsr =
            createDescribeSObjectResult("testConflictJavaFieldNames", stdField, customField, refField);
        
        List<Field> filteredFields = new ForceJPAFieldFilter().filter(dsr);
        
        assertEquals(filteredFields.size(), 3, "Unexpected number of fields after filtering");
        assertEquals(filteredFields.get(0).getName(), "Field", "Unexpected field name after filtering");
        assertEquals(filteredFields.get(1).getName(), "fieldCustom", "Unexpected field name after filtering");
        assertEquals(filteredFields.get(2).getRelationshipName(), "fieldRelationship",
                "Unexpected relationship name after filtering");
    }
    
    private DescribeSObjectResult createDescribeSObjectResult(String name) {
        Field idField = new Field();
        idField.setName("Id");
        idField.setType(FieldType.id);
        
        Field deletedField = new Field();
        deletedField.setName("IsDeleted");
        deletedField.setType(FieldType._boolean);
        
        Field ownerField = new Field();
        ownerField.setName("OwnerId");
        ownerField.setType(FieldType.reference);
        ownerField.setReferenceTo(new String[] { "User" });
        
        Field customField = new Field();
        customField.setName("CustomField__c");
        customField.setType(FieldType.string);

        return createDescribeSObjectResult(name, idField, deletedField, ownerField, customField);
    }
    
    private DescribeSObjectResult createDescribeSObjectResult(String name, Field... fields) {
        DescribeSObjectResult dsr = new DescribeSObjectResult();
        dsr.setName(name);
        dsr.setFields(fields);
        
        return dsr;
    }
    
    private void assertFilteredFields(List<Field> filteredFields, ArrayList<String> expectedFieldNames) {
        assertEquals(filteredFields.size(), expectedFieldNames.size(),
                "Unexpected number of fields after filtering");
        
        for (int i = 0; i < expectedFieldNames.size(); i++) {
            assertEquals(filteredFields.get(i).getName(), expectedFieldNames.get(i),
                    "Unexpected field ordering after filtering at position " + i);
        }
    }
}
