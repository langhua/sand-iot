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
import junit.framework.TestSuite;
import org.apache.ofbiz.base.util.Debug;

/**
 * Test case for intergroup isolation.
 */
public class IsolationTests extends AbstractTestMqtt {
    private static final String MODULE = IsolationTests.class.getName();

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( IsolationTests.class );
    }

    public void testAll() throws Exception {
        initialTcpDevices(MODULE);
        tcpIsolationTests();
        tearDownTcpDevices(MODULE);
        Debug.logInfo("Start waiting 10 seconds TCP devices disconnect to OneNET MQTT server...", MODULE);
        Thread.sleep(10000);

        initialSslDevices(MODULE);
        sslIsolationTests();
        tearDownSslDevices(MODULE);
        Debug.logInfo("Start waiting 10 seconds SSL devices disconnect to OneNET MQTTs server...", MODULE);
        Thread.sleep(10000);
        Debug.logInfo("=== IsolationTests has been finished successfully. ===", MODULE);
    }

    private void tcpIsolationTests() {
        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(group1_device1_tcp, group2_id, group2_key));
        assertNull(getDeviceInfo(group1_device2_tcp, group2_id, group2_key));

        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(group2_device1_tcp, group1_id, group1_key));
        assertNull(getDeviceInfo(group2_device2_tcp, group1_id, group1_key));

        // subscribe publish accepted topic and rejected topic to make tcp device online
        assertTrue(group1_device1_tcp.subPubResponse());
        assertTrue(group1_device2_tcp.subPubResponse());
        assertTrue(group2_device1_tcp.subPubResponse());
        assertTrue(group2_device2_tcp.subPubResponse());

        // subscribe topics always success no matter what the topic is, but only messages belong itself can be received
        Debug.logInfo("Test publish data point message isolation in the same group...", MODULE);
        assertTrue(group1_device1_tcp.subPubResponse(group1_device2_tcp));
        assertTrue(group1_device2_tcp.subPubResponse(group1_device1_tcp));
        assertTrue(group2_device1_tcp.subPubResponse(group2_device2_tcp));
        assertTrue(group2_device2_tcp.subPubResponse(group2_device1_tcp));

        // test publish right and wrong format data
        assertTrue(publishDp(group1_device1_tcp, MODULE));
        assertTrue(publishDp(group1_device2_tcp, MODULE));
        assertTrue(publishDp(group2_device1_tcp, MODULE));
        assertTrue(publishDp(group2_device2_tcp, MODULE));

        assertTrue(pubWrongFormatDp(group1_device1_tcp, MODULE));
        assertTrue(pubWrongFormatDp(group1_device2_tcp, MODULE));
        assertTrue(pubWrongFormatDp(group2_device1_tcp, MODULE));
        assertTrue(pubWrongFormatDp(group2_device2_tcp, MODULE));
        Debug.logInfo("... End of Test publish isolation in the same group", MODULE);

        // subscribe topics always success no matter what the topic is, but only messages belong itself can be received
        Debug.logInfo("Test publish message isolation between groups...", MODULE);
        assertTrue(group1_device1_tcp.subPubResponse(group2_device1_tcp));
        assertTrue(group1_device2_tcp.subPubResponse(group2_device2_tcp));
        assertTrue(group2_device1_tcp.subPubResponse(group1_device1_tcp));
        assertTrue(group2_device2_tcp.subPubResponse(group1_device2_tcp));

        // test publish right and wrong format data
        assertTrue(publishDp(group1_device1_tcp, MODULE));
        assertTrue(publishDp(group1_device2_tcp, MODULE));
        assertTrue(publishDp(group2_device1_tcp, MODULE));
        assertTrue(publishDp(group2_device2_tcp, MODULE));

        assertTrue(pubWrongFormatDp(group1_device1_tcp, MODULE));
        assertTrue(pubWrongFormatDp(group1_device2_tcp, MODULE));
        assertTrue(pubWrongFormatDp(group2_device1_tcp, MODULE));
        assertTrue(pubWrongFormatDp(group2_device2_tcp, MODULE));
        Debug.logInfo("... End of Test isolation between groups", MODULE);

        // test command subscribe
        assertTrue(deviceSubCmd(group1_device1_tcp));
        assertTrue(deviceSubCmd(group1_device2_tcp));
        assertTrue(deviceSubCmd(group2_device1_tcp));
        assertTrue(deviceSubCmd(group2_device2_tcp));

        // send a command from http api by the other group id and key, should fail
        assertFalse(runCommandTest(group1_device1_tcp, group2_id, group2_key, "ipaddress", MODULE));
        assertFalse(runCommandTest(group1_device2_tcp, group2_id, group2_key, "test2", MODULE));
        assertFalse(runCommandTest(group2_device1_tcp, group1_id, group1_key, "test3", MODULE));
        assertFalse(runCommandTest(group2_device2_tcp, group1_id, group1_key, "test4", MODULE));
    }

    private void sslIsolationTests() {
        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(group1_device1_ssl, group2_id, group2_key));
        assertNull(getDeviceInfo(group1_device2_ssl, group2_id, group2_key));

        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(group2_device1_ssl, group1_id, group1_key));
        assertNull(getDeviceInfo(group2_device2_ssl, group1_id, group1_key));

        // subscribe publish accepted topic and rejected topic to make tcp device online
        assertTrue(group1_device1_ssl.subPubResponse());
        assertTrue(group1_device2_ssl.subPubResponse());
        assertTrue(group2_device1_ssl.subPubResponse());
        assertTrue(group2_device2_ssl.subPubResponse());

        // subscribe topics always success no matter what the topic is, but only messages belong itself can be received
        Debug.logInfo("Test publish data point message isolation in the same group...", MODULE);
        assertTrue(group1_device1_ssl.subPubResponse(group1_device2_ssl));
        assertTrue(group1_device2_ssl.subPubResponse(group1_device1_ssl));
        assertTrue(group2_device1_ssl.subPubResponse(group2_device2_ssl));
        assertTrue(group2_device2_ssl.subPubResponse(group2_device1_ssl));

        // test publish right and wrong format data
        assertTrue(publishDp(group1_device1_ssl, MODULE));
        assertTrue(publishDp(group1_device2_ssl, MODULE));
        assertTrue(publishDp(group2_device1_ssl, MODULE));
        assertTrue(publishDp(group2_device2_ssl, MODULE));

        assertTrue(pubWrongFormatDp(group1_device1_ssl, MODULE));
        assertTrue(pubWrongFormatDp(group1_device2_ssl, MODULE));
        assertTrue(pubWrongFormatDp(group2_device1_ssl, MODULE));
        assertTrue(pubWrongFormatDp(group2_device2_ssl, MODULE));
        Debug.logInfo("... End of Test publish isolation in the same group", MODULE);

        // subscribe topics always success no matter what the topic is, but only messages belong itself can be received
        Debug.logInfo("Test publish message isolation between groups...", MODULE);
        assertTrue(group1_device1_ssl.subPubResponse(group2_device1_ssl));
        assertTrue(group1_device2_ssl.subPubResponse(group2_device2_ssl));
        assertTrue(group2_device1_ssl.subPubResponse(group1_device1_ssl));
        assertTrue(group2_device2_ssl.subPubResponse(group1_device2_ssl));

        // test publish right and wrong format data
        assertTrue(publishDp(group1_device1_ssl, MODULE));
        assertTrue(publishDp(group1_device2_ssl, MODULE));
        assertTrue(publishDp(group2_device1_ssl, MODULE));
        assertTrue(publishDp(group2_device2_ssl, MODULE));

        assertTrue(pubWrongFormatDp(group1_device1_ssl, MODULE));
        assertTrue(pubWrongFormatDp(group1_device2_ssl, MODULE));
        assertTrue(pubWrongFormatDp(group2_device1_ssl, MODULE));
        assertTrue(pubWrongFormatDp(group2_device2_ssl, MODULE));
        Debug.logInfo("... End of Test isolation between groups", MODULE);

        // command subscribe
        assertTrue(deviceSubCmd(group1_device1_ssl));
        assertTrue(deviceSubCmd(group1_device2_ssl));
        assertTrue(deviceSubCmd(group2_device1_ssl));
        assertTrue(deviceSubCmd(group2_device2_ssl));

        // send a command from http api by another group id and key and should be failed
        assertFalse(runCommandTest(group1_device1_ssl, group2_id, group2_key, "test5", MODULE));
        assertFalse(runCommandTest(group1_device2_ssl, group2_id, group2_key, "test6", MODULE));
        assertFalse(runCommandTest(group2_device1_ssl, group1_id, group1_key, "test7", MODULE));
        assertFalse(runCommandTest(group2_device2_ssl, group1_id, group1_key, "test8", MODULE));
    }
}
