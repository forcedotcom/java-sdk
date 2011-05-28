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

package com.force.sdk.codegen.renderer;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sforce.soap.partner.*;


/**
 * Unit tests for {@link ForceApiFieldRenderer}.
 *
 * @author Tim Kral
 */
public class ForceJPAFieldRendererTest {

    @DataProvider
    protected Object[][] getterAnnotationFormatProvider() {
        
        return new Object[][] {
                {FieldType._boolean, ""},
                {FieldType._double, ""},
                {FieldType._int, ""},
                {FieldType.anyType, ""},
                {FieldType.base64, ""},
                {FieldType.combobox, ""},
                {FieldType.currency, ""},
                {FieldType.datacategorygroupreference, ""},
                {FieldType.date, ""},
                {FieldType.datetime, ""},
                {FieldType.email, ""},
                {FieldType.encryptedstring, ""},
                {FieldType.id, "@Id\n@GeneratedValue(strategy=GenerationType.IDENTITY)"},
                {FieldType.multipicklist, ""},
                {FieldType.percent, ""},
                {FieldType.phone, ""},
                {FieldType.reference, "@ManyToOne\n@Basic(fetch=FetchType.LAZY)"},
                {FieldType.picklist, ""},
                {FieldType.string, ""},
                {FieldType.textarea, ""},
                {FieldType.time, ""},
                {FieldType.url, "@Basic"},
            };
    }
    
