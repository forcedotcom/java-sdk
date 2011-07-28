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

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.JPATestUtils.Digit;
import com.force.sdk.jpa.entities.*;
import com.force.sdk.jpa.entities.FolderEntity.FolderType;
import com.force.sdk.jpa.query.QueryHints;
import com.force.sdk.qa.util.jpa.BaseJPAFTest;
import com.sforce.ws.ConnectionException;

/**
 * 
 * Tests for bulk delete. Batching starts to occur if more than 200 entities are deleted.
 *
 * @author Dirk Hain
 */
public class DeleteBulkTest extends BaseJPAFTest {
    
    protected ParentTestEntity parent;
    protected ParentTestEntity parentMD;
    
    @BeforeClass(dependsOnMethods = "initialize")
    protected void init() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);
    }

    @AfterClass
    protected void classTearDown() {
        deleteAll(ParentTestEntity.class);
        deleteAll(TestEntity.class);
    }

    @BeforeClass(dependsOnMethods = "init")
    protected void initTestData() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int count = 0;
            parent = new ParentTestEntity();
            parent.init();
            em.persist(parent);
            parentMD = new ParentTestEntity();
            parentMD.init();
            em.persist(parentMD);
            while (count < 210) {
                for (Digit d : Digit.values()) {
                    TestEntity entity = new TestEntity();
                    JPATestUtils.initializeTestEntity(entity, d);
                    entity.setParent(parent);
                    entity.setParentMasterDetail(parentMD);
                    em.persist(entity);
                    count++;
                }
            }
            em.flush();
            tx.commit();
            tx = null;
        } finally {
            if (tx != null) {
                tx.rollback();
            }
        }
    }
        
    @Test
    /**
     * Test that more than 200 rows can be deleted by one delete instruction.
     * Verify that 210 TestEntity and 2 ParentTestEntity instances are present. Delete all TestEntity instances by one
     * command and verify that it retunrs 210. Verify that TestEntity has zero instances left.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults Zero TestEntity instances left in store.
     */
    public void testBulkdelete() {
        final String queryBase = "select o from " + TestEntity.class.getSimpleName() + " o ";
        Assert.assertEquals(em.createQuery(queryBase).getResultList().size(), 210,
                            "Setup did not create all TestEntity objects.");
        final String queryBaseParent = "select o from " + ParentTestEntity.class.getSimpleName() + " o ";
        Assert.assertEquals(em.createQuery(queryBaseParent).getResultList().size(), 2, "Wrong number of parent objects");
        final String deleteQuery = "delete from " + TestEntity.class.getSimpleName();
        int deleted = em.createQuery(deleteQuery).executeUpdate();
        Assert.assertEquals(deleted, 210, "Not all entities were deleted.");
        Assert.assertEquals(em.createQuery(queryBase).getResultList().size(), 0, "Still found TestEntity rows.");
    }

    @Test
    /**
     * Bulk delete and empty recycle bin.
     * Deletes all in the document and folder to ensure clean environment,
     * 1. creates a folder and document in the folder, deletes the document without empty-recycle-bin hint
     * and test that deleting the folder fails exception,
     * 2. Explicitly cleans the recycle-bin by calling connection.emptyRecycleBin(the doc id),
     * Verifies that bulk delete with query hint also empties recycle bin.
     * @hierarchy javasdk
     * @userStory xyz
     * @expectedResults deleting folder should throw exception when a child document is in recycle bin. Deleting folder
     * should pass when the child document was deleted with EMPTY_RECYCLE_BIN hint.
     */
    public void testBulkdeleteAndEmptyRecycleBin() throws ConnectionException {
        // Do some cleanup
        final String folderName = "DeleteBulkTest";
        final String documentName = "DeleteBulkTestDoc";
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.createQuery("delete from Document d where d.name = '" + documentName + "'")
                .setHint(QueryHints.EMPTY_RECYCLE_BIN, true).executeUpdate();
        /**
         * If there is a failure here and it happens when run in an existing org. Please manually login to the org and empty
         * the recycle bin. The reason is some earlier test may have failed and left items in the recycle bin to cause conflict.
         */
        em.createQuery("delete from Folder f where f.name = '" + folderName + "'").executeUpdate();
        tx.commit();
        
        FolderEntity folder = addFolder(folderName);
        DocumentEntity document = addDocument(documentName, folder);
        
        // Now delete document and then try to delete folder
        tx.begin();
        em.createQuery("delete from Document d where d.name = '" + documentName + "'").executeUpdate();
        tx.commit();
        try {
            tx.begin();
            em.createQuery("delete from Folder f where f.name = '" + folderName + "'").executeUpdate();
            Assert.fail("Should not be able to delete folder");
        } catch (PersistenceException pe) {
            tx.rollback();
            Assert.assertTrue(pe.getMessage()
                    .contains("Your attempt to delete DeleteBulkTest cannot be completed "
                                + "because it contains the following documents. "
                                + "If any of these documents are in the Recycle Bin, "
                                + "you must empty your Recycle Bin before deleting the folder.: DeleteBulkTestDoc"),
                    pe.getMessage());
        }
        // Recycle/ undelete recycle-bin now
        String[] cleanIds = { document.getId() };
        service.emptyRecycleBin(cleanIds);
        
        tx.begin();
        // Now delete document with hint and then try to delete folder
        em.createQuery("delete from Document d where d.name = '" + documentName + "'")
                .setHint(QueryHints.EMPTY_RECYCLE_BIN, true).executeUpdate();
        em.createQuery("delete from Folder f where f.name = '" + folderName + "'").executeUpdate();
        tx.commit();
    }

    private FolderEntity addFolder(String folderName) {
        FolderEntity folder = new FolderEntity();
        folder.setName(folderName);
        folder.setDeveloperName(folderName);
        folder.setAccessType(com.force.sdk.jpa.entities.FolderEntity.AccessType.Public);
        folder.setType(FolderType.Document);
        
        em.getTransaction().begin();
        em.persist(folder);
        em.getTransaction().commit();
        return folder;
    }
    
    private DocumentEntity addDocument(String name, FolderEntity folder) {
        final byte[] data = {1, 2, 3, 4, 5, 6};
        DocumentEntity d = new DocumentEntity();
        d.setBody(data);
        d.setName(name);
        d.setFolder(folder);
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        return d;
    }
}
