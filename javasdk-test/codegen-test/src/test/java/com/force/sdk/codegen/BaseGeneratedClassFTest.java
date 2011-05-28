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

package com.force.sdk.codegen;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Table;

import org.datanucleus.ObjectManager;
import org.datanucleus.store.connection.ConnectionFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import com.force.sdk.jpa.ForceManagedConnection;
import com.force.sdk.jpa.model.BaseForceObject;
import com.sforce.soap.partner.PartnerConnection;

/**
 * Base test class for generated JPA Java classes.
 *
 * @author Tim Kral
 */
public abstract class BaseGeneratedClassFTest {

    private static final Pattern GENERATED_CLASS_PATTERN =
        Pattern.compile("^.+/codegen-test/target/test-classes/(.+)/(.+)\\.class$");
    
    private Object[][] generatedClassArray;
    private List<Class<?>> generatedClassList;
    private Map<String, Class<?>> generatedClassNameMap; // [SObjectName -> Class]
    private Map<String, Class<?>> generatedEnumNameMap; // [Enum class name -> Class]
    
    protected EntityManager em;
    protected PartnerConnection conn;
    
    protected void loadEntityManager() {
        if (em == null) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("CodeGenTest");
            em = emf.createEntityManager();
        }
        
        if (conn == null) {
            ObjectManager om = (ObjectManager) em.getDelegate();
            
            // Get the EntityManager's PartnerConnection
            ConnectionFactory connFactory = om.getStoreManager().getConnectionManager().lookupConnectionFactory("force");
            ForceManagedConnection mconn = (ForceManagedConnection) connFactory.createManagedConnection(null, null);
            conn = (PartnerConnection) mconn.getConnection();
        }
    }
    
    @BeforeClass
    public void loadGeneratedClasses() throws IOException, ClassNotFoundException {
        // Load all generated Java classes from the gold standard package
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assertNotNull(classLoader, "Unable to load ClassLoader to find generated JPA Java classes.");
        
        Enumeration<URL> jpaClassFileURL = classLoader.getResources("com/goldstandard/model/");
        assertTrue(jpaClassFileURL.hasMoreElements(), "Cannot find com/goldstandard/model/ resource");
        
        // There should only be one base resource directory
        File jpaClassFileDir = new File(jpaClassFileURL.nextElement().getFile());
        
        generatedClassList = new ArrayList<Class<?>>();
        generatedClassNameMap = new HashMap<String, Class<?>>();
        generatedEnumNameMap = new HashMap<String, Class<?>>();
        
        // Loop through all the class files in the com.goldstandard.model package
        for (File classFile : jpaClassFileDir.listFiles()) {
            Matcher matcher = GENERATED_CLASS_PATTERN.matcher(classFile.getCanonicalPath());
            if (matcher.matches()) {
                String packageName = matcher.group(1).replace('/', '.');
                String className = matcher.group(2);
                
                // Load up the generated class and add it to the data provider
                Class<?> generatedClass = Class.forName(packageName + "." + className);
                if (!generatedClass.isEnum()) {
                    generatedClassList.add(generatedClass);
                    generatedClassNameMap.put(generatedClass.getAnnotation(Table.class).name(), generatedClass);
                } else {
                    generatedEnumNameMap.put(className, generatedClass);
                }
            }
        }
        
        assertTrue(generatedClassList.size() > 0, "Did not find any generated classes");
    }
    
    @DataProvider
    protected Object[][] generatedClassProvider() {
        if (generatedClassArray == null) {
            
            // Load the generated classes into the form used by a TestNG DataProvider
            generatedClassArray = new Object[generatedClassList.size()][1];
            for (int i = 0; i < generatedClassList.size(); i++) {
                generatedClassArray[i] = new Object[] { generatedClassList.get(i) };
            }
        }
        
        return generatedClassArray;
    }
    
    protected List<Class<?>> generatedClassList() {
        return generatedClassList;
    }
    
    protected Map<String, Class<?>> generatedClassNameMap() {
        return generatedClassNameMap;
    }
    
    protected Map<String, Class<?>> generatedEnumNameMap() {
        return generatedEnumNameMap;
    }
    
    @SuppressWarnings("unchecked")
    protected String getEntityQueryName(Class generatedClass) {
        assertTrue(generatedClass.isAnnotationPresent(Entity.class),
                "Could not find @Entity annotation on " + generatedClass.getName());
        
        String entityQueryName = ((Entity) generatedClass.getAnnotation(Entity.class)).name();
        if (entityQueryName == null) entityQueryName = generatedClass.getSimpleName();
        
        return entityQueryName;
    }
    
    protected void persist(EntityManager entityManager, Object entity) {
        EntityTransaction tx = entityManager.getTransaction();
        if (tx.isActive()) {
            entityManager.joinTransaction();
        } else {
            tx.begin();
        }
        
        try {
            entityManager.persist(entity);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
        }
        
        if (entity instanceof BaseForceObject) {
            assertNotNull(((BaseForceObject) entity).getId(), entity.getClass().getName() + " ID was not generated.");
        }
    }
    
    protected void merge(EntityManager entityManager, Object entity) {
        EntityTransaction tx = entityManager.getTransaction();
        if (tx.isActive()) {
            entityManager.joinTransaction();
        } else {
            tx.begin();
        }
        
        try {
            entityManager.merge(entity);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
        }
    }
    
    protected void delete(EntityManager entityManager, Class entity, String where) {
        EntityTransaction tx = entityManager.getTransaction();
        if (tx.isActive()) {
            entityManager.joinTransaction();
        } else {
            tx.begin();
        }
        
        try {
            entityManager.createQuery("delete from " + getEntityQueryName(entity) + where).executeUpdate();
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
        }
    }
    
    protected static boolean isRequiredMethod(Method method) {
        Basic basicAnnotation = method.getAnnotation(Basic.class);
        return basicAnnotation != null && !basicAnnotation.optional();
    }
}
