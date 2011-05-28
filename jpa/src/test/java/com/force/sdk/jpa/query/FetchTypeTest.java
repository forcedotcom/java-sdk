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

import static org.testng.Assert.*;

import javax.persistence.Query;

import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.FetchTypeOverrideEntity;
import com.force.sdk.jpa.entities.FetchTypeTestEntity;

/**
 * Tests for supported fetch types.
 *
 * @author Tim Kral
 */
public class FetchTypeTest extends BaseJPAQueryTest {

    @Test
    public void testQueryWithLazyFetchType() {
        
        Query q = em.createQuery("select o from " + FetchTypeTestEntity.class.getSimpleName() + " o");
        ((QueryNotifier) q.unwrap(org.datanucleus.store.query.Query.class)).addListener("foobar", new QueryListener() {
            @Override
            public void listen(String soqlQuery) {
                assertFalse(soqlQuery.toLowerCase().contains("lazyfetchfield"),
                        "Lazy fetch colums should not be loaded.");
            }
        });
        q.getResultList();
    }
    
    // Test that you can override the fetch type in subclasses
    @Test
    public void testQueryWithFetchTypeOverride() {
        
        Query q = em.createQuery("select o from " + FetchTypeOverrideEntity.class.getSimpleName() + " o");
        ((QueryNotifier) q.unwrap(org.datanucleus.store.query.Query.class)).addListener("foobar", new QueryListener() {
            @Override
            public void listen(String soqlQuery) {
                assertTrue(soqlQuery.toLowerCase().contains("lazyfetchfieldwithoverride__c"),
                        "Lazy fetch colums with Eager overrides should be loaded.");
                assertFalse(soqlQuery.toLowerCase().contains("lazyfetchfield__c"),
                        "Lazy fetch colums without overrides should not be loaded.");
            }
        });
        q.getResultList();
    }
}
