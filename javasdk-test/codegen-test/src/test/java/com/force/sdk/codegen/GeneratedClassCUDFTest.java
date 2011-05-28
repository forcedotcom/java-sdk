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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.jpa.model.BaseForceObject;
import com.google.common.collect.ImmutableSet;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.ws.ConnectionException;

/**
 * Tests that generated JPA Java classes are 
 * enabled for JPA CUD (create, update, delete).
 *
 * @author Tim Kral
 */
public class GeneratedClassCUDFTest extends BaseGeneratedClassFTest {
    
    private static final Set<String> SKIPPED_CUD_CLASSES =
        ImmutableSet.<String>of("Document" /*A delete still leaves this in the recycle bin which blocks Folder delete*/);
    
    private static final Comparator<Class<?>> TOPO_CLASS_SORTER = new Comparator<Class<?>>() {

        @Override
        public int compare(Class<?> c1, Class<?> c2) {
            for (Method method : c1.getDeclaredMethods()) {
                // If class 1 has a required field of type class 2
                // then class 2 needs to come first
                
                // Expect the method to be a setter with one parameter
                if (isRequiredMethod(method) && method.getParameterTypes()[0].equals(c2)) {
                    return 1;
                }
            }
            
            for (Method method : c2.getDeclaredMethods()) {
                // If class 2 has a required field of type class 1
                // then class 1 needs to come first
                
                // Expect the method to be a setter with one parameter
                if (isRequiredMethod(method) && method.getParameterTypes()[0].equals(c1)) {
                    return -1;
                }
            }
            
            return c1.getSimpleName().compareToIgnoreCase(c2.getSimpleName());
        }
        
    };
    
    private Object[][] cudableClassArray;
    private NavigableMap<Class<?>, Object> insertedObjects = new TreeMap<Class<?>, Object>(TOPO_CLASS_SORTER);

    @BeforeClass(dependsOnMethods = "loadGeneratedClasses")
    public void loadCUDableClasses() throws ConnectionException {
        loadEntityManager();
        
        // Run an API describe on all the generated classes
        Map<String, Class<?>> generatedClassMap = new HashMap<String, Class<?>>(generatedClassNameMap());
        DescribeSObjectResult[] dsrs =
            conn.describeSObjects(generatedClassMap.keySet().toArray(new String[generatedClassMap.size()]));
        
        // We'll remove those classes which are not API createable or updateable or deletable
        for (DescribeSObjectResult dsr : dsrs) {
            if (!dsr.isCreateable() || !dsr.isUpdateable() || !dsr.isDeletable()) {
                generatedClassMap.remove(dsr.getName());
            }
        }
        
        // Sort the generated classes so that we can create and delete
        // objects in the correct order
        List<Class<?>> generatedClassList = new ArrayList<Class<?>>(generatedClassMap.values());
        Collections.sort(generatedClassList, TOPO_CLASS_SORTER);
        
        // Load the cudable classes into the form used by a TestNG DataProvider
        cudableClassArray = new Object[generatedClassList.size()][1];
        for (int i = 0; i < generatedClassList.size(); i++) {
            cudableClassArray[i] = new Object[] { generatedClassList.get(i) };
        }
    }
    
    @DataProvider
    protected Object[][] cudableClassProvider() {
        return cudableClassArray;
    }
    
    @Test(dataProvider = "cudableClassProvider")
    public void testGeneratedClassCreateUpdate(Class<?> generatedClass) throws Exception {
        if (SKIPPED_CUD_CLASSES.contains(generatedClass.getSimpleName())) return;
        
        Object object = createObject(generatedClass);
        
        persist(em, object);
        insertedObjects.put(generatedClass, object);
            
        em.clear();
        merge(em, object);
    }

    private Object createObject(Class<?> generatedClass) throws Exception {
        Object object = generatedClass.newInstance();
        
        // Populate fields that are JPA annotated as required
        for (Method entityMethod : generatedClass.getMethods()) {
            if (isRequiredMethod(entityMethod)) {
                // Expect the method to be a setter with one parameter
                entityMethod.invoke(object, getSetterArgument(entityMethod.getParameterTypes()[0]));
            }
        }
        
        // Populate extra fields that may be
        // necessary to persist this object
        populateExtraFields(object);
        
        return object;
    }
    
    private Object getSetterArgument(Class<?> parameterType) {

        if (String.class.equals(parameterType)) {
            return "testGeneratedClassCUD";
        } else if (boolean.class.equals(parameterType)) {
            return false;
        } else if (int.class.equals(parameterType)) {
            return 0;
        } else if (parameterType.isEnum()) {
            try {
                Object[] enumValues = (Object[]) parameterType.getMethod("values").invoke(parameterType);
                return enumValues[0];
            } catch (Exception e) {
                throw new RuntimeException("Exception invoking values() for enum " + parameterType, e);
            }
        } else if (Date.class.equals(parameterType)) {
            return new Date();
        } else if (BigDecimal.class.equals(parameterType)) {
            return new BigDecimal(0);
        } else if (BaseForceObject.class.isAssignableFrom(parameterType)) {
            // For reference fields, try getting a previously inserted object
            // (These objects are topographically sorted by reference)
            return insertedObjects.get(parameterType);
        }
        
        throw new RuntimeException("Cannot find setter method argument for parameter with type " + parameterType);
    }
    
