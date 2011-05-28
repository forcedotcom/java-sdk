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

import java.util.ArrayList;
import java.util.List;

import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.*;
import org.datanucleus.metadata.QueryResultMetaData.PersistentTypeMapping;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.Type;
import org.datanucleus.store.query.AbstractJavaQuery;

import com.force.sdk.jpa.ForceManagedConnection;
import com.force.sdk.jpa.ForceStoreManager;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;

/**
 * ResultObjectFactory that operates using a QueryResultMetaData and returns objects based on the definition.
 * A QueryResultMetaData allows for a row of a ResultSet to be returned as a mix of :-
 * <ul>
 * <li>a number of persistent objects (made up of several ResultSet columns)</li>
 * <li>a number of Objects (from individual ResultSet columns)</li>
 * </ul>
 * Each call to getObject() will then return a set of objects as per the MetaData definition.
 * <h3>ResultSet to object mapping</h3>
 * Each row of the SObject has a set of columns, and these columns are either used for direct outputting
 * back to the user as a "simple" object, or as a field in a persistent object.
 * 
 * @author Fiaz Hossain
 */
public class ResultMetaDataROF {
    
    /** MetaData defining the result from the Query. */
    QueryResultMetaData queryResultMetaData = null;

    /**
     * Constructor.
     * @param qrmd MetaData defining the results from the query.
     */
    public ResultMetaDataROF(QueryResultMetaData qrmd) {
        this.queryResultMetaData = qrmd;
    }

    /**
     * @param ec ExecutionContext
     * @param mconn ForceManagedConnection
     * @param query AbstractJavaQuery
     * @param sobject SObject
     * @return The object(s) for this row of the SObject. We return persistent object first followed by single columns
     * in the order they are described.
     */
    public Object getObject(final ExecutionContext ec, final ForceManagedConnection mconn,
            final AbstractJavaQuery query, SObject sobject) {
        
        List returnObjects = new ArrayList();

        // A). Process persistent types
        PersistentTypeMapping[] persistentTypes = queryResultMetaData.getPersistentTypeMappings();
        ForceStoreManager storeMgr = (ForceStoreManager) ec.getStoreManager();
        if (persistentTypes != null) {
            for (int i = 0; i < persistentTypes.length; i++) {
                AbstractClassMetaData acmd =
                    ec.getMetaDataManager().getMetaDataForClass(persistentTypes[i].getClassName(), ec.getClassLoaderResolver());
                returnObjects.add(ec.findObjectUsingAID(
                        new Type(ec.getClassLoaderResolver().classForName(acmd.getFullClassName())),
                                    ForceQueryUtils.getFieldValues2(acmd, acmd.getDFGMemberPositions(),
                                                                    mconn, storeMgr, sobject, query),
                                    query.getIgnoreCache(), true));
            }
        }

        // B). Process simple columns
        String[] columns = queryResultMetaData.getScalarColumns();
        if (columns != null) {
            for (int i = 0; i < columns.length; i++) {
                Object obj = getResultObject(sobject, columns[i]);
                if (obj == null) {
                    throw new NucleusUserException("No data found for column: " + columns[i]);
                }
                returnObjects.add(obj);
            }
        }

        if (returnObjects.size() == 0) {
            // No objects so user must have supplied incorrect MetaData
            return null;
        } else if (returnObjects.size() == 1) {
            // Return Object
            return returnObjects.get(0);
        } else {
            // Return Object[]
            return returnObjects.toArray(new Object[returnObjects.size()]);
        }
    }

    /**
     * Convenience method to read the value of a column out of an SObject.
     * @param rs XmlObject
     * @param columnName Name of the column
     * @return Value for the column for this row.
     */
    private Object getResultObject(final XmlObject rs, String columnName) {
        if (rs == null || columnName == null) return null;
        int index = columnName.indexOf('.');
        if (index > 0) {
            return getResultObject(rs.getChild(columnName.substring(0, index)), columnName.substring(index + 1));
        }
        return rs.getField(columnName);
    }
}
