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

package com.force.sdk.jpa.entities.cascade;

import java.util.*;

import javax.persistence.*;

import com.force.sdk.jpa.annotation.CustomField;

/**
 * 
 * Entity for testing cascade types.
 *
 * @author Jeff Lai
 */
@Entity
public class CascadeNoneColMDParentTestEntity implements CascadeParentTestEntity2<Collection<CascadeNoneColMDChildTestEntity>> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    
    private String name;
    
    @Column(name = "CascadeParentTestEntity")
    @OneToMany(mappedBy = "parent")
    private Collection<CascadeNoneColMDChildTestEntity> children;
    
    @CustomField(externalId = true)
    private String extIdField;
    
    @Version
    private Calendar lastModifiedDate;
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void addChild(CascadeChildTestEntity child) {
        if (children == null) {
            children = new ArrayList<CascadeNoneColMDChildTestEntity>();
        }
        children.add((CascadeNoneColMDChildTestEntity) child);
    }
    
    @Override
    public void removeChild(CascadeChildTestEntity child) {
        children.remove(child);
    }
    
    @Override
    public Collection<CascadeNoneColMDChildTestEntity> getChildren() {
        return children;
    }
    
    @Override
    public void setChildren(Collection<CascadeNoneColMDChildTestEntity> children) {
        this.children = children;
    }
    
    @Override
    public int getChildrenSize() {
        return getChildren().size();
    }
    
    @Override
    public String getExtIdField() {
        return extIdField;
    }
    
    @Override
    public void setExtIdField(String extIdField) {
        this.extIdField = extIdField;
    }
    
    @Override
    public Calendar getLastModifiedDate() {
        return this.lastModifiedDate;
    }
    
    @Override
    public void setLastModifiedDate(Calendar lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
