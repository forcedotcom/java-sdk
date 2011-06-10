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

package com.force.sdk.jpa.entities;

import javax.persistence.*;
import java.net.URL;
import java.util.List;

/**
 * This entity is used for testing merge etc. with lazy and eager attributes and relationships.
 *    
 * @author Nawab Iqbal
 */
@Entity
public class PersonEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    
    private String name;
    
    @Basic(fetch = FetchType.LAZY)
    private String type;
 
    @Basic(fetch = FetchType.LAZY)
    public URL lazyURL;

    @Basic(fetch = FetchType.EAGER)
    public URL eagerURL;

    @OneToMany(mappedBy = "phoneOwner",  fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PhoneEntity> phoneList;

    @OneToMany(mappedBy = "secondOwner", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PhoneEntity> morePhonesEager;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URL getLazyURL() {
        return lazyURL;
    }

    public void setLazyURL(URL url) {
        this.lazyURL = url;
    }

    public URL getEagerURL() {
        return eagerURL;
    }

    public void setEagerURL(URL url) {
        this.eagerURL = url;
    }

    public List<PhoneEntity> getPhoneList() {
        return phoneList;
    }

    public void setPhoneList(List<PhoneEntity> phoneList) {
        this.phoneList = phoneList;
    }

    public List<PhoneEntity> getMorePhonesEager() {
        return morePhonesEager;
    }

    public void setMorePhonesEager(List<PhoneEntity> morePhonesEager) {
        this.morePhonesEager = morePhonesEager;
    }
}
