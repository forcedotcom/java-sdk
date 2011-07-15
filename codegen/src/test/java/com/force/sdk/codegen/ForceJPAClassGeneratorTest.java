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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.codegen.filter.FieldCombinationFilter;
import com.force.sdk.codegen.filter.FieldFilter;
import com.force.sdk.codegen.filter.FieldNoOpFilter;
import com.force.sdk.codegen.filter.ForceJPAFieldFilter;
import com.force.sdk.codegen.filter.ObjectFilter;
import com.force.sdk.codegen.filter.ObjectNoOpFilter;
import com.sforce.ws.ConnectionException;

/**
 * Unit tests for {@link ForceJPAClassGenerator}.
 *
 * @author Tim Kral
 */
public class ForceJPAClassGeneratorTest {

    @Test
    public void testObjectFilterDefaultsToNoOpFilter() {
        ForceJPAClassGenerator generator = new ForceJPAClassGenerator();
        
        ObjectFilter objectFilter = generator.getObjectFilter();
        assertNotNull(objectFilter, "ForceJPAClassGenerator objectFilter should default when not specified");
        assertEquals(objectFilter.getClass(), ObjectNoOpFilter.class,
                "ForceJPAClassGenerator objectFilter should default to ObjectNoOpFilter");
    }
    
    @Test
    public void testFieldFilterDefaultsToForceJPAFieldFilter() {
        ForceJPAClassGenerator generator = new ForceJPAClassGenerator();
        
        FieldFilter fieldFilter = generator.getFieldFilter();
        assertNotNull(fieldFilter, "ForceJPAClassGenerator fieldFilter should default when not specified");
        assertEquals(fieldFilter.getClass(), ForceJPAFieldFilter.class,
                "ForceJPAClassGenerator fieldFilter should default to ForceJPAFieldFilter");
    }
    
    @Test
    public void testFieldFilterAlwaysIncludesForceJPAFieldFilter() {
        ForceJPAClassGenerator generator = new ForceJPAClassGenerator();
        generator.setFieldFilter(new FieldNoOpFilter());
        
        FieldFilter fieldFilter = generator.getFieldFilter();
        assertNotNull(fieldFilter, "ForceJPAClassGenerator fieldFilter should never be null");
        assertEquals(fieldFilter.getClass(), FieldCombinationFilter.class,
                "ForceJPAClassGenerator should use a FieldCombinationFilter when a caller sets a field filter");
        
        List<FieldFilter> filterList = ((FieldCombinationFilter) fieldFilter).getFilterList();
        assertEquals(filterList.get(filterList.size() - 1).getClass(), ForceJPAFieldFilter.class,
                "ForceJPAFieldFilter should always be the last field filter in ForceJPAClassGenerator");
    }
    
    @DataProvider
    protected Object[][] packageNameProvider() {
        return new Object[][] {
                {"", false},
                {"1", false},
                {"..", false},
                {"com", true},
                {"1com", false},
                {"com1", true},
                {"com.deadbeef", true},
                {"com.1deadbeef", false},
                {"com.deadbeef.", false},
                {"com.deadbeef.model", true},
                {"com/deadbeef/model", false},
        };
    }
    
    @Test(dataProvider = "packageNameProvider")
    public void testIsPackageNameValid(String packageName, boolean expectedIsValid) {
        try {
            ForceJPAClassGenerator.validatePackageName(packageName);
            assertTrue(expectedIsValid, "This package name should be invalid: " + packageName);
        } catch (IllegalArgumentException e) {
            assertFalse(expectedIsValid, "This package name should be valid: " + packageName);
        }
    }
    
    @Test
    public void testGenerateWithInvalidPackageName() throws ConnectionException, IOException {
        ForceJPAClassGenerator generator = new ForceJPAClassGenerator();
        
        try {
            generator.setPackageName("1");
            fail("Should not be able to set an invalid package name");
        } catch (IllegalArgumentException expected) {
            assertEquals(expected.getMessage(), "Invalid package name: 1");
        }
    }
}
