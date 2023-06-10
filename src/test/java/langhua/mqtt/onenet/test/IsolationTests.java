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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import langhua.mqtt.common.MqttClientThread;
import langhua.mqtt.onenet.services.OnenetHttpApi;
import langhua.mqtt.onenet.services.OnenetMqttDevice;
import langhua.mqtt.onenet.services.OnenetMqttsDevice;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import langhua.mqtt.onenet.utils.CommonUtils;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilProperties;

/**
 * Test case for intergroup isolation.
 */
public class IsolationTests extends TestCase {
    private static final String MODULE = IsolationTests.class.getName();

    private static OnenetMqttDevice group1_device1_tcp = null;
    private static OnenetMqttDevice group1_device2_tcp = null;
    private static OnenetMqttDevice group2_device1_tcp = null;
    private static OnenetMqttDevice group2_device2_tcp = null;

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

    public IsolationTests() throws Exception {
        Debug.logInfo("Setting up iot mqtt test ...", MODULE);
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        int i = threadGroup.activeCount();
        Debug.logInfo("--- thread group[" + threadGroup.getName() + "] has [" + i + "] threads.", MODULE);
        Thread[] threads = new Thread[i];
        threadGroup.enumerate(threads, true);
        for (Thread threadInstance : threads) {
            if (threadInstance instanceof MqttClientThread thread) {
                if (thread.getName().equals(group1_device1_id)) {
                    Debug.logInfo("--- Found group1_device1_ssl_thread", MODULE);
                    group1_device1_ssl_thread = thread;
                    group1_device1_ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(group1_device2_id)) {
                    Debug.logInfo("--- Found group1_device2_ssl_thread", MODULE);
                    group1_device2_ssl_thread = thread;
                    group1_device2_ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(group2_device1_id)) {
                    Debug.logInfo("--- Found group2_device1_ssl_thread", MODULE);
                    group2_device1_ssl_thread = thread;
                    group2_device1_ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                } else if (thread.getName().equals(group2_device2_id)) {
                    Debug.logInfo("--- Found group2_device2_ssl_thread", MODULE);
                    group2_device2_ssl_thread = thread;
                    group2_device2_ssl = (OnenetMqttsDevice) thread.getMqttDevice();
                }
            }
        }

        if (group1_device1_ssl_thread == null) {
            group1_device1_ssl = new OnenetMqttsDevice(group1_name, group1_id, group1_device1_name,
                    group1_device1_id, group1_device1_key);
            group1_device1_ssl_thread = new MqttClientThread(threadGroup, group1_device1_id, group1_device1_ssl);
            group1_device1_ssl_thread.start();
        }

        if (group1_device2_ssl_thread == null) {
            group1_device2_ssl = new OnenetMqttsDevice(group1_name, group1_id, group1_device2_name,
                    group1_device2_id, group1_device2_key);
            group1_device2_ssl_thread = new MqttClientThread(threadGroup, group1_device2_id, group1_device2_ssl);
            group1_device2_ssl_thread.start();
        }

        if (group2_device1_ssl_thread == null) {
            group2_device1_ssl = new OnenetMqttsDevice(group2_name, group2_id, group2_device1_name,
                    group2_device1_id, group2_device1_key);
            group2_device1_ssl_thread = new MqttClientThread(threadGroup, group2_device1_id, group2_device1_ssl);
            group2_device1_ssl_thread.start();
        }

        if (group2_device2_ssl_thread == null) {
            group2_device2_ssl = new OnenetMqttsDevice(group2_name, group2_id, group2_device2_name,
                    group2_device2_id, group2_device2_key);
            group2_device2_ssl_thread = new MqttClientThread(threadGroup, group2_device2_id, group2_device2_ssl);
            group2_device2_ssl_thread.start();
        }
        Debug.logInfo("--- Now thread group[" + threadGroup.getName() + "] has [" + threadGroup.activeCount() + "] threads.", MODULE);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( IsolationTests.class );
    }

    public void testAll() {
        tcpIsolationTests();
        sslIsolationTests();
    }

    private void tcpIsolationTests() {

    }

