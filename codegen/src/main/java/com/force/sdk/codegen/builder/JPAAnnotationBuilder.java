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

package com.force.sdk.codegen.builder;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Builds JPA annotation {@code String}s that can be included in generated source code.
 * <p>
 * {@code JPAAnnotationBuilder} takes in {@code Annotation} classes and converts those
 * into {@code String}s that can be added to Java source code.    
 * <p>
 * Built annotations will be in the form:
 * <p>
 *   {@code @<annotationName>}
 *   
 * @author Tim Kral
 */
public class JPAAnnotationBuilder extends BaseBuilder<Class<? extends Annotation>> {
    
    /**
     * Initializes a {@code JPAAnnotationBuilder} with the number
     * of intentions that should proceed each annotation.
     * 
     * @param numIndents the number of indentations that should
     *                   proceed each annotation
     */
    public JPAAnnotationBuilder(int numIndents) {
        super(numIndents);
    }
    
    @Override
    void append(Class<? extends Annotation> annotation) {
        builderString.append('@');
        
      // javax.persistence annotations will have an import (see ForceJPAObject.st)
      // so these do not need to be fully qualified
      if (annotation.getPackage().getName().equals("javax.persistence")) {
          builderString.append(annotation.getSimpleName());
      } else {
          builderString.append(annotation.getName());
      }
    }
    
    /**
     * Adds an annotation with attributes to this {@code JPAAnnotationBuilder}.
     * <p>
     * Note that the attributes map has {@code String} values.  This method
     * will exactly copy the attribute map values into the annotation attribute
     * list.  Therefore callers must exactly write the attribute values as they
     * appear in {@code String} form.  For example, to add the attribute name="FooBar"
     * the caller would do the following:
     * <p>
     *   {@code map.put("name", "\"FooBar\"")}
     * <p>
     * Annotations with attributes will be built in the form:
     * <p>
     *   {@code @<annotationName>(<attr1>=<attr1Value>,<attr2>=<attr2Value>,...)}
     * 
     * @param annotation a subclass of {@code Annotation} that represents the annotation
     *                   that is to be added to this builder
     * @param attrs a {@code java.util.Map} of attribute keys and values in {@code String} form 
     */
    public void add(Class<? extends Annotation> annotation, Map<String, String> attrs) {
        add(annotation);
        
        // Add all of the annotation's attributes in key=value pairs
        if (attrs != null && !attrs.isEmpty()) {
            builderString.append('(');
            for (Map.Entry<String, String> attr : attrs.entrySet()) {
                builderString.append(attr.getKey()).append('=').append(attr.getValue()).append(',');
            }
            
            // Get rid of the last comma (',')
            builderString.deleteCharAt(builderString.lastIndexOf(","));
            builderString.append(')');
        }
    }
}
