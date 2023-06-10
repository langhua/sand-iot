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

/**
 * Jmdns utils
 */
public class JmdnsUtils {

    private static final String MODULE = JmdnsUtils.class.getName();

    private static ServiceInfo httpServiceInfo = ServiceInfo.create("_sandflower_http._tcp.local.", "sandflower_mdns", 5353, "SandFlower Http Service");
    private static ServiceInfo httpsServiceInfo = ServiceInfo.create("_sandflower_https._tcp.local.", "sandflower_mdns", 5353, "SandFlower Https Service");
    private static ServiceInfo raopServiceInfo = ServiceInfo.create("_sandflower_raop._tcp.local.", "sandflower_mdns", 5353, "SandFlower Roap Service");

    /**
     * Register mdns
     * @param mDNS
     * @return
     */
    public boolean registerMdns(JmDNS mDNS){
        try {
            mDNS.registerService(httpServiceInfo);
            mDNS.registerService(httpsServiceInfo);
            mDNS.registerService(raopServiceInfo);
            return true;
        } catch (Exception e) {
            Debug.logError(e.getMessage(), MODULE);
            return false;
        }
    }

    /**
     * Unregister mdns
     */
    public boolean unregisterMdns(JmDNS jmDNS){
        try {
            jmDNS.unregisterService(httpServiceInfo);
            jmDNS.unregisterService(httpsServiceInfo);
            jmDNS.unregisterService(raopServiceInfo);
            return true;
        } catch (Exception e) {
            Debug.logError(e.getMessage(), MODULE);
            return false;
        }
    }
}
