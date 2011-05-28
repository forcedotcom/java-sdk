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

import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.AccountEntity;
import com.force.sdk.test.util.BaseJPAFTest;

/**
 * Tests for simple transactional behavior.
 * 
 * @author Naaman Newbold
 */
public class BasicTransactionTest extends BaseJPAFTest {

    @Test
    public void testPersistDetachedEntityFromRollback() {
        AccountEntity entity = new AccountEntity();
        entity.setName("BasicTransactionTest.testPersistDetachedEntityFromRollback");

        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        em.persist(entity);
        transaction.rollback();

        Assert.assertNull(entity.getId(), "expected a null id for rolled back entity, but got: " + entity.getId());
        Assert.assertFalse(em.contains(entity),
                "Transaction was rolled back, yet the entity manager still contains the persisted entity.");

        transaction = em.getTransaction();
        transaction.begin();
        em.persist(entity);
        transaction.commit();

        Assert.assertNotNull(entity.getId(), "expected an id after entity was committed, but the id was null");
        Assert.assertNotNull(em.find(AccountEntity.class, entity.getId()).getName(),
                "expected a result from retrieving the committed entity from the db, but received a null result");
    }

}
