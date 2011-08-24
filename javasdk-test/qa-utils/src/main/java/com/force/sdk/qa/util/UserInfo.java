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

import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.jpa.PersistenceProviderImpl;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import org.testng.Assert;

import java.io.IOException;
import java.util.HashMap;

import static org.testng.Assert.assertNotNull;

/**
 * 
 * Immutable set of user information that allows to establish context for this user and retrieve any
 * additional information. This is NOT a minimal set of information. Changes to this class, 
 * i.e. addition of user information should be added to the equals implementation.
 *
 * @author Dirk Hain
 */
public class UserInfo {

    /**
     * Datanucleus property name for Force.com endpoint.
     */
    public static final String DN_CONN_URL_PROP = "datanucleus.ConnectionURL";
    /**
     * Datanucleus property name for Force.com username.
     */
    public static final String DN_CONN_USERNAME_PROP = "datanucleus.ConnectionUserName";
    /**
     * Datanucleus property name for Force.com password.
     */
    public static final String DN_CONN_PASSWORD_PROP = "datanucleus.ConnectionPassword";
            
    //Immutables
    protected final String orgId; //optional
    protected final String userId; //optional
    protected final String userName;
    protected final String password;
    protected final String serverEndpoint;


    /**
     * Loads property file from classpath and sets property file values in a new UserInfo object.
     * @param propertyFileName name of properties file
     * @return UserInfo
     * @throws ConnectionException ConnectionException
     * @throws IOException IOException
     */
    public static UserInfo loadFromPropertyFile(String propertyFileName) throws ConnectionException, IOException {
        // Load up the connection properties on the classpath

        ForceServiceConnector connector = new ForceServiceConnector(PropsUtil.getUrlFromProperties(
                PropsUtil.load(propertyFileName)));
        PartnerConnection conn = connector.getConnection();
        
        assertNotNull(conn, "Unable to establish API connection. See " + propertyFileName);
        
        // Get the user information from the API connection
        GetUserInfoResult userInfoResult = conn.getUserInfo();
        assertNotNull(userInfoResult, "Unable to retrieve user info. See " + propertyFileName);
        
        return new UserInfo(userInfoResult.getOrganizationId(),
                            userInfoResult.getUserId(),
                            conn.getConfig().getUsername(),
                            conn.getConfig().getPassword(),
                            conn.getConfig().getServiceEndpoint());
    }
    
    /**
     * Constructor for UserInfo object.
     * @param oId Organization id
     * @param uId User id
     * @param uName username
     * @param pwd password
     * @param serverEP Force.com endpoint
     */
    public UserInfo(String oId, String uId, String uName, String pwd, String serverEP) {
        orgId = oId; //optional
        userId = uId; //optional
        Assert.assertNotNull(uName, "Username not specified.");
        userName = uName;
        Assert.assertNotNull(pwd, "Password not specified.");
        password = pwd;
        Assert.assertNotNull(serverEP, "Server Endpoint not specified.");
        serverEndpoint = normalizeServerEndpoint(serverEP);
    }

    /**
     * Helper to obtain the user information in a format suitable to pass to a EMF for instance.
     * @return HashMap<String,Object> containing persistence unit properties
     */
    public HashMap<String, Object> getUserinfoAsPersistenceunitProperties() {
        HashMap<String, Object> propsMap = new HashMap<String, Object>();
        propsMap.put(DN_CONN_USERNAME_PROP, getUserName());
        propsMap.put(DN_CONN_PASSWORD_PROP, getPassword());
        propsMap.put(DN_CONN_URL_PROP, getServerEndpoint());
        propsMap.put("javax.persistence.provider", PersistenceProviderImpl.class.getName());
        return propsMap;
    }
    
    
    public String getServerEndpoint() {
        return serverEndpoint;
    }
        
    public String getOrgId() {
        return orgId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    /**
     * For some reason the XMLRPC API call to create an org returns a server URL with the newly created OrgID appended.
     * For consistency reasons this gets normalized (chopped off) here
     */
    private String normalizeServerEndpoint(String fullyQualifiedserverEndpoint) {
        int orgIdIndex = fullyQualifiedserverEndpoint.indexOf("00D");
        return orgIdIndex > -1 ? fullyQualifiedserverEndpoint.substring(0, orgIdIndex - 1) : fullyQualifiedserverEndpoint;
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof UserInfo)) return false;
        UserInfo thatUInfo = (UserInfo) that;
        return (orgId.equals(thatUInfo.orgId))
                && (userId.equals(thatUInfo.userId))
                && (userName.equals(thatUInfo.userName))
                && (password.equals(thatUInfo.password))
                && (serverEndpoint.equals(thatUInfo.serverEndpoint));
    }
    
    @Override
    public int hashCode() {
        StringBuffer hashString = new StringBuffer(orgId);
        hashString.append('.').append(userId)
                  .append('.').append(userName)
                  .append('.').append(password)
                  .append('.').append(serverEndpoint);
        
        return hashString.toString().hashCode();
    }
    
}
