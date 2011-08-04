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

package com.force.sdk.jpa.table;

import com.force.sdk.jpa.ForceStoreManager;
import com.force.sdk.jpa.PersistenceUtils;
import com.force.sdk.jpa.schema.ForceMemberMetaData;
import com.force.sdk.jpa.schema.ForceStoreSchemaHandler;
import com.sforce.soap.metadata.*;
import com.sforce.ws.types.Time;
import org.datanucleus.OMFContext;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.InheritanceStrategy;

import javax.persistence.*;
import java.lang.reflect.AccessibleObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 
 * Metadata for fields on Force.com objects. This class turns fields
 * in your @Entity java classes into CustomFields that can be read by the Force.com
 * Metadata API. Fields are stored off on the ForceTableMetaData object and optionally
 * handed to the ForceSchemaWriter
 *
 * @author Fiaz Hossain
 * @author Jill Wetzler
 */
public class ForceColumnMetaData extends ForceMetaData {
    
    private static final Pattern PICK_PATTERN = Pattern.compile(",");

    /**
     * fields that are standard on all Force.com objects.
     */
    public static final Set<String> STANDARD_FIELDS = new HashSet<String>(Arrays.<String>asList(
            "id",
            "name",
            "ownerid",
            "isdeleted",
            "createdbyid",
            "createddate",
            "lastmodifiedbyid",
            "lastmodifieddate",
            "systemmodstamp"));
    
    private final List<AbstractMemberMetaData> fieldsToAdd = new ArrayList<AbstractMemberMetaData>();
    private final ForceStoreManager storeManager;
    private final String fieldFormatString;

    private final List<CustomField> fields = new ArrayList<CustomField>();
    
    /**
     * Creates the column metadata for field registration and possible schema creation.
     * 
     * @param cmd  the class this column belongs to
     * @param table  the table where this column is found
     * @param storeManager  the store manager
     */
    public ForceColumnMetaData(AbstractClassMetaData cmd, TableImpl table, ForceStoreManager storeManager) {
        super(cmd, table);
        fieldFormatString = tableImpl.getTableName().getForceApiName() + ".%s";
        this.storeManager = storeManager;
    }
    
    /**
     * Adds all the fields for this table to the object.  We may need to merge with any existing fields
     * in the case of entities that extend other entities.  There is only one CustomObject representing all
     * the entities and we need the intersection of their fields
     * 
     * @param customObject CustomObject
     */
    public void addFieldsToObject(CustomObject customObject) {
        if (fields.size() > 0) {
            Collection<CustomField> mergedResult = mergeFields(customObject.getFields(), fields);
            customObject.setFields(mergedResult.toArray(new CustomField[mergedResult.size()]));
            fields.clear();
        }
    }
    
    private Collection<CustomField> mergeFields(CustomField[] existing, List<CustomField> toAdd) {
        if (existing == null || existing.length == 0) return toAdd;
        Map<String, CustomField> result = new HashMap<String, CustomField>();
        for (CustomField cf : existing) {
            result.put(String.format(fieldFormatString, cf.getFullName()), cf);
        }
        for (CustomField cf : toAdd) {
            result.put(String.format(fieldFormatString, cf.getFullName()), cf);
        }
        return result.values();
    }

    /**
     * 
     * Checks to see if columns should be created according to persistence properties, and
     * if so, creates CustomField objects and stores them on the parent table to
     * be created later by the {@link com.force.sdk.jpa.schema.ForceSchemaWriter}.
     * 
     * @param namespace  the namespace of the organization creating schema
     */
    public void createFieldSchema(String namespace) {
        updateListOfMissingFields();
        if (fieldsToAdd.size() > 0) {
            if (storeManager.isAutoCreateColumns()) {
                List<AbstractClassMetaData> invalidateList = createColumns(namespace);
                for (AbstractClassMetaData tcmd : invalidateList) {
                    storeManager.getTable(tcmd).setIsValid(false);
                }
            } else {
                StringBuilder msg = new StringBuilder(1024);
                msg.append("Field does not exist in force.com table and datanucleus.autoCreateColumns is false, entity: ")
                   .append(cmd.getName()).append(" table: ")
                   .append(tableImpl.getTableName().getForceApiName()).append(" fields: [");
                int count = 0;
                for (AbstractMemberMetaData ammd : fieldsToAdd) {
                    if (count++ > 0) msg.append(", ");
                    msg.append(ammd.getName());
                }
                msg.append("]");
                if (storeManager.isAutoCreateWarnOnError()) {
                    LOGGER.warn(msg.toString());
                } else {
                    throw new NucleusUserException(msg.toString());
                }
            }
        }
    }

