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

package com.force.sdk.jpa.schema;

import java.util.ArrayList;
import java.util.List;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.*;

/**
 * 
 * Metadata class used during annotation processing
 * (see {@link com.force.sdk.jpa.annotation.ForceAnnotationReader}).
 * It's used to gain access to the list of members
 *
 * @author Fiaz Hossain
 */
public class ForceClassMetaData extends AbstractClassMetaData {

    //lazily instantiated, use getMembersList() inside this class
    private List<AbstractMemberMetaData> members;
    
    /**
     * Creates a class metadata object with the enclosing package.
     * 
     * @param parent The package to which this class/interface belongs
     * @param name (Simple) name of class (omitting the package name)
     */
    public ForceClassMetaData(PackageMetaData parent, String name) {
        super(parent, name);
    }

    /**
     * Creates a class metadata object for an implementation of a "persistent-abstract-class".
     * 
     * @param cmd MetaData for the implementation of the "persistent-abstract-class"
     * @param implClassName Name of the implementation class
     */
    public ForceClassMetaData(ClassMetaData cmd, String implClassName) {
        super(cmd, implClassName);
    }

    /**
     * Creates a class metadata object for an implementation of a "persistent-interface".
     * 
     * @param imd MetaData for the "persistent-interface"
     * @param implClassName Name of the implementation class
     * @param copyMembers Whether to copy the fields/properties of the interface too
     */
    public ForceClassMetaData(InterfaceMetaData imd, String implClassName, boolean copyMembers) {
        super(imd, implClassName, copyMembers);
    }

    @Override
    public void initialise(ClassLoaderResolver clr, MetaDataManager mmgr) {
    }

    @Override
    public void populate(ClassLoaderResolver clr, ClassLoader primary, MetaDataManager mmgr) {
    }
    
    @Override
    public void addMember(AbstractMemberMetaData mmd) {
        getMembersList().add(mmd);
    }
    
    public List<AbstractMemberMetaData> getMembers() {
        return getMembersList();
    }
    
    @Override
    public int getMemberCount() {
        if (members == null) return 0;
        return getMembersList().size();
    }
    
    /**
     * Lazily instantiates the member list.
     */
    private List<AbstractMemberMetaData> getMembersList() {
        if (members == null) {
            members = new ArrayList<AbstractMemberMetaData>();
        }
        
        return members;
    }
}
