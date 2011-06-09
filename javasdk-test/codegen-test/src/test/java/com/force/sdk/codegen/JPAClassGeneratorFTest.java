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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.qa.util.PropsUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.sforce.ws.ConnectionException;

/**
 * Functional tests for JPACodeGenerator.
 *
 * @author Tim Kral
 */
public class JPAClassGeneratorFTest {

    private static final String TMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "JPAClassGeneratorFTest";
    
    @AfterClass(alwaysRun = true)
    public void classTearDown() throws IOException {
        FileUtils.deleteDirectory(new File(TMP_DIR));
    }

    @DataProvider
    public Object[][] customAndStandardObjectsWithCounts() {
        Set<String> standardClasses = ImmutableSet.<String>of("Account.java", "CallCenter.java"
                , "Contact.java", "Group.java", "Organization.java", "Profile.java", "User.java"
                , "UserLicense.java", "UserRole.java");

        return new Object[][] {
                { Lists.newArrayList("Opportunity"), ImmutableSet.<String>of(
                        "Account.java", "BusinessProcess.java", "CallCenter.java", "Campaign.java", "Contact.java"
                      , "Group.java", "Opportunity.java", "Organization.java", "Pricebook2.java", "Profile.java"
                      , "RecordType.java", "User.java", "UserLicense.java", "UserRole.java") }
              , { Lists.newArrayList(""), standardClasses }
              , { Lists.newArrayList("User"), standardClasses }
              , { new ArrayList<String>(ForceJPAClassGenerator.STANDARD_OBJECTS), standardClasses }
              , { Lists.<String>newArrayList((String) null), standardClasses }
        };
    }

    @Test(dataProvider = "customAndStandardObjectsWithCounts")
    public void testGenerateClasses(List<String> objectNames, Set<String> expectedFileNames)
    throws ConnectionException, IOException {
        try {
            ForceJPAClassGenerator generator = new ForceJPAClassGenerator(PropsUtil.FORCE_SDK_TEST_NAME, new File(TMP_DIR));
            generator.setPackageName("com.ftest.model");

            int classCount = generator.generateJPAClasses(objectNames);
            int expectedCount = expectedFileNames.size();

            File[] actualFiles =
                new File(TMP_DIR + File.separator + "com" + File.separator + "ftest" + File.separator + "model")
                    .listFiles();
            int fileCount = actualFiles.length;

            Assert.assertEquals(classCount, expectedCount, "Unexpected number of generated classes.");
            Assert.assertEquals(fileCount, expectedCount, "Unexpected number of generated files.");
            assertGeneratedFileNamesMatch(actualFiles, expectedFileNames);
        } finally {
            FileUtils.deleteDirectory(new File(TMP_DIR));
        }
    }

    private void assertGeneratedFileNamesMatch(File[] files, Set<String> fileNames) {
        Assert.assertEquals(files.length, fileNames.size(), "Mismatched number of fileNames for files");

        for (int i = 0; i < files.length; i++) {
            Assert.assertTrue(fileNames.contains(files[i].getName()), "Unexpected filename: " + files[i]);
        }
    }
    
    @Test
    public void testGenerateClassesWithNullObjectNames() throws ConnectionException, IOException {
        try {
            ForceJPAClassGenerator generator = new ForceJPAClassGenerator(PropsUtil.FORCE_SDK_TEST_NAME, new File(TMP_DIR));
            generator.setPackageName("com.failure.model");
            
            generator.generateJPAClasses(null);
            fail("Generate JPA Classes should have failed because we supplyed a null object names list");
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "Object name list is null", "Unexpected error message");
        } finally {
            FileUtils.deleteDirectory(new File(TMP_DIR));
        }
    }
}
