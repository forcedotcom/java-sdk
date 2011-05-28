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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.callback.*;
import com.force.sdk.test.util.BaseJPAFTest;
import com.force.sdk.test.util.TestContext;

/**
 * Tests for callback methods on jpa entities.
 *
 * @author Jeff Lai
 */
public class CallbackMethodTest extends BaseJPAFTest {

    private static final String COMPANY_NAME = "salesforce.com" + System.currentTimeMillis();
    private static final String LAST_NAME1 = "smith" + System.currentTimeMillis();
    private static final String LAST_NAME2 = "lee" + System.currentTimeMillis();

    @SuppressWarnings("rawtypes")
    @AfterMethod
    public void methodTeardown() {
        em.createQuery("delete from " + Lead.class.getSimpleName() + " o where o.company='" + COMPANY_NAME + "'").executeUpdate();
        em.createQuery("delete from " + CustomEntityWithListener.class.getSimpleName() + " o").executeUpdate();
        em.createQuery("delete from " + Boeing747.class.getSimpleName() + " o").executeUpdate();
        em.createQuery("delete from " + Tuna.class.getSimpleName() + " o").executeUpdate();
        em.createQuery("delete from " + Maple.class.getSimpleName() + " o").executeUpdate();
        // delete all properties that were saved into TestContext during tests
        Enumeration e = TestContext.get().getTestProps().propertyNames();
        Pattern pat = Pattern.compile("^.+(Persist|Update|Remove|Load).+$");
        Matcher mat;
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            mat = pat.matcher(key);
            if (mat.matches()) {
                TestContext.get().getTestProps().remove(key);
            }
        }
    }

    @Test
    public void testPersistCallbackOnStandardEntity() throws InstantiationException, IllegalAccessException {
        setupLead();
        verifyPreAndPost("prePersistLead", "postPersistLead");
    }

    @Test
    public void testPersistListenerOnCustomEntity() throws InstantiationException, IllegalAccessException {
        setupCustomEntity(CustomEntityWithListener.class);
        verifyPreAndPost("prePersistCustom", "postPersistCustom");
    }
    // uncomment once W-904979 is fixed
    //@Test
    public void testPersistCallbackWithInheritance() throws InstantiationException, IllegalAccessException {
        setupCustomEntity(Boeing747.class);
        verifyPreAndPost("prePersistAirplane", "postPersistAirplane");
        verifyPreAndPost("prePersistBoeing747", "postPersistBoeing747");
        verifyOrderMethods(new String[] {"prePersistAirplane", "prePersistBoeing747",
                                         "postPersistAirplane", "postPersistBoeing747"},
            "^.+Persist(Airplane|Boeing747)$");
    }
    // uncomment once W-904979 is fixed
    //@Test
    public void testPersistListenerWithInheritance() throws InstantiationException, IllegalAccessException {
        setupCustomEntity(Tuna.class);
        verifyPreAndPost("prePersistFish", "postPersistFish");
        verifyPreAndPost("prePersistTuna", "postPersistTuna");
        verifyOrderMethods(new String[] {"prePersistFish", "prePersistTuna", "postPersistFish", "postPersistTuna"},
            "^.+Persist(Fish|Tuna)$");
    }
    // uncomment once W-904979 is fixed
    //@Test
    public void testPersistCallbackAndListenerWithInheritance() throws InstantiationException, IllegalAccessException {
        setupCustomEntity(Maple.class);
        verifyPreAndPost("prePersistPlant", "postPersistPlant");
        verifyPreAndPost("prePersistTree", "postPersistTree");
        verifyPreAndPost("prePersistMapleOne", "postPersistMapleOne");
        verifyPreAndPost("prePersistMapleTwo", "postPersistMapleTwo");
        verifyOrderMethods(new String[] {"prePersistTree", "prePersistMapleOne", "prePersistMapleTwo", "prePersistPlant",
                                         "postPersistTree", "postPersistMapleOne", "postPersistMapleTwo", "postPersistPlant"},
                "^.+Persist(Plant|Tree|MapleOne|MapleTwo)$");
    }

    @Test
    public void testUpdateCallbackOnStandardEntity() throws InstantiationException, IllegalAccessException {
        List<Lead> results = setupLead();
        updateLead(results.get(0));
        verifyPreAndPost("preUpdateLead", "postUpdateLead");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateListenerOnCustomEntity() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(CustomEntityWithListener.class);
        updateCustomEntity(results.get(0), CustomEntityWithListener.class);
        verifyPreAndPost("preUpdateCustom", "postUpdateCustom");
    }
    
    @SuppressWarnings("unchecked")
    // uncomment once W-904979 is fixed
    //@Test
    public void testUpdateCallbackWithInheritance() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(Boeing747.class);
        updateCustomEntity(results.get(0), Boeing747.class);
        verifyPreAndPost("preUpdateAirplane", "postUpdateAirplane");
        verifyPreAndPost("preUpdateBoeing747", "postUpdateBoeing747");
        verifyOrderMethods(new String[] {"preUpdateAirplane", "preUpdateBoeing747",
                                         "postUpdateAirplane", "postUpdateBoeing747"},
            "^.+Update(Airplane|Boeing747)$");
    }
    
    @SuppressWarnings("unchecked")
    // uncomment once W-904979 is fixed
    //@Test
    public void testUpdateListenerWithInheritance() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(Tuna.class);
        updateCustomEntity(results.get(0), Tuna.class);
        verifyPreAndPost("preUpdateFish", "postUpdateFish");
        verifyPreAndPost("preUpdateTuna", "postUpdateTuna");
        verifyOrderMethods(new String[] {"preUpdateFish", "preUpdateTuna", "postUpdateFish", "postUpdateTuna"},
            "^.+Update(Fish|Tuna)$");
    }
    
    @SuppressWarnings("unchecked")
    // uncomment once W-904979 is fixed
    //@Test
    public void testUpdateCallbackAndListenerWithInheritance() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(Maple.class);
        updateCustomEntity(results.get(0), Maple.class);
        verifyPreAndPost("preUpdatePlant", "postUpdatePlant");
        verifyPreAndPost("preUpdateTree", "postUpdateTree");
        verifyPreAndPost("preUpdateMapleOne", "postUpdateMapleOne");
        verifyPreAndPost("preUpdateMapleTwo", "postUpdateMapleTwo");
        verifyOrderMethods(new String[] {"preUpdateTree", "preUpdateMapleOne", "preUpdateMapleTwo", "preUpdatePlant",
                                         "postUpdateTree", "postUpdateMapleOne", "postUpdateMapleTwo", "postUpdatePlant"},
                "^.+Update(Plant|Tree|MapleOne|MapleTwo)$");
    }

    @Test
    public void testRemoveCallbackOnStandardEntity() throws InstantiationException, IllegalAccessException {
        List<Lead> results = setupLead();
        removeLead(results.get(0));
        verifyPreAndPost("preRemoveLead", "postRemoveLead");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveListenerOnCustomEntity() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(CustomEntityWithListener.class);
        removeCustomEntity(results.get(0), CustomEntityWithListener.class);
        verifyPreAndPost("preRemoveCustom", "postRemoveCustom");
    }
    
    @SuppressWarnings("unchecked")
    // uncomment once W-904979 is fixed
    //@Test
    public void testRemoveCallbackWithInheritance() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(Boeing747.class);
        removeCustomEntity(results.get(0), Boeing747.class);
        verifyPreAndPost("preRemoveAirplane", "postRemoveAirplane");
        verifyPreAndPost("preRemoveBoeing747", "postRemoveBoeing747");
        verifyOrderMethods(new String[] {"preRemoveAirplane", "preRemoveBoeing747",
                                         "postRemoveAirplane", "postRemoveBoeing747"},
            "^.+Remove(Airplane|Boeing747)$");
    }
    
    @SuppressWarnings("unchecked")
    // uncomment once W-904979 is fixed
    //@Test
    public void testRemoveListenerWithInheritance() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(Tuna.class);
        removeCustomEntity(results.get(0), Tuna.class);
        verifyPreAndPost("preRemoveFish", "postRemoveFish");
        verifyPreAndPost("preRemoveTuna", "postRemoveTuna");
        verifyOrderMethods(new String[] {"preRemoveFish", "preRemoveTuna", "postRemoveFish", "postRemoveTuna"},
            "^.+Remove(Fish|Tuna)$");
    }
    
    @SuppressWarnings("unchecked")
    // uncomment once W-904979 is fixed
    //@Test
    public void testRemoveCallbackAndListenerWithInheritance() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(Maple.class);
        removeCustomEntity(results.get(0), Maple.class);
        verifyPreAndPost("preRemovePlant", "postRemovePlant");
        verifyPreAndPost("preRemoveTree", "postRemoveTree");
        verifyPreAndPost("preRemoveMapleOne", "postRemoveMapleOne");
        verifyPreAndPost("preRemoveMapleTwo", "postRemoveMapleTwo");
        verifyOrderMethods(new String[] {"preRemoveTree", "preRemoveMapleOne", "preRemoveMapleTwo", "preRemovePlant",
                                         "postRemoveTree", "postRemoveMapleOne", "postRemoveMapleTwo", "postRemovePlant"},
                "^.+Remove(Plant|Tree|MapleOne|MapleTwo)$");
    }

    @Test
    public void testLoadCallbackOnStandardEntity() throws InstantiationException, IllegalAccessException {
        List<Lead> results = setupLead();
        findLead(results.get(0));
        verifyCallbackMethod("postLoadLead");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadListenerOnCustomEntity() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(CustomEntityWithListener.class);
        findCustomEntity(results.get(0), CustomEntityWithListener.class);
        verifyCallbackMethod("postLoadCustom");
    }
    
    @SuppressWarnings("unchecked")
    // uncomment once W-904979 is fixed
    //@Test
    public void testLoadCallbackWithInheritance() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(Boeing747.class);
        findCustomEntity(results.get(0), Boeing747.class);
        verifyCallbackMethod("postLoadAirplane");
        verifyCallbackMethod("postLoadBoeing747");
        verifyOrderMethods(new String[] {"postLoadAirplane", "postLoadBoeing747"},
        "^postLoad(Airplane|Boeing747)$");
    }
    
    @SuppressWarnings("unchecked")
    // uncomment once W-904979 is fixed
    //@Test
    public void testLoadListenerWithInheritance() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(Tuna.class);
        findCustomEntity(results.get(0), Tuna.class);
        verifyCallbackMethod("postLoadFish");
        verifyCallbackMethod("postLoadTuna");
        verifyOrderMethods(new String[] {"postLoadFish", "postLoadTuna"},
        "^postLoad(Fish|Tuna)$");
    }
    
    @SuppressWarnings("unchecked")
    // uncomment once W-904979 is fixed
    //@Test
    public void testLoadCallbackAndListenerWithInheritance() throws InstantiationException, IllegalAccessException {
        List<CallbackEntity> results = setupCustomEntity(Maple.class);
        findCustomEntity(results.get(0), Maple.class);
        verifyCallbackMethod("postLoadPlant");
        verifyCallbackMethod("postLoadTree");
        verifyCallbackMethod("postLoadMapleOne");
        verifyCallbackMethod("postLoadMapleTwo");
        verifyOrderMethods(new String[] {"postLoadTree", "postLoadMapleOne", "postLoadMapleTwo", "postLoadPlant"},
        "^postLoad(Plant|Tree|MapleOne|MapleTwo)$");
    }

    private List<Lead> setupLead() throws InstantiationException, IllegalAccessException {
        Lead lead = new Lead();
        lead.setLastName(LAST_NAME1);
        lead.setCompany(COMPANY_NAME);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(lead);
        tx.commit();
        List<Lead> results = em.createQuery("select o from " + Lead.class.getSimpleName()
                                            + " o where company='" + COMPANY_NAME + "'", Lead.class).getResultList();
        Assert.assertEquals(results.size(), 1, "unexpected result from query for entity");
        Lead result = results.get(0);
        Assert.assertEquals(result.getCompany(), COMPANY_NAME, "unexpected value on entity in query result");
        Assert.assertEquals(result.getLastName(), LAST_NAME1, "unexpected value on entity in query result");
        return results;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List setupCustomEntity(Class entityClass) throws InstantiationException, IllegalAccessException {
        CallbackEntity entity;
        entity = (CallbackEntity) entityClass.newInstance();
        entity.setName(COMPANY_NAME);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(entity);
        tx.commit();
        List<CallbackEntity> results =
            em.createQuery("select o from " + entityClass.getSimpleName() + " o ", entityClass).getResultList();
        Assert.assertEquals(results.size(), 1, "unexpected result from query for entity");
        Assert.assertEquals(results.get(0).getName(), COMPANY_NAME, "unexpected value on entity in query result");
        return results;
    }
    
    private void updateLead(Lead lead) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        lead.setLastName(LAST_NAME2);
        em.merge(lead);
        tx.commit();
        List<Lead> results = em.createQuery("select o from " + Lead.class.getSimpleName()
                                            + " o where company='" + COMPANY_NAME + "'", Lead.class).getResultList();
        Assert.assertEquals(results.size(), 1, "unexpected result from query for entity");
        Assert.assertEquals(results.get(0).getCompany(), COMPANY_NAME);
        Assert.assertEquals(results.get(0).getLastName(), LAST_NAME2);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void updateCustomEntity(CallbackEntity custom, Class entityClass) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        custom.setName(LAST_NAME1);
        em.merge(custom);
        tx.commit();
        List<CallbackEntity> results =
            em.createQuery("select o from " + entityClass.getSimpleName() + " o", entityClass).getResultList();
        Assert.assertEquals(results.size(), 1, "unexpected result from query for entity");
        Assert.assertEquals(results.get(0).getName(), LAST_NAME1);
    }
    
    private void removeLead(Lead lead) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.remove(lead);
        tx.commit();
        List<Lead> results = em.createQuery("select o from " + Lead.class.getSimpleName()
                                            + " o where company='" + COMPANY_NAME + "'", Lead.class).getResultList();
        Assert.assertEquals(results.size(), 0, "unexpected result from query for entity");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void removeCustomEntity(CallbackEntity custom, Class entityClass) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.remove(custom);
        tx.commit();
        List<CallbackEntity> results =
            em.createQuery("select o from " + entityClass.getSimpleName() + " o", entityClass).getResultList();
        Assert.assertEquals(results.size(), 0, "unexpected result from query for entity");
    }
    
    private void findLead(Lead lead) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        Lead findLead = em.find(Lead.class, lead.getId());
        tx.commit();
        Assert.assertEquals(findLead.getCompany(), COMPANY_NAME);
        Assert.assertEquals(findLead.getLastName(), LAST_NAME1);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void findCustomEntity(CallbackEntity custom, Class entityClass) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        CallbackEntity findCustom = (CallbackEntity) em.find(entityClass, custom.getId());
        tx.commit();
        Assert.assertEquals(findCustom.getName(), COMPANY_NAME);
    }

    /**
     * verify that pre-event method is called before post-event method.
     */
    private void verifyPreAndPost(String prePropName, String postPropName) {
        verifyCallbackMethod(prePropName);
        verifyCallbackMethod(postPropName);
        Assert.assertTrue(Long.parseLong(TestContext.get().getTestProps().getProperty(prePropName))
                < Long.parseLong(TestContext.get().getTestProps().getProperty(postPropName)),
                "Pre-event method" + prePropName + " should have been executed before Post-event " + postPropName + " method");
    }
    
    /**
     * verify that callback method was invoked.
     */
    private void verifyCallbackMethod(String methodName) {
        Assert.assertNotNull(TestContext.get().getTestProps().getProperty(methodName),
                methodName + "Callback method did not get executed");
        Assert.assertFalse(TestContext.get().getTestProps().getProperty(methodName).equals(""),
                methodName + "Callback method did not get executed");
    }
    
    /**
     * verify that callback methods of a given regex pattern are in the correct order.
     */
    private void verifyOrderMethods(String[] expectedMethodsSorted, String methodNamePattern) {
        List<CallbackMethod> actualMethods = getSortedCallbackMethods(methodNamePattern);
        Assert.assertEquals(actualMethods.size(), expectedMethodsSorted.length,
                "unexpected number of callback methods. We expected "
                + Arrays.asList(expectedMethodsSorted).toString() + " but found " + actualMethods.toString());
        for (int i = 0; i < expectedMethodsSorted.length; i++) {
            Assert.assertEquals(actualMethods.get(i).getName(), expectedMethodsSorted[i],
                "unexpected order of callback methods.  We expected the order "
                + Arrays.asList(expectedMethodsSorted).toString() + " but found " + actualMethods.toString());
        }
    }
    
    /**
     * returns a sorted list of callback methods that satisfy the given regex pattern.
     */
    @SuppressWarnings("rawtypes")
    private List<CallbackMethod> getSortedCallbackMethods(String pattern) {
        Enumeration e = TestContext.get().getTestProps().propertyNames();
        Pattern pat = Pattern.compile(pattern);
        Matcher mat;
        List<CallbackMethod> methods = new ArrayList<CallbackMethod>();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            mat = pat.matcher(key);
            if (mat.matches()) {
                methods.add(new CallbackMethod(key, Long.parseLong(TestContext.get().getTestProps().getProperty(key))));
            }
        }
        Collections.sort(methods);
        return methods;
    }
    
    /**
     * private class to make sorting callback method invocation time easier.
     */
    private static class CallbackMethod implements Comparable<CallbackMethod> {
        
        private String name;
        private long execTime;
        
        CallbackMethod(String name, long execTime) {
            this.name = name;
            this.execTime = execTime;
        }
        
        public String getName() {
            return name;
        }
        
        public long getExecTime() {
            return execTime;
        }
        
        @Override
        public int compareTo(CallbackMethod o) {
            CallbackMethod otherMethod = o;
             if (execTime > otherMethod.getExecTime()) {
                return 1;
            } else if (execTime < otherMethod.getExecTime()) {
                return -1;
            } else { // else exec times are the same
                return 0;
            }
        }
        
        @Override
        public boolean equals(Object that) {
            if (this == that) return true;
            if (!(that instanceof CallbackMethod)) return false;
            
            return this.execTime == ((CallbackMethod) that).getExecTime();
        }
        
        @Override
        public int hashCode() {
            return Long.valueOf(this.execTime).hashCode();
        }
        
        @Override
        public String toString() {
            return "(" + name + " : " + execTime + ")";
        }
        
    }

}
