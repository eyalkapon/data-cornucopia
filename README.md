![Java 8 / 11 Build on Linux](https://github.com/jjsteenkamp/data-cornucopia/workflows/Java%20LTS%20Build%20on%20Linux/badge.svg)

![Java 8 / 11 Build on Windows](https://github.com/jjsteenkamp/data-cornucopia/workflows/Java%20LTS%20Build%20on%20Windows/badge.svg)

# Data-Cornucopia

This repository is a collection of data developments tools, examples and other miscellany useful in a variety of projects and consultancy arrangements.

The maturity and generic applicability of these projects vary significantly. For instance, some may very specific for a particular use-case and I have have not yet had the opportunity to expand and make it production ready.

Hopefully this will improve over time as feedback as derivations of these examples / projects are applied to real world problems.  

## XML-Stomper

There are often examples where data analysts need to 'flatten out' XML documents to a relational structure (say CSV file) in order to work with it in tools like R Studio or Python / Pandas. 

The XML-Stomper was a simple and somewhat limited script achieving that aim. Rather than use this tool, we recommend that you use the [xml-flattener](https://github.com/DevWorxCo/xml-flattener) project instead.

## Etcetera

A number of small relatively inconsequential programs and examples that may be useful to serve as 'mental' notes when building out larger applications.

Assuming you have a recent version of Java installed (i.e. Java 11+) then you should simply be able to call either the Mouse Mover or Mouse Clicker applications:

```
java etcetera/src/main/java/uk/co/devworx/etcetera/MouseMover.java
```

Or 

```
java etcetera/src/main/java/uk/co/devworx/etcetera/MouseClicker.java
```

## JDBC-Runner

A very simple and very tactical tool that can be used to execute a number of JDBC statements against a number of databases.

Was used as part of a project to do some data discovery.


## Spark Examples

A few simple [Apache Spark](https://spark.apache.org/) examples demonstrating basic functionality. These examples can certainly be found elsewhere on the web or in the Apache Spark tutorials - however, it is sometimes useful to have examples you have written yourself and understand better. That is the only way you are able to teach that to others.

Hopefully this section will grow over time.

## Excel Intregation
    
There are still a fair amount of companies out there that have a heavy reliance on the use of desktop Excel - in many cases a rather old version of excel. Yes, Financial Services - that basically means you.
    
This project contains a simple reference point that can convert from Excel (as was required on an ad-hoc basis by a number of customer projects.)

## PDF Utilities

This project contains some utilities to deal with PDF documents. It uses the excellent PDF Box library : https://pdfbox.apache.org/


