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
 * Unit tests for {@link DescribeSObjectResultRenderer}.
 *
 * @author Tim Kral
 */
public class ForceJPAClassRendererTest {

    @Test
    public void testClassAnnotationFormat() {
        DescribeSObjectResult dsr = new DescribeSObjectResult();
        dsr.setName("ns__Custom_Object__c");
        
        ForceJPAClassRenderer renderer = new ForceJPAClassRenderer();
        assertEquals(renderer.toString(dsr, "classAnnotation"),
                "@javax.annotation.Generated(value=\"com.force.sdk.codegen.ForceJPAClassGenerator\")\n"
                + "@Table(name=\"ns__Custom_Object__c\")\n"
                + "@Entity(name=\"NsCustomObject\")\n"
                + "@com.force.sdk.jpa.annotation.CustomObject(readOnlySchema=true)",
                "Unexpected result for DescribeSObjectResultRenderer classAnnotation format");
    }
    
    // The Case entity is special because "Case" is a JPQL keyword
    // this mean that we can't have an entity named "Case"
    @Test
    public void testCaseClassAnnotationFormat() {
        DescribeSObjectResult dsr = new DescribeSObjectResult();
        dsr.setName("Case");
        
        ForceJPAClassRenderer renderer = new ForceJPAClassRenderer();
        assertEquals(renderer.toString(dsr, "classAnnotation"),
                "@javax.annotation.Generated(value=\"com.force.sdk.codegen.ForceJPAClassGenerator\")\n"
                + "@Table(name=\"Case\")\n"
                + "@Entity(name=\"CaseEntity\")\n"
                + "@com.force.sdk.jpa.annotation.CustomObject(readOnlySchema=true)",
                "Unexpected result for DescribeSObjectResultRenderer classAnnotation format");
    }
    
    @DataProvider
    protected Object[][] classNameFormatProvider() {
        
        return new Object[][] {
            {"Account", "Account"},
            {"Case", "CaseEntity"},
            {"CustomObject__c", "CustomObject"},
            {"CustomObject_With_Spaces__c", "CustomObjectWithSpaces"},
            {"CustomObject___With_Spaces__c", "CustomObjectWithSpaces"},
            {"InstanceOf", "InstanceOfEntity"},
            {"ns__CustomObject__c", "NsCustomObject"},
            {"ns__custom_object__c", "NsCustomObject"},
        };
    }
    
    @Test(dataProvider = "classNameFormatProvider")
    public void testClassNameFormat(String objectName, String expectedRenderedString) {
        DescribeSObjectResult dsr = new DescribeSObjectResult();
        dsr.setName(objectName);
        
        ForceJPAClassRenderer renderer = new ForceJPAClassRenderer();
        assertEquals(renderer.toString(dsr, "className"), expectedRenderedString,
                "Unexpected result for DescribeSObjectResultRenderer className format");
    }
    
    @Test
    public void testSuperClassNameFormat() {
        DescribeSObjectResult dsr = new DescribeSObjectResult();
        dsr.setCustom(true);
        
        ForceJPAClassRenderer renderer = new ForceJPAClassRenderer();
        assertEquals(renderer.toString(dsr, "superClassName"), "com.force.sdk.jpa.model.BaseForceCustomObject",
                "Unexpected result for DescribeSObjectResultRenderer superClassName format for a custom object");
        
        // A standard object with not *all* standard object common fields
        dsr.setCustom(false);
        assertEquals(renderer.toString(dsr, "superClassName"), "com.force.sdk.jpa.model.BaseForceObject",
                "Unexpected result for DescribeSObjectResultRenderer superClassName format for a standard object "
                + "with missing common fields");

        Field idField = new Field();
        idField.setName("Id");
        idField.setType(FieldType.id);
        
        Field nameField = new Field();
        nameField.setName("Name");
        nameField.setType(FieldType.string);
        
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
        
        dsr.setFields(new Field[] { idField, nameField, ownerField, createdByIdField,
                                    createdDateField, lastModifiedByIdField,
                                    lastModifiedDateField, systemModstampField });
        
        assertEquals(renderer.toString(dsr, "superClassName"), "com.force.sdk.jpa.model.BaseForceStandardObject",
                "Unexpected result for DescribeSObjectResultRenderer superClassName format for a standard object "
                + "with all common fields");
    }
    
}
