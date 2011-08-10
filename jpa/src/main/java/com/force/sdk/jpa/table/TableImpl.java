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

import java.util.*;

import javax.persistence.Table;

import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;

import com.force.sdk.jpa.*;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.fault.InvalidSObjectFault;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Represents an object (custom or standard) in Force.com.
 *
 * @author Fiaz Hossain
 */
public class TableImpl {
    
    private final Map<String, ColumnImpl> forceApiColumns; //map of ColumnImpls by Force.com API name
    private final Map<String, ColumnImpl> javaColumns; //map of ColumnImpls by java field name
    private final List<ColumnImpl> columnList;
    private final TableName tableName;
    private boolean isValid;
    private ColumnImpl externalId;
    private final String defaultNamespace;
    private boolean created;
    private ForceTableMetaData tableMetaData;
    private boolean tableAlreadyExistsInOrg;

    /**
     * Creates a TableImpl for an entity.
     * 
     * @param defaultNamespace the namespace of the organization connecting to Force.com, to be used by default
     * @param tableName the table name of the entity
     * @param result  the result of an API describe
     * @param mconn the managed connection to the Force.com APIs
     */
    public TableImpl(String defaultNamespace, TableName tableName, DescribeSObjectResult result, ForceManagedConnection mconn) {
        this.defaultNamespace = defaultNamespace;
        this.tableName = tableName;
        this.columnList = new ArrayList<ColumnImpl>();
        this.forceApiColumns = new HashMap<String, ColumnImpl>(); //this will get populated later
        refresh(result, mconn);
        
        //this will get populated later
        this.javaColumns = new HashMap<String, ColumnImpl>(forceApiColumns.size() == 0 ? 4 : forceApiColumns.size());
    }
    
    /**
     * Constructor used for virtual schema (tables that aren't actually backed by an object, like ForceOwner).
     * 
     * @param tableName the table name of the entity
     * @param acmd  the class metadata
     */
    public TableImpl(TableName tableName, AbstractClassMetaData acmd) {
        
        this.defaultNamespace = "";
        this.tableName = tableName;
        
        AbstractMemberMetaData[] ammds = acmd.getManagedMembers();
        this.columnList = new ArrayList<ColumnImpl>(ammds.length);
        this.forceApiColumns = new HashMap<String, ColumnImpl>(ammds.length);
        
        // Populate the column list and api column map directly
        // from the AbstractMemberMetaData
        for (AbstractMemberMetaData ammd : acmd.getManagedMembers()) {
            // Because we won't be asking Force.com to describe this virtual object's fields
            // The developer must describe each field's name through the @Column annotation
            String fieldName = PersistenceUtils.getFieldNameFromJPAAnnotation(ammd);
            if (fieldName == null) {
                throw new NucleusUserException(
                        "All fields in a virtual schema object must specify their name through @Column. "
                        + "Offending field: " + ammd.getName() + " on object " + acmd.getName());
            }
            
            ColumnImpl col = new ColumnImpl(fieldName, null);
            this.columnList.add(col);
            this.forceApiColumns.put(ammd.getName(), col);
        }
        
        this.javaColumns = new HashMap<String, ColumnImpl>(forceApiColumns.size()); //this will get populated later
        this.isValid = true;
        
        // The table doesn't really exist anywhere, but we should be
        // skipping all schema creation for this object
        this.tableAlreadyExistsInOrg = true;
    }
    
    /**
     * This constructor is used in the mocking test framework.
     */
    TableImpl(String defaultNamespace, TableName tableName,
            List<ColumnImpl> columnList, Map<String, ColumnImpl> forceApiColumns, ColumnImpl externalId) {
        this.defaultNamespace = defaultNamespace;
        this.tableName = tableName;
        
        this.columnList = columnList;
        this.forceApiColumns = forceApiColumns;
        this.externalId = externalId;
        
        // this will get populated later
        this.javaColumns = new HashMap<String, ColumnImpl>(forceApiColumns == null ? 0 : forceApiColumns.size());
        
        this.isValid = true;
        this.tableAlreadyExistsInOrg = true;
    }
    
