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

package com.force.sdk.jpa.mock;

import static mockit.Deencapsulation.invoke;
import static mockit.Deencapsulation.newInstance;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

import org.datanucleus.OMFContext;
import org.datanucleus.PersistenceConfiguration;
import org.datanucleus.jpa.metadata.JPAMetaDataManager;
import org.datanucleus.metadata.MetaDataManager;

import com.force.sdk.jpa.annotation.CustomField;
import com.force.sdk.jpa.annotation.CustomObject;
import com.force.sdk.jpa.table.*;
import com.sforce.soap.partner.Field;

/**
 * A mocked PersistenceUtils class.
 * <p>
 * This class can construct a TableImpl using the 
 * @MockApiEntity, @MockApiField and @MockApiRelationship
 * annotations.
 * 
 * @author Tim Kral
 */
public final class MockPersistenceUtils {
    
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern INTEGER_PATTERN = Pattern.compile("(\\d+)");
    
    private MockPersistenceUtils() {  }
    
    public static TableImpl constructTableImpl(Class<?> entityClass) {
        
        MockApiEntity mockEntityAnnotation;
        if ((mockEntityAnnotation = entityClass.getAnnotation(MockApiEntity.class)) == null) {
            throw new IllegalArgumentException("The class " + entityClass.getName()
                    + " must be annotated with @MockJPAEntity to be included in the mock framework.");
        }

        // State needed to build a TableImpl
        List<ColumnImpl> columnList = new ArrayList<ColumnImpl>();
        Map<String, ColumnImpl> forceApiColumns = new HashMap<String, ColumnImpl>();
        ColumnImpl externalId = null;
        
        Set<java.lang.reflect.Field> entityFields =
            new HashSet<java.lang.reflect.Field>(Arrays.<java.lang.reflect.Field>asList(entityClass.getDeclaredFields()));
        
        // Loop through the class hierarchy to load fields from super classes
        Class superClass = entityClass;
        while ((superClass = superClass.getSuperclass()) != null) {
            for (java.lang.reflect.Field superClassField : superClass.getDeclaredFields()) {
                int modifiers = superClassField.getModifiers();
                
                // Add public and protected fields from super classes
                if (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)) {
                    entityFields.add(superClassField);
                }
            }
        }
        
        for (java.lang.reflect.Field entityField : entityFields) {
            MockApiField mockFieldAnnotation;
            MockApiRelationship mockRelationshipAnnotation;
            ColumnImpl colImpl;
            String apiColumnKey;
            if ((mockFieldAnnotation = entityField.getAnnotation(MockApiField.class)) != null) {
            
                // Create an api field as described by @MockApiField
                Field apiField = new Field();
                apiField.setName(mockFieldAnnotation.name());
                apiField.setType(mockFieldAnnotation.type());
                apiField.setCustom(mockFieldAnnotation.custom());

                // Add any extra api field attributes
                // These should come in the form set<attrName>=attrValue
                for (String attr : mockFieldAnnotation.attrs()) {
                    String[] parsedAttr = attr.split("=", 2);
                    if (parsedAttr.length != 2)
                        throw new IllegalArgumentException("Could not parse api field attribute " + attr + ". "
                                + "It must be in the form 'set<attrName>=atrValue'");
                    
                    if (BOOLEAN_PATTERN.matcher(parsedAttr[1]).matches()) {
                        invoke(apiField, parsedAttr[0], Boolean.valueOf(parsedAttr[1]));
                    } else if (INTEGER_PATTERN.matcher(parsedAttr[1]).matches()) {
                        invoke(apiField, parsedAttr[0], Integer.valueOf(parsedAttr[1]));
                    } else {
                        // By default, treat as String argument
                        invoke(apiField, parsedAttr[0], parsedAttr[1]);
                    }
                }
                
                apiColumnKey = mockFieldAnnotation.name().toLowerCase();
                colImpl = new ColumnImpl(mockFieldAnnotation.name().toLowerCase(), apiField);
            } else if ((mockRelationshipAnnotation = entityField.getAnnotation(MockApiRelationship.class)) != null) {
                apiColumnKey = mockRelationshipAnnotation.name().toLowerCase();
                colImpl = new RelationshipImpl(mockRelationshipAnnotation.name());
            } else {
                continue;
            }
            
            columnList.add(colImpl);
            forceApiColumns.put(apiColumnKey, colImpl);
            
            // Set the externalId column
            if (entityField.isAnnotationPresent(CustomField.class)
                    && entityField.getAnnotation(CustomField.class).externalId()) {
                externalId = colImpl;
            }
        }
        
        // The default @MockApiEntity namespace is "" so treat this as null
        String namespace = null;
        if (!"".equals(mockEntityAnnotation.namespace())) {
            namespace = mockEntityAnnotation.namespace();
        }
        TableName tableName = newInstance(TableName.class, new Class[] {String.class, String.class, Boolean.TYPE},
                namespace, entityClass.getSimpleName().toLowerCase(), false /*parseForSuffix*/);
        
        // Construct TableImpl from non-public constructor
        return newInstance(TableImpl.class,
                           new Class[] {String.class, TableName.class, List.class, Map.class, ColumnImpl.class},
                                        namespace, tableName, columnList, forceApiColumns, externalId);
    }
    
    public static TableImpl constructVirtualTableImpl(Class<?> entityClass) {
        CustomObject customObjectAnnotation = entityClass.getAnnotation(CustomObject.class);
        if (customObjectAnnotation == null || !customObjectAnnotation.virtualSchema()) {
            throw new IllegalArgumentException("Trying to construct virtual schema for the class " + entityClass.getName()
                    + " but this class is not annotated as virtual schema.");
        }
        
        TableName tableName = newInstance(TableName.class, new Class[] {String.class, String.class, Boolean.TYPE},
                "" /*namespace*/, entityClass.getSimpleName().toLowerCase(), false /*parseForSuffix*/);
        
        // Construct a virtual TableImpl just like how the ForceStoreSchemaHandler would
        // (see ForceStoreSchemaHandler.addVirtualTable)
        OMFContext omfContext = new OMFContext(new PersistenceConfiguration() { });
        MetaDataManager metaDataManager = new JPAMetaDataManager(omfContext);
        return new TableImpl(tableName,
                             metaDataManager.getMetaDataForClass(entityClass, omfContext.getClassLoaderResolver(null)));
    }
}
