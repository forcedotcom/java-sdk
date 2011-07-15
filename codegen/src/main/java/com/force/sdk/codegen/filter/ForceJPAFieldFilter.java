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

import static com.force.sdk.codegen.ForceJPAClassGeneratorUtils.ALL_OBJECT_COMMON_FIELDS;
import static com.force.sdk.codegen.ForceJPAClassGeneratorUtils.CUSTOM_OBJECT_COMMON_FIELDS;
import static com.force.sdk.codegen.ForceJPAClassGeneratorUtils.STANDARD_OBJECT_COMMON_FIELDS;
import static com.force.sdk.codegen.ForceJPAClassGeneratorUtils.renderJavaName;
import static com.force.sdk.codegen.ForceJPAClassGeneratorUtils.useRelationshipName;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.force.sdk.codegen.ForceJPAClassGeneratorUtils;
import com.google.common.collect.Lists;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

/**
 * A {@link FieldFilter} that filters fields to be included in Force.com
 * JPA Java classes. Specifically, this will filter out fields found in the
 * JPA base classes.
 * 
 * @author Tim Kral
 */
public class ForceJPAFieldFilter implements FieldFilter {

    @Override
    public List<Field> filter(DescribeSObjectResult dsr) {
        if (dsr.isCustom()) {
            // Skip all fields common to Force.com custom objects
            return filterInternal(dsr, CUSTOM_OBJECT_COMMON_FIELDS);
        } else if (ForceJPAClassGeneratorUtils.hasAllCommonFields(dsr)) {
            // Skip all fields common to Force.com standard objects
            return filterInternal(dsr, STANDARD_OBJECT_COMMON_FIELDS);
        }
        
        // Skip all fields common to all Force.com objects
        return filterInternal(dsr, ALL_OBJECT_COMMON_FIELDS);
    }
    
    private List<Field> filterInternal(DescribeSObjectResult dsr, Set<String> fieldsToSkip) {
        Set<String> javaFieldNames = new HashSet<String>();
        
        List<Field> fieldList = Lists.newArrayList(dsr.getFields());
        Iterator<Field> fieldIter = fieldList.iterator();
        while (fieldIter.hasNext()) {
            Field field = fieldIter.next();
            
            String fieldNameLower = field.getName().toLowerCase();
            String javaFieldName = renderJavaName(field, true /*firstCharLowerCase*/);
            
            // Skip over fields in the set
            if (fieldsToSkip.contains(fieldNameLower)) {
                fieldIter.remove();
            } else {
                // Check to see if we've already used this Java field name
                if (javaFieldNames.contains(javaFieldName)) {
                    // If there is a Java name conflict, we have a custom field or
                    // relationship name that conflicts with a standard field  
                    // or relationship name. We'll assume that the describe result
                    // orders standard fields before custom ones so below we are adjusting
                    // the custom field or relationship names.
                    
                    if (useRelationshipName(field)) {
                        javaFieldName = javaFieldName + "Relationship";
                        field.setRelationshipName(javaFieldName);
                    } else {
                        javaFieldName = javaFieldName + "Custom";
                        field.setName(javaFieldName);
                    }
                }
                
                javaFieldNames.add(javaFieldName);
            }
        }
        
        return fieldList;
    }

}
