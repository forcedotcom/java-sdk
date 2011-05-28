---

layout: doc
title: Java and Database.com Terminology

---
# Java and Database.com Terminology

The Database.com Java SDK enables you to model Java classes or entities as plain old Java objects (POJOs) that are automatically mapped to Database.com. The SDK does this mapping by using Java Persistence API ( JPA), which is a specification for managing Java objects in a database. The aim of JPA is to simplify persisting Java objects using transactions and retrieving them from a database.

The Java SDK communicates with Database.com using the Web services API, but this happens under the covers. You
can write Java code using JPA without having to learn about the Web services API. However, it is helpful to understand the
terminology used for Database.com and how it maps to Java concepts that you are already familiar with.

In Java terminology, a class maps to a JPA entity that is stored in a database. In Database.com terminology, these are called Database.com entities,
which are equivalent to Web services API objects.

API objects represent database tables. Objects already created for you by Database.com are called
standard objects. Objects you create in your organization are called custom objects.

A record is a particular occurrence of an API object, similar to a Java object being a particular occurrence of a Java class.

A Java class consists of a set of fields. Similarly a Database.com entity or Web services API object has a set of fields. These fields
are the equivalent of the columns in a database table.

Standard objects have a set of standard fields, while custom objects have a set of custom fields. For example, a Wine Java class
could have name and year fields that would map to custom fields in the Wine custom object.

This table summarizes the mapping of terminology between Java and Database.com. If any table cell has more than one entry,
the terms are equivalent.

<table border="1">
<tr>
    <th>Java Terminology</th>
    <th>Database.com Terminology</th>
</tr>

<tr>
    <td>
        <ul>
            <li>Java class</li>
            <li>JPA entity</li>
        </ul>
    </td>
    <td>
        <ul>
            <li>Web services API object (standard or custom)</li>
            <li>Database.com entity</li>
        </ul>
    </td>
</tr>
<tr>
    <td>Java field</td>
    <td>field (standard or custom)</td>
</tr>
<tr>
    <td>Java object</td>
    <td>record</td>
</tr>

</table>
