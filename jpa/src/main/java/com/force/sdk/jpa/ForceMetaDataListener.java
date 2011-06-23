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

import java.util.Calendar;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.*;
import org.datanucleus.util.Localiser;

import com.force.sdk.jpa.annotation.ForceAnnotationReader;
import com.force.sdk.jpa.schema.ForceClassMetaData;

/**
 * 
 * Customized version of a {@code MetaDataListener} that does all the registration and
 * setup needed when an application starts.  The object schema is validated
 * and cached.
 *
 * @author Fiaz Hossain
 */
public class ForceMetaDataListener implements MetaDataListener {

    /** Localiser for messages. */
    protected static final Localiser LOCALISER = Localiser.getInstance(
        "org.datanucleus.Localisation", ForceStoreManager.class.getClassLoader());

    private ForceStoreManager storeManager;
    private ForceAnnotationReader annotationReader;
    
    /**
     * 
     * Create a {@code MetaDataListener} and store off the store manager, instantiates the
     * annotation reader.
     * 
     * @param storeManager  the store manager with the persistence properties that should be used
     */
    ForceMetaDataListener(ForceStoreManager storeManager) {
        this.storeManager = storeManager;
        this.annotationReader = new ForceAnnotationReader(storeManager.getMetaDataManager());
    }
    
    /**
     * This method is called when an entity is loaded during startup. There is
     * some basic validation that happens here as well as the caching of objects 
     * (@Entity classes).  Registration of fields happens in the {@link ForceMetaDataManager}
     * 
     * @param cmd  the class metadata to load
     */
    @Override
    public void loaded(AbstractClassMetaData cmd) {
        if (cmd.getIdentityType() == IdentityType.DATASTORE && !cmd.isEmbeddedOnly())
        {
            throw new InvalidMetaDataException(LOCALISER, "force.DatastoreID", cmd.getFullClassName());
        }
        /**
         * Annotations do not work at field level. We hack that in here for now.
         */
        updateAnnotations(cmd);

        /**
         * Validate that the annotations and extensions are supported
         */
        validateExtensions(cmd);
        
        /**
         * Load object schema and register it --creation happens in the ForceMetaDataManager
         */
        registerObjectSchema(cmd);
    }

    /**
     * This is a lot of hackery. Basically, force the class to go through ForceAnnotationReader
     * and create a fake class that has Force.com annotations read in as 
     * extension data. We then transfer the extension data from the fake class to the actual one.
     *
     * @param cmd - actual class metadata that will get updated with Force.com annotations
     */
    private void updateAnnotations(AbstractClassMetaData cmd) {
        ClassLoaderResolver clr = storeManager.getOMFContext().getClassLoaderResolver(storeManager.getClass().getClassLoader());
        Class<?> clazz = clr.classForName(cmd.getFullClassName());
        AbstractClassMetaData cmdNew = annotationReader.getMetaDataForClass(clazz, cmd.getPackageMetaData(), clr);
        ExtensionMetaData[] extensions = cmdNew.getExtensions();
        if (extensions != null && extensions.length > 0) {
            for (ExtensionMetaData e : extensions) {
                if (ForceStoreManager.FORCE_KEY.equals(e.getVendorName())) {
                    cmd.addExtension(ForceStoreManager.FORCE_KEY, e.getKey(), e.getValue());
                }
            }
        }
        for (AbstractMemberMetaData ammdNew : ((ForceClassMetaData) cmdNew).getMembers()) {
            AbstractMemberMetaData ammd = cmd.getMetaDataForMember(ammdNew.getName());
            for (ExtensionMetaData e : ammdNew.getExtensions()) {
                ammd.addExtension(e.getVendorName(), e.getKey(), e.getValue());
            }
        }
    }
    
