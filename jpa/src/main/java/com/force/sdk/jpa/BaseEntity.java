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

import javax.jdo.spi.PersistenceCapable;
import javax.persistence.EntityManager;

/**
 * 
 * Utility for allowing merges on transient objects, such as objects that have not 
 * been persisted or retrieved via the {@code EntityManager}.
 *
 * @author Fiaz Hossain
 */
public final class BaseEntity {

    private BaseEntity() {  }
    
    /**
     * Initialise an object of type T to be available for a <code>merge()</code> call even if it has not
     * been persisted or retrieved. We do this by injecting a custom state manager into the object.
     * 
     * @param <T>  the type of a PersistenceCapable entity
     * @param em  the entity manager used for persisting
     * @param obj  the object being persisted
     * @return  the object that is now ready for a transient merge
     */
    public static <T> T initialiseForTransientMerge(EntityManager em, T obj) {
        if (obj instanceof PersistenceCapable) {
            initialiseForTransientMergeInternal(em, (PersistenceCapable) obj);
        }
        return obj;
    }
    
    private static void initialiseForTransientMergeInternal(EntityManager em, PersistenceCapable pc) {
        if (em != null) {
            ((ForceEntityManager) em).injectStateManagerIntoTransient(pc);
        }
    }
}
