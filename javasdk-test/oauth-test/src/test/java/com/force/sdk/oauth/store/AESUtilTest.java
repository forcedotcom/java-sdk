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

package com.force.sdk.oauth.store;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.force.sdk.oauth.context.store.*;
import com.force.sdk.test.util.ForceLogAppenderValidator;

/**
 * 
 * Test the AESUtil class. This tests exercises the encryption.
 *
 * @author John Simone
 */
public class AESUtilTest {

    private static final String ENCRYPTION_TEST_VALUE = "This is a test string";
    
    @Test
    public void testEncryption() throws Exception {
        // Generate the key
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        SecretKey skey = kgen.generateKey();
        byte[] key = skey.getEncoded();
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        
        byte[] encryptedValue =
            AESUtil.encrypt(ENCRYPTION_TEST_VALUE.getBytes(), skeySpec);
        byte[] decryptedValue =
            AESUtil.decrypt(encryptedValue, skeySpec);
        
        Assert.assertEquals(new String(decryptedValue),
                ENCRYPTION_TEST_VALUE, "Values do not match after being encrypted and decrypted");
    }
    
    @Test
    public void testKeyGenerationDefault() throws ForceEncryptionException {
        SecretKeySpec key = AESUtil.getSecretKey();
        Assert.assertNotNull(key);
    }
    
    @Test
    public void testKeyFromFile128() throws ForceEncryptionException {
        SecretKeySpec key = AESUtil.getSecretKey("key-file_128.properties");
        String encodedKey = SecurityContextCookieStore.b64encode(key.getEncoded());
        Assert.assertEquals(encodedKey, "WIxR4rQdz1fKe58kmK3ZvA==");
    }
    
    @Test
    public void testKeyFromFile192() throws ForceEncryptionException {
        SecretKeySpec key = AESUtil.getSecretKey("key-file_192.properties");
        String encodedKey = SecurityContextCookieStore.b64encode(key.getEncoded());
        Assert.assertEquals(encodedKey, "8rvHhcEu1cCByZQC4kGGhkf735BHz7Ii");
    }
    
    @Test
    public void testKeyFromFile256() throws ForceEncryptionException {
        SecretKeySpec key = AESUtil.getSecretKey("key-file_256.properties");
        String encodedKey = SecurityContextCookieStore.b64encode(key.getEncoded());
        Assert.assertEquals(encodedKey, "rzK2DHHJSxS9U2Vq7SMoC4mGkW8VPVI3XclwjcBkV7k=");
    }
    
    @Test
    public void testKeyFromIncorrectFileName() throws ForceEncryptionException {
        String[] testIncorrectFileName = {
                "Could not open file at  path incorrect-file-name. Generating private key...",
            };
        Logger logger = Logger.getLogger(AESUtil.class);
        Level origLevel = logger.getLevel();
        logger.setLevel(Level.WARN);
        ForceLogAppenderValidator appender = new ForceLogAppenderValidator(testIncorrectFileName);
        logger.addAppender(appender);
        
        try {
            logger.addAppender(appender);
            SecretKeySpec key = AESUtil.getSecretKey("incorrect-file-name");
            Assert.assertNotNull(key);
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(origLevel);
        }

        Assert.assertTrue(appender.finishedPatterns());
    }
    
    @Test
    public void testKeyFromIncorrectPropertyName() throws ForceEncryptionException {
        String[] testIncorrectFileName = {
                "private-key property was null in file funcconnurl.properties. Generating private key..."
            };
        
        Logger logger = Logger.getLogger(AESUtil.class);
        Level origLevel = logger.getLevel();
        logger.setLevel(Level.WARN);
        ForceLogAppenderValidator appender = new ForceLogAppenderValidator(testIncorrectFileName);
        logger.addAppender(appender);
        
        try {
            logger.addAppender(appender);
            SecretKeySpec key = AESUtil.getSecretKey("funcconnurl.properties");
            Assert.assertNotNull(key);
        } finally {
            logger.removeAppender(appender);
            logger.setLevel(origLevel);
        }

         Assert.assertTrue(appender.finishedPatterns());
    }
    
    @Test
    public void testWrongKeyEncryption() throws NoSuchAlgorithmException, ForceEncryptionException {
        // Generate the key
        
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        SecretKey skey = kgen.generateKey();
        byte[] key = skey.getEncoded();
        
        byte[] encryptedValue =
            AESUtil.encrypt(ENCRYPTION_TEST_VALUE.getBytes(), new SecretKeySpec(key, "AES"));
        
        // Generate another key
        kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        skey = kgen.generateKey();
        key = skey.getEncoded();
        
        try {
            AESUtil.decrypt(encryptedValue, new SecretKeySpec(key, "AES"));
            Assert.fail("ForceEncryptionException was expected as different key is used for decryption. ");
        } catch (ForceEncryptionException e) {
            // Expected
        }
    }

}
