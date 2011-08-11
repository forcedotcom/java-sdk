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

import static com.force.sdk.jpa.ForceEntityManager.LOGGER;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.OMFContext;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.jpa.metadata.JPAMetaDataManager;
import org.datanucleus.metadata.*;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

import com.force.sdk.jpa.model.ForceOwner;
import com.force.sdk.jpa.table.ColumnImpl;
import com.force.sdk.jpa.table.TableImpl;
import com.sforce.ws.ConnectionException;

/**
 * Custom Metadata Manager so we can control the timing of Force.com object and field creation.
 *
 * @author Jill Wetzler
 */
public class ForceMetaDataManager extends JPAMetaDataManager {
    
    /**
     * Creates the metadata manager.
     * 
     * @param ctxt the object manager factory context
     */
    public ForceMetaDataManager(OMFContext ctxt) {
        super(ctxt);
    }
    
    /**
     * Loads and syncs schema for all persistence units.  The call to the super class will do the main work --
     * everything DataNucleus needs plus the creation of custom SObjects in the user's Force.com organization. The second part
     * is to create the custom fields on each object.  We have to do these separately because the order of object
     * creation is not guaranteed, and this way we can be sure that all lookup fields will be referring to an object
     * that already exists
     * {@inheritDoc}
     */
    @Override
    public FileMetaData[] loadPersistenceUnit(PersistenceUnitMetaData pumd, ClassLoader loader) {
        // Check for a custom force connection name
        if (!omfContext.getPersistenceConfiguration().hasProperty("force.ConnectionName")) {
            
            // Default the force connection name to the persistence unit name.  This will be used to
            // potentially look up connection information (see ForceConnectionFactory.createManagedConnection)
            if (pumd.getName() != null && pumd.getName().length() != 0) {
                omfContext.getPersistenceConfiguration().setProperty("force.ConnectionName", pumd.getName());
            } else if (omfContext.getPersistenceConfiguration().getStringProperty("datanucleus.ConnectionUrl") == null) {
                throw new NucleusUserException("Must specify unit name or connection url");
            }
        }

        ForceStoreManager storeManager = (ForceStoreManager) omfContext.getStoreManager();
        if (storeManager.isSchemaCreateClient()) {
            /**
             * DataNucleus does not automatically initialize classes from jars.
             * When we are working with schema and we are using an existing artifact jar we have to
             * forceDN to look at the classes in the jar. To do that, we add the jar into the {@code PersistenceUnitMetaData}
             * as if the jar was included there. However, we should only add the jar if the persistence unit does not already
             * contain explicitly provided classes.
             */
            if ((pumd.getClassNames() == null || pumd.getClassNames().size() == 0)
                    && "jar".equals(pumd.getRootURI().getScheme())) {
                try {
                    String path = pumd.getRootURI().getSchemeSpecificPart();
                    pumd.addJarFile(new URL(path.substring(0, path.length() - 1)));
                } catch (MalformedURLException ue) {
                    throw new NucleusUserException(ue.getMessage());
                }
            }
            if (loader == null) {
                loader = Thread.currentThread().getContextClassLoader();
            }
        }
        
        // The ForceOwner entity is provided by force-jpa
        // so make sure it is loaded for all persistence units
        pumd.addClassName(ForceOwner.class.getName());
        FileMetaData[] fileMD = super.loadPersistenceUnit(pumd, loader);
        
        for (FileMetaData md : fileMD) {
            for (int i = 0; i < md.getNoOfPackages(); i++) {
                PackageMetaData pmd = md.getPackage(i);
                for (int j = 0; j < pmd.getNoOfClasses(); j++) {
                    ClassLoaderResolver clr = omfContext.getClassLoaderResolver(null);
                    ClassMetaData cmd = pmd.getClass(j);
                    Class c = null;
                    String className = cmd.getFullClassName();
                    try {
                        if (loader == null) {
                            c = Class.forName(className);
                        } else {
                            c = clr.classForName(className, null, false);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new NucleusException(e.getMessage());
                    }
                    
                    try {
                        createSchema(cmd, clr.classForName(cmd.getFullClassName(), c.getClassLoader()), clr, storeManager);
                    } catch (NucleusException ne) {
                        throw ne;
                    }
                }
            }
        }
        
        // Emit all metadata
        new ClassInitializer() {
            @Override
            void init(AbstractClassMetaData cmd, ForceStoreManager storeManager, ForceManagedConnection mconn)
                throws ConnectionException {
                
                TableImpl table = storeManager.getTable(cmd);
                table.getMetaData(cmd).emit(storeManager, mconn);
            }
        } .initialize(fileMD, storeManager);
        
        // now do the writing
        try {
            storeManager.getSchemaWriter().write(storeManager.createConnection());
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new NucleusException(e.getMessage(), e);
            }
        }
        
        // Reload new metadata
        new ClassInitializer() {
            @Override
            void init(AbstractClassMetaData cmd, ForceStoreManager storeManager, ForceManagedConnection mconn)
                throws ConnectionException {
                
                TableImpl table = storeManager.getTable(cmd);
                if (!table.isValid()) {
                    table.refresh(null, mconn);
                }
            }
        } .initialize(fileMD, storeManager);
        
        for (FileMetaData md : fileMD) {
            for (int i = 0; i < md.getNoOfPackages(); i++) {
                PackageMetaData pmd = md.getPackage(i);
                for (int j = 0; j < pmd.getNoOfClasses(); j++) {
                    ClassMetaData cmd = pmd.getClass(j);
                    if (PersistenceUtils.hasNoSchema(cmd) && cmd.getTable() == null) {
                        continue;
                    }
                    try {
                        populateFieldNames(cmd, getOMFContext());
                    } catch (ConnectionException ex) {
                        throw new NucleusException("Exception during initialization", ex);
                    }
                }
            }
        }
        
        return fileMD;
    }
    
