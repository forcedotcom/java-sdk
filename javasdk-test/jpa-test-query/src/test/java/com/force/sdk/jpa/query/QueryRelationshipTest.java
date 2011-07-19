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

package com.force.sdk.jpa.query;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.query.entities.relationship.ChildRelationshipEntity;
import com.force.sdk.jpa.query.entities.relationship.ParentRelationshipEntity;
import com.force.sdk.qa.util.BaseJPAFTest;

/**
 * 
 * Test to query for entities with relationships.
 *
 * @author Jeff Lai
 */
public class QueryRelationshipTest extends BaseJPAFTest {
    
    @Test
    public void testQueryRelationshipWithInheritance() {
        try {
            ParentRelationshipEntity parent = new ParentRelationshipEntity();
            ChildRelationshipEntity child = new ChildRelationshipEntity();
            child.setParent(parent);
            parent.setChildren(Collections.singletonList(child));
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            em.persist(parent);
            tx.commit();
            List<ParentRelationshipEntity> parentResult =
                em.createQuery("select o from " + ParentRelationshipEntity.class.getSimpleName() + " o",
                                ParentRelationshipEntity.class).getResultList();
            Assert.assertEquals(parentResult.size(), 1,
                    "JPQL query for ParentRelationshipEntity did not return expected number of results ");
        } finally {
            deleteAll(ChildRelationshipEntity.class);
            deleteAll(ParentRelationshipEntity.class);
        }
    }

}
