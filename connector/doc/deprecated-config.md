## Configuring Connection URLs

[TODO @Nawab]: notice that all the other files have a layout: doc header. you should include that.
[TODO @Nawab]: you need a title explaining what this page is. you just copied the content from somewhere else and changed the file name. is this all deprecated behavior? there is nothing in the title or content of this page that indicates that behavior is deprecated. you can't rely on the file name to impart this.
[TODO @Nawab]: this doc explains usage of env variables and system props better than the bits you added. you need to take the relevant parts fro this page and put it back in the main doc.

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
        "force://login.salesforce.com?user=user@salesforcedoc.org&password=samplePassword");
        
If you use a Java system property to set the authentication information, you must include <code>\<property
name="datanucleus.storeManagerType" value="force"/></code> in the <code>persistence-unit</code> element in your application's
`persistence.xml` file.        

<a name="propFile"> </a>        
### Connection URL in Properties file in the Classpath

You can set the connection URL in a properties file in the classpath. If you add the properties file to the `src/main/resources` directory of your
application, Maven automatically copies it to your classpath.

The name of the properties file depends on values in your application's `persistence.xml` file. If a <code>persistence-unit</code>
element contains a **force.ConnectionName** property, the file name depends on the property value; otherwise, it depends
on the <code>persistence-unit</code> name.

For example, if you have a <code>\<persistence-unit name="persistenceUnitName"></code> element, set the authentication
information for this persistence unit in a *persistenceUnitName*.properties file in the classpath.

**Note**: The property file name and lookup of property names in the file are case sensitive.

A file containing this line is an example of a properties file containing a url property.

    url=force://login.salesforce.com?user=user@salesforcedoc.org&password=samplePassword

<!-- Comment out until release cliforce. Uncomment this section and add a link to the bullet list earlier in the file when ready.
<a name="UrlConnectionsPropFile"> </a> 

You can set the connection URL in the $HOME/.force/cliforce_urls file, where $HOME refers to your home directory. The format is:

    persistenceUnitName=force://login.salesforce.com?user=user@salesforcedoc.org&password=samplePassword

-->

