/**
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.force.sdk.jpa;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.PersonEntity;
import com.force.sdk.jpa.entities.PhoneEntity;
import com.force.sdk.jpa.query.QueryHints;
import com.force.sdk.qa.util.BaseMultiEntityManagerJPAFTest;

/**
 * This class tests:-
 * 1) merging with lazy and eager attributes, relationship.
 * 2) behavior of getReference(...) with lazy, eager attributes.
 *
 * @author Nawab Iqbal
 */
public class MergeTest extends BaseMultiEntityManagerJPAFTest {

    private static String personAName = "Person A";
    private static String personBName = "Person B";
    private static String oldPhoneName = "oldPhoneName";
    private static String newPhoneName = "new";

    @Test
    public void testEagerLoadingWithCascadedMergeAsPersist() throws MalformedURLException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity person  = new PersonEntity();
        person.setName(personAName);
        PhoneEntity oldPhone = JPATestUtils.createPhoneEntity(oldPhoneName);
        tx.begin();
        em.persist(person);

        LinkedList<PhoneEntity> phoneList = new  LinkedList<PhoneEntity>();
        phoneList.add(oldPhone);
        person.setMorePhonesEager(phoneList);
        oldPhone.setSecondOwner(person);

        em.persist(oldPhone);
        tx.commit();
        
        person = em.find(PersonEntity.class, person.getId());
        em.detach(person);
        Assert.assertEquals(personAName, person.getName());
        Assert.assertNotNull(person.getMorePhonesEager());
        person.getMorePhonesEager().get(0).setType("older");
        person.getMorePhonesEager().get(0).setName("older");
        PhoneEntity newPhone = JPATestUtils.createPhoneEntity(newPhoneName); // adding a new child
        newPhone.setSecondOwner(person);
        tx.begin();
        person.getMorePhonesEager().add(newPhone);
        em.merge(person);
        tx.commit();
        
