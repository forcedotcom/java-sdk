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

package com.force.sdk.jpa.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.sforce.soap.metadata.FieldType;
import com.sforce.soap.metadata.TreatBlanksAs;

/**
 * 
 * CustomField annotation for setting metadata properties on fields in Force.com.
 *
 * @author Fiaz Hossain
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface CustomField {

    /**
     * Force.com field type.
     */
    FieldType type() default FieldType.Text;
    
    /**
     * Force.com field name. 
     */
    String name() default "";
    
    /**
     * Force.com field label. 
     */
    String label() default "";
    
    /**
     * Force.com field description. 
     */
    String description() default "";
    
    /**
     * Force.com field length. 
     */
    int length() default 0;

    /**
     * Force.com field precision. 
     */
    int precision() default 0;

    /**
     * Force.com field scale. 
     */
    int scale() default 0;
    
    /**
     * Start value for Force.com Autonumber field. 
     */
    int startValue() default 0;
    
    /**
     * Force.com child relationship name.
     */
    String childRelationshipName() default "";
    
    /**
     * Enable Force.com Chatter feeds.
     */
    boolean enableFeeds() default false;

    /**
     * Enable external Id on Force.com field.
     */
    boolean externalId() default false;
    
    /**
     * Force.com field formula.
     */
    String formula() default "";
    
    /**
     * Treat blank as value for Force.com Formula field.
     */
    TreatBlanksAs treatBlanksAs() default TreatBlanksAs.BlankAsBlank;
}
