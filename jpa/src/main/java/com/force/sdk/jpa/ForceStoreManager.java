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

import java.lang.reflect.Field;
import java.util.*;

import org.datanucleus.*;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.plugin.PluginRegistry;
import org.datanucleus.store.*;

import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceConnectorUtils;
import com.force.sdk.jpa.schema.*;
import com.force.sdk.jpa.table.TableImpl;

/**
 * 
 * Store manager that holds all the persistence.xml properties.
 *
 * @author Fiaz Hossain
 */
public class ForceStoreManager extends AbstractStoreManager {

    /**
     * Store manager type for Force.com JPA.
     */
    public static final String FORCE_KEY = "force";
    
    private static final String FORCE_PREFIX = FORCE_KEY + "://";
    
    // Connection information for the persistence layer to
    // get a connection to the Force.com service
    private ForceConnectorConfig config;
    
    private boolean autoCreateTables = false;
    private boolean autoCreateColumns = false;
    private boolean autoCreateWarnOnError = false;
    
    private int poolTimeBetweenEvictionRunsMillis;
    private int poolMinEvictableIdleTimeMillis;
    ForceMetaDataListener metadataListener;
    private final boolean enableOptimisticTransactions;
    private ForceSchemaWriter schemaWriter;
    private final boolean forDelete;

    /**
     * Looks into system variable and environment variables if url is in ${...} format.
     * @return Connection URL
     */
    @Override
    public String getConnectionURL() {
        String connectionUrl = super.getConnectionURL();

        if (ForceConnectorUtils.isInjectable(connectionUrl)) {
            connectionUrl = ForceConnectorUtils.extractValue(connectionUrl);
            if (connectionUrl == null || connectionUrl.equals("")) {
                throw new IllegalArgumentException("Unable to load ForceConnectorConfig from environment or system property "
                        + super.getConnectionURL());
            }
        }

        return connectionUrl;
    }

    /**
     * Creates a store manager for use with the Force.com API. Set up the API connection
     * configs, set some default properties and read in values from persistence.xml
     * 
     * @param clr  the class loader resolver
     * @param omfContext the corresponding object manager factory context
     * @throws NoSuchFieldException thrown if there is a problem setting up the plugin manager
     * @throws IllegalAccessException thrown if there is a problem setting up the plugin manager
     */
    public ForceStoreManager(ClassLoaderResolver clr, OMFContext omfContext) throws NoSuchFieldException, IllegalAccessException {
        super(FORCE_KEY, clr, omfContext);
    
        String endpoint = getConnectionURL();



        
        // Grab the connection information from persistence.xml.  If none exists,
        // we'll use the unit name (see ForceMetaDataManager.loadPersistenceUnit
        // and ForceConnectionFactory.createManagedConnection).
        if (endpoint != null) {
            config = new ForceConnectorConfig();
            
            // Treat any url starting with force:// as a connection url
            if (endpoint.startsWith(FORCE_PREFIX)) {
                config.setConnectionUrl(endpoint);
            // Any other endpoint, we'll treat as a normal Force.com API url
            } else {
                config.setAuthEndpoint(endpoint);
                config.setUsername(getConnectionUserName());
                config.setPassword(getConnectionPassword());
            }
        }
        
        setCustomPluginManager();
        
        // Handler for metadata
        metadataListener = new ForceMetaDataListener(this);
        omfContext.getMetaDataManager().registerListener(metadataListener);

        // Handler for persistence process
        persistenceHandler2 = new ForcePersistenceHandler(this);
        
        // Store schema handler
        schemaHandler = new ForceStoreSchemaHandler(this);
        
        PersistenceConfiguration conf = omfContext.getPersistenceConfiguration();
        boolean autoCreateSchema = conf.getBooleanProperty("datanucleus.autoCreateSchema");
        if (autoCreateSchema) {
            autoCreateTables = true;
            autoCreateColumns = true;
        } else {
            autoCreateTables = conf.getBooleanProperty("datanucleus.autoCreateTables");
            autoCreateColumns = conf.getBooleanProperty("datanucleus.autoCreateColumns");
        }
        autoCreateWarnOnError = conf.getBooleanProperty("datanucleus.autoCreateWarnOnError");
        forDelete = conf.getBooleanProperty("force.deleteSchema");
        boolean purgeOnDelete = conf.getBooleanProperty("force.purgeOnDeleteSchema");
        
        schemaWriter = new ForceSchemaWriter(new SchemaDeleteProperty(forDelete, purgeOnDelete));
        
        // how often should the evictor run
        poolTimeBetweenEvictionRunsMillis = conf.getIntProperty("datanucleus.connectionPool.timeBetweenEvictionRunsMillis");
        if (poolTimeBetweenEvictionRunsMillis == 0) {
            poolTimeBetweenEvictionRunsMillis = 15 * 1000; // default, 15 secs
        }
         
        // how long may a connection sit idle in the pool before it may be evicted
        poolMinEvictableIdleTimeMillis = conf.getIntProperty("datanucleus.connectionPool.minEvictableIdleTimeMillis");
        if (poolMinEvictableIdleTimeMillis == 0) {
            poolMinEvictableIdleTimeMillis = 30 * 1000; // default, 30 secs
        }
        
        // setup optimistic enabled
        Object isEnabled = omfContext.getPersistenceConfiguration().getProperty("datanucleus.Optimistic");
        enableOptimisticTransactions = isEnabled != null && (isEnabled instanceof Boolean && isEnabled.equals(Boolean.TRUE)
                || isEnabled instanceof String && "true".equals(isEnabled));
        
        logConfiguration();
    }
    
