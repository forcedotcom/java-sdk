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
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.PersonEntity;
import com.force.sdk.jpa.entities.PhoneEntity;
import com.force.sdk.test.util.BaseMultiEntityManagerJPAFTest;

/**
 * This class tests:-
 * 1) merging with lazy and eager attributes, relationship.
 * 2) behavior of getReference(...) with lazy, eager attributes.  
 * 
 * @author Nawab Iqbal
 */
public class MergeTest extends BaseMultiEntityManagerJPAFTest {
    
    @Test
    public void testEagerLoadingWithCascadedMergeAsPersist() throws MalformedURLException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity acct  = new PersonEntity();
        acct.setName("Account 1");
        PhoneEntity opp = JPATestUtils.createPhoneEntity("old");
        tx.begin();
        em.persist(acct);

        LinkedList<PhoneEntity> phoneList = new  LinkedList<PhoneEntity>();
        phoneList.add(opp);
        acct.setEagerPhones(phoneList);
        opp.setEagerHolder(acct);
        em.persist(opp);
        tx.commit();
        
        acct = em.find(PersonEntity.class, acct.getId());
        em.detach(acct);
        Assert.assertEquals("Account 1", acct.getName());
        Assert.assertNotNull(acct.getEagerPhones());    // changing original child
        acct.getEagerPhones().get(0).setName("older");
        acct.getEagerPhones().get(0).setType("older");
        PhoneEntity newPhone = JPATestUtils.createPhoneEntity("new"); // adding a new child
        newPhone.setEagerHolder(acct);
        tx.begin();
        acct.getEagerPhones().add(newPhone);
        em.merge(acct);
        tx.commit();
        
