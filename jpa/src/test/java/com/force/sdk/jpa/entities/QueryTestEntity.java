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

import java.util.Calendar;

import javax.persistence.*;

import com.force.sdk.jpa.mock.MockApiEntity;
import com.force.sdk.jpa.mock.MockApiField;
import com.sforce.soap.partner.FieldType;

/**
 * Simple test entity used for 
 * Force.com JPA query tests.
 *
 * @author Dirk Hain
 * @author Tim Kral
 */
@Entity
@SqlResultSetMapping(name = "QueryTestMapping", columns = { @ColumnResult(name = "Name") },
        entities = { @EntityResult(entityClass = QueryTestEntity.class) })
@NamedNativeQueries({
    @NamedNativeQuery(name = "QueryNativeBasic", query =
        "select id, name, entityType__c, number__c from QueryTestEntity__c\n"
        + "where entityType__c not in ('AAA') order by entityType__c limit 3",
        resultClass = QueryTestEntity.class),
    @NamedNativeQuery(name = "QueryNativeWithResultSetMapping", query =
        "select id, name, entityType__c, number__c from QueryTestEntity__c",
        resultSetMapping = "QueryTestMapping")
})
@NamedQueries({
    @NamedQuery(name = "BasicJPQLQuery", query =
        "select o from QueryTestEntity o where o.name='five'"
        )
})
@MockApiEntity
public class QueryTestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @MockApiField(name = "Id", type = FieldType.id, custom = false)
    public String id;
    
    @MockApiField(name = "Name", type = FieldType.string, custom = false)
    private String name;
    
    @MockApiField(name = "number__c", type = FieldType._double, custom = true)
    private int number;
    
    @MockApiField(name = "entityType__c", type = FieldType.string, custom = true)
    private String entityType;
    
    @MockApiField(name = "date__c", type = FieldType.datetime, custom = true)
    private Calendar date;
    
    /**
     * Test Picklist values enum.
     * 
     * @author Dirk Hain
     */
    public enum PickValues { ONE, TWO, THREE }
    
    @Enumerated(EnumType.STRING)
    @MockApiField(name = "pickValueMulti__c", type = FieldType.picklist, custom = true)
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
    
    public PickValues[] getPickValueMulti() {
        return pickValueMulti;
    }

    public void setPickValueMulti(PickValues[] pickValueMulti) {
        this.pickValueMulti = pickValueMulti;
    }
    
    @Override
    public String toString() {
        return "[" + name + ", " + number + ", " + entityType + "]";
    }
}
