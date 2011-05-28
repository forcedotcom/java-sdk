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

import com.force.sdk.jpa.annotation.CustomField;
import com.force.sdk.jpa.annotation.PicklistValue;
import com.sforce.soap.metadata.FieldType;

/**
 * If you're trying to add additional fields to this class please add them to AnnotatedEntity.java 
 * to keep TestEntityMethodAnnotations.java in sync.
 * 
 * @author Jill Wetzler
 *
 */
@Entity
public class TestEntity implements AnnotatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;
    
    String name;
    
    boolean boolType; //checkbox
    byte byteType; //text
    short shortType; //number
    public int intType; //number
    long longType; //number
    double doubleType; //number
    float floatType; //number
    char charType; //text
    
    Boolean booleanObject;
    Byte byteObject;
    @CustomField(precision = 11, scale = 0) //CustomField annotation trumps Column annotation
    @Column(precision = 17, scale = 0)
    Short shortObject;
    Integer integerObject;
    Long longObject;
    Double doubleObject;
    Float floatObject;
    Character characterObject;
    
    @Column(precision = 16, scale = 2)
    BigDecimal bigDecimalObject; //currency
    BigInteger bigIntegerObject; //number
    public String stringObject; //text
    @Basic
    URL url; //url
    @CustomField(type = FieldType.Phone)
    String phone; //phone
    @CustomField(type = FieldType.Email)
    String email; //email
    @CustomField(type = FieldType.Percent)
    Integer percent; //percent

    @CustomField(label = "Date field", description = "This is a test for a data field")
    Date date; //date
    Calendar dateTimeCal; //date/time
    GregorianCalendar dateTimeGCal; //date/time
    
    // Test other date formats
    @Temporal(TemporalType.DATE)
    Date dateTemporal; //date
    @Temporal(TemporalType.TIMESTAMP)
    Date dateTimeTemporal; //date/time
    
    //@Temporal(TemporalType.TIME)
    //public Date timeTemporal; -- can't support this since we can't describe this field type even though we support in API
    
    /**
     * Test Picklist values enum.
     * 
     * @author Jill Wetzler
     */
    public enum PickValues { ONE, TWO, THREE }
    
    @Enumerated
    PickValues pickValueDef; //picklist

    @Enumerated(EnumType.STRING)
    PickValues pickValue; //picklist
    
    @Enumerated(EnumType.ORDINAL)
    PickValues pickValueOrdinal; //picklist

    @CustomField(type = FieldType.Picklist)
    @PicklistValue(PickValues.class)
    String liberalPickValueDef; //non-strict picklist value

    @Enumerated
    PickValues[] pickValueMultiDef; //MSP
    
    @Enumerated(EnumType.STRING)
    PickValues[] pickValueMulti; //MSP
    
    @Enumerated(EnumType.ORDINAL)
    PickValues[] pickValueMultiOrdinal; //MSP

    @Enumerated(EnumType.STRING)
    @PicklistValue(PickValues.class)
    String[] liberalPickValueMultiDef; //non-strict picklist value
    
    @Column(name = "ParentTestEntity")
    @ManyToOne
    ParentTestEntity parent; //custom lookup
    
    @Column(name = "User")
    @ManyToOne
    @CustomField(childRelationshipName = "TestEntitiesForUser")
    User userLookUp; //standard lookup
    
    @Column(name = "ParentTestEntity")
    @ManyToOne
    @CustomField(type = FieldType.MasterDetail, childRelationshipName = "TestEntitiesMD", name = "ParentMD")
    ParentTestEntity parentMasterDetail; //standard master detail
    
    @Version
    Calendar lastModifiedDate;
    
    @Transient
    String unused;
    
    @Embedded
    EmbeddedTestEntity embedded;
    
    @CustomField(type = FieldType.AutoNumber, startValue = 100)
    long autoNum;
    
    @CustomField(type = FieldType.TextArea)
    String textArea;

    @CustomField(type = FieldType.LongTextArea)
    String longTextArea;

    @CustomField(type = FieldType.Html)
    String richTextArea;
    
    public TestEntity() {
        
    }
    
    public TestEntity(String name) {
        this.name = name;
    }

    public TestEntity(String name, ParentTestEntity parent, ParentTestEntity parentMD) {
        this.name = name;
        this.parent = parent;
        this.parentMasterDetail = parentMD;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getStringObject() {
        return stringObject;
    }

    @Override
    public void setStringObject(String stringObject) {
        this.stringObject = stringObject;
    }
    
    @Override
    public Boolean getBooleanObject() {
        return booleanObject;
    }

    @Override
    public void setBooleanObject(Boolean booleanObject) {
        this.booleanObject = booleanObject;
    }
    
    @Override
    public boolean getBoolType() {
        return boolType;
    }

    @Override
    public void setBoolType(boolean boolType) {
        this.boolType = boolType;
    }
    
    @Override
    public Byte getByteObject() {
        return byteObject;
    }

    @Override
    public void setByteObject(Byte byteObject) {
        this.byteObject = byteObject;
    }
    
    @Override
    public byte getByteType() {
        return byteType;
    }
    
    @Override
    public void setByteType(byte byteType) {
        this.byteType = byteType;
    }
    
    @Override
    public Short getShortObject() {
        return shortObject;
    }
    
    @Override
    public void setShortObject(Short shortObject) {
        this.shortObject = shortObject;
    }
    
    @Override
    public short getShortType() {
        return shortType;
    }
    
    @Override
    public void setShortType(short shortType) {
        this.shortType = shortType;
    }

    @Override
    public Integer getIntegerObject() {
        return integerObject;
    }
    
    @Override
    public void setIntegerObject(Integer integerObject) {
        this.integerObject = integerObject;
    }
    
    @Override
    public int getIntType() {
        return intType;
    }
    
    @Override
    public void setIntType(int intType) {
        this.intType = intType;
    }
    
    @Override
    public Long getLongObject() {
        return longObject;
    }

    @Override
    public void setLongObject(Long longObject) {
        this.longObject = longObject;
    }
    
    @Override
    public long getLongType() {
        return longType;
    }

    @Override
    public void setLongType(long longType) {
        this.longType = longType;
    }
    
    @Override
    public Double getDoubleObject() {
        return doubleObject;
    }
    
    @Override
    public void setDoubleObject(Double doubleObject) {
        this.doubleObject = doubleObject;
    }
    
    @Override
    public double getDoubleType() {
        return doubleType;
    }
    
    @Override
    public void setDoubleType(double doubleType) {
        this.doubleType = doubleType;
    }
    
    @Override
    public Float getFloatObject() {
        return floatObject;
    }
    
    @Override
    public void setFloatObject(Float floatObject) {
        this.floatObject = floatObject;
    }
    
    @Override
    public float getFloatType() {
        return floatType;
    }
    
    @Override
    public void setFloatType(float floatType) {
        this.floatType = floatType;
    }
    
    @Override
    public Character getCharacterObject() {
        return characterObject;
    }
    
    @Override
    public void setCharacterObject(Character characterObject) {
        this.characterObject = characterObject;
    }
    
    @Override
    public char getCharType() {
        return charType;
    }
    
    @Override
    public void setCharType(char charType) {
        this.charType = charType;
    }
    
    @Override
    public BigDecimal getBigDecimalObject() {
        return bigDecimalObject;
    }

    @Override
    public void setBigDecimalObject(BigDecimal bigDecimalObject) {
        this.bigDecimalObject = bigDecimalObject;
    }
    
    @Override
    public BigInteger getBigIntegerObject() {
        return bigIntegerObject;
    }

    @Override
    public void setBigIntegerObject(BigInteger bigIntegerObject) {
        this.bigIntegerObject = bigIntegerObject;
    }
    
    @Override
    public String getEmail() {
        return email;
    }
    
    @Override
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public int getPercent() {
        return percent;
    }
    
    @Override
    public void setPercent(int percent) {
        this.percent = percent;
    }

    @Override
    public Date getDate() {
        return date;
    }
    
    @Override
    public void setDate(Date date) {
        this.date = date;
    }
    
    @Override
    public Calendar getDateTimeCal() {
        return dateTimeCal;
    }
    
    @Override
    public void setDateTimeCal(Calendar dateTimeCal) {
        this.dateTimeCal = dateTimeCal;
    }
    
    @Override
    public GregorianCalendar getDateTimeGCal() {
        return dateTimeGCal;
    }
    
    @Override
    public void setDateTimeGCal(GregorianCalendar dateTimeGCal) {
        this.dateTimeGCal = dateTimeGCal;
    }
    
    /*
    public Time getTime() {
        return time;
    }
    
    public void setTime(Time time) {
        this.time = time;
    }
    */
    
    @Override
    public Date getDateTemporal() {
        return dateTemporal;
    }
    
    @Override
    public void setDateTemporal(Date dateTemporal) {
        this.dateTemporal = dateTemporal;
    }
    
    @Override
    public Date getDateTimeTemporal() {
        return dateTimeTemporal;
    }
    
    @Override
    public void setDateTimeTemporal(Date dateTime) {
        this.dateTimeTemporal = dateTime;
    }

    /*
    public Date getTimeTemporal() {
        return timeTemporal;
    }
    
    public void setTimeTemporal(Date time) {
        this.timeTemporal = time;
    }
    */
   
    @Override
    public ParentTestEntity getParent() {
        return parent;
    }

    @Override
    public void setParent(ParentTestEntity parent) {
        this.parent = parent;
    }
    
    @Override
    public User getUserLookUp() {
        return userLookUp;
    }

    @Override
    public void setUserLookUp(User user) {
        this.userLookUp = user;
    }
    
    @Override
    public ParentTestEntity getParentMasterDetail() {
        return parentMasterDetail;
    }
    
    @Override
    public void setParentMasterDetail(ParentTestEntity parentMasterDetail) {
        this.parentMasterDetail = parentMasterDetail;
    }
    
    @Override
    public PickValues getPickValueDef() {
        return pickValueDef;
    }
    
    @Override
    public void setPickValueDef(PickValues pickValueDef) {
        this.pickValueDef = pickValueDef;
    }
    
    @Override
    public PickValues getPickValue() {
        return pickValue;
    }
    
    @Override
    public void setPickValue(PickValues pickValue) {
        this.pickValue = pickValue;
    }
    
    @Override
    public PickValues getPickValueOrdinal() {
        return pickValueOrdinal;
    }
    
    @Override
    public void setPickValueOrdinal(PickValues pickValueOrdinal) {
        this.pickValueOrdinal = pickValueOrdinal;
    }
    
    @Override
    public String getLiberalPickValueDef() {
        return liberalPickValueDef;
    }
    
    @Override
    public void setLiberalPickValueDef(String liberalPickValueDef) {
        this.liberalPickValueDef = liberalPickValueDef;
    }
    
    @Override
    public PickValues[] getPickValueMultiDef() {
        return pickValueMultiDef;
    }
    
    @Override
    public void setPickValueMultiDef(PickValues[] pickValueMultiDef) {
        this.pickValueMultiDef = pickValueMultiDef;
    }
    
    @Override
    public PickValues[] getPickValueMulti() {
        return pickValueMulti;
    }
    
    @Override
    public void setPickValueMulti(PickValues[] pickValueMulti) {
        this.pickValueMulti = pickValueMulti;
    }
    
    @Override
    public PickValues[] getPickValueMultiOrdinal() {
        return pickValueMultiOrdinal;
    }
    
    @Override
    public void setPickValueMultiOrdinal(PickValues[] pickValueMultiOrdinal) {
        this.pickValueMultiOrdinal = pickValueMultiOrdinal;
    }

    @Override
    public String[] getLiberalPickValueMultiDef() {
        return liberalPickValueMultiDef;
    }
    
    @Override
    public void setLiberalPickValueMultiDef(String[] liberalPickValueMultiDef) {
        this.liberalPickValueMultiDef = liberalPickValueMultiDef;
    }
    
    @Override
    public URL getUrl() {
        return url;
    }
    
    @Override
    public void setUrl(URL url) {
        this.url = url;
    }
    
    @Override
    public String getPhone() {
        return phone;
    }
    
    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    @Override
    public Calendar getLastModifiedDate() {
        return this.lastModifiedDate;
    }
    
    @Override
    public void setLastModifiedDate(Calendar lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    @Override
    public String getUnused() {
        return null;
    }

    @Override
    public void setUnused(String unused) {
        this.unused = unused;
    }
    
    
    @Override
    public long getAutoNum() {
        return autoNum;
    }
    
    @Override
    public void setAutoNum(long autoNum) {
        this.autoNum = autoNum;
    }
    
    @Override
    public String getTextArea() {
        return textArea;
    }
    
    @Override
    public void setTextArea(String textArea) {
        this.textArea = textArea;
    }
    
    @Override
    public String getLongTextArea() {
        return longTextArea;
    }
    
    @Override
    public void setLongTextArea(String longTextArea) {
        this.longTextArea = longTextArea;
    }
    
    @Override
    public String getRichTextArea() {
        return richTextArea;
    }
    
    @Override
    public void setRichTextArea(String richTextArea) {
        this.richTextArea = richTextArea;
    }

    @Override
    public EmbeddedTestEntity getEmbedded() {
        return embedded;
    }

    @Override
    public void setEmbedded(EmbeddedTestEntity embedded) {
        this.embedded = embedded;
    }
    
}
