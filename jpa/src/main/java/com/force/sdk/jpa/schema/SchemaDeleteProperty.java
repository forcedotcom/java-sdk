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

package com.force.sdk.jpa.schema;

/**
 * 
 * This class tells the schema process how to handle schema deletion. Via persistence.xml or through other schema tools
 * a user can delete the schema in their org, and purge it from the recycle bin if necessary
 *
 * @author Jill Wetzler
 */
public class SchemaDeleteProperty {
    
    private boolean deleteSchema;
    private boolean purgeSchemaOnDelete;
    
    /**
     * Create a delete property that signifies whether we want to delete the schema objects by sending them to the recycle bin
     * or whether we want to bypass the recycle bin and do a hard delete. Note that purgeSchemaOnDelete will be ignored unless
     * deleteSchema is true.
     * 
     * @param deleteSchema  whether the schema for this application should be deleted  
     * @param purgeSchemaOnDelete  if purgeSchemaOnDelete is true, schema objects will bypass the recycle bin and be hard deleted,
     *                             if false, schema objects will be placed in the recycle bin
     */
    public SchemaDeleteProperty(boolean deleteSchema, boolean purgeSchemaOnDelete) {
        this.deleteSchema = deleteSchema;
        if (deleteSchema) {
            this.purgeSchemaOnDelete = purgeSchemaOnDelete;
        }
    }
    
    /** 
     * @return true if schema should be deleted, optionally skip recycle bin by purgeOnDelete 
     */
    public boolean getDeleteSchema() {
        return deleteSchema;
    }
    
    /**
     * @return true if getDeleteSchema() is true and schema should bypass the recycle bin 
     */
    public boolean getPurgeSchemaOnDelete() {
        return purgeSchemaOnDelete;
    }

}