    /**
     * Performs basic validations on the class metadata right after it's been loaded.
     * 
     * @param cmd  the class metadata to validate
     */
    private void validateExtensions(AbstractClassMetaData cmd) {
        // We do not support Secondary tables (yet)
        if (cmd.getJoinMetaData() != null && cmd.getJoinMetaData().length > 0) {
            throw new NucleusUserException("Secondary tables are not supported by Force.com datastore."
                                            + " Offending entity: " + cmd.getFullClassName());
        }
        // We do not support @PrimaryKeyJoinColumn
        if (cmd.getPrimaryKeyMetaData() != null) {
            throw new NucleusUserException("@PrimaryKeyJoinColumn is not supported by Force.com datastore."
                                            + " Offending entity: " + cmd.getFullClassName());
        }
        // We do not support @UniqueConstraint
        if (cmd.getUniqueMetaData() != null && cmd.getUniqueMetaData().length > 0) {
            throw new NucleusUserException("@UniqueConstraint is not supported by Force.com datastore."
                                            + " Offending entity: " + cmd.getFullClassName());
        }
        if (cmd.getInheritanceMetaData() != null) {
            // We only support SingleTable inheritance strategy
            if (cmd.getInheritanceMetaData().getStrategyForTree() != null
                    && !cmd.getInheritanceMetaData().getStrategyForTree().equals("SINGLE_TABLE")) {
                throw new NucleusUserException("Only SINGLE_TABLE inheritance strategy supported by Force.com datastore."
                                                + " Offending entity: " + cmd.getFullClassName());
            }
            // We do not support Attribute or Operation overrides
            if (cmd.getOverriddenMembers() != null && cmd.getOverriddenMembers().length > 0) {
                throw new NucleusUserException("@AttributeOverride or @AssociationOverride"
                                                + " is not supported by Force.com datastore."
                                                + " Offending entity: " + cmd.getFullClassName());
            }
        }
        
        AbstractClassMetaData superClass = cmd.getSuperAbstractClassMetaData();
        // This means we're expecting the super class to be persisted to its own table
        if (superClass != null && superClass.getInheritanceMetaData().getStrategy() != InheritanceStrategy.SUBCLASS_TABLE) {
            
            // We only allow discriminator column in base class
            if (cmd.getDiscriminatorMetaData() != null && cmd.getDiscriminatorMetaData().getColumnMetaData() != null) {
                throw new NucleusUserException("@DiscriminatorColumn is only supported for base entity classes. "
                        + "Offending entity: " + cmd.getFullClassName() + " Super class: " + superClass.getFullClassName());
            }
            // We do not allow table rename on subclasses
            if (cmd.getTable() != null) {
                throw new NucleusUserException("@Table is only supported for base entity classes. "
                        + "Offending entity: " + cmd.getFullClassName() + " Super class: " + superClass.getFullClassName());
            }
            // We cannot have an ID on subclass, there is already one on the base class
            if (cmd.getPKMemberPositions().length > 1) {
                throw new NucleusUserException("@Id is only supported for base entity classes. "
                        + "Offending entity: " + cmd.getFullClassName() + " Super class: " + superClass.getFullClassName());
            }
        } else if (superClass == null && !cmd.isEmbeddedOnly()) {
            // This section is for non-Embedded non-Subclasses i.e. Baseclasses with IDs
            // id field has to be single string and called id
            if (cmd.getPKMemberPositions().length != 1) {
                throw new NucleusUserException("Only single string column primary keys supported as ID by Force.com datastore."
                                                + " Offending entity: " + cmd.getFullClassName());
            } else {
                AbstractMemberMetaData ammd = cmd.getMetaDataForManagedMemberAtPosition(cmd.getPKMemberPositions()[0]);
                checkColumnOrFieldName("id", ammd, ammd.getName(), String.class,
                        "Primay field name should be ID. Offending entity: ", "ID field type should be String."
                        + " Offending entity: ", cmd);
                
                // The only supported strategy is Identity
                if (ammd.getValueStrategy() == null || ammd.getValueStrategy() != IdentityStrategy.IDENTITY) {
                    throw new NucleusUserException("@Id column requires value generation"
                                                    + " @GeneratedValue(strategy = GenerationType.IDENTITY)."
                                                    + " Offending entity: " + cmd.getFullClassName());
                }
            }
        }

        // Embedded only objects cannot have a table specified
        if (cmd.isEmbeddedOnly() && cmd.getTable() != null) {
            throw new NucleusUserException("Embedded objects cannot have table specification."
                                            + " Offending entity: " + cmd.getFullClassName());
        }
        // Version field must be called lastModified and have type Calendar or GregorianCalendar
        if (cmd.getVersionMetaData() != null) {
            checkColumnOrFieldName("lastModifiedDate", cmd.getMetaDataForMember(cmd.getVersionMetaData().getFieldName()),
                cmd.getVersionMetaData().getFieldName(), Calendar.class,
                "Version field name should be lastModifiedDate. Offending entity: ",
                "Version field type should be Calendar or GregorianCalendar. Offending entity: ", cmd);
        }
    }
    
    private static void checkColumnOrFieldName(String expectedName, AbstractMemberMetaData ammd, String fieldName, Class<?> clazz,
            String message1, String message2, AbstractClassMetaData cmd) {
        ColumnMetaData[] columnMD = ammd.getColumnMetaData();
        if (columnMD != null && columnMD.length > 0 && columnMD[0].getName() != null) {
            if (!expectedName.equalsIgnoreCase(columnMD[0].getName())) {
                throw new NucleusUserException(message1 + cmd.getFullClassName());
            }
        } else if (!expectedName.equalsIgnoreCase(fieldName)) {
            throw new NucleusUserException(message1 + cmd.getFullClassName());
        }
        if (!clazz.isAssignableFrom(ammd.getType())) {
            throw new NucleusUserException(message2 + cmd.getFullClassName());
        }
    }
    
    private void registerObjectSchema(AbstractClassMetaData cmd) {
        // We skip embedded and MappedSuperclass as they will inherit schema from the base class
        if (PersistenceUtils.hasNoSchema(cmd)) return;
        
        if (PersistenceUtils.isVirtualSchema(cmd)) {
            storeManager.addVirtualTable(cmd);
            return;
        }
        
        ForceManagedConnection mconn = null;
        try {
            mconn = storeManager.createConnection();
            storeManager.addTable(cmd, mconn);
        } catch (Exception e) {
            throw new NucleusException(e.getMessage(), e);
        } finally {
            if (mconn != null) {
                mconn.close();
            }
        }
    }
}