    /**
     * Creates all the CustomObjects and CustomFields and registers them with the {@code SchemaWriter}.
     * The SchemaWriter's {@code write()} call is made after calling this method.
     */
    private void createSchema(final ClassMetaData cmd, Class cls, final ClassLoaderResolver clr, ForceStoreManager storeManager) {
        synchronized (cmd) {
            ForceManagedConnection mconn = storeManager.createConnection();
            TableImpl table = storeManager.getTable(cmd);
            
            if (PersistenceUtils.hasNoSchema(cmd)) return; //nothing to create
            
            // only create the table if autoCreateTables is true.
            // create the fields without creating the table if the object is already in the organization
            if (storeManager.isAutoCreateTables() && (!table.getTableAlreadyExistsInOrg()
                    || storeManager.isForDelete()) && !PersistenceUtils.isReadOnlySchema(cmd, true)) {
                table.createTableAndFields(cmd, storeManager, mconn);
            } else if (table.getTableAlreadyExistsInOrg() && !PersistenceUtils.isReadOnlySchema(cmd, false)) {
                table.createFields(cmd, storeManager);
            }
            
            // handle the case where autoCreateTables is disabled but the object does not exist in the organization
            if (!storeManager.isAutoCreateTables() && !table.getTableAlreadyExistsInOrg()) {
                StringBuilder msg = new StringBuilder(256);
                msg.append("Table does not exist in force.com and datanucleus.autoCreateTables is false, table: ")
                   .append(table.getTableName().getForceApiName());
                if (storeManager.isAutoCreateWarnOnError()) {
                    LOGGER.warn(msg.toString());
                } else {
                    throw new NucleusUserException(msg.toString());
                }
            }
        }
    }
    
