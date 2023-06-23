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
package langhua.mqtt.common;

import org.apache.ofbiz.base.util.Debug;

public class MqttClientThread extends Thread {
    private static final String MODULE = MqttClientThread.class.getName();
    AbstractSandMqttDevice mqttDevice;

    protected boolean doomed;
    private long startTime;

    public MqttClientThread(ThreadGroup threadGroup, String name, AbstractSandMqttDevice mqttDevice) {
        super(threadGroup, name);
        setDaemon(false);
        this.mqttDevice = mqttDevice;
        doomed = false;
        // set start time
        startTime = System.currentTimeMillis();
    }

    public void start() {
        if (mqttDevice != null && !mqttDevice.isConnected()) {
            mqttDevice.init();
        }
    }

    public AbstractSandMqttDevice getMqttDevice() {
        return mqttDevice;
    }

    public void interrupt() {
        if (mqttDevice != null) {
            if (mqttDevice.isConnected()) mqttDevice.disconnect();
            Debug.logInfo("Device[" + mqttDevice.client.getClientId() + "]" + " run for " + (System.currentTimeMillis() - startTime)/1000 + " seconds", MODULE);
            mqttDevice = null;
        }
    }
}
