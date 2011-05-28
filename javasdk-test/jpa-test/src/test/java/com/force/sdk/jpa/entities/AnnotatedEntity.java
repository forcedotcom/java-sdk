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

import com.force.sdk.jpa.entities.TestEntity.PickValues;

/**
 * Shared class used by TestEntity and TestEntityMethodAnnotations that allow for us
 * to test JPA annotations at both the field level and the method level.
 * If you're trying to add anything new fields to TestEntity.java they should be added here too.
 * 
 * @author Jill Wetzler
 */
public interface AnnotatedEntity {
    /**
     * Please keep TOTAL_FIELDS updated. But if you add a new field and miss to updated this you will fails comparison tests.
     */
    int TOTAL_FIELDS = 48;
    
    String getId();
    void setId(String id);
    String getName();
    void setName(String name);
    String getStringObject();
    void setStringObject(String stringObject);
    Boolean getBooleanObject();
    void setBooleanObject(Boolean booleanObject);
    boolean getBoolType();
    void setBoolType(boolean boolType);
    Byte getByteObject();
    void setByteObject(Byte byteObject);
    byte getByteType();
    void setByteType(byte byteType);
    Short getShortObject();
    void setShortObject(Short shortObject);
    short getShortType();
    void setShortType(short shortType);
    Integer getIntegerObject();
    void setIntegerObject(Integer integerObject);
    int getIntType();
    void setIntType(int intType);
    Long getLongObject();
    void setLongObject(Long longObject);
    long getLongType();
    void setLongType(long longType);
    Double getDoubleObject();
    void setDoubleObject(Double doubleObject);
    double getDoubleType();
    void setDoubleType(double doubleType);
    Float getFloatObject();
    void setFloatObject(Float floatObject);
    float getFloatType();
    void setFloatType(float floatType);
    Character getCharacterObject();
    void setCharacterObject(Character characterObject);
    char getCharType();
    void setCharType(char charType);
    BigDecimal getBigDecimalObject();
    void setBigDecimalObject(BigDecimal bigDecimalObject);
    BigInteger getBigIntegerObject();
    void setBigIntegerObject(BigInteger bigIntegerObject);
    String getEmail();
    void setEmail(String email);
    int getPercent();
    void setPercent(int percent);
    Date getDate();
    void setDate(Date date);
    Calendar getDateTimeCal();
    void setDateTimeCal(Calendar dateTimeCal);
    GregorianCalendar getDateTimeGCal();
    void setDateTimeGCal(GregorianCalendar dateTimeGCal);
    Date getDateTemporal();
    void setDateTemporal(Date date);
    Date getDateTimeTemporal();
    void setDateTimeTemporal(Date dateTime);
    ParentTestEntity getParent();
    void setParent(ParentTestEntity parent);
    User getUserLookUp();
    void setUserLookUp(User user);
    ParentTestEntity getParentMasterDetail();
    void setParentMasterDetail(ParentTestEntity parent);
    PickValues getPickValueDef();
    void setPickValueDef(PickValues pickValueDef);
    PickValues getPickValue();
    void setPickValue(PickValues pickValue);
    PickValues getPickValueOrdinal();
    void setPickValueOrdinal(PickValues pickValueOrdinal);
    String getLiberalPickValueDef();
    void setLiberalPickValueDef(String liberalPickValueDef);
    PickValues[] getPickValueMultiDef();
    void setPickValueMultiDef(PickValues[] pickValueMultiDef);
    PickValues[] getPickValueMulti();
    void setPickValueMulti(PickValues[] pickValueMulti);
    PickValues[] getPickValueMultiOrdinal();
    void setPickValueMultiOrdinal(PickValues[] pickValueMultiOrdinal);
    String[] getLiberalPickValueMultiDef();
    void setLiberalPickValueMultiDef(String[] liberalPickValueMultiDef);
    URL getUrl();
    void setUrl(URL url);
    String getPhone();
    void setPhone(String phone);
    Calendar getLastModifiedDate();
    void setLastModifiedDate(Calendar lastModifiedDate);
    String getUnused();
    void setUnused(String unused);
    EmbeddedTestEntity getEmbedded();
    void setEmbedded(EmbeddedTestEntity embedded);
    long getAutoNum();
    void setAutoNum(long autoNum);
    String getTextArea();
    void setTextArea(String textArea);
    String getLongTextArea();
    void setLongTextArea(String longTextArea);
    String getRichTextArea();
    void setRichTextArea(String richTextArea);
}
