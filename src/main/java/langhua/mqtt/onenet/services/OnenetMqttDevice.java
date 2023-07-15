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
package langhua.mqtt.onenet.services;

import java.io.File;
import java.nio.charset.StandardCharsets;

import langhua.mqtt.common.AbstractSandMqttDevice;
import langhua.mqtt.onenet.utils.CommonUtils;
import langhua.mqtt.onenet.utils.IpAddressUtils;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilProperties;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class OnenetMqttDevice extends AbstractSandMqttDevice {
    private static final String MODULE = OnenetMqttDevice.class.getName();

    public static final String ONENET_SERVER_TCP_SCHEMA = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.server.tcp.schema");
    protected static final String ONENET_SERVER_TCP_IP = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.server.tcp.ip");
    protected static final String ONENET_SERVER_TCP_PORT = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.server.tcp.port");
    private String brokerUrl;
    private String groupId;
    private String groupName;
    private String password;
    private String deviceName;
    private String deviceId;
    private String deviceKey;

    // see https://open.iot.10086.cn/doc/mqtt/book/device-develop/protocol.html
    private boolean clean = true;

    private final MqttClientPersistence dataStore;

    private final String ofbizHome = System.getProperty("ofbiz.home");
    private MqttDefaultFilePersistence fsDataStore = new MqttDefaultFilePersistence((ofbizHome == null ? "" : (ofbizHome + File.separatorChar))
            + "runtime" + File.separatorChar + "tempfiles" + File.separatorChar + "mqtt");
    private MemoryPersistence memDataStore = new MemoryPersistence();

    /**
     * Constructs an instance of the OneNET client wrapper
     *
     * @param groupName    group name
     * @param groupId      group id
     * @param deviceName   device name
     * @param deviceId     device id
     * @param deviceKey    device key
     */
    public OnenetMqttDevice(String groupName, String groupId, String deviceName, String deviceId, String deviceKey) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.deviceName = deviceName;
        this.deviceId = deviceId;
        this.deviceKey = deviceKey;
        this.password = CommonUtils.generateMqttPassword(groupId, deviceName, deviceKey);
        this.dataStore = UtilProperties.getPropertyAsBoolean("mqtt", "mqtt.onenet.client.usefsdatastore", true) ? fsDataStore : memDataStore;
    }

    @Override
    public void init() {
        brokerUrl = ONENET_SERVER_TCP_SCHEMA + ONENET_SERVER_TCP_IP + ":" + ONENET_SERVER_TCP_PORT;

        try {
            // Construct the object that contains connection parameters
            // such as cleanSession and LWT
            MqttConnectOptions conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(true);
            if (password != null) {
                conOpt.setPassword(password.toCharArray());
            }
            if (groupId != null) {
                conOpt.setUserName(groupId);
            }
            conOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            conOpt.setConnectionTimeout(30);
            conOpt.setKeepAliveInterval(10);
            conOpt.setAutomaticReconnect(true);
            setConOpt(conOpt);

            // Construct the MqttClient instance
            MqttAsyncClient client = new MqttAsyncClient(brokerUrl, deviceName, dataStore);

            // Set this wrapper as the callback handler
            client.setCallback(this);
            setClient(client);
        } catch (MqttException e) {
            Debug.logError("Unable to set up client: " + e.toString(), MODULE);
            setClient(null);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        Debug.logInfo("[" + getGroupId() + "/" + getDeviceName() + "] Message Arrived: [" + topic + "] "
                + new String(message.getPayload()), MODULE);
        if (topic.startsWith(CommonUtils.getDeviceTopicPrefix(getGroupId(), getDeviceName()))) {
            String cmdId = topic.substring(topic.lastIndexOf('/') + 1);
            Debug.logInfo("Command id: " + cmdId, MODULE);
            try {
                String response = new String(message.getPayload());
                if ("ipaddress".equals(response)) {
                    response = IpAddressUtils.getIpAddress();
                    Debug.logInfo("Command response:" + response, MODULE);
                } else {
                    response = "command [" + response + "] message received.";
                }
                if (cmdId.equals("accepted") || cmdId.equals("rejected")) {
                    Debug.logInfo("--- Unnecessary to response this message.", MODULE);
                } else {
                    cmdResponse(CommonUtils.getCmdResponseTopic(getGroupId(), getDeviceName(), cmdId), 0, response.getBytes(StandardCharsets.UTF_8));
                }
            } catch (Throwable e) {
                Debug.logError(e.getMessage(), MODULE);
            }
        } else {
            Debug.logError("WRONG topic: " + topic, MODULE);
        }
    }

    /**
     * Get group name of this device.
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * Get password of this device.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Get group id of this device.
     */
    public String getGroupId() {
        return this.groupId;
    }

    /**
     * Get name of this device.
     */
    public String getDeviceName() {
        return this.deviceName;
    }

    /**
     * Get id of this device.
     */
    public String getDeviceId() {
        return this.deviceId;
    }

    /**
     * Set id of this device.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(8);
        sb.append("group id: ");
        sb.append(getGroupId());
        sb.append(", device id: ");
        sb.append(getDeviceId());
        sb.append(", device name: ");
        sb.append(getDeviceName());
        sb.append(", password: ");
        sb.append(getPassword());
        return sb.toString();
    }

    /**
     * Subscribe the device its own command request topic.
     */
    public boolean subCmdRequest() {
        return subCmdRequest(getGroupId(), getDeviceName());
    }

    /**
     * Subscribe a device's command request topic. Return  true if success, else false
     *
     * @param groupId
     * @param deviceName
     */
    public boolean subCmdRequest(String groupId, String deviceName) {
        String topic = CommonUtils.getCmdRequestTopic(groupId, deviceName);
        try {
            subscribe(topic, 0);
        } catch (Throwable e) {
            Debug.logError(e, MODULE);
            return false;
        }
        return true;
    }

    /**
     * Subscribe the device its own command response topic.
     *
     */
    public boolean subCmdResponse() {
        return subCmdResponse(this);
    }

    /**
     * Subscribe a device's command response topic. Return ture if success, else false.
     *
     * @param device
     */
    public boolean subCmdResponse(OnenetMqttDevice device) {
        String topic = CommonUtils.getCmdResponseTopic(device.getGroupId(), device.getDeviceName());
        try {
            subscribe(topic, 0);
        } catch (Throwable e) {
            Debug.logError(e, MODULE);
            return false;
        }
        return true;
    }

    /**
     * Subscribe publish response for this device.
     *
     * @return
     */
    public boolean subPubResponse() {
        return subPubResponse(this);
    }

    /**
     * Subscribe publish response for the device.
     *
     * @param device
     * @return
     */
    public boolean subPubResponse(OnenetMqttDevice device) {
        String acceptedTopic = CommonUtils.getDataPubAcceptedTopic(device.getGroupId(), device.getDeviceName());
        String rejectedTopic = CommonUtils.getDataPubRejectedTopic(device.getGroupId(), device.getDeviceName());
        try {
            subscribe(acceptedTopic, 0);
        } catch (Throwable e) {
            Debug.logError(e, MODULE);
            return false;
        }
        try {
            subscribe(rejectedTopic, 0);
        } catch (Throwable e) {
            Debug.logError(e, MODULE);
            return false;
        }
        return true;
    }

    /**
     * Get data store to persist mqtt client.
     */
    protected MqttClientPersistence getDataStore() {
        return this.dataStore;
    }

    /**
     * Get whether to use clean connection option for this device.
     */
    protected boolean getClean() {
        return this.clean;
    }

    /**
     * Get broker url of this device.
     */
    protected String getBrokerUrl() {
        return this.brokerUrl;
    }

    /**
     * Set broker url of this device.
     */
    protected void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }
}