    private void validateColumn(AbstractMemberMetaData ammd, OMFContext omf) {
        Map<String, String> extensions = PersistenceUtils.getForceExtensions(ammd);
        FieldType fieldType = PersistenceUtils.getFieldTypeFromForceAnnotation(extensions);
        AccessibleObject ao = (AccessibleObject) ammd.getMemberRepresented();
        if (ao.isAnnotationPresent(OneToOne.class)) {
            throw new NucleusUserException("@OneToOne relationship is not supported. Please combine fields into a single object."
                                            + " Offending field: " + ammd.getFullFieldName());
        } else if (ao.isAnnotationPresent(OneToMany.class)) {
            if (ammd.getMappedBy() == null) {
                throw new NucleusUserException("@OneToMany relationship requires the 'mappedBy' attribute."
                                                + " Please add a foreign key field on child object and use that field name"
                                                + " for mappedBy attribute."
                                                + " Offending field: " + ammd.getFullFieldName());
            }

            Basic b;
            if (ao.getAnnotation(OneToMany.class).fetch() == FetchType.EAGER
                    || ((b = ao.getAnnotation(Basic.class)) != null && b.fetch() == FetchType.EAGER)) {
                throw new NucleusUserException("@OneToMany relationships with FetchType of EAGER are currently not supported.");
            }
        } else if (ao.isAnnotationPresent(ManyToMany.class)) {
            throw new NucleusUserException("@ManyToMany relationship is not supported."
                                            + " Please use a junction object with two @ManyToOne relationships."
                                            + " Offending field: " + ammd.getFullFieldName());
        }
        if (ammd.getJoinMetaData() != null) {
            throw new NucleusUserException("@JoinTable is not supported. Offending field: " + ammd.getFullFieldName());
        }
        if (ammd.getColumnMetaData() != null && ammd.getColumnMetaData().length > 0) {
            for (ColumnMetaData md : ammd.getColumnMetaData()) {
                if (md.getTarget() != null && !md.getTarget().equalsIgnoreCase("id")) {
                    throw new NucleusUserException("Referenced foreign key colum name must be 'ID'."
                                                    + " Offending field: " + ammd.getFullFieldName());
                } else if ("CLOB".equals(md.getJdbcType())) {
                    throw new NucleusUserException("@Clob field type is not supported."
                                                    + " Offending field: " + ammd.getFullFieldName());
                }
            }
            if (ammd.getTable() != null) {
                throw new NucleusUserException("Table cannot be specified at a column level."
                                                + " Offending field: " + ammd.getFullFieldName());
            }
        }
        if (FieldType.Picklist == fieldType || ao.isAnnotationPresent(Enumerated.class)) {
            if (!ammd.getType().isEnum() && !(ammd.getType().isArray() && ammd.getType().getComponentType().isEnum())) {
                // This is a non-strict picklist. Non-strict picklists allow only String or String[]
                if (ammd.getType() != String.class && ammd.getType() != String[].class) {
                    throw new NucleusUserException("Non-strict picklist can be of type String or String[] only."
                                                    + " Offending field: " + ammd.getFullFieldName());
                }
                String allValues = extensions.get(PersistenceUtils.PICKLIST_VALUE_FIELD_PREFIX + "value");
                if (allValues == null) {
                    throw new NucleusUserException("Non-strict picklist requires @PicklistValue annotation."
                                                    + " Offending field: " + ammd.getFullFieldName());
                }
                if (PersistenceUtils.isOrdinalEnum(ammd)) {
                    throw new NucleusUserException("Non-strict picklist does not support EnumType.ORDINAL."
                                                    + " Offending field: " + ammd.getFullFieldName());
                }
            }
        }
        if (ammd.getCollection() != null || ammd.getMap() != null) {
            /**
             * We do not want JPA set cascade delete for MasterDetail relationships.
             * It's a no-op but quite inefficient since JPA tries to delete all children individually too.
             * Also, for AllOrNothing transactions trying to delete all children
             * is an error not just an inefficient call.
             * 
             * So check if this is a MasterDetail relationship. If it is then unset Cascade delete.
             */
            FieldType type =
                PersistenceUtils.getFieldTypeFromForceAnnotation(
                        PersistenceUtils.getForceExtensions(ammd.getRelatedMemberMetaData(omf.getClassLoaderResolver(null))[0]));
            if (type != null && type == FieldType.MasterDetail) {
                if (ammd.getCollection() != null) {
                    ammd.getCollection().setDependentElement(false);
                } else if (ammd.getMap() != null) {
                    ammd.getMap().setDependentValue(false);
                }
            }
        }
    }
    
