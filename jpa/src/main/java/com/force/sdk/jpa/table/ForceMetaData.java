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

import java.util.Map;

import org.datanucleus.metadata.AbstractClassMetaData;

import com.force.sdk.jpa.PersistenceUtils;
import com.force.sdk.jpa.schema.ForceAsyncResultProcessor;
import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.metadata.TreatBlanksAs;

/**
 * 
 * Base class for the metadata of objects and fields that will be registered
 * and optionally passed to the schema writer.
 *
 * @author Fiaz Hossain
 */
public abstract class ForceMetaData extends ForceAsyncResultProcessor {
    
    protected CustomObject customObject;
    protected TableImpl tableImpl;
    protected final AbstractClassMetaData cmd;
    protected final boolean isReadOnlyTable;
    protected final boolean isReadOnlyFields;
    
    /**
     * Create a metadata object so we can cache information about an entity.
     * 
     * @param cmd  the DataNucleus metadata object
     * @param tableImpl  our own Table object
     */
    public ForceMetaData(AbstractClassMetaData cmd, TableImpl tableImpl) {
        this.cmd = cmd;
        this.tableImpl = tableImpl;
        this.isReadOnlyFields = PersistenceUtils.isReadOnlySchema(cmd, false);
        this.isReadOnlyTable = isReadOnlyFields ? true : PersistenceUtils.isReadOnlySchema(cmd, true);
    }

    /**
     * Remove the '__c' notation if it exists.
     * 
     * @param name an object name
     * @return  the object name without '__c'
     */
    protected String removeCustomThingSuffix(String name) {
        return name.toLowerCase().endsWith(ColumnImpl.CUSTOM_THING_SUFFIX)
                ? name.substring(0, name.length() - ColumnImpl.CUSTOM_THING_SUFFIX.length()) : name;
    }
    
    protected TreatBlanksAs getTreatBlanksAsFromForceAnnotation(Map<String, String> extensions) {
        String value = extensions.get("treatBlanksAs");
        return value != null ? TreatBlanksAs.valueOf(value) : null;
    }
    
    protected Integer getIntegerFromForceAnnotation(Map<String, String> extensions, String key) {
        String value = extensions.get(key);
        return value != null ? Integer.valueOf(value) : null;
    }
    
    protected boolean getBooleanFromForceAnnotation(Map<String, String> extensions, String key) {
        String value = extensions.get(key);
        return value != null ? Boolean.valueOf(value) : false;
    }
    
    /**
     * Only fill out the basic info for this customObject.  If we're just adding fields to an existing object (standard or custom)
     * right now all we want to set is the name.  I assume there will be more things to set later.
     * 
     */
    protected void createCustomObjectStub() {
        String apiName = tableImpl.getTableName().getForceApiName();
        customObject = new CustomObject();
        customObject.setFullName(apiName);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating custom object: " + apiName);
        }
    }
    
    public boolean getIsReadOnlyTable() {
        return this.isReadOnlyTable;
    }
    
    public TableImpl getTableImpl() {
        return tableImpl;
    }
}
