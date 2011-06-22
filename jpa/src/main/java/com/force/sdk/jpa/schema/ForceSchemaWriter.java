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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.force.sdk.jpa.ForceManagedConnection;
import com.force.sdk.jpa.ForceStoreManager;
import com.force.sdk.jpa.table.ForceMetaData;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.CustomField;
import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.PackageTypeMembers;
import com.sforce.ws.bind.TypeMapper;
import com.sforce.ws.parser.XmlOutputStream;

/**
 * 
 * Writes schema to the Force.com database using the Metadata API. It can be used 
 * to create or delete objects/fields
 *
 * @author Fiaz Hossain
 */
public class ForceSchemaWriter extends ForceAsyncResultProcessor {

    private static final String DEPLOY_DIR = "target" + File.separator + "deploy";
    private static final String DEPLOY_ZIP_DIR = "target" + File.separator + "deploy" + File.separator + "zip";
    private static final String DELETE_MANIFEST_FILE = "destructiveChanges.xml";
    private static final String CREATE_MANIFEST_FILE = "package.xml";
    private static final String DEPLOY_ZIP = "deploy.zip";
    private static final String CUSTOM_OBJECT = "CustomObject";
    private static final String CUSTOM_FIELD = "CustomField";
    private static final String PACKAGE_VERSION = "21.0";
    
    private final SchemaDeleteProperty deleteProperty;
    private final Map<CustomObject, PackageTypeMembers> objects = new HashMap<CustomObject, PackageTypeMembers>();
    private final Map<CustomField, PackageTypeMembers> fields = new HashMap<CustomField, PackageTypeMembers>();
    
    /**
     * Creates a schema writer for generating the deploy zip and using the Metadata API
     * to manipulate schema.
     * 
     * @param deleteProperty whether this application has been started with a persistence.xml
     *                       property that causes schema to be deleted rather than upserted,
     *                       may also have purgeOnDeleteSchema set
     */
    public ForceSchemaWriter(SchemaDeleteProperty deleteProperty) {
        this.deleteProperty = deleteProperty;
    }

    /**
     * Adds a custom object to the map of objects that will be created or deleted.
     * 
     * @param object  the {@link CustomObject} to write to the deploy file
     * @param cmd  the metadata of the JPA entity the CustomObject represents
     * @param storeManager the store manager for entities
     * @param fmd  the Force.com specific metadata for this object
     */
    public void addCustomObject(CustomObject object, AbstractClassMetaData cmd, ForceStoreManager storeManager,
            ForceMetaData fmd) {
        //if we're deleting, include in package file if table is not read only.
        //if we're upserting, include table only if the table does not yet exist.
        addCustomObject(object, deleteProperty.getDeleteSchema()
                        ? !fmd.getIsReadOnlyTable() : !fmd.getTableImpl().getTableAlreadyExistsInOrg());

    }

    /**
     * This method is used by test cleanup. Use {@link #addCustomObject(com.sforce.soap.metadata.CustomObject,
     * org.datanucleus.metadata.AbstractClassMetaData, com.force.sdk.jpa.ForceStoreManager,
     * com.force.sdk.jpa.table.ForceMetaData)} if you're intending to use schemaCreation. includeInPackageFile
     * can be {@code true} or {@code false} depending on the table metadata.
     *
     * @param object  the object to be included in schema creation or deletion
     * @param includeInPackageFile whether the package.xml or destructiveChanges.xml file should include this object
     */
    public void addCustomObject(CustomObject object, boolean includeInPackageFile) {
        PackageTypeMembers type = null;
        if (includeInPackageFile) {
            type = new PackageTypeMembers();
            type.setName(CUSTOM_OBJECT);
            type.setMembers(new String[] {object.getFullName()});
        }
        objects.put(object, type);
    }
    
