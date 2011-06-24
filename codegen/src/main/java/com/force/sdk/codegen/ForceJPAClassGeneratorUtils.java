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

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.apache.commons.lang.WordUtils;

import com.force.sdk.jpa.table.ForceColumnMetaData;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sforce.soap.partner.*;

/**
 * Shared utilities for the Force.com JPA enabled Java class generator.
 *
 * @author Tim Kral
 */
public final class ForceJPAClassGeneratorUtils {
    
    /**
     * Represents a tab (consisting of spaces). 
     */
    public static final String INDENT = "    ";
    
    /**
     * Represents a system independent new line character.
     */
    public static final String NEWLINE = System.getProperty("line.separator");
    
    // The number of characters in a custom postfix (e.g. __c)
    private static final int CUSTOM_POSTFIX_LENGTH = 3;
    
    /**
     * The set of fields common to all Force.com objects. 
     */
    public static final Set<String> ALL_OBJECT_COMMON_FIELDS =
        ImmutableSet.<String>of("id");
    
    /**
     * The set of fields common to *most* Force.com standard objects.
     */
    public static final Set<String> STANDARD_OBJECT_COMMON_FIELDS =
        ImmutableSet.<String>copyOf(Sets.difference(ForceColumnMetaData.STANDARD_FIELDS,
                Sets.newHashSet("createdbyid", "lastmodifiedbyid", "isdeleted")));
    
    /**
     * The set of fields common to all Force.com custom objects.
     */
    public static final Set<String> CUSTOM_OBJECT_COMMON_FIELDS =
        ImmutableSet.<String>copyOf(Sets.difference(ForceColumnMetaData.STANDARD_FIELDS,
                Sets.newHashSet("createdbyid", "lastmodifiedbyid")));
    
    private ForceJPAClassGeneratorUtils() {  }
    
    /**
     * Constructs a Java package name based on a Force.com store name
     * (organization name).
     * 
     * @param userInfo the Force.com user who is running the code generation
     * @return a non {@code null} {@code String} which conforms
     *         to Java class naming standards
     */
    public static String constructPackageName(GetUserInfoResult userInfo) {
        String orgName = userInfo.getOrganizationName();
        
        if (orgName != null) {
            String orgNameDenorm = userInfo.getOrganizationName().replaceAll("(,|\\.|\\s)", "").toLowerCase();
            return "com." + orgNameDenorm + ".model";
        }
        
        return "com.force.model";
    }

    /**
     * Determines if a Force.com object has all standard Force.com fields.
     * 
     * @param dsr a Force.com {@code DescribeSObjectResult} of the object
     *            to be tested
     * @return {@code true} if and only if the given object contains all
     *         common Force.com fields
     * @see ForceJPAClassGeneratorUtils#STANDARD_OBJECT_COMMON_FIELDS
     */
    public static boolean hasAllCommonFields(DescribeSObjectResult dsr) {
        // Custom objects always have all common fields
        if (dsr.isCustom()) return true;
        
        // Gather up all the field names for this standard object
        Set<String> fieldNameSet = new HashSet<String>();
        for (Field field : dsr.getFields()) {
            fieldNameSet.add(field.getName().toLowerCase());
        }
        
        return fieldNameSet.containsAll(STANDARD_OBJECT_COMMON_FIELDS);
    }
    
    /**
     * Constructs a valid Java name from a Force.com {@code DescribeSObjectResult} object name.
     * 
     * @param dsr a Force.com {@code DescribeSObjectResult} object whose name is to
     *            to converted to a Java name
     * @param firstCharLowerCase indicates if the first character in the constructed Java
     *                           name should be lower case
     * @return a non {@code null} {@code String} which conforms
     *         to Java naming standards
     */
    public static String renderJavaName(DescribeSObjectResult dsr, boolean firstCharLowerCase) {
        return renderJavaName(dsr.getName(), "Entity", firstCharLowerCase);
    }

    /**
     * Determines if a Java name should be constructed from the given
     * Force.com API {@code Field} object's relationship name.
     * 
     * @param field the Force.com API {@code Field} object to test
     * @return {@code true} if and only if the given {@code Field}
     *         object is a reference field and has a non {@code null} relationship
     *         name and only references exactly one other Force.com object
     */
    public static boolean useRelationshipName(Field field) {
        return field.getType() == FieldType.reference && field.getRelationshipName() != null
               // With more than one reference, we'll revert back to the field name (and String type)
               && field.getReferenceTo().length == 1;
    }
    
    /**
     * Constructs a valid Java name from a Force.com API {@code Field} object name.
     * 
     * @param field a Force.com API {@code Field} object whose name is to
     *              to converted to a Java name
     * @param firstCharLowerCase indicates if the first character in the constructed Java
     *                           name should be lower case
     * @return a non {@code null} {@code String} which conforms
     *         to Java naming standards
     */
    public static String renderJavaName(Field field, boolean firstCharLowerCase) {
        if (useRelationshipName(field)) {
            return renderJavaName(field.getRelationshipName(), "Field", firstCharLowerCase);
        }
        
        return renderJavaName(field.getName(), "Field", firstCharLowerCase);
    }
    
    // Converts the given name into a suitable Java name
    private static String renderJavaName(String name, String keywordSuffix, boolean firstCharLowerCase) {
        // First, strip off any custom suffix
        if (name.endsWith("__c") || name.endsWith("__r")) {
            name = name.substring(0, name.length() - CUSTOM_POSTFIX_LENGTH);
        }

        // Convert to camelCase
        name = WordUtils.capitalize(name, new char[]{'_'});
        
        //Remove all underscores ('_')
        name = name.replace("_", "");
        
        // If the name we're going to generate is a Java keyword
        // then we'll tweak it with the suffix argument
        if (SourceVersion.isKeyword(name.toLowerCase())) {
            name = name + keywordSuffix;
        }
        
        // Enforce the desired case on the first character
        if (firstCharLowerCase) {
            name = WordUtils.uncapitalize(name);
        } else {
            name = WordUtils.capitalize(name);
        }
        
        return name;
    }
}
