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

package com.force.sdk.test.util;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * 
 * Utility to load properties from different sources.
 *
 * @author Dirk Hain
 */
public final class PropsUtil {

    public static final String FORCE_SDK_TEST_NAME = "force-sdk-test";
    public static final String FORCE_SDK_TEST_PROPS = "force-sdk-test.properties";
    
    public static final String FORCE_USER_PROP = "user";
    public static final String FORCE_PWD_PROP = "password";
    public static final String FORCE_EP_PROP = "endpoint";
    public static final String FORCE_APIV_PROP = "force.apiVersion";
    public static final String FORCE_PROT_PROP = "endpoint.protocol";
    
    private PropsUtil() {  }
    
    /**
     * Utility to load a properties file from the classpath.
     * @param propertiesName name of the properties file. The file needs to be on the classpath.
     */
    public static Properties load(String propertiesName) throws IOException {
        Properties p = new Properties();
        URL url = ClassLoader.getSystemResource(propertiesName);
        if (url == null) {
            throw new FileNotFoundException("Properties file not on classpath: " + propertiesName);
        }
        
        InputStream is = url.openStream();
        try {
            p.load(is);
        } finally {
            is.close();
        }
        return p;
    }
}
