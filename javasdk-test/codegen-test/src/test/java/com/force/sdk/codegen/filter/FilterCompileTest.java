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

package com.force.sdk.codegen.filter;

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.tools.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.codegen.ForceJPAClassGenerator;
import com.force.sdk.codegen.JPATestClassGenerator;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.qa.util.PropsUtil;
import com.force.sdk.qa.util.TestContext;
import com.sforce.ws.ConnectionException;

/**
 * 
 * These tests verify that entities generated from different filters and filter
 * combinations can compile successfully.
 *
 * @author Jeff Lai
 * 
 */
public class FilterCompileTest {
    
    private ForceServiceConnector connector;
    private ForceJPAClassGenerator gen;
    private ObjectCombinationFilter objectFilter;
    private FieldCombinationFilter fieldFilter;
    private Set<String> includes = new HashSet<String>(Arrays.asList(new String[] {"Account", "Contact"}));
    private Set<String> excludes = new HashSet<String>(Arrays.asList(new String[] {"Lead", "Case"}));
    int totalNumSObjects;
    
    @BeforeClass
    public void classSetup() throws IOException, ConnectionException {
        // load code-get test props into TestContext
        URL url = JPATestClassGenerator.class.getClassLoader().getResource("codegen-test.properties");
        InputStream is = null;
        try {
            is = url.openStream();
            Properties props = new Properties();
            props.load(is);
            TestContext.get().setTestProps(props);
        } finally {
            if (is != null) is.close();
        }
        // create test force connector
        connector = new ForceServiceConnector(PropsUtil.loadTestConnectionUrl());
        totalNumSObjects = connector.getConnection().describeGlobal().getSobjects().length;
    }
    
    @DataProvider(name = "generators")
    public Object[][] generators() {
        // Object[0] is ForceJPAClassGenerator 
        // Object[1] is String name for generator, also used as part of path for output files
        // Object[2] is expected number of generated files
        List<Object[]> gensWithNames = new ArrayList<Object[]>();
        
        // No-Op Filter
        initializeGenAndFilters();
        gen.setObjectFilter(new ObjectNoOpFilter());
        gensWithNames.add(new Object[] {gen, "no-op", totalNumSObjects});
        
        // includes ObjectNameWithRefFilter
        initializeGenAndFilters();
        objectFilter.addFilter(new ObjectNameWithRefFilter(includes));
        gen.setObjectFilter(objectFilter);
        gensWithNames.add(new Object[] {gen, "incl-name-with-ref", 9});
        
        // includes ObjectNameFilter, FieldReferenceFilter
        initializeGenAndFilters();
        objectFilter.addFilter(new ObjectNameFilter(true, includes));
        fieldFilter.addFilter(new FieldReferenceFilter(true, includes));
        gen.setObjectFilter(objectFilter);
        gen.setFieldFilter(fieldFilter);
        gensWithNames.add(new Object[] {gen, "incl-name-and-ref", 2});
        
        // excludes ObjectNameFilter, FieldReferenceFilter
        initializeGenAndFilters();
        objectFilter.addFilter(new ObjectNameFilter(false, excludes));
        fieldFilter.addFilter(new FieldReferenceFilter(false, excludes));
        gen.setObjectFilter(objectFilter);
        gen.setFieldFilter(fieldFilter);
        gensWithNames.add(new Object[] {gen, "excl-name-and-ref", totalNumSObjects - 2});
        
        // includes ObjectNameWithRefFilter; excludes ObjectNameFilter, FieldReferenceFilter
        initializeGenAndFilters();
        objectFilter.addFilter(new ObjectNameWithRefFilter(includes));
        objectFilter.addFilter(new ObjectNameFilter(false, excludes));
        fieldFilter.addFilter(new FieldReferenceFilter(false, excludes));
        gen.setObjectFilter(objectFilter);
        gen.setFieldFilter(fieldFilter);
        gensWithNames.add(new Object[] {gen, "incl-name-with-ref-excl-name-and-ref", 9});
        
        // includes ObjectNameFilter, FieldReferenceFilter; excludes ObjectNameFilter, FieldReferenceFilter
        initializeGenAndFilters();
        objectFilter.addFilter(new ObjectNameFilter(true, includes));
        fieldFilter.addFilter(new FieldReferenceFilter(true, includes));
        objectFilter.addFilter(new ObjectNameFilter(false, excludes));
        fieldFilter.addFilter(new FieldReferenceFilter(false, excludes));
        gensWithNames.add(new Object[] {gen, "incl-name-and-ref-excl-name-and-ref", totalNumSObjects});
        
        // convert list to Object[][] which is the required return type for DataProvider
        return gensWithNames.toArray(new Object[gensWithNames.size()][]);
    }
    
    @SuppressWarnings("unchecked")
    @Test(dataProvider = "generators")
    public void testCompile(ForceJPAClassGenerator generator, String genName, int expectedNumFiles)
        throws ConnectionException, IOException {
        
        String generatedDirPath = TestContext.get().getTestProps().getProperty("project.root")
            + File.separator + "target" + File.separator + "compile-test" + File.separator + genName;
        File generatedDir = new File(generatedDirPath);
        String generatedBinDirPath = generatedDirPath + File.separator + "bin";
        // clean target directory for generated files if it already exists
        if (generatedDir.exists()) FileUtils.deleteDirectory(generatedDir);
        FileUtils.forceMkdir(new File(generatedBinDirPath));
        
        // generate entities
        generator.setPackageName("com");
        int actualNumFiles = generator.generateCode(connector.getConnection(), generatedDir);
        Assert.assertEquals(actualNumFiles, expectedNumFiles, "Unexpected number of generated files for "
                + genName + " test case");
        
        // compile generated entities
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Collection<File> generatedFiles = FileUtils.listFiles(new File(generatedDirPath + File.separator + "com"),
                new RegexFileFilter("^.*\\.java$"), null);
        Iterable generatedFilesIterable = fileManager.getJavaFileObjectsFromFiles(generatedFiles);
        String[] options = new String[] {"-d", generatedBinDirPath};
        boolean compileSuccess = compiler.getTask(null, null, null, Arrays.asList(options),
                null, generatedFilesIterable).call();
        fileManager.close();
        Assert.assertTrue(compileSuccess, "There was an error compiling generated entities from the "
                + genName + " test case");
   }
    
    private void initializeGenAndFilters() {
        gen = new ForceJPAClassGenerator();
        objectFilter = new ObjectCombinationFilter();
        fieldFilter = new FieldCombinationFilter();
    }

}