    private void updateListOfMissingFields() {
        
        for (AbstractMemberMetaData ammd : findAllFields()) {
            validateColumn(ammd, storeManager.getOMFContext());
            AbstractClassMetaData relatedEntity =
                storeManager.getMetaDataManager().getMetaDataForEntityName(ammd.getType().getSimpleName());
            if (relatedEntity != null && relatedEntity.isEmbeddedOnly()) {
                // Add all embedded fields to embedding entity
                for (AbstractMemberMetaData eammd : ammd.getEmbeddedMetaData().getMemberMetaData()) {
                    addFieldToListIfAbsent(eammd, storeManager);
                }
            } else {
                addFieldToListIfAbsent(ammd, storeManager);
            }
        }
        
        // If there is a discriminator column add that too
        if (cmd.getSuperAbstractClassMetaData() == null && cmd.getDiscriminatorMetaData() != null
                && cmd.getDiscriminatorMetaData().getColumnMetaData() != null) {
            ColumnMetaData colmd = cmd.getDiscriminatorMetaData().getColumnMetaData();
            addFieldToListIfAbsent(new ForceMemberMetaData(colmd, colmd.getName()) {
                @Override
                public Class<?> getType() {
                    return String.class;
                }
            }, storeManager);
        }
    }
    
    private List<AbstractMemberMetaData> findAllFields() {
        List<AbstractMemberMetaData> allFields = new ArrayList<AbstractMemberMetaData>();
        
        // Load up fields from this class
        AbstractMemberMetaData[] membersInThisClass =  cmd.getManagedMembers();
        if (membersInThisClass != null) allFields.addAll(Arrays.asList(membersInThisClass));
        
        // Load up fields from MappedSuperclasses in class hierarchy
        AbstractClassMetaData superClassMD = cmd.getSuperAbstractClassMetaData();
        while (superClassMD != null
                && superClassMD.getInheritanceMetaData().getStrategy() == InheritanceStrategy.SUBCLASS_TABLE) {
            AbstractMemberMetaData[] membersInSuperClass =  superClassMD.getManagedMembers();
            if (membersInSuperClass != null) allFields.addAll(Arrays.asList(membersInSuperClass));
            superClassMD = superClassMD.getSuperAbstractClassMetaData();
        }
        
        return allFields;
    }
    
    private void addFieldToListIfAbsent(AbstractMemberMetaData ammd, ForceStoreManager storeMgr) {
        if (PersistenceUtils.isNonPersistedColumn(ammd)) return;
        if (!tableImpl.isValid() || storeManager.isForDelete()
                || tableImpl.getColumnByForceApiName(
                        PersistenceUtils.getForceApiName(ammd, storeManager.getOMFContext())) == null) {
            fieldsToAdd.add(ammd);
        }
    }
    
