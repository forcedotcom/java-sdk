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

package com.force.sdk.jpa.query.formatter;

import java.util.regex.Pattern;

/**
 * Convenience class to format a multipicklist value
 * i.e. 'AAA;BBB,CCC' will be formatted as 'AAA;BBB','CCC'
 * for use in the expression of a query.
 *
 * @author Fiaz Hossain
 */
public class MultiPicklistFormatter {

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    
    private String value;
    
    /**
     * Creates a formatter for the given string.
     * 
     * @param value  the string to be formatted.
     */
    public MultiPicklistFormatter(String value) {
        this.value = value;
    }
    
    /**
     * Formats the string.
     * 
     * @return a string that separates groups of picklist values by ','
     */
    public String getFormattedString() {
        if (value == null || value.length() == 0) return value;
        String[] values = COMMA_PATTERN.split(value);
        StringBuilder sb = new StringBuilder(value.length() + values.length * 4);
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("'").append(values[i]).append("'");
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getFormattedString();
    }
}
