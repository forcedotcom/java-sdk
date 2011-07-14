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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.force.sdk.codegen.filter.DataFilter;
import com.force.sdk.codegen.selector.DataSelector;
import com.force.sdk.codegen.template.Template;
import com.force.sdk.codegen.writer.WriterProvider;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

/**
 * Abstract Code Generator.
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
public abstract class AbstractCodeGenerator implements CodeGenerator {
    
    private static final int MAX_BATCH_DESCRIBE_SIZE = 100;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final int generateCode(PartnerConnection conn, File destDir) throws ConnectionException, IOException {
        
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
        
        DataFilter filter = getFilter();
        assert filter != null;
        
        Template template = getTemplate();
        assert template != null;
        
        DataSelector selector = getSelector();
        assert selector != null;
        
        WriterProvider writerProvider = getWriterProvider(destDir);
        assert writerProvider != null;
        
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
    
    protected abstract DataFilter getFilter();
    protected abstract DataSelector getSelector();
    protected abstract Template getTemplate();
    protected abstract WriterProvider getWriterProvider(File destDir);
    
}