    private void sslIsolationTests() {
        // cross subscribe in a group
        assertTrue(group1_device1_ssl.subPubResponse());
        assertTrue(group1_device2_ssl.subPubResponse());
        assertTrue(group2_device1_ssl.subPubResponse());
        assertTrue(group2_device2_ssl.subPubResponse());


        // send a command from http api by another group id and key
        assertFalse(runCommandTest(group1_device1_ssl, group2_id, group2_key, "test5"));
        assertFalse(runCommandTest(group1_device2_ssl, group2_id, group2_key, "test6"));
        assertFalse(runCommandTest(group2_device1_ssl, group1_id, group1_key, "test7"));
        assertFalse(runCommandTest(group2_device2_ssl, group1_id, group1_key, "test8"));

        //
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

    private boolean publishDp(OnenetMqttsDevice device) {
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

    private boolean pubWrongFormatDp(OnenetMqttsDevice device) {
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

    /**
     * Test group1 device1 CAN subscribe group1 device2.
     */
//    private void group1Device1SubDevice2() {
////        assertTrue("group1 device1 CAN subscribe device2 command request", group1_device1_ssl.subCmdRequest(group1_device2_ssl));
////        assertTrue("group1 device1 CAN subscribe device2 command response", group1_device1_ssl.subCmdResponse(group1_device2_ssl));
//
//        String command = "ipaddress";
//        try {
//            assertTrue(runCommandTest(group1_device1_ssl, group1_key, command));
//            assertTrue(runCommandTest(group1_device2_ssl, group1_key, command));
//        } catch (Throwable e) {
//            Debug.logError(e.getMessage(), MODULE);
//        }
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            Debug.logError(e.getMessage(), MODULE);
//        }
//    }

    private boolean runCommandTest(OnenetMqttsDevice device, String groupId, String groupKey, String command) {
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
    
    /**
     * Test group2 internal visit.
     * @throws Throwable 
     */
//    public void testGroup2Internal() throws Throwable {
//        System.out.println();
//        System.out.println("====== test group2 internal visit ======");
//        String command = "ipaddress";
//
//        group2_device2.subCmdRequest(group2_device1);
//        group2_device2.subCmdResponse(group2_device1);
//
//        assertTrue(runCommandTest(group2_device1, group2_key, command));
//        System.out.println();
//        assertTrue(runCommandTest(group2_device2, group2_key, command));
//        System.out.println();
//        Thread.sleep(10000);
//    }
    
    /**
     * Test group1 external visit.
     */
//    public void testGroup1External() throws Throwable {
//        System.out.println();
//        System.out.println("====== test group1 external visit ======");
//        String command = "ipaddress";
//        System.out.println("--- et=" + (System.currentTimeMillis()/1000 + 86400));
//
//        group2_device1.subCmdRequest(group1_device1);
//        group2_device1.subCmdResponse(group1_device1);
//
//        assertFalse(runCommandTest(group1_device1, group2_key, command));
//        System.out.println();
//        assertFalse(runCommandTest(group1_device2, group2_key, command));
//        System.out.println();
//
//        Thread.sleep(10000);
//    }
    
    /**
     * Test external visit: group1 device1 subscribe group2 device1.
     */
//    public void testGroup2External() throws Throwable {
//        String command = "ipaddress";
//
//        group1_device1.subCmdRequest(group2_device1);
//        group1_device1.subCmdResponse(group2_device1);
//
//        assertFalse(runCommandTest(group2_device1, group1_key, command));
//        assertFalse(runCommandTest(group2_device2, group1_key, command));
//        Thread.sleep(10000);
//    }

    protected void tearDown() throws Exception {
        Debug.logInfo("Tear down iot mqtt tests.", MODULE);
        if (group1_device1_ssl != null && !group1_device1_ssl.isConnected()) {
            group1_device1_ssl.disconnect();
        }
        if (group1_device2_ssl != null && !group1_device2_ssl.isConnected()) {
            group1_device2_ssl.disconnect();
        }
        if (group2_device1_ssl != null && !group2_device1_ssl.isConnected()) {
            group2_device1_ssl.disconnect();
        }
        if (group2_device2_ssl != null && !group2_device2_ssl.isConnected()) {
            group2_device2_ssl.disconnect();
        }
    }
}
