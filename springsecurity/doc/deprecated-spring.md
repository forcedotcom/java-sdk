# Database.com Spring Security Integration


Besides &lt;connectionUrl />, other options to provide OAuth info are  &lt;oauthInfo/> or &lt;connectionName /> tag. Note that these tags are mutually exclusive and you can't provide more than one or you will receive an error on application startup. 

**&lt;oauthInfo/>** tag.  This tag allows you to specify an <code>endpoint</code>, <code>oauthKey</code> and <code>oauthSecret</code> as separate attributes. For example:

	<fss:oauth>
		<fss:oauthInfo endpoint="https://login.salesforce.com" oauth-key="${oauthKey}" oauth-secret="${oauthSecret}" />
	</fss:oauth>

**&lt;connectionName />** tag.  This tag allows you to define OAuth properties elsewhere and have them looked up by name.  OAuth properties can be stored as an environment variable, or a Java system property, or a properties file on the classpath.  For more information, see [Database.com Database Connections](connection-url). For example:

    SET FORCE_MYCONNECTOR_URL=force://${endpoint}?oauth_key=${oauthKey}&oauth_secret=${oauth_secret}

    <!-- Uses the connection URL in the FORCE_MYCONNECTOR_URL environment variable -->
    <fss:oauth>
        <fss:connectionName name="myconnector" />
    </fss:oauth>

In these samples, substitute values from your remote access application for the following variables:

- ${oauthKey} - Specifies the consumer key for the application.
- ${oauthSecret} - Specifies the consumer secret for the application.

For more information about remote access applications, see [Creating a Database.com Remote Access Application](oauth-auth#createRAA).

