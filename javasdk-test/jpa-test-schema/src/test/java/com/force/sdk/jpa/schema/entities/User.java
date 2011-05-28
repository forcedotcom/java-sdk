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

package com.force.sdk.jpa.schema.entities;

import javax.persistence.*;

import com.force.sdk.jpa.annotation.CustomField;
import com.sforce.soap.metadata.FieldType;

/**
 * 
 * This standard user entity is not used inside any tests but is required 
 * for the definition of {@link StandardFieldLinkingEntity} which defines
 * fields of type user which we verify get mapped to the standard fields.
 *
 * @author Dirk Hain
 */
@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String id;
    
    String name;
    
    String lastName;

    String alias;
    
    String username;
    
    @CustomField(type = FieldType.Email)
    String email;

    String profileId;

    /**
     * The fields below cannot be loaded as owner.<fieldname> in a relationship. We make them lazy so that if the
     * user needs them they can query the whole User object
     */
    @Basic(fetch = FetchType.LAZY)
    public String newCustomField;
    
    @Basic(fetch = FetchType.LAZY)
    String timeZoneSidKey;
    
    @Basic(fetch = FetchType.LAZY)
    String localeSidKey;
    
    @Basic(fetch = FetchType.LAZY)
    String emailEncodingKey;

    @Basic(fetch = FetchType.LAZY)
    String languageLocaleKey;

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public void setTimeZoneSidKey(String timeZoneSidKey) {
        this.timeZoneSidKey = timeZoneSidKey;
    }

    public void setLocaleSidKey(String localeSidKey) {
        this.localeSidKey = localeSidKey;
    }

    public void setEmailEncodingKey(String emailEncodingKey) {
        this.emailEncodingKey = emailEncodingKey;
    }

    public void setLanguageLocaleKey(String languageLocaleKey) {
        this.languageLocaleKey = languageLocaleKey;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAlias() {
        return alias;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getTimeZoneSidKey() {
        return timeZoneSidKey;
    }

    public String getLocaleSidKey() {
        return localeSidKey;
    }

    public String getEmailEncodingKey() {
        return emailEncodingKey;
    }

    public String getLanguageLocaleKey() {
        return languageLocaleKey;
    }
    
}
