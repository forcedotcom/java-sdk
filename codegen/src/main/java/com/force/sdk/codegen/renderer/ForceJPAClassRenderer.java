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

package com.force.sdk.codegen.renderer;

import java.util.Collections;

import javax.annotation.Generated;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.antlr.stringtemplate.AttributeRenderer;

import com.force.sdk.codegen.ForceJPAClassGenerator;
import com.force.sdk.codegen.ForceJPAClassGeneratorUtils;
import com.force.sdk.codegen.builder.JPAAnnotationBuilder;
import com.force.sdk.jpa.annotation.CustomObject;
import com.force.sdk.jpa.model.*;
import com.sforce.soap.partner.DescribeSObjectResult;

/**
 * A StringTemplate {@code AttributeRenderer} that renders Java class level {@code String}s.
 * <p>
 * This {@code AttributeRenderer} effectively translates between a Force.com {@code DescribeSObjectResult}
 * object and Java class level code.  Within StringTemplate, it is meant to be registered
 * as an {@code AttributeRenderer} for a Force.com {@code DescribeSObjectResult} object.
 *
 * @author Tim Kral
 */
public class ForceJPAClassRenderer implements AttributeRenderer {

    @Override
    public String toString(Object o) {
        return "";
    }
    
    @Override
    public String toString(Object o, String format) {
        DescribeSObjectResult dsr = (DescribeSObjectResult) o;
        if ("classAnnotation".equals(format)) {
            return renderClassAnnotation(dsr);
        } else if ("className".equals(format)) {
            return ForceJPAClassGeneratorUtils.renderJavaName(dsr, false /*firstCharLowerCase*/);
        } else if ("superClassName".equals(format)) {
            return renderSuperClassName(dsr);
        }
        
        return toString(o);
    }

    // Render annotations that are to be add to a JPA object
    // at the class level
    private String renderClassAnnotation(DescribeSObjectResult dsr) {
        JPAAnnotationBuilder builder = new JPAAnnotationBuilder(0 /*numIndents*/);
        builder.add(Generated.class,
                Collections.<String, String>singletonMap("value", "\"" + ForceJPAClassGenerator.class.getName() + "\""));
        builder.add(Table.class,
                Collections.<String, String>singletonMap("name", "\"" + dsr.getName() + "\""));
        builder.add(Entity.class,
                Collections.<String, String>singletonMap("name", "\"" + toString(dsr, "className") + "\""));
        
        // We're generating the class off of already existing schema
        // so there won't be a need to create it.
        builder.add(CustomObject.class,
                Collections.<String, String>singletonMap("readOnlySchema", "true"));
        
        return builder.toString();
    }
    
    private String renderSuperClassName(DescribeSObjectResult dsr) {
        if (dsr.isCustom()) return BaseForceCustomObject.class.getName();
        
        // If the standard object doesn't contain *all* the common
        // standard object fields just extend the generic base class (e.g. User, UserLicense)
        if (!ForceJPAClassGeneratorUtils.hasAllCommonFields(dsr)) return BaseForceObject.class.getName();
        return BaseForceStandardObject.class.getName();
    }
    
}
