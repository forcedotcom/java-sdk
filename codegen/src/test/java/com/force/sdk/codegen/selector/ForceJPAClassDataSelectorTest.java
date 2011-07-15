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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.force.sdk.codegen.filter.FieldNoOpFilter;
import com.force.sdk.codegen.template.StringTemplateWrapper;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.GetUserInfoResult;

/**
 * Unit tests for {@link ForceJPAClassDataSelector}.
 *
 * @author Tim Kral
 */
public class ForceJPAClassDataSelectorTest {
    
    @Test
    public void testBasicSelect() {
        GetUserInfoResult userInfo = new GetUserInfoResult();
        userInfo.setOrganizationName("testBasicSelect UserInfo");
        
        DescribeSObjectResult dsr = new DescribeSObjectResult();
        dsr.setName("testBasicSelect DescribeSObjectResult");
        
        StringTemplateWrapper template = new StringTemplateWrapper("$packageName$ $userInfo.organizationName$ $objectInfo.name$");
        new ForceJPAClassDataSelector().select(userInfo, dsr, new FieldNoOpFilter(), template);
        
        assertEquals(template.toString(),
                "com.testbasicselectuserinfo.model testBasicSelect UserInfo testBasicSelect DescribeSObjectResult",
                "Unexpected template after Force.com JPA class data select");
    }
    
    @Test
    public void testStaticPackageName() {
        GetUserInfoResult userInfo = new GetUserInfoResult();
        userInfo.setOrganizationName("testStaticPackageName UserInfo");
        
        StringTemplateWrapper template = new StringTemplateWrapper("$packageName$");
        ForceJPAClassDataSelector selector = new ForceJPAClassDataSelector();
        selector.setPackageName("com.staticpackage.model");
        
        selector.select(userInfo, new DescribeSObjectResult(), new FieldNoOpFilter(), template);
        
        assertEquals(template.toString(), "com.staticpackage.model",
                "Unexpected template after Force.com JPA class data select");
    }
    
}