    /**
     * Registers the missing columns for creations.
     * 
     * @param namespace  the namespace of the organization creating the schema
     * @return all the {@code AbstractClassMetaData} objects that need to be invalidated
     */
    private List<AbstractClassMetaData> createColumns(String namespace) {
        
        List<AbstractClassMetaData> ret = new ArrayList<AbstractClassMetaData>();
        if (fieldsToAdd != null && fieldsToAdd.size() > 0) {
            ret.add(cmd);

            for (int column = 0; column < fieldsToAdd.size(); column++) {
                AbstractMemberMetaData ammd = fieldsToAdd.get(column);
                if (Collection.class.isAssignableFrom(ammd.getType()) || Map.class.isAssignableFrom(ammd.getType())) {
                    // skip this, we'll make the relationship the other way
                    // but we need to add the relationship column for @OneToMany
                    // however, can't poke into the child object yet because the filed might not have been created yet
                    continue;
                }
                Map<String, String> extensions = PersistenceUtils.getForceExtensions(ammd);
                String fieldName = PersistenceUtils.getForceApiName(ammd, storeManager.getOMFContext());
                AbstractClassMetaData relatedClass = ammd.getType() != null
                        ? storeManager.getMetaDataManager().getMetaDataForClass(ammd.getType(),
                                storeManager.getOMFContext().getClassLoaderResolver(null)) : null;
                // Try the first column if specified
                ColumnMetaData[] colmds = ammd.getColumnMetaData();
                ColumnMetaData colmd = null;
                FieldType type = PersistenceUtils.getFieldTypeFromForceAnnotation(extensions);
                Integer scale = getIntegerFromForceAnnotation(extensions, "scale");
                Integer precision = getIntegerFromForceAnnotation(extensions, "precision");
                Integer length = getIntegerFromForceAnnotation(extensions, "length");
                Integer startValue = getIntegerFromForceAnnotation(extensions, "startValue");
                boolean enableFeeds = getBooleanFromForceAnnotation(extensions, "enableFeeds");
                boolean externalId = getBooleanFromForceAnnotation(extensions, "externalId");
                String childRelationshipName = extensions.get("childRelationshipName");
                String label = extensions.get("label");
                String description = extensions.get("description");
                String formula = extensions.get("formula");
                TreatBlanksAs treatBlanksAs = getTreatBlanksAsFromForceAnnotation(extensions);
                boolean allowNulls = true;
                boolean unique = false;
                if (colmds != null && colmds.length > 0) {
                    colmd = colmds[0];
                    if (type == null) {
                        type = getFieldType(colmd, ammd);
                    }
                    if (precision == null) {
                        scale = colmd.getScale();
                        precision = colmd.getLength();
                    }
                    if (length == null) {
                        length = colmd.getLength();
                    }
                    allowNulls = colmd.getAllowsNull();
                    if (!allowNulls) {
                        // Too bad DataNucleus tries to be smart and add this annotation for various types.
                        // So, we go back to the actual annotations and make sure the user actually added
                        // the @Column(nullable=true)
                        Column columnAnnotation = PersistenceUtils.getMemberAnnotation(ammd.getMemberRepresented(), Column.class);
                        if (columnAnnotation != null) {
                            allowNulls = columnAnnotation.nullable();
                        } else {
                            allowNulls = true;
                        }
                    }
                    unique = colmd.getUnique();
                }
                
                if (startValue != null && type != FieldType.AutoNumber) {
                    throw new NucleusUserException("startValue attribute is only supported for AutoNumber fields types,"
                                                    + " field: " + ammd.getName()
                                                    + " on entity: " + tableImpl.getTableName().getName());
                }
                
                // This will handle bare bones @ManyToOne definitions
                if (type == null && PersistenceUtils.isRelationship(ammd)) {
                    type = FieldType.Lookup;
                }

                if (STANDARD_FIELDS.contains(fieldName.toLowerCase())) {
                    continue;
                }

                boolean isLookupToStandardObject = false;
                if (type == FieldType.Lookup || type == FieldType.MasterDetail) {
                    if (relatedClass == null) {
                        throw new NucleusDataStoreException("Undefined entity type for"
                                                            + " field: " + ammd.getName()
                                                            + " on entity: " + tableImpl.getTableName().getName());
                    }
                    TableImpl table1 = ((ForceStoreSchemaHandler) storeManager.getSchemaHandler()).getTable(relatedClass);
                    isLookupToStandardObject = !table1.getTableName().isCustom();
                    ret.add(relatedClass);
                }

                // we don't want to add __c if this is a lookup to a standard object, because that will screw up the relationship
                TableName columnTypeName = relatedClass != null ? TableName.createTableName(namespace, relatedClass) : null;
                if (isLookupToStandardObject) {
                    fieldName = removeCustomThingSuffix(fieldName);
                }

                CustomField field = new CustomField();

                if (type != null) {
                    field.setType(type);
                } else {
                    setFieldType(field, ammd);
                }
                setOtherValues(field, length, precision, scale, ammd, columnTypeName, fieldName,
                                    tableImpl.getTableName().getName(), childRelationshipName, formula, extensions);
                field.setTrackFeedHistory(enableFeeds);
                field.setExternalId(externalId);
                
                if (isLookupToStandardObject) {
                    //yet we need to have __c on the actual name of the field
                    field.setFullName(String.format("%s%s", fieldName, ColumnImpl.CUSTOM_THING_SUFFIX));
                } else {
                    field.setFullName(fieldName);
                }
                
                field.setLabel(label != null ? label : fieldName);
                field.setDescription(description != null ? description : field.getLabel());
                if (!allowNulls) {
                    field.setRequired(true);
                }
                if (unique || ammd.isUnique()) {
                    field.setUnique(true);
                }
                if (startValue != null) {
                    field.setStartingNumber(startValue);
                }
                if (formula != null) {
                    field.setFormula(formula);
                    field.setFormulaTreatBlanksAs(treatBlanksAs);
                }
                addField(field);
            }
        }
        return ret;
    }
    