    /**
     * Entities are considered valid if they exist in the Force.com database.
     * However, during schema creation the table may be invalidated,
     * but should return to valid after a describe call confirms its existence.
     * Use {@link #exists()} instead if you want to find out if the table exists in the org
     * 
     * @return {@code true} if the table has been validated as existing in the Force.com database,
     *         {@code false} doesn't necessarily mean it will not be created
     */
    public boolean isValid() {
        return this.isValid;
    }
    
    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }
    
    /**
     * Checks existence of a table in the organization used by the application.
     * 
     * @return {@code true} if the entity was already in the organization before the application starts up
     *              or it has already been created
     */
    public boolean exists() {
        return tableAlreadyExistsInOrg || created;
    }

    public ColumnImpl getExternalIdColumn() {
        return this.externalId;
    }
    
    /**
     * Retrieves a Force.com column with a fully qualified Force.com API name.
     * 
     * @param columnName a fully qualified Force.com API name
     * @return a Force.com column registered with this {@code TableImpl} or {@code null}
     *         if no column with the given name exists
     */
    public ColumnImpl getColumnByForceApiName(String columnName) {
        //first try it without the namespace, then use the namespace of the table
        ColumnImpl col = forceApiColumns.get(columnName.toLowerCase());
        
        if (col == null) {
            col = forceApiColumns.get(PersistenceUtils.prependNamespace(defaultNamespace, columnName).toLowerCase());
        }
        return col;
    }
    
    /**
     * Retrieves a Force.com column with a Java field name.
     * <p>
     * Make sure if you're calling this that it happens after schema creation, because this will not
     * be populated until all the class metadata has been fully initialized.
     * 
     * @param columnName a Java field name
     * @return a Force.com column registered with this {@code TableImpl} or {@code null}
     *         if no column with the given name exists
     */
    public ColumnImpl getColumnByJavaName(String columnName) {
        return javaColumns.get(columnName.toLowerCase());
    }
    
    /**
     * Registers a column impl (with specific Force.com attributes, like the API name) under its field or
     * property  name in java.
     * 
     * @param javaFieldName the name of a field or property in java
     * @param column the column object corresponding to this field
     */
    public void registerJavaColumn(String javaFieldName, ColumnImpl column) {
        this.javaColumns.put(javaFieldName.toLowerCase(), column);
    }

    /**
     * Retrieves a Force.com column at the given index.
     * 
     * @param columnIndex index of the Force.com column registered with this {@code TableImpl}
     * @return the Force.com column at the given index
     * @throws NucleusException if the given index is out of bounds
     */
    public ColumnImpl getColumnAt(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columnList.size())
            throw new NucleusException("Column out of bounds");
        return columnList.get(columnIndex);
    }
    
    public List<ColumnImpl> getListOfColumns() {
        return columnList;
    }
    
    public TableName getTableName() {
        return tableName;
    }
    
    @Override
    public String toString() {
        return tableName.toString();
    }
    
    /**
     * Retrieves a list of Force.com columns with the given JPA field metadata.
     * <p>
     * This method can handle embedded JPA metadata.
     * 
     * @param acmd JPA class metadata
     * @param ammd JPA field metadata
     * @param storeManager ForceStoreManager
     * @param columns list which will collect all embedded Force.com columns
     * @return a list of Force.com columns registered with this {@code TableImpl}
     */
    public List<ColumnImpl> getColumnsFor(AbstractClassMetaData acmd, AbstractMemberMetaData ammd,
            ForceStoreManager storeManager, List<ColumnImpl> columns) {
        if (ammd.getEmbeddedMetaData() != null) {
            for (AbstractMemberMetaData eammd : ammd.getEmbeddedMetaData().getMemberMetaData()) {
                getColumnsFor(acmd, eammd, storeManager, columns);
            }
        } else {
            columns.add(getColumnFor(acmd, ammd));
        }
        return columns;
    }
    
    /**
     * Retrieves a Force.com column from the given JPA class metadata at the given field position.
     * 
     * @param acmd JPA class metadata
     * @param fieldNumber JPA field position
     * @return a Force.com column registered with this {@code TableImpl}
     */
    public ColumnImpl getColumnAt(AbstractClassMetaData acmd, int fieldNumber) {
        return getColumnFor(acmd, acmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber));
    }
    
    /**
     * Retrieve a Force.com column with the given JPA field metadata.
     * 
     * @param acmd JPA class metadata
     * @param ammd JPA field metadata
     * @return a Force.com column registered with this {@code TableImpl}
     * @throws NucleusUserException if no Force.com column can be found for the given JPA field metadata
     */
    public ColumnImpl getColumnFor(AbstractClassMetaData acmd, AbstractMemberMetaData ammd) {
        /*
         * Transient methods are not persisted in the database
         */
        if (PersistenceUtils.isNonPersistedColumn(ammd)) return null;
        
        ColumnImpl ret = getColumnByJavaName(ammd.getName());
        if (ret != null) return ret;
        
        /*
         * Check if the field has a different column name
         */
        try {
            Table tab = ((Class<?>) ammd.getType()).getAnnotation(Table.class);
            if (tab != null) {
                String renamedField = tab.name();
                if (renamedField != null && renamedField.length() > 0) {
                    ret = getColumnByForceApiName(renamedField);
                }
            }
        } catch (Exception ignored) {
            // Exception ignored
        }
        if (ret == null) {
            throw new NucleusUserException(
                    String.format("Field name: %s not found in Force.com object: %s",
                                    ammd.getName(), getTableName().getForceApiName()));
        }
        return ret;
    }
    
    /**
     * Retrieves the primary key Force.com API field name for the
     * given class metadata.
     * 
     * @param acmd JPA class metadata
     * @return a Force.com API primary key field name
     */
    public String getPKFieldName(AbstractClassMetaData acmd) {
        ColumnImpl col = getColumnAt(acmd, acmd.getPKMemberPositions()[0]);
        return col != null ? col.getFieldName() : "id";
    }
    
    /**
     * Creates the custom objects and fields for schema creation. This method should only be called if it has been verified
     * that schema creation for tables is enabled 
     * 
     * @param cmd  the class metadata for the table being created
     * @param storeManager the store manager
     * @param mconn the managed connection to the Force.com APIs
     */
    public void createTableAndFields(AbstractClassMetaData cmd, ForceStoreManager storeManager, ForceManagedConnection mconn) {
        if (!created) {
            getMetaData(cmd).createCustomObject(cmd, storeManager, mconn);
        }
        createFields(cmd, storeManager);
        created = true;
    }
    
    /**
     * Creates custom fields only.
     * If autoCreateTables is {@code false} but autoCreateColumns is {@code true}, this method should be called.
     * It assumes that the table already exists in the organization or has already been created
     * 
     * @param cmd the class metadata for the table containing the fields to be created
     * @param storeManager the store manager
     */
    public void createFields(AbstractClassMetaData cmd, ForceStoreManager storeManager) {
        getMetaData(cmd).createCustomFields(cmd, storeManager);
    }


    /**
     * Refreshesd the describe result to verify that objects have been created as we expected.
     * 
     * @param result the describe result for this entity
     * @param mconn the managed connection to the Force.com APIs
     */
    public void refresh(DescribeSObjectResult result, ForceManagedConnection mconn) {
        boolean valid = result != null;
        try {
            if (!valid) {
                result = ((PartnerConnection) mconn.getConnection()).describeSObject(tableName.getForceApiName());
            }

            //loop through the fields and add all to the column list and column map.
            for (Field field : result.getFields()) {
                String columnName = field.getName().toLowerCase();
                ColumnImpl column = new ColumnImpl(columnName, field);
                columnList.add(column);
                forceApiColumns.put(columnName, column);
                
                if (field.isExternalId()) {
                    externalId = column;
                }
            }
            
            ChildRelationship[] relationships = result.getChildRelationships();
            for (ChildRelationship cr : relationships) {
                String relationshipName = cr.getRelationshipName();
                if (relationshipName == null) {
                    continue;
                }
                RelationshipImpl column = new RelationshipImpl(relationshipName);
                columnList.add(column);
                forceApiColumns.put(relationshipName.toLowerCase(), column);
            }

            valid = true;
        } catch (InvalidSObjectFault ie) {
            valid = false;
        } catch (ConnectionException x) {
            throw new NucleusException(x.getMessage(), x);
        }
        this.isValid = valid;
        this.tableAlreadyExistsInOrg = valid;
    }
    
    public boolean getTableAlreadyExistsInOrg() {
        return tableAlreadyExistsInOrg;
    }
    
    /**
     * Retrieves Force.com table metadata for the given JPA class metadata.
     * 
     * @param cmd JPA class metadata
     * @return {@link ForceTableMetaData}
     */
    public ForceTableMetaData getMetaData(AbstractClassMetaData cmd) {
        if (tableMetaData == null) {
            tableMetaData = new ForceTableMetaData(cmd, this);
        }
        return tableMetaData;
    }
    
    /**
     * Clears the existing table metadata.
     */
    void clearMetaData() {
        tableMetaData = null;
    }
}
