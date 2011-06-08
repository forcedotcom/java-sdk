---

layout: doc
title: Configuring the JPA Provider

---
# Configuring the JPA Provider

The standard JPA configuration file is `persistence.xml`. This file must reside in the META-INF directory of the root of the persistence unit of a compiled and packaged application; for example, in the `WEB-INF/classes/META-INF` directory.

The `persistence.xml` file includes one or more *persistence-unit* elements. A persistence unit defines a set of classes and how
to persist them. Each persistence unit has a unique name. In this example, there is one unit defined and its name is *forceDatabase*.

    <?xml version="1.0" encoding="UTF-8"?>
    <persistence xmlns="http://java.sun.com/xml/ns/persistence"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://java.sun.com/xml/ns/persistence
        http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd"
        version="1.0">
        <persistence-unit name="forceDatabase">
            <provider>com.force.sdk.jpa.PersistenceProviderImpl</provider>
            <!-- In a single module web application, no need to specify classes. Classes
            will be autodiscovered based on @Entity annotation -->
            <properties>
                <property name="datanucleus.storeManagerType" value="force" />
                <property name="datanucleus.autoCreateSchema" value="true" />
                <property name="datanucleus.validateTables" value="false" />
                <property name="datanucleus.validateConstraints" value="false" />
                <property name="datanucleus.Optimistic" value="false" />
                <property name="datanucleus.datastoreTransactionDelayOperations"
                value="true" />
                <property name="datanucleus.jpa.addClassTransformer" value="false" />
                <property name="datanucleus.cache.level2.type" value="none" />
                <property name="datanucleus.detachAllOnCommit" value="true" />
                <property name="datanucleus.copyOnAttach" value="false" />
            </properties>
        </persistence-unit>
    </persistence>
    
The `persistence.xml` file includes the following elements:

<dl>
  
  <dt>&lt;persistence> Root Element</dt>
    <dd>The <em>persistence</em> element is the root element of the persistence.xml file. The persistence
element consists of one or more persistence-unit elements.</dd>
  <dt>&lt;persistence-unit> Element</dt>
    <dd>A <em>persistence-unit</em> element has a required <em>name</em> attribute and several optional sub-elements.</dd>
  <dt>&lt;provider> Element</dt>
    <dd>Define a JPA persistence provider in a provider element. The provider for the Database.com JPA provider is
<code>com.force.sdk.jpa.PersistenceProviderImpl</code>.</dd>
  <dt>&lt;properties> Element</dt>
    <dd>Define properties to give you more control over behavior of the DataNucleus JPA implementation. Use the properties
and values defined in the sample. Each of these properties is explained in the <a href="http://www.datanucleus.org/products/accessplatform/persistence_properties.html">DataNucleus Persistence Properties
documentation</a>.</dd>
</dl>

