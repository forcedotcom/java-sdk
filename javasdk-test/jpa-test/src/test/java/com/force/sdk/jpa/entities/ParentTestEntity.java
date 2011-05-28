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

import java.lang.reflect.Field;
import java.util.*;

import javax.persistence.*;

import junit.framework.Assert;

import com.force.sdk.jpa.annotation.CustomField;
import com.force.sdk.jpa.annotation.CustomObject;

/**
 * 
 * Basic Parent testing entity for various smoke tests.
 *
 * @author Jill Wetzler
 */
@Entity
@CustomObject(enableFeeds = true)
public class ParentTestEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private String id;
   
   private String name;
   @CustomField(enableFeeds = true)
   private String textField;
   
    @CustomField(externalId = true)
    private String extIdField;
    
    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    private Collection<TestEntity> testEntities;
    
    @ManyToOne
    private User ownerId;

    @Version
    Calendar lastModifiedDate;

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
   
   public String getTextField() {
        return textField;
    }
    
    public void setTextField(String textField) {
        this.textField = textField;
    }
   
    public String getExtIdField() {
        return extIdField;
    }
    
    public void setExtIdField(String extIdField) {
        this.extIdField = extIdField;
    }
    
    public Collection<TestEntity> getTestEntities() {
        return testEntities;
    }
    
    public void addTestEntity(TestEntity entity) {
        if (testEntities == null) {
            testEntities = new ArrayList<TestEntity>();
        }
        
        testEntities.add(entity);
    }

    public User getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(User owner) {
        this.ownerId = owner;
    }
    
   /**
    * Convenience since this entity is just for testing purposes.
    */
   public void init() {
      name = "parent entity";
   }
   
   /**
     * Convenience method to compare two TestEntity objects based on their fields via reflection.
     * This method will fail if returned types are complex types due to the equals comparison.
     * 
     * @param persisted The entity that was persisted to the DB.
     */
   @Override
    public boolean equals(Object compareTo) {
        if (this == compareTo) return true;
        if (compareTo == null) return false;
        if (!(compareTo instanceof ParentTestEntity)) return false;
        
      ParentTestEntity entity = (ParentTestEntity) compareTo;
        Object[] invokeArg = null;
        Class[] getterArg = null;
        try {
            Field[] fields = Class.forName("com.force.sdk.jpa.ParentTestEntity").getFields();
            for (Field f : fields) {
                if (f.getDeclaringClass().toString().contains("javax.jdo")) continue; //ignore fields created by enhancer
                String fieldName = f.getName();
                String accessor = "get" + fieldName.replace(fieldName.substring(0, 1), fieldName.substring(0, 1).toUpperCase());
                Object expected = this.getClass().getMethod(accessor, getterArg).invoke(this, invokeArg);
                Object actual = entity.getClass().getMethod(accessor, getterArg).invoke(entity, invokeArg);
                // TODO: We have to specialcase the comparisons for now.
                // That is because we are ignoring TimeZone information. We will have to add that in.
                if (expected instanceof Calendar && actual instanceof Calendar) {
                    Assert.assertEquals("Difference at field (type Clendar) " + fieldName + ": ",
                            ((Calendar) expected).getTime(), ((Calendar) actual).getTime());
                } else {
                    Assert.assertEquals("Difference at field " + fieldName + ": ", expected, actual);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        // No need for a hashCode implementation.
        // However, this is here for static analyzers looking
        // to satisfy the equals-hashCode invariant.
        return super.hashCode();
    }
}
