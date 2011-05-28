---

layout: doc
title: Creating, Updating, and Deleting Data

---
# Creating, Updating, and Deleting Data

After you have modeled your entity in a Java class, you can
use it to create, update, or delete records for that entity type. First, you must get an <code>EntityManager</code>
instance.

## Using an EntityManager
An <code>EntityManager</code> instance is used to manage the state and life cycle of objects within a persistence unit. The entity manager
is responsible for creating and removing persistent entity instances and finding entities by their primary key or other query criteria.

You access an entity manager by first creating an instance of an <code>EntityManagerFactory</code> object for a persistence unit defined
in `persistence.xml`. Using the factory class, you can then create an <code>EntityManager</code> object.

The following sample creates an entity manager. The details of interacting with your data after creating the entity manager is
omitted.

    private void setupEntityManager(String persistenceUnitName)
    {
        EntityManagerFactory factory =
            Persistence.createEntityManagerFactory(persistenceUnitName);
        EntityManager em = factory.createEntityManager();
    
        try {
            // Use the entity manager to interact with your data
    
        }
        catch (PersistenceException pex) {
            pex.printStackTrace();
        }
        finally {
            em.close();
        }
    }

## Persisting Records
After you have instantiated an <code>EntityManager</code>, you can create, update, or delete records for entities with an associated Java class.

Here is a trivial sample that creates, updates, and then deletes a record for a Student entity. For more information on transactions, see [Transactions](jpa-transactions).

    private void sampleCreateUpdateDelete(String persistenceUnitName)
    {
        EntityManagerFactory factory =
            Persistence.createEntityManagerFactory(persistenceUnitName);
        EntityManager em = factory.createEntityManager();
    
        EntityTransaction tx;
        try {
            // Create a student object
            Student student = new Student();
            student.setFirstName("Fabio");
            student.setLastName("Socrates");
            student.setEmail("socrates@salesforcetest123.com");
            Calendar cal = Calendar.getInstance();
            cal.set(1990, 5, 1, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);// We do not preserve ms resolution
            Date dob = cal.getTime();
            student.setDateOfBirth(dob);
    
            // Instantiate a transaction
            tx = em.getTransaction();
            // Start the transaction for creating a new record
            tx.begin();
            em.persist(student);
            tx.commit();
    
            tx = em.getTransaction();
            tx.begin();
            // Read back the object
            // Note: you should execute find() and remove() in the same transaction
            Student foundStudent = em.find(Student.class, student.getId());
            
            // Now update the email field for the record
            foundStudent.setEmail(fabio.socrates@salesforcetest.com);
            em.merge(foundStudent);
    
            // Finally, delete the record
            em.remove(foundStudent);
            tx.commit();
    
        }
        catch (PersistenceException pex) {
            pex.printStackTrace();
        } finally {
            em.close();
            tx.rollback();
        }
    }
    
Note: Each custom Java entity has a name field. If you create a record that doesn't have a name value, the ID of the
record is inserted as its name.