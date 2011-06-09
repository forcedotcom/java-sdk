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

import com.force.sdk.connector.ForceConnectorConfig;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.jpa.entities.*;
import com.force.sdk.jpa.entities.TestEntity.PickValues;
import com.force.sdk.qa.util.BaseMultiEntityManagerJPAFTest;
import com.force.sdk.qa.util.TestContext;
import com.force.sdk.qa.util.UserInfo;
import com.sforce.soap.partner.Connector;
import com.sforce.ws.ConnectionException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.persistence.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Tests optimistic transaction support in JPA. Tests use 2 threads to create concurrency
 * and simulate test scenarios.
 *
 * @author Dirk Hain
 */
public class OptimisticTxTestMT extends BaseMultiEntityManagerJPAFTest {

    private static final OptimisticLockException OLE =
            new OptimisticLockException("Some instances failed to flush successfully due to optimistic verification problems.");
    protected int threadcount = 0;

    /**
     * Helper enum to model the execution protocol via an array.
     *
     * @author Dirk Hain
     * 
     */
    public enum Op {
        SWITCHCONTROL(0),
        READ(1),
        UPDATE(2),
        DELETE(3),
        WAIT(4),
        UPDATEwException(5),
        DELETEwException(6),
        COMMIT(7),
        ROLLBACK(8),
        UpdEmb(9),
        UpdRel(11),
        UpdPckl(12),
        UpdTransient(13),
        UpdRelField(14),
        UpdEmbField(15);
        int op;

        private Op(int o) {
            op = o;
        }
    }

