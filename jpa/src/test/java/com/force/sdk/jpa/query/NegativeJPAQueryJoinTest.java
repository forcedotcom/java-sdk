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

import javax.persistence.PersistenceException;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.BasicChildTestEntity;
import com.force.sdk.jpa.entities.BasicParentTestEntity;

/**
 * Negative tests for Force.com JPA queries with joins.
 *
 * @author Tim Kral
 */
public class NegativeJPAQueryJoinTest extends BaseJPAQueryTest {

    @Test
    public void testAllQueryUnsupported() {
        String query = "select c from " + BasicChildTestEntity.class.getSimpleName() + " c"
                            + " where c.name > ALL"
                            + " (select c1.name"
                             + " from " + BasicChildTestEntity.class.getSimpleName() + " c1"
                             + " where c1.parent.name in ('parent1', 'parent2'))";
        
        try {
            em.createQuery(query).getResultList();
            Assert.fail("'" + query + "' should have failed because ALL is not supported.");
        } catch (PersistenceException pe) {
            Assert.assertTrue(pe.getMessage().contains("ALL is not supported in force.com database"),
                    "Exception message did not match: " + pe.getMessage());
        }
    }
    
    @Test
    public void testAnyQueryUnsupported() {
        String query = "select c from " + BasicChildTestEntity.class.getSimpleName() + " c"
                            + " where c.name > ANY"
                            + " (select c1.name"
                             + " from " + BasicChildTestEntity.class.getSimpleName() + " c1"
                             + " where c1.parent.name in ('parent1', 'parent2'))";
        
        try {
            em.createQuery(query).getResultList();
            Assert.fail("'" + query + "' should have failed because ANY is not supported.");
        } catch (PersistenceException pe) {
            Assert.assertTrue(pe.getMessage().contains("ANY is not supported in force.com database"),
                    "Exception message did not match: " + pe.getMessage());
        }
    }
    
    @Test
    public void testExistsQueryUnsupported() {
        String query = "select p from " + BasicParentTestEntity.class.getSimpleName() + " p"
                            + " where exists"
                            + " (select c.parent"
                             + " from " + BasicChildTestEntity.class.getSimpleName() + " c"
                             + " where c.name in ('child1', 'child2'))";
        
        try {
            em.createQuery(query).getResultList();
            Assert.fail("'" + query + "' should have failed because EXISTS is not supported.");
        } catch (PersistenceException pe) {
            Assert.assertTrue(pe.getMessage().contains("EXISTS is not supported in force.com database"),
                    "Exception message did not match: " + pe.getMessage());
        }
    }

    @Test
    public void testNotExistsQueryUnsupported() {
        String query = "select p from " + BasicParentTestEntity.class.getSimpleName() + " p"
                            + " where not exists"
                            + " (select c.parent from " + BasicChildTestEntity.class.getSimpleName() + " c"
                            + " where c.name in ('child1', 'child2'))";
        
        // Try NOT EXISTS query
        try {
            em.createQuery(query).getResultList();
            Assert.fail("'" + query + "' should have failed because NOT EXISTS is not supported.");
        } catch (PersistenceException pe) {
            Assert.assertTrue(pe.getMessage().contains("EXISTS is not supported in force.com database"),
                    "Exception message did not match: " + pe.getMessage());
        }
    }

    @Test
    public void testSomeQueryUnsupported() {
        String query = "select c from " + BasicChildTestEntity.class.getSimpleName() + " c"
                            + " where c.name > SOME"
                            + " (select c1.name"
                             + " from " + BasicChildTestEntity.class.getSimpleName() + " c1"
                             + " where c1.parent.name in ('parent1', 'parent2'))";
        
        try {
            em.createQuery(query).getResultList();
            Assert.fail("'" + query + "' should have failed because SOME is not supported.");
        } catch (PersistenceException pe) {
            Assert.assertTrue(pe.getMessage().contains("SOME is not supported in force.com database"),
                    "Exception message did not match: " + pe.getMessage());
        }
    }
}
