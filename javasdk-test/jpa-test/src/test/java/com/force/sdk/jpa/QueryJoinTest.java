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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.entities.ParentTestEntity;
import com.force.sdk.jpa.entities.TestEntity;
import com.force.sdk.jpa.entities.cascade.*;
import com.force.sdk.jpa.query.QueryHints;
import com.force.sdk.qa.util.BaseJPAFTest;
import com.google.inject.internal.Lists;

/**
 * Functional tests for JPA query joins.
 *
 * @author Fiaz Hossain, Dirk Hain
 */
public class QueryJoinTest extends BaseJPAFTest {

    @BeforeMethod
    public void initTestData() {
        deleteAll(TestEntity.class);
        deleteAll(ParentTestEntity.class);
        
        TestEntity entity1 = new TestEntity();
        JPATestUtils.initializeTestEntity(entity1);
        entity1.setName("entity1");
        entity1.setBoolType(true);
        entity1.setIntType(1);
        TestEntity entity2 = new TestEntity();
        JPATestUtils.initializeTestEntity(entity2);
        entity2.setName("entity2");
        entity2.setBoolType(false);
        entity2.setIntType(2);
        TestEntity entity3 = new TestEntity();
        JPATestUtils.initializeTestEntity(entity3);
        entity3.setName("entityXX");
        entity3.setBoolType(false);
        entity3.setIntType(3);
        ParentTestEntity parent1 = new ParentTestEntity();
        parent1.setName("Parent1");
        entity1.setParent(parent1);
        entity1.setParentMasterDetail(parent1);
        ParentTestEntity parent2 = new ParentTestEntity();
        parent2.setName("Parent2");
        entity2.setParent(parent2);
        entity2.setParentMasterDetail(parent2);
        ParentTestEntity parent3 = new ParentTestEntity();
        parent3.setName("Parent3");
        entity3.setParent(parent3);
        entity3.setParentMasterDetail(parent3);
        
        addTestDataInTx(Lists.newArrayList(parent1, entity1, parent2, entity2, parent3, entity3));
        
        // These object are needed for Map based related objects
        deleteAll(CascadeMapChildTestEntity.class);
        deleteAll(CascadeMapParentTestEntity.class);
        
        CascadeMapChildTestEntity entity11 = new CascadeMapChildTestEntity();
        entity11.setName("entity1");
        entity11.setIntValue(1);
        CascadeMapChildTestEntity entity21 = new CascadeMapChildTestEntity();
        entity21.setName("entity2");
        entity21.setIntValue(2);
        CascadeMapChildTestEntity entity31 = new CascadeMapChildTestEntity();
        entity31.setName("entityXX");
        entity31.setIntValue(3);
        CascadeMapParentTestEntity parent11 = new CascadeMapParentTestEntity();
        parent11.setName("Parent1");
        entity11.setParent(parent11);
        CascadeMapParentTestEntity parent21 = new CascadeMapParentTestEntity();
        parent21.setName("Parent2");
        entity21.setParent(parent21);
        CascadeMapParentTestEntity parent31 = new CascadeMapParentTestEntity();
        parent31.setName("Parent3");
        entity31.setParent(parent31);

        CascadeMapChildTestEntity entity41 = new CascadeMapChildTestEntity();
        entity41.setName("blahblah4");
        entity41.setIntValue(4);
        entity41.setParent(parent11);
        CascadeMapChildTestEntity entity51 = new CascadeMapChildTestEntity();
        entity51.setName("entity5");
        entity51.setIntValue(5);
        entity51.setParent(parent11);

        addTestDataInTx(Lists.newArrayList(parent11, entity41, entity51, entity11, parent21, entity21, parent31, entity31));
        
        // These object are needed for Collection based related objects
        deleteAll(CascadeColChildTestEntity.class);
        deleteAll(CascadeColParentTestEntity.class);
        
        CascadeColChildTestEntity entity1c1 = new CascadeColChildTestEntity();
        entity1c1.setName("entity1");
        CascadeColChildTestEntity entity1c11 = new CascadeColChildTestEntity();
        entity1c11.setName("entity11");
        CascadeColChildTestEntity entity2c1 = new CascadeColChildTestEntity();
        entity2c1.setName("entity2");
        CascadeColChildTestEntity entity3c1 = new CascadeColChildTestEntity();
        entity3c1.setName("entity1");
        CascadeColParentTestEntity parent1c1 = new CascadeColParentTestEntity();
        parent1c1.setName("Parent1");
        entity1c1.setParent(parent1c1);
        entity1c11.setParent(parent1c1);
        CascadeColParentTestEntity parent2c1 = new CascadeColParentTestEntity();
        parent2c1.setName("Parent2");
        entity2c1.setParent(parent2c1);
        CascadeColParentTestEntity parent3c1 = new CascadeColParentTestEntity();
        parent3c1.setName("Parent3");
        entity3c1.setParent(parent3c1);
        CascadeColParentTestEntity parent4c1 = new CascadeColParentTestEntity();
        parent4c1.setName("Parent4");
        CascadeColParentTestEntity parent5c1 = new CascadeColParentTestEntity();
        parent5c1.setName("Parent5");

        addTestDataInTx(Lists.newArrayList(parent1c1, entity1c1, entity1c11, parent2c1, entity2c1,
                                            parent3c1, entity3c1, parent4c1, parent5c1));
    }
    
