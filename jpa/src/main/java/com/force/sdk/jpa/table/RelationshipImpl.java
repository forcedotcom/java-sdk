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

package com.force.sdk.jpa.table;

import org.datanucleus.metadata.AbstractClassMetaData;

import com.force.sdk.jpa.query.ExpressionBuilderHelper;
import com.sforce.soap.partner.FieldType;

/**
 * 
 * Representation of a collection of related objects on a Force.com object.
 * If Object A has a lookup field to Object B and B thus has a collection of 
 * Object As, the collection is not actually represented as a field in Force.com,
 * but rather the lookup to B implies the collection. Because of this we need
 * to be able to differentiate between these types of columns.
 *
 * @author Jill Wetzler
 */
public class RelationshipImpl extends ColumnImpl {
    private final boolean isCustomRelationship;
    
    /**
     * Creates a RelationshipImpl with a given field name, such as "Opportunities"
     * on the account object, or "MyCustomObjects__r".
     * 
     * @param fieldName the name of the relationship field
     */
    public RelationshipImpl(String fieldName) {
        super(fieldName, null);
        isCustomRelationship = fieldName.endsWith(CUSTOM_RELATIONSHIP_SUFFIX);
    }
    
    /**
     * Returns whether the relationship is from a custom lookup field
     * or one that comes standard with Force.com.
     * 
     * @return {@code true} if the relationship is custom, {@code false} if standard
     */
    @Override
    public boolean isCustom() {
        return isCustomRelationship;
    }
    
    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public FieldType getType() {
        return FieldType.reference;
    }
    
    /**
     * Appends itself to the select statement of a query.
     * {@inheritDoc}
     */
    @Override
    public boolean appendSelectString(ExpressionBuilderHelper queryHelper, AbstractClassMetaData acmd,
            int fieldNum, boolean appendComma, String prefix) {
        if (queryHelper.skipRelationship(acmd, fieldNum)) return false;
        appendPrefix(queryHelper, appendComma, null); // just append comma
        queryHelper.appendRelationship(acmd, fieldNum, this, prefix, true);
        return true;
    }
    
    @Override
    public String getSelectFieldName() {
        return getForceApiRelationshipName();
    }
}
