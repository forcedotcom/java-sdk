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

import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.entities.cascade.*;
import com.force.sdk.qa.util.BaseMultiEntityManagerJPAFTest;

/**
 * Class for Cascade CRUD Tests.
 * 
 * @author Jeff Lai
 */
public class CascadeCRUDTest extends BaseMultiEntityManagerJPAFTest {

    private static final String CHILD_NAME = "entity";
    private static final String PARENT_NAME = "Parent";
    
    @Test
    public void testBasicCascadePersist() {
        CascadeChildTestEntity child = new CascadeColChildTestEntity();
        child.setName(CHILD_NAME);
        CascadeParentTestEntity parent = new CascadeColParentTestEntity();
        parent.setName(PARENT_NAME);
        parent.addChild(child);
        child.setParent(parent);
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.persist(parent);
        tx.commit();
    }
    
    @Test
    public void testBasicOneToManyCRUDForCollection() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em, CascadeColParentTestEntity.class, CascadeColChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForCollection() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em, CascadeColMDParentTestEntity.class, CascadeColMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForCollectionOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em2, CascadeColParentTestEntity.class, CascadeColChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForCollectionOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em2, CascadeColMDParentTestEntity.class, CascadeColMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForCollectionAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em3, CascadeColParentTestEntity.class, CascadeColChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForCollectionAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em3, CascadeColMDParentTestEntity.class, CascadeColMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForList() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em, CascadeListParentTestEntity.class, CascadeListChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForList() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em, CascadeListMDParentTestEntity.class, CascadeListMDChildTestEntity.class);
    }

    @Test
    public void testBasicOneToManyCRUDForListOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em2, CascadeListParentTestEntity.class, CascadeListChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForListOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em2, CascadeListMDParentTestEntity.class, CascadeListMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForListAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em3, CascadeListParentTestEntity.class, CascadeListChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForListAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em3, CascadeListMDParentTestEntity.class, CascadeListMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForSet() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em, CascadeSetParentTestEntity.class, CascadeSetChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForSet() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em, CascadeSetMDParentTestEntity.class, CascadeSetMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForSetOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em2, CascadeSetParentTestEntity.class, CascadeSetChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForSetOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em2, CascadeSetMDParentTestEntity.class, CascadeSetMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForSetAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em3, CascadeSetParentTestEntity.class, CascadeSetChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForSetAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em3, CascadeSetMDParentTestEntity.class, CascadeSetMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForMap() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em, CascadeMapParentTestEntity.class, CascadeMapChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForMap() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em, CascadeMapMDParentTestEntity.class, CascadeMapMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForMapOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em2, CascadeMapParentTestEntity.class, CascadeMapChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForMapOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em2, CascadeMapMDParentTestEntity.class, CascadeMapMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyCRUDForMapAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em3, CascadeMapParentTestEntity.class, CascadeMapChildTestEntity.class);
    }
    
    @Test
    public void testBasicOneToManyMDCRUDForMapAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicOneToManyCRUDInternal(em3, CascadeMapMDParentTestEntity.class, CascadeMapMDChildTestEntity.class);
    }
    
    @SuppressWarnings("unchecked")
    public void testBasicOneToManyCRUDInternal(EntityManager emm, Class<? extends CascadeParentTestEntity> parentClazz,
            Class<? extends CascadeChildTestEntity> childClazz)
    throws InstantiationException, IllegalAccessException {
        // Cleanup any old data
        deleteAll(childClazz);
        deleteAll(parentClazz);
        
        // setup data
        CascadeChildTestEntity child1 = childClazz.newInstance();
        CascadeChildTestEntity child2 = childClazz.newInstance();
        child1.setName(CHILD_NAME + "1");
        child2.setName(CHILD_NAME + "2");
        CascadeParentTestEntity parent = parentClazz.newInstance();
        parent.setName(PARENT_NAME);
        parent.addChild(child1);
        child1.setParent(parent);
        parent.addChild(child2);
        child2.setParent(parent);
        EntityTransaction tx = emm.getTransaction();

        tx.begin();
        emm.persist(parent);
        tx.commit();
        parent = checkParentAndChild(emm, 1, 2, parentClazz, childClazz);
        
        // Add a third child to parent
        CascadeChildTestEntity child3 = childClazz.newInstance();
        child3.setName(CHILD_NAME + "3");
        parent.addChild(child3);
        child3.setParent(parent);
        tx.begin();
        parent = emm.merge(parent);
        tx.commit();
        parent = checkParentAndChild(emm, 1, 3, parentClazz, childClazz);
            
        // delete test
        tx = emm.getTransaction();
        tx.begin();
        parent = emm.find(parentClazz, parent.getId());
        emm.remove(parent);
        tx.commit();
        List<CascadeParentTestEntity> parents =
            emm.createQuery("select p from " + parentClazz.getSimpleName() + " p").getResultList();
        Assert.assertEquals(parents.size(), 0, "parent entity should have been removed");
        List<CascadeChildTestEntity> children =
            emm.createQuery("select c from " + childClazz.getSimpleName() + " c").getResultList();
        Assert.assertEquals(children.size(), 0, "child entity should have been removed");
    }
    
    @Test
    public void testBasicCascadeAllCRUD() throws InstantiationException, IllegalAccessException {
        testBasicCascadeAllCRUDInternal(em, CascadeColParentTestEntity.class, CascadeColChildTestEntity.class);
    }
    
    @Test
    public void testBasicMDCascadeAllCRUD() throws InstantiationException, IllegalAccessException {
        testBasicCascadeAllCRUDInternal(em, CascadeColMDParentTestEntity.class, CascadeColMDChildTestEntity.class);
    }
       
    @Test
    public void testBasicCascadeAllCRUDOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicCascadeAllCRUDInternal(em2, CascadeColParentTestEntity.class, CascadeColChildTestEntity.class);
    }
    
    @Test
    public void testBasicMDCascadeAllCRUDOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicCascadeAllCRUDInternal(em2, CascadeColMDParentTestEntity.class, CascadeColMDChildTestEntity.class);
    }
    
    @Test
    public void testBasicCascadeAllCRUDAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicCascadeAllCRUDInternal(em3, CascadeColParentTestEntity.class, CascadeColChildTestEntity.class);
    }

    @Test
    public void testBasicMDCascadeAllCRUDAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicCascadeAllCRUDInternal(em3, CascadeColMDParentTestEntity.class, CascadeColMDChildTestEntity.class);
    }
    
    @SuppressWarnings("unchecked")
    public void testBasicCascadeAllCRUDInternal(EntityManager emm, Class<? extends CascadeParentTestEntity> parentClazz,
            Class<? extends CascadeChildTestEntity> childClazz) throws InstantiationException, IllegalAccessException {
        // Cleanup any old data
        deleteAll(childClazz);
        deleteAll(parentClazz);
        
        // setup data
        CascadeChildTestEntity child1 = childClazz.newInstance();
        CascadeChildTestEntity child2 = childClazz.newInstance();
        child1.setName(CHILD_NAME + "1");
        child2.setName(CHILD_NAME + "2");
        CascadeParentTestEntity parent = parentClazz.newInstance();
        parent.setName(PARENT_NAME);
        parent.addChild(child1);
        child1.setParent(parent);
        parent.addChild(child2);
        child2.setParent(parent);
        EntityTransaction tx = emm.getTransaction();

        tx.begin();
        emm.persist(parent);
        tx.commit();
        // This validates CascadeType.PERSIST, child count 2
        parent = checkParentAndChild(emm, 1, 2, parentClazz, childClazz);
        
        // Add a third child to parent and change a value to an old child
        CascadeChildTestEntity child3 = childClazz.newInstance();
        child3.setName(CHILD_NAME + "3");
        parent.addChild(child3);
        child3.setParent(parent);
        CascadeChildTestEntity childAny =
            (CascadeChildTestEntity) ((CascadeParentTestEntity2) parent).getChildren().iterator().next();
        final String testChildName = CHILD_NAME + "Test";
        childAny.setName(testChildName);
        tx.begin();
        parent = emm.merge(parent);
        tx.commit();
        // This validates CascadeType.MERGE
        parent = checkParentAndChild(emm, 1, 3, parentClazz, childClazz);
        Iterator<? extends CascadeChildTestEntity> iter = ((CascadeParentTestEntity2) parent).getChildren().iterator();
        boolean updateFound = false;
        while (iter.hasNext()) {
            childAny = iter.next();
            if (testChildName.equals(childAny.getName())) {
                updateFound = true;
                break;
            }
        }
        Assert.assertTrue(updateFound, "CascadeType.All did not update child");

        // Check refresh here
        /**
         * Refresh is tricky since it does not work on detached objects.
         * We therefore create a transaction and have to do a refresh within it.
         */
        tx.begin();
        parent = emm.find(parentClazz, parent.getId());
        childAny = (CascadeChildTestEntity) ((CascadeParentTestEntity2) parent).getChildren().iterator().next();
        final String testChildNameTemp = CHILD_NAME + "Temp";
        childAny.setName(testChildNameTemp);
        emm.refresh(parent);
        iter = ((CascadeParentTestEntity2) parent).getChildren().iterator();
        boolean updateStillExists = false;
        while (iter.hasNext()) {
            childAny = iter.next();
            if (testChildNameTemp.equals(childAny.getName())) {
                updateStillExists = true;
                break;
            }
        }
        Assert.assertFalse(updateStillExists, "CascadeType.ALL did not refresh children");
        tx.commit();
        
        // delete test
        tx = emm.getTransaction();
        tx.begin();
        try {
            parent = emm.find(parentClazz, parent.getId());
            emm.remove(parent);
        } finally {
            tx.commit();
        }
        tx.begin();
        try {
            List<CascadeParentTestEntity> parents =
                emm.createQuery("select p from " + parentClazz.getSimpleName() + " p").getResultList();
            Assert.assertEquals(parents.size(), 0, "parent entity should have been removed");
            List<CascadeChildTestEntity> children =
                emm.createQuery("select c from " + childClazz.getSimpleName() + " c").getResultList();
            Assert.assertEquals(children.size(), 0, "child entity should have been removed");
        } finally {
            tx.commit();
        }
    }
    
    @Test
    public void testBasicCascadeNoneCRUD() throws InstantiationException, IllegalAccessException {
        testBasicCascadeNoneCRUDInternal(em, CascadeNoneColParentTestEntity.class,
                                            CascadeNoneColChildTestEntity.class, false);
    }
    
    @Test
    public void testBasicMDCascadeNoneCRUD() throws InstantiationException, IllegalAccessException {
        testBasicCascadeNoneCRUDInternal(em, CascadeNoneColMDParentTestEntity.class,
                                            CascadeNoneColMDChildTestEntity.class, true);
    }
    
    @Test
    public void testBasicCascadeNoneCRUDOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicCascadeNoneCRUDInternal(em2, CascadeNoneColParentTestEntity.class,
                                            CascadeNoneColChildTestEntity.class, false);
    }
    
    @Test
    public void testBasicMDCascadeNoneCRUDOptimistic() throws InstantiationException, IllegalAccessException {
        testBasicCascadeNoneCRUDInternal(em2, CascadeNoneColMDParentTestEntity.class,
                                            CascadeNoneColMDChildTestEntity.class, true);
    }
    
    @Test
    public void testBasicCascadeNoneCRUDAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicCascadeNoneCRUDInternal(em3, CascadeNoneColParentTestEntity.class,
                                            CascadeNoneColChildTestEntity.class, false);
    }
    
    @Test
    public void testBasicMDCascadeNoneCRUDAllOrNothing() throws InstantiationException, IllegalAccessException {
        testBasicCascadeNoneCRUDInternal(em3, CascadeNoneColMDParentTestEntity.class,
                                            CascadeNoneColMDChildTestEntity.class, true);
    }
    
    @SuppressWarnings("unchecked")
    public void testBasicCascadeNoneCRUDInternal(EntityManager emm, Class<? extends CascadeParentTestEntity> parentClazz,
            Class<? extends CascadeChildTestEntity> childClazz, boolean alwaysCascadeDelete)
    throws InstantiationException, IllegalAccessException {
        // Cleanup any old data
        deleteAll(childClazz);
        deleteAll(parentClazz);
        
        // setup data
        CascadeChildTestEntity child1 = childClazz.newInstance();
        CascadeChildTestEntity child2 = childClazz.newInstance();
        child1.setName(CHILD_NAME + "1");
        child2.setName(CHILD_NAME + "2");
        CascadeParentTestEntity parent = parentClazz.newInstance();
        parent.setName(PARENT_NAME);
        parent.addChild(child1);
        child1.setParent(parent);
        parent.addChild(child2);
        child2.setParent(parent);
        EntityTransaction tx = emm.getTransaction();

        tx.begin();
        emm.persist(parent);
        // Since this is cascade none we have to persist children separately
        emm.persist(child1);
        emm.persist(child2);
        tx.commit();
        // This validates no CascadeType, child count 2
        parent = checkParentAndChild(emm, 1, 2, parentClazz, childClazz);
        
        // Add a third child to parent and change a value to an old child
        CascadeChildTestEntity child3 = childClazz.newInstance();
        child3.setName(CHILD_NAME + "3");
        parent.addChild(child3);
        child3.setParent(parent);
        tx.begin();
        emm.persist(child3);
        tx.commit();
        // This validates no CascadeType, child count 3
        parent = checkParentAndChild(emm, 1, 3, parentClazz, childClazz);
        
        CascadeChildTestEntity childAny =
            (CascadeChildTestEntity) ((CascadeParentTestEntity2) parent).getChildren().iterator().next();
        final String testChildName = CHILD_NAME + "Test";
        childAny.setName(testChildName);
        tx.begin();
        emm.merge(parent);
        tx.commit();
        // This means that CascadeType.MERGE is always true even if not set.
        // Child count 1 and the above merge of parent merged child as well
        parent = checkParentAndChild(emm, 1, 3, parentClazz, childClazz);
        Iterator<? extends CascadeChildTestEntity> iter = ((CascadeParentTestEntity2) parent).getChildren().iterator();
        boolean updateFound = false;
        while (iter.hasNext()) {
            childAny = iter.next();
            if (testChildName.equals(childAny.getName())) {
                updateFound = true;
                break;
            }
        }
        Assert.assertTrue(updateFound, "CascadeType none did not update the child, maybe CascadeType.MERGE is fixed now");

        // Check refresh here
        /**
         * Refresh is tricky since it does not work on detached objects.
         * We therefore create a transaction and have to do a refresh within it.
         */
        tx.begin();
        parent = emm.find(parentClazz, parent.getId());
        childAny = (CascadeChildTestEntity) ((CascadeParentTestEntity2) parent).getChildren().iterator().next();
        final String testChildNameTemp = CHILD_NAME + "Temp";
        childAny.setName(testChildNameTemp);
        emm.refresh(parent);
        iter = ((CascadeParentTestEntity2) parent).getChildren().iterator();
        boolean updateStillExists = false;
        while (iter.hasNext()) {
            childAny = iter.next();
            if (testChildNameTemp.equals(childAny.getName())) {
                updateStillExists = true;
                break;
            }
        }
        Assert.assertFalse(updateStillExists, "CascadeType none did not refresh child, maybe CascadeType.REFRESH is fixed now");
        tx.commit();
        
        // delete test
        tx = emm.getTransaction();
        tx.begin();
        emm.remove(emm.find(parent.getClass(), parent.getId()));
        tx.commit();
        
        tx.begin();
        List<CascadeParentTestEntity> parents;
        List<CascadeChildTestEntity> children;
        try {
            parents = emm.createQuery("select p from " + parentClazz.getSimpleName() + " p").getResultList();
            Assert.assertEquals(parents.size(), 0, "parent entity should have been removed");
            children = emm.createQuery("select c from " + childClazz.getSimpleName() + " c").getResultList();
            if (alwaysCascadeDelete) {
                Assert.assertEquals(children.size(), 0, "child entity should have been removed");
                return; // no need to delete child
            } else {
                Assert.assertEquals(children.size(), 3, "child entity should not have been removed");
            }
        } finally {
            tx.commit();
        }

        // Remove child separately
        tx = emm.getTransaction();
        tx.begin();
        emm.remove(emm.find(children.get(0).getClass(), children.get(0).getId()));
        emm.remove(emm.find(children.get(1).getClass(), children.get(1).getId()));
        emm.remove(emm.find(children.get(2).getClass(), children.get(2).getId()));
        tx.commit();
        tx.begin();
        try {
            children =
                emm.createQuery("select c from " + CascadeNoneColChildTestEntity.class.getSimpleName() + " c").getResultList();
            Assert.assertEquals(children.size(), 0, "child entity should have been removed");
        } finally {
            tx.commit();
        }
    }
       
    @SuppressWarnings("unchecked")
    private CascadeParentTestEntity checkParentAndChild(EntityManager emm, int parentCount, int childCount,
            Class<? extends CascadeParentTestEntity> parentClazz, Class<? extends CascadeChildTestEntity> childClazz) {
        List<CascadeParentTestEntity> parents;
        List<CascadeChildTestEntity> children;
        // We need to have a transaction here to avaoid getting old data from cache
        emm.getTransaction().begin();
        parents = emm.createQuery("select p from " + parentClazz.getSimpleName() + " p").getResultList();
        Assert.assertEquals(parents.size(), parentCount, "parent entity not setup properly");
        CascadeParentTestEntity parent = parents.get(0);
        Assert.assertEquals(parent.getName(), PARENT_NAME, "parent entity not setup properly");
        children = emm.createQuery("select c from " + childClazz.getSimpleName() + " c").getResultList();
        Assert.assertEquals(children.size(), childCount, "child entity not setup properly");
        if (children.size() > 0) {
            Assert.assertTrue(children.get(0).getName().startsWith(CHILD_NAME), "child entity not setup properly");
        }
        Assert.assertEquals(parent.getChildrenSize(), childCount, "parent pulled all the children");
        emm.getTransaction().commit();

        return parent;
    }
}
