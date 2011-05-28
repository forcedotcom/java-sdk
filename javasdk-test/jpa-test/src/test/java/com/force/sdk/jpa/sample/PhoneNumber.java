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

package com.force.sdk.jpa.sample;

import java.io.Serializable;

import javax.persistence.*;

import com.force.sdk.jpa.annotation.CustomField;
import com.sforce.soap.metadata.FieldType;

/**
 * 
 * PhoneNumber entity.
 *
 * @author Dirk Hain
 */
@Entity
@Table(name = "PHONE__C")
public class PhoneNumber implements Serializable {
    
    private String id;
    private String number;
    private Phonetype phoneType;
    public Employee employee; //lookup
    public Employee employeeMD; //standard master detail
    
    /**
     * Phone type enum.
     * 
     * @author Dirk Hain
     */
    public enum Phonetype { HOME, OFFICE, CELL }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "PNUM")
    @CustomField(type = FieldType.Phone)
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Column(name = "PTYPE")
    @Enumerated(EnumType.STRING)
    public Phonetype getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(Phonetype phoneType) {
        this.phoneType = phoneType;
    }
    
    @ManyToOne
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    @ManyToOne
    @CustomField(type = FieldType.MasterDetail, childRelationshipName = "EmployeePhonenumbersMD", name = "EmployeeMD")
    public Employee getEmployeeMD() {
        return employeeMD;
    }
    
    public void setEmployeeMD(Employee emp) {
        this.employeeMD = emp;
    }
    
    public static PhoneNumber init(Employee emp, String number, Phonetype type) {
        PhoneNumber num = new PhoneNumber();
        num.setNumber(number);
        num.setPhoneType(type);
        num.setEmployee(emp);
        num.setEmployeeMD(emp);
        return num;
    }
}
