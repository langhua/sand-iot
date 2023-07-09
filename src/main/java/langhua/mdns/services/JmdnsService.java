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

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceTypeListener;

import org.apache.commons.collections4.list.TreeList;
import org.apache.ofbiz.base.util.Debug;

public class JmdnsService implements ServiceTypeListener, ServiceListener {
    private static final String MODULE = JmdnsService.class.getName();

    private static TreeList<String> services = new TreeList<>();

    private static TreeList<String> serviceTypes = new TreeList<>();

    @Override
    public void serviceTypeAdded(ServiceEvent event) {
        Debug.logVerbose("Mdns Service Type added   : " + event.getType(), MODULE);
        serviceTypes.add(event.getType());
    }

    @Override
    public void subTypeForServiceTypeAdded(ServiceEvent event) {
        Debug.logVerbose("Mdns subtype added: " + event.getType(), MODULE);
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        final String name = event.getName();
        Debug.logVerbose("Mdns Service added   : " + event.getInfo(), MODULE);
        services.add(name);
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        final String name = event.getName();
        Debug.logVerbose("Mdns Service removed : " + name + "." + event.getType(), MODULE);
        services.remove(name);
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        Debug.logVerbose("Mdns Service resolved: " + event.getInfo(), MODULE);
    }

    public TreeList<String> getServiceTypes() {
        return serviceTypes;
    }

    public TreeList<String> getServices() {
        return services;
    }
}
