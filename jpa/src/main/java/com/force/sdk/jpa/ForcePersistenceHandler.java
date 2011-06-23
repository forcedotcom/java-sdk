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

import static com.force.sdk.jpa.ForceEntityManager.LOGGER;

import java.util.*;

import org.datanucleus.ObjectManager;
import org.datanucleus.exceptions.*;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.ObjectProviderImpl;
import org.datanucleus.store.*;

import com.force.sdk.jpa.exception.ForceApiExceptionMap;
import com.force.sdk.jpa.table.TableImpl;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.CalendarCodec;
import com.sforce.ws.bind.XmlObject;

/**
 * 
 * Persistence handler that handles all CRUD operations and translates them into
 * API calls.
 *
 * @author Fiaz Hossain
 */
public class ForcePersistenceHandler extends AbstractPersistenceHandler {

    protected final ForceStoreManager storeManager;

    /**
     * Creates the persistence handler that will be used for all API operations.
     * 
     * @param storeManager the store manager
     */
    public ForcePersistenceHandler(StoreManager storeManager) {
        this.storeManager = (ForceStoreManager) storeManager;
    }

    @Override
    public void close() {
        // Nothing to close here
    }

    /**
     * Deletes a persistent object from the datastore.
     *
     * @param op The ObjectProvider of the object to be deleted.
     */
    @Override
    public void deleteObject(ObjectProvider op) {
        // Check if read-only so update not permitted
        storeManager.assertReadOnlyForUpdateOfObject(op);

        ForceManagedConnection mconn = (ForceManagedConnection) storeManager.getConnection(op.getExecutionContext());
        ObjectManager om = ((ObjectProviderImpl) op).getStateManager().getObjectManager();
        boolean isAllOrNothingMode = om instanceof ForceObjectManagerImpl && ((ForceObjectManagerImpl) om).isInAllOrNothingMode();
        try {
            Object pkValue = op.provideField(op.getClassMetaData().getPKMemberPositions()[0]);
            if (!isAllOrNothingMode) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Deleting object: " + pkValue);
                }
                DeleteResult[] results = ((PartnerConnection) mconn.getConnection()).delete(new String[]{(String) pkValue});
                checkForErrors(results);
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Queuing for A-O-N delete object: " + pkValue);
                }
                ((ForceObjectManagerImpl) om).addToDeleteList((String) pkValue);
            }
        } catch (ApiFault af) {
            throw ForceApiExceptionMap.mapToNucleusException(af, false /* isQuery */,
                    storeManager.isEnableOptimisticTransactions());
        } catch (NucleusOptimisticException noe) {
            throw noe;
        } catch (ConnectionException x) {
            throw new NucleusDataStoreException(x.getMessage(), x);
        } finally {
            mconn.release();
        }
    }

    /**
     * Fetches a persistent object from the database.
     *
     * @param op           The ObjectProvider of the object to be fetched.
     * @param fieldNumbers The numbers of the fields to be fetched.
     */
    @Override
    public void fetchObject(ObjectProvider op, int[] fieldNumbers) {
        ForceManagedConnection mconn = (ForceManagedConnection) storeManager.getConnection(op.getExecutionContext());
        try {
            int pkPosition = op.getClassMetaData().getPKMemberPositions()[0];
            /**
             * Check if we are being asked for PK only. If so, return the ID that has been passed in
             */
            ForceFetchFieldManager fm;
            if (fieldNumbers.length == 1 && fieldNumbers[0] == pkPosition) {
                XmlObject sObject = new XmlObject();
                sObject.addField("Id", op.provideField(pkPosition));
                fm = new ForceFetchFieldManager(op, storeManager, mconn, sObject, null);
            } else {
                fm = new ForceFetchFieldManager(op, storeManager, mconn,
                    op.provideField(pkPosition), fieldNumbers, null);
            }
            op.replaceFields(fieldNumbers, fm);
        } catch (ApiFault af) {
            throw ForceApiExceptionMap.mapToNucleusException(af, false /* isQuery */,
                    storeManager.isEnableOptimisticTransactions());
        } catch (NucleusObjectNotFoundException onf) {
            throw onf;
        } catch (Exception x) {
            throw new NucleusDataStoreException(x.getMessage(), x);
        } finally {
            mconn.release();
        }
    }

    @Override
    public Object findObject(ExecutionContext ectx, Object id) {
        // We are not an ODBMS so we do not provide any objects here
        return null;
    }

    /**
     * Inserts a persistent object into the database.
     *
     * @param op The ObjectProvider of the object to be inserted.
     * @throws NucleusDataStoreException when an error occurs in the datastore communication
     */
    @Override
    public void insertObject(ObjectProvider op) {
        upsert(op, null);
    }

    /**
     * Locates this object in the datastore.
     *
     * @param op The ObjectProvider for the object to be found
     */
    @Override
    public void locateObject(ObjectProvider op) {
        ForceManagedConnection mconn = (ForceManagedConnection) storeManager.getConnection(op.getExecutionContext());
        try {
            AbstractClassMetaData acmd = op.getClassMetaData();
            TableImpl table = storeManager.getTable(acmd);
            QueryResult qr = ((PartnerConnection) mconn.getConnection())
                .query("select count() from " + table.getTableName().getForceApiName() + " where id='"
                        + op.provideField(op.getClassMetaData().getPKMemberPositions()[0]) + "'");
            if (qr.getSize() == 0) {
                throw new NucleusObjectNotFoundException();
            }
        } catch (ConnectionException x) {
            throw new NucleusDataStoreException(x.getMessage(), x);
        } finally {
            mconn.release();
        }
    }

    /**
     * Updates a persistent object in the datastore.
     *
     * @param op           The ObjectProvider of the object to be updated.
     * @param fieldNumbers The numbers of the fields to be updated.
     */
    @Override
    public void updateObject(ObjectProvider op, int[] fieldNumbers) {
        upsert(op, fieldNumbers);
    }

    /**
     * Creates objects for AllOrNothing operations.
     *
     * @param objects the objects to be created
     * @param objectProviders the object providers for each object
     * @param ec the execution context for this transaction
     */
    public void createObjects(Collection<SObject> objects, Collection<ObjectProvider> objectProviders, ExecutionContext ec) {
        ForceManagedConnection mconn = (ForceManagedConnection) storeManager.getConnection(ec);
        try {
            SObject[] toSave = objects.toArray(new SObject[objects.size()]);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Creating objects: " + toString(toSave, false, null));
            }
            PartnerConnection connection = (PartnerConnection) mconn.getConnection();
            connection.setAllOrNoneHeader(true);
            try {
                //connection.getConfig().setTraceMessage(true);
                SaveResult[] results = connection.create(toSave);
                checkForErrors(results);
                int i = 0;
                for (ObjectProvider op : objectProviders) {
                    op.setPostStoreNewObjectId(results[i++].getId());
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Created objects: " + toString(results, false, null));
                }
            } finally {
                connection.setAllOrNoneHeader(false);
            }
        } catch (NucleusOptimisticException noe) {
            throw noe;
        } catch (NucleusUserException nue) {
            throw nue;
        } catch (Exception x) {
            throw new NucleusDataStoreException(x.getMessage(), x);
        } finally {
            mconn.release();
        }
    }

    /**
     * Updates objects for AllOrNothing operations.
     *
     * @param objects  the objects to be updated
     * @param versions the versions corresponding with each object for if-modified-before checks for optimistic transactions
     * @param ec the execution context of this transaction
     */
    public void updateObjects(SObject[] objects, Calendar[] versions, ExecutionContext ec) {
        ForceManagedConnection mconn = (ForceManagedConnection) storeManager.getConnection(ec);
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Updating objects: " + toString(objects, true, null));
            }
            PartnerConnection connection = getPartnerConnection(mconn, versions);
            connection.setAllOrNoneHeader(true);
            try {
                SaveResult[] results = connection.update(objects);
                checkForErrors(results);
            } finally {
                try {
                    connection.setAllOrNoneHeader(false);
                } finally {
                    connection.clearConditionalRequestHeader();
                }
            }
        } catch (ApiFault af) {
            throw ForceApiExceptionMap.mapToNucleusException(af, false /* isQuery */,
                    storeManager.isEnableOptimisticTransactions());
        } catch (NucleusOptimisticException noe) {
            throw noe;
        } catch (NucleusUserException nue) {
            throw nue;
        } catch (Exception x) {
            throw new NucleusDataStoreException(x.getMessage(), x);
        } finally {
            mconn.release();
        }
    }

    /**
     * Deletes objects for AllOrNothing operations.
     *
     * @param objects the objects to be deleted
     * @param ec the execution context for this transaction
     */
    public void deleteObjects(String[] objects, ExecutionContext ec) {
        ForceManagedConnection mconn = (ForceManagedConnection) storeManager.getConnection(ec);
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleting objects: " + Arrays.toString(objects));
            }
            PartnerConnection connection = (PartnerConnection) mconn.getConnection();
            connection.setAllOrNoneHeader(true);
            try {
                DeleteResult[] results = connection.delete(objects);
                checkForErrors(results);
            } finally {
                connection.setAllOrNoneHeader(false);
            }
        } catch (ApiFault af) {
            throw ForceApiExceptionMap.mapToNucleusException(af, false /* isQuery */,
                    storeManager.isEnableOptimisticTransactions());
        } catch (NucleusOptimisticException noe) {
            throw noe;
        } catch (NucleusUserException nue) {
            throw nue;
        } catch (Exception x) {
            throw new NucleusDataStoreException(x.getMessage(), x);
        } finally {
            mconn.release();
        }
    }

    private void upsert(ObjectProvider op, int[] fieldNumbers) {
        if (op.getClassMetaData().isEmbeddedOnly()) {
            // Embedded entities will be saved by the parent
            return;
        }
        // Check if read-only so update not permitted
        storeManager.assertReadOnlyForUpdateOfObject(op);

        if (!storeManager.managesClass(op.getClassMetaData().getFullClassName())) {
            storeManager.addClass(op.getClassMetaData().getFullClassName(), op.getExecutionContext().getClassLoaderResolver());
        }

        ForceManagedConnection mconn = (ForceManagedConnection) storeManager.getConnection(op.getExecutionContext());
        try {
            ForceInsertFieldManager fm = new ForceInsertFieldManager(op, storeManager,
                    fieldNumbers != null ? op.provideField(op.getClassMetaData().getPKMemberPositions()[0]) : null);
            op.provideFields(fieldNumbers != null ? fieldNumbers : op.getClassMetaData().getAllMemberPositions(), fm);
            /**
             * It is possible that this field is just a parent whose children have been updated only. In that case
             * the current object will not be dirty and we have nothing else to do.
             */
            if (!fm.isDirty()) return;
            ObjectManager om = ((ObjectProviderImpl) op).getStateManager().getObjectManager();
            boolean isAllOrNothingMode =
                om instanceof ForceObjectManagerImpl && ((ForceObjectManagerImpl) om).isInAllOrNothingMode();
            SObject toSave;
            if (!isAllOrNothingMode) {
                PartnerConnection connection = getPartnerConnection(mconn, op);
                try {
                    toSave = fm.getSObject(false);
                    if (LOGGER.isDebugEnabled()) {
                        if (fieldNumbers != null) {
                            LOGGER.debug("Updating object: " + toSave.getType() + " id: " + toSave.getId());
                        } else {
                            LOGGER.debug("Creating object: " + toSave.getType());
                        }
                    }
                    SaveResult[] results = fieldNumbers != null ? connection.update(new SObject[]{toSave})
                                                : ((PartnerConnection) mconn.getConnection()).create(new SObject[]{toSave});
                    checkForErrors(results);
                    if (fieldNumbers == null) {
                        op.setPostStoreNewObjectId(results[0].getId());
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Created object id: " + results[0].getId());
                        }
                    }
                } finally {
                    connection.clearConditionalRequestHeader();
                }
            } else {
                if (fieldNumbers != null) {
                    //When we do all-or-nothing with optimistic transactions, per jpa spec we need to save properly
                    // even if some objects are missing @Version,
                    // so if op.getVersion is null, we give a Calendar set to System.currentTimeMilis + 1 HOUR so
                    // the if-modified-before check for the object without @Version will always succeed
                    toSave = fm.getSObject(false);
                    ((ForceObjectManagerImpl) om).addToUpdateList(toSave,
                            op.getVersion() != null ? (Calendar) op.getVersion() : getVersionForUnversioned());
                } else {
                    toSave = fm.getSObject(true);
                    ((ForceObjectManagerImpl) om).addToCreateList(toSave, op);
                }
                if (LOGGER.isDebugEnabled()) {
                    if (fieldNumbers != null) {
                        LOGGER.debug("Queuing for A-O-N update object: " + toSave.getType() + " id: " + toSave.getId());
                    } else {
                        LOGGER.debug("Queuing for A-O-N create object: " + toSave.getType());
                    }
                }
            }
        } catch (ApiFault af) {
            throw ForceApiExceptionMap.mapToNucleusException(af, false /* isQuery */,
                storeManager.isEnableOptimisticTransactions());
        } catch (NucleusException ne) {
            throw ne;
        } catch (Exception x) {
            throw new NucleusDataStoreException(x.getMessage(), x);
        } finally {
            mconn.release();
        }
    }

    private static Calendar getVersionForUnversioned() {
        long time = System.currentTimeMillis() + (60 * 60 * 1000);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal;
    }

    private PartnerConnection getPartnerConnection(ForceManagedConnection mconn, ObjectProvider op) {
        PartnerConnection connection = (PartnerConnection) mconn.getConnection();
        if (op.getVersion() != null && storeManager.isEnableOptimisticTransactions()) {
            ConditionalRequestHeader_element ch = new ConditionalRequestHeader_element();
            ch.setIfModifiedBefore((Calendar) op.getVersion());
            connection.__setConditionalRequestHeader(ch);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Conditional header set to: " + new CalendarCodec().getValueAsString(ch.getIfModifiedBefore()));
            }
        }
        return connection;
    }

    private PartnerConnection getPartnerConnection(ForceManagedConnection mconn, Calendar[] versions) {
        PartnerConnection connection = (PartnerConnection) mconn.getConnection();
        if (versions.length > 0 && storeManager.isEnableOptimisticTransactions()) {
            ConditionalRequestHeader_element ch = new ConditionalRequestHeader_element();
            ch.setIfModifiedBeforeArray(versions);
            connection.__setConditionalRequestHeader(ch);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Conditional header set to: " + toString(ch.getIfModifiedBeforeArray(), false, new CalendarCodec()));
            }
        }
        return connection;
    }

    private void checkForErrors(SaveResult[] results) {
        List<Error> failures = null;
        boolean optimisticFailure = false;
        for (SaveResult sr : results) {
            if (!sr.getSuccess()) {
                if (failures == null) {
                    failures = new ArrayList<Error>();
                }
                optimisticFailure = handleError(failures, sr.getErrors()[0]);
            }
        }
        handleFailures(failures, optimisticFailure);
    }

    private static boolean handleError(List<Error> failures, Error error) {
        failures.add(error);
        return error.getStatusCode() == StatusCode.ENTITY_FAILED_IFLASTMODIFIED_ON_UPDATE;
    }

    private static void handleFailures(List<Error> failures, boolean optimisticFailure) {
        if (failures != null) {
            Iterator<Error> iter = failures.iterator();
            while (iter.hasNext()) {
                Error error = iter.next();
                if (error.getStatusCode() == StatusCode.ALL_OR_NONE_OPERATION_ROLLED_BACK && failures.size() > 1) {
                    iter.remove();
                }
            }
            if (optimisticFailure) {
                throw new NucleusOptimisticException(failures.get(0).getMessage(),
                        failures.size() == 1 ? failures.get(0) : failures.toArray(new Error[failures.size()]));
            } else {
                throw new NucleusUserException(failures.get(0).getMessage(),
                        failures.size() == 1 ? failures.get(0) : failures.toArray(new Error[failures.size()]));
            }
        }
    }

    /**
     * Checks for errors in a {@code delete()} call and handles the results properly.
     * 
     * @param results the results from the {@code delete()} API call
     */
    public static void checkForErrors(DeleteResult[] results) {
        List<Error> failures = null;
        boolean optimisticFailure = false;
        for (DeleteResult dr : results) {
            if (!dr.getSuccess()) {
                if (failures == null) {
                    failures = new ArrayList<Error>();
                }
                optimisticFailure = handleError(failures, dr.getErrors()[0]);
            }
        }
        handleFailures(failures, optimisticFailure);
    }

    /**
     * Checks for errors in an {@code emptyRecycleBin()} call.
     * 
     * @param results the results from the {@code emptyRecycleBin()} API call
     */
    public static void checkForRecycleBinErrors(EmptyRecycleBinResult[] results) {
        List<Error> failures = null;
        boolean optimisticFailure = false;
        for (EmptyRecycleBinResult dr : results) {
            if (!dr.getSuccess()) {
                if (failures == null) {
                    failures = new ArrayList<Error>();
                }
                optimisticFailure = handleError(failures, dr.getErrors()[0]);
            }
        }
        handleFailures(failures, optimisticFailure);
    }
        
    private static String toString(Object[] objects, boolean isUpdate, CalendarCodec cCodec) {
        StringBuilder sb = new StringBuilder(objects.length * 40);
        sb.append("[");
        for (Object obj : objects) {
            if (sb.length() > 1) sb.append(", ");
            if (obj instanceof SObject) {
                SObject s = (SObject) obj;
                sb.append("entity: ").append(s.getType());
                if (isUpdate) {
                    sb.append(" id: ").append(s.getId());
                }
            } else if (obj instanceof SaveResult) {
                sb.append(((SaveResult) obj).getId());
            } else if (obj instanceof Calendar) {
                sb.append(cCodec.getValueAsString(obj));
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
