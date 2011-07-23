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

package com.force.sdk.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.OMFContext;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.*;

import com.force.sdk.jpa.table.ColumnImpl;
import com.force.sdk.jpa.table.ForceColumnMetaData;
import com.sforce.soap.metadata.FieldType;

/**
 * 
 * General utility methods needed while persisting and retrieving objects.
 *
 * @author Fiaz Hossain
 * @author Jill Wetzler
 */
public final class PersistenceUtils {

    /**
     * prefix for member metadata extensions on picklist values.
     */
    public static final String PICKLIST_VALUE_FIELD_PREFIX = "pv.";

    /**
     * prefix for member metadata extensions on join filters.
     */
    public static final String JOIN_FILTER = "jf.";

    
    private PersistenceUtils() {  }
    
    /**
     * 
     * Returns the name of the entity, first checking the name attribute of the annotations and then
     * defaulting to the entity name on the metadata object.
     * 
     * @param acmd  the metadata object of the JPA entity
     * @return  the name of the entity to use in JPA (entity name or JPQL queries)
     */
    public static String getEntityName(AbstractClassMetaData acmd) {
        Map<String, String> extensions = getForceExtensions(acmd);
        String name = extensions.get("name") != null ? extensions.get("name") : acmd.getEntityName();
        return name != null ? name : acmd.getName();
    }
    
    /**
     * 
     * Returns whether an entity has no schema.
     * An entity does not have schema if it would not directly represent an object in Force.com;
     * for example, if the entity is embedded or if it's a subclass of another entity
     * 
     * @param acmd  the metadata object of the JPA entity
     * @return  {@code true} if the entity does not have schema
     */
    public static boolean hasNoSchema(AbstractClassMetaData acmd) {
        return acmd.isEmbeddedOnly() || acmd.getInheritanceMetaData() != null
                && acmd.getInheritanceMetaData().getStrategy() == InheritanceStrategy.SUBCLASS_TABLE;
    }
    
    /**
     * 
     * Performs various checks to see if the schema is read only (i.e. schema should not be
     * created, updated, or deleted when the application starts up, regardless of whether schema
     * creation is enabled).
     * 
     * @param acmd  the metadata object of the JPA entity
     * @param checkHierarchy  generally, we don't need to check the hierarchy if we're creating fields, but
     *                        if we're creating tables, we need to check the parent to see if the parent object
     *                        is read only
     * @return  {@code true} if the schema is read only.
     */
    public static boolean isReadOnlySchema(AbstractClassMetaData acmd, boolean checkHierarchy) {
        boolean isReadOnlySchema = false;
        do {
            isReadOnlySchema = isReadOnlySchemaInternal(acmd);
            acmd = acmd.getSuperAbstractClassMetaData();
        } while (!isReadOnlySchema && checkHierarchy && acmd != null);
        return isReadOnlySchema;
    }
    
    private static boolean isReadOnlySchemaInternal(AbstractClassMetaData acmd) {
        Map<String, String> extensions = getForceExtensions(acmd);
        String value = extensions.get("readOnlySchema");
        if (value == null) value = extensions.get("virtualSchema");  // Virtual schema is always read only
        
        return value != null && Boolean.valueOf(value);
    }
    
    private static boolean isReadOnlyFieldSchema(AbstractMemberMetaData ammd, OMFContext omf) {
        // Load up the class metadata for the owning class of this member (for example, if an inherited field,
        // the super class which has declared it)
        AbstractClassMetaData owningAcmd =
            omf.getMetaDataManager().getMetaDataForClass(ammd.getClassName(), omf.getClassLoaderResolver(null));
        return isReadOnlySchemaInternal(owningAcmd);
    }
    
    /**
     * 
     * A JPA entity is virtual schema if it is not backed by an
     * object on Force.com (for example, Owner is not backed by an
     * Owner object on Force.com).
     * 
     * @param acmd the metadata object of the JPA entity
     * @return  {@code true} if the entity is considered virtual schema
     */
    public static boolean isVirtualSchema(AbstractClassMetaData acmd) {
        Map<String, String> extensions = getForceExtensions(acmd);
        String value = extensions.get("virtualSchema");
        return value != null && Boolean.valueOf(value);
    }
    
