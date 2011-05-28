---

layout: doc
title: OAuth Authentication

---
# OAuth Authentication

Your Java applications can use Force.com user accounts for authentication and identity management. The Force.com Java SDK simplifies the process of authenticating users in Force.com using the OAuth protocol. 

## Force.com OAuth Connector

The Force.com OAuth Connector is a library of classes that handles most of the details of the OAuth handshake for you.

<a name="createRAA"> </a>
### Prerequisite:Creating a Force.com Remote Access Application

Before you can use OAuth to authenticate users for your application, you must create a remote access application in Force.com. This generates the consumer key and secret that you'll need for the OAuth configuration. 

1. Log in at `https://login.salesforce.com`.
- Click *Your Name* > **Setup** > **Develop** > **Remote Access**.
- Click **New**.
- Enter your application name in the Application field and your email in the Contact Email field.
- Enter a Callback URL for your application. This is the URL that the user will be returned to after they approve access for the application. To use the Force.com OAuth Connector, your callback URL must be https://\<applicationName>.\<host>/\_auth, where \<applicationName> and \<host> point to where your application is hosted. If you're testing your application on your localhost, the URL can be http://localhost:\<port>/\<applicationName>/_auth, where \<port> is the port used on the localhost. If you are running your application somewhere other than localhost, your URL must use HTTPS.
- Click **Save**.

You'll now see your application in the list of remote access applications. Click on your application to see the details and retrieve the automatically generated consumer key and secret. The consumer key and secret are the values that you'll use for {$oauthKey} and {$oauthSecret} when you configure the Force.com OAuth Connector in your application.

### Configuring the Force.com OAuth Connector

To use the connector, add the following servlet filter to your application's `web.xml` file:

	<!-- Enables Security -->
	<filter>
		<filter-name>AuthFilter</filter-name>
		<filter-class>com.force.sdk.oauth.AuthFilter</filter-class>
			 <init-param>
			 	<param-name>connectionName</param-name>
			 	<param-value>nameOfConnectionToUse</param-value>
			</init-param>


			 <!-- Optional parameters -->
			 <init-param>
			 	<param-name>securityContextStorageMethod</param-name>
			 	<param-value>typeOfSecurityContextStorageMethod</param-value>
			</init-param>
			 <init-param>
			 	<param-name>secure-key-file</param-name>
			 	<param-value>name Of Key-File</param-value>
			</init-param>
			 <init-param>
			 	<param-name>storeUsername</param-name>
			 	<param-value>set to false to not store user name in the security context.</param-value>
			</init-param>
	</filter>
	<filter-mapping>
		<filter-name>AuthFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

The OAuth Connector uses the Force.com API Connector to access the Force.com APIs. The <code>connectionName</code> is used to look up OAuth properties defined in an environment variable, or a Java system property, or in a properties file on the classpath. For more information, see [Force.com Database Connections](connection-url). Other <code>init-param</code> values can be configured to customize behavior:

- <code>securityContextStorageMethod</code> - Control whether data about the authenticated user is stored in a server side session or an encrypted browser cookie. The default is <code>cookie</code>. Set this to <code>session</code> to use sessions. Sessions should only be used if sticky load balancing is available or if the application runs with a single instance.
- <code>secure-key-file</code> - AES encryption is used to encrypt the data about the authenticated user when it is stored in a browser cookie. This is only used if browser cookie storage is on. If cookies are used and no file is specified, a key is automatically generated. However, this should only be done for development purposes because it will be problematic in a multi-instance deployment since each instance will generate a different key. The key is base-64 encoded. For example, replace *yourKeyGoesHere* with a secure key in the following file. For more information on AES, see [Using AES with Java Technology](http://java.sun.com/developer/technicalArticles/Security/AES/AES_v1.html).

        # A valid key in base 64 encoding.   
        private-key=yourKeyGoesHere  

- <code>storeUsername</code> - Flag that sets whether or not the username is stored in the user data. This enables you to avoid storing usernames in browser cookies, but it can be used to prevent username storage in sessions too. Default is true.

The <code>filter-mapping</code> element above contains a url-pattern of "/\*". This redirects every URL through the filter. It is not required to do this. If your application requires only certain URL patterns to be authenticated, you can configure the filter to match a subset of requests. However, the filter must always include the "/\_auth\*" URL pattern. Otherwise, the OAuth callback won't be properly handled. For example, if you only wanted to check for authentication for "/Secure" your configuration would look like this:

	<filter-mapping>
		<filter-name>AuthFilter</filter-name>
		<url-pattern>/Secure</url-pattern>
		<url-pattern>/_auth*</url-pattern>
	</filter-mapping>

