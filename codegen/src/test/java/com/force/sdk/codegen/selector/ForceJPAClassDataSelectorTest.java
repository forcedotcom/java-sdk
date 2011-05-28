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

package com.force.sdk.codegen.selector;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.force.sdk.codegen.renderer.ForceJPAFieldRenderer;
import com.force.sdk.codegen.template.StringTemplateWrapper;
import com.sforce.soap.partner.*;

/**
 * Unit tests for {@link ForceJPAClassDataSelector}.
 *
 * @author Tim Kral
 */
public class ForceJPAClassDataSelectorTest {
    
    @Test
    public void testBasicSelect() {
        GetUserInfoResult userInfo = new GetUserInfoResult();
        userInfo.setOrganizationName("testBasicSelect UserInfo");
        
        DescribeSObjectResult dsr = new DescribeSObjectResult();
        dsr.setName("testBasicSelect DescribeSObjectResult");
        
        StringTemplateWrapper template = new StringTemplateWrapper("$packageName$ $userInfo.organizationName$ $objectInfo.name$");
        new ForceJPAClassDataSelector().select(userInfo, dsr, template);
        
        assertEquals(template.toString(),
                "com.testbasicselectuserinfo.model testBasicSelect UserInfo testBasicSelect DescribeSObjectResult",
                "Unexpected template after Force.com JPA class data select");
    }
    
    @Test
    public void testStaticPackageName() {
        GetUserInfoResult userInfo = new GetUserInfoResult();
        userInfo.setOrganizationName("testStaticPackageName UserInfo");
        
        StringTemplateWrapper template = new StringTemplateWrapper("$packageName$");
        ForceJPAClassDataSelector selector = new ForceJPAClassDataSelector();
        selector.setPackageName("com.staticpackage.model");
        
        selector.select(userInfo, new DescribeSObjectResult(), template);
        
        assertEquals(template.toString(), "com.staticpackage.model",
                "Unexpected template after Force.com JPA class data select");
    }
    
    @Test
    public void testSelectStandardObjectFields() {
        DescribeSObjectResult dsr = createDescribeSObjectResult("testSelectStandardObjectFields");
        dsr.setCustom(false);
        
        StringTemplateWrapper template = new StringTemplateWrapper("$fields:{f | $f.name$ }$");
        new ForceJPAClassDataSelector().select(new GetUserInfoResult(), dsr, template);
        
        assertEquals(template.toString(), "IsDeleted OwnerId CustomField__c ",
                "Unexpected template after Force.com JPA class data select");
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
        
        StringTemplateWrapper template = new StringTemplateWrapper("$fields:{f | $f.name$ }$");
        new ForceJPAClassDataSelector().select(new GetUserInfoResult(), dsr, template);
        
        assertEquals(template.toString(), "IsDeleted CreatedById LastModifiedById CustomField__c ",
                "Unexpected template after Force.com JPA class data select");
    }
    
    @Test
    public void testSelectCustomObjectFields() {
        DescribeSObjectResult dsr = createDescribeSObjectResult("testSelectCustomObjectFields");
        dsr.setCustom(true);
        
        StringTemplateWrapper template = new StringTemplateWrapper("$fields:{f | $f.name$ }$");
        new ForceJPAClassDataSelector().select(new GetUserInfoResult(), dsr, template);
        
        assertEquals(template.toString(), "CustomField__c ", "Unexpected template after Force.com JPA class data select");
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
        
        // Pass the fields through the field renderer to ensure we don't get java name conflicts
        StringTemplateWrapper template = new StringTemplateWrapper("$fields:{f | $f; format=\"fieldName\"$ }$");
        template.registerRenderer(Field.class, new ForceJPAFieldRenderer());
        new ForceJPAClassDataSelector().select(new GetUserInfoResult(), dsr, template);
        
        assertEquals(template.toString(), "field fieldCustom fieldRelationship ",
                "Unexpected template after Force.com JPA class data select");
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
}
