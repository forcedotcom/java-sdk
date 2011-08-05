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

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import javax.persistence.*;
import javax.persistence.metamodel.Attribute;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.datanucleus.exceptions.NucleusException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.jpa.schema.entities.ExistingCustomObject;
import com.force.sdk.jpa.schema.entities.StandardFieldLinkingEntity;
import com.force.sdk.qa.util.TestContext;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Tests related to malformed schemas and the exceptions that the javasdk throws. This test uses multiple
 * persistence unit definitions each containing one faulty schema element and asserts the exception at 
 * schema load time.
 *
 * @author Dirk Hain
 */
public class SchemaTest extends SchemaBaseTest {

    @DataProvider
    public Object[][] schemaDataProvider() throws NumberFormatException, MalformedURLException {
        Object [][] schemaTestVals = new Object[][]{
                {"testMappedSuperclassWithOverride",
                    "@AttributeOverride or @AssociationOverride is not supported by Force.com datastore."},
                {"testUniqueConstraint", "@UniqueConstraint is not supported by Force.com datastore"},
                {"testLob", "@Clob field type is not supported"},
                {"testJoinTable", "@JoinTable is not supported."},
                {"testNonCustomizableStandardObjectWithCustomField", "Cannot add custom fields to entity: CaseComment "},
                {"testNonStringPrimaryKey", "ID field type should be String"},
                {"testEmbeddableId", "ID field type should be String"},
                {"testCompositePrimaryKey", "Only single string column primary keys supported as ID by Force.com datastore"},
                {"testColumnWithTable", "Table cannot be specified at a column level"},
                {"testNegativePrimaryKey_TABLE",
                    "@Id column requires value generation @GeneratedValue(strategy = GenerationType.IDENTITY)"},
                {"testNegativePrimaryKey_AUTO",
                    "@Id column requires value generation @GeneratedValue(strategy = GenerationType.IDENTITY)"},
                {"testNegativePrimaryKey_SEQUENCE",
                    "@Id column requires value generation @GeneratedValue(strategy = GenerationType.IDENTITY)"},
                {"testTableGenerator",
                    "@Id column requires value generation @GeneratedValue(strategy = GenerationType.IDENTITY). "
                        + "Offending entity: com.force.sdk.jpa.schema.entities.TableGeneratorTest"},
                {"testSequenceGenerator",
                    "@Id column requires value generation @GeneratedValue(strategy = GenerationType.IDENTITY). "
                        + "Offending entity: com.force.sdk.jpa.schema.entities.SequenceGeneratorTest"},
                {"testNegativeInheritance_JOINED", "Only SINGLE_TABLE inheritance strategy supported by Force.com datastore"},
                {"testNegativeInheritance_TABLE_PER_CLASS",
                    "Only SINGLE_TABLE inheritance strategy supported by Force.com datastore"},
                {"testNegativeInheritance_JoinColumnOnSubtype", "@PrimaryKeyJoinColumn is not supported by Force.com datastore"},
                {"testSecondaryTable", "Secondary tables are not supported by Force.com datastore"},
                {"testEmbeddable", "Embedded objects cannot have table specification"},
                {"testManyToMany",
                    "@ManyToMany relationship is not supported. Please use a junction object with two @ManyToOne relationships"},
                {"testOneToMany",
                    "@OneToMany relationship requires the 'mappedBy' attribute. "
                        + "Please add a foreign key field on child object and use that field name for mappedBy attribute"},
                {"testNonStrictPicklistNonString",
                    "Non-strict picklist can be of type String or String[] only. Offending field: "},
                {"testNonStrictPicklistNonStringMP",
                    "Non-strict picklist can be of type String or String[] only. Offending field: "},
                {"testNonStrictPicklistValue",
                    "Non-strict picklist requires @PicklistValue annotation. Offending field: "},
                {"testNonStrictPicklistValueMP", "Non-strict picklist requires @PicklistValue annotation. Offending field: "},
                {"testNonStrictPicklistOrdinal", "Non-strict picklist does not support EnumType.ORDINAL. Offending field: "},
                {"testNonStrictPicklistOrdinalMP", "Non-strict picklist does not support EnumType.ORDINAL. Offending field: "},
                {"testCustomObjectWithPrefixDifferentFromOrgNamespace",
                    "Cannot create a new component with the namespace: NotANamespace."
                        + "  Only components in the same namespace as the organization can be created through the API"},
                {"testNewCustomObjectWithInvalidTableName", "Could not parse table: Two__Underscores__Twice__c"}
        };
        return schemaTestVals;
    }
  
    
    @Test(dataProvider = "schemaDataProvider")
    /**
     * Negative tests to assert proper exceptions during schema creation.
     * This set of tests is related to all schema exceptions that the javasdk throws
     * if a developer has provided an invalid schema.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults PersistenceException is thrown.
     */
    public <E extends PersistenceException> void testSchemaError(String persistenceUnitName, String exceptionMessage)
    throws Exception {
        Logger logger = Logger.getLogger("DataNucleus.MetaData");
        // We will run a lot of negative tests that DN will log. We want to avoid that
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.OFF);
        try {
            emfac = Persistence.createEntityManagerFactory(persistenceUnitName, dynamicOrgConfig);
            Assert.fail("Persistence unit " + persistenceUnitName + " contains an unsupported feature and should have"
                    + " thrown an exception.");
        } catch (PersistenceException e) {
            /**
             * This is really sucky, if we do org creation then as a side effect we add this property
             * "javax.persistence.provider" to initialization. This causes DataNucleus provider to try
             * to initialize these test persistence units too. As a result we hit the same error twice,
             * one for our provider and once for DN. Now when we have two exceptions in initialization
             * the error is reported to us in a flattened single level PersistenceException.
             * On the other hand when we do not do org creation we only get one exception which is not flattened.
             * In that cae we have to look at the NucleusUserException.
             * This is the code path that a typical misconfigured system will hit.
             */
            if (e.getCause() == null) {
                Assert.assertTrue(e.getMessage().contains(exceptionMessage), "Exception message was wrong: " + e.getMessage());
            } else {
                NucleusException cause = (NucleusException) e.getCause();
                if (cause.getNestedExceptions() == null) {
                    Assert.assertTrue(cause.getMessage().contains(exceptionMessage),
                            "Exception message was wrong: " + e.getMessage());
                } else {
                    Assert.assertTrue(cause.getNestedExceptions()[0].getMessage().contains(exceptionMessage),
                            "Exception message was wrong: " + e.getMessage());
                }
            }
        } finally {
            logger.setLevel(oldLevel);
        }
    }
    
    
    @Test
    /**
     * Verify that standard fields are linked to the JPA entity.
     * Tests that standard fields are linked to the entity upon schema creation. The {@link StandardFieldLinkingEntity} 
     * defines standard fields explicitly and this tests makes sure that the standard fields are not recreated as
     * custom fields on the type during schema creation.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults StandardFieldLinkEntity should only have standard fields.
     */
    public void testLinkStandardFields() {
        emfac = Persistence.createEntityManagerFactory("testStandardFieldLinking", dynamicOrgConfig);
        em = emfac.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        StandardFieldLinkingEntity entity = new StandardFieldLinkingEntity();
        
        try {
            tx.begin();
            entity.setName(StandardFieldLinkingEntity.class.getSimpleName());
            em.persist(entity);
            Assert.assertTrue(em.contains(entity), "The entity was not stored to the database.");
            tx.commit();
            StandardFieldLinkingEntity retrieve = em.find(StandardFieldLinkingEntity.class, entity.getId());
            Assert.assertEquals(retrieve.getName(), StandardFieldLinkingEntity.class.getSimpleName(),
                    "Entity was not retrieved properly.");
            Set<Attribute<? super StandardFieldLinkingEntity, ?>> attributes =
                em.getMetamodel().entity(StandardFieldLinkingEntity.class).getAttributes();
            Set<Field> s = removeJDOFields(StandardFieldLinkingEntity.class);
            Assert.assertEquals(attributes.size(), s.size(), "There was an unexpected number of attributes.");
            Iterator<Attribute<? super StandardFieldLinkingEntity, ?>> iter = attributes.iterator();
            while (iter.hasNext()) {
                Attribute<? super StandardFieldLinkingEntity, ?> attrib = iter.next();
                Assert.assertTrue(typeDefinesField(StandardFieldLinkingEntity.class, attrib.getName()), "Field ");
            }
        } catch (PersistenceException pex) {
            Assert.fail(pex.getMessage());
        }
    }
    
    @Test
    public void testExtendEntityClassesStandardFieldsOnly() throws Exception {
        emfac = Persistence.createEntityManagerFactory("testExtendEntityClassesStandardFieldsOnly", dynamicOrgConfig);
        em = emfac.createEntityManager();
        // check that custom object Vehicle__c is created correctly
        verifyFieldsOnSObject("Vehicle__c",
                new String[] { "CreatedById", "CreatedDate", "Id", "IsDeleted", "LastModifiedById",  "LastModifiedDate",
                                "Name", "OwnerId", "SystemModstamp"});
        // TODO: add verification for the child entities Car and AudiA8
    }
    
    @Test
    public void testExtendEntityClassesStandardWithCustomFields() throws Exception {
        emfac = Persistence.createEntityManagerFactory("testExtendEntityClassesStandardWithCustomFields", dynamicOrgConfig);
        em = emfac.createEntityManager();
        // check that custom object Animal__c is created correctly
        verifyFieldsOnSObject("Animal__c",
                new String[] { "CreatedById", "CreatedDate", "Id", "IsDeleted", "LastModifiedById", "LastModifiedDate",
                                "Name", "OwnerId", "SystemModstamp", "age__c"});
        // TODO: add verification for the child entities Bear and Mammal
    }
    
    @Test
    public void testExtendEntityClassesWithOneToManyRelationship() throws Exception {
        emfac = Persistence.createEntityManagerFactory("testExtendEntityClassesWithOneToManyRelationship", dynamicOrgConfig);
        em = emfac.createEntityManager();
        // check that custom object AbstractParentEntity__c is created correctly
        verifyFieldsOnSObject("AbstractParentEntity__c",
                new String[] { "CreatedById", "CreatedDate", "Id", "IsDeleted", "LastModifiedById",  "LastModifiedDate",
                                "Name",  "OwnerId", "SystemModstamp"});
        // check that custom object ChildWithAbstractParentEntity__c is created correctly
        verifyFieldsOnSObject("ChildWithAbstractParentEntity__c",
                new String[] { "CreatedById", "CreatedDate", "Id", "IsDeleted", "LastModifiedById",  "LastModifiedDate",
                                "Name",  "OwnerId", "SystemModstamp", "parent__c"});
        // TODO: add verification for ConcreteParentEntity__c
    }
    
    @Test
    /**
     * Startup with autoCreateSchema=false.
     * Test runs with autoCreateSchema = false and verifies appropriate error logs for missing tables and fields.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Correct logs are received by Appender.
     */
    public void testAutoCreateSchemaFalse() {
        Persistence.createEntityManagerFactory("setupExistingCustomObject").createEntityManager();

        Logger logger = Logger.getLogger("com.force.sdk.jpa");
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.WARN);
        final AtomicBoolean receivedTableLog = new AtomicBoolean(false);
        final AtomicBoolean receivedFieldLog = new AtomicBoolean(false);
        Appender appender = new AppenderSkeleton() {
            
            private Pattern missingTablePat =
                Pattern.compile("Table does not exist in force.com and datanucleus.autoCreateTables is false,"
                                    + " table: [a-zA-Z0-9_$]*SimpleValidEntity__c");
            private Pattern missingFieldsPat =
                Pattern.compile("Field does not exist in force.com table and datanucleus.autoCreateColumns is false,"
                                    + " entity: ExistingCustomObjectExtension1 table: [a-zA-Z0-9_$]*"
                                    + "ExistingCustomObject__c fields: \\[newCustomField1\\]");
            
            @Override
            public boolean requiresLayout() {
                return false;
            }
            
            @Override
            public void close() {
            }
            
            @Override
            protected void append(LoggingEvent event) {
                if (event != null) {
                    if (missingTablePat.matcher(event.getRenderedMessage()).find()) {
                        receivedTableLog.set(true);
                    } else if (missingFieldsPat.matcher(event.getRenderedMessage()).find()) {
                        receivedFieldLog.set(true);
                    }
                }
            }
        };
        logger.addAppender(appender);
        
        try {
            emfac = Persistence.createEntityManagerFactory("testAutoCreateSchemaFalse", dynamicOrgConfig);
            em = emfac.createEntityManager();
            ExistingCustomObject ent = new ExistingCustomObject();
            ent.setExistingCustomField("some value");
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(ent);
            tx.commit();
            
            ExistingCustomObject result = em.find(ExistingCustomObject.class, ent.getId());
            Assert.assertEquals(result.getExistingCustomField(), "some value");
            
            Assert.assertTrue(receivedTableLog.get(), "Did not receive log messages for missing tables");
            Assert.assertTrue(receivedFieldLog.get(), "Did not receive log messages for missing fields");
        } finally {
            logger.setLevel(oldLevel);
            logger.removeAppender(appender);
        }
    }
    
    @Test
    /**
     * Test for entity hierarchy of one level (i.e. JPAClass extends OtherJPAClass).
     * Verifies that JPA entity hierarchies are mapped correctly to salesforce objects by the javasdk.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Correct fields should be present in testExtendOneLevel object.
     */
    public void testExtendOneLevel() throws Exception {
        emfac = Persistence.createEntityManagerFactory("testExtendOneLevel", dynamicOrgConfig);
        em = emfac.createEntityManager();
        
        verifyFieldsOnSObject("OneLevelParent__c",
            new String[] {"CreatedById" , "CreatedDate", "Id", "IsDeleted", "LastModifiedById",
                "LastModifiedDate", "Name", "OwnerId", "SystemModstamp", "childOnly__c" }
                );
    }
    
    @Test
    public void testMappedSuperclass() throws Exception {
        emfac = Persistence.createEntityManagerFactory("testMappedSuperclass", dynamicOrgConfig);
        em = emfac.createEntityManager();
        
        // The sub-class should have fields from the mapped super class as well
        verifyFieldsOnSObject("MappedSubclassEntity__c",
                new String[] {"CreatedById", "CreatedDate", "Id", "IsDeleted", "LastModifiedById",
                "LastModifiedDate", "Name", "OwnerId", "SystemModstamp", "someSubtypeValue__c", "someSuperTypeValue__c"});
    }
    
    @Test
    /**
     * Numerical field precision.
     * Verifies that the precision of numerical fields generated in the schema is correct.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Correct fields should be present in testNumericTypePrecision object.
     */
    public void testNumericalFieldPrecision() throws Exception {
        emfac = Persistence.createEntityManagerFactory("testNumericTypePrecision", dynamicOrgConfig);
        em = emfac.createEntityManager();
        verifyFieldsOnSObject("AllNumericTypes__c",
            new String[] {"CreatedById" , "CreatedDate", "Id", "IsDeleted", "LastModifiedById",
                "LastModifiedDate", "Name", "OwnerId", "SystemModstamp", "integerObject__c", "integerPrimitive__c",
                "shortObject__c", "shortPrimitive__c", "doubleObject__c", "doublePrimitive__c",
                "longObject__c", "longPrimitive__c", "floatObject__c", "floatPrimitive__c", "bigInteger__c", "bigDecimal__c" }
                );
        
        String prefix = "";
        String namespace = getNamespaceFromCtx();
        if (namespace != null && namespace != "") {
            prefix = namespace + NAME_SEPARATOR;
        }
        
        Map<String, PrecisionScale> expectedFieldMetadata = new HashMap<String, PrecisionScale>();
        expectedFieldMetadata.put(prefix + "integerObject__c", new PrecisionScale(11, 0));
        expectedFieldMetadata.put(prefix + "integerPrimitive__c", new PrecisionScale(11, 0));
        expectedFieldMetadata.put(prefix + "shortObject__c", new PrecisionScale(6, 0));
        expectedFieldMetadata.put(prefix + "shortPrimitive__c", new PrecisionScale(6, 0));
        expectedFieldMetadata.put(prefix + "doubleObject__c", new PrecisionScale(16, 2));
        expectedFieldMetadata.put(prefix + "doublePrimitive__c", new PrecisionScale(16, 2));
        expectedFieldMetadata.put(prefix + "longObject__c", new PrecisionScale(18, 0));
        expectedFieldMetadata.put(prefix + "longPrimitive__c", new PrecisionScale(18, 0));
        expectedFieldMetadata.put(prefix + "floatObject__c", new PrecisionScale(16, 2));
        expectedFieldMetadata.put(prefix + "floatPrimitive__c", new PrecisionScale(16, 2));
        expectedFieldMetadata.put(prefix + "bigInteger__c", new PrecisionScale(18, 0));
        expectedFieldMetadata.put(prefix + "bigDecimal__c", new PrecisionScale(16, 2));

        verifyFieldPrecisions(prefix + "AllNumericTypes__c", expectedFieldMetadata);
            
    }
    
    /**
     * Class to encapsulate precision and scale for numeric fields.
     * 
     * @author Dirk Hain
     */
    private static class PrecisionScale {
        public int precision;
        public int scale;
        
        public PrecisionScale(int precision, int scale) {
            this.precision = precision;
            this.scale = scale;
        }
    }
    
    /**
     * Verifies that fields are correct in the underlying salesforce custom object.
     */
    private void verifyFieldsOnSObject(String sObject, String[] expectedFieldNames) throws Exception {
        PartnerConnection conn = getPartnerConnection();
        String ns = getNamespaceFromCtx();
        if (ns != null && ns != "") {
            sObject = ns + NAME_SEPARATOR + sObject;
        }
        DescribeSObjectResult describeSObjectResult = conn.describeSObject(sObject);
        com.sforce.soap.partner.Field[] fields = describeSObjectResult.getFields();
        Assert.assertEquals(fields.length, expectedFieldNames.length, "Unexpected number of fields on object " + sObject);
        ArrayList<String> fieldNames = new ArrayList<String>();
        for (com.sforce.soap.partner.Field field : fields) {
            if (field.isCustom() && ns != null && ns != "") {
                String fn = field.getName();
                fieldNames.add(fn.substring(ns.length() + NAME_SEPARATOR.length()));
            } else {
                fieldNames.add(field.getName());
            }
        }
        Collections.sort(fieldNames);
        Arrays.sort(expectedFieldNames);
        
        for (int i = 0; i < fieldNames.size(); i++) {
            Assert.assertEquals(fieldNames.get(i), expectedFieldNames[i], "Unexpected field name");
        }
    }
    
    /**
     * Verifies that field precisions and decimal place counts are correct 
     * in the underlying salesforce custom object.
     */
    private void verifyFieldPrecisions(String sObject,
            Map<String, PrecisionScale> expectedFieldMetadata) throws Exception {
        PartnerConnection conn = getPartnerConnection();
        DescribeSObjectResult describeSObjectResult = conn.describeSObject(sObject);
        com.sforce.soap.partner.Field[] fields = describeSObjectResult.getFields();

        for (int i = 0; i < fields.length; i++) {
            if (expectedFieldMetadata.containsKey(fields[i].getName())) {
                Assert.assertEquals(
                        fields[i].getPrecision(),
                        expectedFieldMetadata.get(fields[i].getName()).precision,
                        "Unexpected precisions on field: " + fields[i].getName());
                Assert.assertEquals(
                        fields[i].getScale(),
                        expectedFieldMetadata.get(fields[i].getName()).scale,
                        "Unexpected precisions on field: " + fields[i].getName());
            }
        }
    }
    
    private PartnerConnection getPartnerConnection() throws ConnectionException {
        ForceConnectorConfig config = new ForceConnectorConfig();
        config.setAuthEndpoint(TestContext.get().getUserInfo().getServerEndpoint());
        config.setUsername(TestContext.get().getUserInfo().getUserName());
        config.setPassword(TestContext.get().getUserInfo().getPassword());
        return new ForceServiceConnector(config).getConnection();
    }
    
}
