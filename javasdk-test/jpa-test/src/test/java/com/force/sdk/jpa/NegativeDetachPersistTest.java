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

import java.util.Random;

import javax.persistence.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.AccountEntity;
import com.force.sdk.qa.util.jpa.BaseJPAFTest;

/**
 * Testing the persistence of detached entities.
 * Following JPA Spec: 3.2.7 Detached Entities:
 * "A detached entity results from transaction commit if a transaction-scoped container-managed entity
 * manager is used (see section 3.3); from transaction rollback (see section 3.3.2); from detaching the
 * entity from the persistence context; from clearing the persistence context; from closing an entity man-
 * ager; or from serializing an entity or otherwise passing an entity by value e.g., to a separate applica-
 * tion tier, through a remote interface, etc."

 @author Naaman Newbold

 */
public class NegativeDetachPersistTest extends BaseJPAFTest {

    @Test
    public void testPersistDetachedEntityByRemoval() {
        AccountEntity account = createTestAccount(true);

        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();

            em.remove(account);

            Assert.fail("Entity " + account.toString() + " should be detached and should not be allowed to be persisted.");
        } catch (IllegalArgumentException iae) {
            Assert.assertTrue(iae.getMessage().contains("detached yet this operation requires it to be attached"),
                    "unexpected error message: " + iae.getMessage());
        } finally {
           if (transaction != null) transaction.rollback();
        }
    }

    @Test
    public void testPersistDetachedEntityByEntityManagerClear() {
        AccountEntity account = createTestAccount(false);

        EntityTransaction transaction = null;
        
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(account);

            Assert.assertTrue(em.contains(account), "The AccountEntity was just persisted, but is not managed.");
            em.clear();
            Assert.assertFalse(em.contains(account),
                    "The entity manager was cleared, but it still contains " + account.toString());

            em.persist(account);
            Assert.fail("Entity " + account.toString() + " should be detached and should not be allowed to be persisted.");
        } catch (EntityExistsException e) {
            Assert.assertEquals(e.getMessage(),
                    "Entity already exists. Use merge to save changes.", "unexpected error message: " + e.getMessage());
        } finally {
            if (transaction != null) transaction.rollback();
        }
    }

    @Test
    public void testMergeDetachedEntityByEntityManagerClear() {
        AccountEntity account = createTestAccount(false);
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(account);

            Assert.assertTrue(em.contains(account), "The AccountEntity was just persisted, but is not managed.");
            em.clear();
            Assert.assertFalse(em.contains(account),
                    "The entity manager was cleared, but it still contains " + account.toString());

            em.merge(account);

            Assert.fail("Entity " + account.toString() + " should be detached and should not be allowed to be persisted.");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Detached entity with null id cannot be merged.",
                    "unexpected error message: " + e.getMessage());
        } finally {
            if (transaction != null) transaction.rollback();
        }
    }

    @Test
    public void testCloseEntityManagerThenPersist() {
        AccountEntity account = createTestAccount(false);
        EntityManager em = emfac.createEntityManager();
        // recreating the em is expensive
        // TODO: mock the em so we don't actually close and recreate
        try {
            em.close();
            em.persist(account);
            Assert.fail("Entity manager was closed, but an entity was allowed to persist.");
        } catch (IllegalStateException ise) {
            Assert.assertTrue(ise.getMessage().contains("EntityManager is already closed"),
                    "unexpected error message: " + ise.getMessage());
        } 
    }

    @Test
    public void testPersistFlushAndRollback() {
        AccountEntity account = createTestAccount(false);

        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        em.persist(account);
        em.flush();
        transaction.rollback();
        Assert.assertNull(account.getId(), "Entity should not have been saved.");
    }
    
    private AccountEntity createTestAccount(boolean create) {
        AccountEntity account = new AccountEntity();
        account.setName("Test" + String.valueOf(new Random().nextInt(10000)));

        if (create) {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            em.persist(account);
            transaction.commit();
        }

        return account;
    }
}