        person = em.find(PersonEntity.class, person.getId());
        Assert.assertNotNull(person.getMorePhonesEager());
        Assert.assertEquals(person.getMorePhonesEager().size(), 2);
        JPATestUtils.verifyContains(person.getMorePhonesEager(), new String[] {"older", newPhoneName});
    }

    protected void verifyCascadedMergeAsPersist(Boolean loadLazy)
    throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException,
           NoSuchMethodException, IllegalAccessException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity personEntity  = new PersonEntity();
        personEntity.setName(personAName);
        PhoneEntity phoneEntity = JPATestUtils.createPhoneEntity(oldPhoneName);
        LinkedList<PhoneEntity> phoneEntities = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(personEntity);
        phoneEntities.add(phoneEntity);
        personEntity.setPhoneList(phoneEntities);
        phoneEntity.setPhoneOwner(personEntity);
        em.persist(phoneEntity);
        tx.commit();

        personEntity = em.find(PersonEntity.class, personEntity.getId());
        if (loadLazy) {
            personEntity.getPhoneList();
        }
        em.detach(personEntity);
        Assert.assertEquals(personAName, personEntity.getName());

        PhoneEntity newPhone = JPATestUtils.createPhoneEntity(newPhoneName);
        newPhone.setPhoneOwner(personEntity);

        if (loadLazy) {
            Assert.assertNotNull(personEntity.getPhoneList());
            personEntity.getPhoneList().add(newPhone);
        } else {
            JPATestUtils.assertDetachedFieldException(personEntity, "getPhoneList");
            
            phoneEntities = new  LinkedList<PhoneEntity>();
            phoneEntities.add(newPhone);
            personEntity.setPhoneList(phoneEntities);
        }
        
        tx.begin();
        em.merge(personEntity);
        tx.commit();
       
        personEntity = em.find(PersonEntity.class, personEntity.getId());
        Assert.assertNotNull(personEntity.getPhoneList());
        Assert.assertEquals(personEntity.getPhoneList().size(), 2);
        JPATestUtils.verifyContains(personEntity.getPhoneList(), new String[] {newPhoneName, oldPhoneName});
    }
        
    /*
     * By 'forced', I mean loading of an attribute by calling getter; when it is originally marked for lazy loading.  
     */
    @Test
    public void testForcedLoadingWithCascadedMergeAsPersist()
    throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException,
           NoSuchMethodException, IllegalAccessException  {
        verifyCascadedMergeAsPersist(true);
    }
    
    @Test
    public void testLazyLoadingWithCascadedMergeAsPersist()
    throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException,
           NoSuchMethodException, IllegalAccessException  {
        verifyCascadedMergeAsPersist(false);
    }
        
    @Test
    public void testForcedLazyLoadingWithCascadedMerge() throws MalformedURLException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity personEntity  = new PersonEntity();
        personEntity.setName(personAName);
        PhoneEntity phoneEntity = JPATestUtils.createPhoneEntity(oldPhoneName);
        LinkedList<PhoneEntity> oppList = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(personEntity);
        oppList.add(phoneEntity);
        personEntity.setPhoneList(oppList);
        phoneEntity.setPhoneOwner(personEntity);
        em.persist(phoneEntity);
        tx.commit();
        em.clear();
        
        personEntity = em.find(PersonEntity.class, personEntity.getId(),
                Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object) 2));

        // TODO: this should be fixed by w-970625; Eager doesn't work well with FetchDepth > 2.
        //personEntity = em.find(PersonEntity.class, personEntity.getId(),
        //        Collections.singletonMap(QueryHints.MAX_FETCH_DEPTH, (Object) 3));

        personEntity.getPhoneList();
        em.detach(personEntity);
        Assert.assertEquals(personAName, personEntity.getName());
        Assert.assertNotNull(personEntity.getPhoneList());
        
        PhoneEntity p = personEntity.getPhoneList().get(0);
        p.setName("new phone");
        p.setType("new type");
        // TODO: this will work after the ManyToOne issue is fixed.
        //Assert.assertNotNull(p.getPhoneOwner());

        tx.begin();
        em.merge(personEntity);
        tx.commit();
        
        personEntity = em.find(PersonEntity.class, personEntity.getId());
        Assert.assertNotNull(personEntity.getPhoneList());
        Assert.assertEquals(personEntity.getPhoneList().size(), 1);
        PhoneEntity o = personEntity.getPhoneList().get(0);
        JPATestUtils.verifyPhoneEntity(o, newPhoneName);
        //Assert.assertNotNull(o.getPhoneOwner(), "Person is null.");
        //Assert.assertEquals(o.getPhoneOwner(), personEntity, "Account references are not same.");
    }
    
    /*
     * From spec: 
     * The persistence provider must not merge fields marked LAZY that have not been fetched: 
     * it must ignore such fields when merging.
     */
    @Test
    public void testLazyLoadingWithCascadedMerge()
    throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException,
           NoSuchMethodException, IllegalAccessException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity personEntity  = new PersonEntity();
        personEntity.setName(personAName);
        PhoneEntity phoneEntity = JPATestUtils.createPhoneEntity(oldPhoneName);
        LinkedList<PhoneEntity> oppList = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(personEntity);
        oppList.add(phoneEntity);
        personEntity.setPhoneList(oppList);
        phoneEntity.setPhoneOwner(personEntity);
        em.persist(phoneEntity);
        tx.commit();
        
        personEntity = em.find(PersonEntity.class, personEntity.getId());
        em.detach(personEntity); // since personEntity has been lazy loaded, the phones list will be null.
                         // And will NOT be merged according to spec.  
        Assert.assertEquals(personAName, personEntity.getName());
        JPATestUtils.assertDetachedFieldException(personEntity, "getPhoneList");

        PhoneEntity newPhone = JPATestUtils.createPhoneEntity(newPhoneName);
        oppList = new  LinkedList<PhoneEntity>();
        oppList.add(newPhone);
        newPhone.setPhoneOwner(personEntity);
        personEntity.setPhoneList(oppList);

        tx.begin();
        em.merge(personEntity);
        tx.commit();

        personEntity = em.find(PersonEntity.class, personEntity.getId());
        Assert.assertNotNull(personEntity.getPhoneList());
        Assert.assertEquals(personEntity.getPhoneList().size(), 2);
        JPATestUtils.verifyContains(personEntity.getPhoneList(),  new String[] {newPhoneName, oldPhoneName});
    }

    @Test
    public void testPersistWithReference() throws MalformedURLException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity person  = new PersonEntity();
        person.setName(personAName);
        PhoneEntity oldPhone = JPATestUtils.createPhoneEntity(oldPhoneName);

        LinkedList<PhoneEntity> phoneEntities = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(person);
        em.flush();
        tx.commit();
        em.detach(person);
        
        tx.begin();
        person = em.getReference(PersonEntity.class, person.getId());
        phoneEntities.add(oldPhone);
        person.setPhoneList(phoneEntities);
        oldPhone.setPhoneOwner(person);
        em.persist(oldPhone);
        tx.commit();
        
        person = em.find(PersonEntity.class, person.getId());
        Assert.assertEquals(personAName, person.getName());
        Assert.assertNotNull(person.getPhoneList());
        Assert.assertEquals(person.getPhoneList().size(), 1);
        
        List<PhoneEntity> l = person.getPhoneList();
        PhoneEntity o = (PhoneEntity) l.toArray()[0];
        JPATestUtils.verifyPhoneEntity(o, oldPhoneName);
        // TODO: This will work after ManyToOne issue is fixed.
        //Assert.assertNotNull(o.getPhoneOwner(), "Person is null.");
        //Assert.assertEquals(o.getPhoneOwner(), person, "Person references are not same.");
    }

    @Test
    public void testGetReference()
    throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException,
           NoSuchMethodException, IllegalAccessException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity personEntity  = new PersonEntity();
        personEntity.setName(personAName);
        personEntity.setType("Lazy: do not fetch for getRef");
        URL lazyURL = new URL("http://www.lazy.com");
        URL eagerURL = new URL("http://www.eager.com");
        personEntity.setLazyURL(lazyURL);
        personEntity.setEagerURL(eagerURL);
        
        PhoneEntity opp = JPATestUtils.createPhoneEntity(oldPhoneName);
        LinkedList<PhoneEntity> oppList = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(personEntity);
        em.flush();
        tx.commit();
        em.detach(personEntity);
        
        tx.begin();
        personEntity = em.find(PersonEntity.class, personEntity.getId());
        personEntity.setPhoneList(oppList);
        opp.setPhoneOwner(personEntity);
        em.persist(opp);
        tx.commit();
        em.detach(personEntity); // No op
        
        personEntity = em.getReference(PersonEntity.class, personEntity.getId());
        em.detach(personEntity);
        
        // name is eager.
        // However, I am confused if eager attributes should also throw exception 
        // because of getReference was used (as opposed to find)
        personEntity.getName();
        personEntity.getEagerURL(); // same as above
        
        JPATestUtils.assertDetachedFieldException(personEntity, "getType");
        JPATestUtils.assertDetachedFieldException(personEntity, "getPhoneList");
        JPATestUtils.assertDetachedFieldException(personEntity, "getLazyURL");
    }
    
    @Test
    /* From the spec:
     *  - If X is a detached entity, the state of X is copied onto a pre-existing managed entity instance X'
     *    of the same identity or a new managed copy X' of X is created. <<= I interpret it as: the id and 
     *    the complete state is copied into the persistence context.
     *  
     *  - If X is a new entity instance, a new managed entity instance X' is created and the state of X
     *    is copied into the new managed entity instance X'.
     *  
     *  A detached entity instance is an instance with a persistent identity that is not (or no longer) 
     *  associated with a persistence context.
     */
    public void testMergeWithValidKey() {
        PersonEntity person = new PersonEntity();
        person.setName(personAName);
        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(person);
        tx.commit();
          
        PersonEntity newPerson = new PersonEntity();
        newPerson .setId(person.getId());
        newPerson .setName(personBName);
        
        tx = em.getTransaction();
        tx.begin();
        newPerson  = em.merge(newPerson);
        tx.commit();
        
        Assert.assertTrue(newPerson.getId() != null, "Id is null. Account object was not persisted by merge().");
        
        //TODO: Currently, we need to find() before merge (but this is how Hibernate works as well).
        //      Following will pass once we are able to merge without find().
        //Assert.assertEquals(newPerson.getId(), person.getId(), "Ids don't match. New Person object was created by merge().");
        //PersonEntity a1 = em.find(PersonEntity.class, person.getId());
        //Assert.assertEquals(a1.getName(), newPerson.getName(), "New name was not merged by merge().");
    }

    
    @Test
    /**
     * If X is a removed entity instance, an IllegalArgumentException will be thrown 
     * by the merge operation (or the transaction commit will fail).
     */
    public void testMergeWithInvalidKey() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setName("acct 1");
        personEntity.setId("some invalid key");
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.merge(personEntity);
        tx.commit();
        
        Assert.assertTrue(personEntity.getId() != null, "Id is null. Account object was not persisted by merge().");
        Assert.assertNotSame(personEntity.getId(), "some invalid key");
    }

    @Test
    public void testPersistWithInvalidKey() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setName("acct 1");
        personEntity.setId("some invalid key");
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(personEntity);
        tx.commit();
        Assert.assertTrue(personEntity.getId() != null, "Id is null. Account object was not persisted by merge().");
    }

    @Test
    /* From spec:
     * If X is a new entity instance, a new managed entity instance X' is created 
     * and the state of X is copied into the new managed entity instance X'.
     */
    public void testMergeAsPersist() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setName("acct 1");
        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.merge(personEntity);
        tx.commit();
          
        Assert.assertTrue(personEntity.getId() != null, "Id is null. Account object was not persisted by merge().");
        
        PersonEntity a1 = em.find(PersonEntity.class, personEntity.getId());
        Assert.assertEquals(a1.getName(), personEntity.getName(), "name was not persisted by merge().");
        
        // verify that two objects are different.
        Assert.assertNotSame(a1, personEntity, "New object was not created by merge().");
    }
    
    @Test
    public void testMergeInSeparateTx() {
        PersonEntity personEntity = new PersonEntity();
        personEntity.setName("testMergeInSeparateTx");
        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(personEntity);
        tx.commit();
        
        String persistPersonEntityId = personEntity.getId();
        personEntity.setName("testMergeInSeparateTxUpdate");
        
        tx = em.getTransaction();
        tx.begin();
        em.merge(personEntity);
        tx.commit();
        
        Assert.assertEquals(personEntity.getId(), persistPersonEntityId,
                                "Created new entity in separate transaction merge.");
    }
    
}
