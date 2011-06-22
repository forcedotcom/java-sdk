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

package com.force.sdk.jpa.query;

import java.util.Collections;
import java.util.List;

/**
 * Convenience class to maintain short and long names for a tuple i.e. c as short and a.b.c
 * as long name.
 *
 * @author Fiaz Hossain
 */
public class TupleName {

    List<String> tuple;
    
    /**
     * Creates a tuple name out of a list.
     * 
     * @param tuple  A list of strings that make up the name
     */
    public TupleName(List<String> tuple) {
        this.tuple = tuple;
    }
    
    /**
     * Creates a tuple name from just one string.
     * 
     * @param name  the name of the tuple
     */
    public TupleName(String name) {
        this.tuple = Collections.singletonList(name);
    }
    
    public List<String> getTuple() {
        return tuple;
    }
    
    /**
     * Returns tuple short name.
     * 
     * @return tuple short name
     */
    public String getShortName() {
        return tuple.get(tuple.size() - 1);
    }
    
    /**
     * Returns the prefix that when combined with the tuple
     * short name creates the tuple long name.
     * <p>
     * For example, If tuple long name is a.b.c, the short name prefix is a.b
     * 
     * @return short name prefix
     */
    public String getShortNamePrefix() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tuple.size() - 1; i++) {
            if (sb.length() > 0) sb.append(".");
            sb.append(tuple.get(i));
        }
        return sb.toString();
    }
    
    /**
     * Returns the tuple long name.
     * 
     * @return fully qualified tuple name
     */
    public String getLongName() {
        StringBuilder sb = new StringBuilder();
        for (String s : tuple) {
            if (sb.length() > 0) sb.append(".");
            sb.append(s);
        }
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        return getShortName().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof TupleName)  {
            TupleName other = (TupleName) obj;
            return tuple.get(tuple.size() - 1).equals(other.tuple.get(other.tuple.size() - 1));
        }
        return false;
    }
    
    @Override
    public String toString() {
        return getLongName();
    }
}
