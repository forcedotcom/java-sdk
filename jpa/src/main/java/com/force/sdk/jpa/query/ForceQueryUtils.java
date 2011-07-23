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

package com.force.sdk.jpa.query;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

import javax.jdo.identity.StringIdentity;
import javax.jdo.spi.PersistenceCapable;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.FetchPlan;
import org.datanucleus.exceptions.*;
import org.datanucleus.metadata.*;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.query.evaluator.JDOQLResultClassMapper;
import org.datanucleus.query.expression.*;
import org.datanucleus.query.expression.Expression.MonadicOperator;
import org.datanucleus.query.symbol.Symbol;
import org.datanucleus.store.*;
import org.datanucleus.store.query.AbstractJavaQuery;
import org.datanucleus.store.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.force.sdk.jpa.*;
import com.force.sdk.jpa.annotation.JoinFilter;
import com.force.sdk.jpa.exception.ForceApiExceptionMap;
import com.force.sdk.jpa.query.formatter.MultiPicklistFormatter;
import com.force.sdk.jpa.table.*;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

/**
 * 
 * This class contains the bulk of the logic for building SOQL queries based on 
 * the Java query object.
 *
 * @author Fiaz Hossain
 */
public class ForceQueryUtils {

    static final Logger LOGGER = LoggerFactory.getLogger("com.force.sdk.jpa.query");
    
    private static final int MAX_EXPRESSION_DEPTH = 100;
    private static final int MAX_DELETE_IDS = 200;
    private static final Set<String> AGGREGATE_METHODS;
    private static final Set<String> UNSUPPORTED_JOINS;
    
    static {
        HashSet<String> am = new HashSet<String>();
        am.add("COUNT");
        am.add("SUM");
        am.add("AVG");
        am.add("MIN");
        am.add("MAX");
        AGGREGATE_METHODS = Collections.unmodifiableSet(am);
        
        // Unsupported Joins
        HashSet<String> uj = new HashSet<String>();
        uj.add("ALL");
        uj.add("ANY");
        uj.add("SOME");
        UNSUPPORTED_JOINS = Collections.unmodifiableSet(uj);
    }

    /**
     * limit type used for determining how the JPA query is executed.
     */
    enum LimitType {
        None,
        Soql,
        Java
    }
    
    private ExecutionContext ec;
    private ForceManagedConnection mconn;
    private AbstractJavaQuery query;
    private Map<Object, Object> parameters;
    private Map<String, QueryListener> listeners;
    private Map<String, Object> hints;
    private int currentHint;
   
    /**
     * Creates the query util for a specific query.
     * 
     * @param ec the ExecutionContext
     * @param mconn the managed connection to connect to Force.com
     * @param query - parsed query
     * @param parameters - query parameters
     * @param listeners - query listeners
     * @param hints - query hints
     */
    public ForceQueryUtils(ExecutionContext ec, ForceManagedConnection mconn,
            AbstractJavaQuery query, Map<Object, Object> parameters,
            Map<String, QueryListener> listeners, Map<String, Object> hints) {
        this.ec = ec;
        this.mconn = mconn;
        this.query = query;
        this.parameters = parameters;
        this.listeners = listeners;
        this.hints = hints;
        this.currentHint = 0;
    }
    
    /**
     * See if there is a limit set and if we can use SOQL to execute the query.
     * 
     * @param query Query
     * @return the limit type of Soql, Java, or None
     */
    public static LimitType getLimitType(Query query) {
        if (query.getRangeFromIncl() > 0 || (query.getRangeToExcl() > 0 && query.getRangeToExcl() < Long.MAX_VALUE)) {
            if (query.getRangeFromIncl() == 0) return LimitType.Soql;
            return LimitType.Java;
        }
        return LimitType.None;
    }
    
    ExecutionContext getExecutionContext() {
        return ec;
    }
    
    ForceManagedConnection getManagedConnection() {
        return mconn;
    }
    
    Object getHints(String hint) {
        return hints != null ? hints.get(hint) : null;
    }
    /**
     * 
     * Convenience method to delete all objects of the candidate type from the 
     * specified XML connection.
     * 
     * @param candidateClass Candidate
     * @return Number of objects deleted
     */
    Long deleteObjectsOfCandidateType(Object candidateClass) {
        try {
            AbstractClassMetaData acmd =
                candidateClass instanceof String ? ec.getMetaDataManager().getMetaDataForEntityName((String) candidateClass)
                : ec.getMetaDataManager().getMetaDataForClass((Class) candidateClass, ec.getClassLoaderResolver());
            if (acmd == null) {
                throw new NucleusUserException("Entity not found: " + candidateClass);
            }
            final ForceStoreManager storeManager = (ForceStoreManager) ec.getStoreManager();

            TableImpl table = storeManager.getTable(acmd);
            PartnerConnection service = (PartnerConnection) mconn.getConnection();

            long totalDeleted = 0;
            boolean done = false;
            while (!done) {
                QueryResult qr =
                    service.query(buildQuery(table, acmd, null, query.getCompilation(), false,
                                                MAX_DELETE_IDS, null, table.getTableName().getForceApiName()));
                if (qr.getSize() == 0) return totalDeleted;
                String[] idsToDelete = new String[qr.getSize()];
                for (int i = 0; i < idsToDelete.length; i++) {
                    idsToDelete[i] = qr.getRecords()[i].getId();
                }
                // Now bulk delete them
                DeleteResult[] deleteResult = service.delete(idsToDelete);
                ForcePersistenceHandler.checkForErrors(deleteResult);
                
                // If all is well check for "EmptyRecycleBin" hint
                if (hints != null) {
                    if (Boolean.TRUE.equals(hints.get(QueryHints.EMPTY_RECYCLE_BIN))) {
                        EmptyRecycleBinResult[] emptyResult = service.emptyRecycleBin(idsToDelete);
                        ForcePersistenceHandler.checkForRecycleBinErrors(emptyResult);
                    }
                }
                totalDeleted += deleteResult.length;
                if (deleteResult.length < MAX_DELETE_IDS) {
                    return totalDeleted;
                }
            }
            return totalDeleted;
        } catch (ApiFault af) {
            throw new NucleusDataStoreException(af.toString(), af);
        } catch (Exception e) {
            throw new NucleusDataStoreException(e.getMessage(), e);
        }
    }

