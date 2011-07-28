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

import javax.persistence.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.TestEntity;
import com.force.sdk.qa.util.jpa.BaseJPAFTest;

/**
 * 
 * Test for exceeding length limit of a string field.
 *
 * @author John Simone
 */
public class MaxFieldLengthExceptionTest extends BaseJPAFTest {

    String longString = "This is a 300 Character String "
            + "This is a 300 Character String This is a 300 Character String "
            + "This is a 300 Character String This is a 300 Character String "
            + "This is a 300 Character String This is a 300 Character String "
            + "This is a 300 Character String This is a 300 Character String "
            + "This is a 300 Charact";
    String exceptionMessageSnippet = "stringObject__c: data value too large:";
    
    @Test
    /**
     * String length exception test.
     * Verifies that the correct exception is thrown when attempting 
     * to save a String longer than 255 characters that is mapped to a Text(255).
     * 
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults PersistenceException is thrown with error message 'stringObject__c: data value too large:'.
     */
    public void testMaxStringLength() {
        testMaxStringLengthInternal(em);
    }
    
    public void testMaxStringLengthInternal(EntityManager testEm) {
        TestEntity entity = new TestEntity();
        JPATestUtils.initializeTestEntity(entity);
        entity.setStringObject(longString);
        EntityTransaction tx = testEm.getTransaction();
        try {
            tx.begin();
            testEm.persist(entity);
            tx.commit();
            
            Assert.fail("Exception should have been thrown when field length was exceeded.");
        } catch (Exception e) {
           Assert.assertTrue(e instanceof PersistenceException);
           Assert.assertEquals(e.getMessage().substring(0, 38), exceptionMessageSnippet);
        } finally {
            tx.rollback();

        }
    }
    
}
