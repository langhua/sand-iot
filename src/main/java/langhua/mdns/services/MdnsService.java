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

import langhua.mdns.common.MdnsUtils;
import org.apache.ofbiz.base.util.Debug;

import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;
import javax.jmdns.impl.ServiceInfoImpl;
import javax.jmdns.impl.util.ByteWrangler;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MdnsService implements ServiceTypeListener, ServiceListener {
    private static final String MODULE = MdnsService.class.getName();

    private static final Map<String, Map<String, Map<String, String>>> serviceTypes = new HashMap<>();

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
        if (!serviceTypes.containsKey(type)) {
            Debug.logVerbose("Mdns Service Type added   : " + event.getType(), MODULE);
            serviceTypes.put(type, new HashMap<>());
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
        String name = event.getName().trim();
        String base64Name = MdnsUtils.encodeBase64Name(name);

        Debug.logInfo("Mdns Service added   : " + event.getInfo(), MODULE);
        Map<String, Map<String, String>> names;
        if (serviceTypes.containsKey(type)) {
            names = serviceTypes.get(type);
            if (!names.containsKey(name)) {
                Map<String, String> attrs = new HashMap<>();
                if (base64Name != null && !name.equals(base64Name)) {
                    attrs.put("base64Name", base64Name);
                }
                attrs.put("status", Boolean.TRUE.toString());
                names.put(name, attrs);
            }
        } else {
            names = new HashMap<>();
            Map<String, String> attrs = new HashMap<>();
            if (base64Name != null && !name.equals(base64Name)) {
                attrs.put("base64Name", base64Name);
            }
            attrs.put("status", Boolean.TRUE.toString());
            names.put(name, attrs);
            serviceTypes.put(type, names);
        }
    }

    @Override
    public synchronized void serviceRemoved(ServiceEvent event) {
        String type = event.getType();
        final String name = event.getName().trim();
        Debug.logInfo("Mdns Service removed : " + name + "." + event.getType(), MODULE);
        Map<String, Map<String, String>> names;
        if (serviceTypes.containsKey(type)) {
            names = serviceTypes.get(type);
            Map<String, String> attrs = names.get(name);
            if (attrs != null) {
                attrs.put("status", Boolean.FALSE.toString());
            } else {
                attrs = new HashMap<>();
                attrs.put("status", Boolean.FALSE.toString());
                names.put(name, attrs);
            }
        } else {
            names = new HashMap<>();
            Map<String, String> attrs = new HashMap<>();
            String base64Name = MdnsUtils.encodeBase64Name(name);
            if (base64Name != null && !name.equals(base64Name)) {
                attrs.put("base64Name", base64Name);
            }
            names.put(name, attrs);
            serviceTypes.put(type, names);
        }
    }

    @Override
    public synchronized void serviceResolved(ServiceEvent event) {
        Debug.logVerbose("Mdns Service resolved: " + event.getInfo(), MODULE);
    }

    /**
     * Get mdns service types and related host name list.
     */
    public Map<String, Map<String, Map<String, String>>> getServiceTypes() {
        Debug.logInfo("Mdns services: " + serviceTypes, MODULE);
        return serviceTypes;
    }

    /**
     * Get mdns service types and related host name list in json string.
     */
    public String serviceTypesToJson() {
        Set<String> sortedTypes = new TreeSet<>(String::compareTo);
        sortedTypes.addAll(serviceTypes.keySet());
        StringBuilder json = new StringBuilder("{");
        boolean isFirstType = true;
        for (String type : sortedTypes) {
            if (!isFirstType) {
                json.append(",");
            }
            json.append("\"").append(type).append("\":");
            Map<String, Map<String, String>> services = serviceTypes.get(type);
            Set<String> sortedServices = new TreeSet<>(String::compareTo);
            sortedServices.addAll(services.keySet());
            if (sortedServices.isEmpty()) {
                json.append("{}");
            } else {
                json.append("{");
                boolean isFirstService = true;
                for (String service : sortedServices) {
                    if (!isFirstService) {
                        json.append(",");
                    }
                    json.append("\"").append(service).append("\":{");
                    Map<String, String> attrs = services.get(service);
                    if (attrs.containsKey("base64Name") && attrs.get("base64Name") != null) {
                        json.append("\"base64Name\":\"").append(attrs.get("base64Name")).append("\"");
                    }
                    if (attrs.containsKey("status") && attrs.get("status") != null) {
                        if (attrs.containsKey("base64Name") && attrs.get("base64Name") != null) {
                            json.append(",");
                        }
                        json.append("\"status\":\"").append(attrs.get("status")).append("\"");
                    }
                    json.append("}");
                    isFirstService = false;
                }
                json.append("}");
            }
            isFirstType = false;
        }
        json.append("}");
        Debug.logInfo("Mdns services in json: " + json.toString(), MODULE);
        return json.toString();
    }

    /**
     * Get mdns service types and related host name list in json string.
     */
    public String serviceDetailsToJson(String type, String name) {
        ServiceInfoImpl info = (ServiceInfoImpl) getServiceInfo(type, name);
        StringBuilder json = new StringBuilder("{");
        json.append("\"name\": \"").append(name).append("\", ");
        json.append("\"type\": \"").append(info.getTypeWithSubtype()).append("\", ");
        json.append("\"address\": [");
        InetAddress[] addresses = info.getInetAddresses();
        if (addresses.length > 0) {
            boolean first = true;
            for (InetAddress address : addresses) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(address.getHostAddress()).append(':').append(info.getPort()).append("\"");
                first = false;
            }
        } else {
            json.append("\"").append("null:").append(info.getPort()).append("\"");
        }
        json.append("], ");
        json.append("\"persistent\": ").append(info.isPersistent() ? "\"true\"" : "\"false\"");

        if (info.getTextBytes().length > 0) {
            Enumeration<String> propertyNames = info.getPropertyNames();
            json.append(", \"properties\": {");
            boolean first = true;
            while (propertyNames.hasMoreElements()) {
                String propertyName = propertyNames.nextElement();
                if (!first) {
                    json.append(", ");
                }
                json.append("\"").append(propertyName).append("\": \"").append(info.getPropertyString(propertyName)).append("\"");
                first = false;
            }
            json.append("}");
        }
        json.append("}");
        Debug.logInfo("Mdns service{type: " + type + ", name: " + name + "} in json: " + json.toString(), MODULE);
        return json.toString();
    }

    /**
     * Get a mdns service info by specified type and name
     */
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