    /**
     * Convenience method to get all objects of the candidate type (and optional subclasses) from the 
     * specified XML connection.
     * 
     * @param ec ObjectManager
     * @param mconn WSC connection
     * @param candidateClass Candidate
     * @param query - parsed query
     * @return List of objects of the candidate type (or subclass)
     */
    List<Object> getObjectsOfCandidateType(Expression[] resultExpr) {
        try {
            ForceStoreManager storeManager = (ForceStoreManager) ec.getStoreManager();

            ClassLoaderResolver clr = ec.getClassLoaderResolver();
            AbstractClassMetaData acmd = null;
            TableImpl table = null;
            /**
             * - If there is a candidate class and result class we are in JPQL and give result class the upper hand
             * - If result class can be mapped to a table use it.
             *       Otherwise result class must be a non-persistence capable class
             * - If there is no candidate class but a result class it can be JPQL or NativeQuery.
             *       Either way, use that class.
             */
            if (query.getCandidateClass() != null) {
                AbstractClassMetaData candidateCmd = ec.getMetaDataManager().getMetaDataForClass(query.getCandidateClass(), clr);
                if (candidateCmd == null) {
                    throw new NucleusUserException("Candidate entity not found: " + query.getCandidateClass());
                }
                TableImpl candidateTable = storeManager.getTable(candidateCmd);
                if (query.getResultClass() != null) {
                   /**
                    * Validate that Result class is compatible with the candidate class. This can happen if two entities are
                    * mapped to the same standard/custom object in Force.com
                    */
                    acmd = ec.getMetaDataManager().getMetaDataForClass(query.getResultClass(), clr);
                    if (acmd != null) {
                        TableImpl resultTable = storeManager.getTable(acmd);
                        if (!resultTable.getTableName().getForceApiName()
                                .equals(candidateTable.getTableName().getForceApiName())) {
                            // The data is incompatible and cannot be saved to this entity
                            throw new NucleusUserException(
                                    String.format("Result class: %s is not compatible with force.com table: %s",
                                                    query.getResultClass().getName(), candidateTable.getTableName().getName()));
                        }
                        table = resultTable;
                    }
                }
                if (acmd == null) {
                   acmd = candidateCmd;
                   table = candidateTable;
                }
            } else if (query.getResultClass() != null) {
                acmd = ec.getMetaDataManager().getMetaDataForClass(query.getResultClass(), clr);
            }
            
            PartnerConnection service = (PartnerConnection) mconn.getConnection();

            QueryResult qr;
            Set<Integer> fieldsToLoad;
            int [] fieldsLoaded;
            if (query.getCompilation() != null && acmd != null) {
                // This is used for JDOQL and JPQL
                fieldsToLoad = getFieldsToLoad(acmd, query.getFetchPlan());
                String soqlQuery =
                    buildQuery(table, acmd, fieldsToLoad, query.getCompilation(), false,
                                query.getRangeToExcl(), query.getFetchPlan(), table.getTableName().getForceApiName());
                if (LOGGER.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder(soqlQuery.length() * 2);
                    sb.append("Executing JPQL: " + query.getSingleStringQuery()).append("\n").append("SOQL: ").append(soqlQuery);
                    LOGGER.debug(sb.toString());
                }
                qr = service.query(soqlQuery);
                fieldsLoaded = new int[fieldsToLoad.size()];
                int i = 0;
                for (int f : fieldsToLoad) {
                    fieldsLoaded[i++] = f;
                }
            } else {
                // This is used for Native query
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Executing Native SOQL: " + query.getSingleStringQuery());
                }
                qr = service.query(query.getSingleStringQuery());
                fieldsLoaded = acmd != null ? acmd.getDFGMemberPositions() : null;
            }
            return getResultFromQueryResult(acmd, fieldsLoaded, qr, resultExpr);
        } catch (ApiFault af) {
            throw ForceApiExceptionMap.mapToNucleusException(af, true /* isQuery */,
                    ((ForceStoreManager) ec.getStoreManager()).isEnableOptimisticTransactions());
        } catch (Exception e) {
            throw new NucleusDataStoreException(e.getMessage(), e);
        }
    }
    
    private Set<Integer> getFieldsToLoad(AbstractClassMetaData acmd, FetchPlan fetchPlan) {
        Set<Integer> memberPositions;
        if (fetchPlan != null && fetchPlan.getGroups().size() > 1) {
            memberPositions = new LinkedHashSet<Integer>(acmd.getMemberCount());
            for (String group : fetchPlan.getGroups()) {
                if ("default".equals(group)) {
                    for (int i : acmd.getDFGMemberPositions()) {
                        memberPositions.add(i);
                    }
                } else {
                    FetchGroupMetaData fgmd = acmd.getFetchGroupMetaData(group);
                    if (fgmd == null) {
                        throw new NucleusDataStoreException("Fetch group metadata not found for group: " + group);
                    }
                    addFetchGroupMemberPositions(acmd, memberPositions, fgmd);
                }
            }
        } else {
            memberPositions = new LinkedHashSet<Integer>(acmd.getDFGMemberPositions().length);
            for (int pos : acmd.getDFGMemberPositions()) {
                memberPositions.add(pos);
            }
        }
        return memberPositions;
    }
    
    private void addFetchGroupMemberPositions(AbstractClassMetaData acmd, Set<Integer> memberPositions, FetchGroupMetaData fm) {
        for (AbstractMemberMetaData ammd : fm.getMemberMetaData()) {
            memberPositions.add(ammd.getAbsoluteFieldNumber());
        }
        for (FetchGroupMetaData f : fm.getFetchGroupMetaData()) {
            addFetchGroupMemberPositions(acmd, memberPositions, f);
        }
    }
    
    private List<Object> getResultFromQueryResult(final AbstractClassMetaData acmd, final int[] fieldsToLoad,
            QueryResult qr, Expression[] resultExpr) throws ConnectionException, SQLException {
        List<Object> results = new ArrayList<Object>();
        final ClassLoaderResolver clr = ec.getClassLoaderResolver();
        final ForceStoreManager storeManager = (ForceStoreManager) ec.getStoreManager();
        /**
         * Here is how we send the data back -
         *
         * - No candidate class and no result class - the result will be a List of Objects
         *   (when there is a single column in the query), or a List of Object[]s (when there are multiple columns in the query)
         * - Candidate class specified, no result class - the result will be a List of candidate class objects,
         *   or will be a single candidate class object (when you have specified "unique").
         *   The columns of the query's result set are matched up to the fields of the candidate class by name.
         *   You need to select a minimum of the PK columns in the SQL statement.
         * - No candidate class, result class specified - the result will be a List of result class objects,
         *   or will be a single result class object (when you have specified "unique").
         *   Your result class has to abide by the rules of JDO2 result classes (see Result Class specification)
         *   This typically means either providing public fields matching
         *   the columns of the result, or providing setters/getters for the columns of the result.
         * - Candidate class and result class specified - the result will be a List of result class objects,
         *   or will be a single result class object (when you have specified "unique").
         *   The result class has to abide by the rules of JDO2 result classes (see Result Class specification).
         */
        if (resultExpr != null || (acmd == null && query.getResultClass() != null)) {
            /**
             * This section is for scalar, aggregate queries, group by, having etc.
             */
            if (qr.getRecords().length > 0) {
                results.addAll(readNonEntityObjects(qr.getRecords(), resultExpr, query.getResultClass()));
            } else if (resultExpr.length == 1 && resultExpr[0] instanceof InvokeExpression
                        && "COUNT".equals(((InvokeExpression) resultExpr[0]).getOperation())) {
                // Typically means select count() or no rows found.
                // For count we return a long value for no rows found we just return empty result
                results.add((long) qr.getSize());
            }
        } else if (fieldsToLoad == null) {
            // This is for native queries only.
            // Where we do not specify return object type. We just return the SObjects in that case
            if (query instanceof SOQLQuery && ((SOQLQuery) query).getResultMetaData() != null) {
                // Do resultset processing
                ResultMetaDataROF rof = new ResultMetaDataROF(((SOQLQuery) query).getResultMetaData());
                for (final SObject sobject : qr.getRecords()) {
                    results.add(rof.getObject(ec, mconn, query, sobject));
                }
            } else {
                Collections.addAll(results, qr.getRecords());
            }
        } else {
            for (final SObject sobject : qr.getRecords()) {
                results.add(ec.findObjectUsingAID(new Type(clr.classForName(acmd.getFullClassName())),
                                getFieldValues2(acmd, fieldsToLoad, mconn, storeManager, sobject, query),
                                query.getIgnoreCache(), true));
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Result raw rows: " + qr.getRecords().length + " processed rows: " + results.size());
        }
        return results;
    }

    /**
     * Constructs the proper interface for fetching fields on sobjects.
     *
     * @param acmd  the class metadata for the entity being queried
     * @param fieldsToLoad  the fields selected by the query (or selected by the fetch group)
     * @param mconn  the managed connection to Force.com APIs
     * @param storeManager  the store manager
     * @param sobject the sobject being queried
     * @param query the query for retrieving objects
     * @return  an interface for retrieving field values
     */
    public static FieldValues2 getFieldValues2(
            final AbstractClassMetaData acmd, final int[] fieldsToLoad,
            final ForceManagedConnection mconn,
            final ForceStoreManager storeManager, final XmlObject sobject, final Query query) {
        return new FieldValues2() {
            // StateManager calls the fetchFields method
            @Override
            public void fetchFields(ObjectProvider sm) {
                try {
                    sm.replaceFields(acmd.getPKMemberPositions(),
                                        new ForceFetchFieldManager(sm, storeManager, mconn, sobject, query));
                    sm.replaceFields(fieldsToLoad, new ForceFetchFieldManager(sm, storeManager, mconn, sobject, query));
                } catch (Exception e) {
                    throw new NucleusException(e.getMessage(), e);
                }
            }

            @Override
            public void fetchNonLoadedFields(ObjectProvider sm) {
                try {
                    sm.replaceNonLoadedFields(acmd.getAllMemberPositions(),
                                                new ForceFetchFieldManager(sm, storeManager, mconn, sobject, query));
                } catch (Exception e) {
                    throw new NucleusException(e.getMessage(), e);
                }
            }

            @Override
            public FetchPlan getFetchPlanForLoading() {
                return null;
            }
        };
    }
    
    private void getFieldNameList(SObject[] sObjects, boolean createFieldNameExpressions,
            List<String> fieldNameList, List<Expression> fieldNameExprs) {
        Iterator<XmlObject> fieldIter = sObjects[0].getChildren();
        /**
         * Skip the first two items. First item is "type" and second is "id".
         * If we have selected id then it will show up again in the list.
         */
        for (int i = 0; fieldIter.hasNext(); i++) {
            XmlObject xo = fieldIter.next();
            String name = xo.getName().getLocalPart();
            if (i < 2) continue;
            fieldNameList.add(name);
            if (createFieldNameExpressions) {
                fieldNameExprs.add(new PrimaryExpression(Arrays.asList(name)));
            }
        }
    }
    
    private Collection<Object> readExpressionObjects(SObject[] sObjects, Expression[] exprs, List<String> fieldNameList)
        throws ConnectionException, SQLException {
        
        List<Object> res = new ArrayList<Object>(sObjects.length);
        Map<String, ForceFetchFieldManager> ffms = new HashMap<String, ForceFetchFieldManager>();
        for (SObject sObject : sObjects) {
            ffms.clear();
            List<Object> row = new ArrayList<Object>(exprs.length);
            for (int i = 0; i < exprs.length; i++) {
                row.add(getDataForExpression(ffms, sObject, exprs[i], fieldNameList.get(i), null));
            }
            res.add(row.size() > 1 ? row.toArray() : row.get(0));
        }
        return res;
    }

    private Collection<Object> readCreatorExpressionObjects(Class clazz, SObject[] sObjects, Expression[] exprs,
            List<String> fieldNameList) throws ConnectionException, SQLException {
        Collection<Object> res = readExpressionObjects(sObjects, exprs, fieldNameList);
        return new JDOQLResultClassMapper(clazz).map(res, toShortNameExpressions(exprs));
    }

    /**
     * Given a list of expressions in a.b.c format return a list of expressions that are only field names, 'c' in this example.
     * @param exprs - expressions to convert
     * @return - returns an array of expressions that only contain the field name
     */
    private Expression[] toShortNameExpressions(Expression[] exprs) {
        if (exprs == null || exprs.length == 0) return exprs;
        Expression[] shortNameExpr = new Expression[exprs.length];
        for (int i = 0; i < exprs.length; i++) {
            if (exprs[i] instanceof PrimaryExpression) {
                List<String> t = ((PrimaryExpression) exprs[i]).getTuples();
                shortNameExpr[i] = new PrimaryExpression(Arrays.asList(t.get(t.size() - 1)));
            } else {
                shortNameExpr[i] = exprs[i];
            }
        }
        return shortNameExpr;
    }
    
    /**
     * Loads the class and member metadata for the given expression.
     * 
     * @param expr the expression
     * @return the metadata for the given expression
     */
    private ExpressionMetaData getExpressionMetaData(Expression expr) {
        AbstractClassMetaData cmd = null;
        AbstractMemberMetaData mmd = null;
        
        ExpressionMetaData exprMetaData = null;
        if (expr instanceof PrimaryExpression) {
            exprMetaData = new ExpressionMetaData();
            
            List<String> ids = ((PrimaryExpression) expr).getTuples();
            String id = null;
            for (int i = 0; i < ids.size(); i++) {
                id = ids.get(i);
                
                // Load up the class meta data from the symbol (e.g. Select o.Id From User o
                // will load the metadata for User from the alias 'o')
                Symbol symbol = query.getCompilation().getSymbolTable().getSymbol(id);
                if (symbol != null) {
                    cmd = ec.getMetaDataManager().getMetaDataForClass(symbol.getValueType(), ec.getClassLoaderResolver());
                    exprMetaData.setClassMetaData(cmd);
                    
                // Get the member meta data from the class loaded above
                } else if (cmd != null) {
                    mmd = cmd.getMetaDataForMember(id);
                    if (mmd == null) {
                        throw new NucleusUserException("Symbol not found, entity: " + cmd.getName() + " symbol: " + id);
                    }
                    
                    exprMetaData.setMemberMetaData(mmd);
                } else {
                    throw new NucleusUserException("Could not find alias for field: " + id);
                }
            }
        }
        return exprMetaData;
    }
    
    private Object getDataForExpression(Map<String, ForceFetchFieldManager> ffms, SObject sObject, Expression expr,
            String fieldName, Object valueOverride) throws ConnectionException, SQLException {
        if (expr instanceof PrimaryExpression) {
            ExpressionMetaData exprMetaData = getExpressionMetaData(expr);
            if (exprMetaData != null) {
                AbstractMemberMetaData mmd = exprMetaData.getMemberMetaData();
                AbstractClassMetaData cmd = exprMetaData.getClassMetaData();
                if (cmd != null) {
                    ForceFetchFieldManager ffm = ffms.get(cmd.getName());
                    if (ffm == null) {
                        ffm =
                            new ForceFetchFieldManager(ec, cmd, (ForceStoreManager) ec.getStoreManager(), mconn, sObject, query);
                        ffms.put(cmd.getName(), ffm);
                    }
                    return ffm.fetchObjectField(mmd, valueOverride);
                }
            }
        } else if (expr instanceof InvokeExpression) {
            /**
             * COUNT always returns Long. Note count expressions never come here since SOQL returns 0 rows for that
             * MAX and MIN return the type of the expression they are applied on
             * AVG always returns Double (default)
             * SUM returns Long for an integral type, Double for a floating point types, 
             *     BigInteger for BigInteger and BigDecimal for BigDecimal
             */
            InvokeExpression ev = (InvokeExpression) expr;
            String oper = ev.getOperation();
            if ("mapKey".equals(oper) || "mapValue".equals(oper) || "mapEntry".equals(oper)) {
                String alias = expr.getLeft().getSymbol().getQualifiedName();
                Map<String, Object> childrenMap =
                    (Map) getDataForExpression(ffms, sObject,
                            getPrimaryExpresionFromJoinAlias(query.getCompilation(), alias), fieldName, null);
                if ("mapKey".equals(oper)) {
                    return Collections.unmodifiableList(new ArrayList<String>(childrenMap.keySet()));
                } else if ("mapValue".equals(oper)) {
                    return Collections.unmodifiableList(new ArrayList<Object>(childrenMap.values()));
                } else {
                    return Collections.unmodifiableList(new ArrayList<Object>(childrenMap.entrySet()));
                }
            } else if ("MAX".equals(oper) || "MIN".equals(oper)) {
                /**
                 * We send a valueOverride which is the raw data and have it formatted according to the underlying
                 * field.
                 */
                Object value = sObject.getField(fieldName);
                if (value == null) return null;
                if (value instanceof Date || value instanceof Calendar) {
                    return value;
                }
                return getDataForExpression(ffms, sObject, ev.getArguments().get(0), fieldName, value.toString());
            } else if ("SUM".equals(oper)) {
                Object value = sObject.getField(fieldName);
                if (value == null) return value;
                /**
                 * SUM returns Long for an integral type, Double for a floating point types,
                 *     BigInteger for BigInteger and BigDecimal
                 */
                ExpressionMetaData exprMetaData = getExpressionMetaData(ev.getArguments().get(0));
                AbstractMemberMetaData mmd = exprMetaData.getMemberMetaData();
                if (mmd != null) {
                    if (mmd.getType() == Short.TYPE || mmd.getType() == Short.class
                            || mmd.getType() == Integer.TYPE || mmd.getType() == Integer.class
                            || mmd.getType() == Long.TYPE || mmd.getType() == Long.class) {
                        return ((Double) value).longValue();
                    } else if (mmd.getType() == BigInteger.class || mmd.getType() == BigDecimal.class) {
                        return getDataForExpression(ffms, sObject, ev.getArguments().get(0), fieldName, value.toString());
                    }
                }
            }
        }
        return sObject.getField(fieldName);
    }
    
    private PrimaryExpression getPrimaryExpresionFromJoinAlias(QueryCompilation compilation, String alias) {
        for (Expression fromExpr : compilation.getExprFrom()) {
            for (Expression expr = fromExpr.getRight(); expr != null; expr = expr.getRight()) {
                if (expr instanceof JoinExpression && alias.equals(expr.getAlias())) {
                    return ((JoinExpression) expr).getPrimaryExpression();
                }
            }
        }
        return null;
    }
    
    private List<Object> getResultAsCollection(SObject[] sObjects, List<String> fieldNameList) {
        List<Object> res = new ArrayList<Object>(sObjects.length);
        for (SObject sObject : sObjects) {
            List<Object> row = new ArrayList<Object>(fieldNameList.size());
            for (int i = 0; i < fieldNameList.size(); i++) {
                row.add(sObject.getField(fieldNameList.get(i)));
            }
            res.add(row.toArray());
        }
        return res;
    }
    
    private Collection<Object> readNonEntityObjects(SObject[] sObjects, Expression[] exprs, Class resultClass)
        throws ConnectionException, SQLException {
        
        // Create metadata first.
        // Assume the data comes back in the same order as the expressions and use first item to get the metadata
        List<String> fieldNameList = new ArrayList<String>();
        List<Expression> fieldNameExprs = new ArrayList<Expression>();
        boolean createFieldNameExpressions = resultClass != null;
        getFieldNameList(sObjects, createFieldNameExpressions, fieldNameList, fieldNameExprs);

        // Now read the data based on the metadata
        if (createFieldNameExpressions) {
            if (exprs != null && exprs.length > 0) {
                return readCreatorExpressionObjects(resultClass, sObjects, exprs, fieldNameList);
            } else {
                return new JDOQLResultClassMapper(resultClass).map(getResultAsCollection(sObjects, fieldNameList),
                                                                fieldNameExprs.toArray(new Expression[fieldNameExprs.size()]));
            }
        } else if (exprs != null && exprs.length > 0) {
            if (exprs[0] instanceof CreatorExpression) {
                CreatorExpression ce = (CreatorExpression) exprs[0];
                return readCreatorExpressionObjects(ce.getSymbol().getValueType(), sObjects,
                                ce.getArguments().toArray(new Expression[ce.getArguments().size()]), fieldNameList);
            } else {
                return readExpressionObjects(sObjects, exprs, fieldNameList);
            }
        } else {
            return getResultAsCollection(sObjects, fieldNameList);
        }
    }
    
    /**
     * This is used for single item fetch by ID.
     * 
     * @param table the table for the entity being queried
     * @param acmd  the class metadata for the entity being queried
     * @param fieldNumbers  the fields to fetch
     * @param pkValue the id of the object being fetched
     * @param fetchDepth the maximum depth that can be traversed
     *          by a query involving relationships
     * @return the SOQL query
     */
    public String buildQueryWithPK(TableImpl table, AbstractClassMetaData acmd, int[] fieldNumbers,
            String pkValue, int fetchDepth, Set<String> queriedRelationships) {
        ExpressionBuilderHelper helper =
            new ExpressionBuilderHelper(this, fieldNumbers.length * 20 + 100, table, acmd,
                                            false, null, ec.getFetchPlan(), fetchDepth, null, queriedRelationships);
        helper.sb.append("select id");
        List<ColumnImpl> columns = new ArrayList<ColumnImpl>();
        for (int fieldNum : fieldNumbers) {
            columns.clear();
            
            List<ColumnImpl> cols =
                table.getColumnsFor(acmd, acmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNum),
                                        (ForceStoreManager) ec.getStoreManager(), columns);
            for (ColumnImpl col : cols) {
                if (!"id".equals(col.getForceApiName())) {
                    col.appendSelectString(helper, acmd, fieldNum, true, null);
                }
            }
        }
        helper.sb.append(" from ").append(table.getTableName().getForceApiName());
        helper.sb.append(String.format(" where %s='%s'", table.getPKFieldName(acmd), pkValue));
        String ret = helper.sb.toString();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Fetch object: " + table.getTableName().getName() + " id: " + pkValue);
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Fetch query: " + ret);
        }
        return helper.sb.toString();
    }
    
    /**
     * 
     * Builds a query for fetching multiple top level objects.
     * 
     * @param table  the table representing the object being queried
     * @param acmd  the class metadata for the object being queried
     * @param fieldsToLoad the set of fields to load
     * @param compilation the compile query
     * @param skipId  {@code true} if the id field should be skipped which might be necessary during joins
     * @param maxLimit the max number of entities that can be retrieved by this query
     * @param fetchPlan  the fetch plan used for this query
     * @param tableName  the name of the entity in Force.com
     * @return the SOQL query
     */
    public String buildQuery(TableImpl table, AbstractClassMetaData acmd, Set<Integer> fieldsToLoad, QueryCompilation compilation,
            boolean skipId, long maxLimit, FetchPlan fetchPlan, String tableName) {
        return buildQuery(table, acmd, fieldsToLoad, compilation, skipId, maxLimit, fetchPlan,
                            0, tableName, true, false, null, null, null);
    }
    
    /**
     * 
     * Builds a query for fetching multiple objects, whether top level or part of a join query.
     * 
     * @param table  the table representing the object being queried
     * @param acmd  the class metadata for the object being queried
     * @param fieldsToLoad the set of fields to load
     * @param compilation the compile query
     * @param skipId  {@code true} if the id field should be skipped which might be necessary during joins
     * @param maxLimit the max number of entities that can be retrieved by this query
     * @param fetchPlan  the fetch plan used for this query
     * @param fetchDepth the depth that fetches should go (i.e. how many relationships can be traversed)
     * @param tableName  the name of the entity in Force.com
     * @param isTopLevel whether this object is the top level entity being queried or is part of a subquery
     * @param isJoin whether this is part of a join call
     * @param joinAlias the alias for a join query
     * @param parentHelper the expression builder for the parent query
     * @return the SOQL query
     */
    public String buildQuery(TableImpl table, AbstractClassMetaData acmd, Set<Integer> fieldsToLoad, QueryCompilation compilation,
            boolean skipId, long maxLimit, FetchPlan fetchPlan, int fetchDepth,
            String tableName, boolean isTopLevel, boolean isJoin, String joinAlias, ExpressionBuilderHelper parentHelper,
            Set<String> queriedRelationships)  {
        ExpressionBuilderHelper helper =
            new ExpressionBuilderHelper(this, (fieldsToLoad != null ? fieldsToLoad.size() : 3) * 20 + 100,
                table, acmd, isJoin, compilation, fetchPlan, fetchDepth, parentHelper, queriedRelationships);
        helper.sb.append("select ");
        if (compilation != null && compilation.getResultDistinct()) {
            throw new NucleusException("select distinct not supported by force.com datastore");
        }
        if (compilation != null && compilation.getExprResult() != null) {
            appendExpressionList(helper, compilation.getExprResult(), ec);
        } else {
            int count = 0;
            if (!skipId) {
                helper.sb.append("id");
                count++;
            }
            if (fieldsToLoad != null) {
                /**
                 * If there are relatedObject filters eager load them if not already loaded
                 */
                if (helper.relatedJoinAliases != null) {
                    for (Map.Entry<TupleName, String> ent : helper.relatedJoinAliases.entrySet()) {
                        TupleName fullName = ent.getKey();
                        int pos = acmd.getAbsolutePositionOfMember(fullName.getShortName());
                        if (pos < 0) {
                            throw new NucleusDataStoreException("Cannot locate member metadata for field: "
                                                                + fullName.getLongName());
                        }
                        fieldsToLoad.add(pos);
                    }
                }

                List<ColumnImpl> columns = new ArrayList<ColumnImpl>();
                for (int fieldNum : fieldsToLoad) {
                    columns.clear();

                    List<ColumnImpl> cols =
                        table.getColumnsFor(acmd, acmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNum),
                                                (ForceStoreManager) ec.getStoreManager(), columns);
                    for (ColumnImpl col : cols) {
                        if (!"id".equals(col.getForceApiName())) {
                            if (col.appendSelectString(helper, acmd, fieldNum, count > 0, null)) {
                                count++;
                            }
                        }
                    }
                }
            }
        }
        helper.sb.append(" from ").append(tableName);
        if (compilation != null) {
            String alias = isTopLevel ? compilation.getCandidateAlias() : joinAlias;
            if (alias != null) {
                helper.sb.append(String.format(" %s ", alias));
            }
            
            // Do the where clause
            Expression filterExpression = helper.getFilterExpression(alias);
            if (filterExpression != null) {
                helper.sb.append(" where (");
                appendExpression(helper, filterExpression, ec);
                helper.sb.append(")");
            }
            
            // Do any join processing for top level but not for Map/Collection joins
            if (isTopLevel && helper.relatedJoinAliases == null) {
                processJoin(helper, compilation, fetchPlan, filterExpression != null);
            }

            // Do the group by clause
            if (isTopLevel && compilation.getExprGrouping() != null) {
                helper.sb.append(" group by ");
                appendExpressionList(helper, compilation.getExprGrouping(), ec);
            }
            // Do the having clause
            if (isTopLevel && compilation.getExprHaving() != null) {
                if (compilation.getExprGrouping() == null) {
                    throw new NucleusException("Queries specifying a HAVING clause must also specify a GROUP BY clause");
                }
                Expression expr = compilation.getExprHaving();
                boolean isAggregate = isAggregate(expr);
                if (!isAggregate) {
                    throw new NucleusException("HAVING clauses must reference an aggregate function");
                }
                helper.sb.append(" having ");
                appendExpression(helper, compilation.getExprHaving(), ec);
            }
            // Do the order by clause
            if (isTopLevel && compilation.getExprOrdering() != null) {
                helper.sb.append(" order by ");
                appendExpressionList(helper, compilation.getExprOrdering(), ec);
            }
        }
        // Add limit if needed
        if (maxLimit > 0 && maxLimit < Long.MAX_VALUE) {
            helper.sb.append(" limit ");
            helper.sb.append(maxLimit);
        }
        String ret = helper.sb.toString();
        if (listeners != null && !listeners.isEmpty()) {
            for (QueryListener listener : listeners.values()) {
                listener.listen(ret);
            }
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Table: " + table.getTableName().getName() + " query: " + ret);
        }
        return ret;
    }
    
    private void processJoin(ExpressionBuilderHelper h, QueryCompilation compilation, FetchPlan fetchPlan, boolean hasWhere) {
        if (compilation.getExprFrom() == null) return;
        for (Expression fromExpr : compilation.getExprFrom()) {
            if (!(fromExpr instanceof ClassExpression)) {
                throw new NucleusDataStoreException("Unsupported from expression: " + fromExpr);
            }
            Expression expr = fromExpr.getRight();
            while (expr != null) {
                if (expr instanceof JoinExpression) {
                    JoinExpression je = (JoinExpression) expr;
                    processJoinExpression(je.getPrimaryExpression(), je.getAlias(), h, compilation, true, hasWhere, false);
                    h.sb.append("))");
                    // Either this was already true or will be after the first call
                    hasWhere = true;
                } else {
                    throw new NucleusDataStoreException("Unsupported expression found inside a join expression: "
                                                        + fromExpr.getRight());
                }
                expr = expr.getRight();
            }
        }
    }
    
    /**
     * Handles explicit JOIN o.ownerId type of joins or implicit member of o.children.name.
     * 
     * @return h.sb is updated with in where (select ...) query.
     *         The query is unclosed and the caller is expected to close it with ')'
     */
    private void processJoinExpression(PrimaryExpression pe, String alias, ExpressionBuilderHelper h,
            QueryCompilation compilation, boolean needsWhere, boolean hasWhere, boolean not)  {
        AbstractClassMetaData cmd = h.acmd;
        AbstractMemberMetaData mmd = null;
        List<String> ids = pe.getTuples();
        String id = null;
        for (int i = 0; i < ids.size(); i++) {
            id = ids.get(i);
            Symbol symbol = compilation.getSymbolTable().getSymbol(id);
            if (symbol != null) {
                cmd = ec.getMetaDataManager().getMetaDataForClass(symbol.getValueType(), ec.getClassLoaderResolver());
            } else {
                mmd = cmd.getMetaDataForMember(id);
                if (mmd == null) {
                    throw new NucleusUserException("Symbol not found, entity: " + cmd.getName() + " symbol: " + id);
                }
                // If these are collections get underlying object metadata
                if (mmd.getCollection() != null || mmd.getMap() != null) {
                    cmd = PersistenceUtils.getMemberElementClassMetaData(mmd, ec.getClassLoaderResolver(),
                                                                            ec.getMetaDataManager());
                } else {
                    cmd = ec.getMetaDataManager().getMetaDataForClass(mmd.getType(), ec.getClassLoaderResolver());
                }
            }
        }

        TableImpl joinTable = ((ForceStoreManager) ec.getStoreManager()).getTable(cmd);
        Set<Integer> joinFieldsToLoad = null;
        String inField = null;
        boolean skipId = true;
        if (mmd.getMappedBy() != null) {
            //Now find the ammd that is the FK to this ammd since the relationship can be invoked from that direction
            mmd = cmd.getMetaDataForMember(mmd.getMappedBy());
            joinFieldsToLoad = Collections.singleton(mmd.getAbsoluteFieldNumber());
            inField = "id";
        } else {
            // Here we select id of the Entity but the inField has to be the FK name
            joinFieldsToLoad = Collections.singleton(cmd.getPKMemberPositions()[0]);
            TableImpl joiningTable = ((ForceStoreManager) ec.getStoreManager()).getTable(mmd.getAbstractClassMetaData());
            inField = joiningTable.getColumnByJavaName(mmd.getName()).getFieldName();
            skipId = false;
        }
        if (needsWhere) {
            if (hasWhere) {
                h.sb.append(" and (");
            } else {
                h.sb.append(" where (");
            }
        }
        h.sb.append(inField);
        if (not) h.sb.append(" not");
        h.sb.append(" in (").append(buildQuery(joinTable, cmd, joinFieldsToLoad, compilation, skipId,
                                                0, h.fetchPlan, h.fetchDepth, joinTable.getTableName().getForceApiName(),
                                                false, true, alias, h, h.queriedRelationships));
    }
    
    private boolean isAggregate(Expression expr) {
        if (expr == null) return false;
        
        if (expr instanceof InvokeExpression) {
            String methodName = ((InvokeExpression) expr).getOperation();
            return AGGREGATE_METHODS.contains(methodName);
        } else {
            return isAggregate(expr.getRight()) || isAggregate(expr.getLeft());
        }
    }
    
    private void appendExpressionList(ExpressionBuilderHelper h, Expression[] exprList, ExecutionContext executionContext)  {
        for (int i = 0; i < exprList.length; i++) {
            if (i > 0) {
                h.sb.append(", ");
            }
            appendExpression(h, exprList[i], executionContext);
        }
    }

    private boolean appendExpression(ExpressionBuilderHelper h, Expression expr, ExecutionContext executionContext)  {
        if (expr == null) return false;
        boolean done = false;
        if (h.level > MAX_EXPRESSION_DEPTH) {
            throw new NucleusException(
                    "Expression too deep. Max depth reached at: " + expr);
        }
        h.level++;

        try {
            if (expr instanceof DyadicExpression) {
                if (expr.getOperator() instanceof MonadicOperator) {
                    if (expr.getLeft() instanceof InvokeExpression) {
                        appendInvokeExpression(h, (InvokeExpression) expr.getLeft(),
                                                executionContext, expr.getOperator() == Expression.OP_NOT);
                    } else {
                        h.sb.append(expr.getOperator().toString());
                        appendExpression(h, expr.getLeft(), executionContext);
                    }
                } else {
                    boolean lparens = expr.getLeft() instanceof DyadicExpression;
                    if (lparens) h.sb.append("( ");
                    boolean skip = appendExpression(h, expr.getLeft(), executionContext);
                    if (lparens) h.sb.append(" )");

                    /**
                     * There are certain InvokeExpressions that are rewritten to not require the right expression.
                     * However, we do not want to propagate doneness any further
                     */
                    if (!skip) {
                        h.sb.append(expr.getOperator().toString());
    
                        boolean rparens = expr.getRight() instanceof DyadicExpression;
                        if (rparens) h.sb.append("( ");
                        appendExpression(h, expr.getRight(), executionContext);
                        if (rparens) h.sb.append(" )");
                    }
                }
            } else if (expr instanceof PrimaryExpression) {
                // Recurse!
                appendExpression(h, expr.getLeft(), executionContext);
                appendExpression(h, expr.getRight(), executionContext);
                List<String> ids = ((PrimaryExpression) expr).getTuples();
                int pos = 0;
                TableImpl t = h.table;
                AbstractClassMetaData cmd = h.acmd;
                for (String id : ids) {
                    if (pos++ > 0) {
                        h.sb.append(".");
                    }
                    String name = id;
                    if (pos == ids.size()) {
                        ColumnImpl column = t.getColumnByJavaName(id);
                        if (column != null) {
                            name = column.getFieldName();
                        }
                    } else {
                        AbstractMemberMetaData mmd = cmd.getMetaDataForMember(id);
                        if (mmd != null) {
                            ColumnImpl column = t.getColumnByJavaName(mmd.getName());
                            name = column.getForceApiRelationshipName();
                            AbstractClassMetaData tcmd =
                                executionContext.getMetaDataManager().getMetaDataForClass(mmd.getType(), null);
                            if (tcmd != null) {
                                cmd = tcmd;
                                t = ((ForceStoreManager) executionContext.getStoreManager()).getTable(cmd);
                            }
                        }
                    }
                    h.sb.append(name);
                }
            } else if (expr instanceof InvokeExpression) {
                done = appendInvokeExpression(h, (InvokeExpression) expr, executionContext, false);
            } else if (expr instanceof ParameterExpression) {
                appendValue(h, getParameterValue(h, (ParameterExpression) expr, executionContext));
            } else if (expr instanceof VariableExpression) {
                VariableExpression varExpr = (VariableExpression) expr;
                if (varExpr.getSymbol() != null && varExpr.getSymbol().getQualifiedName() != null) {
                    /**
                     * This is vanilla subquery processing, i.e. select ... (select ...) created from JPQL/JDOQL
                     */
                    QueryCompilation subCompilation =
                        h.compilation.getCompilationForSubquery(varExpr.getSymbol().getQualifiedName());
                    if (subCompilation != null) {
                        AbstractClassMetaData cmd =
                            executionContext.getMetaDataManager().getMetaDataForClass(subCompilation.getCandidateClass(),
                                                                            executionContext.getClassLoaderResolver());
                        TableImpl joinTable = ((ForceStoreManager) executionContext.getStoreManager()).getTable(cmd);
                        // Pretend it's a top level query since it has its own compilation. Also fieldsList is null as
                        // we want select to use subCompilation.getExprResult()
                        h.sb.append("(").append(buildQuery(joinTable, cmd, null, subCompilation, true,
                                                            0, h.fetchPlan, joinTable.getTableName().getForceApiName()))
                                        .append(")");
                    }
                } else {
                    throw new NucleusUserException(
                            "Unexpected expression type while parsing query.  Are you certain that a field named "
                                    + varExpr.getId() + " exists on your object?");
                }
            } else if (expr instanceof Literal) {
                Object literal = ((Literal) expr).getLiteral();
                if (literal == null) literal = "NULL";
                appendValue(h, literal);
            } else if (expr instanceof OrderExpression) {
                appendExpression(h, expr.getLeft(), executionContext);
                OrderExpression order = (OrderExpression) expr;
                h.sb.append(String.format(" %s ", "ascending".equals(order.getSortOrder()) ? "ASC" : "DESC"));
            } else if (expr instanceof CreatorExpression) {
                CreatorExpression ce = (CreatorExpression) expr;
                appendExpressionList(h, ce.getArguments().<Expression>toArray(new Expression[ce.getArguments().size()]),
                                        executionContext);
            } else if (expr instanceof SubqueryExpression) {
                SubqueryExpression subExpr = (SubqueryExpression) expr;
                if ("EXISTS".equals(subExpr.getKeyword())) {
                    throw new NucleusUserException("EXISTS is not supported in force.com database");
                } else if (UNSUPPORTED_JOINS.contains(subExpr.getKeyword())) {
                    throw new NucleusUserException(subExpr.getKeyword() + " is not supported in force.com database");
                } else {
                    throw new NucleusUserException("Unexpected subquery expression: " + subExpr);
                }
            } else {
                throw new NucleusException(
                        "Unexpected expression type while parsing query: " + expr.getClass().getName());
            }
        } finally {
            h.level--;
        }
        return done;
    }
    
    private TupleName getMappedExpression(ExpressionBuilderHelper h, InvokeExpression expr) {
        String alias = expr.getLeft().getSymbol().getQualifiedName();
        TupleName fieldName = null;
        for (Map.Entry<TupleName, String> ent : h.relatedJoinAliases.entrySet()) {
            if (ent.getValue().equals(alias)) {
                fieldName = ent.getKey();
                break;
            }
        }
        if (fieldName == null) {
            throw new NucleusDataStoreException("Cannot find field name for alias: " + alias);
        }
        return fieldName;
    }
    
    private void appendMappedByExpression(ExpressionBuilderHelper h, InvokeExpression expr) {
        TupleName fieldName = getMappedExpression(h, expr);
        /**
         * The key is defined by @MapKey on the Map field and defaulted to "id" 
         */
        String columnToAdd = "id";
        AbstractClassMetaData acmd = fieldName.getTuple().size() > 1
                ? ec.getMetaDataManager()
                        .getMetaDataForClass(h.compilation.getSymbolTable().getSymbol(fieldName.getShortNamePrefix())
                                .getValueType(), ec.getClassLoaderResolver()) : h.acmd;
        AbstractMemberMetaData ammd = acmd.getMetaDataForMember(fieldName.getShortName());
        if (ammd.getKeyMetaData() != null) {
            AbstractClassMetaData cmd =
                PersistenceUtils.getMemberElementClassMetaData(ammd, ec.getClassLoaderResolver(), ec.getMetaDataManager());
            if (cmd != null) {
               TableImpl joinedTable = ((ForceStoreManager) ec.getStoreManager()).getTable(cmd);
                ColumnImpl col = joinedTable.getColumnByJavaName(ammd.getKeyMetaData().getMappedBy());
                if (col != null) {
                    columnToAdd = col.getFieldName();
                }
            }
        }
        h.sb.append(columnToAdd);
    }

    private boolean appendInvokeExpression(ExpressionBuilderHelper h, InvokeExpression invocation,
                                            ExecutionContext executionContext, boolean not) {
        boolean done = false;
        String oper = invocation.getOperation();
        if ("matches".equals(oper)) {
            if (not) h.sb.append("NOT ");
            appendExpression(h, invocation.getLeft(), executionContext);
            h.sb.append(" like ");
            appendExpression(h, invocation.getArguments().get(0), executionContext);
        } else if ("toLowerCase".equals(oper)) {
            //ignore toLowerCase since SFDC is case insensitive
            appendExpression(h, invocation.getLeft(), executionContext);
        } else if ("COUNT".equals(oper)) {
            h.sb.append(String.format(" %s()", oper));
        } else if ("CURRENT_DATE".equals(oper)) {
            h.sb.append(String.format(" %s", getCurrentDateHint(h)));
        } else if ("CURRENT_TIMESTAMP".equals(oper) || "CURRENT_TIME".equals(oper)) {
            throw new NucleusUserException("CURRENT_TIMESTAMP or CURRENT_TIME is not supported by Force.com datastore");
        } else if ("mapKey".equals(oper) || "mapValue".equals(oper) || "mapEntry".equals(oper)) {
            if (h.isInSelect) {
                /**
                 * Based JSR-317 Final Release section 4.4.4 Path Expressions I interpret valueOf(p) = p
                 */
                TupleName fieldName = getMappedExpression(h, invocation);
                ColumnImpl column = h.table.getColumnByJavaName(fieldName.getShortName());
                if (column != null) {
                    column.appendSelectString(h, h.acmd,
                                                h.acmd.getAbsolutePositionOfMember(fieldName.getShortName()), false, null);
                }
            } else {
                appendMappedByExpression(h, invocation);
            }
        } else if ("contains".equals(oper)) {
            appendContainsExpression(h, invocation, not);
        } else if ("size".equals(oper)) {
            boolean isEmpty = invocation.getParent().getOperator() == Expression.OP_EQ;
            // If the method isEmpty we request !contains; if isnotEmpty we request contains
            appendContainsExpression(h, invocation, isEmpty);
            // Now there is leftover = 0 expression that needs to be skipped
            done = true;
        } else {
            // The format here is operation(left, arguments ...);
            if (not) h.sb.append(" NOT ");
            h.sb.append(String.format(" %s(", oper));
            appendExpression(h, invocation.getLeft(), executionContext);

            int pos = 0;
            for (Expression e : invocation.getArguments()) {
                if (pos++ > 0) {
                    h.sb.append(", ");
                }
                appendExpression(h, e, executionContext);
            }
            h.sb.append(") ");
            appendExpression(h, invocation.getRight(), executionContext);
        }
        return done;
    }
    
    private void appendContainsExpression(ExpressionBuilderHelper h, InvokeExpression expr, boolean not)  {
        PrimaryExpression pe = (PrimaryExpression) expr.getLeft();
        TupleName fieldName = new TupleName(pe.getTuples());
        AbstractMemberMetaData ammd = h.acmd.getMetaDataForMember(fieldName.getShortName());
        if (ammd != null && (ammd.getMap() != null || ammd.getCollection() != null)) {
            processJoinExpression(pe, null, h, h.compilation, false, false, not);
            if (expr.getArguments().size() > 0) {
                String name = hints != null ? (String) hints.get(QueryHints.MEMBER_OF_FIELD) : null;
                h.sb.append(" where ");
                appendExpression(h, new PrimaryExpression(new TupleName(name != null ? name : "name").getTuple()), ec);
                h.sb.append(" = ");
                appendExpression(h, expr.getArguments().get(0), ec);
            }
            h.sb.append(")");
        } else {
            // This is simply picklist values
            appendExpression(h, expr.getLeft(), ec);
            if (expr.getArguments().size() > 0) {
                h.sb.append(not ? " excludes(" : " includes(");
                Literal l = (Literal) expr.getArguments().get(0);
                h.sb.append(new MultiPicklistFormatter(l.getLiteral().toString()).getFormattedString());
                h.sb.append(")");
            } else {
                h.sb.append(not ? " = " : " != ").append("null");
            }
        }
    }
    
    private String getCurrentDateHint(ExpressionBuilderHelper h) {
        Object hint = hints != null ? hints.get(QueryHints.CURRENT_DATE) : null;
        if (hint instanceof String[]) {
            String[] hArray = (String[]) hint;
            if (currentHint < hArray.length) {
                hint = hArray[currentHint++];
            }
        }
        if (hint instanceof String) {
            return (String) hint;
        }
        return "TODAY";
    }
    
    private void appendValue(ExpressionBuilderHelper h, Object value) {
        Class clazz = value.getClass();
        if (clazz == String.class || clazz == Character.class || clazz == URL.class
                || clazz == Byte.class || clazz == byte.class) {
            h.sb.append(String.format("'%s'", value));
        } else if (clazz == Date.class || value instanceof Calendar) {
            if (clazz == Date.class) {
                h.sb.append(SOQLDateFormatUtil.getSOQLFormat((Date) value));
            } else {
                h.sb.append(SOQLDateFormatUtil.getSOQLFormat((Calendar) value));
            }
        } else {
            h.sb.append(value);
        }
    }
    
    private Object getParameterValue(ExpressionBuilderHelper h, ParameterExpression expr, ExecutionContext executionContext) {
        Object paramValue = parameters.get(expr.getId());
        if (paramValue != null) return getTransformedValueFromParamValue(paramValue, executionContext);

        // Check if a positional param is set
        try {
            paramValue = parameters.get(Integer.parseInt(expr.getId()));
            if (paramValue != null) {
                return getTransformedValueFromParamValue(paramValue, executionContext);
            }
            return paramValue;
        } catch (NumberFormatException e) {
            // we are going to settle for null here
        }
        return new NucleusException("Cannot find parameter expression: " + expr.toString());
    }
    
    private Object getTransformedValueFromParamValue(Object paramValue, ExecutionContext executionContext) {
        AbstractClassMetaData acmd =
            executionContext.getMetaDataManager().getMetaDataForClass(paramValue.getClass(),
                                                                        executionContext.getClassLoaderResolver());
        if (acmd != null && paramValue instanceof PersistenceCapable) {
            return getIdFromObject((PersistenceCapable) paramValue, acmd).toString();
        } else if (paramValue instanceof Collection<?>) {
            return new CollectionParameter((Collection<?>) paramValue);
        } else {
            return paramValue;
        }
    }
    
    /**
     * Convenience method for getting the id value from an entity.
     * 
     * @param entity the entity containing the id
     * @param acmd the class metadata (for discovering the id field)
     * @return the id value of the object
     */
    public static Object getIdFromObject(PersistenceCapable entity, AbstractClassMetaData acmd) {
        Object ret = null;
        AbstractMemberMetaData ammd = acmd.getMetaDataForManagedMemberAtAbsolutePosition(acmd.getPKMemberPositions()[0]);
        try {
            ret = PersistenceUtils.getMemberValue(acmd, acmd.getPKMemberPositions()[0], entity);
            if (ret instanceof String) {
                ret = new StringIdentity(ammd.getType(), (String) ret);
            }
            return ret;
        } catch (Exception e) {
            throw new NucleusDataStoreException(e.getMessage(), e);
        }
    }
    
    /**
     * Helper method to handle joining a relationship field to a query that is currently being built.
     * 
     * @param helper  the expression builder with the in progress query
     * @param ammd  the member metadata for the relationship being appended
     * @param col  the column data for the relationship field
     */
    public void appendRelationshipQuery(ExpressionBuilderHelper helper, AbstractMemberMetaData ammd, ColumnImpl col)  {
        FetchPlan fetchPlan = ec.getFetchPlan();
        Set<Integer> joinFieldsToLoad = getFieldsToLoad(helper.acmd, fetchPlan);
        String relName = col.getForceApiRelationshipName();
        helper.getBuilder().append("(")
                           .append(buildQuery(helper.table, helper.acmd, joinFieldsToLoad, null, false,
                                               0, fetchPlan, helper.fetchDepth, relName, false, false, null, null,
                                               helper.queriedRelationships));
        /**
         * If there is a filter for these related object in the context use it
         * Else use any JoinFilters
         * Else no filter
         */
       TupleName name = new TupleName(ammd.getName());
       if (helper.relatedJoinAliases != null && helper.relatedJoinAliases.containsKey(name)) {
            Expression filter = helper.aliasToFilterMappings.get(helper.relatedJoinAliases.get(name));
            if (filter == null) {
                throw new NucleusDataStoreException("Could not locate related filter for alias: "
                                                    + helper.relatedJoinAliases.get(name));
            }
            helper.getBuilder().append(" ").append(helper.relatedJoinAliases.get(name)).append(" where (");
            boolean oldIsInSelect = helper.isInSelect;
            helper.isInSelect = false;
            appendExpression(helper, filter, ec);
            helper.isInSelect = oldIsInSelect;
            helper.getBuilder().append(")");
        } else {
            JoinFilter joinFilter = PersistenceUtils.getMemberAnnotation(ammd.getMemberRepresented(), JoinFilter.class);
            if (joinFilter != null) {
                if (joinFilter.alias().length() > 0) {
                    helper.getBuilder().append(String.format(" %s", joinFilter.alias()));
                }
                if (joinFilter.value().length() > 0) {
                    helper.getBuilder().append(" where (").append(joinFilter.value()).append(")");
                }
            }
        }
        if (ammd.getOrderMetaData() != null && ammd.getOrderMetaData().getOrdering() != null
                && !ammd.getOrderMetaData().getOrdering().equals("#PK")) {
            JPQLPartialCompiler partialCompiler = new JPQLPartialCompiler(ec, helper.compilation, null, parameters);
            helper.getBuilder().append(" order by ");
            String orderBy = ammd.getOrderMetaData().getOrdering();
            if (orderBy == null || orderBy.length() == 0) {
                orderBy = "id";
            }
            Expression[] exprList = partialCompiler.compileOrdering(orderBy);
            appendExpressionList(helper, exprList, ec);
        }
        helper.getBuilder().append(")");
    }
    
    /**
     * Append the fields in the default fetch group for the entity in the relationship to the query.
     * 
     * @param helper  the expression builder with the in progress query
     * @param col  the column data for the relationship field
     * @param prefix  the prefix to add to the relationship field
     */
    public void appendRelationshipFields(ExpressionBuilderHelper helper, ColumnImpl col, String prefix) {
        // Add all the fields for this table
        List<ColumnImpl> columns = new ArrayList<ColumnImpl>();
        int count = 0;
        String newPrefix = prefix != null ? prefix + col.getForceApiRelationshipName() + "."
                                                : col.getForceApiRelationshipName() + ".";
        for (int num : helper.acmd.getDFGMemberPositions()) {
            columns.clear();
            
            List<ColumnImpl> cols =
                helper.table.getColumnsFor(helper.acmd, helper.acmd.getMetaDataForManagedMemberAtAbsolutePosition(num),
                                            (ForceStoreManager) ec.getStoreManager(), columns);
            for (ColumnImpl c : cols) {
                if (c.appendSelectString(helper, helper.acmd, num, count > 0, newPrefix)) {
                    count++;
                }
            }
        }
    }
    
    /**
     * Simple bean which stores JPA class and member metadata 
     * for a DataNucleus Expression.
     *
     * @author Tim Kral
     */
    private static final class ExpressionMetaData {
        private AbstractClassMetaData acmd;
        private AbstractMemberMetaData ammd;
        
        private ExpressionMetaData() {  }
        
        private AbstractClassMetaData getClassMetaData() {
            return this.acmd;
        }
        
        private void setClassMetaData(AbstractClassMetaData classMetaData) {
            this.acmd = classMetaData;
        }
        
        private AbstractMemberMetaData getMemberMetaData() {
            return this.ammd;
        }
        
        private void setMemberMetaData(AbstractMemberMetaData memberMetaData) {
            this.ammd = memberMetaData;
        }
    }
}
