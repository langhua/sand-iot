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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
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

/**
 * Test case for intergroup isolation.
 */
public class FunctionTests extends TestCase {
    private static final String MODULE = FunctionTests.class.getName();

    private static OnenetMqttDevice group1_device1_tcp = null;
    private static OnenetMqttDevice group1_device2_tcp = null;
    private static OnenetMqttDevice group2_device1_tcp = null;
    private static OnenetMqttDevice group2_device2_tcp = null;

    private MqttClientThread group1_device1_tcp_thread;
    private MqttClientThread group1_device2_tcp_thread;
    private MqttClientThread group2_device1_tcp_thread;
    private MqttClientThread group2_device2_tcp_thread;
    private MqttClientThread group1_device1_ssl_thread;
    private MqttClientThread group1_device2_ssl_thread;
    private MqttClientThread group2_device1_ssl_thread;
    private MqttClientThread group2_device2_ssl_thread;

    private static OnenetMqttsDevice group1_device1_ssl = null;
    private static OnenetMqttsDevice group1_device2_ssl = null;
    private static OnenetMqttsDevice group2_device1_ssl = null;
    private static OnenetMqttsDevice group2_device2_ssl = null;

    // Group1 devices
    private static final String group1_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.name");
    private static final String group1_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.id");
    private static final String group1_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.key");
    private static final String group1_device1_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device1.name");
    private static final String group1_device1_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device1.id");
    private static final String group1_device1_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device1.key");
    private static final String group1_device2_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device2.name");
    private static final String group1_device2_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device2.id");
    private static final String group1_device2_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device2.key");

    // Group2 devices
    private static final String group2_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.name");
    private static final String group2_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.id");
    private static final String group2_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.key");
    private static final String group2_device1_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device1.name");
    private static final String group2_device1_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device1.id");
    private static final String group2_device1_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device1.key");
    private static final String group2_device2_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device2.name");
    private static final String group2_device2_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device2.id");
    private static final String group2_device2_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device2.key");

    private void initialTcpDevices() throws MqttException {
        Debug.logInfo("Initial TCP threads and devices ...", MODULE);
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int i = threadGroup.activeCount();
        Debug.logInfo("--- thread group[" + threadGroup.getName() + "] has [" + i + "] threads.", MODULE);
        Thread[] threads = new Thread[i];
        threadGroup.enumerate(threads, true);
        for (Thread threadInstance : threads) {
            if (threadInstance instanceof MqttClientThread thread) {
                if (thread.getName().equals(OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + group1_id + "/" + group1_device1_id)) {
                    Debug.logInfo("--- Found group1_device1_tcp_thread", MODULE);
                    group1_device1_tcp_thread = thread;
                    group1_device1_tcp = (OnenetMqttDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + group1_id + "/" + group1_device2_id)) {
                    Debug.logInfo("--- Found group1_device2_tcp_thread", MODULE);
                    group1_device2_tcp_thread = thread;
                    group1_device2_tcp = (OnenetMqttDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + group2_id + "/" + group2_device1_id)) {
                    Debug.logInfo("--- Found group2_device1_tcp_thread", MODULE);
                    group2_device1_tcp_thread = thread;
                    group2_device1_tcp = (OnenetMqttDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + group2_id + "/" + group2_device2_id)) {
                    Debug.logInfo("--- Found group2_device2_tcp_thread", MODULE);
                    group2_device2_tcp_thread = thread;
                    group2_device2_tcp = (OnenetMqttDevice) thread.getMqttDevice();
                }
            }
        }

        if (group1_device1_tcp_thread == null) {
            group1_device1_tcp = new OnenetMqttDevice(group1_name, group1_id, group1_device1_name,
                    group1_device1_id, group1_device1_key);
            group1_device1_tcp_thread = new MqttClientThread(threadGroup,
                    OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + group1_id + "/" + group1_device1_id, group1_device1_tcp);
            group1_device1_tcp_thread.start();
        }

        if (group1_device2_tcp_thread == null) {
            group1_device2_tcp = new OnenetMqttDevice(group1_name, group1_id, group1_device2_name,
                    group1_device2_id, group1_device2_key);
            group1_device2_tcp_thread = new MqttClientThread(threadGroup,
                    OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + group1_id + "/" + group1_device2_id, group1_device2_tcp);
            group1_device2_tcp_thread.start();
        }

        if (group2_device1_tcp_thread == null) {
            group2_device1_tcp = new OnenetMqttDevice(group2_name, group2_id, group2_device1_name,
                    group2_device1_id, group2_device1_key);
            group2_device1_tcp_thread = new MqttClientThread(threadGroup,
                    OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + group2_id + "/" + group2_device1_id, group2_device1_tcp);
            group2_device1_tcp_thread.start();
        }

        if (group2_device2_tcp_thread == null) {
            group2_device2_tcp = new OnenetMqttDevice(group2_name, group2_id, group2_device2_name,
                    group2_device2_id, group2_device2_key);
            group2_device2_tcp_thread = new MqttClientThread(threadGroup,
                    OnenetMqttDevice.ONENET_SERVER_TCP_SCHEMA + group2_id + "/" + group2_device2_id, group2_device2_tcp);
            group2_device2_tcp_thread.start();
        }
        Debug.logInfo("--- Now thread group[" + threadGroup.getName() + "] has [" + threadGroup.activeCount() + "] threads.", MODULE);
    }

