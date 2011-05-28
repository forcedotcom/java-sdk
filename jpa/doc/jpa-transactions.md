---

layout: doc
title: Creating, Updating, and Deleting Data

---
# Transactions
A transaction is a set of operations that either succeeds or fails completely. This allows recovery from error conditions to a
known state and avoids cleanup necessary from a partially successful operation where some records are persisted, while others
are not persisted due to errors.

The Database.com JPA provider supports transactions with a read-committed isolation level. This means that data modified in a transaction is not visible to a query until <code>commit()</code> is called on the transaction.

Before using transactions in your code, you must configure transaction properties in your application's `persistence.xml`
file. See [Transaction Properties](jpa-config-persistence#transProps).

In JPA, a transaction is started by calling the <code>begin()</code> method and committed by calling <code>commit()</code>. The following snippet
of code assumes that you've already established an <code>EntityManager</code> object, em, and populated a new student record.

    // Instantiate a transaction
    EntityTransaction tx = em.getTransaction();
    tx.begin();
    em.persist(student);
    tx.commit();
    
This code snippet is intended to show the basic transaction syntax. If you are using Spring, you can use the <code>@Transactional</code> annotation on a method instead to delineate a transaction for the method.

If you are deleting a record, you should execute <code>find()</code> and <code>remove()</code> in the same transaction. 

Note: The Database.com JPA provider doesn't do anything when <code>flush()</code> is called on an <code>EntityManager</code>. Use the <code>@Transactional</code> annotation in Spring or <code>commit()</code> instead for transactions.
