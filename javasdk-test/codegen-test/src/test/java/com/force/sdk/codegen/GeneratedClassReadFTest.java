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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.FetchType;

import org.apache.commons.lang.WordUtils;
import org.datanucleus.ObjectManager;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.ws.ConnectionException;


/**
 * Tests that generated JPA Java classes are 
 * enabled for JPA reads (e.g. find and queries).
 *
 * @author Tim Kral
 */
public class GeneratedClassReadFTest extends BaseGeneratedClassFTest {
    
    private Object[][] queryableClassesArray;
    
    @BeforeClass(dependsOnMethods = "loadGeneratedClasses")
    public void loadQueryableClasses() throws ConnectionException {
        loadEntityManager();
        
        // Run an API describe on all the generated classes
        Map<String, Class<?>> generatedClassMap = new HashMap<String, Class<?>>(generatedClassNameMap());
        DescribeSObjectResult[] dsrs =
            conn.describeSObjects(generatedClassMap.keySet().toArray(new String[generatedClassMap.size()]));
        
        // We'll remove those classes which are not API queryable
        for (DescribeSObjectResult dsr : dsrs) {
            if (!dsr.isQueryable()) {
                generatedClassMap.remove(dsr.getName());
            }
        }
        
        // Load the queryable classes into the form used by a TestNG DataProvider
        List<Class<?>> queryableClassesList = new ArrayList<Class<?>>(generatedClassMap.values());
        queryableClassesArray = new Object[queryableClassesList.size()][1];
        for (int i = 0; i < queryableClassesList.size(); i++) {
            queryableClassesArray[i] = new Object[] { queryableClassesList.get(i) };
        }
    }
    
    @DataProvider
    protected Object[][] queryableClassProvider() {
        return queryableClassesArray;
    }
    
    // Tests that we can execute a find() for each generated Java class
    @SuppressWarnings("unchecked")
    @Test(dataProvider = "queryableClassProvider")
    public void testGeneratedClassFind(Class generatedClass) {
        // This shouldn't return any results.  We just want to ensure that
        // find() can be run without error
        em.find(generatedClass, "deadbeef");
    }

    @Test(dataProvider = "queryableClassProvider")
    public void testGeneratedClassQueryCommonFields(Class generatedClass) {
        ObjectManager om = (ObjectManager) em.getDelegate();
        AbstractClassMetaData acmd = om.getMetaDataManager().getMetaDataForClass(generatedClass, null);
        
        StringBuffer selectCommonFields = new StringBuffer("o.id");
        if (acmd.hasMember("name")) selectCommonFields.append(", o.name"); // Not every entity has a name field
        
        // We don't care about the results, just that we can execute a query without error
        String entityQueryName = getEntityQueryName(generatedClass);
        em.createQuery("select " + selectCommonFields + " from " + entityQueryName + " o").getResultList();
    }
    
    // Tests that we can execute a basic JPQL query for each generated Java class
    @Test(dataProvider = "queryableClassProvider")
    public void testGeneratedClassQueryAllFields(Class generatedClass) {
        // We don't care about the results, just that we can execute a query without error
        String entityQueryName = getEntityQueryName(generatedClass);
        em.createQuery("select o from " + entityQueryName + " o").getResultList();
    }
    

    
    // DataProvider for each lazy fetch field on each persisteable generated Java class
    // The resulting Object[][] is { generatedClass, List of lazy fetch fields }
    @DataProvider
    protected Object[][] lazyFieldProvider() throws SecurityException, NoSuchFieldException, IOException, ClassNotFoundException {
        Object[][] persistableGeneratedClassArray = queryableClassProvider();
        Object[][] lazyFieldArray = new Object[persistableGeneratedClassArray.length][2];
        
        // Loop through all persistable generated Java classes
        for (int i = 0; i < persistableGeneratedClassArray.length; i++) {
            Class generatedClass = (Class) persistableGeneratedClassArray[i][0];
            
            List<Field> lazyFieldList = new ArrayList<Field>();
            
            // Loop through each declared method on the generated class
            for (Method entityMethod : generatedClass.getDeclaredMethods()) {
                
                // If the method is marked as a lazy fetch type
                Basic basicAnnotation;
                if ((basicAnnotation = entityMethod.getAnnotation(Basic.class)) != null
                        && basicAnnotation.fetch() == FetchType.LAZY) {
                    
                    // Convert method name to field name (e.g. getFieldName -> fieldName)
                    String fieldName = entityMethod.getName().substring(3);
                    fieldName = WordUtils.uncapitalize(fieldName);
                    
                    lazyFieldList.add(generatedClass.getDeclaredField(fieldName));
                }
            }
            
            lazyFieldArray[i] = new Object[] { generatedClass, lazyFieldList };
        }
        
        return lazyFieldArray;
    }
    
    
    @Test(dataProvider = "lazyFieldProvider")
    public void testGeneratedClassQueryLazyFields(Class generatedClass, List<Field> lazyFields) {
        // If there are no lazy fields then 
        // this entity is completely covered in testGeneratedClassQuery
        if (lazyFields.isEmpty()) return;
        
        StringBuffer selectLazyFields = new StringBuffer();
        for (Field lazyField : lazyFields) {
            selectLazyFields.append("o.").append(lazyField.getName()).append(',');
        }
        
        // Remove the last comma
        selectLazyFields.deleteCharAt(selectLazyFields.lastIndexOf(","));
        
        // We don't care about the results, just that we can execute a query without error
        String entityQueryName = getEntityQueryName(generatedClass);
        em.createQuery("select " + selectLazyFields + " from " + entityQueryName + " o").getResultList();
    }
    
