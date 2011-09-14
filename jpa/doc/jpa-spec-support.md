---

layout: doc
title: JPA 2.0 Specification Support

---
# JPA 2.0 Specification Support

This section lists the areas of the JPA 2.0 specification that are supported by the Database.com JPA provider and how it differs from the standard JPA specification.

The section numbers below map directly to the sections of the [JPA 2.0 specification (JSR 317)](http://jcp.org/aboutJava/communityprocess/final/jsr317/index.html). Overview sections and other sections that don't include specification details, such as sections only containing examples, have been omitted.

## 2 Entities

### 2.1 Entity Class

Supported

### 2.2 Persistent Fields and Properties

Supported

### 2.3 Access Type

Both field and getter method access is supported. However, use of both types on the same file results in undefined behavior.

### 2.4 Primary Keys and Entity Identity

Partially supported. The primary key must be a <code>String</code> data type with <code>@GeneratedValue(strategy=GenerationType.IDENTITY)</code>. Derived identities are not supported. For more details, see [Primary Keys](jpa-provider#primary-keys).

### 2.5 Embeddable Class

Supported

### 2.6 Collections of Embeddable Classes and Basic Types

Supported

### 2.7 Map Collections 

Partially supported.

- The map key and value must be a basic type; embeddable classes and entities are not supported.
- <code>@MapKey</code> is defaulted to the <code>Id</code> field

### 2.8 Mapping Defaults for Non-relationship Fields or Properties

Supported

### 2.9 Entity Relationships

<code>@OneToMany</code> and <code>@ManyToOne</code> are supported. There are some limitations on [cascade](database-com-datatypes#cascade) and limits on [depth](jpa-queries#fetchDepth).

### 2.10 Relationship Mapping Defaults

Our mapping defaults differ from the specification. For more details, see [Relationship Fields](database-com-datatypes#relFields). 

### 2.11 Inheritance

Partially supported. <code>InheritanceType.SINGLE_TABLE</code> is the only supported mapping strategy.

The discriminator column for the <code>InheritanceType.SINGLE_TABLE</code> mapping strategy can't be a primary key.

### 2.12 Inheritance Mapping Strategies

May vary from specification

Partially supported. <code>InheritanceType.SINGLE_TABLE</code> is the only supported mapping strategy.

### 2.13 Naming of Database Objects

Undelimited names are supported. Delimited names are not supported.

## 3 Entity Operations

### 3.1 EntityManager

Supported

### 3.2 Entity Instance's Life Cycle

Supported

### 3.3 Persistence Context Lifetime

#### 3.3.1 Transaction commit

Supported with limitations. For more details, see [Transactions](jpa-transactions).

#### 3.3.2 Transaction rollback

Supported with limitations. For more details, see [Transaction Properties](jpa-config-persistence#transProps).

### 3.4 Locking and Concurrency

#### 3.4.1 Optimistic Locking

Not supported

#### 3.4.2 Version Attributes

The name and type of the version field are restricted. For more details, see [Transaction Properties](jpa-config-persistence#transProps).

#### 3.4.3 Pessimistic Locking

Not supported

#### 3.4.4 Lock Modes

Not supported

#### 3.4.5 OptimisticLockException

Not supported

### 3.5 Entity Listeners and Callback Methods

Supported

### 3.6 Bean Validation

Not supported

### 3.7 Caching

Not supported

### 3.8 Query APIs 

Not supported

### 3.9 Summary of Exceptions

Exceptions work as specified for supported features

## 4 Query Language

### 4.2 Statement Types

Select is supported. Bulk delete is supported but not in transactions. Bulk update is not supported.

### 4.3 Abstract Schema Types and Query Domains

Abstract schema types are supported. Query domains are not supported.

### 4.4 The FROM Clause and Navigational Declarations

Partially supported

#### 4.4.1 Identifiers

Supported

#### 4.4.2 Identification Variables

Supported

#### 4.4.3 Range Variable Declarations

Supported

#### 4.4.4 Path expressions

Partially supported

- The map key and value must be a basic type; embeddable classes and entities are not supported.
- You can only use <code>value()</code> in a SELECT clause. It is not valid in a WHERE clause. For more details, see [Child-Map Joins](jpa-queries#childMapJoins).

#### 4.4.5 Joins

Partially supported with limitations. For more details, see [JPQL Joins](jpa-queries#jpqlJoins).

#### 4.4.6 Collection Member Declarations

Supported. For more details, see [IN Joins](jpa-queries#inJoins).

#### 4.4.7 FROM clause and SQL

Partially supported. The Database.com JPA provider always performs outer joins; inner joins are not supported.

#### 4.4.8 Polymorphism

Supported

### 4.5 WHERE Clause

Supported

### 4.6 Conditional Expressions

#### 4.6.1 Literals

Supported except in the select clause. For example, <code>SELECT 'abc' FROM Entity</code> is not supported.

#### 4.6.2 Identification Variables

Supported

#### 4.6.3 Path Expressions

Supported

#### 4.6.4 Input Parameters

Supported

#### 4.6.5 Conditional Expression Composition

Supported

#### 4.6.6 Operators and Operator Precedence

Supported

#### 4.6.7 Comparison Expressions

Supported

#### 4.6.8 Between Expressions

Supported

#### 4.6.9 In Expressions

Supported

#### 4.6.10 Like Expressions

Supported

#### 4.6.11 Null Comparison Expressions

Supported

#### 4.6.12 Empty Collection Comparison Expressions

You can use the <code>[NOT] IS EMPTY</code> comparison operator to select parent entities whose children or multi-select picklist field values are empty. For more details, see [IS EMPTY](jpa-queries#isEmpty).

#### 4.6.13 Collection Member Expressions

You can use the <code>[NOT] MEMBER OF</code> comparison operator to select parent entities whose children or multi-select picklist field values match defined criteria. For more details, see [MEMBER OF](jpa-queries#memberOf).

#### 4.6.14 Exists Expressions

Not supported

#### 4.6.15 All or Any Expressions

Not supported

#### 4.6.16 Subqueries

Partially supported

Subqueries are supported with an IN clause, but not with an EXISTS clause.

#### 4.6.17 Scalar Expressions

Partially supported. Arithmetic and String functions are not supported. The <code>CURRENT_TIME</code> and <code>CURRENT_TIMESTAMP</code> date functions are not supported.

### 4.7 GROUP BY, HAVING

Partially supported

You can only use the GROUP BY and HAVING syntax supported for SOQL. For more details, see [GROUP BY](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_soql_select_groupby.htm) and [HAVING](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_soql_select_having.htm).

### 4.8 SELECT Clause

Supported, except the DISTINCT keyword in JPQL is not supported.

However, you can use a GROUP BY clause without an aggregated function in SOQL to query all the distinct values, including null, for an object. The following query returns the distinct set of values stored in the Country field for the User entity.

<pre>
<code>
SELECT Country
FROM User
GROUP BY Country
</code>
</pre>

#### 4.8.1 Result Type of the SELECT Clause

Supported

#### 4.8.2 Constructor Expressions in the SELECT Clause

Supported

#### 4.8.3 Null Values in the Query Result

Supported

#### 4.8.4 Embeddables in the Query Result

Supported

#### 4.8.5 Aggregate Functions in the SELECT Clause

Supported

#### 4.8.6 Numeric Expressions in the SELECT Clause

Not supported

### 4.9 ORDER BY Clause

Supported

### 4.10 Bulk Update and Delete Operations

Bulk delete is supported but it doesn't participate in existing transactions. It runs in its own transaction. Bulk update is not supported. For more details, see [Bulk Delete and Queries](jpa-queries#bulkDelete).

### 4.11 Null Values

Supported

### 4.12 Equality and Comparison Semantics

Supported

## 5 Metamodel API

Not supported

## 6 Criteria API

Not supported

## 7 Entity Managers and Persistence Contexts

### 7.2 Obtaining an EntityManager

Only application-managed entity managers are supported. Obtaining an Entity Manager in a J2EE container is not supported.

### 7.3 Obtaining an EntityManagerFactory

Supported in Java SE; not supported in Java EE

### 7.5 Controlling Transactions

JTA is not supported. Resource-local is supported and the EntityTransaction interface is supported.

### 7.6 Container-managed Persistence Contexts

Supported

### 7.7 Application-managed Persistence Contexts

Supported

### 7.8 Requirements on the Container

Not supported

### 7.9 Runtime Contracts between the Container and Persistence Provider

Not supported

### 7.10 Cache Interface

Not supported

### 7.11 PersistenceUnitUtil Interface

Supported

## 8 Entity Packaging

### 8.1 Persistence Unit

Supported

### 8.2 Persistence Unit Packaging

Supported. There are additional properties in `persistence.xml` and there is some behavior specific to Database.com. For more details, see [Configuring the JPA Provider](jpa-config-persistence).

## 9 Container and Provider Contracts for Deployment and Bootstrapping

### 9.1 Java EE Deployment

Not supported

### 9.2 Bootstrapping in Java SE environments

Supported. For details on configuring database connections, see [Connection Configuration](connection-url).

### 9.3 Determining the Available Persistence Providers

Supported

### 9.4 Responsibilities of the Persistence Provider

Works as specified except Java EE is not supported

### 9.5 javax.persistence.spi.PersistenceUnitInfo Interface

Supported

### 9.6 javax.persistence.Persistence Class

Supported

### 9.7 PersistenceUtil Interface

Supported

## 10 Metadata Annotations

### 10.1 Entity

Supported

### 10.2 Callback Annotations

Supported

### 10.3 Annotations for Queries

### 10.3.1 NamedQuery Annotation

Supported

#### 10.3.2 NamedNativeQuery Annotation

Supported

**Note**: A native query uses SOQL. SQL is not supported.

#### 10.3.3 Annotations for SQL Query Result Set Mappings

Supported

**Note**: A native query uses SOQL. SQL is not supported.

### 10.4 References to EntityManager and EntityManagerFactory

Supported

## 11 Metadata for Object/Relational Mapping

### 11.1 Annotations for Object/Relational Mapping

#### 11.1.1 Access Annotation

Supported

#### 11.1.2 AssociationOverride Annotation

Not supported

#### 11.1.3 AssociationOverrides Annotation 

Not supported

#### 11.1.4 AttributeOverride Annotation 

Not supported

#### 11.1.5 AttributeOverrides Annotation 

Not supported

#### 11.1.6 Basic Annotation 

Supported

#### 11.1.7 Cacheable Annotation 

Not supported

#### 11.1.8 CollectionTable Annotation 

Not supported. Use <code>@OneToMany</code> and <code>@ManyToOne</code> instead.

#### 11.1.9 Column Annotation 

Partially supported

The <code>columnDefinition</code> and <code>table</code> attributes are not supported.

#### 11.1.10 DiscriminatorColumn Annotation 

Supported

#### 11.1.11 DiscriminatorValue Annotation 

Supported

#### 11.1.12 ElementCollection Annotation 

Supported

#### 11.1.13 Embeddable Annotation 

Supported

#### 11.1.14 Embedded Annotation 

Supported

#### 11.1.15 EmbeddedId Annotation 

Not supported

#### 11.1.16 Enumerated Annotation 

Supported

#### 11.1.17 GeneratedValue Annotation 

Partially supported. You must use the <code>GenerationType.IDENTITY</code> strategy.

#### 11.1.18 Id Annotation 

Partially supported. The ID must be a String data type. For more details, see [Primary Keys](jpa-provider#primary-keys).

#### 11.1.19 IdClass Annotation 

Not supported

#### 11.1.20 Inheritance Annotation 

Partially supported. You must use the <code>InheritanceType.SINGLE_TABLE</code> strategy.

#### 11.1.21 JoinColumn Annotation 

Not supported

#### 11.1.22 JoinColumns Annotation 

Not supported

#### 11.1.23 JoinTable Annotation 

Not supported

#### 11.1.24 Lob Annotation 

Not supported

#### 11.1.25 ManyToMany Annotation 

Not supported

#### 11.1.26 ManyToOne Annotation 

Supported with some limitations. For more details, see [Relationship Fields](database-com-datatypes#relFields). 

#### 11.1.27 MapKey Annotation 

Supported

#### 11.1.28 MapKeyClass Annotation 

Supported

#### 11.1.29 MayKeyColumn Annotation 

Supported

#### 11.1.30 MapKeyEnumerated Annotation 

Not supported

#### 11.1.31 MapKeyJoinColumn Annotation 

Not supported

#### 11.1.32 MapKeyJoinColumns Annotation 

Not supported

#### 11.1.33 MapKeyTemporal Annotation 

Not supported

#### 11.1.34 MappedSuperclass Annotation 

Supported

**Note**: The AttributeOverride, AttributeOverrides, AssociationOverride, and AssociationOverrides annotations are not supported.

#### 11.1.35 MapsId Annotation 

Not supported

#### 11.1.36 OneToMany Annotation 

Supported with some limitations. For more details, see [Relationship Fields](database-com-datatypes#relFields).

#### 11.1.37 OneToOne Annotation 

Not supported

#### 11.1.38 OrderBy Annotation 

Supported

#### 11.1.39 OrderColumn Annotation 

Not supported

#### 11.1.40 PrimaryKeyJoinColumn Annotation 

Not supported

#### 11.1.41 PrimaryKeyJoinColumns Annotation 

Not supported

#### 11.1.42 SecondaryTable Annotation 

Not supported

#### 11.1.43 SecondaryTables Annotation 

Not supported

#### 11.1.44 SequenceGenerator Annotation 

Not supported

#### 11.1.45 Table Annotation 

Supported

#### 11.1.46 TableGenerator Annotation 

Not supported

#### 11.1.47 Temporal Annotation 

Supported

#### 11.1.48 Transient Annotation 

Supported

#### 11.1.49 UniqueConstraint Annotation 

Not supported

#### 11.1.50 Version Annotation 

Supported with limitations. For more details, see [Transaction Properties](jpa-config-persistence#transProps). 

## 12 XML Object/Relational Mapping Descriptor

Not supported
