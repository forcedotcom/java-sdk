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

import static org.testng.Assert.*;

import java.io.StringWriter;

import org.testng.annotations.Test;

import com.force.sdk.codegen.filter.NoOpDataFilter;
import com.force.sdk.codegen.selector.DefaultDataSelector;
import com.force.sdk.codegen.template.StringTemplateWrapper;
import com.force.sdk.codegen.writer.BasicWriterProvider;

/**
 * Unit tests for {@link CodeGenerator}.
 *
 * @author Tim Kral
 */
public class CodeGeneratorTest {

    @Test
    public void testFilterDefaultsToNoOpFilter() {
        CodeGenerator.Builder builder = new CodeGenerator.Builder();
        builder.selector(new DefaultDataSelector())
               .template(new StringTemplateWrapper())
               .writerProvider(new BasicWriterProvider(new StringWriter()));
        
        CodeGenerator generator = builder.build();
        assertNotNull(generator.filter, "CodeGenerator filter should default when not specified");
        assertEquals(generator.filter.getClass(), NoOpDataFilter.class,
                "CodeGenerator filter should default to NoOpFilter");
    }
    
    @Test
    public void testSelectorDefaultsToDefaultDataSelector() {
        CodeGenerator.Builder builder = new CodeGenerator.Builder();
        builder.filter(new NoOpDataFilter())
               .template(new StringTemplateWrapper())
               .writerProvider(new BasicWriterProvider(new StringWriter()));
        
        CodeGenerator generator = builder.build();
        assertNotNull(generator.selector, "CodeGenerator selector should default when not specified");
        assertEquals(generator.selector.getClass(), DefaultDataSelector.class,
                "CodeGenerator selector should default to DefaultDataSelector");
    }
    
    @Test
    public void testTemplateIsRequired() {
        CodeGenerator.Builder builder = new CodeGenerator.Builder();
        builder.filter(new NoOpDataFilter())
               .selector(new DefaultDataSelector())
               .writerProvider(new BasicWriterProvider(null));
        
        try {
            builder.build();
            fail("CodeGenerator build should have failed because there is no template specified");
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "Cannot build a CodeGenerator with a null template",
                "Unexpected CodeGenerator.Builder error messsage.");
        }
    }
    
    @Test
    public void testWriterProviderIsRequired() {
        CodeGenerator.Builder builder = new CodeGenerator.Builder();
        builder.filter(new NoOpDataFilter())
               .selector(new DefaultDataSelector())
               .template(new StringTemplateWrapper());
        
        try {
            builder.build();
            fail("CodeGenerator build should have failed because there is no writer provider specified");
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "Cannot build a CodeGenerator with a null writer provider",
                "Unexpected CodeGenerator.Builder error messsage.");
        }
    }
    
}
