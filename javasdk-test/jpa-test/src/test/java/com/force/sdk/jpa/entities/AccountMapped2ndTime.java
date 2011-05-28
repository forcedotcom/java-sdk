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

import com.force.sdk.jpa.annotation.CustomObject;

/**
 * 
 * Test multiple entities mapped to the same table.
 *
 * @author Jeff Lai
 */
@Entity(name = "Account")
@Table(name = "Account")
@CustomObject(readOnlySchema = true)
public class AccountMapped2ndTime {

        private String type;
        private String description;
        private String lastModifiedById;
        private String shippingCity;
        private String billingPostalCode;
        private String annualRevenue;
        private String systemModstamp;
        private String shippingStreet;
        private Integer numberOfEmployees;
        private String billingCountry;
        private String fax;
        private String lastActivityDate;
        private String website;
        private String ownerId;
        private String parentId;
        private String shippingCountry;
        private String createdDate;
        private String phone;
        private String shippingPostalCode;
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private String id;
        private String industry;
        private String billingStreet;
        private String billingState;
        private String billingCity;
        private String name;
        private String createdById;
        private String masterRecordId;
        private Boolean isDeleted;
        private String lastModifiedDate;
        private String shippingState;
        public void setType(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getDescription() {
            return description;
        }
        public void setLastModifiedById(String lastModifiedById) {
            this.lastModifiedById = lastModifiedById;
        }
        public String getLastModifiedById() {
            return lastModifiedById;
        }
        public void setShippingCity(String shippingCity) {
            this.shippingCity = shippingCity;
        }
        public String getShippingCity() {
            return shippingCity;
        }
        public void setBillingPostalCode(String billingPostalCode) {
            this.billingPostalCode = billingPostalCode;
        }
        public String getBillingPostalCode() {
            return billingPostalCode;
        }
        public void setAnnualRevenue(String annualRevenue) {
            this.annualRevenue = annualRevenue;
        }
        public String getAnnualRevenue() {
            return annualRevenue;
        }
        public void setSystemModstamp(String systemModstamp) {
            this.systemModstamp = systemModstamp;
        }
        public String getSystemModstamp() {
            return systemModstamp;
        }
        public void setShippingStreet(String shippingStreet) {
            this.shippingStreet = shippingStreet;
        }
        public String getShippingStreet() {
            return shippingStreet;
        }
        public void setNumberOfEmployees(Integer numberOfEmployees) {
            this.numberOfEmployees = numberOfEmployees;
        }
        public Integer getNumberOfEmployees() {
            return numberOfEmployees;
        }
        public void setBillingCountry(String billingCountry) {
            this.billingCountry = billingCountry;
        }
        public String getBillingCountry() {
            return billingCountry;
        }
        public void setFax(String fax) {
            this.fax = fax;
        }
        public String getFax() {
            return fax;
        }
        public void setLastActivityDate(String lastActivityDate) {
            this.lastActivityDate = lastActivityDate;
        }
        public String getLastActivityDate() {
            return lastActivityDate;
        }
        public void setWebsite(String website) {
            this.website = website;
        }
        public String getWebsite() {
            return website;
        }
        public void setOwnerId(String ownerId) {
            this.ownerId = ownerId;
        }
        public String getOwnerId() {
            return ownerId;
        }
        public void setParentId(String parentId) {
            this.parentId = parentId;
        }
        public String getParentId() {
            return parentId;
        }
        public void setShippingCountry(String shippingCountry) {
            this.shippingCountry = shippingCountry;
        }
        public String getShippingCountry() {
            return shippingCountry;
        }
        public void setCreatedDate(String createdDate) {
            this.createdDate = createdDate;
        }
        public String getCreatedDate() {
            return createdDate;
        }
        public void setPhone(String phone) {
            this.phone = phone;
        }
        public String getPhone() {
            return phone;
        }
        public void setShippingPostalCode(String shippingPostalCode) {
            this.shippingPostalCode = shippingPostalCode;
        }
        public String getShippingPostalCode() {
            return shippingPostalCode;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getId() {
            return id;
        }
        public void setIndustry(String industry) {
            this.industry = industry;
        }
        public String getIndustry() {
            return industry;
        }
        public void setBillingStreet(String billingStreet) {
            this.billingStreet = billingStreet;
        }
        public String getBillingStreet() {
            return billingStreet;
        }
        public void setBillingState(String billingState) {
            this.billingState = billingState;
        }
        public String getBillingState() {
            return billingState;
        }
        public void setBillingCity(String billingCity) {
            this.billingCity = billingCity;
        }
        public String getBillingCity() {
            return billingCity;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
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
        public void setIsDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
        }
        public Boolean getIsDeleted() {
            return isDeleted;
        }
        public void setLastModifiedDate(String lastModifiedDate) {
            this.lastModifiedDate = lastModifiedDate;
        }
        public String getLastModifiedDate() {
            return lastModifiedDate;
        }
        public void setShippingState(String shippingState) {
            this.shippingState = shippingState;
        }
        public String getShippingState() {
            return shippingState;
        }
    }