    /**
     * 
     * Checks whether a column has a Persistence Modifier that is anything other than
     * PERSISTENT.
     * 
     * @param ammd  the metadata object representing a column or field
     * @return true if the column is not persistent
     */
    public static boolean isNonPersistedColumn(AbstractMemberMetaData ammd) {
        return ammd.getPersistenceModifier() != FieldPersistenceModifier.PERSISTENT;
    }
 
    /**
     * 
     * Utility method that returns only the Force.com specific extensions for a piece of
     * metadata (either an entity or a field).
     * 
     * @param md  The metadata containing extensions
     * @return    a map of the extension key and value of the extension (limited to Force.com extensions)
     */
    public static Map<String, String> getForceExtensions(MetaData md) {
        Map<String, String> map = new HashMap<String, String>(4);
        ExtensionMetaData[] extensions = md.getExtensions();
        if (extensions != null && extensions.length > 0) {
            for (ExtensionMetaData e : extensions) {
                if (ForceStoreManager.FORCE_KEY.equals(e.getVendorName())) {
                    map.put(e.getKey(), e.getValue());
                }
            }
        }
        return map;
    }

    /**
     * 
     * Utility method to get the field type from a map of extensions.
     * 
     * @param extensions  a Map of Force.com extensions on a metadata object
     * @return            if a type has been specified, parse to a FieldType, otherwise return {@code null}
     */
    public static FieldType getFieldTypeFromForceAnnotation(Map<String, String> extensions) {
        String value = extensions.get("type");
        return value != null ? FieldType.valueOf(value) : null;
    }

    /**
     * 
     * A check to see if a field has an ordinal enum type, meaning it's annotated with
     * {@code @Enumerated(EnumType.ORDINAL)}.  Callers of this method should have already verified
     * ammd to be a picklist field.
     * 
     * @param ammd  the metadata object of a picklist field
     * @return      {@code true} if the field is an ordinal enum
     */
    public static boolean isOrdinalEnum(AbstractMemberMetaData ammd) {
        ColumnMetaData[] cmds = ammd.getColumnMetaData();
        if (cmds != null && cmds.length > 0 && "INTEGER".equals(cmds[0].getJdbcType())) return true;
        // If the fast method fails take the slow route
        Enumerated enumerated = ((AccessibleObject) ammd.getMemberRepresented()).getAnnotation(Enumerated.class);
        return enumerated != null && (enumerated.value() == null || enumerated.value() == EnumType.ORDINAL);
    }
    
    /**
     * 
     * Checks whether a field is a multiselect picklist or a single select picklist.
     * Callers of this method should have already verified ammd to be a picklist field
     * 
     * @param ammd  the metadata object of a picklist field
     * @return      {@code true} if the field represents a multi select picklist, {@code false} otherwise.
     */
    public static boolean isMultiPicklist(AbstractMemberMetaData ammd) {
        // Somehow when we create an Enum[] and annotate with @Enumerated
        // it is not picked up by DataNucleus and included into ColumnMetadata
        if (ammd.getMemberRepresented() == null) return false;
        return ((AccessibleObject) ammd.getMemberRepresented()).isAnnotationPresent(Enumerated.class)
                && (ammd.getType().isArray() && ammd.getType().getComponentType().isEnum() || ammd.getType() == String[].class);
    }
    
    /**
     * 
     * Checks whether the field is a ManyToOne or OneToMany relationship (the only relationships currently
     * supported).
     * 
     * @param ammd  the metadata object for a field
     * @return      {@code true} if this field is a relationship field
     */
    public static boolean isRelationship(AbstractMemberMetaData ammd) {
        AccessibleObject methodOrProp = (AccessibleObject) ammd.getMemberRepresented();
        if (methodOrProp == null) return false;
        if (methodOrProp.isAnnotationPresent(OneToMany.class)
                || methodOrProp.isAnnotationPresent(ManyToOne.class)) {
            return true;
        }
        return false;
    }
    
    /**
     * 
     * Retrieves the value stored at a particular position in an entity.
     * 
     * @param acmd  the class metadata for the entity that holds the value
     * @param position  the position of the field you want the value for
     * @param entity  the actual entity that contains the value
     * @return  the value stored in the entity at the position
     * @throws IllegalAccessException  thrown if the field cannot be accessed
     * @throws InvocationTargetException  thrown if the field's get method cannot be invoked
     */
    public static Object getMemberValue(AbstractClassMetaData acmd, int position, Object entity)
        throws IllegalAccessException, InvocationTargetException {
        return getMemberValue(acmd.getMetaDataForManagedMemberAtAbsolutePosition(position).
                getMemberRepresented(), entity);
    }
    
