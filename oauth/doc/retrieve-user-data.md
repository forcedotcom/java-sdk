---

layout: doc
title: Retrieving User Data

---
# Retrieving User Data

After configuring the Database.com OAuth Connector for your application, you can access information about the authenticated user in your code. Use the <code>get()</code> method in the static ForceSecurityContextHolder object to return an object that implements the SecurityContext interface:

	SecurityContext sc = ForceSecurityContextHolder.get();

SecurityContext has getters that expose:

- User Id
- User Name (optional)
- Database.com Session Id
- Database.com Refresh Token
- Database.com Authentication Endpoint
- User Language
- User Locale
- User Timezone
- Organization Id

You can also retrieve the user's role as a string. The role stored in the SecurityContext defaults to a single value unless you override this functionality, as described in [Customizing User Data Retrieval](#customUserData).

The storage of the username is optional and can be turned off if data privacy is a concern. See the OAuth or Spring Security documentation for more information.


<a name="customUserData"> </a>
## Customizing User Data Retrieval

The default behavior of the Database.com OAuth Connector is to retrieve a minimal amount of data about the authenticated user. However, you can customize this behavior using standard OAuth or Spring Security OAuth integration.

The mechanism for extending the user data retrieval functionality is the same for both approaches. The only difference is how the custom class gets configured or injected into the framework.

To customize user data retrieval, you must extend the CustomUserDataRetriever and CustomSecurityContext classes. Your custom data retriever must implement a method that retrieves the user data and returns a CustomSecurityContext that can subsequently be used in your code.

Here is a simple example of these two classes:

	/**
	 * The security context is where you store your custom user data.
	 * This object will also make the default SecurityContext values available to you.
	 */
	public class SampleSecurityContext extends CustomSecurityContext {

		String sampleValue;

		public String getSampleValue() {
			return sampleValue;
		}

		public void setSampleValue(String sampleValue) {
			this.sampleValue = sampleValue;
		}
	}

	/**
	 * The data retriever must implement the retrieveUserData() method, which handles the custom
	 * data retrieval logic
	 */
	public class SampleUserDataRetriever extends CustomUserDataRetriever<SampleSecurityContext> {

		@Override
		public SampleSecurityContext retrieveUserData() {
			SampleSecurityContext ssc = new SampleSecurityContext();
			//retrieve other data and store it in your security context
			return ssc;
		}

	}

Your custom security context can store whatever data you like. The SecurityContext interface provides a getter and setter for the role field. If you're using Spring Security, the role controls page access by default.

You can also use it with any other framework that you integrate the Database.com OAuth Connector with. As long as you can call <code>getRole()</code> and feed the value into your framework of choice it will work.

If you don't customize the user data loading, the value is defaulted to "ROLE_USER". The CustomSecurityContext base class provides a setter for this field so you can set it in the <code>retrieveUserData()</code> method. You can also override the <code>getRole()</code> method in your custom security context to implement role logic.

### Configuring A Custom User Data Retriever

Once you've extended CustomUserDataRetriever and CustomSecurityContext, you need to configure your OAuth application to use the sub-classes. This configuration depends on whether you're using Spring Security.

#### Standard OAuth Connector

If you aren't using Spring Security, you must provide the name of your CustomUserDataRetriever in the filter definition of your application's `web.xml`:

	<filter>
		<filter-name>AuthFilter</filter-name>
		<filter-class>com.force.sdk.oauth.AuthFilter</filter-class>
			 <init-param>
			 	<param-name>url</param-name>
			 	<param-value>URL or a ${Java system property} or ${environment variable}</param-value>
			 </init-param>
			 <init-param>
			 	<param-name>customDataRetriever</param-name>
			 	<param-value>com.salesforce.oauthsample.context.SampleUserDataRetriever</param-value>
			</init-param>
	</filter>

The second <code>init-param</code> element defines the fully qualified name of the SampleUserDataRetriever object.

#### Spring Security

