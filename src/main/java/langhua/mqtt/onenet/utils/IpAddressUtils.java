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
package langhua.mqtt.onenet.utils;

import langhua.mqtt.common.Utils;
import org.apache.ofbiz.base.util.Debug;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class IpAddressUtils {
    private static final String MODULE = IpAddressUtils.class.getName();

    public static String getIpAddress() {
        Map<String, Map<String, String>> ipMap = new HashMap<String, Map<String, String>>();
        getIpMap(ipMap);
        StringBuffer sb = new StringBuffer(8);
        sb.append("{\"cmd\":\"ipaddress\",\"result\":");
        sb.append(getIpJsonContent(ipMap));
        sb.append("}");
        return sb.toString();
    }

    public static String getIpJsonContent(Map<String, Map<String, String>> ipMap) {
        StringBuffer sb = new StringBuffer(8);
        sb.append("{\"id\":");
        sb.append(Utils.getIdByNowTime());
        sb.append(",\"dp\":{");
        sb.append(getIpDpContent(ipMap));
        sb.append("}}");
        return sb.toString();
    }

    private static StringBuffer getIpDpContent(Map<String, Map<String, String>> ipMap) {
        long now = System.currentTimeMillis()/1000;
        boolean isFirstKey = true;
        StringBuffer sb = new StringBuffer(8);
        for (String key : ipMap.keySet()) {
            Map<String, String> ipValues = ipMap.get(key);
            if (ipValues != null) {
                if (!isFirstKey) {
                    sb.append(",");
                }
                sb.append("\"");
                sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8).replaceAll(" ", "%20").replaceAll("\\+", "%20"));
                boolean isFirstValue = true;
                sb.append("\":[{\"v\":{\"");
                for (String ipKey : ipValues.keySet()) {
                    String ipValue = ipValues.get(ipKey);
                    if (ipValue == null) {
                        continue;
                    }
                    if (!isFirstValue) {
                        sb.append(",\"");
                    }
                    sb.append(URLEncoder.encode(ipKey, StandardCharsets.UTF_8).replaceAll(" ", "%20").replaceAll("\\+", "%20"));
                    sb.append("\":\"");
                    sb.append(ipValue);
                    sb.append("\"");
                    isFirstValue = false;
                }
                sb.append("}, \"t\": " + now + "}]");
                isFirstKey = false;
            }
        }
        return sb;
    }

    public static boolean getIpMap(Map<String, Map<String, String>> ipMap) {
        boolean isIpChanged = false;
        Enumeration<NetworkInterface> nis = null;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Debug.logError(e.getMessage(), MODULE);
        }
        if (nis != null) {
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                String niName = ni.getName().toLowerCase();
                Debug.logInfo("Network interface found: [" + niName + "]", MODULE);
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLinkLocalAddress() && !address.isLoopbackAddress()) {
                        Map<String, String> niIps = ipMap.get(niName);
                        if (niIps == null) {
                            niIps = new HashMap<String, String>();
                        }

                        String ip = address.toString();
                        ip = ip.substring(ip.lastIndexOf('/') + 1);

                        String ipType = null;
                        if (ip.contains(".")) {
                            ipType = "ipv4";
                        } else if (ip.contains(":")) {
                            ipType = "ipv6";
                        }
                        if (ip.contains("%")) {
                            ip = ip.substring(0, ip.indexOf("%"));
                        }
                        if (ipType != null) {
                            Map<String, String> oldNiIps = ipMap.get(niName);
                            if (oldNiIps == null || !oldNiIps.get(ipType).equals(ip)) {
                                isIpChanged = true;
                                niIps.put(ipType, ip);
                                ipMap.put(niName, niIps);
                            }
                        }
                    }
                }
            }
        }
        Debug.logVerbose("-- ipMap: " + ipMap, MODULE);
        return isIpChanged;
    }
}