    /**
     * 
     * Retrieves the value of a field on an entity via reflection.
     * 
     * @param member  the Member object for a particular field on an entity
     * @param entity  the entity that contains the value
     * @return  the value stored in the entity's member object
     * @throws IllegalAccessException thrown if the member cannot be accessed
     * @throws InvocationTargetException thrown if the member's get method cannot be invoked
     */
    public static Object getMemberValue(Member member, Object entity) throws IllegalAccessException, InvocationTargetException {
        if (member instanceof Method) {
            Method m = (Method) member;
            if (!m.isAccessible()) {
                m.setAccessible(true);
            }
            return m.invoke(entity, (Object[]) null);
        } else if (member instanceof Field) {
            Field f = (Field) member;
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            return f.get(entity);
        }
        return null;
    }
    
    /**
     * 
     * Returns the annotation of type T on a field or method. For example, you can use this method to get
     * the {@code @Column} or {@code @JoinFilter} annotation on a getter method for a column
     * 
     * @param <T>  an Annotation type
     * @param member the member representing a particular field or method
     * @param annotationClass  the class of the annotation object that should be returned
     * @return  the Annotation on this field or method
     */
    public static <T extends Annotation> T getMemberAnnotation(Member member, Class<T> annotationClass) {
        if (member instanceof Method) {
            Method m = (Method) member;
            return m.getAnnotation(annotationClass);
        } else if (member instanceof Field) {
            Field f = (Field) member;
            return f.getAnnotation(annotationClass);
        }
        return null;
    }

