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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.sforce.soap.partner.PicklistEntry;

/**
 * Builds Java enums from Force.com {@code PicklistEntry}s that can be included
 * in generated source code.
 * <p>
 * Force.com {@code PicklistEntry}s describe available values for a given Force.com
 * schema field.  Such values can be directly translated into Java enum values and
 * included in generated Java code.
 * <p>
 * Built Java enum values will be in the form:
 * <p>
 *   {@code ENUM_NAME(<active>,<defaultValue>,<label>,<value>),}
 *
 * @author Tim Kral
 */
public class ForcePicklistEnumBuilder extends BaseBuilder<PicklistEntry> {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+).*");

    private final Set<String> enumValueNameSet = new HashSet<String>();
    
    /**
     * Initializes a {@code ForcePicklistEnumBuilder} with the number
     * of intentions that should proceed each Java enum value.
     * 
     * @param numIndents the number of indentations that should
     *                   proceed each Java enum value
     */
    public ForcePicklistEnumBuilder(int numIndents) {
        super(numIndents);
    }
    
    @Override
    void append(PicklistEntry picklistEntry) {
        String label = picklistEntry.getLabel();
        String value = picklistEntry.getValue();
        
        // If the picklist value is only one character
        // try using the (hopefully) more descriptive label
        String enumValueName;
        if (value.length() == 1 && label != null) {
            enumValueName = label;
        } else {
            enumValueName = value;
        }
        
        // If the enum value name is only digits
        // prepend a string to make the name valid
        if (NUMBER_PATTERN.matcher(enumValueName).matches()) {
            enumValueName = "VALUE_" + enumValueName;
        }

        // Replace all non-alphanumeric characters with underscores ('_')
        enumValueName = enumValueName.replaceAll("\\W", "_").toUpperCase();
        
        // We have a duplicate name, so comment out the dupliate
        // (otherwise there's a Java compile error)
        if (enumValueNameSet.contains(enumValueName)) {
            builderString.append("//");
        } else {
            enumValueNameSet.add(enumValueName);
        }
        
        builderString.append(enumValueName);
        builderString.append('(');
        builderString.append(Boolean.toString(picklistEntry.getActive())).append(',');
        builderString.append(Boolean.toString(picklistEntry.getDefaultValue())).append(',');
        builderString.append(label == null ? "null" : "\"" + label + "\"").append(',');
        builderString.append("\"" + picklistEntry.getValue() + "\"");
        builderString.append("),");
    }
    
}
