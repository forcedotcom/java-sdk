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

import java.util.Map;

import org.datanucleus.query.compiler.JPQLParser;
import org.datanucleus.query.compiler.QueryCompilation;
import org.datanucleus.query.expression.Expression;
import org.datanucleus.query.expression.ExpressionCompiler;
import org.datanucleus.query.node.Node;
import org.datanucleus.query.symbol.SymbolTable;
import org.datanucleus.store.ExecutionContext;

/**
 * 
 * Utility class that compiles JPQL query fragments into query expressions.
 * This is mostly intended to take query expressions from annotations and convert them
 * into expressions that can be appended to existing queries.
 *
 * @author Fiaz Hossain
 */
public class JPQLPartialCompiler {

    private JPQLParser parser;
    private SymbolTable symbolTable;
    
    /**
     * Instantiates a JPQLParser and stores the symbol table from the compilation, or 
     * creates a new one.
     * 
     * @param ec  the current execution context
     * @param compilation  possibly {@code null}, contains components of a query string
     * @param options  JPQL parser options
     * @param params  parameters for the parser
     */
    public JPQLPartialCompiler(ExecutionContext ec, QueryCompilation compilation, Map options, Map params) {
        parser = new JPQLParser(options, params);
        this.symbolTable = compilation != null ? compilation.getSymbolTable() : new SymbolTable(ec.getClassLoaderResolver());
    }
    
    /**
     * 
     * Adds the proper ordering to the parser and returns the resulting expressions.
     * 
     * @param ordering  the orderBy string
     * @return  an ordered expression array
     */
    public Expression[] compileOrdering(String ordering) {
        if (ordering == null) {
            return null;
        }

        Node[] node = parser.parseOrder(ordering);
        Expression[] expr = new Expression[node.length];
        for (int i = 0; i < node.length; i++) {
            ExpressionCompiler comp = new ExpressionCompiler();
            comp.setSymbolTable(symbolTable);
            expr[i] = comp.compileOrderExpression(node[i]);
            expr[i].bind(symbolTable);
        }
        return expr;
    }
}
