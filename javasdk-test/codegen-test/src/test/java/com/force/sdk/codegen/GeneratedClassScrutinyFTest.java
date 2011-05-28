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

package com.force.sdk.codegen;

import static org.testng.Assert.assertNotNull;

import java.lang.reflect.*;

import org.apache.commons.lang.WordUtils;
import org.testng.annotations.Test;

import com.force.sdk.jpa.model.BaseForceObject;

/**
 * Tests that generated JPA Java classes 
 * conform to certain rules.
 *
 * @author Tim Kral
 */
public class GeneratedClassScrutinyFTest extends BaseGeneratedClassFTest {

    // Tests that all generated Java classes have visibility to the id field
    @Test(dataProvider = "generatedClassProvider")
    public void testIdMethodVisibility(Class generatedClass) {
        Method getIdMethod = null;
        for (Method method : generatedClass.getMethods()) {
            if ("getId".equals(method.getName())) {
                getIdMethod = method;
                break;
            }
        }
        
        assertNotNull(getIdMethod, "getId() Method is not visible for generated class " + generatedClass.getName());
    }
    
    // Tests that standard object generated Java classes have visibility to the name field
    @Test(dataProvider = "generatedClassProvider")
    public void testNameMethodVisibility(Class generatedClass) {
        // Direct children of BaseForceObject will declare their own name field
        // (or they don't have one)
        if (generatedClass.getSuperclass() == BaseForceObject.class) {
            return;
        }
        
        Method getNameMethod = null;
        for (Method method : generatedClass.getMethods()) {
            if ("getName".equals(method.getName())) {
                getNameMethod = method;
                break;
            }
        }
        
        assertNotNull(getNameMethod, "getName() Method is not visible for generated class " + generatedClass.getName());
    }
    
    @SuppressWarnings("unchecked")
    @Test(dataProvider = "generatedClassProvider")
    public void testGeneratedFieldsHaveGetterSetter(Class generatedClass) throws SecurityException, NoSuchMethodException {
        for (Field entityField : generatedClass.getDeclaredFields()) {
            int fieldModifiers = entityField.getModifiers();
            
            // Skip any fields added by the DataNucleus enhancer
            if (!Modifier.isTransient(fieldModifiers) && !Modifier.isStatic(fieldModifiers)
                    && !entityField.getName().contains("jdo")) {
                
                // Convert field name to method name (e.g. fieldName -> getFieldName, setFieldName)
                String methodName = WordUtils.capitalize(entityField.getName());
                assertNotNull(generatedClass.getMethod("get" + methodName),
                        "Unable to find getter for field " + entityField.getName()
                        + " on generated class " + generatedClass.getName());
                
                assertNotNull(generatedClass.getMethod("set" + methodName, entityField.getType()),
                        "Unable to find setter for field " + entityField.getName()
                        + " on generated class " + generatedClass.getName());
            }
            
        }
    }
    
    @Test(dataProvider = "generatedClassProvider")
    public void testGeneratedClassHasEmptyConstructor(Class generatedClass)
    throws InstantiationException, IllegalAccessException {
        // We don't need to do anything with the new instance
        // just verify that we can get one without error
        generatedClass.newInstance();
    }
    
}
