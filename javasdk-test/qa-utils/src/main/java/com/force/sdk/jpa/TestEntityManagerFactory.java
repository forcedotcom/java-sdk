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

import java.util.Map;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import com.force.sdk.test.util.TestContext;

/**
 * 
 * This class is intended for testing under Spring where we need to be able to recreate an EntityManagerFactory initialized
 * from a newly constructed org.
 *
 * @author Fiaz Hossain
 */
public class TestEntityManagerFactory implements EntityManagerFactory {

    private volatile EntityManagerFactory emf;
    
    /**
     * Initializes a {@code TestEntityManagerFactory} wrapper
     * for the given {@code EntityManagerFactory}.
     * 
     * @param emf EntityManagerFactory
     */
    public TestEntityManagerFactory(EntityManagerFactory emf) {
        setEntityManagerFactory(emf);
        TestContext.get().setEntityManagerFactory(this);
    }
    
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.emf = entityManagerFactory;
    }
    
    @Override
    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return emf.createEntityManager(map);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return emf.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return emf.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return emf.isOpen();
    }

    @Override
    public void close() {
        emf.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return emf.getProperties();
    }

    @Override
    public Cache getCache() {
        return emf.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return emf.getPersistenceUnitUtil();
    }
}
