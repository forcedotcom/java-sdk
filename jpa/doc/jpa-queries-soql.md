---

layout: doc
title: Querying with SOQL

---
# Querying with SOQL
JPQL is the preferred approach for writing queries with the Database.com JPA provider. However, you can also use native queries with [SOQL](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_soql.htm), a query language optimized for querying Database.com entities.

There are three different approaches for executing SOQL queries.

+ [Returning SObject Records](#sobject)
+ [Returning Typed-Object Records](#typedObject)
+ [Returning Mixed-Object Records](#mixedObject)

<a name="sobject"> </a>
## Returning SObject Records
This first approach returns a list of SObject records.

    private void sampleSObjectSOQLQuery()
    {
        EntityManagerFactory factory =
            Persistence.createEntityManagerFactory(persistenceUnitName);
        EntityManager em = factory.createEntityManager();
    
        try {
            String soqlQuery = "SELECT Email, LastName " +
                    "FROM User WHERE FirstName = :firstName";
            Query q = em.createNativeQuery(soqlQuery);
            // Bind the named parameter into the query
            q.setParameter("firstName", "Bob");
    
            List<SObject> results = q.getResultList();
            int size = results.size();
            User user;
            for (int i = 0; i < size; i++) {
                user = (User)results.get(i);
                System.out.println("Email: " + user.getEmail());
                System.out.println("LastName: " + user.getLastName() + "\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            em.close();
        }
    }

<a name="typedObject"> </a>
## Returning Typed-Object Records
This second approach returns a list of records of an explicit SObject sub-type. The type of records in the list corresponds to
the type, in this case <code>User.class</code>, passed in as the second argument to the <code>createNativeQuery()</code> method.

    private void sampleObjectTypedSOQLQuery()
        throws Exception
    {
        EntityManagerFactory factory =
            Persistence.createEntityManagerFactory(persistenceUnitName);
        EntityManager em = factory.createEntityManager();
    
        try {
            String soqlQuery = "SELECT Email, LastName " +
                    "FROM User WHERE FirstName = :firstName";
            Query q = em.createNativeQuery(soqlQuery, User.class);
            // Bind the named parameter into the query
            q.setParameter("firstName", "Bob");
    
            List<User> results = q.getResultList();
            int size = results.size();
            User user;
            for (int i = 0; i < size; i++) {
                user = results.get(i);
                System.out.println("Email: " + user.getEmail());
                System.out.println("LastName: " + user.getLastName() + "\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            em.close();
        }
    }

<a name="mixedObject"> </a>
## Returning Mixed-Object Records
The third approach returns a list of records that can have different object types. This is used for cases where the query result
is used to create objects of multiple types. The <code>resultSetMapping</code> parameter passed in as the second argument to the
<code>createNativeQuery()</code> method points to a named mapping that defines the various types of objects returned.

## Relationship Queries
Relationship queries traverse parent-to-child and child-to-parent relationships between entities so that you can return data
or filter based on fields in multiple objects. Relationships are represented by a lookup or master-detail field in a child object.
You can't create relationships with other field types. For more information, see [Relationship Fields](database-com-datatypes#relFields).

In a SOQL query, you can navigate child-to-parent and parent-to-child relationships. For each relationship between entities,
there is a relationshipName property that enables you to traverse the relationship in a query. For more information about
relationships, see [Understanding Relationship Names](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_soql_relationships.htm).

The following sample uses a child entity, ChildEntity, that has a lookup relationship to a parent entity, ParentEntity. The
parent entity includes a childEntities field that is a Collection of ChildEntity records. The query in the sample
uses the <code>ParentEntity_ChildEntitys__r</code> relationship name, which represents the parent-to-child relationship in
ParentEntity. ChildEntity includes a boolType\_\_c custom field.

    private void sampleSOQLRelationshipQuery(EntityManager em)
        throws Exception
    {
        String soqlQuery = "SELECT id, name, " +
            "(SELECT id, boolType__c FROM ParentEntity_ChildEntitys__r) " +
            "FROM ParentEntity__c";
        Query q = em.createNativeQuery(soqlQuery, ParentEntity.class);
        List<SObject> results = q.getResultList();
        // Assume at least one result so can use get(0)
        ArrayList<ChildEntity> childEntities =
            (ArrayList<ChildEntity>) results.get(0).getChildEntities();
        boolean boolType1 = childEntities.get(0).getBoolType();
    }
    
For more information about the number of levels of relationships that you can traverse in a query, see [Fetch Depth](jpa-queries#fetchDepth).
