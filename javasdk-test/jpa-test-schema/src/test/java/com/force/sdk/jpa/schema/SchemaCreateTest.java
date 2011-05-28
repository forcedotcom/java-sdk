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

package com.force.sdk.jpa.schema;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.force.sdk.jpa.schema.entities.ExistingCustomObjectExtension1;
import com.force.sdk.jpa.schema.entities.UserCustomFields;
import com.force.sdk.test.util.BaseJPAFTest;
import com.sforce.soap.partner.fault.InvalidFieldFault;

/**
 * 
 * Tests adding columns to standard and custom objects that already exist in a developer's org.
 *
 * @author Jill Wetzler
 */
public class SchemaCreateTest extends BaseJPAFTest {

    @BeforeMethod
    public void testSetup() {
        Persistence.createEntityManagerFactory("setupExistingCustomObject").createEntityManager();
    }
    
    @Test
    /**
     * The DOT used for ftests contains a standard object "User" as well as a custom object "ExistingCustomObject".
     * The entity files User.java and ExistingCustomObject.java contain custom fields that don't exist in the DOT.
     * 
     * This test writes a simple query against both entities to verify that the fields got created once the test starts up.
     */
    public void testCreateColumnsOnExistingObjects() throws Exception {
        EntityManager manager =
            Persistence.createEntityManagerFactory("testCreateColumnsOnExistingObjects").createEntityManager();

        String errors = "";
        try {
            service.query("select " + getFieldName(manager, UserCustomFields.class, "newCustomField") + " from User limit 1");
        } catch (Exception e) {
            if (e instanceof InvalidFieldFault) {
                errors = "Problem querying for new custom field on User, ";
            } else {
                errors = "Exception thrown querying User: " + e.getMessage() + ", ";
            }
        }

        try {
            service.query("select " + getFieldName(manager, ExistingCustomObjectExtension1.class, "newCustomField1")
                    + " from " + getTableName(manager, ExistingCustomObjectExtension1.class) + " limit 1");
        } catch (Exception e) {
            if (e instanceof InvalidFieldFault) {
                errors += "Problem querying for new custom field on custom object";
            } else {
                errors = "Exception thrown querying custom object: " + e.getMessage();
            }
        }
        
        if (!"".equals(errors)) {
            Assert.fail(errors);
        }
    }
}
