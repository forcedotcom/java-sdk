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

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import com.force.sdk.codegen.filter.DataFilter;
import com.force.sdk.codegen.filter.NoOpDataFilter;
import com.force.sdk.codegen.selector.DataSelector;
import com.force.sdk.codegen.selector.DefaultDataSelector;
import com.force.sdk.codegen.template.Template;
import com.force.sdk.codegen.writer.WriterProvider;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectionException;

/**
 * Basic Code Generator.
 * <p>
 * The generator will load up all Force.com schema object (SObject) information 
 * for a given Force.com store (organization). It then filters and selects
 * the data that is necessary for code generation and writes
 * this to a {@code Template}.
 * <p>
 * Notice that code generation is analogous to MVC (well, at
 * least the MV parts): 
 * <p>
 * <ol>
 *   <li>
 *    Model = {@code DataFilter}, {@code DataSelector}. This provides the data that will
 *    be used in the template.
 *    </li>
 *    <li>
 *    View = {@code Template}.  This represents the physical layout of
 *    the code being generated.
 *    </li>
 *    <li>
 *    A {@code WriterProvider} provides Java {@code Writer}s to the {@code CodeGenerator}
 *    to tell it where to write the generated code. 
 *    </li>
 * </ol>
 *
 * @author Tim Kral
 */
public final class CodeGenerator {

    private static final int MAX_BATCH_DESCRIBE_SIZE = 100;
    
    final DataFilter filter;
    final DataSelector selector;
    final Template template;
    final WriterProvider writerProvider;
    
    private CodeGenerator(Builder b) {
        this.filter = b.filter == null ? new NoOpDataFilter() : b.filter;
        this.selector = b.selector == null ? new DefaultDataSelector() : b.selector;
        
        if (b.template == null)
            throw new IllegalArgumentException("Cannot build a CodeGenerator with a null template");
        this.template = b.template;
        
        if (b.writerProvider == null)
            throw new IllegalArgumentException("Cannot build a CodeGenerator with a null writer provider");
        this.writerProvider = b.writerProvider;
    }
    
    /**
     * Generates code based on a connection to a Force.com store (organization).
     * <p>
     * This will make various Force.com API calls to a Force.com store.  First,
     * it will run a global describe.  Next, it will run full describes on the
     * global describe results.  These calls will be batched in the most efficient
     * way possible (that is, making the least number of Force.com API calls).
     * <p>
     * The full describe results will be filtered and injected into a {@code Template}
     * whose contents will then be written to a Java {@code Writer}.
     * 
     * @param conn a Force.com API connection to the Force.com store (organization)
     *             that is to be described for code generation
     * @return the number of Force.com objects for which code is generated
     * @throws ConnectionException if an error occurs while connecting to the Force.com
     *                             store (organization)
     * @throws IOException if an error occurs while writing the generated code
     */
    public int generateCode(PartnerConnection conn) throws ConnectionException, IOException {
        
        // Get all known SObjects in the organization
        List<String> allOrgObjectNames = new ArrayList<String>();
        for (DescribeGlobalSObjectResult dgsr : conn.describeGlobal().getSobjects()) {
            allOrgObjectNames.add(dgsr.getName());
        }
        
        // Describe all known SObjects in the organization.  Batch by the appropriate size.
        // TODO: Is this ok to read entirely into memory?
        List<DescribeSObjectResult> allOrgObjects = new ArrayList<DescribeSObjectResult>();
        for (int i = 0; i < allOrgObjectNames.size(); i += MAX_BATCH_DESCRIBE_SIZE) {
            // Ensure that our range only goes to the end of the allOrgObjectNames array
            int endIndex = i + MAX_BATCH_DESCRIBE_SIZE;
            if (endIndex > allOrgObjectNames.size()) endIndex = allOrgObjectNames.size();
            
            List<String> objectNameBatch = allOrgObjectNames.subList(i, endIndex);
            DescribeSObjectResult[] dsrs =
                conn.describeSObjects(objectNameBatch.toArray(new String[objectNameBatch.size()]));
            
            allOrgObjects.addAll(Arrays.<DescribeSObjectResult>asList(dsrs));
        }
        
        // Get the user information
        GetUserInfoResult userInfo = conn.getUserInfo();
        
        // Write filtered data to the template
        int numGeneratedCode = 0;
        for (DescribeSObjectResult dsr : filter.filter(allOrgObjects)) {
            // Before we write a new source file, make sure the template is reset
            template.reset();
            
            // Select the data that we're interested in 
            selector.select(userInfo, dsr, template);
            
            Writer writer = null;
            try {
                writer = writerProvider.getWriter(userInfo, dsr);
                template.write(writer);
                numGeneratedCode++;
            } finally {
                if (writer != null) writer.close();
            }
        }
        
        return numGeneratedCode;
    }
    
    /**
     * Builder pattern for {@code CodeGenerator}.
     */
    public static class Builder {
        DataFilter filter;
        DataSelector selector;
        Template template;
        WriterProvider writerProvider;
        
        /**
         * Injects a {@code DataFilter} into this {@code Builder}.
         * <p>
         * When this {@code Builder} builds a {@code CodeGenerator} object, 
         * that object will use the given {@code DataFilter}.
         * 
         * @param filter the {@code DataFilter} to be used in the built
         *               {@code CodeGenerator} object
         * @return instance of this {@code Builder}
         */
        public Builder filter(DataFilter filter) {
            this.filter = filter;
            return this;
        }
        
        /**
         * Injects a {@code DataSelector} into this {@code Builder}.
         * <p>
         * When this {@code Builder} builds a {@code CodeGenerator} object, 
         * that object will use the given {@code DataSelector}.
         * 
         * @param selector the {@code DataSelector} to be used in the built
         *                 {@code CodeGenerator} object
         * @return instance of this {@code Builder}
         */
        public Builder selector(DataSelector selector) {
            this.selector = selector;
            return this;
        }

        /**
         * Injects a {@code Template} into this {@code Builder}.
         * <p>
         * When this {@code Builder} builds a {@code CodeGenerator} object, 
         * that object will use the given {@code Template}.
         * 
         * @param template the {@code Template} to be used in the built
         *                 {@code CodeGenerator} object
         * @return instance of this {@code Builder}
         */
        public Builder template(Template template) {
            this.template = template;
            return this;
        }
        
        /**
         * Injects a {@code WriterProvider} into this {@code Builder}.
         * <p>
         * When this {@code Builder} builds a {@code CodeGenerator} object, 
         * that object will use the given {@code WriterProvider}.
         * 
         * @param writerProvider the {@code WriterProvider} to be used in the built
         *                       {@code CodeGenerator} object
         * @return instance of this {@code Builder}
         */
        public Builder writerProvider(WriterProvider writerProvider) {
            this.writerProvider = writerProvider;
            return this;
        }
        
        /**
         * Builds a {@code CodeGenerator} object.
         * 
         * @return a {@code CodeGenerator} object with the state
         *         contained in this {@code Builder}
         */
        public CodeGenerator build() {
            return new CodeGenerator(this);
        }
    }
}
