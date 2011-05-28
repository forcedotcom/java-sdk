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

package com.force.sdk.jpa.query;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * Util class for formatting dates in the form that SOQL expects.
 *
 * @author Saptarshi Roy
 */
public final class SOQLDateFormatUtil {

    private SOQLDateFormatUtil() {  }
    
    /**
     * Formats a {@code java.util.Calendar} object as a SOQL date.
     * 
     * @param value {@code java.util.Calendar} object to format
     * @return a SOQL date formatted String 
     */
    public static String getSOQLFormat(Calendar value) {
        TimeZone tz = value.getTimeZone();
        TimeZone localTz = TimeZone.getDefault();
        int offset = tz.getRawOffset();
        //find the offset difference between the timezone of this host and the timezone of
        //the calendar object passed in
        int offsetDiffFromLocal = offset - localTz.getRawOffset();
        //adjust the date value by the difference between the two time zones. This is
        //necessary because getTime and getTimeInMillis both automatically localize
        //to the current time zone.
        Date adjustedDateValue = new Date(value.getTimeInMillis() + offsetDiffFromLocal);
        
        String timezone =
            String.format("%s%02d:%02d", offset >= 0 ? "+" : "-", Math.abs(offset / 3600000), Math.abs((offset / 60000) % 60));
        
        //Append the original time zone of the date that we're formatting 
        //to the output so that we keep the timezone consistent.
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(adjustedDateValue) + timezone;
    }
    
    /**
     * Formats a {@code java.util.Date} object as a SOQL date.
     * 
     * @param date {@code java.util.Date} object to format
     * @return a SOQL date formatted String 
     */
    public static String getSOQLFormat(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
    
}
