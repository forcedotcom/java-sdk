---

layout: doc
title: Quick Start

---
# Quick start

This quick start walks you through creating a new Spring MVC application from a template that defines a simple JPA entity stored in Database.com. You can use this template as a starting point for your own web application.

## Prerequisites

* Username, password and [security token][1] for a [Force.com Developer Edition account][2] or other system administrator account on a Salesforce organization.
* [Maven 3](http://maven.apache.org/download.html)
* [A recent JDK 1.6][4], like 1.6.0_24

[1]: http://na1.salesforce.com/help/doc/en/user_security_token.htm
[2]: http://www.developerforce.com/events/regular/registration.php?d=70130000000EjHb
[3]: http://maven.apache.org/download.html
[4]: http://www.oracle.com/technetwork/java/javase/downloads/index.html

### A word about Maven before you get started

Many of the commands in this quick start trigger downloads of additional Maven dependencies. If you have never used Maven before or if you are going through this quick start on a new machine or with a new user account, this can be slow initially. However, once the modules are downloaded, they are cached in a local repository and subsequent commands execute more quickly.

## 1. Create a new project from a template

Navigate to the directory where you want to create the new project and create it from a Maven archetype by executing the following command:

    mvn archetype:generate -DarchetypeGroupId=com.force.sdk -DarchetypeArtifactId=springmvc-archetype
    
You will be prompted for a couple of properties for your project. You only need to fill out two: groupId and artifactId. Set groupId to the Java package name for your code and artifactId to your application's name:

    Define value for property 'groupId': : org.docsample
    Define value for property 'artifactId': : hellocloud

Press **Enter** for the **version** and **package** options to select the default values. Finally, press **Enter** again to confirm your property selections.

Both groupId and artifactId are Maven specific terms, but it is a common practice to map groupId to your Java package name and artifactId to your application name. Maven creates a new project in a directory with the same name as the artifactId. 

## 2. Set the connection URL for Database.com

The template application that you just created defines a simple JPA entity in a persistence unit called `forceDatabase`. The Database.com JPA provider will look for a connection URL in an environment variable called `FORCE_<UPPERCASE_PERSISTENCE_UNIT_NAME>_URL`; in this case, `FORCE_FORCEDATABASE_URL`.

For Linux, set this variable by running the following command after replacing the user and password with your own username and password. Remember to append your security token to the password and to enclose the URL in quotes.

    export FORCE_FORCEDATABASE_URL="force://login.salesforce.com?user=scott@acme.com&password=tigerVXoAIbgYSMOhSEVtcGxgt4mRP"

For Windows, set this variable by running the following command after replacing the user and password with your own username and password.

    set FORCE_FORCEDATABASE_URL=force://login.salesforce.com?user=scott@acme.com^&password=tigerVXoAIbgYSMOhSEVtcGxgt4mRP

Notice that & has been escaped as ^& for configuring on Windows.

For more information about connection URLs, see [Connection URLs](connection-url).

## 3. Build and run your application

Building your application is simple. First, navigate into the `hellocloud` directory for your template application.

    cd hellocloud

Next, build the project and start a Tomcat server running the `hellocloud` application.

    mvn tomcat:run-war

Navigate to <http://localhost:8080/hellocloud> where you should see a welcome screen. When Tomcat started up, the Database.com JPA provider automatically created a new entity in Database.com called MyEntity. You can create new records of this entity from the "Create a MyEntity Record" link.

## 4. Next Steps

The Database.com SDK is not tied to any particular runtime. You can now proceed to deploy the application on your own server, Heroku, EC2, or other cloud runtimes as long as you can make HTTPS connections to Database.com.

See [Running your quick start application on Heroku](quick-start-on-heroku) for instructions to get your application running on Heroku.

If you want to take a look at an application that is built out a little bit more, you can look at the "Music Library" application here: <https://github.com/forcedotcom/javasample-musiclib>. You can clone this project by running:

    git clone git://github.com/forcedotcom/javasample-musiclib.git

If Tomcat is still running your quick start application, stop the server.

Now, build and run the musiclib application.

    mvn tomcat:run-war

And see it at <http://localhost:8080/musiclib>. It will pick up the same connection parameters that you already set up and create entities in the same Force.com organization.