    private void populateExtraFields(Object object) throws Exception {
        Class<?> generatedClass;
        if ((generatedClass = generatedClassNameMap().get("Asset")) != null
                && object.getClass() == generatedClass) {
            Class<?> accountClass = generatedClassNameMap().get("Account");
            
            // Either Account or Contact must be specified
            Object acctObject = insertedObjects.get(accountClass);
            generatedClass.getMethod("setAccount", accountClass).invoke(object, acctObject);
        } else if ((generatedClass = generatedClassNameMap().get("CallCenter")) != null
                && object.getClass() == generatedClass) {
            
            // Version must be a positive number
            generatedClass.getMethod("setVersion", double.class).invoke(object, 1d);
        } else if ((generatedClass = generatedClassNameMap().get("Contract")) != null
                && object.getClass() == generatedClass) {
            
            // Contract Term must be a positive value
            generatedClass.getMethod("setContractTerm", int.class).invoke(object, 1);
        } else if ((generatedClass = generatedClassNameMap().get("Folder")) != null
                && object.getClass() == generatedClass) {
            
            // Unfortunately, this isn't marked as required
            generatedClass.getMethod("setDeveloperName", String.class).invoke(object, "testGeneratedClassCUD");
        } else if ((generatedClass = generatedClassNameMap().get("Group")) != null
                && object.getClass() == generatedClass) {
            Class<?> typeClass = generatedEnumNameMap().get("Group$TypeEnum");
            Field regularType = typeClass.getField("REGULAR");
            
            // IncludeBosses should only be specified for public groups
            generatedClass.getMethod("setType", typeClass).invoke(object, regularType.get(typeClass));
            generatedClass.getMethod("setDoesIncludeBosses", boolean.class).invoke(object, false);
        } else if ((generatedClass = generatedClassNameMap().get("UserRole")) != null
                && object.getClass() == generatedClass) {
            Class<?> opportunityAccessClass = generatedEnumNameMap().get("UserRole$OpportunityAccessForAccountOwnerEnum");
            Field editOpportunityAccess = opportunityAccessClass.getField("EDIT");
            
            Class<?> caseAccessClass = generatedEnumNameMap().get("UserRole$CaseAccessForAccountOwnerEnum");
            Field editCaseAccess = caseAccessClass.getField("EDIT");
            
            Class<?> contactAccessClass = generatedEnumNameMap().get("UserRole$ContactAccessForAccountOwnerEnum");
            Field editContactAccess = contactAccessClass.getField("EDIT");
            
            // Access levels must be at or above organization defaults
            generatedClass.getMethod("setOpportunityAccessForAccountOwner", opportunityAccessClass)
                          .invoke(object, editOpportunityAccess.get(opportunityAccessClass));
            generatedClass.getMethod("setCaseAccessForAccountOwner", caseAccessClass)
                          .invoke(object, editCaseAccess.get(caseAccessClass));
            generatedClass.getMethod("setContactAccessForAccountOwner", contactAccessClass)
                          .invoke(object, editContactAccess.get(contactAccessClass));
        }
    }

    @DataProvider
    protected Object[][] objectsToDeleteProvider() throws IOException, ClassNotFoundException {
        // Reverse the insert order to get the delete order
        Map<Class<?>, Object> reverseInsertedObjects = insertedObjects.descendingMap();
        Object[][] reverseInsertedObjectArray = new Object[reverseInsertedObjects.size()][2];
        int i = 0;
        for (Map.Entry<Class<?>, Object> insertedObject : reverseInsertedObjects.entrySet()) {
            reverseInsertedObjectArray[i] = new Object[] { insertedObject.getKey(), insertedObject.getValue() };
            i++;
        }

        return reverseInsertedObjectArray;
    }
    
    // NOTE: This requires you to run testGeneratedClassCreateUpdate beforehand
    @Test(alwaysRun = true, dataProvider = "objectsToDeleteProvider", dependsOnMethods = "testGeneratedClassCreateUpdate")
    public void testGeneratedClassDelete(Class<?> generatedClass, Object object) {
        if (object instanceof BaseForceObject) {
            BaseForceObject forceObj = (BaseForceObject) object;
            if (forceObj.getId() != null) {
                delete(em, generatedClass, " o where o.id = '" + forceObj.getId() + "'");
                insertedObjects.remove(generatedClass);
            }
        }
    }

}