    private void initialSslDevices() throws Exception {
        Debug.logInfo("Initial SSL threads and devices ...", MODULE);
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int i = threadGroup.activeCount();
        Debug.logInfo("--- thread group[" + threadGroup.getName() + "] has [" + i + "] threads.", MODULE);
        Thread[] threads = new Thread[i];
        threadGroup.enumerate(threads, true);
        for (Thread threadInstance : threads) {
            if (threadInstance instanceof MqttClientThread thread) {
                if (thread.getName().equals(OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + group1_id + "/" + group1_device1_id)) {
                    Debug.logInfo("--- Found group1_device1_ssl_thread", MODULE);
                    group1_device1_ssl_thread = thread;
                    group1_device1_ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + group1_id + "/" + group1_device2_id)) {
                    Debug.logInfo("--- Found group1_device2_ssl_thread", MODULE);
                    group1_device2_ssl_thread = thread;
                    group1_device2_ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + group2_id + "/" + group2_device1_id)) {
                    Debug.logInfo("--- Found group2_device1_ssl_thread", MODULE);
                    group2_device1_ssl_thread = thread;
                    group2_device1_ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + group2_id + "/" + group2_device2_id)) {
                    Debug.logInfo("--- Found group2_device2_ssl_thread", MODULE);
                    group2_device2_ssl_thread = thread;
                    group2_device2_ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                }
            }
        }

        if (group1_device1_ssl_thread == null) {
            group1_device1_ssl = new OnenetMqttsDevice(group1_name, group1_id, group1_device1_name,
                    group1_device1_id, group1_device1_key);
            group1_device1_ssl_thread = new MqttClientThread(threadGroup,
                    OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + group1_id + "/" + group1_device1_id, group1_device1_ssl);
            group1_device1_ssl_thread.start();
        }

        if (group1_device2_ssl_thread == null) {
            group1_device2_ssl = new OnenetMqttsDevice(group1_name, group1_id, group1_device2_name,
                    group1_device2_id, group1_device2_key);
            group1_device2_ssl_thread = new MqttClientThread(threadGroup,
                    OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + group1_id + "/" + group1_device2_id, group1_device2_ssl);
            group1_device2_ssl_thread.start();
        }

        if (group2_device1_ssl_thread == null) {
            group2_device1_ssl = new OnenetMqttsDevice(group2_name, group2_id, group2_device1_name,
                    group2_device1_id, group2_device1_key);
            group2_device1_ssl_thread = new MqttClientThread(threadGroup,
                    OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + group2_id + "/" + group2_device1_id, group2_device1_ssl);
            group2_device1_ssl_thread.start();
        }

        if (group2_device2_ssl_thread == null) {
            group2_device2_ssl = new OnenetMqttsDevice(group2_name, group2_id, group2_device2_name,
                    group2_device2_id, group2_device2_key);
            group2_device2_ssl_thread = new MqttClientThread(threadGroup,
                    OnenetMqttsDevice.ONENET_SERVER_SSL_SCHEMA + group2_id + "/" + group2_device2_id, group2_device2_ssl);
            group2_device2_ssl_thread.start();
        }

        Debug.logInfo("--- Now thread group[" + threadGroup.getName() + "] has [" + threadGroup.activeCount() + "] threads.", MODULE);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( FunctionTests.class );
    }

    public void testAll() throws Exception {
        initialTcpDevices();
        tcpFunctionTests();
        tearDownTcpDevices();
        Debug.logInfo("Start waiting 10 seconds TCP devices disconnect to OneNET MQTT server...", MODULE);
        Thread.sleep(10000);

        initialSslDevices();
        sslFunctionTests();
        tearDownSslDevices();
        Debug.logInfo("=== FunctionTests has been finished successfully. ===", MODULE);
    }

    private void tcpFunctionTests() {
        // use group1 key to get info of group1 device1 and device2, should be successful
        assertNotNull(getDeviceInfo(group1_device1_tcp, group1_id, group1_key));
        assertNotNull(getDeviceInfo(group1_device2_tcp, group1_id, group1_key));

        // use group2 key to get info of group2 device1 and device2, should be successful
        assertNotNull(getDeviceInfo(group2_device1_tcp, group2_id, group2_key));
        assertNotNull(getDeviceInfo(group2_device2_tcp, group2_id, group2_key));

        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(group1_device1_tcp, group2_id, group2_key));
        assertNull(getDeviceInfo(group1_device2_tcp, group2_id, group2_key));

        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(group2_device1_tcp, group1_id, group1_key));
        assertNull(getDeviceInfo(group2_device2_tcp, group1_id, group1_key));

        // subscribe publish accepted topic and rejected topic
        assertTrue(group1_device1_tcp.subPubResponse());
        assertTrue(group1_device2_tcp.subPubResponse());
        assertTrue(group2_device1_tcp.subPubResponse());
        assertTrue(group2_device2_tcp.subPubResponse());

        // test publish right and wrong format data
        assertTrue(publishDp(group1_device1_tcp));
        assertTrue(publishDp(group1_device2_tcp));
        assertTrue(publishDp(group2_device1_tcp));
        assertTrue(publishDp(group2_device2_tcp));

        assertTrue(pubWrongFormatDp(group1_device1_tcp));
        assertTrue(pubWrongFormatDp(group1_device2_tcp));
        assertTrue(pubWrongFormatDp(group2_device1_tcp));
        assertTrue(pubWrongFormatDp(group2_device2_tcp));

        // test command subscribe
        assertTrue(deviceSubCmd(group1_device1_tcp));
        assertTrue(deviceSubCmd(group1_device2_tcp));
        assertTrue(deviceSubCmd(group2_device1_tcp));
        assertTrue(deviceSubCmd(group2_device2_tcp));

        // send a command from http api by the same group id and key
        assertTrue(runCommandTest(group1_device1_tcp, group1_id, group1_key, "ipaddress"));
        assertTrue(runCommandTest(group1_device2_tcp, group1_id, group1_key, "test2"));
        assertTrue(runCommandTest(group2_device1_tcp, group2_id, group2_key, "test3"));
        assertTrue(runCommandTest(group2_device2_tcp, group2_id, group2_key, "test4"));
    }

    private void sslFunctionTests() {
        // use group1 key to get info of group1 device1 and device2, should be successful
        assertNotNull(getDeviceInfo(group1_device1_ssl, group1_id, group1_key));
        assertNotNull(getDeviceInfo(group1_device2_ssl, group1_id, group1_key));

        // use group2 key to get info of group2 device1 and device2, should be successful
        assertNotNull(getDeviceInfo(group2_device1_ssl, group2_id, group2_key));
        assertNotNull(getDeviceInfo(group2_device2_ssl, group2_id, group2_key));

        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(group1_device1_ssl, group2_id, group2_key));
        assertNull(getDeviceInfo(group1_device2_ssl, group2_id, group2_key));

        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(group2_device1_ssl, group1_id, group1_key));
        assertNull(getDeviceInfo(group2_device2_ssl, group1_id, group1_key));

        // subscribe publish accepted topic and rejected topic
        assertTrue(group1_device1_ssl.subPubResponse());
        assertTrue(group1_device2_ssl.subPubResponse());
        assertTrue(group2_device1_ssl.subPubResponse());
        assertTrue(group2_device2_ssl.subPubResponse());

        // test publish right and wrong format data
        assertTrue(publishDp(group1_device1_ssl));
        assertTrue(publishDp(group1_device2_ssl));
        assertTrue(publishDp(group2_device1_ssl));
        assertTrue(publishDp(group2_device2_ssl));

        assertTrue(pubWrongFormatDp(group1_device1_ssl));
        assertTrue(pubWrongFormatDp(group1_device2_ssl));
        assertTrue(pubWrongFormatDp(group2_device1_ssl));
        assertTrue(pubWrongFormatDp(group2_device2_ssl));

        // test command subscribe
        assertTrue(deviceSubCmd(group1_device1_ssl));
        assertTrue(deviceSubCmd(group1_device2_ssl));
        assertTrue(deviceSubCmd(group2_device1_ssl));
        assertTrue(deviceSubCmd(group2_device2_ssl));

        // send a command from http api by the same group id and key
        assertTrue(runCommandTest(group1_device1_ssl, group1_id, group1_key, "ipaddress"));
        assertTrue(runCommandTest(group1_device2_ssl, group1_id, group1_key, "test2"));
        assertTrue(runCommandTest(group2_device1_ssl, group2_id, group2_key, "test3"));
        assertTrue(runCommandTest(group2_device2_ssl, group2_id, group2_key, "test4"));
    }

    /**
     * Get device info.
     *
     * @param device        a device instance of OnenetMqttClient
     * @param groupKey      group key
     * @return String       device info
     */
    private String getDeviceInfo(OnenetMqttDevice device, String groupId, String groupKey) {
        OnenetHttpApi httpApi = new OnenetHttpApi(groupId, groupKey);
        return OnenetHttpApi.getDeviceInfo(httpApi.getApiToken(), device);
    }

    /**
     * Test device subscribe command request and response topics.
     */
    private boolean deviceSubCmd(OnenetMqttDevice device) {
        return device.subCmdRequest() && device.subCmdResponse();
    }

    private boolean publishDp(OnenetMqttDevice device) {
        Debug.logInfo("Test group1_device1 publish a data point.", MODULE);
        HashMap<String, String> values = new HashMap<>();
        Random random = new Random();
        values.put("temp", String.valueOf(28 + random.nextInt(10)));
        values.put("hum", 40 + random.nextInt(10) + "%");
        String dataPub = CommonUtils.generateDataPubContent(values);
        Debug.logInfo("Data point to publish: " + dataPub, MODULE);
        try {
            device.publish(CommonUtils.getDataPubTopic(device.getGroupId(), device.getDeviceName()),
                    0, dataPub.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Throwable e) {
            Debug.logInfo(e, MODULE);
            return false;
        }
    }

    private boolean pubWrongFormatDp(OnenetMqttDevice device) {
        Debug.logInfo("Test " + device.getDeviceName() + " publish a data point with format wrong.", MODULE);
        HashMap<String, String> values = new HashMap<>();
        Random random = new Random();
        values.put("temp", String.valueOf(28 + random.nextInt(10)));
        values.put("hum", 40 + random.nextInt(10) + "%");
        String dataPub = "make format wrong. " + CommonUtils.generateDataPubContent(values);
        Debug.logInfo(" Data to pub: " + dataPub, MODULE);
        try {
            device.publish(CommonUtils.getDataPubTopic(device.getGroupId(), device.getDeviceName()),
                    0, dataPub.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Throwable e) {
            Debug.logInfo(e, MODULE);
            return false;
        }
    }

    private boolean runCommandTest(OnenetMqttDevice device, String groupId, String groupKey, String command) {
        OnenetHttpApi httpApi = new OnenetHttpApi(groupId, groupKey);
        String result = OnenetHttpApi.sendCommand(httpApi.getApiToken(), device.getDeviceId(), command);
        if (OnenetHttpApi.isSuccessResponse(result)) {
            Map<String, ? extends Object> cmdResponse = OnenetHttpApi.parseSuccessResponse(result);
            Debug.logInfo("--- Command[" + command + "] success: " + (cmdResponse != null ? cmdResponse.get("result") : result), MODULE);
            return true;
        } else {
            Debug.logInfo("--- Command[" + command + "] error: " + result, MODULE);
            return false;
        }
    }
    
    private void tearDownTcpDevices() throws Exception {
        Debug.logInfo("Tear down OneNET mqtt TCP devices...", MODULE);
        if (group1_device1_tcp != null && group1_device1_tcp.isConnected()) {
            group1_device1_tcp.disconnect();
            Debug.logInfo("--- Tcp device [" + group1_device1_tcp.getDeviceName() + "] disconnected.", MODULE);
        }
        if (group1_device1_tcp_thread != null && group1_device1_tcp_thread.isAlive()) {
            group1_device1_tcp_thread.interrupt();
        }
        if (group1_device2_tcp != null && group1_device2_tcp.isConnected()) {
            group1_device2_tcp.disconnect();
            Debug.logInfo("--- Tcp device [" + group1_device2_tcp.getDeviceName() + "] disconnected.", MODULE);
        }
        if (group1_device2_tcp_thread != null && group1_device2_tcp_thread.isAlive()) {
            group1_device2_tcp_thread.interrupt();
        }
        if (group2_device1_tcp != null && group2_device1_tcp.isConnected()) {
            group2_device1_tcp.disconnect();
            Debug.logInfo("--- Tcp device [" + group2_device1_tcp.getDeviceName() + "] disconnected.", MODULE);
        }
        if (group2_device1_tcp_thread != null && group2_device1_tcp_thread.isAlive()) {
            group2_device1_tcp_thread.interrupt();
        }
        if (group2_device2_tcp != null && group2_device2_tcp.isConnected()) {
            group2_device2_tcp.disconnect();
            Debug.logInfo("--- Tcp device [" + group2_device2_tcp.getDeviceName() + "] disconnected.", MODULE);
        }
        if (group2_device2_tcp_thread != null && group2_device2_tcp_thread.isAlive()) {
            group2_device2_tcp_thread.interrupt();
        }
        Debug.logInfo("... Tear down tcp devices completed", MODULE);
    }

    private void tearDownSslDevices() throws Exception {
        Debug.logInfo("Tear down OneNET mqtt SSL devices...", MODULE);
        if (group1_device1_ssl != null && group1_device1_ssl.isConnected()) {
            group1_device1_ssl.disconnect();
            Debug.logInfo("--- Ssl device [" + group1_device1_ssl.getDeviceName() + "] disconnected.", MODULE);
        }
        if (group1_device1_ssl_thread != null && group1_device1_ssl_thread.isAlive()) {
            group1_device1_ssl_thread.interrupt();
        }
        if (group1_device2_ssl != null && group1_device2_ssl.isConnected()) {
            group1_device2_ssl.disconnect();
            Debug.logInfo("--- Ssl device [" + group1_device2_ssl.getDeviceName() + "] disconnected.", MODULE);
        }
        if (group1_device2_ssl_thread != null && group1_device2_ssl_thread.isAlive()) {
            group1_device2_ssl_thread.interrupt();
        }
        if (group2_device1_ssl != null && group2_device1_ssl.isConnected()) {
            group2_device1_ssl.disconnect();
            Debug.logInfo("--- Ssl device [" + group2_device1_ssl.getDeviceName() + "] disconnected.", MODULE);
        }
        if (group2_device1_ssl_thread != null && group2_device1_ssl_thread.isAlive()) {
            group2_device1_ssl_thread.interrupt();
        }
        if (group2_device2_ssl != null && !group2_device2_ssl.isConnected()) {
            group2_device2_ssl.disconnect();
            Debug.logInfo("--- Ssl device [" + group2_device2_ssl.getDeviceName() + "] disconnected.", MODULE);
        }
        if (group2_device2_ssl_thread != null && group2_device2_ssl_thread.isAlive()) {
            group2_device2_ssl_thread.interrupt();
        }
        Debug.logInfo("... Tear down ssl devices completed", MODULE);
    }
}
