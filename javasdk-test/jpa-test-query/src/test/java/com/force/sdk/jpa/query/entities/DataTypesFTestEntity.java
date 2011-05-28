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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;

import javax.persistence.*;

import com.force.sdk.jpa.annotation.CustomField;
import com.sforce.soap.metadata.FieldType;
import com.sforce.ws.types.Time;

/**
 * 
 * Entity used for testing all supported data types.
 *
 * @author Fiaz Hossain
 */
@Entity
public class DataTypesFTestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    
    private String name;
    
    @CustomField(type = FieldType.TextArea)
    private String textArea;
    
    private boolean booleanType; // string
    private byte byteType; // string
    private char charType; // string
    private short shortType; //number
    private int intType; //number
    private long longType; //number
    private float floatType; //number
    private double doubleType; //number
    
    private Boolean booleanObject;
    private Byte byteObject;
    private Character characterObject;
    @CustomField(precision = 11, scale = 0) // CustomField annotation trumps Column annotation
    @Column(precision = 17, scale = 0)
    private Short shortObject;
    private Integer integerObject;
    private Long longObject;
    private Float floatObject;
    private Double doubleObject;
    private String stringObject; //text
    
    private BigInteger bigIntegerObject; //number
    
    @Column(precision = 16, scale = 2)
    private BigDecimal bigDecimalObject; //currency
    
    @CustomField(label = "Date field", description = "This is a test for a data field")
    private Date date; //date
    
    //date/time
    private Calendar dateTimeCal;
    private GregorianCalendar dateTimeGCal;
    @Basic
    private Time time;
    
    /**
     * Test Picklist values enum.
     * 
     * @author Tim Kral
     */
    public enum PickValues { ONE, TWO, THREE }
    
    @Enumerated
    private PickValues pickValueDef; //picklist
    
    @Enumerated(EnumType.STRING)
    private PickValues pickValue; //picklist
    
    @Basic
    private URL url;
    
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTextArea(String textArea) {
        this.textArea = textArea;
    }

    public String getTextArea() {
        return textArea;
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
    
    public void setPickValueDef(PickValues pickValueDef) {
        this.pickValueDef = pickValueDef;
    }

    public PickValues getPickValueDef() {
        return pickValueDef;
    }

    public void setPickValue(PickValues pickValue) {
        this.pickValue = pickValue;
    }

    public PickValues getPickValue() {
        return pickValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }
}
