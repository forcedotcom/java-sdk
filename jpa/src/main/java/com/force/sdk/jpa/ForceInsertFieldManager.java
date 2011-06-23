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

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.datanucleus.ObjectManager;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.state.ObjectProviderImpl;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.fieldmanager.AbstractFieldManager;

import com.force.sdk.jpa.model.PicklistValueEnum;
import com.force.sdk.jpa.table.ColumnImpl;
import com.force.sdk.jpa.table.TableImpl;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.types.Time;

/**
 * 
 * Field manager for updating records.  This class takes updated fields and stores them on the
 * SObject which will eventually be pushed to Force.com via a SOQL update (or create or delete)
 *
 * @author Fiaz Hossain
 */
public class ForceInsertFieldManager extends AbstractFieldManager {

    private static final Random PART_B = new Random(System.nanoTime());

    private final ObjectProvider objectProvider;
    private final ForceStoreManager storeManager;
    private final SObject sobject;
    private final TableImpl table;
    private final int pkIndexToSkip;
    private final int versionIndexToSkip;
    private boolean dirty;
    
    /**
     * Creates a manager that will upsert the specified field values of a particular entity.
     *
     * @param objectProvider the object provider for this entity
     * @param storeManager the store manager
     * @param pkValue the id object (string for Force.com entities) for this entity, <code>null</code> if this is an insert
     */
    public ForceInsertFieldManager(ObjectProvider objectProvider, ForceStoreManager storeManager, Object pkValue) {
        this.objectProvider = objectProvider;
        this.storeManager = storeManager;
        this.sobject = new SObject();
        AbstractClassMetaData acmd = objectProvider.getClassMetaData();
        this.table = storeManager.getTable(acmd);
        if (this.table == null) {
            throw new NucleusUserException("Entity does not exist in Force.com datastore: " + acmd.getEntityName());
        }
        sobject.setType(this.table.getTableName().getForceApiName());
        if (pkValue != null) {
            sobject.addField(table.getPKFieldName(acmd), pkValue);
        } else {
            // If PK value is null, this is an insert and we consider this object to be dirty all the time
            dirty = true;
        }
        // During insert this field index should not be added as it is the "id" field which should be null.
        // During update it has already been added above so it needs to be skipped too to avoid duplicate fields in statement
        pkIndexToSkip = acmd.getPKMemberPositions()[0];
        if (acmd.getVersionMetaData() != null && pkValue != null) {
            versionIndexToSkip = acmd.getAbsolutePositionOfMember(acmd.getVersionMetaData().getFieldName());
            Object version = objectProvider.provideField(versionIndexToSkip);
            if (version == null && storeManager.isEnableOptimisticTransactions()) {
                throw new NucleusUserException("Version field must be set to non-null value, field: "
                                                + acmd.getVersionMetaData().getFieldName() + " object id:" + sobject.getId());
            }
            objectProvider.setVersion(version);
        } else {
            versionIndexToSkip = -1;
        }
    }

    /**
     * Retrieves the Force.com object (SObject) for this {@code ForceInsertFieldManager}.
     * 
     * @param appendExternalId flag indicating if an external id should be added to
     *                         the Force.com object (SObject) if none exists
     * @return the Force.com object (SObject) for this {@code ForceInsertFieldManager}
     */
    public SObject getSObject(boolean appendExternalId) {
        if (appendExternalId) {
            if (table.getExternalIdColumn() != null) {
                String name = table.getExternalIdColumn().getFieldName();
                if (sobject.getField(name) == null) {
                    sobject.setField(name, "" + System.nanoTime() + PART_B.nextInt());
                }
            }
        }
        return sobject;
    }
    