    @Test
    public void testGeneratedClassPicklist() throws Exception {
        Class<?> accountClass = generatedClassNameMap().get("Account");
        
        Class<?> industryClass = generatedEnumNameMap().get("Account$IndustryEnum");
        Field agricultureIndustryField = industryClass.getField("AGRICULTURE");
        
        // Load the String value of the (non-restricted) picklist value
        Object agricultureIndustryValue =
            industryClass.getMethod("value").invoke(agricultureIndustryField.get(industryClass));
        
        Object account = accountClass.newInstance();
        accountClass.getMethod("setName", String.class).invoke(account, "testGeneratedClassPicklist");
        accountClass.getMethod("setIndustry", String.class).invoke(account, agricultureIndustryValue);
        
        String accountId = null;
        try {
            persist(em, account);
            accountId = (String) accountClass.getMethod("getId").invoke(account);
            assertNotNull(accountId, "Null account id after persist.");
            
            em.clear();
            account = em.find(accountClass, accountId);
            
            assertEquals(accountClass.getMethod("getIndustry").invoke(account),
                    agricultureIndustryValue,
                    "Unexpected Account industry");
        } finally {
            if (accountId != null) {
                delete(em, accountClass, " a where a.id = '" + accountId + "'");
            }
        }
    }
    
    @Test
    public void testGeneratedClassRestrictedPicklist() throws Exception {
        Class<?> folderClass = generatedClassNameMap().get("Folder");
        

        Class<?> accessTypeClass = generatedEnumNameMap().get("Folder$AccessTypeEnum");
        Field publicAccessType = accessTypeClass.getField("PUBLIC");
        Class<?> typeClass = generatedEnumNameMap().get("Folder$TypeEnum");
        Field documentType = typeClass.getField("DOCUMENT");
        
        Object folder = folderClass.newInstance();
        folderClass.getMethod("setName", String.class).invoke(folder, "testGeneratedClassRestrictedPicklist");
        folderClass.getMethod("setDeveloperName", String.class).invoke(folder, "testGeneratedClassRestrictedPicklist");
        
        // Set restricted picklist values from the enum values
        folderClass.getMethod("setAccessType", accessTypeClass).invoke(folder, publicAccessType.get(accessTypeClass));
        folderClass.getMethod("setType", typeClass).invoke(folder, documentType.get(typeClass));
        
        String folderId = null;
        try {
            persist(em, folder);
            folderId = (String) folderClass.getMethod("getId").invoke(folder);
            assertNotNull(folderId, "Null folder id after persist.");
            
            em.clear();
            folder = em.find(folderClass, folderId);
        
            assertEquals(folderClass.getMethod("getAccessType").invoke(folder),
                            publicAccessType.get(accessTypeClass),
                            "Unexpected Folder access type");
            assertEquals(folderClass.getMethod("getType").invoke(folder),
                            documentType.get(typeClass),
                            "Unexpected Folder type");
        } finally {
            if (folderId != null) {
                delete(em, folderClass, " f where f.id = '" + folderId + "'");
            }
        }
    }
    
}
