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

import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.orderby.*;


/**
 * 
 * Tests for querying for entities annotated with OrderBy.
 *
 * @author Jeff Lai
 */
public class QueryOrderByAnnotationTest extends BaseJPAQueryTest {
    
    @Test
    public void testOrderByInt() {
        mockQueryConn.setExpectedSoqlQuery("select id,"
                + " (select id, myInt__c from ParentEntityOrderByInt_childentityorderb__r order by myInt__c ASC )"
                + " from parententityorderbyint__c o ");
        em.createQuery("select o from " + ParentEntityOrderByInt.class.getSimpleName() + " o",
                ParentEntityOrderByInt.class).getResultList();
    }
    
    @Test
    public void testOrderByIntString() {
        mockQueryConn.setExpectedSoqlQuery("select id,"
                + " (select id, myInt__c, myString__c from ParentEntityOrderByIntString_childentity__r"
                  + " order by myInt__c ASC , myString__c DESC )"
                + " from parententityorderbyintstring__c o ");
        em.createQuery("select o from " + ParentEntityOrderByIntString.class.getSimpleName() + " o",
                ParentEntityOrderByIntString.class).getResultList();
    }
    
    @Test
    public void testOrderByPk() {
        mockQueryConn.setExpectedSoqlQuery("select id,"
                + " (select id from ParentEntityOrderByPk_childentityorderby__r order by Id ASC )"
                + " from parententityorderbypk__c o ");
        em.createQuery("select o from " + ParentEntityOrderByPk.class.getSimpleName() + " o",
                ParentEntityOrderByPk.class).getResultList();
    }

}
