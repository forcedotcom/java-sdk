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

package com.force.sdk.jpa.entities.callback;

import javax.persistence.*;

/**
 * 
 * Entity for testing callbacks that exist in both the current and the parent entity.
 *
 * @author Jeff Lai
 */
@Entity
public class Boeing747 extends Airplane implements CallbackEntity {
    
    @PrePersist
    protected void prePersist2() {
        CallBackUtil.storeExecTime("prePersistBoeing747");
    }
    
    @PostPersist
    protected void postPersist2() {
        CallBackUtil.storeExecTime("postPersistBoeing747");
    }
    
    @PreRemove
    protected void preRemove2() {
        CallBackUtil.storeExecTime("preRemoveBoeing747");
    }
    
    @PostRemove
    protected void postRemove2() {
        CallBackUtil.storeExecTime("postRemoveBoeing747");
    }
    
    @PreUpdate
    protected void preUpdate2() {
        CallBackUtil.storeExecTime("preUpdateBoeing747");
    }
    
    @PostUpdate
    protected void postUpdate2() {
        CallBackUtil.storeExecTime("postUpdateBoeing747");
    }
    
    @PostLoad
    protected void postLoad2() {
        CallBackUtil.storeExecTime("postLoadBoeing747");
    }

}
