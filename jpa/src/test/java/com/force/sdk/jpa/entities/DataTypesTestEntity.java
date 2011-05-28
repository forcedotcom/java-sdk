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
import java.math.BigInteger;
import java.net.URL;
import java.util.*;

import javax.persistence.*;

import com.force.sdk.jpa.mock.MockApiEntity;
import com.force.sdk.jpa.mock.MockApiField;
import com.sforce.soap.partner.FieldType;
import com.sforce.ws.types.Time;

/**
 * 
 * Entity which contains all Force.com
 * JPA data types.
 *
 * @author Tim Kral
 */
@Entity
@MockApiEntity
public class DataTypesTestEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @MockApiField(name = "Id", type = FieldType.id, custom = false)
    private String id;
    @MockApiField(name = "Name", type = FieldType.string, custom = false)
    private String name;
    
    @MockApiField(name = "booleanType__c", type = FieldType._boolean, custom = true)
    private boolean booleanType;
    @MockApiField(name = "byteType__c", type = FieldType.string, custom = true)
    private byte byteType;
    @MockApiField(name = "charType__c", type = FieldType.string, custom = true)
    private char charType;
    @MockApiField(name = "shortType__c", type = FieldType._double, custom = true)
    private short shortType;
    @MockApiField(name = "intType__c", type = FieldType._double, custom = true)
    private int intType;
    @MockApiField(name = "longType__c", type = FieldType._double, custom = true)
    private long longType;
    @MockApiField(name = "floatType__c", type = FieldType._double, custom = true)
    private float floatType;
    @MockApiField(name = "doubleType__c", type = FieldType._double, custom = true)
    private double doubleType;

    @MockApiField(name = "booleanObject__c", type = FieldType._boolean, custom = true)
    private Boolean booleanObject;
    @MockApiField(name = "byteObject__c", type = FieldType.string, custom = true)
    private Byte byteObject;
    @MockApiField(name = "characterObject__c", type = FieldType.string, custom = true)
    private Character characterObject;
    @MockApiField(name = "shortObject__c", type = FieldType._double, custom = true)
    private Short shortObject;
    @MockApiField(name = "integerObject__c", type = FieldType._double, custom = true)
    private Integer integerObject;
    @MockApiField(name = "longObject__c", type = FieldType._double, custom = true)
    private Long longObject;
    @MockApiField(name = "floatObject__c", type = FieldType._double, custom = true)
    private Float floatObject;
    @MockApiField(name = "doubleObject__c", type = FieldType._double, custom = true)
    private Double doubleObject;
    @MockApiField(name = "stringObject__c", type = FieldType.string, custom = true)
    private String stringObject;
    
    @MockApiField(name = "bigIntegerObject__c", type = FieldType._double, custom = true)
    private BigInteger bigIntegerObject; //number
    
    @Column(precision = 16, scale = 2)
    @MockApiField(name = "bigDecimalObject__c", type = FieldType.currency, custom = true,
                  attrs = { "setPrecision=18", "setScale=2" })
    private BigDecimal bigDecimalObject;

    @MockApiField(name = "date__c", type = FieldType.date, custom = true)
    private Date date;

    @MockApiField(name = "dateTimeCal__c", type = FieldType.datetime, custom = true)
    private Calendar dateTimeCal;
    @MockApiField(name = "dateTimeGCal__c", type = FieldType.datetime, custom = true)
    private GregorianCalendar dateTimeGCal;
    @Basic
    @MockApiField(name = "time__c", type = FieldType.datetime, custom = true)
    private Time time;
    
    /**
     * Test Picklist values enum.
     * 
     * @author Tim Kral
     */
    public enum PickValues { ONE, TWO, THREE }
    
    @Enumerated(EnumType.STRING)
    @MockApiField(name = "pickValue__c", type = FieldType.picklist, custom = true)
    private PickValues pickValue;
    
    @Basic
    @MockApiField(name = "url__c", type = FieldType.url, custom = true)
    private URL url;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setBooleanType(boolean booleanType) {
        this.booleanType = booleanType;
    }
    
    public boolean getBooleanType() {
        return booleanType;
    }
    
    public void setByteType(byte byteType) {
        this.byteType = byteType;
    }
    
    public byte getByteType() {
        return byteType;
    }
    
    public void setCharType(char charType) {
        this.charType = charType;
    }
    
    public char getCharType() {
        return charType;
    }
    
    public void setShortType(short shortType) {
        this.shortType = shortType;
    }

    public short getShortType() {
        return shortType;
    }

    public void setIntType(int intType) {
        this.intType = intType;
    }

    public int getIntType() {
        return intType;
    }

    public void setLongType(long longType) {
        this.longType = longType;
    }

    public long getLongType() {
        return longType;
    }

    public void setFloatType(float floatType) {
        this.floatType = floatType;
    }

    public float getFloatType() {
        return floatType;
    }

    public void setDoubleType(double doubleType) {
        this.doubleType = doubleType;
    }

    public double getDoubleType() {
        return doubleType;
    }

    public void setBooleanObject(Boolean booleanObject) {
        this.booleanObject = booleanObject;
    }
    
    public Boolean getBooleanObject() {
        return booleanObject;
    }
    
    public void setByteObject(Byte byteObject) {
        this.byteObject = byteObject;
    }
    
    public Byte getByteObject() {
        return byteObject;
    }
    
    public void setCharacterObject(Character characterObject) {
        this.characterObject = characterObject;
    }
    
    public Character getCharacterObject() {
        return characterObject;
    }
    
    public void setShortObject(Short shortObject) {
        this.shortObject = shortObject;
    }

    public Short getShortObject() {
        return shortObject;
    }

    public void setIntegerObject(Integer integerObject) {
        this.integerObject = integerObject;
    }

    public Integer getIntegerObject() {
        return integerObject;
    }

    public void setLongObject(Long longObject) {
        this.longObject = longObject;
    }

    public Long getLongObject() {
        return longObject;
    }

    public void setFloatObject(Float floatObject) {
        this.floatObject = floatObject;
    }

    public Float getFloatObject() {
        return floatObject;
    }

    public void setDoubleObject(Double doubleObject) {
        this.doubleObject = doubleObject;
    }

    public Double getDoubleObject() {
        return doubleObject;
    }

    public void setStringObject(String stringObject) {
        this.stringObject = stringObject;
    }

    public String getStringObject() {
        return stringObject;
    }

    public void setBigIntegerObject(BigInteger bigIntegerObject) {
        this.bigIntegerObject = bigIntegerObject;
    }

    public BigInteger getBigIntegerObject() {
        return bigIntegerObject;
    }

    public void setBigDecimalObject(BigDecimal bigDecimalObject) {
        this.bigDecimalObject = bigDecimalObject;
    }

    public BigDecimal getBigDecimalObject() {
        return bigDecimalObject;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setDateTimeCal(Calendar dateTimeCal) {
        this.dateTimeCal = dateTimeCal;
    }

    public Calendar getDateTimeCal() {
        return dateTimeCal;
    }

    public void setDateTimeGCal(GregorianCalendar dateTimeGCal) {
        this.dateTimeGCal = dateTimeGCal;
    }

    public GregorianCalendar getDateTimeGCal() {
        return dateTimeGCal;
    }

    public void setTime(Time time) {
        this.time = time;
    }
    
    public Time getTime() {
        return time;
    }

    public void setPickValue(PickValues pickValue) {
        this.pickValue = pickValue;
    }

    public PickValues getPickValue() {
        return pickValue;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }
}

