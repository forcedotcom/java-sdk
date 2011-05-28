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

import java.util.*;

import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.metadata.QueryResultMetaData;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.query.evaluator.JPQLEvaluator;
import org.datanucleus.query.evaluator.JavaQueryEvaluator;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.query.AbstractJavaQuery;
import org.datanucleus.util.NucleusLogger;

import com.force.sdk.jpa.ForceManagedConnection;
import com.force.sdk.jpa.query.ForceQueryUtils.LimitType;

/**
 * 
 * Query class that represents SOQL queries.
 *
 * @author Fiaz Hossain
 */
public class SOQLQuery extends AbstractJavaQuery {

    private String soqlQuery;
    private QueryResultMetaData resultMetadata;
    
    /**
     * Create a query object for SOQL queries.
     * 
     * @param ec the execution context for this query
     */
    public SOQLQuery(ExecutionContext ec) {
        super(ec);
    }
    
    /**
     * Create a query object for SOQL queries.
     * 
     * @param ec the execution context for this query
     * @param query the SOQL query string
     */
    public SOQLQuery(ExecutionContext ec, String query) {
        super(ec);
        this.soqlQuery = query;
    }
    
    @Override
    public String getSingleStringQuery() {
        return soqlQuery;
    }

    @Override
    protected void compileInternal(Map parameterValues) {
        // nothing to do on client side
    }

    QueryResultMetaData getResultMetaData() {
        return this.resultMetadata;
    }
    
    @Override
    public void setResultMetaData(QueryResultMetaData qrmd) {
        resultMetadata = qrmd;
        super.setResultClass(null);
    }
    
    @Override
    public void setResultClass(Class resultCls) {
        super.setResultClass(resultCls);
        resultMetadata = null;
    }
    
    @Override
    protected Object performExecute(Map parameters) {
        if (parameters.size() > 0) {
            throw new NucleusException("Bind parameters not supported on native SOQL query");
        }

        ForceManagedConnection mconn = (ForceManagedConnection) ec.getStoreManager().getConnection(ec);
        try {
            long startTime = System.currentTimeMillis();
            if (NucleusLogger.QUERY.isDebugEnabled()) {
                NucleusLogger.QUERY.debug(LOCALISER.msg("021046", "SOQL", getSingleStringQuery(), null));
            }
            Object results = null;
            if (candidateCollection == null) {
                List<Object> rawResults =
                    new ForceQueryUtils(ec, mconn, this, parameters, null, null).getObjectsOfCandidateType(null);
                if (ForceQueryUtils.getLimitType(this) == LimitType.Java) {
                    JavaQueryEvaluator resultMapper = new JPQLEvaluator(this, rawResults, newDummyQueryCompilation(),
                            parameters, ec.getClassLoaderResolver());
                        results = resultMapper.execute(false, false, false, false, true);
                } else {
                    results = rawResults;
                }
            } else {
                List candidates = new ArrayList(candidateCollection);
                // Apply any result restrictions to the results
                JavaQueryEvaluator resultMapper = new JPQLEvaluator(this, candidates, newDummyQueryCompilation(),
                    parameters, ec.getClassLoaderResolver());
                results = resultMapper.execute(true, true, true, true, true);
            }
    
            if (NucleusLogger.QUERY.isDebugEnabled()) {
                NucleusLogger.QUERY.debug(LOCALISER.msg("021074", "SOQL", "" + (System.currentTimeMillis() - startTime)));
            }
            return results;
        } catch (NucleusException ne) {
            throw ne;
        } catch (Exception e) {
            throw new NucleusException(e.getMessage(), e);
        } finally {
            mconn.release();
        }
    }

    private QueryCompilation newDummyQueryCompilation() {
        return new QueryCompilation(getCandidateClass(), null, null, null, null, null, null, null, null, null);
    }
    
    @Override
    public String getLanguage() {
        return "SOQL";
    }
}
