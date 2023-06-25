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
import javax.jmdns.ServiceListener;

import org.apache.ofbiz.base.util.Debug;

public class JmdnsService {
    private static final String MODULE = JmdnsService.class.getName();

    public static class SampleListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            Debug.logInfo("Jmdns Service added   : " + event.getName() + "." + event.getType(), MODULE);
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            Debug.logInfo("Jmdns Service removed : " + event.getName() + "." + event.getType(), MODULE);
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            Debug.logInfo("Jmdns Service resolved: " + event.getInfo(), MODULE);
        }
    }
}