    /**
     * Adds a custom field to the map which will be written to the package file later
     * for deployment.
     * 
     * @param object  the custom object this field belongs to
     * @param field  the {@link CustomField} object that will be upserted or deleted
     */
    public void addCustomField(CustomObject object, CustomField field) {
        //if we are deleting the object and it is included in the package i.e. objects contains a non null PackageTypeMember,
        // then there is no point adding fields for delete individually
        if (deleteProperty.getDeleteSchema() && objects.get(object) != null) return;
        PackageTypeMembers type = new PackageTypeMembers();
        type.setName(CUSTOM_FIELD);
        type.setMembers(new String[] {String.format("%s.%s", object.getFullName(), field.getFullName())});
        fields.put(field, type);
    }
    
    /**
     * Creates the proper deploy zip and makes the Metadata API call.
     * 
     * @param mconn the Metadata API connection
     * @throws Exception thrown if something goes wrong during the schema write
     */
    public void write(ForceManagedConnection mconn) throws Exception {
        File deployDir = new File(DEPLOY_DIR);
        if (!deployDir.exists() && !deployDir.mkdirs()) {
            throw new NucleusUserException("Cannot create deploy staging directory: " + deployDir);
        }
        File deployZipDir = new File(DEPLOY_ZIP_DIR);
        if (!deployZipDir.exists() && !deployZipDir.mkdirs()) {
            throw new NucleusUserException("Cannot create directory for deploy zip: " + deployZipDir);
        }
        if (objects.size() == 0 && fields.size() == 0) return; //nothing to deploy
        List<File> schemaFiles = new ArrayList<File>();
        if (deleteProperty.getDeleteSchema()) {
            createPackageFile(DELETE_MANIFEST_FILE, objects.values(), fields.values());
            createPackageFile(CREATE_MANIFEST_FILE, null, null);
            schemaFiles.add(new File(DEPLOY_DIR, DELETE_MANIFEST_FILE));
            schemaFiles.add(new File(DEPLOY_DIR, CREATE_MANIFEST_FILE));
        } else {
            createPackageFile(CREATE_MANIFEST_FILE, objects.values(), fields.values());
            schemaFiles.add(new File(DEPLOY_DIR, CREATE_MANIFEST_FILE));
            createSchemaFiles(schemaFiles);
        }
        
        DeployOptions deployOptions = new DeployOptions();
        deployOptions.setPerformRetrieve(false);
        deployOptions.setRollbackOnError(true);
        deployOptions.setSinglePackage(true);
        deployOptions.setAutoUpdatePackage(true);
        deployOptions.setAllowMissingFiles(true);
        deployOptions.setPurgeOnDelete(deleteProperty.getPurgeSchemaOnDelete());

        MetadataConnection metadatabinding = mconn.getMetadataConnection();
        createZipFile(schemaFiles.toArray(new File[schemaFiles.size()]), DEPLOY_ZIP);
        AsyncResult asyncResult = metadatabinding.deploy(readZipFile(DEPLOY_ZIP), deployOptions);

        // Wait for the deploy to complete  
        waitForAsyncResult(metadatabinding, new AsyncResult[] {asyncResult}, true, DEPLOY_ZIP);

        DeployResult result = metadatabinding.checkDeployStatus(asyncResult.getId());
        
        // Log any messages  
        StringBuilder buf = new StringBuilder();
        if (result.getMessages() != null) {
            for (DeployMessage rm : result.getMessages()) {
                if (rm.getProblem() != null) {
                    buf.append("Error deploying: " + rm.getFileName() + " - " + rm.getProblem() + " \n ");
                }
            }
        }
        if (buf.length() > 0) {
            LOGGER.error("Deploy warnings:\n" + buf);
            throw new NucleusDataStoreException(buf.toString());
        }
    }
    
    private void createSchemaFiles(List<File> schemaFiles) throws IOException {
        TypeMapper typeMapper = new TypeMapper();
        for (CustomObject obj : objects.keySet()) {
            File f = new File(DEPLOY_DIR, obj.getFullName() + ".object");
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(f);
            XmlOutputStream xout = new XmlOutputStream(fos, true);
            xout.setPrefix("", "http://soap.sforce.com/2006/04/metadata");
            try {
                xout.startDocument();
                obj.write(new QName("http://soap.sforce.com/2006/04/metadata", "CustomObject"), xout, typeMapper);
            } finally {
                xout.endDocument();
                xout.close();
                fos.close();
            }
            
            schemaFiles.add(f);
        }
    }
    
