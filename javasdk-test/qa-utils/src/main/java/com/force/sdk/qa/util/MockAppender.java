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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Mock log appender which caches log lines received.
 * 
 * @author Tim Kral
 */
public class MockAppender extends AppenderSkeleton {
    final AtomicBoolean receivedLogLine = new AtomicBoolean(false);
    final String expectedLogLine;
    private int logLineTimes = 0;
    
    /**
     * Constructor for Mock Appender.
     * @param expectedLogLine
     */
    public MockAppender(String expectedLogLine) {
        this.expectedLogLine = expectedLogLine;
    }
    
    @Override
    public boolean requiresLayout() {
        return false;
    }
    
    @Override
    public void close() {  }
    
    @Override
    protected void append(LoggingEvent event) {
        if (event != null && event.getRenderedMessage().contains(expectedLogLine)) {
            receivedLogLine.set(true);
            logLineTimes++;
        }
    }
    
    /**
     * Check if expected log line was received.
     * @return true if log line received, otherwise false
     */
    public boolean receivedLogLine() {
        return receivedLogLine.get();
    }
    
    /**
     * Return the number of times expected log line was received
     * @return int
     */
    public int getLogLineTimes() {
        return logLineTimes;
    }
    
}
