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

package com.force.sdk.jpa.schema;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.schema.StoreSchemaData;
import org.datanucleus.store.schema.StoreSchemaHandler;
import org.slf4j.LoggerFactory;

import com.force.sdk.jpa.*;
import com.force.sdk.jpa.table.TableImpl;
import com.force.sdk.jpa.table.TableName;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.fault.InvalidSObjectFault;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Cached information about the schema used by this application is stored in
 * the Schema Handler.
 *
 * @author Fiaz Hossain
 */
public class ForceStoreSchemaHandler implements StoreSchemaHandler {

    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("com.force.sdk.jpa.schema");
    
    private static final int MAX_DESCRIBE_SOBJECT = 100;
    
    private ForceStoreManager storeManager;
    private ConcurrentHashMap<String, TableImpl> entityTables =
                new ConcurrentHashMap<String, TableImpl>(); // mapping from entity name to tableImpl
    private ConcurrentHashMap<String, TableImpl> tables =
                new ConcurrentHashMap<String, TableImpl>(); // mapping from table name to tableImpl

    private Map<String , DescribeSObjectResult> sObjectResults; // temporary holding space during initialization

    /**
     * Creates the schema handler for Force.com sObjects.
     * 
     * @param storeManager the store manager
     */
    public ForceStoreSchemaHandler(ForceStoreManager storeManager) {
        this.storeManager = storeManager;
    }
    
    @Override
    public void clear() {
    }

    @Override
    public void createSchema(Object conn, String schemaName) {
        throw new NucleusUserException("DataNucleus doesnt currently support creation of schemas for Force.com");
    }

    @Override
    public void deleteSchema(Object conn, String schemaName) {
        throw new NucleusUserException("DataNucleus doesnt currently support deletion of schemas for Force.com");
    }

    @Override
    public StoreSchemaData getSchemaData(Object conn, String objName, Object[] values) {
        final TableImpl table = getTable(objName);
        return table == null ? null : new StoreSchemaData() {
            
            @Override
            public Object getProperty(String name) {
                return table.getColumnByForceApiName(name);
            }
            
            @Override
            public void addProperty(String name, Object value) {
                throw new NucleusUserException("DataNucleus doesnt currently support adding properties"
                                                + " for Force.com schema data");
            }
        };
    }
    
    /**
     * Makes a describe call for each class in the collection and caches the results.
     * 
     * @param classes a collection of class metadata to describe
     * @param mconn the managed connection to the Force.com APIs
     */
    public void cacheDescribeSObjects(Collection<AbstractClassMetaData> classes, ForceManagedConnection mconn) {
        ArrayList<String> sObjectNames = new ArrayList<String>(classes.size());
        try {
            Iterator<AbstractClassMetaData> iter = classes.iterator();
            while (iter.hasNext()) {
                AbstractClassMetaData cmd = iter.next();
                // Similar to PersistenceUtils.createObjectSchema
                if (PersistenceUtils.hasNoSchema(cmd)) continue;
                TableName tableName = TableName.createTableName(mconn.getNamespace(), cmd);
                if (cmd.getSuperAbstractClassMetaData() == null) {
                   sObjectNames.add(tableName.getForceApiName());
                }
            }
            // Setup the initialization cache
            sObjectResults = new HashMap<String, DescribeSObjectResult>(sObjectNames.size());

            for (int siz = 0; siz < sObjectNames.size(); siz += MAX_DESCRIBE_SOBJECT) {
                int len = sObjectNames.size() - siz > MAX_DESCRIBE_SOBJECT ? MAX_DESCRIBE_SOBJECT : sObjectNames.size() - siz;
                List<String> nameList = sObjectNames.subList(siz, siz + len);
                if (LOGGER.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    for (String s : nameList) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(s);
                    }
                    LOGGER.debug("DescribeSObjects: " + sb);
                }
                DescribeSObjectResult[] results =
                    ((PartnerConnection) mconn.getConnection()).describeSObjects(nameList.toArray(new String[nameList.size()]));
                for (int i = 0; i < results.length; i++) {
                    sObjectResults.put(nameList.get(i), results[i]);
                }
            }
        } catch (InvalidSObjectFault ie) {
            // Well we seem to be missing some objects so we just fall back to single item describe as we go along
            LOGGER.trace("Describes sObjects failed: ", ie);
        } catch (ConnectionException x) {
            throw new NucleusException(x.getMessage(), x);
        }
    }
    
    /**
     * Clears out the describe results after intitialization.
     */
    public void clearDescribeSObjects() {
        // Clear the initialization cache
        sObjectResults.clear();
    }

    /**
     * Gets the table impl for a particular entity name from the cache.
     * 
     * @param objName the name of the object to retrieve a table for
     * @return the tableImpl, {@code null} if it hasn't been registered
     */
    public TableImpl getTable(String objName) {
        return getTable(storeManager.getMetaDataManager().getMetaDataForEntityName(objName));
    }

    /**
     * Gets the table impl for a particular entity from the cache.
     * 
     * @param acmd the metadata to retrieve table info for 
     * @return the tableImpl, {@code null} if it hasn't been registered
     */
    public TableImpl getTable(AbstractClassMetaData acmd) {
        if (acmd == null) return null;
        String objName = PersistenceUtils.getEntityName(acmd);
        return entityTables.get(objName.toLowerCase());
    }
    
    /**
     * Registers a table with the cache, creates its TableName and TableImpl.
     * 
     * @param acmd the class metadata of the entity to register
     * @param conn the managed connection to Force.com APIs, used to get at the namespace
     * @return the created TableImpl
     */
    public TableImpl addTable(AbstractClassMetaData acmd, ForceManagedConnection conn) {
        String entityName = PersistenceUtils.getEntityName(acmd).toLowerCase();
        try {
            TableName tableName = TableName.createTableName(conn.getNamespace(), acmd);
            TableImpl tableImpl = tables.get(tableName.getForceApiName().toLowerCase());
            if (tableImpl == null) {
                tableImpl = new TableImpl(conn.getNamespace(), tableName, sObjectResults.get(tableName.getForceApiName()), conn);
                tables.put(tableName.getForceApiName().toLowerCase(), tableImpl);
            }
            entityTables.put(entityName, tableImpl);
            return tableImpl;
        } catch (ConnectionException e) {
            throw new NucleusException("Could not initialize table: " + entityName, e);
        }
    }
    
    /**
     * Registers a virtual table (a table not backed by an actual object in Force.com, like ForceOwner).
     * 
     * @param acmd the class metadata of the virtual table to register with the cache
     * @return the created TableImpl
     */
    public TableImpl addVirtualTable(AbstractClassMetaData acmd) {
        String entityName = PersistenceUtils.getEntityName(acmd).toLowerCase();
        TableName tableName = TableName.createTableName("", acmd);
        TableImpl tableImpl = tables.get(tableName.getForceApiName().toLowerCase());
        if (tableImpl == null) {
            tableImpl = new TableImpl(tableName, acmd);
            tables.put(tableName.getForceApiName().toLowerCase(), tableImpl);
        }
        entityTables.put(entityName, tableImpl);
        return tableImpl;
    }
    
    @Override
    public StoreManager getStoreManager() {
        return storeManager;
    }
}