    @BeforeClass(dependsOnMethods = "initialize")
    protected void init() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);
        deleteAll("Case");
        deleteAll("Account");
    }


    @AfterClass
    protected void classTearDown() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);
        deleteAll("Case");
        deleteAll("Account");
    }

    @DataProvider
    public Object[][] txData() throws NumberFormatException, MalformedURLException {
        Object[][] scenarios = new Object[][]{
                {"concurrent update", emfac2, new Op[][]{{Op.READ, Op.WAIT, Op.UPDATE, Op.COMMIT, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
                {"concurrent update a-o-n", emfac3, new Op[][]{{Op.READ, Op.WAIT, Op.UPDATE, Op.COMMIT, Op.READ},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
                {"concurrent delete", emfac2, new Op[][]{{Op.READ, Op.WAIT, Op.UPDATE, Op.COMMIT, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.DELETE, Op.COMMIT}},
                        null}, //deleting a modified entity is allowed
                {"concurrent delete a-o-n", emfac3, new Op[][]{{Op.READ, Op.WAIT, Op.UPDATE, Op.COMMIT, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.DELETE, Op.COMMIT}},
                        null}, //deleting a modified entity is allowed
                {"concurrent update w/ rollback", emfac2,
                        new Op[][]{{Op.READ, Op.WAIT, Op.UPDATE, Op.SWITCHCONTROL, Op.WAIT, Op.ROLLBACK, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATE, Op.SWITCHCONTROL, Op.WAIT, Op.COMMIT}},
                        null},
                {"concurrent upd w/ rollback a-o-n", emfac3,
                        new Op[][]{{Op.READ, Op.WAIT, Op.UPDATE, Op.SWITCHCONTROL, Op.WAIT, Op.ROLLBACK, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATE, Op.SWITCHCONTROL, Op.WAIT, Op.COMMIT}},
                        null},
                {"concurrent picklist update", emfac2, new Op[][]{{Op.READ, Op.WAIT, Op.UpdPckl, Op.COMMIT, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
                {"concurrent picklist update a-o-n", emfac3, new Op[][]{{Op.READ, Op.WAIT, Op.UpdPckl, Op.COMMIT, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
                {"concurrent update transient field", emfac2, new Op[][]{{Op.READ, Op.WAIT, Op.UpdTransient, Op.COMMIT, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATE, Op.COMMIT, Op.SWITCHCONTROL}},
                        null}, //transient update is allowed
                {"concurrent update transient field a-o-n", emfac3,
                        new Op[][]{{Op.READ, Op.WAIT, Op.UpdTransient, Op.COMMIT, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATE, Op.COMMIT, Op.SWITCHCONTROL}},
                        null}, //transient update is allowed
                {"concurrent update relationship", emfac2, new Op[][]{{Op.READ, Op.WAIT, Op.UpdRel, Op.COMMIT, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATE, Op.COMMIT, Op.SWITCHCONTROL}},
                        null}, //related entity can change
                {"concurrent update relationship a-o-n", emfac3, new Op[][]{{Op.READ, Op.WAIT, Op.UpdRel, Op.COMMIT, Op.WAIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATE, Op.COMMIT, Op.SWITCHCONTROL}},
                        null}, //related entity can change
                {"concurrent update relationship field", emfac2, new Op[][]{{Op.READ, Op.WAIT, Op.UpdRelField, Op.COMMIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
                {"concurrent update relationship field a-o-n", emfac3, new Op[][]{{Op.READ, Op.WAIT, Op.UpdRelField, Op.COMMIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
                {"concurrent update embedded field", emfac2, new Op[][]{{Op.READ, Op.WAIT, Op.UpdEmbField, Op.COMMIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
                {"concurrent update embedded field a-o-n", emfac3, new Op[][]{{Op.READ, Op.WAIT, Op.UpdEmbField, Op.COMMIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
                {"concurrent update embedded entity", emfac2, new Op[][]{{Op.READ, Op.WAIT, Op.UpdEmb, Op.COMMIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
                {"concurrent update embedded entity a-o-n", emfac3, new Op[][]{{Op.READ, Op.WAIT, Op.UpdEmb, Op.COMMIT},
                        {Op.WAIT, Op.READ, Op.WAIT, Op.UPDATEwException}},
                        OLE},
        };
        return scenarios;
    }


    @Test(dataProvider = "txData", invocationCount = 1, timeOut = 15000)
    /**
     * Optimistic transaction tests for multiple transactional scenarios.
     * Test multiple scenarios like dirty read or lost update to verify optimistic transaction support of the javasdk.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults All operations in array prot are executed successfully.
     */
    public <E extends Exception> void runTxScenariosTest(String scenarioName, EntityManagerFactory emf, Op[][] prot, E ex)
    throws InterruptedException, ConnectionException, IOException {

        //test data
        AnnotatedEntity ane = new TestEntity();
        JPATestUtils.initializeTestEntity(ane);
        ParentTestEntity pane = JPATestUtils.setMasterDetailRelationship(ane);
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) tx.begin();
        em.persist(pane);
        em.persist(ane);
        tx.commit();
        Thread.sleep(1000); //this is required as otherwise the first update might happen within the same second

        BlockingQueue<Long> semaphor = new ArrayBlockingQueue<Long>(3) {  };
        LinkedList<Throwable> excQ = new LinkedList<Throwable>();
        StringBuffer errLog = new StringBuffer("-----------------------------\nSCENARIO " + scenarioName + " START\n");

        for (int i = 0; i < prot.length; i++) {
            TestContext t = TestContext.get();
            new Thread(new ScriptRunner<E>(semaphor, excQ, errLog, t.getUserInfo(),
                    emf, ex, ane.getId(), ane.getClass(), pane.getId(), Arrays.asList(prot[i]))).start();
        }
        synchronized (excQ) {
            try {
                while (threadcount > 0) {
                    excQ.wait();
                    errLog.append("SCENARIO " + scenarioName + " END\n-----------------------------\n");
                }
            } catch (InterruptedException e1) {
                errLog.append("***SCENARIO " + scenarioName + " was interrupted.");
                excQ.add(e1);
            } finally {
                if (!excQ.isEmpty()) {
                    Iterator<Throwable> it = excQ.iterator();
                    while (it.hasNext()) {
                        Throwable error = it.next();
                        errLog.append("\n" + error.getCause());
                        errLog.append("\n" + error.getMessage());
                    }
                    Assert.fail("***FAILED SCENARIO: " + scenarioName + "\n" + errLog);
                }
                
                // Ensure the ForceServiceConnector ThreadLocal is cleaned up
                ForceServiceConnector.setThreadLocalConnectorConfig(null);
            }
        }
    }


    /**
     * Typical producer for producer-consumer pattern. Synchronization through {@link BlockingQueue}.
     * @author Dirk Hain
     * 
     */
    @SuppressWarnings("rawtypes")
    class ScriptRunner<E extends Exception> implements Runnable {

        protected BlockingQueue<Long> sema; //thread synchronization
        protected LinkedList<Throwable> exceptions; //test synchronization
        protected StringBuffer errLog; //shared StringBuffer between threads
        private EntityTransaction tx;
        private Stack<Op> protocol; //thread's protocol to run
        private EntityManager tem; //EM to use 
        private String entityId; //entity to work on
        private String parentId; //parent to work on
        private Class clazz;
        private Exception exType; //expected exception, null if none expected

        ScriptRunner(BlockingQueue<Long> q, LinkedList<Throwable> m, StringBuffer err, UserInfo ctx,
                EntityManagerFactory emf, E exc, String id, Class c, String parId, List<Op> p)
                throws ConnectionException, IOException {
            this.sema = q;
            this.exceptions = m;
            this.errLog = err;
            this.entityId = id;
            this.parentId = parId;
            this.clazz = c;
            this.exType = exc;
            protocol = new Stack<Op>();
            Collections.reverse(p); //reverse order since we use a stack during execution
            protocol.addAll(p);
            threadcount++;
            loadThreadContext(ctx);
            this.tem = emf.createEntityManager();
            this.tx = tem.getTransaction();
        }

        /**
         * Initializes the thread to access the dynamically created test org.
         */
        private void loadThreadContext(UserInfo info) throws ConnectionException, IOException {
            Assert.assertNotNull(info, "UserInfo is not specified.");
            Assert.assertNotNull(info.getUserName(), "UserName is not specified.");
            ForceConnectorConfig cc = new ForceConnectorConfig();
            cc.setUsername(info.getUserName());
            cc.setPassword(info.getPassword());
            cc.setAuthEndpoint(info.getServerEndpoint());
            service = Connector.newConnection(cc);
            ForceServiceConnector.setThreadLocalConnectorConfig(cc); //required so that this thread uses dynamic org
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            AnnotatedEntity testRef = (AnnotatedEntity) tem.find(clazz, entityId); //obtain a local copy to create concurrency
            ParentTestEntity parRef = tem.find(ParentTestEntity.class, parentId);
            try {
                while (true) {
                    if (protocol.empty()) return; //work for this thread is done
                    Op o = protocol.pop();
                    switch(o) {
                        case COMMIT:
                            String before = "";
                            Calendar commitedTime = testRef.getLastModifiedDate();
                            if (tx.isActive()) {
                                before = testRef.getId(); //Will lose Id if delete is committed.
                                tx.commit();
                                errLog.append(getThreadPrefix() + "Committed. "
                                                + "lMod before: " + getPrintDateMs(commitedTime) + " Id: " + before + "\n");
                            }
                            sema.put(Thread.currentThread().getId());
                            break;

                        case ROLLBACK:
                            if (tx.isActive()) {
                                tx.rollback();
                                errLog.append(getThreadPrefix() + "Rollback at " + getPrintDateMs(Calendar.getInstance()) + "\n");
                            }
                            sema.put(Thread.currentThread().getId());
                            break;

                        case READ:
                            testRef = tem.find(testRef.getClass(), entityId);
                            errLog.append(getThreadPrefix() + "Read :" + entityId
                                            + " lMod: " + getPrintDateMs(testRef.getLastModifiedDate()) + "\n");
                            sema.put(Thread.currentThread().getId());
                            break;

                        case UPDATE:
                            if (!tx.isActive()) tx.begin();
                            String beforeName = testRef.getName();
                            before = testRef.getId();
                            testRef.setName("Update_" + Long.toString(System.currentTimeMillis()));
                            tem.merge(testRef);
                            errLog.append(getThreadPrefix() + "Modified at " + getPrintDateMs(testRef.getLastModifiedDate())
                                            + ": " + before + ": " + beforeName + " to " + testRef.getName() + "\n");
                            Thread.sleep(1000); //scheduler swap after 1s
                            break;

                        case DELETE:
                            if (!tx.isActive()) tx.begin();
                            tem.merge(testRef);
                            tem.remove(testRef);
                            errLog.append(getThreadPrefix() + "Deleted: " + testRef.getId() + "\n");
                            Thread.sleep(1000); //scheduler swap after 1s
                            break;

                        case UPDATEwException:
                            try {
                                if (!tx.isActive()) tx.begin();
                                beforeName = testRef.getName();
                                before = testRef.getId();
                                testRef.setName("Update_" + Long.toString(System.currentTimeMillis()));
                                tem.merge(testRef);
                                errLog.append(getThreadPrefix() + "Attempted modification at "
                                                + getPrintDateMs(testRef.getLastModifiedDate()) + ": " + before
                                                + ": " + beforeName + " to " + testRef.getName() + "\n");
                                tx.commit();
                                Thread.sleep(1000); //scheduler swap after 1s
                                Assert.fail("Expected exception during update of " + entityId);
                            } catch (Exception ue) {
                                Assert.assertTrue(exType.getClass().isInstance(ue.getCause()),
                                        "Wrong exception type received: " + ue.getMessage());
                                Assert.assertTrue(ue.getCause().getMessage().contains(exType.getMessage()),
                                        "Wrong exception message on update. " + ue.getMessage());
                            }
                            break;

                        case DELETEwException:
                            try {
                                if (!tx.isActive()) tx.begin();
                                before = testRef.getId();
                                tem.merge(testRef);
                                tem.remove(testRef);
                                errLog.append(getThreadPrefix() + "Attempted deletion. "
                                              + "lMod: " + getPrintDateMs(testRef.getLastModifiedDate()) + ": " + before + "\n");
                                tx.commit();
                                Assert.fail("Expected exception during delete of " + entityId);
                            } catch (Exception de) {
                                Assert.assertTrue(exType.getClass().isInstance(de),
                                        "Wrong exception type received: " + de.getMessage());
                                Assert.assertTrue(de.getMessage().contains(exType.getMessage()),
                                        "Wrong exception message on delete. " + de.getMessage());
                            }
                            break;

                        case UpdEmb:
                            if (!tx.isActive()) tx.begin();
                            EmbeddedTestEntity ete = testRef.getEmbedded();
                            ete.setEmbedded("" + System.currentTimeMillis());
                            tem.merge(testRef);
                            errLog.append(getThreadPrefix() + "Modified embedded entity. "
                                            + "lMod: " + getPrintDateMs(testRef.getLastModifiedDate()) + "\n");
                            Thread.sleep(1000);
                            break;

                        case UpdEmbField:
                            if (!tx.isActive()) tx.begin();
                            before = testRef.getId();
                            ete = new EmbeddedTestEntity();
                            ete.setEmbedded("Upd_" + System.currentTimeMillis());
                            testRef.setEmbedded(ete);
                            tem.merge(testRef);
                            errLog.append(getThreadPrefix() + "Modified embedded field. "
                                            + "lMod: " + getPrintDateMs(testRef.getLastModifiedDate()) + "\n");
                            Thread.sleep(1000); //scheduler swap after 1s
                            break;

                        case UpdRel:
                            if (!tx.isActive()) tx.begin();
                            parRef.setName("new Parent name" + System.currentTimeMillis());
                            tem.merge(parRef);
                            errLog.append(getThreadPrefix() + "Modified parent. TestEntity "
                                            + "lMod: " + getPrintDateMs(testRef.getLastModifiedDate()) + "\n");
                            Thread.sleep(1000); //scheduler swap after 1s
                            break;

                        case UpdRelField:
                            if (!tx.isActive()) tx.begin();
                            Thread.sleep(1000); //scheduler swap after 1s
                            parRef.setName("new Parent name " + System.currentTimeMillis());
                            tem.merge(parRef);
                            testRef.setParent(parRef);
                            errLog.append(getThreadPrefix() + "Modified parent and parent ref. "
                                            + "TestEntity lMod: " + getPrintDateMs(testRef.getLastModifiedDate()) + "\n");
                            Thread.sleep(1000); //scheduler swap after 1s
                            break;

                        case UpdPckl:
                            if (!tx.isActive()) tx.begin();
                            testRef.setPickValueMulti(new PickValues[] {PickValues.THREE});
                            tem.merge(testRef);
                            errLog.append(getThreadPrefix() + "Modified picklist value. "
                                            + "lMod: " + getPrintDateMs(testRef.getLastModifiedDate()) + "\n");
                            Thread.sleep(1000); //scheduler swap after 1s
                            break;

                        case UpdTransient:
                            if (!tx.isActive()) tx.begin();
                            testRef.setUnused("Upd_" + System.currentTimeMillis());
                            tem.merge(testRef);
                            errLog.append(getThreadPrefix() + "Modified transient. "
                                            + "TestEntity lMod: " + getPrintDateMs(testRef.getLastModifiedDate()) + "\n");
                            break;

                        case SWITCHCONTROL:
                            sema.put(Thread.currentThread().getId());
                            break;

                        case WAIT:
                            long token = sema.take();
                            errLog.append(getThreadPrefix() + "Waited." + "\n");
                            if (token == Thread.currentThread().getId()) {
                                sema.put(token);
                                Thread.sleep(100);
                                this.protocol.push(o); //wait some more
                            }
                            break;
                         default:
                             Assert.fail("Unknown Op: " + o);
                    }
                }
            } catch (Throwable notExpected) {
                exceptions.add(notExpected);
                while (notExpected.getCause() != null) {
                    exceptions.add(notExpected.getCause());
                    notExpected = notExpected.getCause();
                }
            } finally {
                try {
                    sema.put(Thread.currentThread().getId()); //release any waiting threads
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                threadcount--;
                errLog.append(getThreadPrefix() + "End thread on " + getPrintDateMs(Calendar.getInstance()) + "\n");
                if (threadcount == 0) {
                    synchronized (exceptions) {
                        exceptions.notify();
                    }
                }
            }
        }
    }


    @Test
    /**
     * Tests for attribute with @Version.
     * Verifies that entities get versioned properly and that the attribute defined with @Version 
     * (typically {@link TestEntity.lastModifiedDate}) is updated upon change.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Version property {@link TestEntity.lastModifiedDate} is assigned a new value.
     */
    public void testEntityVersioning() throws InterruptedException {
        AnnotatedEntity a = new TestEntity();
        JPATestUtils.initializeTestEntity(a);
        ParentTestEntity p = JPATestUtils.setMasterDetailRelationship(a);
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) tx.begin();
        em.persist(p);
        em.persist(a);
        tx.commit();
        Thread.sleep(1000L);
        a = em.find(a.getClass(), a.getId());
        Calendar before = a.getLastModifiedDate();
        Assert.assertTrue(before != null, "@Version property was not created.");
        EntityTransaction t = em.getTransaction();
        if (!t.isActive()) t.begin();
        a.setStringObject("String_" + System.currentTimeMillis());
        em.merge(a);
        t.commit();
        a = em.find(a.getClass(), a.getId());
        Calendar after = a.getLastModifiedDate();
        Assert.assertTrue(after.after(before), "@Version property is not dated later than before the change.");
    }
    
    @Test
    /**
     * Update entity without @Version attribute.
     * Test that updating entities, one with @Version and one without, will still succeed.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Entities correctly persisted.
     */
    public void testEntitiesWithAndWithoutVersion() throws Exception {
        ParentTestEntity pte = new ParentTestEntity();
        pte.setName("testVersion" + System.currentTimeMillis());
        
        AccountEntity ae = new AccountEntity();
        ae.setName("testVersions" + System.currentTimeMillis());
        
        EntityTransaction tx = em3.getTransaction();
        tx.begin();
        try {
            em3.persist(ae);
            em3.persist(pte);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            Assert.fail(e.getMessage());
        }
        
        tx.begin();
        try {
            ParentTestEntity updatedPte = em3.find(ParentTestEntity.class, pte.getId());
            AccountEntity updatedAe = em3.find(AccountEntity.class, ae.getId());
            updatedPte.setName("testVersions" + System.currentTimeMillis());
            updatedAe.setName("testVersions" + System.currentTimeMillis());

            em3.merge(updatedAe);
            em3.merge(updatedPte);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            String failureMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            Assert.fail(failureMsg);
        }
    }


    @DataProvider
    public Object[][] txCollectionUpdate() throws NumberFormatException, MalformedURLException {
        Object [][] scenarios = new Object[][]{
            {"Collection update", emfac2},
            {"Collection update a-o-n", emfac3},
        };
        return scenarios;
    }


    @Test(dataProvider = "txCollectionUpdate")
    /**
     * Optimistic transaction with changes in collection properties.
     * Verify correct optimistic transaction behavior for changes in collection valued properties.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Collection is correctly updated.
     */
    public void testCollectionUpdate(String scenarioName, EntityManagerFactory emf) throws InterruptedException {
        final int numChildren = 3;
        ParentTestEntity parent = new ParentTestEntity();
        parent.init();
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) tx.begin();
        em.persist(parent);
        for (int i = 0; i < numChildren; i++) {
            TestEntity te = new TestEntity();
            JPATestUtils.initializeTestEntity(te);
            te.setParent(parent);
            te.setParentMasterDetail(parent);
            em.persist(te);
            parent.addTestEntity(te);
        }
        em.merge(parent);
        tx.commit();
        Thread.sleep(1000);
        runCollectionUpdateTest(emf, parent, numChildren);
    }

    private void runCollectionUpdateTest(EntityManagerFactory emFac, ParentTestEntity parent, int childCount)
    throws InterruptedException {
        EntityManager e1 = emFac.createEntityManager();
        EntityManager e2 = emFac.createEntityManager();
        ParentTestEntity p1 = e1.find(parent.getClass(), parent.getId());
        ParentTestEntity p2 = e2.find(parent.getClass(), parent.getId());

        EntityTransaction tx1 = e1.getTransaction();
        Assert.assertEquals(p1.getTestEntities().size(), childCount, "Wrong number of children before update");
        Assert.assertEquals(p2.getTestEntities().size(), childCount, "Wrong number of children before update");
        TestEntity newTE = new TestEntity();
        JPATestUtils.initializeTestEntity(newTE);
        newTE.setParent(p1);
        newTE.setParentMasterDetail(p1);
        p1.addTestEntity(newTE);
        /**
         * You have to touch the parent.
         * Adding a child only does not touch the parent 
         * since we do not persist parent to child relationship information.
         * Only the child has a foreignkey to the parent so parent object is 
         * not touched at all. Therefore there is no chance of optimistic failure 
         * on parent if you add a child or modify it.
         */
        p1.setName("Upd1_" + System.currentTimeMillis());
        if (!tx1.isActive()) tx1.begin();
        e1.merge(newTE);
        e1.merge(p1);
        tx1.commit();
        Thread.sleep(1000);
        //re-read and verify update
        p1 = e1.find(parent.getClass(), parent.getId());
        Assert.assertEquals(p1.getTestEntities().size(), childCount + 1, "Wrong number of children after update");
        Assert.assertEquals(p2.getTestEntities().size(), childCount, "Wrong number of children after update on unchanged parent");
        //attempt concurrent update
        p2.setName("Upd2_" + System.currentTimeMillis());
        EntityTransaction tx2 = e2.getTransaction();
        if (!tx2.isActive()) tx2.begin();
        e2.merge(p2);
        try {
            tx2.commit();
            Assert.fail("Expected exception during concurrent update of collection valued property");
        } catch (RollbackException re) {
            Assert.assertTrue(OLE.getClass().isInstance(re.getCause()),
                    "Wrong exception type received: " + re.getCause().getMessage());
            Assert.assertTrue(re.getCause().getMessage().contains(OLE.getMessage()),
                    "Wrong exception message on delete. " + re.getCause().getMessage());
        }
    }

    /**
     * Generates prefix (hopefully) specific to thread. Prefix might be equal if
     * several thread numbers are in the same equivalence class for %25.
     */
    static String getThreadPrefix() {
        int id = Long.valueOf(Thread.currentThread().getId()).intValue();
        if (id > 25) id = id % 25;
        StringBuffer prefix = new StringBuffer();
        for (int i = 0; i < id; i++) {
            prefix.append('|');
        }
        return prefix.toString();
    }

    /**
     * Helper for millisecond time stamps.
     * @param cal
     * @return
     */
    static String getPrintDateMs(Calendar cal) {
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        int h = cal.get(Calendar.HOUR_OF_DAY);
        int m = cal.get(Calendar.MINUTE);
        int s = cal.get(Calendar.SECOND);
        int ms = cal.get(Calendar.MILLISECOND);
        return h + ":" + m + ":" + s + ":" + ms;
    }
}
