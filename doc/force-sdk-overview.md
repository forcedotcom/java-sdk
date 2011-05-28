---

layout: doc
title: Introduction

---
# Introduction

Database.com is the cloud database that powers Force.com and all the applications built on the platform, including Sales Cloud, Service Cloud and Chatter from salesforce.com. [TODO: need to understand Database.com positioning to see if this text (and text in the rest of this page) is ok. Work out whether we can use the term "Database.com" to mean the underlying database or whether it can only be used as the brand name for the product we're selling. Also, need to work out how we can talk about other clouds, CRM etc]

If you are a Force.com developer, you are already using Database.com. The cloud database manages the data model for your application, stores all data and automatically exposes your data in both a [REST API][1] and [SOAP API][2]. It also provides a [Metadata API][3] to handle changes to your data model.

[1]: http://developer.force.com/REST
[2]: http://www.salesforce.com/us/developer/docs/api/index.htm
[3]: http://www.salesforce.com/us/developer/docs/api_meta/index.htm

If you build your application using the Force.com drag-and-drop tools to define objects, layouts, workflows, you never really work with Database.com directly. [TODO: review for positioning]The service is transparently provided to you.

But what if you are a Java developer and you want to build a Java application that uses the same database? This is what the Database.com Java SDK is for. With this SDK you can:

* use Database.com as the database for application data
* interact with data already stored in Database.com, for example by one of your Force.com applications or by your CRM application
* leverage the Database.com user security model to manage data access for your application
* extend existing Force.com applications with Java logic

The SDK is not tied to any particular runtime. You can build apps that run on your own server, on Cloud Foundry, on EC2 or other cloud runtimes as long as you can make HTTPS connections to Database.com.

The SDK consists of the following components.

## API Connector

The SDK is built on the existing Database.com API. [TODO: @Jesper what is the existing Database.com API?]A core service connector class is responsible for managing connections to the API for the application. You configure this connector using a connection URL string which can be provided in property files, system properties, or environment variables. The connector automatically handles session refreshes after expiration and it can use the OAuth module to inject the session ID. The connector uses [WSC](http://code.google.com/p/sfdc-wsc) for the actual protocol implementation.

[Read more](connection-url)

## JPA Provider

The JPA provider enables you to use Database.com as a persistent data store for entities defined in Java using the JPA standard. You can define entities in pure Java, including related entities using one-to-many or many-to-one relationships, and the JPA provider automatically manages the schema in Database.com. It also enables you to map existing entities in Database.com to JPA entities so you can query and operate transactionally on data that's already there, such as your CRM entities. The JPA provider is based on the [DataNucleus Access Platform](http://www.datanucleus.org).

[Read more](jpa-provider)

## OAuth authentication and authorization

If you're building an application for authenticated Database.com users, you can configure single sign-on and leverage the security model in Database.com using OAuth2.

[Read more](force-security)

## Spring security plugin

This plugin enables you to use Spring Security for authentication and authorization in your applications. The Spring Security library uses the API Connector and the OAuth Connector to allow authentication via the Database.com APIs. There is also a custom Spring Security namespace that helps to simplify configuration.

[Read more](force-security)

## Spring MVC project template

The SDK includes a Spring MVC project template distributed as a Maven archetype. You can quickly and easily create new projects from this template. The template sets up all the Maven dependencies needed for a new Spring MVC application using the JPA provider and Spring Security plugin.
