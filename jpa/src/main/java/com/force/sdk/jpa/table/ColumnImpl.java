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
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.FieldType;

/**
 * 
 * Representation of a Force.com field.
 *
 * @author Fiaz Hossain
 */
public class ColumnImpl {
    
    /**
     * Suffix for Force.com custom relationship.
     */
    public static final String CUSTOM_RELATIONSHIP_SUFFIX = "__r";
    
    /**
     * Suffix for Force.com custom object.
     */
    public static final String CUSTOM_THING_SUFFIX = "__c";
    
    protected final String fieldName;
    private final Field field;
    
    /**
     * Creates a columnImpl with the name of a field as it would appear in the Force.com SOAP API. 
     * 
     * @param fieldName  the name of a field as it would appear in the Force.com SOAP API
     * @param field the {@link Field} object for this column
     */
    public ColumnImpl(String fieldName, Field field) {
        this.fieldName = fieldName;
        this.field = field;
    }

    public String getForceApiName() {
        return fieldName;
    }
    
    public String getForceApiRelationshipName() {
        return field != null  ? field.getRelationshipName() : fieldName;
    }

    public String getFieldName() {
        return field != null ? field.getName() : fieldName;
    }

    /**
     * Returns the Force.com API field type for this column.
     * 
     * @return Force.com API field type
     */
    public FieldType getType() {
        if (field == null) return null;
        
        FieldType type = field.getType();
        
        if (type == FieldType._double && field.getScale() == 0) {
        type = FieldType._int;
        }
        
        return type;
    }
    
    public Field getField() {
        return field;
    }

    /**
     * Distinguishes whether a field has been created by a user in this organization (custom) or whether it is
     * standard in a Force.com product.
     * 
     * @return {@code true} if the field is a custom field
     */
    public boolean isCustom() {
        return field != null && field.isCustom();
    }

    /**
     * Appends a given prefix during the building of a SOQL query -- used for relationships.
     * 
     * @param queryHelper  the expression builder that is currently in progress
     * @param appendComma  whether to append a comma before the prefix
     * @param prefix  the prefix to use
     */
    protected void appendPrefix(ExpressionBuilderHelper queryHelper, boolean appendComma, String prefix) {
        if (appendComma) queryHelper.getBuilder().append(", ");
        if (prefix != null) queryHelper.getBuilder().append(prefix);
    }
    
    /**
     * 
     * Appends an in-progress SOQL select string with the right name for this field.
     * 
     * @param queryHelper  the expression builder that is currently in progress
     * @param acmd the class metadata for the entity this field belongs to
     * @param fieldNum the number of this field in DataNucleus
     * @param appendComma whether to append a comma before the field
     * @param prefix  the prefix to prepend, if necessary 
     * @return true if something was appended to the query
     */
    public boolean appendSelectString(ExpressionBuilderHelper queryHelper, AbstractClassMetaData acmd,
            int fieldNum, boolean appendComma, String prefix)  {
        if (field != null && field.getType() == FieldType.reference && !queryHelper.isJoinQuery()) {
            if (queryHelper.skipRelationship()) return false;
            appendPrefix(queryHelper, appendComma, null); // just append comma
            queryHelper.appendRelationship(acmd, fieldNum, this, prefix, false);
        } else {
            appendPrefix(queryHelper, appendComma, prefix);
            queryHelper.getBuilder().append(getFieldName());
        }
        return true;
    }
    
    public String getSelectFieldName() {
        return field != null && field.getType() == FieldType.reference ? getForceApiRelationshipName() : getFieldName();
    }
    
    @Override
    public String toString() {
        return getFieldName();
    }
}
