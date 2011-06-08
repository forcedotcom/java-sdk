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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.force.sdk.jpa.ForceManagedConnection;
import com.force.sdk.jpa.schema.ForceSchemaWriter;
import com.force.sdk.jpa.schema.SchemaDeleteProperty;
import com.sforce.soap.metadata.CustomField;
import com.sforce.soap.metadata.CustomObject;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectorConfig;
import org.testng.Assert;

/**
 * Testing util for SFDC specific needs.
 * 
 * @author Jeff Lai
 */
public final class SfdcTestingUtil {

    private static Set<String> customFieldsToKeep;

    static {
        String namespace = null;
        try {
            namespace = PropsUtil.load(PropsUtil.FORCE_SDK_TEST_PROPS).getProperty("force.namespace");
        } catch (IOException e) {
            Assert.fail("Failed to load test properties");
        }
        namespace = (namespace == null || "".equals(namespace)) ? "" : namespace + "__";
        customFieldsToKeep = new HashSet<String>();
        customFieldsToKeep.add(("Opportunity." + namespace + "TrackingNumber__c").toLowerCase());
        customFieldsToKeep.add(("Account." + namespace + "SLA__c").toLowerCase());

    }

    private SfdcTestingUtil() { }
    
    /**
     * getPartnerConnection
     * This method returns a partner connection for a given UserInfo object.
     * @param user is the UserInfo object
     * @return PartnerConnection
     * @throws Exception
     */
    public static PartnerConnection getPartnerConnection(UserInfo user) throws Exception {
        ConnectorConfig conf = new ConnectorConfig();
        conf.setAuthEndpoint(user.serverEndpoint);
        conf.setUsername(user.getUserName());
        conf.setPassword(user.getPassword());
        PartnerConnection conn = Connector.newConnection(conf);
        return conn;
    }

    public static void cleanSchema(ForceManagedConnection mconn) throws Exception {
        PartnerConnection pc = (PartnerConnection) mconn.getConnection();
        ForceSchemaWriter writer = new ForceSchemaWriter(new SchemaDeleteProperty(true, true));
        DescribeGlobalResult objs = pc.describeGlobal();
        for (DescribeGlobalSObjectResult s : objs.getSobjects()) {
            CustomObject co = new CustomObject();
            co.setFullName(s.getName());
            DescribeSObjectResult sobject = pc.describeSObject(s.getName());
            Field[] fields = sobject.getFields();
            List<CustomField> customFields = new ArrayList<CustomField>();
            for (Field f : fields) {
                if (f.isCustom()) {
                    CustomField cf = new CustomField();
                    cf.setFullName(f.getName());
                    if (!s.isCustom()) {
                        if (!customFieldsToKeep.contains((s.getName() + "." + f.getName()).toLowerCase())) {
                            writer.addCustomField(co, cf);
                        }
                    } else {
                        writer.addCustomField(co, cf);
                        customFields.add(cf);
                    }
                }
            }
            if (customFields.size() > 0) {
                co.setFields(customFields.toArray(new CustomField[customFields.size()]));
            }
            if (s.isCustom()) {
                writer.addCustomObject(co, true);
            }
        }
        writer.write(mconn);
    }

}
