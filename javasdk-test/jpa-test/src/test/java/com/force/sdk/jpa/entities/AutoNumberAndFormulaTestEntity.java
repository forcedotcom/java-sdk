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

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

import com.force.sdk.jpa.annotation.CustomField;
import com.sforce.soap.metadata.FieldType;

/**
 * 
 * Entity for testing read only autonumber and formula fields.
 *
 * @author Fiaz Hossain
 */
@Entity
public class AutoNumberAndFormulaTestEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String name;
    
    @CustomField(type = FieldType.AutoNumber, startValue = 100)
    private long autoNum1;
    
    @CustomField(type = FieldType.AutoNumber, name = "AUTO_NUM_2")
    private long autoNum2;
    
    @CustomField(type = FieldType.AutoNumber)
    @Column(name = "AUTO_NUM_3")
    private long autoNum3;
        
    @CustomField(formula = "name & autoNum1__c")
    private String nameWithNumber;
    
    @CustomField(formula = "TODAY() + 24 * 3600")
    private Date tomorrow;
    
    @CustomField(formula = "VALUE(autoNum1__c) + 1")
    private int nextAutoNum1;
    
    @CustomField(formula = "IF (MOD(VALUE(autoNum1__c ),  2) = 0,  50.50, 100.10)")
    private BigDecimal nextBonus;
    
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
    
    public long getAutoNum1() {
        return autoNum1;
    }
    
    public void setAutoNum1(long autoNum1) {
        this.autoNum1 = autoNum1;
    }
    
    public long getAutoNum2() {
        return autoNum2;
    }
    
    public void setAutoNum2(long autoNum2) {
        this.autoNum2 = autoNum2;
    }
    
    public long getAutoNum3() {
        return autoNum3;
    }
    
    public void setAutoNum3(long autoNum3) {
        this.autoNum3 = autoNum3;
    }
    
    public String getNameWithNumber() {
        return nameWithNumber;
    }
    
    public void setNameWithNumber(String nameWithNumber) {
        this.nameWithNumber = nameWithNumber;
    }
    
    public Date getTomorrow() {
        return tomorrow;
    }
    
    public void setTomorrow(Date tomorrow) {
        this.tomorrow = tomorrow;
    }
    
    public int getNextAutoNum1() {
        return nextAutoNum1;
    }
    
    public void setNextAutoNum1(int nextAutoNum1) {
        this.nextAutoNum1 = nextAutoNum1;
    }
    
    public BigDecimal getNextBonus() {
        return nextBonus;
    }
    
    public void setNextBonus(BigDecimal nextBonus) {
        this.nextBonus = nextBonus;
    }
}