        acct = em.find(PersonEntity.class, acct.getId());
        Assert.assertNotNull(acct.getEagerPhones());
        Assert.assertEquals(acct.getEagerPhones().size(), 2);
        JPATestUtils.verifyContains(acct.getEagerPhones(), new String[] {"older", "new"});
    }

    protected void testCascadedMergeAsPersist(Boolean loadLazy)
    throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException,
           NoSuchMethodException, IllegalAccessException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity acct  = new PersonEntity();
        acct.setName("Account 1");
        PhoneEntity opp = JPATestUtils.createPhoneEntity("old");
        LinkedList<PhoneEntity> oppList = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(acct);
        oppList.add(opp);
        acct.setPhones(oppList);
        opp.setPhoneHolder(acct);
        em.persist(opp);
        tx.commit();

        acct = em.find(PersonEntity.class, acct.getId());
        if (loadLazy) {
            acct.getPhones();
        }
        em.detach(acct);
        Assert.assertEquals("Account 1", acct.getName());

        PhoneEntity newOpp = JPATestUtils.createPhoneEntity("new");
        newOpp.setPhoneHolder(acct);

        if (loadLazy) {
            Assert.assertNotNull(acct.getPhones());
            acct.getPhones().add(newOpp);
        } else {
            JPATestUtils.assertDetachedFieldException(acct, "getPhones");
            
            oppList = new  LinkedList<PhoneEntity>();
            oppList.add(newOpp);
            acct.setPhones(oppList);
        }
        
        tx.begin();
        em.merge(acct);
        tx.commit();
       
        acct = em.find(PersonEntity.class, acct.getId());
        Assert.assertNotNull(acct.getPhones());
        Assert.assertEquals(acct.getPhones().size(), 2);
        JPATestUtils.verifyContains(acct.getPhones(), new String[] {"new", "old"});
    }
        
    /*
     * By 'forced', I mean loading of an attribute by calling getter; when it is originally marked for lazy loading.  
     */
    @Test
    public void testForcedLoadingWithCascadedMergeAsPersist()
    throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException,
           NoSuchMethodException, IllegalAccessException  {
        testCascadedMergeAsPersist(true);
    }
    
    @Test
    public void testLazyLoadingWithCascadedMergeAsPersist()
    throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException,
           NoSuchMethodException, IllegalAccessException  {
        testCascadedMergeAsPersist(false);
    }
        
    @Test
    public void testForcedLazyLoadingWithCascadedMerge() throws MalformedURLException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity acct  = new PersonEntity();
        acct.setName("Account 1");
        PhoneEntity opp = JPATestUtils.createPhoneEntity("old");
        LinkedList<PhoneEntity> oppList = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(acct);
        oppList.add(opp);
        acct.setPhones(oppList);
        opp.setPhoneHolder(acct);
        em.persist(opp);
        tx.commit();
        
        acct = em.find(PersonEntity.class, acct.getId());
        acct.getPhones();
        em.detach(acct);
        Assert.assertEquals("Account 1", acct.getName());
        Assert.assertNotNull(acct.getPhones());
        
        PhoneEntity p = acct.getPhones().get(0);
        p.setName("new phone");
        p.setType("new type");
        // TODO: this should work. It is not working for now. 
        //Assert.assertNotNull(p.getPhoneHolder());

        tx.begin();
        em.merge(acct);
        tx.commit();
        
        acct = em.find(PersonEntity.class, acct.getId());
        Assert.assertNotNull(acct.getPhones());
        Assert.assertEquals(acct.getPhones().size(), 1);
        PhoneEntity o = acct.getPhones().get(0);
        JPATestUtils.verifyPhoneEntity(o, "new");
        //Assert.assertNotNull(o.getPhoneHolder(), "Account is null.");
        //Assert.assertEquals(o.getPhoneHolder(), acct, "Account references are not same.");
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
        PersonEntity acct  = new PersonEntity();
        acct.setName("Account 1");
        PhoneEntity opp = JPATestUtils.createPhoneEntity("old");
        LinkedList<PhoneEntity> oppList = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(acct);
        oppList.add(opp);
        acct.setPhones(oppList);
        opp.setPhoneHolder(acct);
        em.persist(opp);
        tx.commit();
        
        acct = em.find(PersonEntity.class, acct.getId());
        em.detach(acct); // since acct has been lazy loaded, the phones list will be null.
                         // And will NOT be merged according to spec.  
        Assert.assertEquals("Account 1", acct.getName());
        JPATestUtils.assertDetachedFieldException(acct, "getPhones");

        PhoneEntity newOpp = JPATestUtils.createPhoneEntity("new");
        oppList = new  LinkedList<PhoneEntity>();
        oppList.add(newOpp);
        newOpp.setPhoneHolder(acct);
        acct.setPhones(oppList);

        tx.begin();
        em.merge(acct);
        tx.commit();

        acct = em.find(PersonEntity.class, acct.getId());
        Assert.assertNotNull(acct.getPhones());
        Assert.assertEquals(acct.getPhones().size(), 2);
        JPATestUtils.verifyContains(acct.getPhones(),  new String[] {"new", "old"});
    }

    @Test
    public void testPersistWithReference() throws MalformedURLException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity acct  = new PersonEntity();
        acct.setName("Account 1");
        PhoneEntity opp = JPATestUtils.createPhoneEntity("old");

        LinkedList<PhoneEntity> oppList = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(acct);
        em.flush();
        tx.commit();
        em.detach(acct);
        
        tx.begin();
        acct = em.getReference(PersonEntity.class, acct.getId());
        oppList.add(opp);
        acct.setPhones(oppList);
        opp.setPhoneHolder(acct);
        em.persist(opp);
        tx.commit();
        
        acct = em.find(PersonEntity.class, acct.getId());
        Assert.assertEquals("Account 1", acct.getName());
        Assert.assertNotNull(acct.getPhones());
        Assert.assertEquals(acct.getPhones().size(), 1);
        
        List<PhoneEntity> l = acct.getPhones();
        PhoneEntity o = (PhoneEntity) l.toArray()[0];
        JPATestUtils.verifyPhoneEntity(o, "old");
        // TODO: Following should work.  
        //Assert.assertNotNull(o.getPhoneHolder(), "Account is null.");
        //Assert.assertEquals(o.getPhoneHolder(), acct, "Account references are not same.");
    }

    @Test
    public void testGetReference()
    throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException,
           NoSuchMethodException, IllegalAccessException  {
        EntityTransaction tx = em.getTransaction();
        PersonEntity acct  = new PersonEntity();
        acct.setName("Account 1");
        acct.setType("Lazy: do not fetch for getRef");
        URL lazyURL = new URL("http://www.lazy.com");
        URL eagerURL = new URL("http://www.eager.com");
        acct.setLazyURL(lazyURL);
        acct.setEagerURL(eagerURL);
        
        PhoneEntity opp = JPATestUtils.createPhoneEntity("old");
        LinkedList<PhoneEntity> oppList = new  LinkedList<PhoneEntity>();
        
        tx.begin();
        em.persist(acct);
        em.flush();
        tx.commit();
        em.detach(acct);
        
        tx.begin();
        acct = em.find(PersonEntity.class, acct.getId());
        acct.setPhones(oppList);
        opp.setPhoneHolder(acct);
        em.persist(opp);
        tx.commit();
        em.detach(acct); // No op
        
        acct = em.getReference(PersonEntity.class, acct.getId());
        em.detach(acct);
        
        // name is eager.
        // However, I am confused if eager attributes should also throw exception 
        // because of getReference was used (as opposed to find)
        acct.getName();
        acct.getEagerURL(); // same as above
        
        JPATestUtils.assertDetachedFieldException(acct, "getType");
        JPATestUtils.assertDetachedFieldException(acct, "getPhones");
        // TODO: url is null (which is expected) but getLazyURL should also cause exception.
        //AssertDetachedFieldException(acct, "getLazyURL");
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
        person.setName("acct 1");
        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(person);
        tx.commit();
          
        PersonEntity newPerson = new PersonEntity();
        newPerson .setId(person.getId());
        newPerson .setName("acct 2");
        
        tx = em.getTransaction();
        tx.begin();
        newPerson  = em.merge(newPerson);
       tx.commit();
        
        Assert.assertTrue(newPerson.getId() != null, "Id is null. Account object was not persisted by merge().");
        
        //TODO: These asserts should pass.
        //Assert.assertEquals(newAccount.getId(), account.getId(), "Ids don't match. New Account object was created by merge().");
        //PersonEntity a1 = em.find(PersonEntity.class, person.getId());
        //Assert.assertEquals(a1.getName(), newAccount.getName(), "New name was not merged by merge().");
    }

    
    @Test
    /**
     * If X is a removed entity instance, an IllegalArgumentException will be thrown 
     * by the merge operation (or the transaction commit will fail).
     */
    public void testMergeWithInvalidKey() {
        PersonEntity account = new PersonEntity();
        account.setName("acct 1");
        account.setId("some invalid key");
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.merge(account);
        tx.commit();
        
        Assert.assertTrue(account.getId() != null, "Id is null. Account object was not persisted by merge().");
        Assert.assertNotSame(account.getId(), "some invalid key");
    }

    @Test
    public void testPersistWithInvalidKey() {
        PersonEntity account = new PersonEntity();
        account.setName("acct 1");
        account.setId("some invalid key");
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(account);
        tx.commit();
        Assert.assertTrue(account.getId() != null, "Id is null. Account object was not persisted by merge().");
    }

    @Test
    /* From spec:
     * If X is a new entity instance, a new managed entity instance X' is created 
     * and the state of X is copied into the new managed entity instance X'.
     */
    public void testMergeAsPersist() {
        PersonEntity account = new PersonEntity();
        account.setName("acct 1");
        
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.merge(account);
        tx.commit();
          
        Assert.assertTrue(account.getId() != null, "Id is null. Account object was not persisted by merge().");
        
        PersonEntity a1 = em.find(PersonEntity.class, account.getId());
        Assert.assertEquals(a1.getName(), account.getName(), "name was not persisted by merge().");
        
        // verify that two objects are different.
        Assert.assertNotSame(a1, account, "New object was not created by merge().");
    }
}