    /**
     * 
     * Sets the value of a field on an entity.
     * 
     * @param clazz  The entity class
     * @param acmd  metadata object for the entity
     * @param position  the position of the field that should be set
     * @param entity  the instance of the entity being updated
     * @param value  the value that should be set on the field
     * @throws SecurityException  thrown if the field cannot be accessed
     * @throws NoSuchFieldException  thrown if the field does not exist
     * @throws IllegalArgumentException  thrown if the set fails
     * @throws IllegalAccessException  thrown if the set fails
     */
    public static void setFieldValue(Class clazz, AbstractClassMetaData acmd, int position, Object entity, Object value)
        throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field field = clazz.getDeclaredField(acmd.getMetaDataForManagedMemberAtAbsolutePosition(position).getName());
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        field.set(entity, value);
    }
    
    private static String getForceApiRelationshipName(AbstractMemberMetaData ammd, Map<String, String> extensions,
            OMFContext omf) {
        AbstractClassMetaData acmd =
            PersistenceUtils.getMemberElementClassMetaData(ammd, omf.getClassLoaderResolver(null), omf.getMetaDataManager());
        AbstractMemberMetaData mmd = acmd.getMetaDataForMember(ammd.getMappedBy());
        if (mmd == null) {
            // The mappedBy field does not exist
            throw new NucleusUserException("Cannot access mappedBy field " + ammd.getMappedBy()
                                            + " on entity " + acmd.getName() + " at " + ammd.getFullFieldName());
        }
        String tableName = acmd.getEntityName();
        String fieldNameNoSuffix = getFieldName(mmd, extensions);
        extensions = getForceExtensions(mmd);
        String childRelationshipName = extensions.get("childRelationshipName");
        String relationshipName =
            childRelationshipName != null ? childRelationshipName : String.format("%s_%ss", fieldNameNoSuffix, tableName);
        if (relationshipName.length() > 40) {
            relationshipName = relationshipName.substring(0, 40);
        }
        return relationshipName;
    }
    
    /**
     * 
     * Utility method to determine the name of a field based on the member metadata. Field names can be set
     * via JPA or Force.com annotations, and relationship fields require special handling.
     * 
     * @param ammd  the metadata object for the field we're retrieving a name for
     * @param omf   the Object Manager Factory context
     * @return the name of a field/column as it would appear in the Force.com API
     */
    public static String getForceApiName(AbstractMemberMetaData ammd, OMFContext omf) {
        Map<String, String> extensions = getForceExtensions(ammd);
        
        if (ammd.getCollection() != null || ammd.getMap() != null) {
            String relationshipName = getForceApiRelationshipName(ammd, extensions, omf);
            
            // If the relationship already exists
            if (isReadOnlyFieldSchema(ammd, omf)) {
                return relationshipName;
            }
            
            // Append the custom suffix if it's missing
            if (!relationshipName.endsWith(ColumnImpl.CUSTOM_RELATIONSHIP_SUFFIX)) {
                return relationshipName + ColumnImpl.CUSTOM_RELATIONSHIP_SUFFIX;
            }
            
            // Not a standard relationship and already ends with __r
            return relationshipName;
        } else {
            // Get the java field name
            String fieldName = getFieldName(ammd, extensions);
            
            // Standard fields names are the same as the java field name
            // Also, if the entity is read only, the schema already exists in Force.com
            // so we should just use whatever java field name is loaded
            // (e.g. standard field name, @Column annotation)
            if (ForceColumnMetaData.STANDARD_FIELDS.contains(fieldName.toLowerCase())
                    || isReadOnlyFieldSchema(ammd, omf)) {
                return fieldName;
            }
            
            // Append the custom suffix if it's missing
            if (!fieldName.endsWith(ColumnImpl.CUSTOM_THING_SUFFIX)) {
                return fieldName + ColumnImpl.CUSTOM_THING_SUFFIX;
            }
            
            // Not a standard field and already ends with __c
            return fieldName;
        }
    }
    
    /**
     * 
     * Utility method to prepend a namespace to a name, namespace can be {@code null}.
     * 
     * @param namespace  A possibly {@code null} namespace string 
     * @param name  the name of a field or object
     * @return  the name if namespace is {@code null}, otherwise the appended string
     */
    public static String prependNamespace(String namespace, String name) {
        return namespace == null ? name : String.format("%s__%s", namespace, name);
    }
    
    /**
     * 
     * Gets the class metadata for a member's type. For collections, get the metadata for the type parameter
     * 
     * @param ammd  The member whose class metadata you want to retrieve
     * @param clr  the classloader resolver to use
     * @param mdm  the metadata manager to use
     * @return  the class metadata object for the given member 
     */
    public static AbstractClassMetaData getMemberElementClassMetaData(AbstractMemberMetaData ammd, ClassLoaderResolver clr,
            MetaDataManager mdm) {
        if (ammd.getCollection() != null) {
            return ammd.getCollection().getElementClassMetaData(clr, mdm);
        } else if (ammd.getMap() != null) {
            return ammd.getMap().getValueClassMetaData(clr, mdm);
        } else {
            return mdm.getMetaDataForClass(ammd.getType(), clr);
        }
    }
    
    /**
     * 
     * Gets the name of a field for use with JPA. First, try to get the name from the Force.com annotations,
     * then try to get the name from standard JPA annotations, otherwise use the name of the field in Java.
     * 
     * @param ammd  the metadata object for the field we're retrieving a name for
     * @param extensions  the Force.com extensions for this metadata (see getForceExtensions}
     * @return  the name of the field for JPQL
     */
    public static String getFieldName(AbstractMemberMetaData ammd, Map<String, String> extensions) {
        // First, try to get the field name from the @CustomObject annotation
        String fieldName = extensions.get("name");
        
        if (fieldName != null) {
            return fieldName;
        }
        
        // Next, try to get the field name from JPA annotations
        fieldName = getFieldNameFromJPAAnnotation(ammd);
        if (fieldName == null) {
            // Fall back to the field/property name
            fieldName = ammd.getName();
        }
        if (fieldName.indexOf(":") > -1) {
            fieldName = fieldName.substring(fieldName.indexOf(":") + 1);
        }
        
        return fieldName;
    }
    
    /**
     * 
     * Returns the name of a column as specified in a JPA annotation (can possibly be {@code null}).
     * 
     * @param ammd  the metadata object for the field we're retrieving a name for
     * @return  {@code null} if the field name is not defined by a JPA annotation, otherwise return the field name
     */
    public static String getFieldNameFromJPAAnnotation(AbstractMemberMetaData ammd) {
        String fieldName = null;
        
        // Try the first column if specified
        ColumnMetaData[] colmds = ammd.getColumnMetaData();
        ElementMetaData elemmd;
        if (colmds != null && colmds.length > 0) {
            fieldName = colmds[0].getName();
            
        // In some cases (for example Collections and Arrays)
        // the ColumnMetaData will be embedded in ElementMetaData
        } else if ((elemmd = ammd.getElementMetaData()) != null) {
            colmds = elemmd.getColumnMetaData();
            if (colmds != null && colmds.length > 0) {
                fieldName = colmds[0].getName();
            }
        }
        
        return fieldName;
    }
}
