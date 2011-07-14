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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * Unit tests for {@link FieldNoOpFilter}.
 *
 * @author Tim Kral
 */
public class FieldNoOpFilterTest {

    @Test
    public void testFilterWithNullValue() {
        assertNull(new FieldNoOpFilter().filter(null), "A no op filter of a null value should be null");
    }
    
    @Test
    public void testFilterWithNonNullValue() {
        DescribeSObjectResult dsr = new DescribeSObjectResult();
        dsr.setName("Object_Name__c");
        
        Field idField = new Field();
        idField.setName("Id");
        idField.setType(FieldType.id);
        
        Field nameField = new Field();
        nameField.setName("Name");
        nameField.setType(FieldType.string);
        
        dsr.setFields(new Field[] { idField, nameField });
        
        dsr = new FieldNoOpFilter().filter(dsr);
        
        assertNotNull(dsr, "A no op filter of a non-null value should be non-null");
        assertNotNull(dsr.getFields(), "A no op field filter should not null out object Field list");
        assertEquals(dsr.getFields().length, 2, "Unexpected number of Fields after no op filter");
        assertEquals(dsr.getFields()[0].getName(), "Id", "Unexpected Field order after no op filter");
    }
}
