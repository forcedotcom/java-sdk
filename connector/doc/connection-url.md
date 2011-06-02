---

layout: doc
title: Connection Configuration

---
# Connection Configuration

Java applications run with the permissions and sharing of the user that authenticates the application with Database.com. You can configure a JPA connection URL that sets a default integration user to authenticate your application with Database.com. This allows a connection to the database before users authenticate with their own credentials.

If your application requires a user to log in to use some or all of the application, the application runs with the permissions and sharing of the user after they log in.

The Database.com Java SDK includes an OAuth Connector that simplifies the process of using OAuth to authenticate Database.com users.

## Typical Web Application Authentication Scenarios

This section describes some typical authentication scenarios for Web applications. It is not an exhaustive list and you can use a different scheme for authenticating your applications.

### Application Allows Anonymous Access

Any user can access the application without authentication.

You must configure JPA connection information for the default integration user that authenticates your application with Database.com. No OAuth credentials are needed since users don't need to authenticate.

### Application Requires User Authentication

A user must authenticate with their Database.com credentials before accessing the application.

You must configure JPA connection information for the default integration user that authenticates your application with Database.com. This user is used when your application needs to create schema in Database.com. You must configure OAuth credentials if you want to use OAuth to authenticate users.

### Application Requires a Mix of User Authentication and Anonymous Access

A user can access some application pages without authentication, but they must authenticate with their Database.com credentials before accessing protected application pages.

You must configure JPA connection information for the default integration user that authenticates your application with Database.com. This user is used when your application needs to connect to Database.com. You must configure OAuth credentials if you want to use OAuth to authenticate users.

The next section describes the various options for configuring your JPA connection or OAuth credentials. These options offer you flexibility for managing your configuration information in different environments. For example, you may want to manage authentication information differently in development, staging, and production environments.

You can encode the authentication information in a URL or you can split the information into a few separate properties.

## Connection URLs

You can encode the JPA connection information for the default integration user and the OAuth credentials in a connection URL using key-value pairs. A connection URL must include credentials in the *username* and *password* parameters. If you are using OAuth, also include the *oauth_key* and *oauth_secret* parameters.

The format of a connection URL that includes JPA connection information for the default integration user and OAuth credentials is:

<pre>
  <code>force://login.salesforce.com;user=<em>user@salesforcedoc.org</em>;password=<em>samplePassword</em>;oauth_key=<em>xyz</em>;oauth_secret=<em>abc</em></code>
</pre>

Substitute values in the *user* and *password* fields with the username and password for the default integration user.

If you are using OAuth, substitute values in the *oauth_key* and *oauth_secret* fields with the OAuth consumer key and secret. To generate a consumer key and secret for your application, see [remote access applications](oauth-auth#RAA). 

There is also an optional *timeout* parameter, which is the number of milliseconds to wait to establish a connection before
timing out. This parameter is equivalent to the **datanucleus.datastoreReadTimeout** property in `persistence.xml`. This connection URL shows sample usage:
    
<pre>
  <code>force://login.salesforce.com;user=<em>user@salesforcedoc.org</em>;password=<em>samplePassword</em>;timeout=<em>10000</em></code>
</pre>

Each version of the Database.com Java SDK is automatically linked with an API version. For example, version 22.0.0 of the SDK
uses API version 22.0. For JPA functionality, the major version of the SDK must always match the API version. To use a native API connection class to override the default API version for the SDK, see <a href="native-api#setAPIversion">Setting an API Version</a>.

<a name ="configConnectionURL"> </a>
## Configuring Connection URLs

There are multiple options for configuring a connection URL. These options offer you flexibility for managing your configuration information in different environments. For example, you
may want to manage authentication information differently in development, staging, and production environments.

The Database.com JPA provider looks for a connection URL in the following order. The first matching configuration is used.

1. [Connection URL in Environment Variable](#UrlEnvVar)
1. [Connection URL in Java System Property](#UrlJavaSysProp)
1. [Connection URL in Properties file in the Classpath](#propFile)

<a name="UrlEnvVar"> </a>
### Connection URL in Environment Variable

You can set the connection URL in an environment
variable. The name of the environment variable depends on values in your application's [`persistence.xml`](jpa-config-persistence#authProps) file. If a <code>persistence-unit</code> element contains a **force.ConnectionName** property, the name depends on the property value; otherwise, it depends on the <code>persistence-unit</code> name.

For example, if you have a <code>\<persistence-unit name="persistenceUnitName"></code> element, set the authentication
information for this persistence unit in an environment variable named FORCE\_*PERSISTENCEUNITNAME*\_URL. However, if the <code>persistence-unit</code> contains <code>\<property name="force.ConnectionName" value="forceDatabase"/></code>, the environment variable name should be FORCE\_*FORCEDATABASE*\_URL.

**Note**: The environment variable name must contain all uppercase characters including the name of the
<code>persistence-unit</code> element or the **force.ConnectionName** property converted to uppercase characters.

If you use an environment variable to set the authentication information, you must include <code>\<property
name="datanucleus.storeManagerType" value="force"/></code> in the <code>persistence-unit</code> element in your application's
`persistence.xml` file.

<a name="UrlJavaSysProp"> </a>
### Connection URL in Java System Property

You can set the connection URL in a Java system
property. The name of the Java system property depends on values in your application's `persistence.xml` file. If a
<code>persistence-unit</code> element contains a **force.ConnectionName** property, the name of the Java system property depends
on the **force.ConnectionName** property value; otherwise, it depends on the <code>persistence-unit</code> name.

For example, if you have a <code>\<persistence-unit name="persistenceUnitName"></code> element, set the authentication
information for this persistence unit in a Java system property named force.*persistenceUnitName*.url.

**Note**: The Java system property name is case sensitive.

The following code shows you how to set the authentication information in a Java system property:

    System.setProperty("force.persistenceUnitName.url",
        "force://login.salesforce.com;user=user@salesforcedoc.org;password=samplePassword");
        
If you use a Java system property to set the authentication information, you must include <code>\<property
name="datanucleus.storeManagerType" value="force"/></code> in the <code>persistence-unit</code> element in your application's
`persistence.xml` file.        

<a name="propFile"> </a>        
### Connection Properties File

You can set the connection URL in a properties file in the classpath. If you add the properties file to the `src/main/resources` directory of your
application, Maven automatically copies it to your classpath.

The name of the properties file depends on values in your application's `persistence.xml` file. If a <code>persistence-unit</code>
element contains a **force.ConnectionName** property, the file name depends on the property value; otherwise, it depends
on the <code>persistence-unit</code> name.

For example, if you have a <code>\<persistence-unit name="persistenceUnitName"></code> element, set the authentication
information for this persistence unit in a *persistenceUnitName*.properties file in the classpath.

**Note**: The property file name and lookup of property names in the file are case sensitive.

A file containing this line is an example of a properties file containing a url property.

    url=force://login.salesforce.com;user=user@salesforcedoc.org;password=samplePassword

<!-- Comment out until release cliforce. Uncomment this section and add a link to the bullet list earlier in the file when ready.
<a name="UrlConnectionsPropFile"> </a> 

You can set the connection URL in the $HOME/.force/cliforce_urls file, where $HOME refers to your home directory. The format is:

    persistenceUnitName=force://login.salesforce.com;user=user@salesforcedoc.org;password=samplePassword

-->
