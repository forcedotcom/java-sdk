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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import mockit.*;

import org.testng.annotations.Test;

import com.force.sdk.qa.util.PropsUtil;
import com.force.sdk.qa.util.TestContext;
import com.force.sdk.qa.util.UserInfo;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * One aspect of performance testing that counts
 * certain method invocations during schema loading.
 * 
 * @author Tim Kral
 */
public class SchemaLoadInvocationFTest {
    
    @Test
    public void testSchemaCreateLoginInvocations() throws ConnectionException, IOException {
        TestContext.get().setUserInfo(getClass().getName(), UserInfo.loadFromPropertyFile(PropsUtil.FORCE_SDK_TEST_NAME));
        UserInfo userInfo = TestContext.get().getUserInfo();

        ConnectorConfig config = new ConnectorConfig();
        config.setManualLogin(true);
        config.setServiceEndpoint(userInfo.getServerEndpoint());

        PartnerConnection conn = new PartnerConnection(config);

        // Login to the org.  The LoginResult will be returned by the login counter mock
        // which simply keeps track of the number of login calls made.
        LoginResult lr = conn.login(userInfo.getUserName(), userInfo.getPassword());
        PartnerConnectionWithLoginCounter connWithLoginCounter = new PartnerConnectionWithLoginCounter(lr);
        Mockit.setUpMock(PartnerConnection.class, connWithLoginCounter);

        try {
            EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("SchemaLoadInvocationFTest",
                                                        userInfo.getUserinfoAsPersistenceunitProperties());
            emf.createEntityManager();

            // Assert that during schema creation, we only logged in once
            assertEquals(connWithLoginCounter.getLoginCount(), 1,
                    "Unexpected number of PartnerConnection logins during schema creation.");
        } finally {
            Mockit.tearDownMocks();
        }
    }

    /**
     * Mock Force.com API connection which counts the
     * number of API logins.
     * 
     * @author Tim Kral
     */
    @MockClass(realClass = PartnerConnection.class)
    public static class PartnerConnectionWithLoginCounter {

        private int loginCount = 0;
        private final LoginResult loginResult;

        public PartnerConnectionWithLoginCounter(LoginResult loginResult) {
            assertNotNull(loginResult, "Cannot construct PartnerConnectionWithLoginCounter with null LoginResult");
            this.loginResult = loginResult;
        }

        @Mock
        public LoginResult login(String username, String password) throws ConnectionException {
            loginCount++;
            return loginResult;
        }

        int getLoginCount() {
            return loginCount;
        }
    }
}
