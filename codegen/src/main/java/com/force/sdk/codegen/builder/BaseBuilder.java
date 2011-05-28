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

import static com.force.sdk.codegen.ForceJPAClassGeneratorUtils.INDENT;
import static com.force.sdk.codegen.ForceJPAClassGeneratorUtils.NEWLINE;

/**
 * A base class for code generation builders.
 * <p>
 * Builders will build {@code String}s that can be included in
 * generated source code.
 *
 * @param <T> the object type that is to be added to this builder
 *            (roughly, the object type to be converted into a {@code String}
 *            via this builder)
 * @author Tim Kral
 */
public abstract class BaseBuilder<T> {

    protected final StringBuffer builderString = new StringBuffer();
    private final int numIndents;
    
    BaseBuilder(int numIndents) {
        this.numIndents = numIndents;
    }
    
    /**
     * Adds an object to this builder.
     * <p>
     * Added objects will be converted into a {@code String} to be
     * used in generated code according to the internal rules
     * of the specific builder implementation. 
     * 
     * @param item the object to be added to this builder 
     */
    public void add(T item) {
        // Separate out multiple annotations with a newline
        if (builderString.length() > 0) {
            builderString.append(NEWLINE);
        }
        
        for (int i = 0; i < numIndents; i++) {
            builderString.append(INDENT);
        }
        
        append(item);
    }
    
    abstract void append(T item);
    
    @Override
    public String toString() {
        return this.builderString.toString();
    }
}
