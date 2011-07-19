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

package com.force.sdk.jpa;

import org.datanucleus.ObjectManager;
import org.datanucleus.jpa.state.JPAStateManagerImpl;
import org.datanucleus.metadata.AbstractClassMetaData;

/**
 * Used as a marker to distinguish from JPAStateManagerImpl. The logic is used for
 * {@code merge()} calls to transient objects.
 *
 * @author Fiaz Hossain
 */
public class ForceJPAStateManagerImpl extends JPAStateManagerImpl {

    /**
     * Create a special state manager to use for {@code merge()} calls on transient objects.
     * 
     * @param om the object manager
     * @param cmd the metadata for an entity class
     */
    public ForceJPAStateManagerImpl(ObjectManager om, AbstractClassMetaData cmd) {
        super(om, cmd);
    }
    
    /**
     * Relationship checks fail with NPE because this StateManager is created on transient objects. We override the
     * method here to bypass the super class implementation
     */
    @Override
    public void checkManagedRelations() {
        // no-op
    }
    
    /**
     * Relationship checks fail with NPE because this StateManager is created on transient objects. We override the
     * method here to bypass the super class implementation
     */
    @Override
    public void processManagedRelations() {
        // no-op
    }
    
    /**
     * Relationship checks fail with NPE because this StateManager is created on transient objects. We override the
     * method here to bypass the super class implementation
     */
    @Override
    public void clearManagedRelations() {
        // no-op
    }
}
