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
import org.apache.ofbiz.base.util.Debug;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.impl.ServiceInfoImpl;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class JmdnsThread extends Thread {
    private static final String MODULE = JmdnsThread.class.getName();

    private JmDNS jmdns;

    protected boolean doomed;
    private final long startTime;

    public JmdnsThread(ThreadGroup threadGroup, String name) {
        super(threadGroup, name);
        setDaemon(false);
        doomed = false;
        // set start time
        startTime = System.currentTimeMillis();
    }

    public void start() {
        try {
            InetAddress ia = MdnsUtils.getProperInetAddress();
            int port = 8080;
            Debug.logInfo("-- ia: " + ia, MODULE);
            ServiceListener sl = new JmdnsService();
            jmdns = JmDNS.create(ia, MdnsUtils.MDNS_HOST_NAME);
            Map<ServiceInfo.Fields, String> qualifiedNameMap = new HashMap<>();
            qualifiedNameMap.put(ServiceInfo.Fields.Domain, "local");
            qualifiedNameMap.put(ServiceInfo.Fields.Application, "http");
            qualifiedNameMap.put(ServiceInfo.Fields.Protocol, "tcp");
            qualifiedNameMap.put(ServiceInfo.Fields.Instance, MdnsUtils.MDNS_HOST_NAME);
            qualifiedNameMap.put(ServiceInfo.Fields.Subtype, "");
            Map<String, String> props = new HashMap<>();
            props.put("description", MdnsUtils.MDNS_DESCRIPTION);
            props.put("base_url", "http://" + MdnsUtils.MDNS_HOST_NAME + ":" + port);
            ServiceInfoImpl httpServiceInfo = new ServiceInfoImpl(qualifiedNameMap, port, 0, 0, true, props);
            jmdns.registerService(httpServiceInfo);
            jmdns.addServiceListener(MdnsUtils.MDNS_TYPE, sl);
        } catch (IOException e) {
            Debug.logError("Failed to create jmdns: " + e.getMessage(), MODULE);
        }
    }

    public void interrupt() {
        if (jmdns != null) {
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
            } catch (IOException e) {
                Debug.logError(e, MODULE);
            }
        }
        Debug.logInfo("JmdnsThread run for " + (System.currentTimeMillis() - startTime)/1000 + " seconds", MODULE);
    }
}