    private void addField(CustomField field) {
        if (isReadOnlyFields) {
            throw new NucleusUserException("Cannot add custom"
                                            + " field: " + field.getFullName()
                                            + " to readOnlySchema object: " + tableImpl.getTableName().getName());
        }
        fields.add(field);
    }
    
    private FieldType getFieldType(ColumnMetaData md, AbstractMemberMetaData ammd) {
        if (PersistenceUtils.isRelationship(ammd)) {
            return FieldType.Lookup;
        }
        
        Class<?> clazzType = ammd.getType();
        if (clazzType.isEnum()) {
            return FieldType.Picklist;
        }
        String type = md.getJdbcType() != null ? md.getJdbcType() : md.getSqlType();
        if (type == null) {
            return null;
        } else {
            type = type.toLowerCase();
        }
        
        if (type.equals("varchar")) {
            return FieldType.Text;
        } else if (type.equals("integer") || type.equals("number") || type.equals("numeric")) {
            return FieldType.Number;
        } else if (type.equals("date")) {
            return FieldType.Date;
        } else if (type.equals("timestamp") || type.equals("time")) {
            return FieldType.DateTime;
        } else if (type.equals("boolean")) {
            return FieldType.Checkbox;
        } else if (type.equals("clob")) {
            return FieldType.LongTextArea;
        } else {
            throw new NucleusUserException("Unsupported column type: " + type);
        }
    }
    
    private void setFieldType(CustomField field, AbstractMemberMetaData ammd) {
        Class<?> type = ammd.getType();
        if (type == String.class) {
            field.setType(FieldType.Text);
        } else if (type == Integer.class || type == Integer.TYPE
                || type == Long.class || type == Long.TYPE
                || type == Double.class || type == Double.TYPE
                || type == Float.class || type == Float.TYPE
                || type == Short.class || type == Short.TYPE
                || type == BigInteger.class) {
            field.setType(FieldType.Number);
        } else if (type == Date.class) {
            field.setType(FieldType.Date);
        } else if (type == Calendar.class || type == GregorianCalendar.class || type == Time.class) {
            field.setType(FieldType.DateTime);
        } else if (type == Boolean.class || type == Boolean.TYPE) {
            field.setType(FieldType.Checkbox);
            field.setDefaultValue("false");
        } else if (type == BigDecimal.class) {
            field.setType(FieldType.Currency);
        } else if (type == URL.class) {
            field.setType(FieldType.Url);
        } else if (PersistenceUtils.isMultiPicklist(ammd)) {
            field.setType(FieldType.MultiselectPicklist);
            field.setVisibleLines(3);  // TODO: need to make this a variable
        } else if (type == Byte.class || type == Byte.TYPE) {
            field.setType(FieldType.Text);
        } else if (type == Character.class || type == Character.TYPE) {
            field.setType(FieldType.Text);
        } else {
            throw new NucleusUserException("Unsupported column for"
                                            + " Java type: " + type
                                            + " for entity: " + cmd.getName()
                                            + " field: " + ammd.getName());
        }
    }
    