    /**
     * Initialises the provided FileMetaData so that it's ready for use.
     * We copied this method verbatim from base class only to be 
     * able to call the pre/postInitialise methods.
     * 
     * @param fileMetaData Collection of {@code FileMetaData}
     * @param clr ClassLoader resolver
     * @throws NucleusUserException thrown if an error occurs during the populate/initialise
     *     of the supplied metadata.
     */
    @Override
    protected void initialiseFileMetaDataForUse(Collection fileMetaData, ClassLoaderResolver clr)
    {
        HashSet<Throwable> exceptions = new HashSet<Throwable>();

        // a). Populate MetaData
        if (NucleusLogger.METADATA.isDebugEnabled()) {
            NucleusLogger.METADATA.debug(LOCALISER.msg("044018"));
        }
        Iterator iter = fileMetaData.iterator();
        while (iter.hasNext()) {
            FileMetaData filemd = (FileMetaData) iter.next();
            if (!filemd.isInitialised()) {
                populateFileMetaData(filemd, clr, null);
            }
        }

        /**
         * Call preInitialise here
         */
        ForceStoreManager storeManager = (ForceStoreManager) omfContext.getStoreManager();
        storeManager.preInitialiseFileMetaData(classMetaDataByClass.values());
        
        // b). Initialise MetaData
        if (NucleusLogger.METADATA.isDebugEnabled()) {
            NucleusLogger.METADATA.debug(LOCALISER.msg("044019"));
        }
        iter = fileMetaData.iterator();
        while (iter.hasNext()) {
            FileMetaData filemd = (FileMetaData) iter.next();
            if (!filemd.isInitialised()) {
                try {
                    initialiseFileMetaData(filemd, clr, null);
                } catch (Exception e) {
                    NucleusLogger.METADATA.error(StringUtils.getStringFromStackTrace(e));
                    exceptions.add(e);
                }
            }
        }
        if (exceptions.size() > 0) {
            throw new NucleusUserException(LOCALISER.msg("044020"),
                exceptions.toArray(new Throwable[exceptions.size()]));
        }
        /**
         * Call postInitialise here
         */
        storeManager.postInitialiseFileMetaData();
    }
    
    private void populateFieldNames(AbstractClassMetaData acmd, OMFContext omf) throws ConnectionException {
        ForceStoreManager storeManager = (ForceStoreManager) omfContext.getStoreManager();
        
        // Grab transactional connection factory
        TableImpl table = storeManager.getTable(acmd);
        // now that we've stored all the Salesforce fields, find the {@code @Entity} definition and register the
        // Java field name on the ColumnImpl.  We'll need this info later
        int[] fieldNumbers =  acmd.getAllMemberPositions();
        if (fieldNumbers != null && fieldNumbers.length > 0) {
            for (int column : fieldNumbers) {
                addColumn(table, acmd.getMetaDataForManagedMemberAtAbsolutePosition(column), omf);
            }
        }
        
        DiscriminatorMetaData dmd = acmd.getDiscriminatorMetaData();
        if (dmd != null && dmd.getColumnName() != null) {
            ColumnImpl col = table.getColumnByForceApiName(dmd.getColumnName());
            if (col != null) {
                String javaFieldName = dmd.getColumnName();
                table.registerJavaColumn(javaFieldName, col);
            }
        }
        
    }
    
    private void addColumn(TableImpl table, AbstractMemberMetaData ammd, OMFContext omf) {
        if (PersistenceUtils.isNonPersistedColumn(ammd)) return;
        if (ammd.getEmbeddedMetaData() != null) {
            for (AbstractMemberMetaData eammd : ammd.getEmbeddedMetaData().getMemberMetaData()) {
                addColumn(table, eammd, omf);
            }
        } else {
            ColumnImpl col = table.getColumnByForceApiName(PersistenceUtils.getForceApiName(ammd, omf));
            if (col != null) {
                String javaFieldName = ammd.getName();
                table.registerJavaColumn(javaFieldName, col);
            }
        }
    }

    /**
     * Inner class for providing initialization methods when entities are first loaded.
     */
    private abstract static class ClassInitializer {

        /**
         * Provides an init method which will be run when the class is initialized.
         *
         * @param cmd the class metadata for the entity being initialized
         * @param storemanager  the store manager
         * @param mconn  managed connection for the Force.com APIs
         * @throws ConnectionException thrown if connecting to the Force.com API fails
         */
        abstract void init(AbstractClassMetaData cmd, ForceStoreManager storemanager, ForceManagedConnection mconn)
            throws ConnectionException;
        
        void initialize(FileMetaData[] fileMD, final ForceStoreManager storeManager) {
            for (FileMetaData md : fileMD) {
                for (int i = 0; i < md.getNoOfPackages(); i++) {
                    PackageMetaData pmd = md.getPackage(i);
                    for (int j = 0; j < pmd.getNoOfClasses(); j++) {
                        ClassMetaData cmd = pmd.getClass(j);
                        if (PersistenceUtils.hasNoSchema(cmd) && cmd.getTable() == null) {
                            continue;
                        }
                        try {
                            ForceManagedConnection mconn = storeManager.createConnection();
                            try {
                                init(cmd, storeManager, mconn);
                            } finally {
                                mconn.close();
                            }
                        } catch (ConnectionException ex) {
                            throw new NucleusException("Exception during initialization", ex);
                        }
                    }
                }
            }
        }
    }
}
