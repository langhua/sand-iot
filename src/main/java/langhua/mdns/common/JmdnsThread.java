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
package langhua.mdns.common;

import langhua.mdns.services.JmdnsService;
import langhua.mdns.utils.JmdnsUtils;
import org.apache.ofbiz.base.util.Debug;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JmdnsThread extends Thread {
    private static final String MODULE = JmdnsThread.class.getName();
    private static ServiceInfo httpServiceInfo = ServiceInfo.create("_http._tcp.local.", "_sandflower_http", 8080, "SandFlower Http Service");
    private static ServiceInfo httpsServiceInfo = ServiceInfo.create("_https._tcp.local.", "_sandflower_https", 8443, "SandFlower Https Service");
    private Set<JmDNS> jmdnsSet;

    protected boolean doomed;
    private long startTime;

    public JmdnsThread(ThreadGroup threadGroup, String name) {
        super(threadGroup, name);
        setDaemon(false);
        jmdnsSet = new HashSet<>();
        doomed = false;
        // set start time
        startTime = System.currentTimeMillis();
    }

    public void start() {
        if (jmdnsSet == null) {
            jmdnsSet = new HashSet<>();
        }
        if (jmdnsSet.isEmpty()) {
            try {
                boolean isFirst = true;
                for (InetAddress ia : JmdnsUtils.getInetAddressSet()) {
                    JmDNS jmdns = JmDNS.create(ia);
                    if (isFirst) {
                        jmdns.registerService(httpServiceInfo);
                        jmdns.registerService(httpsServiceInfo);
                    } else {
                        jmdns.registerService(httpServiceInfo.clone());
                        jmdns.registerService(httpsServiceInfo.clone());
                    }
                    jmdns.addServiceListener("_http._tcp.local.", new JmdnsService.SampleListener());
                    jmdns.addServiceListener("_https._tcp.local.", new JmdnsService.SampleListener());
                    jmdnsSet.add(jmdns);
                    isFirst = false;
                }
            } catch (IOException e) {
                Debug.logError("Failed to create jmdns: " + e.getMessage(), MODULE);
            }
        }
    }

    public void interrupt() {
        if (jmdnsSet != null && !jmdnsSet.isEmpty()) {
            for (JmDNS jmdns : jmdnsSet) {
                jmdns.unregisterAllServices();
                try {
                    jmdns.close();
                } catch (IOException e) {
                    Debug.logError(e, MODULE);
                }
            }
        }
        Debug.logInfo("JmdnsThread run for " + (System.currentTimeMillis() - startTime)/1000 + " seconds", MODULE);
    }
}
