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

import com.force.sdk.jpa.ForceStoreManager;
import com.force.sdk.jpa.PersistenceUtils;
import com.force.sdk.jpa.table.ColumnImpl;
import com.force.sdk.jpa.table.TableImpl;
import org.datanucleus.FetchPlan;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.query.expression.*;
import org.datanucleus.query.symbol.PropertySymbol;

import java.util.*;

/**
 * 
 * Helper class for building the WHERE clause in SOQL queries.
 *
 * @author Fiaz Hossain
 */
public class ExpressionBuilderHelper {
    ForceQueryUtils forceQuery;
    StringBuilder sb;
    TableImpl table;
    AbstractClassMetaData acmd;
    int level;
    boolean isJoin;
    QueryCompilation compilation;
    FetchPlan fetchPlan;
    int fetchDepth;
    private final int maxFetchDepth;
    Map<String, Expression> aliasToFilterMappings; // alias => where expression
    Map<TupleName, String> relatedJoinAliases; // fieldname => alias
    boolean isInSelect;
    private final boolean isTopLevel;
    Set<String> queriedRelationships; // a set of strings in the form of ParentEntityName->ChildEntityName (or
                                              // vice versa) so we can keep track of which relationships we've already
                                              // included in our query, will help us avoid cycles
    private static final String RELATIONSHIP_SEPARATOR = "->";

    ExpressionBuilderHelper(ForceQueryUtils forceQuery, int length, TableImpl table,
            AbstractClassMetaData acmd, boolean isJoin, QueryCompilation compilation, FetchPlan fetchPlan,
            int fetchDepth, ExpressionBuilderHelper parent, Set<String> queriedRelationships) {
        this.forceQuery = forceQuery;
        this.sb = new StringBuilder(length);
        this.table = table;
        this.acmd = acmd;
        this.level = 0;
        this.isJoin = isJoin;
        this.compilation = compilation;
        this.fetchPlan = fetchPlan;
        this.fetchDepth = fetchDepth;
        this.queriedRelationships = queriedRelationships != null ? queriedRelationships : new HashSet<String>();
        Object mfd = forceQuery.getHints(QueryHints.MAX_FETCH_DEPTH);
        /**
         * For maxFetchDepth use the following priority -
         * First preference given to QueryHints.
         * Second preference to fetchPlan parameter.
         * Finally use the default configured "datanucleus.maxFetchDepth" property
         */
        int maxDepth = mfd != null ? (Integer) mfd : fetchPlan != null ? fetchPlan.getMaxFetchDepth()
            : forceQuery.getExecutionContext().getOMFContext()
                                                .getPersistenceConfiguration().getIntProperty("datanucleus.maxFetchDepth");
        if (maxDepth > 5) {
            throw new NucleusException("Max fetch depth cannot be greater than 5.");
        }
        this.maxFetchDepth = maxDepth >= 0 ? maxDepth : 5;
        if (parent != null) {
            this.aliasToFilterMappings = parent.aliasToFilterMappings;
            this.relatedJoinAliases = parent.relatedJoinAliases;
        } else {
            initRelatedAliases(compilation);
        }
        this.isInSelect = true;
        this.isTopLevel = parent == null;
    }
    
    /**
     * Appends a relationship via a join to this query builder.
     * 
     * @param colCmd the class metadata of the relationship column
     * @param fieldNum the field number of the relationship column
     * @param col the column object for the relationship field
     * @param prefix the prefix to use for the join
     * @param isQuery {@code true} if we're appending the relationship field but not all of the fields of the related object
     */
    public void appendRelationship(AbstractClassMetaData colCmd, int fieldNum, ColumnImpl col, String prefix, boolean isQuery)  {
        AbstractMemberMetaData ammd = colCmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNum);
        AbstractClassMetaData cmd =
            PersistenceUtils.getMemberElementClassMetaData(ammd, forceQuery.getExecutionContext().getClassLoaderResolver(),
                                                            forceQuery.getExecutionContext().getMetaDataManager());
        
        if (cmd == null) {
            /**
             * This can be true if there are Foreign key fields in Force.com that are not fully mapped in Java, e.g.
             * User has a ProfileId field but there is no Profile entity in Java. Treat it just as a string field.
             */
            if (prefix != null) {
                sb.append(prefix);
            }
            sb.append(col.getFieldName());
            return;
        }

        String relationshipString = colCmd.getEntityName() + RELATIONSHIP_SEPARATOR + ammd.getName();
        queriedRelationships.add(relationshipString);

        TableImpl joinTable = ((ForceStoreManager) forceQuery.getExecutionContext().getStoreManager()).getTable(cmd);

        TableImpl parentTable = this.table;
        AbstractClassMetaData parentCmd = this.acmd;
        this.table = joinTable;
        this.acmd = cmd;
        this.fetchDepth++;

