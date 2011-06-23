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

import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.InheritanceStrategy;

import com.force.sdk.jpa.PersistenceUtils;

/**
 * 
 * Force.com objects have names that consist of a namespace, the base name of the object,
 * and a suffix (like __c).  This class manages all of these elements.
 *
 * @author Fiaz Hossain
 */
public final class TableName {

    private static final String NAME_SEPARATOR = "__";
    
    /**
     * Enum of all possible table types.
     */
    public enum Type {
        /**
         * Standard Force.com table.
         */
        Standard(""),
        /**
         * Custom Force.com table.
         */
        Custom("c"),
        /**
         * Custom Force.com relationship.
         */
        CustomRelationship("r"),
        /**
         * Custom Force.com component.
         */
        CustomComponent("s"),
        /**
         * Force.com knowledge article.
         */
        KnowledgeArticle("ka"),
        /**
         * Force.com knowledge article version.
         */
        KnowledgeArticleVersion("kav"),
        /**
         * Force.com Chatter feed.
         */
        Feed("feed");
        
        private String suffix;
        
        Type(String suffix) {
            this.suffix = suffix;
        }
        
        String getSuffix() {
            return suffix;
        }
        
        static Type parse(String suffix) {
            switch(suffix.length()) {
            case 1:
                if (suffix.equals(Custom.suffix)) return Custom;
                else if (suffix.equals(CustomRelationship.suffix)) return CustomRelationship;
                else if (suffix.equals(CustomComponent.suffix)) return CustomComponent;
                break;
            case 2:
                if (suffix.equals(KnowledgeArticle.suffix)) return KnowledgeArticle;
                break;
            default:
                if (suffix.equals(KnowledgeArticleVersion.suffix)) return KnowledgeArticleVersion;
                if (suffix.equals(Feed.suffix)) return Feed;
                break;
            }
            throw new NucleusUserException("Unsupported custom object suffix: " + suffix);
        }
    }
    
    private final String name; // always the short name
    private final boolean isCustom;
    private final Type type;
    private final String namespace;

    /**
     * 
     * Creates a TableName object with all the components necessary for Force.com
     * API access, including the name of the object, the namespace, and the suffix
     * if needed.
     * 
     * @param namespace in most cases this will be the namespace of the organization holding the API connection
     * @param acmd  the metadata of the entity
     * @return  The name to use for Force.com API access
     */
    public static TableName createTableName(String namespace, AbstractClassMetaData acmd) {
        String tableName = null;
        if (acmd.getTable() != null) {
            tableName = acmd.getTable();
        }
        if (tableName == null) {
            if (acmd.getInheritanceMetaData() != null
                    && acmd.getInheritanceMetaData().getStrategy() == InheritanceStrategy.SUPERCLASS_TABLE) {
                AbstractClassMetaData sacmd;
                while ((sacmd = acmd.getSuperAbstractClassMetaData()) != null) {
                    acmd = sacmd;
                    if (acmd.getTable() != null) {
                        tableName = acmd.getTable();
                        break;
                    }
                }
            }
        }
        if (tableName != null) {
            return new TableName(namespace, tableName, true);
        } else {
            return new TableName(namespace, PersistenceUtils.getEntityName(acmd), false);
        }
    }
    
    /**
     * Creates a TableName object with the option to parse the full name to determine the suffix.
     * 
     * @param namespace  the namespace of the org, for defaulting purposes if the fullname
     *                   does not contain a namespace
     * @param fullName  the full name of an object, possibly includes a suffix and namespace
     * @param parseForSuffix  whether we should parse the fullname or take the elements as is
     */
    private TableName(String namespace, String fullName, boolean parseForSuffix) {
        if (parseForSuffix) {
            String[] tokens = fullName.split(NAME_SEPARATOR); //either namespace__entity__suffix or entity__suffix
            if (tokens.length > 3) {
                throw new NucleusUserException("Could not parse table: " + fullName);
            }
            
            switch (tokens.length) {
                case 1:
                    this.type = Type.Standard;
                    this.name = fullName;
                    this.namespace = namespace;
                    break;
                case 2: //name__suffix
                    String suffix = tokens[1].toLowerCase();
                    this.type = Type.parse(suffix);
                    this.name = tokens[0];
                    this.namespace = namespace;
                    break;
                case 3: //namespace__name__suffix
                default:
                    String suf = tokens[2].toLowerCase();
                    this.type = Type.parse(suf);
                    this.namespace = tokens[0];
                    this.name = tokens[1];
                    break;
            }
        } else {
            this.type = Type.Custom;
            this.name = fullName;
            this.namespace = namespace;
        }
        this.isCustom = this.type != Type.Standard;
    }
    
    public String getName() {
        return name;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    /**
     * Returns fully qualified Force.com API name for this {@code TableName}.
     * 
     * @return fully qualified Force.com API name
     */
    public String getForceApiName() {
        if (isCustom) {
            return PersistenceUtils.prependNamespace(namespace, String.format("%s__%s", name, type.suffix));
        } else {
            return name;
        }
    }
    
    /**
     * Returns whether an object is user created or standard in Force.com.
     * 
     * @return {@code true} if this object is custom
     */
    public boolean isCustom() {
        return isCustom;
    }
    
    public Type getType() {
        return type;
    }
        
    @Override
    public String toString() {
        return getName();
    }
}