    @Test(dataProvider = "getterAnnotationFormatProvider")
    public void testGetterAnnotationFormat(FieldType type, String expectedRenderedString) {
        Field field = new Field();
        field.setType(type);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "getterAnnotation0"), expectedRenderedString,
                "Unexpected result for ForceApiFieldRenderer getterAnnotation format");
    }
    
    @Test
    public void testCustomGetterAnnotationFormat() {
        Field field = new Field();
        field.setName("ns__Custom_Field__c");
        field.setCustom(true);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "getterAnnotation0"), "@Column(name=\"ns__Custom_Field__c\")",
                "Unexpected result for ForceApiFieldRenderer getterAnnotation format");
    }
    
    @Test
    public void testReferenceGetterAnnotationFormat() {
        Field field = new Field();
        field.setName("refField");
        field.setType(FieldType.reference);
        field.setRelationshipName("relationshipName");
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "getterAnnotation0"),
                "@ManyToOne\n@Basic(fetch=FetchType.LAZY)\n@Column(name=\"refField\")",
                "Unexpected result for ForceApiFieldRenderer getterAnnotation format");
    }
    
    @Test
    public void testRestrictedPicklistGetterAnnotationFormat() {
        Field field = new Field();
        field.setName("picklistField");
        field.setType(FieldType.picklist);
        field.setRestrictedPicklist(true);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "getterAnnotation0"), "@Enumerated(value=EnumType.STRING)",
                "Unexpected result for ForceApiFieldRenderer getterAnnotation format");
    }
    
    @Test
    public void testVersionGetterAnnotationFormat() {
        Field field = new Field();
        field.setName("LastModifiedDate");
        field.setType(FieldType.datetime);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "getterAnnotation0"), "@Version",
                "Unexpected result for ForceApiFieldRenderer getterAnnotation format");
    }
    
    @Test
    public void testEnumAnnotationFormat() {
        Field field = new Field();
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "enumAnnotation0"),
                "@javax.annotation.Generated(value=\"com.force.sdk.codegen.ForceJPAClassGenerator\")",
                "Unexpected result for ForceApiFieldRenderer enumAnnotation format");
    }
    
    @Test
    public void testSetterAnnotationFormat() {
        Field field = new Field();
        field.setName("requiredField");
        field.setNillable(false);
        field.setDefaultedOnCreate(false);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "setterAnnotation0"), "@Basic(optional=false)",
                "Unexpected result for ForceApiFieldRenderer getterAnnotation format");
    }
    
    @DataProvider
    protected Object[][] fieldNameFormatProvider() {
        
        return new Object[][] {
            {"Account", "account"},
            {"Case", "caseField"},
            {"CustomField__c", "customField"},
            {"customField", "customField"},
            {"CustomRelationship__r", "customRelationship"},
            {"CustomField_With_Spaces__c", "customFieldWithSpaces"},
            {"CustomField___With_Spaces__c", "customFieldWithSpaces"},
            {"InstanceOf", "instanceOfField"},
            {"ns__CustomField__c", "nsCustomField"},
            {"ns__custom_field__c", "nsCustomField"},
        };
    }
    
    @Test(dataProvider = "fieldNameFormatProvider")
    public void testFieldNameFormat(String fieldName, String expectedRenderedString) {
        Field field = new Field();
        field.setName(fieldName);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldName"), expectedRenderedString,
                "Unexpected result for ForceApiFieldRenderer fieldName format");
    }
    
    @Test
    public void testReferenceFieldNameFormat() {
        Field field = new Field();
        field.setName("fieldName");
        field.setType(FieldType.reference);
        field.setRelationshipName("relationshipName");
        field.setReferenceTo(new String[] { "Relationship" });
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldName"), "relationshipName",
                "Unexpected result for ForceApiFieldRenderer fieldName format");
    }
    
    @Test
    public void testMultiReferenceFieldNameFormat() {
        Field field = new Field();
        field.setName("fieldName");
        field.setType(FieldType.reference);
        field.setRelationshipName("relationshipName");
        field.setReferenceTo(new String[] { "Relationship1", "Relationship2" });
        
        // Since we have more then one relationship object,
        // we'll revert back tot he field name
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldName"), "fieldName",
                "Unexpected result for ForceApiFieldRenderer fieldName format");
    }
    
    @Test
    public void testFieldCommentsFormat() {
        Field field = new Field();
        field.setName("refField");
        field.setType(FieldType.reference);
        field.setRelationshipName("relationshipName");
        field.setReferenceTo(new String[] { "Relationship1", "Relationship2" });
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldComments0"),
                "// refField possible references:\n// Relationship1\n// Relationship2",
                "Unexpected result for ForceApiFieldRenderer fieldComments format");
    }
    
    @DataProvider
    protected Object[][] fieldTypeFormatProvider() {
        
        return new Object[][] {
            {FieldType._boolean, "boolean"},
            {FieldType._double, "double"},
            {FieldType._int, "int"},
            {FieldType.anyType, "String"},
            {FieldType.base64, "String"},
            {FieldType.combobox, "boolean"},
            {FieldType.currency, "java.math.BigDecimal"},
            {FieldType.datacategorygroupreference, "String"},
            {FieldType.date, "java.util.Date"},
            {FieldType.datetime, "java.util.Calendar"},
            {FieldType.email, "String"},
            {FieldType.encryptedstring, "String"},
            {FieldType.id, "String"},
            {FieldType.multipicklist, "String[]"},
            {FieldType.percent, "double"},
            {FieldType.phone, "String"},
            {FieldType.picklist, "String"},
            {FieldType.string, "String"},
            {FieldType.textarea, "String"},
            {FieldType.time, "java.util.Date"},
            {FieldType.url, "java.net.URL"},
        };
    }
    
    @Test(dataProvider = "fieldTypeFormatProvider")
    public void testFieldTypeFormat(FieldType type, String expectedRenderedString) {
        Field field = new Field();
        field.setType(type);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldType"), expectedRenderedString,
                "Unexpected result for ForceApiFieldRenderer fieldType format");
    }
    
    @Test
    public void testRestrictedPicklistFieldTypeFormat() {
        Field field = new Field();
        field.setName("picklistField");
        field.setType(FieldType.picklist);
        field.setRestrictedPicklist(true);
        field.setPicklistValues(new PicklistEntry[1]);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldType"), "PicklistFieldEnum",
                "Unexpected result for ForceApiFieldRenderer fieldType format");
    }
    
    @Test
    public void testRestrictedPicklistWithNoValuesFieldTypeFormat() {
        Field field = new Field();
        field.setName("picklistField");
        field.setType(FieldType.picklist);
        field.setRestrictedPicklist(true);

        // Since this picklist field has no picklist values, it should be a String type
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldType"), "String",
                "Unexpected result for ForceApiFieldRenderer fieldType format");
    }
    
    @Test
    public void testRestrictedMultiPicklistFieldTypeFormat() {
        Field field = new Field();
        field.setName("picklistField");
        field.setType(FieldType.multipicklist);
        field.setRestrictedPicklist(true);
        field.setPicklistValues(new PicklistEntry[1]);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldType"), "PicklistFieldEnum[]",
                "Unexpected result for ForceApiFieldRenderer fieldType format");
    }

    @Test
    public void testRestrictedMultiPicklistWithNoValuesFieldTypeFormat() {
        Field field = new Field();
        field.setName("picklistField");
        field.setType(FieldType.multipicklist);
        field.setRestrictedPicklist(true);
        
        // Since this multipicklist field has no picklist values, it should be a String[] type
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldType"), "String[]",
                "Unexpected result for ForceApiFieldRenderer fieldType format");
    }
    
    @DataProvider
    protected Object[][] referenceTypeFormatProvider() {
        
        return new Object[][] {
            {new String[] {"Account"}, "Account"},
            {new String[] {"User", "Group"}, "String"},
            {new String[] {"CustomObject__c"}, "CustomObject"},
            {new String[] {"CustomObject_With_Spaces__c"}, "CustomObjectWithSpaces"},
            {new String[] {"ns__CustomObject__c"}, "NsCustomObject"}
        };
    }
    
    @Test(dataProvider = "referenceTypeFormatProvider")
    public void testReferenceTypeFormat(String[] referenceTo, String expectedRenderedString) {
        Field field = new Field();
        field.setType(FieldType.reference);
        field.setReferenceTo(referenceTo);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "fieldType"), expectedRenderedString,
                "Unexpected result for ForceApiFieldRenderer fieldType format");
    }
    
    @DataProvider
    protected Object[][] methodNameFormatProvider() {
        
        return new Object[][] {
            {"Account", "Account"},
            {"Case", "CaseField"},
            {"CustomField__c", "CustomField"},
            {"CustomRelationship__r", "CustomRelationship"},
            {"CustomField_With_Spaces__c", "CustomFieldWithSpaces"},
            {"CustomField___With_Spaces__c", "CustomFieldWithSpaces"},
            {"InstanceOf", "InstanceOfField"},
            {"ns__CustomField__c", "NsCustomField"},
            {"ns__custom_field__c", "NsCustomField"},
        };
    }
    
    @Test(dataProvider = "methodNameFormatProvider")
    public void testMethodNameFormat(String fieldName, String expectedRenderedString) {
        Field field = new Field();
        field.setName(fieldName);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "methodName"), expectedRenderedString,
                "Unexpected result for ForceApiFieldRenderer methodName format");
    }
    
    @Test
    public void testReferenceMethodNameFormat() {
        Field field = new Field();
        field.setName("fieldName");
        field.setType(FieldType.reference);
        field.setRelationshipName("relationshipName");
        field.setReferenceTo(new String[] { "Relationship" });
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "methodName"), "RelationshipName",
                "Unexpected result for ForceApiFieldRenderer methodName format");
    }
    
    @Test
    public void testEnumNameFormat() {
        Field field = new Field();
        field.setName("picklistField");
        field.setType(FieldType.picklist);
        field.setRestrictedPicklist(true);
        
        ForceJPAFieldRenderer renderer = new ForceJPAFieldRenderer();
        assertEquals(renderer.toString(field, "enumName"), "PicklistFieldEnum",
                "Unexpected result for ForceApiFieldRenderer enumName format");
    }
}
