---

layout: doc
title: Database.com JPA Provider

---
# Database.com JPA Provider

The Database.com JPA provider enables you to persist data in the database of your Force.com organization. JPA is an abstraction layer that lets you implement your persistence layer in standard Java code without having to focus on the specific APIs, terminology, or query language of Force.com.

If you're not familiar with JPA, you should start with [Oracle's Introduction to the Java Persistence API](http://download.oracle.com/javaee/6/tutorial/doc/bnbpz.html).

It's important to understand that Database.com is different in several aspects from standard
relational databases and the Database.com JPA provider reflects this difference. This section describes how the
Database.com JPA provider differs from other implementations of the JPA specification.

The Database.com JPA provider is built on top of [DataNucleus Access Platform](http://www.datanucleus.org/products/accessplatform/index.html). The Database.com JPA provider officially supports the properties listed in the sample [persistence.xml](jpa-config-persistence) file. Other properties that are part of the DataNucleus JPA provider haven’t been tested.

The Database.com Java SDK doesn't support JDO.

The Database.com JPA provider implements a [subset of the JPA 2.0 specification](jpa-spec-support).

## Data types

Database.com has many of the same data types as other relational databases, but the types and their
names are shaped by business needs. For example, there are Phone and URL types. In most cases, you don't have
to concern yourself with Database.com data types. Your Java types are translated automatically into the
appropriate Database.com data type.

You can read more about data types here:

* [Database.com data types](database-com-datatypes)
* [Data type mappings](java-db-com-datatypes-map)

<a name="primaryKeys"> </a>
## Primary Keys

Every entity in Database.com has a primary key that is an automatically generated string. A sample
primary key value is a0x30000000H7js.

The JPA standard allows you to define a variety of primary key types. These are not all supported by the
Database.com JPA provider, which has the following constraints for the primary key:

* The primary key Java type must be String
* Only the GenerationType.IDENTITY strategy is supported
* Composite primary keys are not supported
* Fields, including the primary key, must always start with a lowercase letter. You can't use Id instead of id.

You can declare your primary key as follows:

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    String id;

The id field name is a reserved system field and can't be used by non-primary-key fields.

If you want to use a field name other than id for the primary key field in your Java class, use the @Column
standard annotation. For example:

    @Id
    @Column(name="id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    String sfdcId;

**Note**: Primary keys in Database.com have two formats: a 15-character case-sensitive string and an
18-character case-insensitive string. API calls always return 18–character values, but API requests also accept
15-character values. If you use the Database.com JPA provider or an API call, you always use the 18-character
format.

## Entities

JPA entities are stored in Database.com as sObjects (the Database.com equivalent of a database table). The JPA provider automatically creates and updates the database schema based on the JPA entity classes in your application.

Most of the [standard JPA annotations](jpa-annotations-standard) work as expected and an additional set of [custom annotations](jpa-annotations-custom) are also available to customize behavior specific to Database.com.

## Relationships

Database.com supports both @ManyToOne and @OneToMany relationships. @OneToMany is not explicitly managed in the database. It is inferred from a @ManyToOne relationship in the opposite direction. Therefore you can't have an entity that references another entity with @OneToMany without also having the opposite @ManyToOne relationship. @OneToOne and @ManyToMany relationships are not supported.

## Transactions

The Database.com JPA provider uses the Web services API to access Database.com so transactions are
demarcated at the HTTP request boundary. A single transaction can't span multiple requests. Therefore, the full set of transaction semantics you would expect from a database is not available.

Read more:

* [Configuring transaction properties in persistence.xml](jpa-config-persistence)
* [Transactions](jpa-transactions)

## Queries

You can use JPQL to execute queries on your entities with some limitations. For example, you can't perform joins between entities that don't have an explicit relationship and you can't evaluate expressions.

Read more:

* [JPA query support](jpa-spec-support#4-query-language)
* [JPA query guide](jpa-queries)

## Resource Limits

Applications that store data in Database.com using the JPA provider are subjected to the same limits as any other use of your Force.com organization. The JPA provider uses the Web Services API and Metadata API. You can read about the limits for these APIs here:

* [Web Services API][1]
* [Metadata API][2]

### Concurrent, long-running API request limit

You can only execute a limited number of long-running, concurrent requests. The threshold for a long-running request is currently 20 seconds. If your limit is, for example, 25 requests and you currently have 25 active requests that have run for longer than 20 seconds, then you can't make any more requests.

### Total, daily API request limit

You can only execute a certain total number of API calls within a rolling 24 hour period. You can see your current consumption and limit by logging into your organization and clicking *Your Name* > **Setup** > (**Administration Setup**) > **Company Profile** > **Company Information** and looking at the **API Requests, Last 24 Hours** field. The first number is your current consumption. The second number in parentheses is your limit.

### Storage limit

You have limits on both file storage and record storage. File storage is consumed by storing documents and attachments. Record storage is a limit on the total number of records in all your entities. You can find your storage consumption and limits under *Your Name* > **Setup** > (**Administration Setup**) > **Data Management** > **Storage Usage**.

## Other limits

There are other limits that are specific to the operation you are performing. For example, if you are [inserting records](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_create.htm), you can only insert 200 records in one operation. You can read more about these limits in the API docs:

* [Web Services API][1]
* [Metadata API][2]

[1]: http://www.salesforce.com/us/developer/docs/api/index.htm
[2]: http://www.salesforce.com/us/developer/docs/api_meta/index.htm

