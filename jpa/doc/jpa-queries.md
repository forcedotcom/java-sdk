---

layout: doc
title: Querying with JPQL

---
# Querying with JPQL

JPA supports Java Persistence Query Language (JPQL), a platform-independent object-oriented query language.

**Note**: Although JPA supports native queries with SQL, the Database.com JPA provider doesn't support SQL. Instead,
the Database.com JPA provider supports [native queries with SOQL](jpa-queries-soql). 

You can also bypass JPA and execute a [query() call](native-api) using the Web services API.

The most portable approach to writing queries is to use JPQL, but it is your
choice to use JPQL, SOQL, or the Web services API.

The JPQL syntax resembles SQL, but it executes against JPA entities rather than directly against database tables. Since JPQL is a JPA standard, it is the preferred approach for building queries with the Database.com JPA provider.

For complete information on JPQL syntax, see the [DataNucleus documentation](http://www.datanucleus.org/products/accessplatform/jpa/jpql.html).

The following sample shows a simple JPQL query and iteration of the query results.

    private void sampleJpqlQuery()
        throws Exception
    {
        EntityManagerFactory factory =
            Persistence.createEntityManagerFactory(persistenceUnitName);
        EntityManager em = factory.createEntityManager();
    
        try {
            String jpqlQuery = "SELECT u " +
                    "FROM User u WHERE u.FirstName = :firstName";
            Query q = em.createQuery(jpqlQuery);
            // Bind the named parameter into the query
            q.setParameter("firstName", "Bob");
    
            List<User> results = (List)q.getResultList();
            int size = results.size();
            User user;
            for (int i = 0; i < size; i++) {
                user = results.get(i);
                System.out.println("Email: " + user.getEmail());
                System.out.println("LastName: " + user.getLastName() + "\n");
            }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            em.close();
        }
    }
    
**Note**: A query that contains the entity alias in the SELECT clause returns all [eagerly fetched fields](#eagerVsLazy) for the entity. For example, this query returns all eagerly fetched fields for the User entity. 

    SELECT u FROM User u

<a name="bulkDelete"> </a>
### Bulk Delete and Queries
As well as querying records, JPQL supports deleting records.

**Note**: Records deleted with JPQL don't participate in all-or-nothing transactions or trigger cascading deletion for
child records. Don't use bulk delete with JPQL if you want the option to roll back the deletions as part of a transaction
or use cascade deletion. Use the <code>remove()</code> method in <code>EntityManager</code> to delete individual records instead.

To delete a set of records, call <code>executeUpdate()</code> on a <code>Query</code> object representing a delete operation. For example:

    public void sampleJpqlQueryDelete(EntityManager em)
        throws Exception
    {
        String jpqlDelete = "DELETE FROM Wine__c WHERE varietal = 'Zinfandel'";
        Query q = em.createQuery(jpqlDelete);
        int deletedRecords = q.executeUpdate();
    }
    
To delete a set of records without storing them in the Recycle Bin, set a hint on your query. For example:

    public void sampleJpqlQueryDelete(EntityManager em)
        throws Exception
    {
        String jpqlDelete = "DELETE FROM Wine__c WHERE varietal = 'Zinfandel'";
        Query q = em.createQuery(jpqlDelete).setHint(QueryHints.EMPTY_RECYCLE_BIN, true);
        int deletedRecords = q.executeUpdate();
    }
    
**Note**: Although JPQL supports a similar syntax for UPDATE, the Database.com JPA provider doesn't support updating records with JPQL.

## JPQL Date (Temporal) Functions
JPA supports date literals, such as [CURRENT_DATE](http://www.datanucleus.org/products/accessplatform/jpa/jpql_functions.html), in JPQL to perform comparisons with Date or Date/Time fields. For
example, the following JPQL query returns users that have logged in before today:

    SELECT Email, LastName FROM User WHERE LastLoginDate < CURRENT_DATE
    
**Note**: The Database.com JPA provider doesn't support the CURRENT_TIME and CURRENT_TIMESTAMP JPA date literals.

The Database.com JPA provider also supports [date literals](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_soql_select_dateformats.htm), such as TOMORROW, that aren't part of the JPA specification. The CURRENT_DATE JPA date literal is equivalent to the TODAY Database.com date literal.

To use Database.com date literals, use a query hint. For example:

    EntityManagerFactory factory =
        Persistence.createEntityManagerFactory(persistenceUnitName);
    EntityManager em = factory.createEntityManager();
    
    try {
        String jpqlQuery = "SELECT Email, LastName " +
                "FROM User WHERE LastLoginDate > CURRENT_DATE";
        Query q = em.createQuery(jpqlQuery).setHint(
                      QueryHints.CURRENT_DATE, "YESTERDAY");
        List<User> results = (List)q.getResultList();
    }
    catch (Exception e) {
        e.printStackTrace();
        throw e;
    }
    finally {
        em.close();
    }

To include multiple date literals in one query, use a <code>String[]</code> for the hint. The hints are substituted in the query in the order
they are listed in the <code>String[]</code>. For example:

    String jpqlQuery = "SELECT Email, LastName " +
        "FROM User " +
        "WHERE LastLoginDate < CURRENT_DATE AND LastLoginDate > CURRENT_DATE";
        Query q = em.createQuery(jpqlQuery).setHint(
                      QueryHints.CURRENT_DATE, new String[]{"TOMORROW", "YESTERDAY"});
        List<User> results = (List)q.getResultList();
        
The DataNucleus Access Platform supports date (temporal) functions, such as YEAR(dateField). These are not supported
by the Database.com JPA provider, which instead supports [date functions](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_soql_select_date_functions.htm), such as CALENDAR_YEAR(dateField). Use native queries with SOQL to work with the SOQL date functions.


<a name="jpqlJoins"> </a>
## JPQL Joins
You often want to retrieve related data from multiple objects in one query to avoid having to merge results
from multiple queries in your code. The Database.com JPA provider provides a few different ways to join data from related entities in one JPQL query.

+ [Implicit Joins](#implicitJoins)
+ [Explicit Joins](#explicitJoins)
+ [IN Joins](#inJoins)
+ [Semi-Joins and Anti-Joins](#semiAntiJoins)
+ [Relationship Joins](#relJoins)
+ [Child-Map Joins](#childMapJoins)

<a name="implicitJoins"> </a>
### Implicit Joins
The Database.com JPA provider automatically performs implicit joins on JQPL queries that reference a parent entity with an
<code>@OneToMany</code> field related to a collection or map of child entity records.

For example, a Producer entity with a one-to-many
relationship to a Wine entity could point to a collection of wines. If you query the Producer entity and retrieve the collection
of wines from the result set, the Database.com JPA provider creates an implicit join to fetch the collection of wines.

    // Exception handling omitted for brevity
    List<Producer> jpqlResult =
        (List<ParentTestEntity>)em.createQuery("SELECT p from Producer p)").getResultList();
    // Collection/Map records are lazily fetched by default.
    // The actual loading of the collection happens when the next line is executed.
    ArrayList<Wine> wines = (ArrayList<Wine>)jpqlResult.get(0).getWines();
    String wineName = wines.get(0).getName();
    
If you add the <code>FetchType.EAGER</code> attribute to the <code>@OneToMany</code> annotation in Producer.java, the collection of wines is
returned without the need for a second query in the background.

**Note**: If you use a native SOQL query instead of JPQL, there is no implicit join; you would have to write a sub-query
referencing the parent-to-child relationship.

#### Ordering with @OrderBy
An implicit join returns the elements in a collection or map in random order. To order the results, add an <code>@OrderBy</code> annotation
to the <code>@OneToMany</code> field. For example, the following <code>@OrderBy</code> annotation sorts the elements in the collection by the name
field in the Wine entity:

    @OneToMany(mappedBy="producer")
    @OrderBy(value="name ASC")
    private Collection<Wine> wines;

<a name="AnnotationJoinFilter"> </a>
#### Filtering with @JoinFilter
The custom Database.com <code>@JoinFilter</code> annotation enables implicit queries to include a WHERE clause to filter the returned
child collection or map. The following code shows a collection field. When the collection is returned in an implicit query, the
child joined entity is aliased as w and the query results are filtered by the <code>w.name LIKE 'Chateau%'</code> WHERE clause.

    @OneToMany(mappedBy="producer")
    @JoinFilter(alias="w", value="w.name LIKE 'Chateau%'")
    private Collection<Wine> wines;

<a name="explicitJoins"> </a>
### Explicit Joins
You can use the JOIN syntax in JPQL queries to perform explicit joins that navigate a parent-to-child or a child-to-parent
relationship. In this sample, a ParentEntity has relationships to ChildEntity and User entities. The query joins to the other
entities using the relationship fields.

    SELECT p
    FROM ParentEntity p
        JOIN p.childEntities c
        JOIN p.ownerId o
    WHERE c.name = 'sample1')
        AND o.username LIKE 'bob%'
        
**Note**: The Database.com JPA provider always performs outer joins, so there is no difference in the query results if you
use JOIN or LEFT OUTER JOIN.

<a name="inJoins"> </a>
### IN Joins
In some cases, you can use an IN clause instead of JOIN. For example:

    SELECT p
    FROM ParentEntity p
        IN (o.childEntities) c
    WHERE c.name = 'sample1'
    
This query is equivalent to a similar query using the JOIN syntax.

    SELECT p
    FROM ParentEntity p
        JOIN (o.childEntities) c
    WHERE c.name = 'sample1'

<a name="semiAntiJoins"> </a>
### Semi-Joins and Anti-Joins
A semi-join is a subquery on another object in an IN clause to restrict the records returned. For example:

    SELECT p
    FROM ParentEntity p
    WHERE id IN (
        SELECT c.parent FROM ChildEntity c
        WHERE c.name = 'sample1'
    )
    
An anti-join is a subquery on another object in a NOT IN clause to restrict the records returned. For example:

    SELECT p
    FROM ParentEntity p
    WHERE id NOT IN (
        SELECT c.parent FROM ChildEntity c
        WHERE c.name = 'sample1'
    )
    
For more information on semi- and anti-joins in SOQL, see the [Web Services API Developer's Guide](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_soql_select_comparisonoperators.htm#semi_and_anti).    


<a name="relJoins"> </a>
### Relationship Joins
Relationships between entities in Database.com are represented by a lookup or master-detail field in a child entity. You can't
create relationships with other field types. For more information, see [Relationship Fields](database-com-datatypes#relFields).

For each relationship between objects, there is a relationshipName property that enables you to traverse the relationship
in a query. For more information about relationships, see [Understanding Relationship Names](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_soql_relationships.htm).

In a JPQL query, you can navigate child-to-parent relationships using a dot notation to perform a join. For example, the
c.ParentEntity.name notation in the following query navigates the child-to-parent relationship to reference the name field
in the parent entity.

    SELECT c
    FROM ChildEntity c
    WHERE c.ParentEntity.name in ('Parent1', 'Parent2')
    
**Note**: You can't navigate parent-to-child relationships like you can in native SOQL queries. However, it's easier to
take advantage of implicit joins in JPQL queries for querying parent-to-child relationships.

For more information about the number of levels of relationships that you can traverse in a query, see [Fetch Depth](#fetchDepth).

<a name="childMapJoins"> </a>
### Child-Map Joins
If you are joining a parent entity to a child entity represented by a map, you can use the <code>key()</code>, <code>value()</code>, and <code>entry()</code>
functions in a JPQL query to select and filter records. Let's look at MapParentEntity class with an <code>@OneToMany</code> children
field containing a map of child MapChildEntity records.

    @OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
    @MapKey(name="name")
    private Map<String, MapChildEntity> children;
    
#### key()
Use <code>key()</code> in the SELECT and WHERE to select and filter records based on the map's key. In this sample, <code>key()</code> is used in
the WHERE clause to return a filtered map.

    SELECT o
    FROM MapParentEntity o
        JOIN o.children c
    WHERE key(c) = 'sample1'
    
The next sample is similar and shows <code>key()</code> in the SELECT and WHERE clauses. The second column in the result is a <code>List\<String></code> which matches the WHERE clause filter.

    SELECT o.name, key(c)
    FROM MapParentEntity o
    JOIN o.children c
    WHERE key(c) = 'sample1'
    
#### value()
Use <code>value()</code> in a SELECT clause to return a List of values from the child collection. This sample returns
<code>List\<MapChildEntity></code> for <code>value()</code>.

    SELECT o.name, value(c)
    FROM MapParentEntity o
        JOIN o.children c
    WHERE key(c) = 'sample1'
    
#### entry()
Use <code>entry()</code> in a SELECT clause when you want to return the key-value pair for a map entry. This sample returns
<code>List\<Map.Entry\<String, MapChildEntity>></code> for <code>entry()</code>.

    SELECT o.name, entry(c)
    FROM MapParentEntity o
        JOIN o.children c
    WHERE key(c) = 'sample1'
    
Note the following when using these functions:  

- You can only use one of the <code>key()</code>, <code>value()</code>, and <code>entry()</code> functions in a single SELECT clause.
- Map joins are always eager joins so there is only one query used to return the keys, values, or entries.

<a name="isEmpty"> </a>
### IS EMPTY Comparison Operator
You can use the [NOT] IS EMPTY comparison operator to select parent entities whose children or multi-select picklist field values are empty.

The following examples use this field in a ParentEntity class:

    @OneToMany(mappedBy="parent", cascade=CascadeType.ALL)
    private Set<ChildTestEntity> children;

##### Child Entities
This query returns ParentEntity records that have no children.

    em.createQuery(
        "SELECT o FROM ParentEntity o
        WHERE o.children IS EMPTY"
    ).getResultList();

##### Multi-Select Picklists
This query returns Entity records with no picklist values in the **multiPickValue** field.

    em.createQuery(
        "SELECT o FROM Entity o
        WHERE o.multiPickValue IS EMPTY"
    ).getResultList();


<a name="memberOf"> </a>
### MEMBER OF Comparison Operator
You can use the [NOT] MEMBER OF comparison operator to select parent entities whose children or multi-select picklist field values match defined criteria.

##### Child Entities
The Database.com JPA provider uses the <b>name</b> standard field by default to filter for matching child entities.

This query returns ParentEntity records that contain at least one child whose name is 'entity1'.

    em.createQuery(
        "SELECT o FROM ParentEntity o
        WHERE 'entity1' MEMBER OF o.children"
    ).getResultList();

You can filter by a field other than **name** by using a <code>MEMBER_OF_FIELD</code> query hint. This query returns ParentEntity records that contain at least one child whose gender is 'Female' .

    em.createQuery(
        "SELECT o FROM ParentEntity o
        WHERE 'Female' MEMBER OF o.children"
    ).setHint(QueryHints.MEMBER_OF_FIELD, "gender").getResultList();

##### Multi-Select Picklists
This query returns Entity records that have a picklist value of 'ONE'  in the **multiPickValue** field.

    em.createQuery(
        "SELECT o FROM Entity o 
        WHERE 'ONE' MEMBER OF o.multiPickValue"
    ).getResultList();

Use a semicolon to check for multiple values in a picklist (boolean AND). Use a comma to check for one of multiple values (boolean OR). For example, this query returns Entity records that have a multiPickValue field including both ('AAA' and 'BBB') values or a 'CCC' value - ('AAA' && 'BBB') || 'CCC'.

    SELECT o FROM Entity o 
    WHERE 'AAA;BBB,CCC' MEMBER OF o.multiPickValue

    
<a name="eagerVsLazy"> </a>
## Eager Versus Lazy Fetch Types

There are two different fetch types for fields in a record returned by a query: lazy or eager. If a field has an eager fetch type,
it is loaded at the time the query is executed, and the field is populated in the query results. If a field has a lazy fetch type, the
query results don't include the data for the field. When the field is accessed by a getter method, for example a separate
query executes in the background and returns the actual field data.

Fields of the following data types are members of a default fetch group and are eagerly loaded by default.

<dl>
  <dt><b>Primitives</b></dt>
    <dd>
    <ul>
        <li>boolean</li>
        <li>byte</li>
        <li>char</li>
        <li>double</li>
        <li>float</li>
        <li>int</li>
        <li>long</li>
        <li>short</li>
    </ul>
    </dd>
  
  <dt><b>Object Wrappers for Primitives</b></dt>
    <dd>
    <ul>
        <li>Boolean</li>
        <li>Byte</li>
        <li>Character</li>
        <li>Double</li>
        <li>Float</li>
        <li>Integer</li>
        <li>Long</li>
        <li>Short</li>
    </ul>
    </dd>

  <dt><b>Others</b></dt>
    <dd>
    <ul>
        <li>String</li>
        <li>Number</li>
        <li>Enum</li>
        <li>BigDecimal</li>
        <li>BigInteger</li>
        <li>Date</li>
        <li>Calendar</li>
        <li>GregorianCalendar</li>
    </ul>
    </dd>
</dl>

These default eager data types are standard for DataNucleus, except for Calendar and GregorianCalendar, which are
eagerly loaded by the Database.com JPA provider to support optimistic transactions.

You can also explicitly mark a field as eager or lazy by adding the <code>FetchType.EAGER</code> or <code>FetchType.LAZY</code> attribute to an
<code>@Basic</code>, <code>@OneToMany</code>, or <code>@ManyToOne</code> annotation.

**Note**: A separate query is executed when you access the data for any lazily loaded field.

<a name="fetchDepth"> </a>
### Fetch Depth
Fetch depth is the number of levels of relationships traversed and fetched for a query. Consider a Grandchild entity with a
lookup relationship to a Child entity that has a lookup relationship to a Parent entity. If a query references the child-to-parent
relationships from the Grandchild to Child entities and the Child to Parent entities, you must set the fetch depth to two to
retrieve <code>FetchType.EAGER</code> fields for the Parent entity.

A query doesn't return an error if it references <code>FetchType.EAGER</code> fields that exceed the fetch depth; it just returns data
within the fetch depth. Setting fetch depth to zero means that you only get data for the entity in the FROM clause and relationships
are ignored.

The default fetch depth is one. You can change the default fetch depth by setting the **datanucleus.maxFetchDepth**
property in `persistence.xml`. You can also change the fetch depth for an individual query by using a query hint. For
example, to set the fetch depth to two in a JPQL query:

    em.createQuery("SELECT o FROM SampleEntity o)", SampleEntity.class)
        .setHint(QueryHints.MAX_FETCH_DEPTH, 2).getResultList();
        
Similarly, you can use a query hint in a <code>find()</code> method to set the fetch depth to two:

    em.find(SampleEntity.class, "entityIdHere",
        Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object)2 ));
        
For child-to-parent relationships, the maximum fetch depth is five.

**Caution:** A query containing a parent-to-child relationship can only traverse one relationship level. Setting query
depth greater than one on <code>FetchType.EAGER</code> parent-to-child relationships will result in an error.
