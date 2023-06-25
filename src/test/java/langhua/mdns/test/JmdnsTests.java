/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package langhua.mdns.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import langhua.mdns.common.JmdnsThread;
import org.apache.ofbiz.base.util.Debug;

public class JmdnsTests extends TestCase {
    private static final String MODULE = JmdnsTests.class.getName();

    protected JmdnsThread jmdnsThread;

    private static final String JMDNS_THREAD_NAME = "sandflower-jmdns-thread";

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(JmdnsTests.class);
    }

    public void testAll() throws Exception {
        initialJmdnsThread();
        Thread.sleep(3000);
        jmdnsThread.interrupt();
    }

    protected void initialJmdnsThread() {
        Debug.logInfo("Initial TCP threads and devices ...", MODULE);
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int i = threadGroup.activeCount();
        Debug.logInfo("--- thread group[" + threadGroup.getName() + "] has [" + i + "] threads.", MODULE);
        Thread[] threads = new Thread[i];
        threadGroup.enumerate(threads, true);
        for (Thread threadInstance : threads) {
            if (threadInstance instanceof JmdnsThread thread) {
                if (thread.getName().equals(JMDNS_THREAD_NAME)) {
                    Debug.logInfo("--- Found sandflower jmdns thread", MODULE);
                    jmdnsThread = thread;
                }
            }
        }

        if (jmdnsThread == null) {
            jmdnsThread = new JmdnsThread(threadGroup, JMDNS_THREAD_NAME);
            jmdnsThread.start();
        }
    }
}