        try {
            if (isQuery) {
                forceQuery.appendRelationshipQuery(this, ammd, col);
            } else {
                forceQuery.appendRelationshipFields(this, col, prefix);
            }
        } finally {
            this.table = parentTable;
            this.acmd = parentCmd;
            this.fetchDepth--;
        }
    }
    
    public StringBuilder getBuilder() {
        return sb;
    }
    
    /**
     * Checks whether the query is joined.
     * 
     * @return {@code true} if this query is being joined
     */
    public boolean isJoinQuery() {
        return isJoin;
    }

    /**
     * First looks in filter mappings cache for the filter expression associated
     * with the given alias, else returns the expression from the compilation.
     * @param alias the alias of the queried object
     * @return  the filter expression of a query
     */
    public Expression getFilterExpression(String alias) {
        return aliasToFilterMappings != null ? aliasToFilterMappings.get(alias)
                                                : isTopLevel && compilation != null ? compilation.getExprFilter() : null;
    }

    /**
     * Determines whether to skip querying for relationship fields
     * by comparing the current depth of the query to the maximum.
     *
     * @param cmd       the class with the relationship fields (either OneToMany or ManyToOne)
     * @param fieldNum  the number of the relationship field
     * @return true if the current depth of the query is greater or equal to the maximum depth we can fetch
     */
    public boolean skipRelationship(AbstractClassMetaData cmd, int fieldNum) {
        if (fetchDepth >= maxFetchDepth) return true;
        AbstractMemberMetaData ammd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(fieldNum);

        String relationshipString = cmd.getEntityName() + RELATIONSHIP_SEPARATOR + ammd.getName();
        if (queriedRelationships.contains(relationshipString)) {
            return true;
        }
        return false;
    }
    
    private void initRelatedAliases(QueryCompilation queryCompilation) {
        if (queryCompilation == null || queryCompilation.getExprFilter() == null) return;
        Map<String, Expression> ret = new HashMap<String, Expression>();
        Set<String> mappingAliases = new HashSet<String>();
        createAliasToFilterMappings(queryCompilation.getExprFilter(), ret, mappingAliases);
        if (ret.size() == 0) return;
        this.aliasToFilterMappings = ret;
        if (mappingAliases.size() == 0) return;
        
        // Now setup the relatedJoinAliases
        this.relatedJoinAliases = new HashMap<TupleName, String>();
        for (Expression fromExpr : queryCompilation.getExprFrom()) {
            for (Expression expr = fromExpr.getRight(); expr != null; expr = expr.getRight()) {
                if (expr instanceof JoinExpression && mappingAliases.contains(expr.getAlias())) {
                    List<String> t = ((JoinExpression) expr).getPrimaryExpression().getTuples();
                    relatedJoinAliases.put(new TupleName(t), expr.getAlias());
                    /**
                     * DataNucleus seems to have incorrect alias mapping for this in symbols
                     */
                    AbstractMemberMetaData mmd = acmd.getMetaDataForMember(t.get(t.size() - 1));
                    AbstractClassMetaData cmd = PersistenceUtils.getMemberElementClassMetaData(mmd,
                                                                    forceQuery.getExecutionContext().getClassLoaderResolver(),
                                                                    forceQuery.getExecutionContext().getMetaDataManager());
                    PropertySymbol ps =
                        new PropertySymbol(expr.getAlias(),
                            forceQuery.getExecutionContext().getClassLoaderResolver().classForName(cmd.getFullClassName()));
                    queryCompilation.getSymbolTable().removeSymbol(ps);
                    queryCompilation.getSymbolTable().addSymbol(ps);
                }
            }
        }
    }
    
    private String createAliasToFilterMappings(Expression expr, Map<String, Expression> map, Set<String> mappingAliases) {
        String leftAlias = null;
        String rightAlias = null;
        if (expr.getLeft() instanceof DyadicExpression) {
            leftAlias = createAliasToFilterMappings(expr.getLeft(), map, mappingAliases);
            if (expr.getRight() instanceof DyadicExpression) {
                rightAlias = createAliasToFilterMappings(expr.getRight(), map, mappingAliases);
            } else {
                rightAlias = getAlias(expr.getRight(), mappingAliases);
            }
        } else if (expr instanceof DyadicExpression) {
            leftAlias = getAliasFromDyadictExpression(expr, mappingAliases);
        } else {
            leftAlias = getAlias(expr, mappingAliases);
        }
        
        if (leftAlias != null) {
            if (rightAlias == null || leftAlias.equals(rightAlias)) {
                // Left and right are the same so current expression is a potential top level expression
                map.put(leftAlias, expr);
                return leftAlias;
            } else {
                // Left and right are not the same so we split them and toss the current expression and look further right
                map.put(leftAlias, expr.getLeft());
                map.put(rightAlias, expr.getRight());
                return rightAlias;
            }
        }
        if (rightAlias != null) {
            // Here Left alias is null but right has an alias current expression is a potential top level expression
            map.put(rightAlias, expr);
            return rightAlias;
        }
        return null;
    }
    
    private String getAliasFromDyadictExpression(Expression expr, Set<String> mappingAliases) {
        String leftAlias = getAlias(expr.getLeft(), mappingAliases);
        String rightAlias = getAlias(expr.getRight(), mappingAliases);
        if (leftAlias != null && rightAlias != null && !leftAlias.equals(rightAlias)) {
            throw new NucleusDataStoreException("There cannot be two different aliases in a leaf DyadictExpression: " + expr);
        }
        return leftAlias != null ? leftAlias : rightAlias;
    }
    
    private String getAlias(Expression expr, Set<String> mappingAliases) {
        if (expr instanceof PrimaryExpression) {
            List<String> tuples = ((PrimaryExpression) expr).getTuples();
            return tuples.size() > 1 ? tuples.get(0) : null;
        } else if (expr instanceof InvokeExpression) {
            return getAliasIfMapExpression((InvokeExpression) expr, mappingAliases);
        }
        return null;
    }
    
    private String getAliasIfMapExpression(InvokeExpression expr, Set<String> mappingAliases) {
        if ("mapValue".equals(expr.getOperation()) || "mapKey".equals(expr.getOperation())
                || "mapEntry".equals(expr.getOperation())) {
            String alias = expr.getLeft().getSymbol().getQualifiedName();
            mappingAliases.add(alias);
            return alias;
        } else {
            return getAlias(expr.getLeft(), mappingAliases);
        }
    }
}
