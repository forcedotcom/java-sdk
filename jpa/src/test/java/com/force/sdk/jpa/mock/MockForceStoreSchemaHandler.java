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

package com.force.sdk.jpa.mock;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;

import mockit.Mock;
import mockit.MockClass;

import org.datanucleus.metadata.AbstractClassMetaData;

import com.force.sdk.jpa.ForceManagedConnection;
import com.force.sdk.jpa.PersistenceUtils;
import com.force.sdk.jpa.annotation.CustomObject;
import com.force.sdk.jpa.model.Owner;
import com.force.sdk.jpa.schema.ForceStoreSchemaHandler;
import com.force.sdk.jpa.table.TableImpl;

/**
 * A mock ForceStoreSchemaHandler class.
 * <p>
 * This class is useful for query tests.
 * Any registered class can be served up
 * to the JPA query builder.
 *
 * @author Tim Kral
 */
@MockClass(realClass = ForceStoreSchemaHandler.class,
           stubs = { "initialize", "cacheDescribeSObjects", "clearDescribeSObjects" })
public final class MockForceStoreSchemaHandler {
    
    private Map<String, TableImpl> tables = new HashMap<String, TableImpl>();
    
    @Mock
    public TableImpl getTable(AbstractClassMetaData cmd) {
        // Any time the query builder wants table information, we'll
        // serve it up here
        return tables.get(PersistenceUtils.getEntityName(cmd));
    }
    
    @Mock
    public TableImpl addTable(AbstractClassMetaData acmd, ForceManagedConnection conn) {
        return getTable(acmd);
    }
    
    // Iterates through the com.force.sdk.jpa.entities package and registers
    // all JPA entities found within
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void registerAllTables() throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assertNotNull(classLoader, "Unable to load ClassLoader to register JPA entities.");
        
        // We'll load all JPA entities from the com.force.sdk.jpa.entities package
        Enumeration<URL> entityFileURLs = classLoader.getResources("com/force/sdk/jpa/entities/");
        assertTrue(entityFileURLs.hasMoreElements(), "Cannot find com/force/sdk/jpa/entities resource");

        // There should only be one base resource directory
        File entityFileDir = new File(entityFileURLs.nextElement().getFile());
        // load all classes in subpackages of the resource directory
        List<File> entityList = getListEntityClasses(entityFileDir);
        
        // regex pattern for package path from class file path
        Pattern pat = Pattern.compile("^.+/jpa/target/test-classes/(.+)/(.+)\\.class$");
        
        // Register all of our JPA entities here.
        // This would normally be done by PersistenceUtils in createSchema or loadSchema
        for (File entityFile : entityList) {
            // get package name and class name from entity file path
            Matcher mat = pat.matcher(entityFile.getCanonicalPath());
            mat.matches();
            String packageName = mat.group(1).replace("/", ".") + ".";
            String entityClassName = packageName + mat.group(2);
            Class entityClass = Class.forName(entityClassName);

            if (entityClass.isAnnotationPresent(Entity.class)) {
                registerTable(entityClass);
            }
        }
        
        // All persistence units load the Owner class so make sure it's registered
        // (see ForceMetaDataManager.loadPersistenceUnit)
        registerTable(Owner.class);
    }
    
    @SuppressWarnings("rawtypes")
    public void registerTable(Class<?> entityClass) {
        TableImpl tableImpl;
        CustomObject customObjectAnnotation = entityClass.getAnnotation(CustomObject.class);
        if (customObjectAnnotation != null && customObjectAnnotation.virtualSchema()) {
            tableImpl = MockPersistenceUtils.constructVirtualTableImpl(entityClass);
        } else {
            tableImpl = MockPersistenceUtils.constructTableImpl(entityClass);
        }
        
        tables.put(entityClass.getSimpleName(), tableImpl);
    }
    
    // search recursively and get a list of all subpackage entity class files
    private List<File> getListEntityClasses(File file) {
        List<File> entityList = new ArrayList<File>();
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                entityList.addAll(getListEntityClasses(subFile));
            }
        } else if (file.getPath().endsWith(".class")) {
            entityList.add(file);
        }
        return entityList;
    }
    
}
