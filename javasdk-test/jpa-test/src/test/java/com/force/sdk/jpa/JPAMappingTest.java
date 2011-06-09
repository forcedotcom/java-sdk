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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.AccountMapped2ndTime;
import com.force.sdk.qa.util.BaseJPAFTest;

/**
 * 
 * Tests for how salesforce entities are mapped to pojos.
 *
 * @author Jeff Lai
 */
public class JPAMappingTest extends BaseJPAFTest {
    
    @Test
    public void testTwoPojosMappedToOneEntity() {
        try {
            AccountMapped2ndTime acc = new AccountMapped2ndTime();
            acc.setName("account0");
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(acc);
            tx.commit();
            List<AccountMapped2ndTime> results =
                em.createQuery("select o from Account o where name='account0'", AccountMapped2ndTime.class).getResultList();
            Assert.assertEquals(results.size(), 1, "unexpected number of results returned in query");
            Assert.assertEquals(results.get(0).getId(), acc.getId(), "unexpected id in returned entity");
        } finally {
           em.createQuery("delete from Account o where o.name='account0'").executeUpdate();
        }
    }

}