    /**
     * The entity is considered dirty if fields have been updated (meaning an update is necessary).
     * Entities with no ids, meaning they are for insert, are always marked as dirty.
     * @return {@code true} if the entity is considered dirty and needs to be upserted
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * All store methods delegate to this method for proper transformation on the value so it
     * can be stored in the sObject and upserted properly by the API.
     * 
     * @param fieldNumber the index of the field to update
     * @param value the value that should be stored in the field
     */
    private void storeField(int fieldNumber, Object value) {
        if (value == null || fieldNumber == pkIndexToSkip || fieldNumber == versionIndexToSkip) return;
        AbstractMemberMetaData ammd =
            objectProvider.getClassMetaData().getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
        if (ammd.getEmbeddedMetaData() != null) {
            // we need to add all the embedded fields
            storeEmbeddedFields(ammd, value);
            return;
        }
        ColumnImpl column = table.getColumnFor(objectProvider.getClassMetaData(), ammd);
        if (column == null || column.getField() == null || !column.getField().isCreateable()) return;
        Object actualValue = value;
        String actualFieldName = column.getFieldName();
        try {
            switch (column.getType()) {
            case datetime:
                if (value instanceof Date) {
                    ColumnMetaData[] cmds = ammd.getColumnMetaData();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime((Date) value);
                    if (cmds != null && cmds.length > 0 && "TIME".equals(cmds[0].getJdbcType())) {
                        actualValue = new Time(cal);
                    } else {
                        actualValue = cal;
                    }
                } else if (value instanceof Time) {
                    actualValue = value;
                }
                break;
            case currency:
                actualValue = value.toString();
                break;
            case reference:
                AbstractClassMetaData acmd =
                    storeManager.getMetaDataManager().getMetaDataForClass(ammd.getType(),
                                                        objectProvider.getExecutionContext().getClassLoaderResolver());
                if (acmd != null) {
                    actualValue = PersistenceUtils.getMemberValue(acmd, acmd.getPKMemberPositions()[0], value);
                    if (actualValue == null) {
                        ObjectManager om = ((ObjectProviderImpl) objectProvider).getStateManager().getObjectManager();
                        if (((ForceObjectManagerImpl) om).isInAllOrNothingMode()) {
                            /**
                             * This is instance of AllOrNothing transaction
                             * Since the parent object has not been saved to the db yet
                             * we do not have an id yet so we link objects by extId
                             */
                            SObject parentRef = new SObject();
                            TableImpl parent = storeManager.getTable(acmd);
                            if (parent.getExternalIdColumn() == null) {
                                throw new NucleusUserException("EntityManager in persistence.xml has 'force.AllOrNothing'"
                                                               + " set to true. In this mode all top parent Entities must"
                                                               + " have an externalId field. Offending entity: "
                                                               + parent.getTableName().getName());
                            }
                            parentRef.setType(parent.getTableName().getForceApiName());
                            SObject parentSObject = ((ForceObjectManagerImpl) om).getParentSObject(value);
                            parentRef.setField(parent.getExternalIdColumn().getFieldName(),
                                                parentSObject.getField(parent.getExternalIdColumn().getFieldName()));
                            actualValue = parentRef;
                            actualFieldName = column.getForceApiRelationshipName();
                        } else {
                            throw new NucleusUserException("Child entity cannot be saved before parent entity.");
                        }
                    }
                } else {
                    if (ammd.getCollection() != null || ammd.getMap() != null) {
                        /**
                         * There is no data to set on the parent as the relationship is only maintained from the child's end
                         */
                        return;
                    }
                }
                break;
            case multipicklist:
                if (value.getClass().isArray()) {
                    if (ammd.getType().getComponentType().isEnum()) {
                        Enum<?>[] enums = (Enum<?>[]) value;
                        boolean isOrdinal = PersistenceUtils.isOrdinalEnum(ammd);
                        boolean isPicklistValue = PicklistValueEnum[].class.isAssignableFrom(enums.getClass());
                        
                        StringBuilder sb = new StringBuilder(enums.length * 20);
                        for (Enum<?> e : enums) {
                            if (sb.length() > 0) sb.append(";");
                            if (isOrdinal) {
                                sb.append(e.ordinal());
                            } else if (isPicklistValue) {
                                sb.append(((PicklistValueEnum) e).value());
                            } else {
                                sb.append(e.name());
                            }
                        }
                        actualValue = sb.toString();
                    } else {
                        // else treat it like a non-strict picklist value
                        String[] values = (String[]) value;
                        StringBuilder sb = new StringBuilder(values.length * 20);
                        for (String v : values) {
                            if (sb.length() > 0) sb.append(";");
                            sb.append(v);
                        }
                        actualValue = sb.toString();
                    }
                    break;
                }
                // else treat it like a single picklist value
            case picklist:
                if (value.getClass().isEnum()) {
                    Enum e = (Enum) value;
                    if (PersistenceUtils.isOrdinalEnum(ammd)) {
                        actualValue = "" + e.ordinal();
                    } else if (e instanceof PicklistValueEnum) {
                        actualValue = ((PicklistValueEnum) e).value();
                    } else {
                        actualValue = e.name();
                    }
                }
                break;
            case url:
                actualValue = value.toString();
                break;
            case base64:
                if (ammd.getType() == byte[].class || ammd.getType() == Byte[].class) {
                    /**
                     * Odd that I did not have to encode this, yet when reading I have to decode it.
                     */
                    //actualValue = Base64.encode((byte[]) value);
                    actualValue = value;
                } else {
                    throw new NucleusUserException("Bad datatype for base64 encoding: " + ammd.getTypeName());
                }
                break;
            default:
            }
        } catch (InvocationTargetException x) {
            throw new NucleusException(x.getMessage(), x);
        } catch (IllegalAccessException x) {
            throw new NucleusException(x.getMessage(), x);
        }
        sobject.addField(actualFieldName, actualValue);
        dirty = true;
    }
    
