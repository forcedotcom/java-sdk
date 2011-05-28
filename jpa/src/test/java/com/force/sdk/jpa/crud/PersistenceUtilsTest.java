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

package com.force.sdk.jpa.crud;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import javax.persistence.*;

import mockit.Mockit;

import org.datanucleus.ObjectManager;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.testng.annotations.*;

import com.force.sdk.jpa.PersistenceUtils;
import com.force.sdk.jpa.entities.*;
import com.force.sdk.jpa.mock.*;

/**
 * Unit tests for PersistenceUtils.
 *
 * @author Tim Kral
 */
public class PersistenceUtilsTest {

    // The EntityManager used to execute Force.com JPA queries
    protected EntityManager em;
    
    // Schema handler which stores table and field names
    protected MockForceStoreSchemaHandler mockSchemaHandler;
    
    @BeforeClass
    public void classSetUp() throws IOException, ClassNotFoundException {
        // Register all of our JPA entities.
        // This would normally be done by PersistenceUtils in createSchema or loadSchema
        mockSchemaHandler = new MockForceStoreSchemaHandler();
        mockSchemaHandler.registerAllTables();
        
        Mockit.setUpMocks(MockForceTableMetaData.class, MockForceColumnMetaData.class,
                            MockForceManagedConnection.class, mockSchemaHandler, MockForceSchemaWriter.class);

        // Setup the EntityManager just like normal
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpaQueryUnitTest");
        em = emf.createEntityManager();
    }
    
    @DataProvider
    public Object[][] javaFieldProvider() {
        return new Object[][]{
                // Tests API names for standard fields common to all entities (e.g. Id, Name)
                {"testCommonStandardField", User.class, "id", "id"},
                
                {"testStandardField", User.class, "emailEncodingKey", "emailEncodingKey"},
                {"testExistingCustomField", User.class, "existingCustomField", "existingCustomField__c"},
                {"testNewCustomField", UserCustomFields.class, "newCustomField", "newCustomField__c"},
                {"testStandardFieldOnSubclass", UserCustomFields.class, "emailEncodingKey", "emailEncodingKey"},
                {"testExistingCustomFieldOnSubclass", UserCustomFields.class, "existingCustomField", "existingCustomField__c"},
                
                {"testStandardRelationshipField", Account.class, "childOpportunities", "opportunities"},
                {"testCustomRelationshipField", BasicParentTestEntity.class, "childEntities",
                    "BasicParentTestEntity_BasicChildTestEnti__r"},
        };
    }
    
    @Test(dataProvider = "javaFieldProvider")
    public void testGetForceApiName(String testName, Class entityClass, String memberName, String expectedApiName) {
        ObjectManager om = (ObjectManager) em.getDelegate();

        AbstractClassMetaData acmd = om.getMetaDataManager().getMetaDataForClass(entityClass, null);
        AbstractMemberMetaData ammd = acmd.getMetaDataForMember(memberName);
        
        String apiName = PersistenceUtils.getForceApiName(ammd, om.getOMFContext());
        assertEquals(apiName, expectedApiName, "Unexpected API name");
    }
}
