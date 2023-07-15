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
package langhua.mqtt.onenet.test;

import junit.framework.TestCase;
import langhua.mqtt.common.MqttClientThread;
import langhua.mqtt.onenet.services.OnenetHttpApi;
import langhua.mqtt.onenet.services.OnenetMqttDevice;
import langhua.mqtt.onenet.services.OnenetMqttsDevice;
import langhua.mqtt.onenet.utils.CommonUtils;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilProperties;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class AbstractTestMqtt extends TestCase {

    private static OnenetMqttDevice group1Device1Tcp = null;
    private static OnenetMqttDevice group1Device2Tcp = null;
    private static OnenetMqttDevice group2Device1Tcp = null;
    private static OnenetMqttDevice group2Device2Tcp = null;

    private MqttClientThread group1Device1TcpThread;
    private MqttClientThread group1Device2TcpThread;
    private MqttClientThread group2Device1TcpThread;
    private MqttClientThread group2Device2TcpThread;
    private MqttClientThread group1Device1SslThread;
    private MqttClientThread group1Device2SslThread;
    private MqttClientThread group2Device1SslThread;
    private MqttClientThread group2Device2SslThread;

    private static OnenetMqttsDevice group1Device1Ssl = null;
    private static OnenetMqttsDevice group1Device2Ssl = null;
    private static OnenetMqttsDevice group2Device1Ssl = null;
    private static OnenetMqttsDevice group2Device2Ssl = null;

    // Group1 devices
    protected static final String GROUP1_NAME = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.name");
    protected static final String GROUP1_ID = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.id");
    protected static final String GROUP1_KEY = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.key");
    protected static final String GROUP1_DEVICE1_NAME = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device1.name");
    protected static final String GROUP1_DEVICE1_ID = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device1.id");
    protected static final String GROUP1_DEVICE1_KEY = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device1.key");
    protected static final String GROUP1_DEVICE2_NAME = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device2.name");
    protected static final String GROUP1_DEVICE2_ID = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device2.id");
    protected static final String GROUP1_DEVICE2_KEY = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device2.key");

    // Group2 devices
    protected static final String GROUP2_NAME = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.name");
    protected static final String GROUP2_ID = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.id");
    protected static final String GROUP2_KEY = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.key");
    protected static final String GROUP2_DEVICE1_NAME = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device1.name");
    protected static final String GROUP2_DEVICE1_ID = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device1.id");
    protected static final String GROUP2_DEVICE1_KEY = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device1.key");
    protected static final String GROUP2_DEVICE2_NAME = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device2.name");
    protected static final String GROUP2_DEVICE2_ID = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device2.id");
    protected static final String GROUP2_DEVICE2_KEY = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device2.key");

    /**
     * Initial all tcp-connected mqtt devices.
     *
     * @param module
     * @throws MqttException
     */
    protected void initialTcpDevices(String module) throws MqttException {
        Debug.logInfo("Initial TCP threads and devices ...", module);
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int i = threadGroup.activeCount();
        Debug.logInfo("--- thread group[" + threadGroup.getName() + "] has [" + i + "] threads.", module);
        Thread[] threads = new Thread[i];
        threadGroup.enumerate(threads, true);
        for (Thread threadInstance : threads) {
            if (threadInstance instanceof MqttClientThread thread) {
                if (thread.getName().equals(OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + GROUP1_ID + "/" + GROUP1_DEVICE1_ID)) {
                    Debug.logInfo("--- Found group1 device1 tcp thread", module);
                    group1Device1TcpThread = thread;
                    group1Device1Tcp = (OnenetMqttDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + GROUP1_ID + "/" + GROUP1_DEVICE2_ID)) {
                    Debug.logInfo("--- Found group1 device2 tcp thread", module);
                    group1Device2TcpThread = thread;
                    group1Device2Tcp = (OnenetMqttDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + GROUP2_ID + "/" + GROUP2_DEVICE1_ID)) {
                    Debug.logInfo("--- Found group2 device1 tcp thread", module);
                    group2Device1TcpThread = thread;
                    group2Device1Tcp = (OnenetMqttDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + GROUP2_ID + "/" + GROUP2_DEVICE2_ID)) {
                    Debug.logInfo("--- Found group2 device2 tcp thread", module);
                    group2Device2TcpThread = thread;
                    group2Device2Tcp = (OnenetMqttDevice) thread.getMqttDevice();
                }
            }
        }

        if (group1Device1TcpThread == null) {
            group1Device1Tcp = new OnenetMqttDevice(GROUP1_NAME, GROUP1_ID, GROUP1_DEVICE1_NAME,
                    GROUP1_DEVICE1_ID, GROUP1_DEVICE1_KEY);
            group1Device1TcpThread = new MqttClientThread(threadGroup,
                    OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + GROUP1_ID + "/" + GROUP1_DEVICE1_ID, group1Device1Tcp);
            group1Device1TcpThread.start();
        }

        if (group1Device2TcpThread == null) {
            group1Device2Tcp = new OnenetMqttDevice(GROUP1_NAME, GROUP1_ID, GROUP1_DEVICE2_NAME,
                    GROUP1_DEVICE2_ID, GROUP1_DEVICE2_KEY);
            group1Device2TcpThread = new MqttClientThread(threadGroup,
                    OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + GROUP1_ID + "/" + GROUP1_DEVICE2_ID, group1Device2Tcp);
            group1Device2TcpThread.start();
        }

        if (group2Device1TcpThread == null) {
            group2Device1Tcp = new OnenetMqttDevice(GROUP2_NAME, GROUP2_ID, GROUP2_DEVICE1_NAME,
                    GROUP2_DEVICE1_ID, GROUP2_DEVICE1_KEY);
            group2Device1TcpThread = new MqttClientThread(threadGroup,
                    OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + GROUP2_ID + "/" + GROUP2_DEVICE1_ID, group2Device1Tcp);
            group2Device1TcpThread.start();
        }

        if (group2Device2TcpThread == null) {
            group2Device2Tcp = new OnenetMqttDevice(GROUP2_NAME, GROUP2_ID, GROUP2_DEVICE2_NAME,
                    GROUP2_DEVICE2_ID, GROUP2_DEVICE2_KEY);
            group2Device2TcpThread = new MqttClientThread(threadGroup,
                    OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + GROUP2_ID + "/" + GROUP2_DEVICE2_ID, group2Device2Tcp);
            group2Device2TcpThread.start();
        }
        Debug.logInfo("--- Now thread group[" + threadGroup.getName() + "] has [" + threadGroup.activeCount() + "] threads.", module);
    }

    /**
     * Initial all ssl-connected mqtt devices.
     *
     * @param module
     * @throws Exception
     */
    protected void initialSslDevices(String module) throws Exception {
        Debug.logInfo("Initial SSL threads and devices ...", module);
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int i = threadGroup.activeCount();
        Debug.logInfo("--- thread group[" + threadGroup.getName() + "] has [" + i + "] threads.", module);
        Thread[] threads = new Thread[i];
        threadGroup.enumerate(threads, true);
        for (Thread threadInstance : threads) {
            if (threadInstance instanceof MqttClientThread thread) {
                if (thread.getName().equals(OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + GROUP1_ID + "/" + GROUP1_DEVICE1_ID)) {
                    Debug.logInfo("--- Found group1 device1 ssl thread", module);
                    group1Device1SslThread = thread;
                    group1Device1Ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + GROUP1_ID + "/" + GROUP1_DEVICE2_ID)) {
                    Debug.logInfo("--- Found group1 device2 ssl thread", module);
                    group1Device2SslThread = thread;
                    group1Device2Ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + GROUP2_ID + "/" + GROUP2_DEVICE1_ID)) {
                    Debug.logInfo("--- Found group2 device1 ssl thread", module);
                    group2Device1SslThread = thread;
                    group2Device1Ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + GROUP2_ID + "/" + GROUP2_DEVICE2_ID)) {
                    Debug.logInfo("--- Found group2 device2 ssl thread", module);
                    group2Device2SslThread = thread;
                    group2Device2Ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                }
            }
        }

        if (group1Device1SslThread == null) {
            group1Device1Ssl = new OnenetMqttsDevice(GROUP1_NAME, GROUP1_ID, GROUP1_DEVICE1_NAME,
                    GROUP1_DEVICE1_ID, GROUP1_DEVICE1_KEY);
            group1Device1SslThread = new MqttClientThread(threadGroup,
                    OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + GROUP1_ID + "/" + GROUP1_DEVICE1_ID, group1Device1Ssl);
            group1Device1SslThread.start();
        }

        if (group1Device2SslThread == null) {
            group1Device2Ssl = new OnenetMqttsDevice(GROUP1_NAME, GROUP1_ID, GROUP1_DEVICE2_NAME,
                    GROUP1_DEVICE2_ID, GROUP1_DEVICE2_KEY);
            group1Device2SslThread = new MqttClientThread(threadGroup,
                    OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + GROUP1_ID + "/" + GROUP1_DEVICE2_ID, group1Device2Ssl);
            group1Device2SslThread.start();
        }

        if (group2Device1SslThread == null) {
            group2Device1Ssl = new OnenetMqttsDevice(GROUP2_NAME, GROUP2_ID, GROUP2_DEVICE1_NAME,
                    GROUP2_DEVICE1_ID, GROUP2_DEVICE1_KEY);
            group2Device1SslThread = new MqttClientThread(threadGroup,
                    OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + GROUP2_ID + "/" + GROUP2_DEVICE1_ID, group2Device1Ssl);
            group2Device1SslThread.start();
        }

        if (group2Device2SslThread == null) {
            group2Device2Ssl = new OnenetMqttsDevice(GROUP2_NAME, GROUP2_ID, GROUP2_DEVICE2_NAME,
                    GROUP2_DEVICE2_ID, GROUP2_DEVICE2_KEY);
            group2Device2SslThread = new MqttClientThread(threadGroup,
                    OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + GROUP2_ID + "/" + GROUP2_DEVICE2_ID, group2Device2Ssl);
            group2Device2SslThread.start();
        }

        Debug.logInfo("--- Now thread group[" + threadGroup.getName() + "] has [" + threadGroup.activeCount() + "] threads.", module);
    }

    /**
     * Get device info.
     *
     * @param device        a device instance of OnenetMqttClient
     * @param groupKey      group key
     * @return String       device info
     */
    protected String getDeviceInfo(OnenetMqttDevice device, String groupId, String groupKey) {
        OnenetHttpApi httpApi = new OnenetHttpApi(groupId, groupKey);
        return OnenetHttpApi.getDeviceInfo(httpApi.getApiToken(), device);
    }

    /**
     * Test device subscribe command request and response topics.
     */
    protected boolean deviceSubCmd(OnenetMqttDevice device) {
        return device.subCmdRequest() && device.subCmdResponse();
    }

    /**
     * Publish a data point.
     *
     * @param device
     * @param module
     * @return
     */
    protected boolean publishDp(OnenetMqttDevice device, String module) {
        Debug.logInfo("Test group1_device1 publish a data point.", module);
        HashMap<String, String> values = new HashMap<>();
        Random random = new Random();
        values.put("temp", String.valueOf(28 + random.nextInt(10)));
        values.put("hum", 40 + random.nextInt(10) + "%");
        String dataPub = CommonUtils.generateDataPubContent(values);
        Debug.logInfo("Data point to publish: " + dataPub, module);
        try {
            device.publish(CommonUtils.getDataPubTopic(device.getGroupId(), device.getDeviceName()),
                    0, dataPub.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Throwable e) {
            Debug.logInfo(e, module);
            return false;
        }
    }

    /**
     * Publish a wrong format data point.
     *
     * @param device
     * @param module
     * @return
     */
    protected boolean pubWrongFormatDp(OnenetMqttDevice device, String module) {
        Debug.logInfo("Test " + device.getDeviceName() + " publish a data point with format wrong.", module);
        HashMap<String, String> values = new HashMap<>();
        Random random = new Random();
        values.put("temp", String.valueOf(28 + random.nextInt(10)));
        values.put("hum", 40 + random.nextInt(10) + "%");
        String dataPub = "make format wrong. " + CommonUtils.generateDataPubContent(values);
        Debug.logInfo(" Data to pub: " + dataPub, module);
        try {
            device.publish(CommonUtils.getDataPubTopic(device.getGroupId(), device.getDeviceName()),
                    0, dataPub.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Throwable e) {
            Debug.logInfo(e, module);
            return false;
        }
    }

    /**
     * Test send command by http api to a mqtt device.
     *
     * @param device
     * @param groupId
     * @param groupKey
     * @param command
     * @param module
     * @return
     */
    protected boolean runCommandTest(OnenetMqttDevice device, String groupId, String groupKey, String command, String module) {
        OnenetHttpApi httpApi = new OnenetHttpApi(groupId, groupKey);
        String result = OnenetHttpApi.sendCommand(httpApi.getApiToken(), device.getDeviceId(), command);
        if (OnenetHttpApi.isSuccessResponse(result)) {
            Map<String, ?> cmdResponse = OnenetHttpApi.parseSuccessResponse(result);
            Debug.logInfo("--- Command[" + command + "] success: " + (cmdResponse != null ? cmdResponse.get("result") : result), module);
            return true;
        } else {
            Debug.logInfo("--- Command[" + command + "] error: " + result, module);
            return false;
        }
    }

    /**
     * Stop all tcp-connected mqtt devices.
     *
     * @param module
     * @throws Exception
     */
    protected void tearDownTcpDevices(String module) throws Exception {
        Debug.logInfo("Tear down OneNET mqtt TCP devices...", module);
        if (group1Device1TcpThread != null && !group1Device1TcpThread.isInterrupted()) {
            group1Device1TcpThread.interrupt();
        }
        if (group1Device2TcpThread != null && !group1Device2TcpThread.isInterrupted()) {
            group1Device2TcpThread.interrupt();
        }
        if (group2Device1TcpThread != null && !group2Device1TcpThread.isInterrupted()) {
            group2Device1TcpThread.interrupt();
        }
        if (group2Device2TcpThread != null && !group2Device2TcpThread.isInterrupted()) {
            group2Device2TcpThread.interrupt();
        }
        Debug.logInfo("... Tear down tcp devices completed", module);
    }

    /**
     * Stop all ssl-connected mqtt devices.
     *
     * @param module
     */
    protected void tearDownSslDevices(String module) {
        Debug.logInfo("Tear down OneNET mqtt SSL devices...", module);
        if (group1Device1SslThread != null && group1Device1SslThread.isInterrupted()) {
            group1Device1SslThread.interrupt();
        }
        if (group1Device2SslThread != null && group1Device2SslThread.isInterrupted()) {
            group1Device2SslThread.interrupt();
        }
        if (group2Device1SslThread != null && group2Device1SslThread.isInterrupted()) {
            group2Device1SslThread.interrupt();
        }
        if (group2Device2SslThread != null && group2Device2SslThread.isInterrupted()) {
            group2Device2SslThread.interrupt();
        }
        Debug.logInfo("... Tear down ssl devices completed", module);
    }

    /**
     * Get group1 device1 with tcp connection.
     */
    protected OnenetMqttDevice getGroup1Device1Tcp() {
        return group1Device1Tcp;
    }

    /**
     * Get group1 device2 with tcp connection.
     */
    protected OnenetMqttDevice getGroup1Device2Tcp() {
        return group1Device2Tcp;
    }

    /**
     * Get group2 device1 with tcp connection.
     */
    protected OnenetMqttDevice getGroup2Device1Tcp() {
        return group2Device1Tcp;
    }

    /**
     * Get group2 device2 with tcp connection.
     */
    protected OnenetMqttDevice getGroup2Device2Tcp() {
        return group2Device2Tcp;
    }

    /**
     * Get group1 device1 with ssl connection.
     */
    protected OnenetMqttsDevice getGroup1Device1Ssl() {
        return group1Device1Ssl;
    }

    /**
     * Get group1 device2 with ssl connection.
     */
    protected OnenetMqttsDevice getGroup1Device2Ssl() {
        return group1Device2Ssl;
    }

    /**
     * Get group2 device1 with ssl connection.
     */
    protected OnenetMqttsDevice getGroup2Device1Ssl() {
        return group2Device1Ssl;
    }

    /**
     * Get group2 device2 with ssl connection.
     */
    protected OnenetMqttsDevice getGroup2Device2Ssl() {
        return group2Device2Ssl;
    }
}
