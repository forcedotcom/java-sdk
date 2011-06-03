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

package com.force.sdk.codegen.writer;

import java.io.*;

import com.force.sdk.codegen.ForceJPAClassGeneratorUtils;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.GetUserInfoResult;

/**
 * Provides FileWriters for Force.com JPA Java class files.
 *
 * @author Tim Kral
 */
/**
 * A {@link WriterProvider} which provides {@code FileWriter}s for Force.com JPA
 * enabled Java classes.
 * <p>
 * A {@code ForceJPAFileWriterProvider} cares about three pieces of state:
 * <p>
 * <ol>
 *   <li>A destination (project) directory</li>
 *   <li>A Java package name</li>
 *   <li>A Java class name</li>
 * </ol>
 * For example, if the destination directory is /home/tkral, the Java package name
 * is com.force and the class name is TestClass, a {@code ForceJPAFileWriterProvider} 
 * will provide a {@code FileWriter} to the following file:
 * <p>
 *   {@code /home/tkral/com/force/TestClass.java}
 * A Java package name need not be specified explicitly.  If no package name is provided,
 * {@code ForceJPAFileWriterProvider} will create a package name based off of the callers
 * Force.com store name (i.e. Organization name).  Similarly, a Java class name need not
 * be specified explicitly.  If missing, {@code ForceJPAFileWriterProvider} will use
 * the name from a Force.com {@code DescribeSObjectResult} object. 
 * 
 * @author Tim Kral
 */
public class ForceJPAFileWriterProvider implements WriterProvider {

    private final File destDir;  // Root destination directory for FileWriter
    
    // Allow a static package name (as opposed to a dynamically
    // generated package name)
    private String packageName = null;
    private String className = null;  // Common class name for all files
    
    /**
     * Initalizes a {@code ForceJPAFileWriterProvider} to provide {@code FileWriter}s
     * to the given destination (project) directory.
     * 
     * @param destDir a Java {@code File} which represents a root project directory
     */
    public ForceJPAFileWriterProvider(File destDir) {
        this.destDir = destDir;
    }
    
    /**
     * Sets the Java class name of the generate Java classes.
     * <p>
     * If no class name is specified, one will be created based
     * off of a Force.com {@code DescribeSObjectResult} object.
     * 
     * @param className a non {@code null} {@code String} which conforms
     *                  to Java class naming standards
     */
    public void setClassName(String className) {
        this.className = className;
    }
    
    /**
     * Sets the Java package name under which the generated Java
     * classes will be written.
     * <p>
     * If no package name is specified, one will be created based
     * off of the callers Force.com store name (i.e. Organization name).
     * 
     * @param packageName a non {@code null} {@code String} which conforms
     *                    to Java package naming standards
     * @see ForceJPAClassGeneratorUtils#constructPackageName(GetUserInfoResult)
     * @see com.force.sdk.codegen.selector.ForceJPAClassDataSelector#setPackageName(String)
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    @Override
    public Writer getWriter(GetUserInfoResult userInfo, DescribeSObjectResult dsr) throws IOException {
        String javaPackageName;
        if (this.packageName != null) {
            javaPackageName = this.packageName;
        } else {
            javaPackageName = ForceJPAClassGeneratorUtils.constructPackageName(userInfo);
        }
        
        String javaClassName;
        if (this.className != null) {
            javaClassName = this.className;
        } else {
            javaClassName = ForceJPAClassGeneratorUtils.renderJavaName(dsr, false /*firstCharLowerCase*/);
        }
        
        return createSourceFileWriter(javaPackageName, javaClassName);
    }
    
    private FileWriter createSourceFileWriter(String javaPackageName, String javaClassName) throws IOException {
        
        if (destDir == null)
            throw new IllegalStateException("Cannot construct Java class file with null destination directory");
        
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        File sourceFileDestDir = new File(destDir.getAbsolutePath() + "/" + javaPackageName.replace('.', '/'));
        if (!sourceFileDestDir.exists()) {
            sourceFileDestDir.mkdirs();
        }
        
        File sourceFile = new File(sourceFileDestDir.getAbsolutePath() + "/" + javaClassName + ".java");
        if (!sourceFile.exists()) {
            sourceFile.createNewFile();
        }
        
        return new FileWriter(sourceFile);
    }
}