The **datanucleus.maxFetchDepth** property is not listed in the earlier sample. For details about this property, see [Fetch Depth](jpa-queries#fetchDepth).

<a name="schemaProps"> </a>
## Schema Creation Properties

To control whether the Database.com JPA provider automatically creates schema for entities or fields that don't already exist in Database.com, you can configure the following properties in your application's `persistence.xml` file.

### datanucleus.autoCreateSchema
Set this property to <codeph>true</code> to automatically create schema for entities that don't already exist in Database.com. If the entity already exists, new fields defined in an associated Java class are added to the entity. If schema creation fails, the application is terminated during startup.

### datanucleus.autoCreateWarnOnError
This property is only relevant if **datanucleus.autoCreateSchema** is set to <code>false</code>.

Set **datanucleus.autoCreateWarnOnError** to <code>false</code> to terminate the application on startup if there is a schema mismatch
between a Java entity and Database.com. This is the recommended setting. If the property is set to <code>true</code>, the
provider logs a warning about the schema mismatch. The schema in Database.com is out-of-sync with the Java
entity definition, which may cause application errors or unpredictable behavior.

<a name="schemaDeleteProps"> </a>
## Schema Deletion Properties

To control whether the Database.com JPA provider can delete schema for entities or fields in your organization, you can configure the following properties in your application's `persistence.xml` file. The main reason for deleting schema is to remove customizations during testing to start with a consistent set of schema.

If you have appropriate permissions, you can always delete schema using the user interface in Salesforce regardless of these property values.

### force.deleteSchema
This property is only relevant if **datanucleus.autoCreateSchema** is set to <code>true</code>.

Set this property to <code>true</code> to allow the Database.com JPA provider to delete schema in your organization on startup. If you've specified <code>@CustomObject(readOnlySchema = true)</code> on an entity, that entity will not be deleted.

### force.purgeOnDeleteSchema
This property is only relevant if both **datanucleus.autoCreateSchema** and **force.deleteSchema** are set to <code>true</code>.

Set this property to <code>true</code> if you want schema deletion to bypass the Recycle Bin. Instead, deleted schema becomes immediately eligible for deletion.

<a name="authProps"> </a>
## Authentication Properties

For user authentication in JPA, you can configure the following property in your application's `persistence.xml` file.

**Note**: To connect to Database.com, an authenticated user must have the "API Enabled" user permission in
their profile. If the **datanucleus.autoCreateSchema** property is enabled in `persistence.xml`, an authenticated
user must have the "Modify All Data" user permission in their profile to create new Database.com entities in the database.

### datanucleus.ConnectionURL
Use the URL format to encode all authentication information.

    force://login.salesforce.com;user=user@salesforcedoc.org;password=samplePassword

Each version of the Database.com Java SDK is automatically linked with an API version. 

<img src="http://na1.salesforce.com//img/help/helpWarning_icon.gif" alt="Caution icon" /> **Caution**: For JPA configuration, we don't support overriding the default API version of the SDK. 

### force.ConnectionName
Use this property to define a named connection for a persistence unit. You can refer to the named connection when you are configuring a connection URL outside `persistence.xml`. For example, you can configure a connection URL in an environment variable or Java system property.  

For more details, see [Configuring Connection URLs](connection-url#configConnectionURL).

<a name="transProps"> </a>
## Transaction Properties

To use transactions with JPA, you must configure the following properties in your application's `persistence.xml` file.

### datanucleus.datastoreTransactionDelayOperations
Set this property to true to ensure that all JPA operations are buffered until <code>commit()</code> is called on a transaction. If the property is set to false, every operation handled by the EntityManager is independently committed to the database.

**Note**: The Database.com JPA provider doesn't do anything when <code>flush()</code> is called on an EntityManager. Use the <code>@Transactional</code> annotation in Spring or <code>commit()</code> instead for transactions.

### sfdc.AllOrNothing
Set this property to <code>true</code> to ensure that all changes are rolled back if any errors occur when persisting records. If the
property is set to <code>false</code>, changes to records with no errors are committed even if there are errors persisting other records
in the transaction.

If a transaction includes an insert, update, and a delete operation, this property applies to each operation separately. For
example, if the insert and delete operations have no errors, but the update operation has at least one error, the insert and
operations are committed, while the update operation doesn't change any records due to the error.

### datanucleus.Optimistic
Contact salesforce.com if you want to enable optimistic transactions for your organization. Once salesforce.com has enabled optimistic transactions for your organization, you can set this property to <code>true</code> to use optimistic transactions.

Optimistic concurrency control is a method that assumes that
multiple transactions can complete without affecting each other, and that transactions can proceed without locking the
data resources that they affect. Before committing, each transaction verifies that no other transaction has modified its
data. If the check reveals conflicting modifications, the committing transaction rolls back.

When this property is set to <code>true</code>, each Java class that models an entity used in a transaction should include a field with
the following signature:

    @Version
    private Calendar lastModifiedDate;

The lastModifiedDate field is a system field that is automatically created for every object in Database.com.
The @Version annotation enables JPA to use this date field to do <code>ifModifiedBefore()</code> checks on update and
delete operations. If this check indicates that another operation has updated a record in the transaction, <code>javax.persistence.OptimisticLockException</code> is thrown as the record in the transaction has stale data. If the
**sfdc.AllOrNothing** property is enabled, the transaction is rolled back.

**Note**: If you're using optimistic transactions and you don't specify a lastModifiedDate field with an <code>@Version</code> annotation, then your transaction always succeeds even if another operation has updated a record in the transaction.

### datanucleus.detachAllOnCommit
Set this property to <code>true</code> to detach all objects enlisted in a transaction when the transaction is committed.