    @AfterMethod
    protected void classTearDown() {
        deleteAll(TestEntity.class);
        deleteAll(ParentTestEntity.class);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testKeyValueInSemiJoin() {
        // Try to get children from a Map on the parent
        List<CascadeMapParentTestEntity> jpqlResult =
            em.createQuery("select o from " + CascadeMapParentTestEntity.class.getSimpleName() + " o "
                            + "JOIN o.children c "
                            + "where key(c) = 'entity1' order by o.name").getResultList();
        Assert.assertEquals(jpqlResult.size(), 3);
        Assert.assertEquals(jpqlResult.get(0).getChildren().size(), 1,
                "We did not get right value: " + jpqlResult.get(0).getChildren().size());
        
        // Try to select the key this time
        List<Object[]> jpqlObjResult =
            em.createQuery("select o.name, key(c) from " + CascadeMapParentTestEntity.class.getSimpleName() + " o "
                            + "JOIN o.children c "
                            + "where key(c) = 'entity1' order by o.name").getResultList();
        Assert.assertEquals(jpqlObjResult.size(), 3);
        Assert.assertTrue(jpqlObjResult.get(0)[0].equals("Parent1"), "We did not get right value: " + jpqlObjResult.get(0)[0]);
        List<String> keyResult = (List<String>) jpqlObjResult.get(0)[1];
        Assert.assertTrue(keyResult.get(0).equals("entity1"), "We did not get right value: " + keyResult.get(0));
        
        // Try to select the value this time
        jpqlObjResult =
            em.createQuery("select o.name, value(c) from " + CascadeMapParentTestEntity.class.getSimpleName() + " o "
                            + "JOIN o.children c "
                            + "where key(c) = 'entity1' order by o.name").getResultList();
        Assert.assertEquals(jpqlObjResult.size(), 3);
        Assert.assertTrue(jpqlObjResult.get(0)[0].equals("Parent1"), "We did not get right value: " + jpqlObjResult.get(0)[0]);
        List<CascadeMapChildTestEntity> childResult = (List<CascadeMapChildTestEntity>) jpqlObjResult.get(0)[1];
        Assert.assertEquals(childResult.get(0).getIntValue(), 1,
                "We did not get right value: " + childResult.get(0).getIntValue());

        // Try to select the entry this time
        jpqlObjResult =
            em.createQuery("select o.name, entry(c) from " + CascadeMapParentTestEntity.class.getSimpleName() + " o "
                            + "JOIN o.children c "
                            + "where key(c) = 'entity1' order by o.name").getResultList();
        Assert.assertEquals(jpqlObjResult.size(), 3);
        Assert.assertTrue(jpqlObjResult.get(0)[0].equals("Parent1"), "We did not get right value: " + jpqlObjResult.get(0)[0]);
        Assert.assertTrue(keyResult.get(0).equals("entity1"), "We did not get right value: " + keyResult.get(0));

        List<Map.Entry<String, CascadeMapChildTestEntity>> childEntryResult =
            (List<Map.Entry<String, CascadeMapChildTestEntity>>) jpqlObjResult.get(0)[1];
        Assert.assertTrue(childEntryResult.get(0).getKey().equals("entity1"),
                "We did not get right value: " + childEntryResult.get(0).getKey());
        Assert.assertEquals(childEntryResult.get(0).getValue().getIntValue(), 1,
                "We did not get right value: " + childEntryResult.get(0).getValue().getIntValue());
        
        // Try two level where filters
        jpqlResult =
            em.createQuery("select o from " + CascadeMapParentTestEntity.class.getSimpleName() + " o "
                    + "JOIN o.children c "
                    + "where key(c) = 'entity1' and o.name = 'Parent1'").getResultList();
        Assert.assertEquals(jpqlResult.size(), 1);
        Assert.assertEquals(jpqlResult.get(0).getChildren().size(), 1,
                "We did not get right value: " + jpqlResult.get(0).getChildren().size());
        
        // Now make sure a reload does not change the value
        // FIXME - make this work somehow!!
        //em.refresh(jpqlResult.get(0));
        Assert.assertEquals(jpqlResult.get(0).getChildren().size(), 1,
                "We did not get right value: " + jpqlResult.get(0).getChildren().size());
        
        /**
         * This is a Datanucleus bug it can't parse expressions like value(c).intValue. JPA examples do have this expression.
         */
        //jpqlResult =
        //    em.createQuery("select o from " + CascadeMapParentTestEntity.class.getSimpleName() + " o "
        //                    + "JOIN o.children c "
        //                    + "where value(c).intValue = 5 order by o.name").getResultList();
        //Assert.assertEquals(jpqlResult.size(), 3);
        //Assert.assertEquals(jpqlResult.get(0).getChildren().size(), 1,
        //        "We did not get right value: " + jpqlResult.get(0).getChildren().size());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMemberOfInSemiJoin() {
        // Try to get children from a Collection on the parent
        List<CascadeColParentTestEntity> jpqlResult =
            em.createQuery("select o from " + CascadeColParentTestEntity.class.getSimpleName() + " o"
                + " where 'entity1' member of o.children").setHint(QueryHints.MEMBER_OF_FIELD, "name").getResultList();
        Assert.assertEquals(jpqlResult.size(), 2);
        
        // Test default name
        jpqlResult =
            em.createQuery("select o from " + CascadeColParentTestEntity.class.getSimpleName() + " o "
                            + "where 'entity1' member of o.children").getResultList();
        Assert.assertEquals(jpqlResult.size(), 2);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMemberOfInAntiJoin() {
        // Try to get children from a Collection on the parent
        List<CascadeColParentTestEntity> jpqlResult =
            em.createQuery("select o from " + CascadeColParentTestEntity.class.getSimpleName() + " o "
                    + "where 'entity1' not member of o.children").setHint(QueryHints.MEMBER_OF_FIELD, "name").getResultList();
        Assert.assertEquals(jpqlResult.size(), 3);
        
        // Test default name
        jpqlResult =
            em.createQuery("select o from " + CascadeColParentTestEntity.class.getSimpleName() + " o "
                    + "where 'entity1' not member of o.children").getResultList();
        Assert.assertEquals(jpqlResult.size(), 3);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testIsEmpty() {
        // Try to get children from a Collection on the parent
        List<CascadeColParentTestEntity> jpqlResult =
            em.createQuery("select o from " + CascadeColParentTestEntity.class.getSimpleName() + " o "
                + "where o.children is empty").getResultList();
        Assert.assertEquals(jpqlResult.size(), 2);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testIsNotEmpty() {
        // Try to get children from a Collection on the parent
        List<CascadeColParentTestEntity> jpqlResult =
            em.createQuery("select o from " + CascadeColParentTestEntity.class.getSimpleName() + " o "
                + "where o.children is not empty").getResultList();
        Assert.assertEquals(jpqlResult.size(), 3);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testAtJoinFilter() {
        // Try to get children from a Map on the parent
        List<CascadeMapParentTestEntity> jpqlResult =
            em.createQuery("select o from " + CascadeMapParentTestEntity.class.getSimpleName() + " o "
                + "where o.name in ('Parent1')").getResultList();
        Assert.assertEquals(jpqlResult.size(), 1);
        // We added three children but the children field has a filter so we get only two object back
        Assert.assertEquals(jpqlResult.get(0).getChildren().size(), 2,
                "We did not get right value: " + jpqlResult.get(0).getChildren().size());
        // Make sure a refresh on the object does not change values
        em.refresh(jpqlResult.get(0));
        Assert.assertEquals(jpqlResult.get(0).getChildren().size(), 2,
                "We did not get right value: " + jpqlResult.get(0).getChildren().size());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testAtOrderBy() {
        // Try to get children from a Map on the parent
        List<CascadeMapParentTestEntity> jpqlResult =
            em.createQuery("select o from " + CascadeMapParentTestEntity.class.getSimpleName() + " o "
                + "where o.name in ('Parent1')").getResultList();
        Assert.assertEquals(jpqlResult.size(), 1);
        // We added three children but the children field has a filter so we get only two object back
        Assert.assertEquals(jpqlResult.get(0).getChildren().size(), 2,
                "We did not get right value: " + jpqlResult.get(0).getChildren().size());
        Map<String, CascadeMapChildTestEntity> children =
            (Map<String, CascadeMapChildTestEntity>) jpqlResult.get(0).getChildren();
        Iterator<Map.Entry<String, CascadeMapChildTestEntity>> childIter = children.entrySet().iterator();
        Map.Entry<String, CascadeMapChildTestEntity> child = childIter.next();
        Assert.assertEquals(child.getValue().getIntValue(), 1,
                "We did not get right key: " + child.getKey() + " value: " + child.getValue());
        child = childIter.next();
        Assert.assertEquals(child.getValue().getIntValue(), 5,
                "We did not get right key: " + child.getKey() + " value: " + child.getValue());
        // Make sure a refresh on the object does not change values
        em.refresh(jpqlResult.get(0));
        Assert.assertEquals(jpqlResult.get(0).getChildren().size(), 2,
                "We did not get right value: " + jpqlResult.get(0).getChildren().size());
        children = (Map<String, CascadeMapChildTestEntity>) jpqlResult.get(0).getChildren();
        childIter = children.entrySet().iterator();
        child = childIter.next();
        Assert.assertEquals(child.getValue().getIntValue(), 1,
                "We did not get right key: " + child.getKey() + " value: " + child.getValue());
        child = childIter.next();
        Assert.assertEquals(child.getValue().getIntValue(), 5,
                "We did not get right key: " + child.getKey() + " value: " + child.getValue());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNegativeSubQueries() {
        // Try non-existent user
        List<ParentTestEntity> result =
            em.createNativeQuery("select id, name from " + getTableName(em, ParentTestEntity.class)
                    + " where id in (select " + getFieldName(em, TestEntity.class, "parent")
                                    + " from " + getTableName(em, TestEntity.class)
                                    + " where name in ('entity1', 'entity2'))"
                    + " AND OwnerId in (select id from User where username like '%xxx')",
            ParentTestEntity.class).getResultList();
        Assert.assertEquals(result.size(), 0);
        // Try non-existent entity
        result =
            em.createNativeQuery("select id, name from " + getTableName(em, ParentTestEntity.class)
                    + " where id in (select " + getFieldName(em, TestEntity.class, "parent")
                                    + " from " + getTableName(em, TestEntity.class)
                                    + " where name in ('entity3', 'entity4'))"
                    + " AND OwnerId in (select id from User where username like '%')",
            ParentTestEntity.class).getResultList();
        Assert.assertEquals(result.size(), 0);
        // Try non-existent entity and user
        result =
            em.createNativeQuery("select id, name from " + getTableName(em, ParentTestEntity.class)
                    + " where id in (select " + getFieldName(em, TestEntity.class, "parent")
                                    + " from " + getTableName(em, TestEntity.class)
                                    + " where name in ('entity3', 'entity4'))"
                    + " AND OwnerId in (select id from User where username like '%xxx')",
            ParentTestEntity.class).getResultList();
        Assert.assertEquals(result.size(), 0);
                
        // Try non-existent user
        List<ParentTestEntity> jpqlResult =
            em.createQuery("select o from " + ParentTestEntity.class.getSimpleName() + " o INNER JOIN o.testEntities t "
                + " INNER JOIN o.ownerId n where t.name in ('entity1', 'entity2') AND n.username like '%xxx'",
             ParentTestEntity.class).getResultList();
        Assert.assertEquals(jpqlResult.size(), 0);
        
        // Try non-existent entity
       jpqlResult = em.createQuery("select o from " + ParentTestEntity.class.getSimpleName() + " o INNER JOIN o.testEntities t "
                + " INNER JOIN o.ownerId n where t.name in ('entity3', 'entity4') AND n.username like '%'",
                ParentTestEntity.class).getResultList();
        Assert.assertEquals(jpqlResult.size(), 0);
        
        // Try non-existent user and entity
        jpqlResult = em.createQuery("select o from " + ParentTestEntity.class.getSimpleName() + " o INNER JOIN o.testEntities t "
                + " INNER JOIN o.ownerId n where t.name in ('entity3', 'entity4') AND n.username like '%xxx'",
                ParentTestEntity.class).getResultList();
           Assert.assertEquals(jpqlResult.size(), 0);
    }
    
    private static final Pattern TEST_QUERY_TRACE_LOGGING_PAT =
        Pattern.compile("Table: CascadeColChildTestEntity query: select [a-zA-Z0-9_$]*parent__c "
                            + "from [a-zA-Z0-9_$]*CascadeColChildTestEntity__c");
    
    @Test
    public void testQueryTraceLogging() throws Exception {
        Logger logger = Logger.getLogger("com.force.sdk.jpa.query");
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.TRACE);
        final AtomicBoolean receivedLog = new AtomicBoolean(false);
        Appender appender = new AppenderSkeleton() {
            @Override
            public boolean requiresLayout() {
                return false;
            }
            
            @Override
            public void close() {
            }
            
            @Override
            protected void append(LoggingEvent event) {
                if (event != null && TEST_QUERY_TRACE_LOGGING_PAT.matcher(event.getRenderedMessage()).find()) {
                    receivedLog.set(true);
                }
            }
        };
        logger.addAppender(appender);
        try {
            em.createQuery("select o from " + CascadeColParentTestEntity.class.getSimpleName() + " o"
                           + " where 'entity1' member of o.children").setHint(QueryHints.MEMBER_OF_FIELD, "name").getResultList();
        } finally {
            logger.setLevel(oldLevel);
            logger.removeAppender(appender);
        }
        
        Assert.assertTrue(receivedLog.get(), "Did not receive log messages");
    }
}
