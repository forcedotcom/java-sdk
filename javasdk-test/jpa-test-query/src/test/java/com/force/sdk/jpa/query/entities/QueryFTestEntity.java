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

package com.force.sdk.jpa.query.entities;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.*;

import com.force.sdk.jpa.annotation.CustomField;

/**
 * 
 * Entity used for testing some basic queries, focusing on expression operators.
 *
 * @author John Simone
 */
@Entity
public class QueryFTestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public String id;
    private String name;
    @Basic(fetch = FetchType.LAZY)
    private int number;
    @CustomField(label = "Type of the entity")
    private String entityType;
    @CustomField(label = "Date field", description = "This is a test for a data field")
    private Calendar date; //date
    private int checkVersion;
    
    /**
     * Test Picklist values enum.
     * 
     * @author John Simone
     */
    public enum PickValues { ONE, TWO, THREE }
    
    @Enumerated(EnumType.STRING)
    PickValues[] pickValueMulti;
    
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

    public int getNumber() {
        return number;
    }
    
    public void setNumber(int number) {
        this.number = number;
    }
    
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Calendar getDate() {
        return date;
    }
    
    public void setDate(Calendar date) {
        this.date = date;
    }
    
    public int getCheckVersion() {
        return checkVersion;
    }
    
    public void setCheckVersion(int checkVersion) {
        this.checkVersion = checkVersion;
    }
    
    @Override
    public String toString() {
        return "[" + name + ", " + number + ", " + entityType + ", " + date + ", " + checkVersion + "]";
    }
    
    /**
     * Convenience since this entity is just for testing purposes.
     */
    public static QueryFTestEntity init(String name, int number, String entityType, Date date, int checkVersion,
            PickValues[] pickValueMulti) {
        QueryFTestEntity qte = new QueryFTestEntity();
        
        qte.setName(name);
        qte.setNumber(number);
        qte.setEntityType(entityType);
        qte.setCheckVersion(checkVersion);
        qte.setPickValueMulti(pickValueMulti);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        qte.setDate(cal);
        
        return qte;
    }

    public PickValues[] getPickValueMulti() {
        return pickValueMulti;
    }

    public void setPickValueMulti(PickValues[] pickValueMulti) {
        this.pickValueMulti = pickValueMulti;
    }
}