    private void setCustomPluginManager() throws NoSuchFieldException, IllegalAccessException {
        PluginManager pluginMgr = omfContext.getPluginManager();
        Field registryField = PluginManager.class.getDeclaredField("registry");
        registryField.setAccessible(true);
        registryField.set(pluginMgr, new ForcePluginRegistry((PluginRegistry) registryField.get(pluginMgr)));
    }

    @Override
    protected void registerConnectionMgr() {
        super.registerConnectionMgr();
        this.connectionMgr.disableConnectionPool();
    }

    public ForceConnectorConfig getConfig() {
        return config;
    }
    
    /**
     * Releases resources.
     */
    @Override
    public void close() {
        omfContext.getMetaDataManager().deregisterListener(metadataListener);
        super.close();
    }

    @Override
    public NucleusConnection getNucleusConnection(ExecutionContext om) {
        return new NativeConnection(getConnection(om));
    }

    /**
     * Accessor for the supported options in string form.
     * {@inheritDoc}
     */
    @Override
    public Collection getSupportedOptions() {
        Set<String> set = new HashSet<String>();
        set.add("ApplicationIdentity");
        set.add("OptimisticTransaction");
        set.add("TransactionIsolationLevel.read-committed");
        return set;
    }
    
    /**
     * Specifies whether fields should be created automatically on app startup.
     * @return {@code true} if fields should be created
     */
    public boolean isAutoCreateColumns() {
        return autoCreateColumns;
    }
    
    /**
     * Specifies whether objects should be created automatically on app startup.
     * @return {@code true} if objects should be created
     */
    public boolean isAutoCreateTables() {
        return autoCreateTables;
    }
    
    /**
     * Used for schema mismatches. For example, a column or table is specified in an application,
     * but the column or table doesn't exist in the Force.com organization, and schema creation
     * is turned OFF for field or columns. Value defaults to {@code false} but can be set in persistence.xml,
     * if true warnings will be logged, exceptions will not be thrown.
     * 
     * @return {@code true} if schema mismatches should log warnings, {@code false} if they should throw errors
     */
    public boolean isAutoCreateWarnOnError() {
        return autoCreateWarnOnError;
    }
    
    public int getPoolMinEvictableIdleTimeMillis() {
        return poolMinEvictableIdleTimeMillis;
    }
    
    public int getPoolTimeBetweenEvictionRunsMillis() {
        return poolTimeBetweenEvictionRunsMillis;
    }
    
    /**
     * Retrieves Force.com table information from JPA class metadata.
     * 
     * @param acmd JPA class metadata
     * @return Force.com table information
     */
    public TableImpl getTable(AbstractClassMetaData acmd) {
        ForceStoreSchemaHandler sh = (ForceStoreSchemaHandler) getSchemaHandler();
        return sh.getTable(acmd);
    }
    
    /**
     * Registers a table with the schema handler.
     * 
     * @param acmd the class metadata of the table to register
     * @param mconn the managed connection to the Force.com APIs
     * @return the created TableImpl for this object
     */
    public TableImpl addTable(AbstractClassMetaData acmd, ForceManagedConnection mconn) {
        ForceStoreSchemaHandler sh = (ForceStoreSchemaHandler) getSchemaHandler();
        return sh.addTable(acmd, mconn);
    }
    
    /**
     * Registers a virtual table with the schema handler (a table not backed by
     * an actual object but is still needed for JPA queries, e.g. ForceOwner)
     * 
     * @param acmd the class metadata of the virtual table to register
     * @return the TableImpl for this object
     */
    public TableImpl addVirtualTable(AbstractClassMetaData acmd) {
        ForceStoreSchemaHandler sh = (ForceStoreSchemaHandler) getSchemaHandler();
        return sh.addVirtualTable(acmd);
    }
    
    /**
     * Specifies whether optimistic locking is enabled for this persistence unit. Optimistic transactions
     * prevent two separate transactions from updating the same record at the same time, thus
     * overwriting one transaction's update
     * @return {@code true} if optimistic transactions are enabled
     */
    public boolean isEnableOptimisticTransactions() {
        return enableOptimisticTransactions;
    }
    
    public ForceMetaDataListener getMetaDataListener() {
        return metadataListener;
    }

    /**
     * Creates a {@code ForceManagedConnection} for connection to Force.com.
     * @return the created managed connection to Force.com
     */
    public ForceManagedConnection createConnection() {
        ForceConnectionFactory connFactory =
            (ForceConnectionFactory) getConnectionManager().lookupConnectionFactory(FORCE_KEY);
            
        return (ForceManagedConnection) connFactory.createManagedConnection(null, null);
    }
    
    /**
     * Before entities are registered or created, we execute describe calls on the entities to determine
     * entity shape and whether the entities already exist.
     * 
     * @param classes a collection of class metadata to cache describe results for
     */
    void preInitialiseFileMetaData(Collection<AbstractClassMetaData> classes) {
        ForceManagedConnection mconn = createConnection();
        try {
            ((ForceStoreSchemaHandler) getSchemaHandler()).cacheDescribeSObjects(classes, mconn);
        } finally {
            mconn.release();
        }
    }
    
    /**
     * Clears out the describe calls for all entities in this application.
     */
    void postInitialiseFileMetaData() {
        ((ForceStoreSchemaHandler) getSchemaHandler()).clearDescribeSObjects();
    }
    
    public ForceSchemaWriter getSchemaWriter() {
        return schemaWriter;
    }
    
    /**
     * Specifies whether this application has been started up with the flag to delete schema
     * rather than create/upsert it.
     * @return {@code true} if this application should delete schema
     */
    public boolean isForDelete() {
        return forDelete;
    }
}
