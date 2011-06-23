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

import java.lang.reflect.Method;
import java.util.Map;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.*;
import org.datanucleus.metadata.annotations.*;

import com.force.sdk.jpa.ForceStoreManager;
import com.force.sdk.jpa.PersistenceUtils;
import com.force.sdk.jpa.schema.ForceClassMetaData;
import com.force.sdk.jpa.schema.ForceMemberMetaData;

/**
 * 
 * Reader for processing annoations found in the com.force.sdk.jpa.annotation package.
 *
 * @author Fiaz Hossain
 */
public class ForceAnnotationReader extends AbstractAnnotationReader {

    /**
     * Creates an annotation reader that is designed to read specific Force.com annotations.
     * 
     * @param mgr the metadata manager for this application
     */
    public ForceAnnotationReader(MetaDataManager mgr) {
        super(mgr);
        
        // We support Force.com annotations in this reader.
        setSupportedAnnotationPackages(new String[] {ForceAnnotationReader.class.getPackage().getName()});
    }

    /**
     * Processes annotations at the class level.
     */
    @Override
    protected AbstractClassMetaData processClassAnnotations(PackageMetaData pmd, Class cls,
            AnnotationObject[] annotations, ClassLoaderResolver clr) {
        AbstractClassMetaData acmd = new ForceClassMetaData(pmd, cls.getSimpleName());
        if (annotations != null && annotations.length > 0) {
            for (int i = 0; i < annotations.length; i++) {
                AnnotationObject annotation = annotations[i];
                if (annotation.getName().equals(CustomObject.class.getName())) {
      
                    Map nameValues = annotation.getNameValueMap();
                    for (Method m : CustomObject.class.getDeclaredMethods()) {
                        Object value = nameValues.get(m.getName());
                        if (value != null && !value.equals(m.getDefaultValue())) {
                            // Only store the non-default values
                            acmd.addExtension(ForceStoreManager.FORCE_KEY, m.getName(), value.toString());
                        }
                    }
                } else {
                    throw new NucleusUserException("Unknown Force.com annotation: " + annotation.getName());
                }
            }
        }
        return acmd;
    }

    /**
     * Processes annotations at the field/property level.
     */
    @Override
    protected AbstractMemberMetaData processMemberAnnotations(AbstractClassMetaData cmd, Member member,
            AnnotationObject[] annotations, boolean propertyAccessor) {
        AbstractMemberMetaData mmd = null;
        if (annotations != null && annotations.length > 0) {
            for (int i = 0; i < annotations.length; i++) {
                AnnotationObject annotation = annotations[i];
                if (annotation.getName().equals(CustomField.class.getName())) {
                    mmd = addForceExtension(cmd, member, annotation, CustomField.class, "", mmd);
                } else if (annotation.getName().equals(PicklistValue.class.getName())) {
                    mmd = addForceExtension(cmd, member, annotation, PicklistValue.class,
                                                PersistenceUtils.PICKLIST_VALUE_FIELD_PREFIX, mmd);
                } else if (annotation.getName().equals(JoinFilter.class.getName())) {
                    mmd = addForceExtension(cmd, member, annotation, JoinFilter.class, PersistenceUtils.JOIN_FILTER, mmd);
                } else {
                    throw new NucleusUserException("Unknown Force.com annotation: " + annotation.getName());
                }
            }
        }
        if (mmd != null) {
            cmd.addMember(mmd);
        }
        return mmd;
    }
    
    /**
     * Adds extensions with a signifier that these are Force.com specific.
     * 
     * @param cmd the class metdata that contains members with Force.com annotations
     * @param member the member with the annotation
     * @param annotation the annotation to add
     * @param clazz the class of the annoation
     * @param prefix the prefix for the key, if necessary 
     * @param mmd the metadata for the member with the annotation
     * @return the member metadata with the new extension
     */
    private AbstractMemberMetaData addForceExtension(AbstractClassMetaData cmd, Member member,
            AnnotationObject annotation, Class<?> clazz, String prefix, AbstractMemberMetaData mmd) {
        mmd = mmd == null ? new ForceMemberMetaData(cmd, member.getName()) : mmd;
        Map nameValues = annotation.getNameValueMap();
        for (Method m : clazz.getDeclaredMethods()) {
            Object value = nameValues.get(m.getName());
            if (value != null && !value.equals(m.getDefaultValue())) {
                // Only store the non-default values
                String strValue = "";
                if (value instanceof Class) {
                    for (Object o : ((Class<?>) value).getEnumConstants()) {
                        strValue += strValue.length() == 0 ? ((Enum<?>) o).name() : "," + ((Enum<?>) o).name();
                    }
                } else {
                    strValue = value.toString();
                }
                mmd.addExtension(ForceStoreManager.FORCE_KEY, prefix + m.getName(), strValue);
            }
        }
        return mmd;
    }

    /**
     * Overrides base class method to disable processing of annotations on methods, stick to properties only.
     */
    @Override
    protected void processMethodAnnotations(AbstractClassMetaData cmd, Method method) {  }
}
