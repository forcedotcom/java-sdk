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
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.SourceVersion;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.StringTemplateGroup;

import com.force.sdk.codegen.filter.FieldCombinationFilter;
import com.force.sdk.codegen.filter.FieldFilter;
import com.force.sdk.codegen.filter.ForceJPAFieldFilter;
import com.force.sdk.codegen.filter.ObjectFilter;
import com.force.sdk.codegen.filter.ObjectNoOpFilter;
import com.force.sdk.codegen.injector.ForceJPAClassTemplateInjector;
import com.force.sdk.codegen.injector.TemplateInjector;
import com.force.sdk.codegen.renderer.ForceJPAClassRenderer;
import com.force.sdk.codegen.renderer.ForceJPAFieldRenderer;
import com.force.sdk.codegen.template.StringTemplateWrapper;
import com.force.sdk.codegen.template.Template;
import com.force.sdk.codegen.writer.ForceJPAFileWriterProvider;
import com.force.sdk.codegen.writer.WriterProvider;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

/**
 * Generator for Force.com JPA enabled Java classes.
 * <p>
 * This will generate a closed set of Force.com JPA enabled Java classes based on a list of
 * Force.com schema object (SObject) names from a Force.com store (organization).  For example, 
 * a caller wishing to generate a Java class for the Account object will get the Account Java 
 * class plus all the Java classes to which the Account class refers (i.e. the Account 
 * object's references).  If the first name in the list of schema object names is a single star 
 * ("*") then the generator will produce Java classes for all known objects in the Force.com
 * store (organization).
 * <p>
 * A {@code ForceJPAClassGenerator} has a predetermined set of Force.com schema objects that
 * will always be generated in addition to what is requested by the caller.
 *
 * @author Tim Kral
 */
public class ForceJPAClassGenerator extends AbstractCodeGenerator {

    // The renderers for Force.com JPA object generation
    private static final Map<Class<?>, AttributeRenderer> RENDERER_MAP =
        new HashMap<Class<?>, AttributeRenderer>(2);
    
    static {
        RENDERER_MAP.put(DescribeSObjectResult.class, new ForceJPAClassRenderer());
        RENDERER_MAP.put(Field.class, new ForceJPAFieldRenderer());
    }
    
    // Allow a static package name (as opposed to a dynamically
    // generated package name)
    private String packageName;
    
    // Allow the caller to specify an ObjectFilter
    private ObjectFilter objectFilter;

    // Allow the caller to specify a FieldFilter
    private FieldFilter fieldFilter;
    
    /**
     * Sets the Java package name under which the Java classes
     * will be generated.
     * 
     * @param packageName a non {@code null} {@code String} which conforms
     *                    to Java package naming standards
     */
    public void setPackageName(String packageName) {
        if (packageName != null) {
            validatePackageName(packageName);
        }
        
        this.packageName = packageName;
    }

    @Override
    public final ObjectFilter getObjectFilter() {
        if (objectFilter != null) return objectFilter;
        return new ObjectNoOpFilter();
    }
    
    public final void setObjectFilter(ObjectFilter objectFilter) {
        this.objectFilter = objectFilter;
    }
    
    @Override
    public final FieldFilter getFieldFilter() {
        // If the caller has specified a field filter
        // then ensure we always run a ForceJPAFieldFilter
        // after it.
        if (fieldFilter != null) {
            return new FieldCombinationFilter()
                        .addFilter(fieldFilter)
                        .addFilter(new ForceJPAFieldFilter());
        }
        
        return new ForceJPAFieldFilter();
    }
    
    public final void setFieldFilter(FieldFilter fieldFilter) {
        this.fieldFilter = fieldFilter;
    }
    
    @Override
    protected final Template getTemplate() {
        StringTemplateWrapper template =
            new StringTemplateWrapper(new StringTemplateGroup("JPA").getInstanceOf("templates/ForceJPAClass"));
        template.setAttributeRenderers(RENDERER_MAP);
        
        return template;
    }
    
    @Override
    protected final TemplateInjector getTemplateInjector() {
        ForceJPAClassTemplateInjector templateInjector = new ForceJPAClassTemplateInjector();
        
        if (packageName != null) {
            templateInjector.setPackageName(packageName);
        }
        
        return templateInjector;
    }
    
    @Override
    protected final WriterProvider getWriterProvider(File destDir) {
        ForceJPAFileWriterProvider writerProvider = new ForceJPAFileWriterProvider(destDir);
        
        if (packageName != null) {
            writerProvider.setPackageName(packageName);
        }
        
        return writerProvider;
    }
    
    static void validatePackageName(String packageName) {
        if (!SourceVersion.isName(packageName)) {
            throw new IllegalArgumentException("Invalid package name: " + packageName);
        }
    }
}
