---

layout: doc
title: Set up SpringSource Tool Suite

---
# Set up SpringSource Tool Suite

If you've gone through the quick start guide, you have a basic project template for a Database.com Java web application. Now, you can build out this web application in an IDE.

The quick start application uses Maven as a build and dependency management tool. This makes it easy to import the project into most IDEs. Here, we will show you how to import and build the application in [SpringSource Tool Suite (STS)](http://www.springsource.com/developer/sts), a flavor of Eclipse enhanced with useful tools for Spring projects.

## 1. Install SpringSource Tool Suite

To download STS:

1. Navigate to <http://www.springsource.com/products/eclipse-downloads>.
1.  Download the STS version for your operating system.
1.  Follow the installation instructions on the download page.

**Note**: The easiest way to get started is to download the most recent of the SpringSource Tool Suite distributions that includes Eclipse. However, you can also install the SpringSource Tool Suite components and dependencies from an update site into an existing 3.5-based Eclipse installation on your computer.

## 2. Install the DataNucleus STS Plugin

The Database.com JPA provider uses the [DataNucleus Access Platform](http://www.datanucleus.org/products/accessplatform.html) implementation of the JPA specification.

To install DataNucleus Eclipse plugin: 

1. Click the SpringSource icon in the toolbar to open the Dashboard. 
- Click the Extensions tab at the bottom left of the Dashboard. 
- Search for DataNucleus and install the DataNucleus Eclipse Plugin. 
- Click Install at the bottom right of the Dashboard. 
- An Install popup window lists the items for installation. Click Next. 
- Review the installation details and click Next and Finish. 
- Restart SpringSource Tool Suite for the changes to take effect.

## 3. Import the Quick Start Sample Application

1. Once you have STS installed, launch it with a new workspace.
1. Click **File > Import**.
1. In the import source list, click **Maven > Existing Maven Projects** and then click Next.
1. Click **Browse** and navigate to the root folder of the `hellocloud` sample application you created in the [quick start](quick-start). The root folder contains pom.xml.
1. Click **OK** and you should see your pom.xml and project name in the Projects list.
1. Click **Finish**.

Your application should now show up in the Package Explorer and the first build will automatically run.

## 4. Run the Application Locally

After importing the project into STS, you can run the code on the [SpringSource tc Server](http://www.springsource.com/products/tcserver) that comes with STS. SpringSource tc Server is a drop-in replacement for Apache Tomcat, and it ensures a seamless migration path for existing custom-built and commercial software applications already certified for Tomcat.

You can run your application on a local Tomcat server and connect to Database.com. By running your application on a local server you can more quickly redeploy code and you can use standard debugging tools, such as the Eclipse debugger.

Using a cloud database for development means that you still have to be online to develop, but the benefit is that the database is managed for you and you won't have to deal with any incompatibility problems when you migrate from development to production. When you have tested locally, you can deploy your application wherever you want to host it.

To run your application on SpringSource tc Server:

1. Right click on the project name in the Package Explorer.
2. Click **Run As > Run On Server**.
3. Click **Finish**.

**Note**: If you are prompted to enable [Spring Insight](http://www.springsource.org/insight), choose No for this optional feature.

Your application server should start with your application running. If the application fails to start, you may need to run **Project > Clean**. 

You can access the application by navigating to `http://localhost:8080/hellocloud/` in a browser.
