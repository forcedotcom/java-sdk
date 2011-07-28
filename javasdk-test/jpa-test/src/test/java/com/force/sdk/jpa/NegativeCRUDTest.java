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

import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.*;
import com.force.sdk.qa.util.jpa.BaseJPAFTest;

/**
 * Negative tests for JPA CRUD operations.
 *
 * @author Tim Kral
 */
public class NegativeCRUDTest extends BaseJPAFTest {

    @DataProvider
    protected Object[][] invalidIdProvider() {
        return new Object[][] {
                {""},
                {"deadbeef"},             // Id is too short
                {"00a00deadbeef00"},      // Invalid 15 char id
                {"00a00deadbeef00AAA"},   // Invalid 18 char id
                {"00a00deadbeef00AAAAA"}, // Id is too long
        };
    }
    
    @Test(dataProvider = "invalidIdProvider")
    public void testFindWithInvalidId(String invalidId) throws Exception {
        Assert.assertNull(em.find(TestEntity.class, invalidId));
    }
    
    @Test
    public void testQueryResultCastShouldFail() {
        try {
        AccountEntity account = new AccountEntity();
        account.setName("Best Buy");
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(account);
        tx.commit();
        List<Community> results =
            em.createQuery("select o from Account o where name='Best Buy'", Community.class).getResultList();
        results.get(0).getId();
        Assert.fail("Query from Account to Community should have failed");
        } catch (PersistenceException pe) {
            Assert.assertTrue(pe.getMessage()
                    .contains("Result class: com.force.sdk.jpa.entities.Community "
                                + "is not compatible with force.com table: Account"));
        } finally {
            em.createQuery("delete from Account o where o.name='Best Buy'").executeUpdate();
        }
    }
    
    /**
     * Simulates running in the spring container and forgetting to add.
     * 
     * @Transactional to a method that will retrieve and remove an object.
     * In this situation Spring will get a different entity manager for each call
     * therefore the em used for em.remove will see the object as detached. Our
     * framework adds additional information to an otherwise non-descriptive 
     * exception in this case. This test checks for that additional message.
     */
    @Test
    public void testRemoveWithoutTransactionFailure() {
        try {
            AccountEntity account = new AccountEntity();
            account.setName("Best Buy");
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(account);
            tx.commit();
            
            tx = em.getTransaction();
            tx.begin();
            account = em.find(AccountEntity.class, account.getId());
            tx.commit();
            em.remove(account);
            Assert.fail("Expecting a TransactionRequiredException to be thrown "
                            + "when attempting to remove an entity outside of a transaction. No Exception was thrown.");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(
                    e.getMessage().contains("It has most likely become detached since the find() operation. "
                                            + "Either put find() and remove() under same transaction or merge() the object first")
                    , "TransactionRequiredException contains an unexpected message: " + e.getMessage());
        } catch (Exception e) {
            Assert.fail("Expecting a TransactionRequiredException to be thrown "
                        + "when attempting to remove an entity outside of a transaction. Instead threw: " + e.getClass());
        }
        
    }
    
}
