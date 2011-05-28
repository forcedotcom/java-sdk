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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sforce.soap.partner.PicklistEntry;

/**
 * Unit tests for {@link ForcePicklistEnumBuilder}.
 *
 * @author Tim Kral
 */
public class ForcePicklistEnumBuilderTest {

    @DataProvider
    protected Object[][] picklistEntryProvider() {
        
        return new Object[][] {
                {true, false, "Basic Picklist Value", "BasicPicklistValue",
                    "BASICPICKLISTVALUE(true,false,\"Basic Picklist Value\",\"BasicPicklistValue\"),"},
                {false, true, "Inactive With Default", "InactiveWithDefault",
                    "INACTIVEWITHDEFAULT(false,true,\"Inactive With Default\",\"InactiveWithDefault\"),"},
                {true, false, "Non White Space", "Non!White@Space#$",
                    "NON_WHITE_SPACE__(true,false,\"Non White Space\",\"Non!White@Space#$\"),"},
                {true, false, null, "NullLabel",
                    "NULLLABEL(true,false,null,\"NullLabel\"),"},
                {true, false, "Short Value", "S",
                    "SHORT_VALUE(true,false,\"Short Value\",\"S\"),"},
                {true, false, "0", "0",
                    "VALUE_0(true,false,\"0\",\"0\"),"},
                {true, false, "0123", "0123",
                    "VALUE_0123(true,false,\"0123\",\"0123\"),"},
                {true, false, "5 Minute Down Time", "5MinuteDownTime",
                    "VALUE_5MINUTEDOWNTIME(true,false,\"5 Minute Down Time\",\"5MinuteDownTime\"),"},
            };
    }
    
    @Test(dataProvider = "picklistEntryProvider")
    public void testBasicEnumValue(boolean isActive, boolean isDefaultValue, String label, String value,
            String expectedRenderedString) {
        
        PicklistEntry picklistEntry = new PicklistEntry();
        picklistEntry.setActive(isActive);
        picklistEntry.setDefaultValue(isDefaultValue);
        picklistEntry.setLabel(label);
        picklistEntry.setValue(value);
        
        ForcePicklistEnumBuilder builder = new ForcePicklistEnumBuilder(0);
        builder.add(picklistEntry);
        assertEquals(builder.toString(), expectedRenderedString, "Unexpected result for ForcePicklistEnumBuilder");
    }
    
    @Test
    public void testDuplicateEnumValues() {
        PicklistEntry picklistEntry1 = new PicklistEntry();
        picklistEntry1.setLabel("label");
        picklistEntry1.setValue("value");
        
        PicklistEntry picklistEntry2 = new PicklistEntry();
        picklistEntry2.setLabel("label");
        picklistEntry2.setValue("value");
        
        ForcePicklistEnumBuilder builder = new ForcePicklistEnumBuilder(0);
        builder.add(picklistEntry1);
        builder.add(picklistEntry2);
        
        assertEquals(builder.toString(),
                "VALUE(false,false,\"label\",\"value\"),\n"
                + "//VALUE(false,false,\"label\",\"value\"),",
                "Unexpected result for ForcePicklistEnumBuilder");
    }
}
