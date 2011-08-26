---

layout: doc
title: Native API Clients

---
# Native API Clients

The Database.com Service Connector gives you access to a set of native API connection classes for writing directly to the native Force.com APIs. For more details, see the Javadoc for ForceServiceConnector.

## PartnerConnection

The PartnerConnection class enables you to execute SOAP data API calls using the untyped, generic sObject type. If your application needs to perform operations on entities it does not know about, you can use the partner API to dynamically inspect entities and execute dynamically typed CRUD calls.

[Read more about the Partner API](http://www.salesforce.com/us/developer/docs/api/index.htm)

## MetadataConnection

The MetadataConnection class enables you to execute Metadata API calls to retrieve a bundle of XML files that represent your data model in Database.com. You can _transactionally_ deploy this set of XML files that describe your data model to another organization, such as a development, staging, or production environment. Think of it as transactional DDL.

[Read more about the Metadata API](http://www.salesforce.com/us/developer/docs/api_meta/index.htm)

## BulkConnection

The BulkConnection class enables you to execute Bulk API requests. The Bulk API is RESTful, and is optimized for asynchronously loading or deleting large sets of data.

[Read more about the Bulk API](http://www.salesforce.com/us/developer/docs/api_asynch/)

## API Queries

An alternative to using JPQL or SOQL queries is to bypass JPA and execute a [query()](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_calls_query.htm) call using the Web services API. The advantage of using JPQL or SOQL rather than a <code>query()</code> call is that JPA entities are
populated with the results of your query instead of requiring you to loop through a QueryResult object.

The following sample shows a simple query using the Web services API. It uses [WSC](http://code.google.com/p/sfdc-wsc/wiki/GettingStarted) as a Web services client and assumes
that you have logged in and established an EnterpriseConnection using the [enterprise WSDL](http://www.salesforce.com/us/developer/docs/api/index_Left.htm#StartTopic=Content/sforce_api_quickstart_intro.htm#enterprise_wsdl).

    private void sampleAPIQuery(EnterpriseConnection connection)
        throws Exception
    {
        try {
            String soqlQuery = "SELECT Email, LastName " +
                    "FROM User WHERE FirstName = 'John'";
            QueryResult queryResults = connection.query(soqlQuery);
            User user;
            if (queryResults.getSize() > 0) {
                for (int i=0; i < queryResults.getRecords().length; i++) {
                    // cast the SObject to a User object
                    User user = (User)queryResults.getRecords()[i];
                    System.out.println("Email: " + user.getEmail());
                    System.out.println("LastName: " + user.getLastName() + "\n");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
