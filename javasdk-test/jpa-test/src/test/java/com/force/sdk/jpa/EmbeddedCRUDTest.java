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

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.jpa.sample.*;
import com.force.sdk.jpa.sample.PhoneNumber.Phonetype;
import com.force.sdk.qa.util.BaseMultiEntityManagerJPAFTest;

/**
 * 
 * CRUD test for Embedded entities.
 *
 * @author Fiaz Hossain
 */
public class EmbeddedCRUDTest extends BaseMultiEntityManagerJPAFTest {

    @Test
    public void testCRUDForEmbedded() {
        testCRUDForEmbeddedInternal(em);
    }

    @Test
    public void testCRUDForEmbeddedOptimistic() {
        testCRUDForEmbeddedInternal(em2);
    }
    
    @Test
    public void testCRUDForEmbeddedAllOrNothing() {
        testCRUDForEmbeddedInternal(em3);
    }
    
    public void testCRUDForEmbeddedInternal(EntityManager entityManager) {
        deleteAll("EmployeeEntity");
        
        Employee emp = new Employee();
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
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        entityManager.persist(emp);
        entityManager.persist(p2);
        entityManager.persist(pe2);
        entityManager.persist(p1);
        entityManager.persist(pe1);
        entityManager.persist(office);
        entityManager.persist(cell);
        entityManager.persist(address);
        tx.commit();
        
        tx.begin();
        List<Employee> results = entityManager.createQuery("Select t From EmployeeEntity t").getResultList();
        emp = results.iterator().next();
        
        Assert.assertNotNull(emp.getId(), emp.getClass().getName() + " ID was not generated.");
        Assert.assertTrue(entityManager.contains(emp), "contains() failed.");
        
        Employee empFromDb = entityManager.find(Employee.class, emp.getId());
        Assert.assertNotNull(empFromDb,
                "The entity was not stored to the database.");
        Assert.assertEquals(empFromDb.getEmploymentPeriod().getStartDate(), emp.getEmploymentPeriod().getStartDate(),
                "Start date not same");
        tx.commit();
        // Reset Employee to last read
        emp = empFromDb;
        
        // Now we need to make sure update works on embedded entities
        Calendar endDate = JPATestUtils.getCalendar(false);
        endDate.add(Calendar.DAY_OF_YEAR, 90);
        emp.getEmploymentPeriod().setEndDate(endDate.getTime());
        tx.begin();
        entityManager.merge(emp);
        tx.commit();
        
        // Read back upated object and make sure endDate is updated
        tx.begin();
        try {
            emp = entityManager.find(Employee.class, emp.getId());
            Assert.assertEquals(emp.getEmploymentPeriod().getEndDate(), endDate.getTime(),
                    "End date not same");
        } finally {
            tx.commit();
        }
    }
}
