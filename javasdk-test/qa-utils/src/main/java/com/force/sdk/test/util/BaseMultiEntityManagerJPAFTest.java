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

package com.force.sdk.test.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.testng.annotations.AfterMethod;

/**
 * Extend this class if you need more than one EntityManager form different entity manager factory.
 * 
 * @author Fiaz Hossain
 */
public abstract class BaseMultiEntityManagerJPAFTest extends BaseJPAFTest {

    protected EntityManagerFactory emfac2;
    protected EntityManagerFactory emfac3;
    public EntityManager em2;
    public EntityManager em3;

    @Override
    protected void createStaticEntityMangers() throws Exception {
        super.createStaticEntityMangers();
        TestContext ctx = TestContext.get();
        emfac2 = Persistence.createEntityManagerFactory(ctx.getPersistenceUnitName() + "2");
        emfac3 = Persistence.createEntityManagerFactory(ctx.getPersistenceUnitName() + "3");
        em2 = emfac2.createEntityManager();
        em3 = emfac3.createEntityManager();
    }
        
    /**
     * Any test unspecific cleaning that needs to be done.
     * @throws IOException 
     */
    @AfterMethod
    @Override
    protected void testCleanup() throws Exception {
        super.testCleanup();
        if (em2.getTransaction().isActive()) {
            em2.getTransaction().rollback();
        }
        if (em3.getTransaction().isActive()) {
            em3.getTransaction().rollback();
        }
    }
}
