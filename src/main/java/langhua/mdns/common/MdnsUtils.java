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

import org.apache.commons.lang3.StringUtils;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilProperties;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Jmdns utils
 */
public class MdnsUtils {

    private static final String MODULE = MdnsUtils.class.getName();

    public static final String MDNS_HOST_NAME = UtilProperties.getPropertyValue("mdns", "mdns.host.name", "sandflower");

    public static final String MDNS_DESCRIPTION = UtilProperties.getPropertyValue("mdns", "mdns.description",
            MDNS_HOST_NAME.toLowerCase() + "_http_service");

    private static final String MDNS_IP_PREFERRED = UtilProperties.getPropertyValue("mdns", "mdns.ip.preferred", null);

    public static InetAddress getProperInetAddress() {
        InetAddress inetAddr = null;
        Debug.logInfo("MDNS_IP_PREFERRED: " + MDNS_IP_PREFERRED, MODULE);
        if (MDNS_IP_PREFERRED != null) {
            try {
                inetAddr = InetAddress.getByAddress(MDNS_IP_PREFERRED.getBytes());
                return inetAddr;
            } catch (UnknownHostException e) {
                Debug.logError(e, MODULE);
            }
        }
        Enumeration<NetworkInterface> nis = null;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Debug.logError(e.getMessage(), MODULE);
        }
        if (nis != null) {
            Set<InetAddress> niInet4Addresses = new HashSet<>();
            Set<InetAddress> niInet6Addresses = new HashSet<>();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                Debug.logInfo("Network interface found:" + ni, MODULE);
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLinkLocalAddress() && !address.isLoopbackAddress()) {
                        if (address instanceof Inet4Address) {
                            niInet4Addresses.add(address);
                        } else if (address instanceof Inet6Address) {
                            niInet6Addresses.add(address);
                        }
                    }
                }
            }
            if (!niInet4Addresses.isEmpty()) {
                for (InetAddress ia : niInet4Addresses) {
                    String ip = ia.getHostAddress();
                    if (!ip.endsWith(".1") && !ip.endsWith(".255")) {
                        inetAddr = ia;
                        break;
                    }
                }
            }
            if (inetAddr == null && !niInet6Addresses.isEmpty()) {
                inetAddr = (InetAddress) niInet6Addresses.toArray()[0];
            }
            if (inetAddr == null && !niInet4Addresses.isEmpty()) {
                inetAddr = (InetAddress) niInet4Addresses.toArray()[0];
            }
        }
        return inetAddr;
    }

    public static String encodeBase64Name(String name) {
        String base64Name = Base64.getUrlEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8));
        if (!name.equals(base64Name)) {
            return base64Name;
        }
        return null;
    }

    public static String decodeBase64Name(String name) {
        String originalName = new String(Base64.getUrlDecoder().decode(name), StandardCharsets.UTF_8);
        if (!name.equals(originalName)) {
            return originalName;
        }
        return null;
    }
}
