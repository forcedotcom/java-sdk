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

import java.io.*;
import java.util.Random;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import org.testng.Assert;
import org.testng.annotations.*;

import com.force.sdk.jpa.entities.SerializableEntity;
import com.force.sdk.test.util.BaseJPAFTest;

/**
 * Spec mentions serialized entities may become detached:
 * "A detached entity results from ... serializing an entity
 * or otherwise passing an entity by value - e.g. to a
 * separate application tier, through a remote interface, etc."
 * Including basic tests for serialized and deserialized entities
 * to ensure a serialized entity can be persisted and a
 * deserialized entity must be merged.
 * 
 * @author Naaman Newbold
 */
public class SerializedAttachTest extends BaseJPAFTest {
    ByteArrayOutputStream byteArrayOutputStream = null;
    ByteArrayInputStream byteArrayInputStream = null;
    ObjectOutputStream out = null;
    ObjectInputStream in = null;

    @BeforeMethod
    public void initStreams() throws Exception {
        byteArrayOutputStream = new ByteArrayOutputStream();
        out = new ObjectOutputStream(byteArrayOutputStream);
    }

    @AfterMethod(alwaysRun = true)
    public void closeStreams() throws Exception {
        if (byteArrayOutputStream != null) byteArrayOutputStream.close();
        if (out != null) out.close();
        if (byteArrayInputStream != null) byteArrayInputStream.close();
        if (in != null) in.close();
    }

    @Test
    public void testDeserializedEntityNotImplicitlyManaged() throws Exception {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        SerializableEntity serializableEntity = new SerializableEntity();
        serializableEntity.setName("NegativeDetachPersistTest.testDeserializedEntityIsDetached");

        em.persist(serializableEntity);
        Assert.assertTrue(em.contains(serializableEntity),
                "serializableEntity was just persisted by the EntityManager, but it is not managed.");

        out.writeObject(serializableEntity);

        Assert.assertTrue(em.contains(serializableEntity),
                "serializableEntity was persisted by the EntityManager and should still be managed.");

        byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        in = new ObjectInputStream(byteArrayInputStream);
        SerializableEntity deserializedSerializableEntity = (SerializableEntity) in.readObject();

        Assert.assertTrue(em.contains(serializableEntity),
                "serializableEntity was just persisted by the EntityManager, but it is not managed.");
        Assert.assertFalse(em.contains(deserializedSerializableEntity), "deserializedSerializableEntity should not be managed.");

        transaction.commit();

        Assert.assertFalse(em.contains(serializableEntity),
                "serializableEntity was committed by the EntityManager, but is still managed.");
        Assert.assertFalse(em.contains(deserializedSerializableEntity), "deserializedSerializableEntity should not be managed.");
    }

    @Test
    public void testMergeDeserializedEntityForPersistedSerializedEntityInSingleTransaction() throws Exception {
        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();

            SerializableEntity serializableEntity = new SerializableEntity();
            serializableEntity.setName("Test" + String.valueOf(new Random().nextInt(10000)));

            em.persist(serializableEntity);
            Assert.assertTrue(em.contains(serializableEntity),
                    "serializableEntity was just persisted by the EntityManager, but it is not managed.");

            out.writeObject(serializableEntity);

            Assert.assertTrue(em.contains(serializableEntity),
                    "serializableEntity was persisted by the EntityManager and should still be managed.");

            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            in = new ObjectInputStream(byteArrayInputStream);
            SerializableEntity deserializedSerializableEntity = (SerializableEntity) in.readObject();

            em.merge(deserializedSerializableEntity);

            Assert.fail("A deserialized entity cannot be merged for a managed entity with the same identity.");
        } catch (PersistenceException pe) {
            Assert.assertTrue(pe.getMessage()
                    .contains("another persistent object with this identity already exists enlisted in this transaction"),
                    "unexpected error message: " + pe.getMessage());
        }
    }

    @Test
    public void testMergeDeserializedEntityForPersistedSerializedEntityInTwoTransactions() throws Exception {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        SerializableEntity serializableEntity = new SerializableEntity();
        serializableEntity.setName("Test" + String.valueOf(new Random().nextInt(10000)));

        em.persist(serializableEntity);
        Assert.assertTrue(em.contains(serializableEntity),
                "serializableEntity was just persisted by the EntityManager, but it is not managed.");

        transaction.commit();

        Assert.assertFalse(em.contains(serializableEntity), "serializableEntity was committed, but is still managed.");

        transaction = em.getTransaction();
        transaction.begin();

        out.writeObject(serializableEntity);

        byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        in = new ObjectInputStream(byteArrayInputStream);
        SerializableEntity deserializedSerializableEntity = (SerializableEntity) in.readObject();

        em.merge(deserializedSerializableEntity);

        transaction.commit();
    }

}
