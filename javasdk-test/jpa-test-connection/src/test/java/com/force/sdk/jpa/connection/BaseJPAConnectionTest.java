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

package com.force.sdk.jpa.connection;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.persistence.EntityManager;

import org.datanucleus.ObjectManager;
import org.datanucleus.store.connection.ConnectionFactory;
import org.testng.annotations.BeforeClass;

import com.force.sdk.jpa.ForceManagedConnection;
import com.force.sdk.qa.util.PropsUtil;
import com.force.sdk.qa.util.UserInfo;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

/**
 * Base class for Force.com JPA connection functional tests.
 *
 * @author Tim Kral
 */
public class BaseJPAConnectionTest {

    protected UserInfo userInfo;
    
    @BeforeClass
    public void classSetUp() throws Exception {
        // Get the userInfo from the force-sdk-test.properties on the classpath
        userInfo = UserInfo.loadFromPropertyFile(PropsUtil.FORCE_SDK_TEST_NAME);
    }
    
    protected String createConnectionUrl() {
        // Strip out protocol, if it exists
        String[] parsedEndPoint = userInfo.getServerEndpoint().split("://");
        
        StringBuffer sb = new StringBuffer();
        sb.append("force://").append(parsedEndPoint[parsedEndPoint.length - 1])
            .append("?user=").append(userInfo.getUserName())
            .append("&password=").append(userInfo.getPassword());
        
        return sb.toString();
    }
    
    protected void verifyEntityManager(EntityManager em) throws ConnectionException {
        PartnerConnection conn = getPartnerConnection(em);
        
        GetUserInfoResult userInfoResult = conn.getUserInfo();
        assertNotNull(userInfoResult);
        
        assertEquals(userInfoResult.getOrganizationId(), userInfo.getOrgId());
        assertEquals(userInfoResult.getUserId(), userInfo.getUserId());
        assertEquals(userInfoResult.getUserName(), userInfo.getUserName());
    }
    
    protected ForceManagedConnection getManagedConnection(EntityManager em) {
        ObjectManager om = (ObjectManager) em.getDelegate();
        
        ConnectionFactory connFactory = om.getStoreManager().getConnectionManager().lookupConnectionFactory("force");
        return (ForceManagedConnection) connFactory.createManagedConnection(null, null);
    }
    
    protected MetadataConnection getMetadataConnection(EntityManager em) throws ConnectionException {
        return getManagedConnection(em).getMetadataConnection();
    }
    
    protected PartnerConnection getPartnerConnection(EntityManager em) {
        return (PartnerConnection) getManagedConnection(em).getConnection();
    }
}