    private void createZipFile(File[] files, String zipFileName) throws IOException {
        File resultsFile = new File(DEPLOY_ZIP_DIR, zipFileName);
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(resultsFile)));
        byte[] buf = new byte[8092];
        try {
            for (File file : files) {
                String filename = file.getName();
                if (file.getName().endsWith(".object")) {
                    filename = "objects/" + filename;
                }
                
                LOGGER.debug("Adding file to zip: " + file.getAbsolutePath());
                // Add ZIP entry to output stream.
                zos.putNextEntry(new ZipEntry(filename));

                BufferedInputStream src = new BufferedInputStream(new FileInputStream(file));
                try {
                    int len;
                    while ((len = src.read(buf)) > 0) {
                        zos.write(buf, 0, len);
                    }
                } finally {
                    src.close();
                }
                // Close the entry
                zos.closeEntry();
            }
            
            LOGGER.debug("Results written to " + resultsFile.getAbsolutePath());
        } finally {
            zos.close();
        }
    }
    
    /**
     * Reads the zip file contents into a byte array.
     * @return byte[]
     * @throws Exception - if cannot find the zip file to deploy
     */  
    private byte[] readZipFile(String fileName) throws Exception {
        // We assume here that you have a deploy.zip file.  
        File deployZip = new File(DEPLOY_ZIP_DIR, fileName);
        if (!deployZip.exists() || !deployZip.isFile())
            throw new Exception("Cannot find the zip file to deploy. Looking for "
                    + deployZip.getAbsolutePath());
        
        ByteArrayOutputStream bos = null;
        InputStream is = new BufferedInputStream(new FileInputStream(deployZip));
        try {
            bos = new ByteArrayOutputStream();
            int readbyte = -1;
            while ((readbyte = is.read()) != -1)  {
                bos.write(readbyte);
            }
        } finally {
            try {
                is.close();
            } finally {
                bos.close();
            }
        }
        return bos.toByteArray();
    }

    private static void createPackageFile(String fileName, Collection<PackageTypeMembers> objects,
            Collection<PackageTypeMembers> fields) throws ParserConfigurationException, TransformerException, IOException {
        /*
        <?xml version="1.0" encoding="UTF-8"?>
        <Package xmlns="http://soap.sforce.com/2006/04/metadata">
            <types>
                <members>MyCustomObject__c</members>
                <name>CustomObject</name>
            </types>
            <types>
                <members>*</members>
                <name>CustomTab</name>
            </types>
            <types>
                <members>Standard</members>
                <name>Profile</name>
            </types>
            <version>21.0</version>
        </Package>
         */
        DocumentBuilder db =
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document d = db.newDocument();
        Element p = d.createElementNS("http://soap.sforce.com/2006/04/metadata", "Package");
        Element version = d.createElement("version");
        version.appendChild(d.createTextNode(PACKAGE_VERSION));
        p.appendChild(version);
        if (fields != null && fields.size() > 0) {
            Node t = d.createElement("types");
            for (PackageTypeMembers type : fields) {
                Element members = d.createElement("members");
                members.appendChild(d.createTextNode(type.getMembers()[0]));
                t.appendChild(members);
            }
            Element name = d.createElement("name");
            name.appendChild(d.createTextNode(CUSTOM_FIELD));
            t.appendChild(name);
            p.appendChild(t);
        }
        if (objects != null && objects.size() > 0) {
            Node t = d.createElement("types");
            for (PackageTypeMembers type : objects) {
                if (type != null) {
                    Element members = d.createElement("members");
                    members.appendChild(d.createTextNode(type.getMembers()[0]));
                    t.appendChild(members);
                }
            }
            Element name = d.createElement("name");
            name.appendChild(d.createTextNode(CUSTOM_OBJECT));
            t.appendChild(name);
            p.appendChild(t);
        }
        d.appendChild(p);
        
        // Now write to file
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer tx = tf.newTransformer();
        DOMSource source = new DOMSource(d);
        StreamResult result = new StreamResult(new File(DEPLOY_DIR, fileName));
        tx.setOutputProperty(OutputKeys.INDENT, "yes");
        tx.transform(source, result);
    }
}
