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

package com.force.sdk.codegen.selector;

import com.force.sdk.codegen.filter.FieldFilter;
import com.force.sdk.codegen.template.Template;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.GetUserInfoResult;

/**
 * Selects data from a Force.com {@code GetUserInfoResult} object and a
 * Force.com {@code DescribeSObjectResult} object and injects it into a code generation 
 * {@link Template}.
 *
 * @author Tim Kral
 */
public interface DataSelector {

    /**
     * Selects data from a {@code GetUserInfoResult} object and {@code DescribeSObjectResult}
     * object and injects it into a {@code Template}.
     * 
     * @param userInfo the Force.com user who is running the code generation  
     * @param dsr the Force.com schema object for which code will be generated
     * @param fieldFilter the {@code FieldFilter} to run while generating code
     * @param template the {@code Template} object representing the physical 
     *                 layout of the code to be generated
     */
    void select(GetUserInfoResult userInfo, DescribeSObjectResult dsr, FieldFilter fieldFilter, Template template);
}
