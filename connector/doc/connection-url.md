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
  <code>force://login.salesforce.com?user=<em>user@salesforcedoc.org</em>&password=<em>samplePassword</em>&oauth_key=<em>xyz</em>&oauth_secret=<em>abc</em></code>
</pre>

Substitute values in the *user* and *password* fields with the username and password for the default integration user.

If you are using OAuth, substitute values in the *oauth_key* and *oauth_secret* fields with the OAuth consumer key and secret. To generate a consumer key and secret for your application, see [remote access applications](oauth-auth#RAA). 

There is also an optional *timeout* parameter, which is the number of milliseconds to wait to establish a connection before
timing out. This parameter is equivalent to the **datanucleus.datastoreReadTimeout** property in `persistence.xml`. This connection URL shows sample usage:
    
<pre>
  <code>force://login.salesforce.com?user=<em>user@salesforcedoc.org</em>&password=<em>samplePassword</em>&timeout=<em>10000</em></code>
</pre>

Each version of the Database.com Java SDK is automatically linked with an API version. The major version of the SDK matches the major version of the API. For example, version 22.0.0 of the SDK uses API version 22.0.

<a name ="configConnectionURL"> </a>
## Configuring Connection URLs

The connection URL is specified in your `persistence.xml` file. 

You can specify the connection URL as follows: 

     <property name="datanucleus.ConnectionURL" value="force://login.salesforce.com?user=user@salesforcedoc.org&amp;password=samplePassword" />

Notice that & has been escaped as &amp as & has special meaning in xml files. 

It's also possibly to put your connection URL in a system property or environment variable. Then you can reference it in your `persistence.xml` with dollar sign curly brace notation:

     <property name="datanucleus.ConnectionURL" value="${CONNECTION_URL}" />

The Database.com JPA provider will look for CONNECTION_URL first in Java system properties and if not found it will look into environment variables.

Note: the name of the variable is case sensitive so be sure that you have the case correct.

We recommend that you don't store any files that contain your connection url in version control. The connection url contains sensitive information. It will also vary from one environment to another so keeping it in version control makes your application lifecycle harder to manage. Using environment variables or system properties is the best way to avoid this.

The format for specifying the environment variable name is the same that is used by Spring's property placeholder configured. In a Spring based application Spring will try to resolve these values for you. This isn't an issue because system properties and environment variables are supported by Spring in the same way. However if you're using Spring be sure that you have the property placeholder configurer enabled. You can do this by including the following tag in your application context xml: 

    <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer" />
