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

    protected static OnenetMqttDevice group1_device1_tcp = null;
    protected static OnenetMqttDevice group1_device2_tcp = null;
    protected static OnenetMqttDevice group2_device1_tcp = null;
    protected static OnenetMqttDevice group2_device2_tcp = null;

    protected MqttClientThread group1_device1_tcp_thread;
    protected MqttClientThread group1_device2_tcp_thread;
    protected MqttClientThread group2_device1_tcp_thread;
    protected MqttClientThread group2_device2_tcp_thread;
    protected MqttClientThread group1_device1_ssl_thread;
    protected MqttClientThread group1_device2_ssl_thread;
    protected MqttClientThread group2_device1_ssl_thread;
    protected MqttClientThread group2_device2_ssl_thread;

    protected static OnenetMqttsDevice group1_device1_ssl = null;
    protected static OnenetMqttsDevice group1_device2_ssl = null;
    protected static OnenetMqttsDevice group2_device1_ssl = null;
    protected static OnenetMqttsDevice group2_device2_ssl = null;

    // Group1 devices
    protected static final String group1_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.name");
    protected static final String group1_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.id");
    protected static final String group1_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.key");
    protected static final String group1_device1_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device1.name");
    protected static final String group1_device1_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device1.id");
    protected static final String group1_device1_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device1.key");
    protected static final String group1_device2_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device2.name");
    protected static final String group1_device2_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device2.id");
    protected static final String group1_device2_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group1.device2.key");

    // Group2 devices
    protected static final String group2_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.name");
    protected static final String group2_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.id");
    protected static final String group2_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.key");
    protected static final String group2_device1_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device1.name");
    protected static final String group2_device1_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device1.id");
    protected static final String group2_device1_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device1.key");
    protected static final String group2_device2_name = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device2.name");
    protected static final String group2_device2_id = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device2.id");
    protected static final String group2_device2_key = UtilProperties.getPropertyValue("mqttTest", "mqtt.onenet.group2.device2.key");

    protected void initialTcpDevices(String MODULE) throws MqttException {
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

    protected void initialSslDevices(String MODULE) throws Exception {
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

    protected boolean publishDp(OnenetMqttDevice device, String MODULE) {
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

    protected boolean pubWrongFormatDp(OnenetMqttDevice device, String MODULE) {
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

    protected boolean runCommandTest(OnenetMqttDevice device, String groupId, String groupKey, String command, String MODULE) {
        OnenetHttpApi httpApi = new OnenetHttpApi(groupId, groupKey);
        String result = OnenetHttpApi.sendCommand(httpApi.getApiToken(), device.getDeviceId(), command);
        if (OnenetHttpApi.isSuccessResponse(result)) {
            Map<String, ?> cmdResponse = OnenetHttpApi.parseSuccessResponse(result);
            Debug.logInfo("--- Command[" + command + "] success: " + (cmdResponse != null ? cmdResponse.get("result") : result), MODULE);
            return true;
        } else {
            Debug.logInfo("--- Command[" + command + "] error: " + result, MODULE);
            return false;
        }
    }

    protected void tearDownTcpDevices(String MODULE) throws Exception {
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

    protected void tearDownSslDevices(String MODULE) throws Exception {
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
