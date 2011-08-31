---

layout: doc
title: Running Your Quick Start Application on Heroku

---
# Running Your Quick Start Application on Heroku

Once you have run through the [Quick Start Tutorial](quick-start) you'll have a simple application that uses the Database.com SDK. If you want to run this application, or one that you have built using the same structure, in the cloud then one of your choices is the [Heroku](http://www.heroku.com) platform. This tutorial will show you how to run your application on Heroku.

## Getting Set Up To Use Heroku

You can sign up for an account at [Heroku's website](http://www.heroku.com).

You'll also find instructions on [Installing the Heroku gem](http://devcenter.heroku.com/articles/quickstart).

Make sure you have both of those things set up before continuing.

## Build Your App

Build your app locally:

    $ mvn package

## Store Your App in Git

Create a git repository for your application and do your initial commit.

    $ git init
    $ git add .
    $ git commit -m "init"

# Deploy to Heroku/Cedar

Create the app on the Cedar stack:

    $ heroku create --stack cedar
    Creating stark-sword-398... done, stack is cedar
    http://stark-sword-398.herokuapp.com/ | git@heroku.com:stark-sword-398.git
    Git remote heroku added

Deploy your code:

    $ git push heroku master
    Counting objects: 9, done.
    Delta compression using up to 4 threads.
    Compressing objects: 100% (5/5), done.
    Writing objects: 100% (9/9), 1.37 KiB, done.
    Total 9 (delta 0), reused 0 (delta 0)
    
    -----> Heroku receiving push
    -----> Java app detected
    -----> Installing Maven 3.0.3..... done
    -----> executing .maven/bin/mvn -B -Duser.home=/tmp/build_1cq2vqzdjg7yh -DskipTests=true clean install
           [INFO] Scanning for projects...
           [INFO]                                                                         
           [INFO] ------------------------------------------------------------------------
           [INFO] Building hellocloud 1.0-SNAPSHOT
           [INFO] ------------------------------------------------------------------------
           ...
           [INFO] ------------------------------------------------------------------------
           [INFO] BUILD SUCCESS
           [INFO] ------------------------------------------------------------------------
           [INFO] Total time: 25.671s
           [INFO] Finished at: Thu Aug 18 05:22:18 UTC 2011
           [INFO] Final Memory: 10M/225M
           [INFO] ------------------------------------------------------------------------
    -----> Discovering process types
           Procfile declares types -> web
    -----> Compiled slug size is 43.4MB
    -----> Launching... done, v1
           http://stark-sword-398.herokuapp.com deployed to Heroku

Looks good.  We can now visit the app with `heroku open`.


