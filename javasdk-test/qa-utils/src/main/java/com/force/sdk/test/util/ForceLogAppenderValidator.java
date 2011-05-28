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

import java.util.regex.Pattern;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.testng.Assert;

/**
 * 
 * Utility class that validates log messages.
 *
 * @author Fiaz Hossain
 */
public class ForceLogAppenderValidator extends AppenderSkeleton {

    private Pattern[] expectedLogPattterns;
    private int current;
    
    public ForceLogAppenderValidator(String[] expectedLogs) {
        initPatterns(expectedLogs);
    }

    public ForceLogAppenderValidator(boolean isActive, String[] expectedLogs) {
        super(isActive);
        initPatterns(expectedLogs);
    }

    private void initPatterns(String[] expectedLogs) {
        expectedLogPattterns = expectedLogs != null ? new Pattern[expectedLogs.length] : new Pattern[0];
        for (int i = 0; i < expectedLogPattterns.length; i++) {
            expectedLogPattterns[i] = Pattern.compile(expectedLogs[i]);
        }
    }
    
    @Override
    public void close() {
        current = 0;
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    public boolean finishedPatterns() {
        return this.current == expectedLogPattterns.length;
    }
    
    @Override
    protected void append(LoggingEvent event) {
        if (event != null && expectedLogPattterns.length > 0) {
            Pattern p = expectedLogPattterns[current];
            if (!p.matcher(event.getRenderedMessage()).find()) {
                Assert.fail("Log message at postion: " + current + " did not match actual: "
                            + event.getRenderedMessage() + " expected: " + p.pattern());
            }
            current++;
        }
    }
}
