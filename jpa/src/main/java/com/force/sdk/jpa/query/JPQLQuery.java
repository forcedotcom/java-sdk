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
import org.datanucleus.query.evaluator.JPQLEvaluator;
import org.datanucleus.query.evaluator.JavaQueryEvaluator;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.query.AbstractJPQLQuery;
import org.datanucleus.util.NucleusLogger;

import com.force.sdk.jpa.ForceManagedConnection;
import com.force.sdk.jpa.query.ForceQueryUtils.LimitType;

/**
 * 
 * Extension of {@code AbstractJPQLQuery} to apply result restrictions. Implements {@code QueryNotifier}
 * so that listeners can be attached to queries.
 *
 * @author Fiaz Hossain
 */
public class JPQLQuery extends AbstractJPQLQuery implements QueryNotifier {

    private Map<String, QueryListener> listeners = new HashMap<String, QueryListener>(4);
    
    /**
     * Constructs a JPQL query with a null query.
     * 
     * @param ec the execution context
     */
    public JPQLQuery(ExecutionContext ec) {
        this(ec, (JPQLQuery) null);
    }

    /**
     * Constructs a JPQL query.
     * 
     * @param ec the execution context
     * @param q the JPQL query object
     */
    public JPQLQuery(ExecutionContext ec, AbstractJPQLQuery q) {
        super(ec, q);
    }

    /**
     * Constructs a JPQL query with a string query.
     * 
     * @param ec  the execution context
     * @param query  the query in string form
     */
    public JPQLQuery(ExecutionContext ec, String query) {
        super(ec, query);
    }

    @Override
    protected Object performExecute(Map parameters) {
        if (type == BULK_UPDATE) {
            throw new NucleusException("Bulk Update is not yet supported");
        }
        ForceManagedConnection mconn = (ForceManagedConnection) ec.getStoreManager().getConnection(ec);
        try {
            long startTime = System.currentTimeMillis();
            if (NucleusLogger.QUERY.isDebugEnabled()) {
                NucleusLogger.QUERY.debug(LOCALISER.msg("021046", "JPQL", getSingleStringQuery(), null));
            }
            Object results = null;
            if (type == BULK_DELETE) {
                results =
                    new ForceQueryUtils(ec, mconn, this, parameters, listeners, getExtensions())
                            .deleteObjectsOfCandidateType(candidateClass != null ? candidateClass : getFrom());
            } else {
                if (candidateCollection == null) {
                    List<Object> rawResults =
                        new ForceQueryUtils(ec, mconn, this, parameters, listeners, getExtensions())
                                .getObjectsOfCandidateType(compilation.getExprResult());
                    if (ForceQueryUtils.getLimitType(this) == LimitType.Java) {
                        JavaQueryEvaluator resultMapper = new JPQLEvaluator(this, rawResults, compilation,
                                parameters, ec.getClassLoaderResolver());
                            results = resultMapper.execute(false, false, false, false, true);
                    } else {
                        results = rawResults;
                    }
                } else {
                    List candidates = new ArrayList(candidateCollection);
                    // Apply any result restrictions to the results
                    JavaQueryEvaluator resultMapper = new JPQLEvaluator(this, candidates, compilation,
                        parameters, ec.getClassLoaderResolver());
                    results = resultMapper.execute(true, true, true, true, true);
                }
            }
    
            if (NucleusLogger.QUERY.isDebugEnabled()) {
                NucleusLogger.QUERY.debug(LOCALISER.msg("021074", "JPQL", "" + (System.currentTimeMillis() - startTime)));
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

    @Override
    public void addListener(String name, QueryListener listener) {
        listeners.put(name, listener);
    }

    @Override
    public void removeListener(String name) {
        listeners.remove(name);
    }

}
