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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.schema.entities.ExistingCustomObject;

/**
 *
 * Test class to delete schema. Note that this test can't do any verification that force.purgeOnDeleteSchema successfully bypasses
 * the recycle bin. We cover that via tests in core that test the deployOption works properly, and a unit test called
 * {@link SchemaDeletionTest} that tests that the deployOption is set when we expect it to be set.
 *
 * @author Fiaz Hossain
 */
public class SchemaDeleteTest extends SchemaBaseTest {

    @BeforeMethod
    public void testSetup() {
        Persistence.createEntityManagerFactory("setupExistingCustomObject").createEntityManager();
    }

    @DataProvider
    public Object[][] schemaDeleteDataProvider() throws NumberFormatException, MalformedURLException {
        return new Object[][]{
                {true},
                {false}
        };
    }


    @Test(dataProvider = "schemaDeleteDataProvider")
    /**
     * Delete schema elements that are not readOnlySchema.
     * Tests that schema delete does remove schema metadata for objects not created with readOnlySchema=true. The test
     * first creates the schema and then removes it via invoking schema creation again.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Writable elements should be deleted from schema.
     */
    public void testDeleteSchema(boolean purgeSchemaOnDelete) throws Exception {
        emfac = Persistence.createEntityManagerFactory("testDeleteSchema", dynamicOrgConfig);
        em = emfac.createEntityManager();
        em.createQuery("select o from ExistingCustomObject o").getResultList();
        em.createNativeQuery("select " + getFieldName(em, ExistingCustomObject.class, "newCustomField1")
                                + " from " + getTableName(em, ExistingCustomObject.class)).getResultList();
        em.createQuery("select o from SimpleValidEntity o").getResultList();

        // Set the property and load the entity manager factory again
        Map<String, Object> props = dynamicOrgConfig != null ? dynamicOrgConfig : new HashMap<String, Object>();
        props.put("force.deleteSchema", true);
        if (purgeSchemaOnDelete) {
            props.put("force.purgeOnDeleteSchema", true);
        }
        try {
            emfac = Persistence.createEntityManagerFactory("testDeleteSchema", props);
            em = emfac.createEntityManager();

            // At this point the object should have been deleted so running the query will get an API exception.
            try {
                em.createQuery("select o from SimpleValidEntity o").getResultList();
                Assert.fail("SimpleValidEntity should have been deleted");
            } catch (PersistenceException pe) {
                Assert.assertTrue(pe.getMessage().contains("InvalidSObjectFault"), "Unexpected message: " + pe.getMessage());
            }

            //  However ExistingCustomObject should not be deleted as it's created with readonlySchema
            em.createQuery("select o from ExistingCustomObject o").getResultList();

            // Fields added to a readOnlySchema object are still deleted
            try {
                em.createNativeQuery("select " + getFieldName(em, ExistingCustomObject.class, "newCustomField1")
                                        + " from " + getTableName(em, ExistingCustomObject.class)).getResultList();
                Assert.fail("ExistingCustomObject__c.newCustomField1__c should have been deleted");
            } catch (PersistenceException pe) {
                Assert.assertTrue(pe.getMessage().contains("InvalidFieldFault"), "Unexpected message: " + pe.getMessage());
            }
        } finally {
            props.remove("force.deleteSchema");
            props.remove("force.purgeOnDeleteSchema");
        }
    }

}
