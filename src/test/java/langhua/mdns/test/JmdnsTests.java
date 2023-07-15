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

import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;

public class JmdnsTests extends TestCase {
    private static final String MODULE = JmdnsTests.class.getName();

    private JmdnsThread jmdnsThread;

    private static final String JMDNS_THREAD_NAME = "sandflower-jmdns-thread";

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(JmdnsTests.class);
    }

    /**
     * Test entrance.
     *
     * @throws Exception
     */
    public void testAll() throws Exception {
        initialJmdnsThread();
        Thread.sleep(3000);
        scanJmdns();
        Debug.logInfo("Now sleep about 10 minutes, you may proof the above mdns.", MODULE);
        Thread.sleep(600000);
        jmdnsThread.interrupt();
    }

    private void scanJmdns() throws InterruptedException, IOException {
        Debug.logInfo("Start to scan mdns ...", MODULE);
        JmmDNS registry = JmmDNS.Factory.getInstance();
        Thread.sleep(10000);
        ServiceInfo[] httpSiArray = registry.list("_http._tcp.local.");
        for (ServiceInfo si : httpSiArray) {
            if (si.getInetAddresses().length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("[");
                boolean isFirst = true;
                for (InetAddress ia : si.getInetAddresses()) {
                    if (!isFirst) {
                        sb.append(", ");
                    } else {
                        isFirst = false;
                    }
                    sb.append(ia.getHostAddress());
                    sb.append(":");
                    sb.append(si.getPort());
                }
                sb.append("]");
                Debug.logInfo("-- Service Info found: type:" + si.getType() + ", name:" + si.getName() + ", ip:" + sb.toString(), MODULE);
                Debug.logInfo("---- You can try \"ping " + si.getName() + "\" in a terminal.", MODULE);
            }
        }
        registry.close();
    }

    /**
     * Initial a JmdnsThread.
     */
    protected void initialJmdnsThread() {
        Debug.logInfo("Initial Jmdns thread ...", MODULE);
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
