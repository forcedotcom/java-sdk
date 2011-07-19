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
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.force.sdk.codegen.filter.ObjectNameWithRefFilter;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.qa.util.PropsUtil;
import com.google.common.collect.ImmutableSet;
import com.sforce.ws.ConnectionException;

/**
 * Generates JPA Classes used in codegen FTests.
 * 
 * @author Tim Kral
 */
public final class JPATestClassGenerator {

    private static final String PACKAGE_NAME = "com.goldstandard.model";
    
    private JPATestClassGenerator() {  }
    
    /**
     * Main entry point for test class generator.
     * 
     * @param args No arguments required
     * @throws ConnectionException If an error occurs while getting a connection to
     *                             the Force.com service
     * @throws IOException If an i/o error occurs
     */
    public static void main(String[] args) throws ConnectionException, IOException {
        // If we're skipping the tests, don't bother generating any classes
        if (Boolean.valueOf(System.getProperty("skipTests"))) return;
        
        URL url = JPATestClassGenerator.class.getClassLoader().getResource("codegen-test.properties");
        InputStream is = url.openStream();
        
        try {
            Properties props = new Properties();
            props.load(is);
            
            ForceJPAClassGenerator generator = new ForceJPAClassGenerator();
            generator.setPackageName(PACKAGE_NAME);
            generator.setObjectFilter(new ObjectNameWithRefFilter(
                                        ImmutableSet.<String>of("Account", "ActivityHistory",
                                                                "Case", "CaseHistory", "Document",
                                                                "Folder", "LoginHistory")));
            
            
            // We'll generate the sources into the target directory
            // (as defined in pom.xml under build-helper-maven-plugin)
            String generatedFileDir = props.getProperty("project.root") + File.separator + "target"
                                        + File.separator + "generated-test-files";
            
            // Load the connection information from java-sdk-test.properties
            ForceServiceConnector connector = new ForceServiceConnector(PropsUtil.FORCE_SDK_TEST_NAME);
            
            generator.generateCode(connector.getConnection(), new File(generatedFileDir));
        } finally {
            is.close();
        }
    }

}
