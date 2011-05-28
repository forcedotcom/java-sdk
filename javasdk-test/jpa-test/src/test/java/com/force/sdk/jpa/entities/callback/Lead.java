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

import com.force.sdk.jpa.annotation.CustomObject;

/**
 * 
 * Test callbacks with standard entity.
 *
 * @author Jeff Lai
 */
@Entity
@Table(name = "Lead")
@CustomObject(readOnlySchema = true)
public class Lead {
    
    private String website;
    private String createdDate;
    private String title;
    private String lastModifiedDate;
    private String convertedAccountId;
    private String ownerId;
    private String email;
    private String industry;
    private String systemModstamp;
    private String annualRevenue;
    private String company;
    private Boolean isUnreadByOwner;
    private String country;
    private String name;
    private String status;
    private String lastModifiedById;
    private String state;
    private String createdById;
    private String masterRecordId;
    private String phone;
    private String convertedDate;
    private String emailBouncedReason;
    private String description;
    private String firstName;
    private Boolean isConverted;
    private String lastActivityDate;
    private Boolean isDeleted;
    private String leadSource;
    private String postalCode;
    private String salutation;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private Integer numberOfEmployees;
    private String emailBouncedDate;
    private String lastName;
    private String city;
    private String street;
    private String convertedOpportunityId;
    private String rating;
    private String convertedContactId;
    public void setWebsite(String website) {
        this.website = website;
    }
    public String getWebsite() {
        return website;
    }
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    public String getCreatedDate() {
        return createdDate;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    public String getLastModifiedDate() {
        return lastModifiedDate;
    }
    public void setConvertedAccountId(String convertedAccountId) {
        this.convertedAccountId = convertedAccountId;
    }
    public String getConvertedAccountId() {
        return convertedAccountId;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    public String getOwnerId() {
        return ownerId;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
    public void setIndustry(String industry) {
        this.industry = industry;
    }
    public String getIndustry() {
        return industry;
    }
    public void setSystemModstamp(String systemModstamp) {
        this.systemModstamp = systemModstamp;
    }
    public String getSystemModstamp() {
        return systemModstamp;
    }
    public void setAnnualRevenue(String annualRevenue) {
        this.annualRevenue = annualRevenue;
    }
    public String getAnnualRevenue() {
        return annualRevenue;
    }
    public void setCompany(String company) {
        this.company = company;
    }
    public String getCompany() {
        return company;
    }
    public void setIsUnreadByOwner(Boolean isUnreadByOwner) {
        this.isUnreadByOwner = isUnreadByOwner;
    }
    public Boolean getIsUnreadByOwner() {
        return isUnreadByOwner;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public String getCountry() {
        return country;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
    public void setLastModifiedById(String lastModifiedById) {
        this.lastModifiedById = lastModifiedById;
    }
    public String getLastModifiedById() {
        return lastModifiedById;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getState() {
        return state;
    }
    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }
    public String getCreatedById() {
        return createdById;
    }
    public void setMasterRecordId(String masterRecordId) {
        this.masterRecordId = masterRecordId;
    }
    public String getMasterRecordId() {
        return masterRecordId;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getPhone() {
        return phone;
    }
    public void setConvertedDate(String convertedDate) {
        this.convertedDate = convertedDate;
    }
    public String getConvertedDate() {
        return convertedDate;
    }
    public void setEmailBouncedReason(String emailBouncedReason) {
        this.emailBouncedReason = emailBouncedReason;
    }
    public String getEmailBouncedReason() {
        return emailBouncedReason;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setIsConverted(Boolean isConverted) {
        this.isConverted = isConverted;
    }
    public Boolean getIsConverted() {
        return isConverted;
    }
    public void setLastActivityDate(String lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
    public String getLastActivityDate() {
        return lastActivityDate;
    }
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    public void setLeadSource(String leadSource) {
        this.leadSource = leadSource;
    }
    public String getLeadSource() {
        return leadSource;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }
    public String getSalutation() {
        return salutation;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
    public void setNumberOfEmployees(Integer numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }
    public Integer getNumberOfEmployees() {
        return numberOfEmployees;
    }
    public void setEmailBouncedDate(String emailBouncedDate) {
        this.emailBouncedDate = emailBouncedDate;
    }
    public String getEmailBouncedDate() {
        return emailBouncedDate;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getCity() {
        return city;
    }
    public void setStreet(String street) {
        this.street = street;
    }
    public String getStreet() {
        return street;
    }
    public void setConvertedOpportunityId(String convertedOpportunityId) {
        this.convertedOpportunityId = convertedOpportunityId;
    }
    public String getConvertedOpportunityId() {
        return convertedOpportunityId;
    }
    public void setRating(String rating) {
        this.rating = rating;
    }
    public String getRating() {
        return rating;
    }
    public void setConvertedContactId(String convertedContactId) {
        this.convertedContactId = convertedContactId;
    }
    public String getConvertedContactId() {
        return convertedContactId;
    }

    @PrePersist
    protected void prePersist() {
        CallBackUtil.storeExecTime("prePersistLead");
    }
    
    @PostPersist
    protected void postPersist() {
        CallBackUtil.storeExecTime("postPersistLead");
    }
    
    @PreRemove
    protected void preRemove() {
        CallBackUtil.storeExecTime("preRemoveLead");
    }
    
    @PostRemove
    protected void postRemove() {
        CallBackUtil.storeExecTime("postRemoveLead");
    }
    
    @PreUpdate
    protected void preUpdate() {
        CallBackUtil.storeExecTime("preUpdateLead");
    }
    
    @PostUpdate
    protected void postUpdate() {
        CallBackUtil.storeExecTime("postUpdateLead");
    }
    
    @PostLoad
    protected void postLoad() {
        CallBackUtil.storeExecTime("postLoadLead");
    }
    
    
}
