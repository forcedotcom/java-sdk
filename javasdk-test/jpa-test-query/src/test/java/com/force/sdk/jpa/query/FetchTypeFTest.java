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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import org.testng.annotations.*;

import com.force.sdk.jpa.query.entities.FetchTypeFTestEntity;
import com.force.sdk.test.util.BaseJPAFTest;
import com.google.common.collect.Lists;

/**
 * 
 * Test for eager and lazy fetch types.
 *
 * @author Fiaz Hossain
 */
public class FetchTypeFTest extends BaseJPAFTest {

    @BeforeClass(dependsOnMethods = "initialize")
    public void initTestData() {
        FetchTypeFTestEntity entity = new FetchTypeFTestEntity();
        entity.setName("FetchTypeFTestEntity1");
        entity.setLazyFetchField("lazyFetchField1");
        entity.setEagerFetchField("eagerFetchField1");
        
        addTestDataInTx(Lists.newArrayList(entity));
    }
    
    @AfterClass
    protected void classTearDown() {
        deleteAll(FetchTypeFTestEntity.class);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    // TODO: This really should be a unit test, but I couldn't
    // quite follow the implementation well enough to know what
    // needed to be mocked out
    public void testNativeQueryWithPartialLoadAndLazyFields() {
        // Partially loaded entities with lazy fields still get full data
        String nativeQuery = "select id, name, " + getFieldName(em, FetchTypeFTestEntity.class, "eagerFetchField")
                                + " from " + getTableName(em, FetchTypeFTestEntity.class) + " limit 2";
        
        List<FetchTypeFTestEntity> results = em.createNativeQuery(nativeQuery, FetchTypeFTestEntity.class).getResultList();

        // Assert the the lazy fetch field is still loaded
        assertEquals(results.size(), 1, "Unexpected number of results for native query " + nativeQuery);
        assertNotNull(results.get(0).getLazyFetchField(), "Expected lazy fetch field to be not null");
    }
    
}
