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
package langhua.mdns.services;

import org.apache.ofbiz.base.util.Debug;

import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MdnsService implements ServiceTypeListener, ServiceListener {
    private static final String MODULE = MdnsService.class.getName();

    private static Map<String, List<String>> serviceTypes = new HashMap<>();

    private static JmmDNS registry;

    public MdnsService() {
        registry = JmmDNS.Factory.getInstance();
        try {
            registry.addServiceTypeListener(this);
        } catch (IOException e) {
            Debug.logError(e, MODULE);
        }
    }

    @Override
    public synchronized void serviceTypeAdded(ServiceEvent event) {
        String type = event.getType().trim();
        if (!serviceTypes.keySet().contains(type)) {
            Debug.logVerbose("Mdns Service Type added   : " + event.getType(), MODULE);
            serviceTypes.put(type, new ArrayList<>());
            registry.addServiceListener(type, this);
        }
    }

    @Override
    public synchronized void subTypeForServiceTypeAdded(ServiceEvent event) {
        Debug.logInfo("Jmdns SUBTYPE added: " + event.getType(), MODULE);
    }

    @Override
    public synchronized void serviceAdded(ServiceEvent event) {
        String type = event.getType().trim();
        final String name = event.getName().trim();
        Debug.logInfo("Mdns Service added   : " + event.getInfo(), MODULE);
        List<String> names;
        if (serviceTypes.containsKey(type)) {
            names = serviceTypes.get(type);
            if (!names.contains(name)) {
                names.add(name);
            }
        } else {
            names = new ArrayList<>();
            names.add(name);
            serviceTypes.put(type, names);
        }
    }

    @Override
    public synchronized void serviceRemoved(ServiceEvent event) {
        String type = event.getType();
        final String name = event.getName().trim();
        Debug.logInfo("Mdns Service removed : " + name + "." + event.getType(), MODULE);
        if (serviceTypes.containsKey(type)) {
            List<String> names = serviceTypes.get(type);
            names.remove(name);
        }
    }

    @Override
    public synchronized void serviceResolved(ServiceEvent event) {
        Debug.logVerbose("Mdns Service resolved: " + event.getInfo(), MODULE);
    }

    /**
     * Get mdns service types and related host name list.
     *
     * @return
     */
    public Map<String, List<String>> getServiceTypes() {
        return serviceTypes;
    }

    public ServiceInfo getServiceInfo(String type, String name) {
        ServiceInfo[] infos = registry.getServiceInfos(type, name);
        if (infos != null && infos.length > 0) {
            return infos[0];
        }
        return null;
    }

    /**
     * Close mdns service registry.
     */
    public void close() {
        if (registry != null) {
            try {
                registry.close();
            } catch (IOException e) {
                Debug.logError(e, MODULE);
            }
        }
    }
}
