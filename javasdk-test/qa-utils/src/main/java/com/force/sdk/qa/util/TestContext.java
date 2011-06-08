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

package com.force.sdk.qa.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * Threadlocal test context for java sdk tests.
 *
 * @author Dirk Hain
 */
public final class TestContext implements Serializable {
    
    private static final long serialVersionUID = 2211885487131071483L;
    
    public static final String CREATE_NEW_ORG = "test.func.createNewOrg";
    public static final String CREATE_NEW_ORG_PER_TESTCLASS = "test.func.createNewOrgPerTestclass";
    public static final String PERSISTENCE_UNIT_NAME = "test.func.persistenceUnitName";
    public static final String LONG_REGEX = "[\\\\d]{" + (((Long) System.currentTimeMillis()).toString().length() + 4) + "}";
    
    /**
     * Tracks the type of test being run.
     * 
     * @author Jeff Lai
     */
    public static enum TestType {
        UNIT, FUNCTIONAL, INTEGRATION, INTEG_MOCK_SPRING_SECURITY, INTEG_ENDTOEND_SPRING_SECURITY
    }
    
    private volatile String testName;
    private transient volatile UserInfo userInfo; //last created org during this test run
    private Properties testrunProps;
    private TestType testType;
    private static ConcurrentHashMap<String, UserInfo> testOrgInfo = new ConcurrentHashMap<String, UserInfo>();
    
    private static final InheritableThreadLocal<TestContext> TLTC =
        new InheritableThreadLocal<TestContext>() {
            @Override
            protected TestContext initialValue() {
                TestContext tc = new TestContext();
                try {
                    tc.setTestProps(PropsUtil.load(PropsUtil.FORCE_SDK_TEST_PROPS));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return tc;
            }
        };

    /**
     * 
     */
    private TestContext() {  }
    
    /**
     * Get default test context.
     */
    public static TestContext get() {
        return TLTC.get() == null ? TestContext.get(PropsUtil.FORCE_SDK_TEST_PROPS) : TLTC.get();
    }
    
    /**
     * Get test context from specific properties file.
     * @param testPropsName Properties file on the classpath
     */
    public static TestContext get(String testPropsName) {
        release();
        
        TestContext tc = new TestContext();
        try {
            tc.setTestProps(PropsUtil.load(testPropsName));
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to load test properties " + testPropsName
                    + "\n" + ioe.getMessage()); // abort the test
        }
        
        TLTC.set(tc);
        return tc;
    }
    
    public static void set(TestContext tc) {
        TLTC.set(tc);
    }
    
    public static void release() {
        TLTC.set(null);
    }
    
    public Properties getTestProps() {
        return testrunProps;
    }
    
    public void setTestProps(Properties testProps) {
        this.testrunProps = testProps;
    }
    
    public void addTestProps(Properties testProps) {
        if (this.testrunProps == null) {
            this.testrunProps = new Properties();
        }
        
        this.testrunProps.putAll(testProps);
    }

    public String getTestName() {
        return testName;
    }
    
    public synchronized UserInfo getUserInfo() {
        return userInfo;
    }
    
    /**
     * Retrieves the userinfo for the specified test.
     * @param tname name of the test
     * @return associated user info or null
     */
    public synchronized UserInfo getUserInfo(String tname) {
        if (testName.equals(tname)) {
            return userInfo;
        } else {
            return testOrgInfo.get(tname);
        }
    }
    
    public synchronized void setUserInfo(String tname, UserInfo uInfo) {
        this.testName = tname;
        this.userInfo = uInfo;
        
        if (testName != null) testOrgInfo.put(tname, uInfo);
    }

    public String getPersistenceUnitName() {
        return testrunProps == null ? null : testrunProps.getProperty(PERSISTENCE_UNIT_NAME);
    }
    
    public void setTestType(TestType tType) {
        testType = tType;
    }

    public TestType getTestType() {
        return testType;
    }
    
    public void clearTestType() {
        testType = null;
    }
}