    private void storeEmbeddedFields(AbstractMemberMetaData ammd, Object value) {
        AbstractClassMetaData cmd = objectProvider.getClassMetaData();
        for (AbstractMemberMetaData eammd : ammd.getEmbeddedMetaData().getMemberMetaData()) {
            if (eammd.getPersistenceModifier() != FieldPersistenceModifier.PERSISTENT) continue;
            try {
                Object newValue = PersistenceUtils.getMemberValue(eammd.getMemberRepresented(), value);
                if (eammd.getEmbeddedMetaData() != null) {
                    storeEmbeddedFields(eammd, newValue);
                } else {
                        ColumnImpl column = table.getColumnFor(cmd, eammd);
                        if (column != null) {
                            sobject.addField(column.getFieldName(), newValue);
                            dirty = true;
                        }
                }
            } catch (Exception e) {
                throw new NucleusUserException(e.getMessage());
            }
        }
    }
    
    @Override
    public void storeBooleanField(int fieldNumber, boolean value) {
        storeField(fieldNumber, value);
    }
    
    @Override
    public void storeByteField(int fieldNumber, byte value) {
        storeField(fieldNumber, String.valueOf(value));
    }
    
    @Override
    public void storeCharField(int fieldNumber, char value) {
        storeField(fieldNumber, String.valueOf(value));
    }
    
    @Override
    public void storeDoubleField(int fieldNumber, double value) {
        storeField(fieldNumber, value);
    }
    
    @Override
    public void storeFloatField(int fieldNumber, float value) {
        storeDoubleField(fieldNumber, value);
    }
    
    @Override
    public void storeIntField(int fieldNumber, int value) {
        storeField(fieldNumber, value);
    }
    
    @Override
    public void storeLongField(int fieldNumber, long value) {
        storeObjectField(fieldNumber, value);
    }
    
    @Override
    public void storeObjectField(int fieldNumber, Object value) {
        if (value instanceof Long) {
            storeField(fieldNumber, value.toString());
        } else if (value instanceof Float) {
            storeField(fieldNumber, ((Float) value).doubleValue());
        } else if (value instanceof Short) {
            storeField(fieldNumber, ((Short) value).intValue());
        } else if (value instanceof Byte) {
            storeField(fieldNumber, ((Byte) value).toString());
        } else if (value instanceof BigInteger) {
            storeField(fieldNumber, value.toString());
        } else if (value instanceof Character) {
            storeField(fieldNumber, ((Character) value).toString());
        } else {
            storeField(fieldNumber, value);
        }
    }
    
    @Override
    public void storeShortField(int fieldNumber, short value) {
        storeIntField(fieldNumber, value);
    }
    
    @Override
    public void storeStringField(int fieldNumber, String value) {
        storeField(fieldNumber, value);
    }
}
