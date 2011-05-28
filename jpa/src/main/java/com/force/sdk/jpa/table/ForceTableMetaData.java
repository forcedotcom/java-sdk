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

import java.util.Map;

import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;

import com.force.sdk.jpa.*;
import com.force.sdk.jpa.schema.ForceSchemaWriter;
import com.sforce.soap.metadata.*;

/**
 * 
 * Metadata for Force.com objects.  This class converts your non-read-only @Entity java classes
 * into {@link CustomObject}s that the Force.com metadata API can read. The CustomObject
 * gets updated with any specified fields and if schema creation is enabled, the
 * object is emitted to the ForceSchemaWriter to be deployed to the developer's
 * org.
 *
 * @author Fiaz Hossain
 * @author Jill Wetzler
 */
public class ForceTableMetaData extends ForceMetaData {

    /**
     * Create the metadata for an object in Force.com.
     * 
     * @param cmd  the class metadata for the entity 
     * @param tableImpl the table object
     */
    public ForceTableMetaData(AbstractClassMetaData cmd, TableImpl tableImpl) {
        super(cmd, tableImpl);
    }
    
    /**
     * if a {@link CustomObject} needed to be created for this entity, pass it
     * to the schema writer which will store it and then later write all objects
     * and fields at once.  Pass all fields on the custom object to the schema
     * writer as well
     * 
     * @param storeManager the store manager
     * @param mconn the managed connection that contains connections for the Force.com APIs
     */
    public void emit(ForceStoreManager storeManager, ForceManagedConnection mconn) {
        try {
            ForceSchemaWriter schemaWriter = storeManager.getSchemaWriter();
            if (customObject != null) {
                schemaWriter.addCustomObject(customObject, cmd, storeManager, this);
                
                for (CustomField field : customObject.getFields()) {
                    schemaWriter.addCustomField(customObject, field);
                }
                
                customObject = null;
            }
        } catch (Exception x) {
            throw new NucleusException(x.getMessage(), x);
        }
        // Clear metadata in tableImpl
        tableImpl.clearMetaData();
    }
    
    /**
     * This method should be called only if it has been determined that table schema should be created (or deleted).
     * Create the {@link CustomObject} for the metadata API to use during deploy
     * 
     * @param cmd  the class metadata for the entity
     * @param storeManager the store manager
     * @param mconn the managed connection that contains connections to the Force.com APIs
     */
    public void createCustomObject(AbstractClassMetaData cmd, ForceStoreManager storeManager, ForceManagedConnection mconn) {
        String shortName = removeCustomThingSuffix(tableImpl.getTableName().getName());

        if (isReadOnlyTable) {
            throw new NucleusUserException("Cannot create readOnlySchema custom object: " + shortName);
        }
        
        createCustomObjectStub();
        
        customObject.setDescription(shortName + ": Persistenceforce created custom object");
        customObject.setDeploymentStatus(DeploymentStatus.Deployed);
        customObject.setSharingModel(SharingModel.ReadWrite);
        customObject.setLabel(shortName);
        customObject.setPluralLabel(shortName);
        
        CustomField nf = new CustomField();
        nf.setType(FieldType.Text);
        nf.setLabel("name");
        nf.setFullName("name");
        nf.setDescription("name");

        customObject.setNameField(nf);

        Map<String, String> classExtensions = PersistenceUtils.getForceExtensions(cmd);
        String value = classExtensions.get("enableFeeds");
        if (value != null) {
            customObject.setEnableFeeds(Boolean.valueOf(value));
        }
    }
    
    /**
     * This method should be called only if it has been determined that field schema should be created (or deleted).
     * Create the {@link CustomField} for the metadata API to use during deploy
     * 
     * @param cmd  the class metadata for the entity
     * @param storeManager the store manager
     */
    public void createCustomFields(AbstractClassMetaData cmd, ForceStoreManager storeManager) {
        synchronized (cmd) {
            try {
                if (cmd.isEmbeddedOnly() || PersistenceUtils.isReadOnlySchema(cmd, false)) return;
                ForceManagedConnection mconn = storeManager.createConnection();
                try {
                    ForceColumnMetaData cols = new ForceColumnMetaData(cmd, tableImpl, storeManager);
                    cols.createFieldSchema(mconn.getNamespace());
                    if (customObject == null) {
                        createCustomObjectStub();
                    }
                    cols.addFieldsToObject(customObject);
                } catch (NucleusException ce) {
                    throw ce;
                } catch (Exception e) {
                    throw new NucleusException(e.getMessage(), e);
                } finally {
                    mconn.close();
                }
            } catch (Exception e) {
                throw new NucleusUserException("Exception during initialisation of metadata for "
                        + cmd.getFullClassName(), e);
            }
        }
    }
}
