---

layout: doc
title: Database.com Spring Security Integration

---
# Database.com Spring Security Integration

[Spring Security][ss] provides a comprehensive authentication and authorization solution for J2EE-based applications.

The Database.com Spring Security integration simplifies usage of the OAuth Connector with the Spring Security framework. You can take advantage of this if your application uses Spring Security.

The simplest way to configure a Spring application is to include the fss namespace in your `spring-configuration.xml` file.

	<?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns="http://www.springframework.org/schema/beans"
	       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	       xmlns:security="http://www.springframework.org/schema/security"
	       xmlns:fss="http://www.salesforce.com/schema/springsecurity"
	       xsi:schemaLocation="
		   http://www.springframework.org/schema/beans
		   http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
		   http://www.springframework.org/schema/security
		   http://www.springframework.org/schema/security/spring-security-3.0.xsd
		   http://www.salesforce.com/schema/springsecurity
		   http://media.developerforce.com/schema/force-springsecurity-1.1.xsd">

	    <!-- Database.com OAuth security config -->
	    <fss:oauth logout-from-sfdc="true" />
		<fss:oauthInfo endpoint="https://login.salesforce.com" oauth-key="${sfdc.oauthKey}"
		oauth-secret="${sfdc.oauthSecret}" />
	    </fss:oauth>

	    <!-- Configure Spring Security -->
	    <security:http>
		<security:anonymous />
	    </security:http>
	   
	</beans>

The main customizations of interest are:

- Include the namespace: xmlns:fss="http://www.salesforce.com/schema/springsecurity"
- Specify the schema location for the force-springsecurity XSD
- Add the \<fss:oauth /> tag
- Add the \<security:http /> tag. For more information about this tag, see [Spring Security documentation][ss].

The \<oauth /> tag requires that you provide OAuth properties.  This can be done using an &lt;oauthInfo/>, &lt;connectionUrl/>, or &lt;connectionName /> tag. Note that these tags are mutually exclusive and you can't provide more than one or you will receive an error on application startup. 

**&lt;oauthInfo/>** tag.  This tag allows you to specify an <code>endpoint</code>, <code>oauthKey</code> and <code>oauthSecret</code> as separate attributes. For example:

	<fss:oauth>
		<fss:oauthInfo endpoint="https://login.salesforce.com" oauth-key="${oauthKey} oauth-secret="${oauthSecret}" />
	</fss:oauth>

**&lt;connectionUrl/>** tag.  This tag allows you to combine the <code>endpoint</code>, <code>oauthKey</code> and <code>oauthSecret</code> properties into one connection URL.  For example:

	<fss:oauth>
            <fss:connectionUrl url="force://login.salesforce.com;oauth_key=${oauthKey};oauth_secret=${oauthSecret}" />
        </fss:oauth>

**&lt;connectionName />** tag.  This tag allows you to define OAuth properties elsewhere and have them looked up by name.  OAuth properties can be stored as an environment variable, or a Java system property, or a properties file on the classpath.  For more information, see [Database.com Database Connections](connection-url). For example:

      SET FORCE_MYCONNECTOR_URL=force://${endpoint};oauth_key=${oauthKey};oauth_secret=${oauth_secret}

      <!-- Uses the connection URL in the FORCE_MYCONNECTOR_URL environment variable -->
      <fss:oauth>
        <fss:connectionName name="myconnector" />
      </fss:oauth>

In these samples, substitute values from your remote access application for the following variables:

- ${oauthKey} - Specifies the consumer key for the application.
- ${oauthSecret} - Specifies the consumer secret for the application.

For more information about remote access applications, see [Creating a Database.com Remote Access Application](oauth-auth#createRAA).

The following attributes are optional for &lt;fss:oauth> in `spring-configuration.xml`:

| Attribute | Description |
| ------------ | ------ |
|default-login-success|A user is redirected to this URL after a successful OAuth logout. The default value is `/spring/logoutSuccess`|
|default-logout-success|A user is redirected to this URL after a successful OAuth logout. The default value is `/spring/logoutSuccess`|
|login-url|Navigation to this URL initiates a login sequence. The default value is `/spring/login`|
|logout-url|Navigation to this URL initiates a logout sequence. The default is `/spring/logout`|
|logout-from-sfdc|This attribute controls whether a logout from the OAuth application also logs the user out of Force.com. The default value is false|
|store-data-in-session|Flag that sets whether data about the authenticated user is stored in a server side session or an encrypted browser cookie. The default is false (cookies are used). Sessions should only be used if sticky load balancing is available or if the application runs with a single instance.|
|store-user-name|Flag that sets whether or not the username is stored in the user data. This enables you to avoid storing usernames in browser cookies, but it can be used to prevent storing the username in sessions too. The default value is true.|
|secure-key-file|The name of a secure key file, which must be on the classpath. AES encryption is used to encrypt the data about the authenticated user when it's stored in a browser cookie. This is only used if browser cookie storage is on. If cookies are used and no file is specified, a key is automatically generated. However, this should only be done for development purposes because it will be problematic in a multi-instance deployment since each instance will generate a different key. The key is base-64 encoded.|

The following is a sample file for the secure-key-file attribute. Replace *yourKeyGoesHere* with a secure key. For more information on AES, see [Using AES with Java Technology](http://java.sun.com/developer/technicalArticles/Security/AES/AES_v1.html).

    # A valid key in base-64 encoding.
    private-key=yourKeyGoesHere


[ss]: http://static.springsource.org/spring-security/site/docs/3.0.x/reference/springsecurity.html
