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

package com.force.sdk.codegen.template;

import java.io.IOException;
import java.io.Writer;

/**
 * A representation of the physical layout of generated code.
 * <p>
 * A {@code Template} class defines what the generated code should look like.
 * It can accept injected state and use that state to dynamically generate
 * source code. 
 *
 * @author Tim Kral
 */
public interface Template {

    /**
     * Injects view state into a {@code Template}.
     * <p>
     * A {@code Template} should recognize view state
     * by a name.  Thereforce, this method will inject
     * the given view state object for the given name. 
     * 
     * @param viewName the name of the view state
     * @param viewState the view state object
     */
    void injectView(String viewName, Object viewState);
    
    /**
     * Removes all previously injected view state from a {@code Template}.
     */
    void reset();
    
    /**
     * Writes a {@code Template} along with any injected view state
     * to the given {@code Writer}.
     * 
     * @param writer a Java {@code Writer} to which the {@code Template}
     *               and its view state should be written
     * @throws IOException if there is an error writing to the Java {@code Writer}
     */
    void write(Writer writer) throws IOException;
}
