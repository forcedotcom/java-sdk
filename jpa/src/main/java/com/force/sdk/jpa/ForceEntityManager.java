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

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.spi.PersistenceCapable;
import javax.persistence.*;

import org.datanucleus.PersistenceConfiguration;
import org.datanucleus.StateManager;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.jpa.*;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.QueryResultMetaData;
import org.datanucleus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.force.sdk.jpa.query.ForceQueryUtils;
import com.force.sdk.jpa.query.QueryHints;

/**
 * 
 * The main EntityManager for the Force.com JPA implementation.  Persist,
 * find, etc. calls go through here. This class is where we configure native queries
 * to use SOQL.
 *
 * @author Fiaz Hossain
 */
public class ForceEntityManager extends EntityManagerImpl {
    
    private static final String SOQL_LANGUAGE = "SOQL";
    private static final Calendar EPOCH_TIME = new GregorianCalendar(1970, 0, 1, 0, 0, 0);
    
    /**
     * This is the logger for logging all JPA request.
     * The convention here is that all calls to JPA interface starts with JPA <operation_name> 
     * 
     * e.g. JPA Persist ...
     * 
     * for a persist call. This is to distinguish other JPA related calls that are logged by the datasource implementation.
     * 
     */
    static final Logger LOGGER = LoggerFactory.getLogger("com.force.sdk.jpa");

    /**
     * Construct an entity manager for persisting objects to Force.com.
     * 
     * @param emf the entity manager factory creating this entity manager
     * @param pmf the persistence manager factory that will create the persistence manager
     * @param contextType the persistence context type for this application
     */
    public ForceEntityManager(EntityManagerFactory emf, PersistenceManagerFactory pmf, PersistenceContextType contextType) {
        super(emf, pmf, contextType);
        if (tx != null) {
            tx = new ForceEntityTransactionImpl(om);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("EntityManager created: " + this);
        }
    }
    
    /**
     * Overrides the base class method to implement our own ForceTransactionImpl.
     */
    @Override
    public void joinTransaction() {
        assertIsOpen();
        //assertIsActive();
        //TODO assertNotActive
        //assertTransactionNotRequired();
        tx = new ForceEntityTransactionImpl(om);
    }
    
