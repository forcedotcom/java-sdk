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

package com.force.sdk.jpa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.force.sdk.jpa.sample.Address;
import com.force.sdk.jpa.sample.CovertProject;
import com.force.sdk.jpa.sample.Employee;
import com.force.sdk.jpa.sample.EmploymentPeriod;
import com.force.sdk.jpa.sample.GovernmentProject;
import com.force.sdk.jpa.sample.PhoneNumber;
import com.force.sdk.jpa.sample.PhoneNumber.Phonetype;
import com.force.sdk.jpa.sample.Project;
import com.force.sdk.jpa.sample.ProjectEmployee;
import com.force.sdk.test.util.BaseTransactionalSpringContextJPAFTest;

/**
 * 
 * Tests around creating entities via JPA. The tests use a sample data model from the JPA spec 
 * JSR 220: Enterprise JavaBeansTM,Version 3.0 that was adjusted for supported features.
 *
 * @author Dirk Hain
 */
public class SpringCreateTest extends BaseTransactionalSpringContextJPAFTest {

    Employee emp = null;
    
    @Test
    @Transactional
    @Rollback(false)
    /**
     * Spring based test creating an entity hierarchy.
     * This test creates an instance of an entity hierarchy containing most of the supported JPA elements. The 
     * sample is derived from the JSR 220 spec 'complex example'. Newly supported features should be modeled into
     * the example entities.
     * @hierarchy javasdk
     * @userStory xyz
     */
    public void testCreateSample() {
        PlatformTransactionManager txMgr = (PlatformTransactionManager) applicationContext.getBean("transactionManager");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus txStatus = txMgr.getTransaction(def);

        emp = new Employee();
        emp.setSalary(Long.valueOf(100));
        emp.setName("Sample Employee");
        
        Address address = new Address();
        address.setCity("San Francisco");
        address.setStreet("One Market St. #300");
        address.setEmployee(emp);
        
        PhoneNumber office = PhoneNumber.init(emp, "415-555-0000", Phonetype.OFFICE);
        office.setEmployee(emp);
        PhoneNumber cell = PhoneNumber.init(emp, "415-555-1111", Phonetype.CELL);
        cell.setEmployee(emp);
        
        EmploymentPeriod empPer = new EmploymentPeriod();
        empPer.setStartDate(JPATestUtils.getCalendar(false).getTime());
        empPer.setEndDate(JPATestUtils.getCalendar(false).getTime());
        emp.setEmploymentPeriod(empPer);

        Project p1 = new GovernmentProject();
        p1.setName("GovernmentProject");
        Project p2 = new CovertProject("classA");
        p2.setName("CovertProject");
        ProjectEmployee pe1 = new ProjectEmployee();
        pe1.setEmployee(emp);
        pe1.setProject(p1);
        ProjectEmployee pe2 = new ProjectEmployee();
        pe2.setEmployee(emp);
        pe2.setProject(p2);
        
        //TODO create another employee and assign to govt. and verify collection type props
        
        entityManager.persist(emp);
        entityManager.persist(p2);
        entityManager.persist(pe2);
        entityManager.persist(p1);
        entityManager.persist(pe1);
        entityManager.persist(office);
        entityManager.persist(cell);
        entityManager.persist(address);
                
        txMgr.commit(txStatus);
        
        Metamodel m = entityManager.getMetamodel();
        EntityType<Employee> etype = m.entity(Employee.class); //deliberately a little verbose here
        Query q = entityManager.createQuery("Select t From " + etype.getName() + " t");
        @SuppressWarnings("rawtypes")
        List results = q.getResultList();
        Employee employee = (Employee) results.iterator().next();
        
        Assert.assertNotNull(employee.getId(), employee.getClass().getName() + " ID was not generated.");
        Assert.assertTrue(entityManager.contains(employee), "contains() failed.");

        txStatus = txMgr.getTransaction(def);
        Employee empFromDb = entityManager.find(Employee.class, employee.getId());
        Assert.assertNotNull(empFromDb,
                "The entity was not stored to the database.");
        Assert.assertEquals(empFromDb.getEmploymentPeriod().getStartDate(), employee.getEmploymentPeriod().getStartDate(),
                "Start date not same");
        
        // Now we need to make sure update works on embedded entities
        Calendar endDate = JPATestUtils.getCalendar(false);
        endDate.add(Calendar.DAY_OF_YEAR, 90);
        empFromDb.getEmploymentPeriod().setEndDate(endDate.getTime());
        
        entityManager.merge(empFromDb);
        txMgr.commit(txStatus);
        entityManager.clear();
        
        // Read back upated object and make sure endDate is updated
        empFromDb = entityManager.find(Employee.class, employee.getId());
        Assert.assertEquals(empFromDb.getEmploymentPeriod().getEndDate(), endDate.getTime(),
                "End date not same");
    }
    
    
    @AfterMethod
    protected void tearDown() {
        deleteAll(new String[]{CovertProject.class.getSimpleName(),
                GovernmentProject.class.getSimpleName(), ProjectEmployee.class.getSimpleName(),
                PhoneNumber.class.getSimpleName(), Address.class.getSimpleName(),
                "EmployeeEntity"}, entityManager);
    }

    /**
     * Helper to delete all entities of any of the types supplied in entities in the org 
     * referenced by {@link EntityManager} em.
     * @param entities entity types to delete
     * @param em references the org where the delete should happen
     */
    public static <T> void deleteAll(Class<T>[] entities, EntityManager em) {
        ArrayList<String> entityNames = new ArrayList<String>();
        for (Class<T> e : entities) {
            entityNames.add(e.getClass().getSimpleName());
        }
        deleteAll(entityNames.toArray(new String[entityNames.size()]), em);
    }
    
    /**
     * Helper to delete all entities of any of the types supplied in entityNames in th org
     * referenced {@link EntityManager} em.
     *  
     * @param entityNames entity types to delete
     * @param em references the org where the delete should happen
     */
    @Transactional
    @Rollback(false)
    public static void deleteAll(String[] entityNames, EntityManager em) {
        for (String entityName : entityNames) {
            em.createQuery("delete from " + entityName).executeUpdate();
        }
    }
}
