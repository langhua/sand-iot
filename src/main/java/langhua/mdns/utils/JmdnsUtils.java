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
package langhua.mdns.utils;

import org.apache.ofbiz.base.util.Debug;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * Jmdns utils
 */
public class JmdnsUtils {

    private static final String MODULE = JmdnsUtils.class.getName();

    private static ServiceInfo httpServiceInfo = ServiceInfo.create("_sandflower_http._tcp.local.", "sandflower_http", 8080, "SandFlower Http Service");
    private static ServiceInfo httpsServiceInfo = ServiceInfo.create("_sandflower_https._tcp.local.", "sandflower_https", 8443, "SandFlower Https Service");

    public static Set<InetAddress> getInetAddressSet() {
        Set<InetAddress> inetAddrs = new HashSet<>();
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
                Debug.logInfo("Network interface found:" + ni, MODULE);
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLinkLocalAddress() && !address.isLoopbackAddress()) {
                        inetAddrs.add(address);
                        Debug.logInfo("-- InetAddress added: " + address, MODULE);
                    }
                }
            }
        }
        return inetAddrs;
    }
}