    @Override
    public void close() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("EntityManager close: " + this);
        }
        super.close();
    }

    @Override
    public void clear() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("EntityManager clear: " + this);
        }
        super.clear();
    }

    /**
     * Method added to allow a StateManager to be injected into a transient object
     * so that we can track which fields have been touched. Then if the user calls
     * <code>merge()</code> on that transient object we can copy the fields over to a detached
     * object and call <code>merge()</code> on it. If the user called persist instead, we simply
     * discard this injected StateManager and proceed as usual.
     * 
     * @param pc PersistenceCapable
     */
    public void injectStateManagerIntoTransient(PersistenceCapable pc) {
        AbstractClassMetaData acmd = om.getMetaDataManager().getMetaDataForClass(pc.getClass(), om.getClassLoaderResolver());
        ForceJPAStateManagerImpl sm = new ForceJPAStateManagerImpl(om, acmd);
        sm.initialiseForHollowPreConstructed(null, pc);
        om.putObjectIntoCache(sm);
        if (acmd.hasVersionStrategy()) {
            // This is not the right value but we need something to pacify DataNucleus.
            // We require that the user set a valid version before calling merge
            sm.setVersion(EPOCH_TIME);
        }
    }
    
    /**
     * Create an instance of Query for executing an SOQL query.
     * @param soqlString a native SOQL query string
     * @param resultClass the class of the resulting instance(s)
     * @return the new query instance
     */
    @Override
    public Query createNativeQuery(String soqlString, Class resultClass) {
        assertIsOpen();
        try {
            org.datanucleus.store.query.Query internalQuery = om.getOMFContext().getQueryManager().newQuery(
                    SOQL_LANGUAGE, om.getExecutionContext(), soqlString);
            if (resultClass != null) {
                internalQuery.setResultClass(resultClass);
            }
            return new JPAQuery(this, internalQuery, SOQL_LANGUAGE);
        } catch (NucleusException ne) {
            throw new IllegalArgumentException(ne.getMessage(), ne);
        }
    }
    
    /**
     * Create an instance of Query for executing an SOQL query.
     * @param soqlString a native SOQL query string
     * @param resultSetMapping the name of the result set mapping
     * @return the new query instance
     */
    @Override
    public Query createNativeQuery(String soqlString, String resultSetMapping) {
        assertIsOpen();
        try {
            org.datanucleus.store.query.Query internalQuery = om.getOMFContext().getQueryManager().newQuery(
                    SOQL_LANGUAGE, om.getExecutionContext(), soqlString);
            QueryResultMetaData qrmd = om.getMetaDataManager().getMetaDataForQueryResult(resultSetMapping);
            if (qrmd == null) {
                // TODO Localise this, and check if it is the correct exception to throw
                throw new IllegalArgumentException("ResultSetMapping " + resultSetMapping + " is not found");
            }
            internalQuery.setResultMetaData(qrmd);
            return new JPAQuery(this, internalQuery, SOQL_LANGUAGE);
        } catch (NucleusException ne) {
            throw new IllegalArgumentException(ne.getMessage(), ne);
        }
    }
    
    @Override
    public void persist(Object entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("JPA Persist - entity: " + entity);
        }
        if (entity instanceof PersistenceCapable) {
            PersistenceCapable pc = (PersistenceCapable) entity;
            if (pc.jdoIsDetached()) {
                /**
                 * Sec - 3.2.2
                 * If X is a detached object, the EntityExistsException may be thrown when the persist
                 * operation is invoked, or the EntityExistsException or another PersistenceException may be thrown
                 * at flush or commit time
                 */
                throwException(new EntityExistsException("Entity already exists. Use merge to save changes."));
            }
            // We may have added a state manager which is not needed for persist calls
            StateManager sm = om.findStateManager(entity);
            if (sm != null) {
                if (sm instanceof ForceJPAStateManagerImpl) {
                    om.clearDirty(sm);
                    om.removeStateManager(sm);
                    pc.jdoReplaceStateManager(null);
                } else {
                    /**
                     * Sec - 3.2.2
                     * If X is a preexisting managed entity, it is ignored by the persist operation. However, the persist
                     * operation is cascaded to entities referenced by X, if the relationships from X to these other
                     * entities are annotated with the cascade=PERSIST or cascade=ALL annotation element
                     * value or specified with the equivalent XML descriptor element.
                     */
                    // Do nothing and just return
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("JPA Persist - entity already persisted doing nothing for entity: " + entity);
                    }
                    return;
                }
            }
        }
        super.persist(entity);
    }
    
    @Override
    public Object merge(Object entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("JPA Merge: - entity: " + entity);
        }
        if (entity instanceof PersistenceCapable) {
            PersistenceCapable pc = (PersistenceCapable) entity;
            if (pc.jdoIsDetached() && pc.jdoGetObjectId() == null) {
                throw new IllegalArgumentException("Detached entity with null id cannot be merged.");
            }
            // Read id from entity
            StateManager sm = om.findStateManager(entity);
            if (sm != null) {
                // This path is taken by all merge() calls.
                // We need to isolate merge() calls that were made to Transient object instead of Detached objects
                AbstractClassMetaData acmd = om.getMetaDataManager()
                                                    .getMetaDataForClass(entity.getClass(), om.getClassLoaderResolver());
                Object id = ForceQueryUtils.getIdFromObject(pc, acmd);
                if (id != null) {
                    if (sm instanceof ForceJPAStateManagerImpl) {
                            sm.initialiseForDetached(pc, id, sm.getVersion(pc));
                            entity = sm.getObject();
                    }
                }
            }
        }
        return super.merge(entity);
    }
    
    @Override
    public void remove(Object entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("JPA Remove - entity: " + entity);
        }
        
        try {
            super.remove(entity);
        } catch (IllegalArgumentException e) {
            // Add more descriptive message when this exception occurs because of
            // a detached entity
            if (e.getMessage().contains(LOCALISER.msg("EM.EntityIsDetached",
                    StringUtils.toJVMIDString(entity), "" + om.getApiAdapter().getIdForObject(entity)))) {
                throw new IllegalArgumentException(e.getMessage()
                        + " - It has most likely become detached since the find() operation."
                        + " Either put find() and remove() under same transaction or merge() the object first");
            } else {
                throw e;
            }
        }
    }
    
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lock, Map<String, Object> properties) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("JPA Find - entity: " + entityClass.getName() + " id: " + primaryKey + " lock: " + lock);
        }
        /**
         * TODO - Current DN implementation ignores the properties so we try to to do the best we can now. Once
         * there is full support for properties QueryHints can move elsewhere.
         */
        Object fetchDepth = properties != null ? properties.get(QueryHints.MAX_FETCH_DEPTH) : null;
        if (fetchDepth != null && !(fetchDepth instanceof Integer))
            throw new IllegalArgumentException(QueryHints.MAX_FETCH_DEPTH + " requires Integer value");
        Integer oldFetchDepth = null;
        if (fetchDepth != null) {
            oldFetchDepth = om.getFetchPlan().getMaxFetchDepth();
            om.getFetchPlan().setMaxFetchDepth((Integer) fetchDepth);
        }
        try {
            return super.find(entityClass, primaryKey, lock, properties);
        } catch (NucleusException ne) {
            throw NucleusJPAHelper.getJPAExceptionForNucleusException(ne);
        } finally {
            if (oldFetchDepth != null) {
                om.getFetchPlan().setMaxFetchDepth(oldFetchDepth);
            }
        }
    }
    
    @Override
    public Object getReference(Class entityClass, Object primaryKey) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("JPA getReference - entity: " + entityClass.getName() + " id: " + primaryKey);
        }
        return super.getReference(entityClass, primaryKey);
    }
    

    @Override
    public void refresh(Object entity, LockModeType lock, Map<String, Object> properties) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("JPA Refresh - entity: " + entity + " lock: " + lock);
        }
        super.refresh(entity, lock, properties);
    }
    
    /**
     * We ignore <code>flush()</code> if we are running within a transaction.
     * Use commit() instead for transactions.
     */
    @Override
    public void flush() {
        if (getTransaction().isActive()) {
            // We ignore flush if we are running within a transaction
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("JPA Flush ignored");
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("JPA Flush");
            }
            super.flush();
        }
    }
    

    @Override
    public void lock(Object entity, LockModeType lock, Map<String, Object> properties) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("JPA Lock - entity: " + entity + " lock: " + lock);
        }
        super.lock(entity, lock, properties);
    }
    

    @Override
    public void detach(Object entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("JPA Detach - entity: " + entity);
        }
        super.detach(entity);
    }
    
    private void assertIsOpen() {
        if (om.isClosed()) {
            throw new IllegalStateException(LOCALISER.msg("EM.IsClosed"));
        }
    }
    
    /**
     * Convenience method to throw the supplied exception.
     * If the supplied exception is a PersistenceException, also mark the current transaction for rollback.
     * @param re The exception
     */
    private Object throwException(RuntimeException re) {
        if (re instanceof PersistenceException) {
            PersistenceConfiguration conf = om.getOMFContext().getPersistenceConfiguration();
            boolean markForRollback = conf.getBooleanProperty("datanucleus.jpa.txnMarkForRollbackOnException");
            if (markForRollback) {
                // The JPA spec says that all PersistenceExceptions thrown should mark the transaction for 
                // rollback. Seems excessive to me. For example, you try to find an object with a particular id and it 
                // doesn't exist so you then have to rollback the txn and start again.
                getTransaction().setRollbackOnly();
            }
        }
        throw re;
    }
}