If you're using Spring Security, you can inject your CustomUserDataRetriever into the framework via the Force.com Spring Security namespace or through standard configuration. The custom namespace looks like this:


    <!-- SFDC OAuth security config -->
    <fss:oauth logout-from-sfdc="true" />
	<fss:oauthInfo endpoint="https://login.salesforce.com" oauth-key="sfdc.oauthKey"
	oauth-secret="sfdc.oauthSecret" />
	<fss:customUserDataRetriever ref="sampleUserDataRetriever"/>
    </fss:oauth>

Substitute values for the `sfdc.oauthKey` and `sfdc.oauthSecret` placeholders.

    <bean id="sampleUserDataRetriever" class="com.force.samples.SampleUserDataRetriever"/>

The <code>fss:customUserDataRetriever</code> tag accepts a reference to a bean that is defined as your SampleUserDataRetriever class.

This can also be done without the Force.com security namespace by adding the following to your `spring-configuration.xml` file:

    <bean id="oauthConnector" class="com.force.sdk.oauth.connector.ForceOAuthConnector">
        <property name="connectionName" value = "sampleConnectionName" />
        <property name="userDataRetrievalService" ref="customDataRetrievalService"/>
    </bean>
    
    <bean id="customDataRetrievalService" class="com.force.sdk.oauth.userdata.CustomUserDataRetrievalService">
		<constructor-arg name="customDataRetriever" ref="sampleDataRetriever"/>
    </bean>
    
    <bean id="sampleDataRetriever" class="com.force.samples.SampleUserDataRetriever"/>

In this case, you must define one additional bean. The SampleUserDataRetriever class gets wired into the CustomUserDataRetrievalService, which is wired into the oauthConnector bean that you already have.

## Session Management and Integration with The Database.com API Connector

Because of the highly scalable nature of cloud applications, you need to carefully plan how user data is stored and made persistent across requests. By default, the Database.com SDK works with stateless application instances. The user data that is stored as part of the default SecurityContext and your custom SecurityContext is encrypted and stored in a browser cookie. 

Alternatively, you can configure your application to use server-side sessions. However, server side sessions have drawbacks and should only be used in environments where sticky load balancing is available.

In either case, the cookie or session store is treated simply as a data cache. User authentication always occurs through the OAuth handshake and results in the creation of a session. User data is loaded through an API call that takes place after the handshake. Custom user data logic is executed at the same time.

Returning users are recognized by the presence of a session id and their user data in either the browser cookie or server-side session. However, if this data isn't present, the user is authenticated with an OAuth handshake and their data is automatically loaded by the user-data retrieval flow.

### Configuring Session Management

User data is stored in browser cookies by default. However, you can use server-side sessions instead. The configuration method you'll need to use to do this depends on whether you use the Spring Security integration. 

#### Standard OAuth Connector

To use server side sessions, you need to add an additional <code>init-param</code> to your AuthFilter declaration:

	<init-param>
		<param-name>securityContextStorageMethod</param-name>
		<param-value>session</param-value>
	</init-param>

#### Spring Security

If you're using the Force.com security namespace, add the <code>store-data-in-session</code> attribute to the oauth tag to configure session-based user-data storage.

    <!-- SFDC OAuth security config -->
    <fss:oauth logout-from-sfdc="true" store-data-in-session="true"/>
        <fss:oauthInfo endpoint="https://login.salesforce.com" oauth-key="sfdc.oauthKey"
	    oauth-secret="sfdc.oauthSecret" />
        <fss:customUserDataRetriever ref="sampleUserDataRetriever"/>
    </fss:oauth>

Substitute values for the `sfdc.oauthKey` and `sfdc.oauthSecret` placeholders.

If you're not using the namespace, you need to change the class that you wire into the securityContextStorageServiceBean. The example above shows SecurityContextCookieStore wired into the bean. To use server sessions to store user data, change it to SecurityContextSessionStore:

	<bean id="securityContextStorageService" class="com.force.sdk.oauth.context.store.SecurityContextSessionStore"/>
