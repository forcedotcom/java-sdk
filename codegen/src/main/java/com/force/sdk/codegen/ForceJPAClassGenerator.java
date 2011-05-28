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

package com.force.sdk.codegen;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.lang.model.SourceVersion;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.StringTemplateGroup;

import com.force.sdk.codegen.filter.*;
import com.force.sdk.codegen.renderer.ForceJPAClassRenderer;
import com.force.sdk.codegen.renderer.ForceJPAFieldRenderer;
import com.force.sdk.codegen.selector.ForceJPAClassDataSelector;
import com.force.sdk.codegen.template.StringTemplateWrapper;
import com.force.sdk.codegen.writer.ForceJPAFileWriterProvider;
import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceServiceConnector;
import com.google.common.collect.ImmutableSet;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectionException;

/**
 * Generator for Force.com JPA enabled Java classes.
 * <p>
 * This will generate a closed set of Force.com JPA enabled Java classes based on a list of
 * Force.com schema object (SObject) names from a Force.com store (organization).  For example, 
 * a caller wishing to generate a Java class for the Account object will get the Account Java 
 * class plus the all Java classes to which the Account class will refer (i.e. the Account 
 * object's references).  If the first name in the list of schema object names is a single star 
 * ("*") then the generator will produce Java classes for all known objects in the Force.com
 * store (organization).
 * <p>
 * A {@code ForceJPAClassGenerator} has a predetermined set of Force.com schema objects that
 * will always be generated in addition to what is requested by the caller.
 *
 * @author Tim Kral
 */
public class ForceJPAClassGenerator {

    // The set of standard objects that will always be generated (along with their dependencies)
    /* package */ static final Set<String> STANDARD_OBJECTS = ImmutableSet.<String>of("User");
    
    // The renderers for Force.com JPA object generation
    private static final Map<Class<?>, AttributeRenderer> RENDERER_MAP =
        new HashMap<Class<?>, AttributeRenderer>(2);
    
    static {
        RENDERER_MAP.put(DescribeSObjectResult.class, new ForceJPAClassRenderer());
        RENDERER_MAP.put(Field.class, new ForceJPAFieldRenderer());
    }
    
    // Connection to the org that we're doing the generation for
    private final PartnerConnection conn;
    
    // The destination of the generated Java classes
    private final File destDir;
    
    // Allow a static package name (as opposed to a dynamically
    // generated package name)
    private String packageName;
    
    /**
     * Initializes a {@code ForceJPAClassGenerator} with a named {@link ForceConnectorConfig}
     * source and a destination (project) directory.
     * <p>
     * The {@code ForceJPAClassGenerator} will obtain a connection to the Force.com service
     * using the {@code ForceConnectorConfig} source.  Generated Java classes will be written
     * to the destination directory. 
     * 
     * @param connectionName a named {@code ForceConnectorConfig} source
     * @param destDir the destination (project) directory to which the generated Java classes
     *                will be written
     * @throws ConnectionException if there is an error connecting to the Force.com service
     */
    public ForceJPAClassGenerator(String connectionName, File destDir) throws ConnectionException {
        ForceServiceConnector connector = new ForceServiceConnector(connectionName);
        this.conn = connector.getConnection();
        this.destDir = destDir;
    }

    /**
     * Initializes a {@code ForceJPAClassGenerator} with a {@link ForceConnectorConfig}
     * and a destination (project) directory.
     * <p>
     * The {@code ForceJPAClassGenerator} will obtain a connection to the Force.com service
     * using the {@code ForceConnectorConfig}.  Generated Java classes will be written
     * to the destination directory. 
     * 
     * @param config a {@code ForceConnectorConfig}
     * @param destDir the destination (project) directory to which the generated Java classes
     *                will be written
     * @throws ConnectionException if there is an error connecting to the Force.com service
     */
    public ForceJPAClassGenerator(ForceConnectorConfig config, File destDir) throws ConnectionException {
        ForceServiceConnector connector = new ForceServiceConnector(config);
        this.conn = connector.getConnection();
        this.destDir = destDir;
    }
    
    /**
     * Initializes a {@code ForceJPAClassGenerator} with a connection to the Force.com
     * service and a destination (project) directory.
     * <p>
     * Generated Java classes will be written to the destination directory. 
     * 
     * @param conn a {@code PartnerConnection} to the Force.com service
     * @param destDir the destination (project) directory to which the generated Java classes
     *                will be written
     */    
    public ForceJPAClassGenerator(PartnerConnection conn, File destDir) {
        this.conn = conn;
        this.destDir = destDir;
    }
    
    /**
     * Sets the Java package name under which the Java classes
     * will be generated.
     * 
     * @param packageName a non {@code null} {@code String} which conforms
     *                    to Java package naming standards
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Execute the Java class generation.
     * 
     * @param objectNames the Force.com schema objects that are to be generated as
     *                    Java classes (along with their references)
     * @return the number of Force.com JPA enabled Java classes generated
     * @throws ConnectionException if an error occurs trying to connection to the Force.com service
     * @throws IOException if an error occurs trying to write out the generated Java classes
     */
    public int generateJPAClasses(List<String> objectNames) throws ConnectionException, IOException {
        // Generate objects (see ForceObject.st)
        CodeGenerator codeGenerator = createClassGenerator(objectNames);
        return codeGenerator.generateCode(conn);
    }
    
    private CodeGenerator createClassGenerator(List<String> objectNames) {
        CodeGenerator.Builder builder = new CodeGenerator.Builder();
        
        if (objectNames == null) {
            throw new IllegalArgumentException("Object name list is null");
        }
        
        // If the first object name parameter a '*' then we'll
        // generate source files for all the org's SObjects
        DataFilter filter;
        if (objectNames.size() > 0 && "*".equals(objectNames.get(0))) {
            filter = new NoOpDataFilter();
        } else {
            Set<String> objectNameSet = new HashSet<String>(STANDARD_OBJECTS);
            objectNameSet.addAll(objectNames);
            filter = new ObjectNameWithRefDataFilter(objectNameSet);
        }
        
        ForceJPAClassDataSelector selector = new ForceJPAClassDataSelector();
        
        StringTemplateWrapper template =
            new StringTemplateWrapper(new StringTemplateGroup("JPA").getInstanceOf("templates/ForceObject"));
        template.setAttributeRenderers(RENDERER_MAP);
        
        ForceJPAFileWriterProvider writerProvider = new ForceJPAFileWriterProvider(destDir);
        
        if (packageName != null) {
            if (!isValidPackageName(packageName)) {
                throw new IllegalArgumentException("Invalid package name: " + packageName);
            }
            
            selector.setPackageName(packageName);
            writerProvider.setPackageName(packageName);
        }
        
        builder.filter(filter).selector(selector)
               .template(template).writerProvider(writerProvider);
 
        return builder.build();
    }
    
    static boolean isValidPackageName(String packageName) {
        if (!SourceVersion.isName(packageName)) return false;
        return true;
    }
}
