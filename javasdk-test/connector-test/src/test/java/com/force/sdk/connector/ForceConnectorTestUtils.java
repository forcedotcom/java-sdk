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

package com.force.sdk.connector;

import java.io.*;
import java.util.Properties;

/**
 * Class for shared Force.com connector test utils.
 *
 * @author Tim Kral
 */
public final class ForceConnectorTestUtils {

    private ForceConnectorTestUtils() {  }
    
    public static void createCliforceConn(String connectionName, String connectionUrl) throws IOException {
        
        // Setup the CLIForce connection file to be in the current working directory.
        // This should be a test directory as the file will eventually get deleted. 
        ForceConnectorUtils.cliforceConnFile = new File(System.getProperty("user.dir") + "/cliforce_urls_test");
        
        if (!ForceConnectorUtils.cliforceConnFile.exists()) {
            // Create the cliforce connections file
            File cliforceConnFileDir = ForceConnectorUtils.cliforceConnFile.getParentFile();
            if (!cliforceConnFileDir.exists())
                cliforceConnFileDir.mkdirs();
            
            ForceConnectorUtils.cliforceConnFile.createNewFile();
            ForceConnectorUtils.cliforceConnFile.deleteOnExit();
        }
        
        InputStream is = null;
        Writer writer = null;
        try {
            // Read in the cliforce connection urls
            is = new FileInputStream(ForceConnectorUtils.cliforceConnFile);
            Properties cliforceConnUrls = new Properties();
            cliforceConnUrls.load(is);
            
            // Add the new url property
            // Note: We should always do this in case the url changes
            // (e.g. in the case of static org changes)
            cliforceConnUrls.put(connectionName, connectionUrl);
            
            // Write out the cliforce connection urls
            writer = new FileWriter(ForceConnectorUtils.cliforceConnFile);
            cliforceConnUrls.store(writer, null/*comments*/);
        } finally {
            if (writer != null) writer.close();
            if (is != null) is.close();
        }
    }
}