    private void setOtherValues(CustomField field, Integer length, Integer precision, Integer scale,
            AbstractMemberMetaData ammd, TableName columnTypeName, String fieldName,
            String tableName, String childRelationshipName, String formula, Map<String, String> extensions) {
        Class<?> type = ammd.getType();
        switch (field.getType()) {
        case Text:
            if (formula == null) {
                field.setLength(length != null ? length : 255);
            }
            break;

        case LongTextArea:
        case Html:
            field.setLength(length != null ? length : 32000);
            field.setVisibleLines(field.getType() == FieldType.Html ? 25 : 3);
            break;
            
        case Number:
        case Currency:
        case Percent:
         if (type == Integer.class || type == Integer.TYPE) {
             field.setPrecision(11);
             field.setScale(0);
         } else if (type == Long.class || type == Long.TYPE) {
             field.setPrecision(18);
             field.setScale(0);
         } else if (type == Double.class || type == Double.TYPE) {
             field.setPrecision(16);
             field.setScale(2);
         } else if (type == Float.class || type == Float.TYPE) {
             field.setPrecision(16);
             field.setScale(2);
         } else if (type == Short.class || type == Short.TYPE) {
             field.setPrecision(6);
             field.setScale(0);
         } else if (type == BigInteger.class) {
             field.setPrecision(18);
             field.setScale(0);
         } else if (type == BigDecimal.class) {
             field.setPrecision(16);
             field.setScale(2);
         }
         break;

        case Picklist:
            setPicklistType(field, type, extensions, PersistenceUtils.isOrdinalEnum(ammd));
            break;

        case MultiselectPicklist:
            setPicklistType(field, type.getComponentType(), extensions, PersistenceUtils.isOrdinalEnum(ammd));
            break;
            
        case Lookup:
        case MasterDetail:
            String fieldNameNoSuffix = fieldName;
            if (fieldName.endsWith(ColumnImpl.CUSTOM_THING_SUFFIX)) {
                fieldNameNoSuffix = fieldName.substring(0, fieldName.lastIndexOf(ColumnImpl.CUSTOM_THING_SUFFIX));
            }
            field.setReferenceTo(columnTypeName.getForceApiName());
            // if we just made the childRelationshipName == tableName
            // the Metadata API would fail to create the field if you have more than one lookup on that object
            // so for a lookup from entity1__c to entity2__c, your childRelationshipName is entity2_entity1s
            // so we're less likely to collide
            String relationshipName = childRelationshipName != null
                    ? childRelationshipName : String.format("%s_%ss", fieldNameNoSuffix, tableName);
            if (relationshipName.length() > 40) {
                relationshipName = relationshipName.substring(0, 40);
            }
            field.setRelationshipName(relationshipName);
            field.setRelationshipLabel(fieldNameNoSuffix);
            break;

        default:
        }
    }
    
    private void setPicklistType(CustomField field, Class<?> type, Map<String, String> extensions, boolean isOrdinal) {
        String[] values;
        if (type == String.class || type == String[].class) {
            // This is for non-strict picklists, we already validated in ForceMetaDataListener
            // that this @PicklistValue is present and is not ordinal
            values = PICK_PATTERN.split(extensions.get(PersistenceUtils.PICKLIST_VALUE_FIELD_PREFIX + "value"));
        } else {
            Object[] enumConstants = type.getEnumConstants();
            values = new String[enumConstants.length];
            for (int i = 0; i < enumConstants.length; i++) {
                values[i] = isOrdinal ? "" + i : ((Enum<?>) enumConstants[i]).name();
            }
        }
        Picklist pick = new Picklist();
        PicklistValue[] picklistValues = new PicklistValue[values.length];
        for (int i = 0; i < values.length; i++) {
            PicklistValue pv = new PicklistValue();
            pv.setFullName(values[i]);
            picklistValues[i] = pv;
        }
        pick.setPicklistValues(picklistValues);
        field.setPicklist(pick);
    }
}
