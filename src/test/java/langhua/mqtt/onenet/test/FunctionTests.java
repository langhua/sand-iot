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
public class FunctionTests extends AbstractTestMqtt {
    private static final String MODULE = FunctionTests.class.getName();

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( FunctionTests.class );
    }

    public void testAll() throws Exception {
        initialTcpDevices(MODULE);
        tcpFunctionTests();
        tearDownTcpDevices(MODULE);

        initialSslDevices(MODULE);
        sslFunctionTests();
        tearDownSslDevices(MODULE);
        Debug.logInfo("=== FunctionTests has been finished successfully. ===", MODULE);
    }

    private void tcpFunctionTests() {
        // use group1 key to get info of group1 device1 and device2, should be successful
        assertNotNull(getDeviceInfo(group1_device1_tcp, group1_id, group1_key));
        assertNotNull(getDeviceInfo(group1_device2_tcp, group1_id, group1_key));

        // use group2 key to get info of group2 device1 and device2, should be successful
        assertNotNull(getDeviceInfo(group2_device1_tcp, group2_id, group2_key));
        assertNotNull(getDeviceInfo(group2_device2_tcp, group2_id, group2_key));

        // subscribe publish accepted topic and rejected topic
        assertTrue(group1_device1_tcp.subPubResponse());
        assertTrue(group1_device2_tcp.subPubResponse());
        assertTrue(group2_device1_tcp.subPubResponse());
        assertTrue(group2_device2_tcp.subPubResponse());

        // test publish right and wrong format data
        assertTrue(publishDp(group1_device1_tcp, MODULE));
        assertTrue(publishDp(group1_device2_tcp, MODULE));
        assertTrue(publishDp(group2_device1_tcp, MODULE));
        assertTrue(publishDp(group2_device2_tcp, MODULE));

        assertTrue(pubWrongFormatDp(group1_device1_tcp, MODULE));
        assertTrue(pubWrongFormatDp(group1_device2_tcp, MODULE));
        assertTrue(pubWrongFormatDp(group2_device1_tcp, MODULE));
        assertTrue(pubWrongFormatDp(group2_device2_tcp, MODULE));

        // test command subscribe
        assertTrue(deviceSubCmd(group1_device1_tcp));
        assertTrue(deviceSubCmd(group1_device2_tcp));
        assertTrue(deviceSubCmd(group2_device1_tcp));
        assertTrue(deviceSubCmd(group2_device2_tcp));

        // send a command from http api by the same group id and key
        assertTrue(runCommandTest(group1_device1_tcp, group1_id, group1_key, "ipaddress", MODULE));
        assertTrue(runCommandTest(group1_device2_tcp, group1_id, group1_key, "test2", MODULE));
        assertTrue(runCommandTest(group2_device1_tcp, group2_id, group2_key, "test3", MODULE));
        assertTrue(runCommandTest(group2_device2_tcp, group2_id, group2_key, "test4", MODULE));
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
        assertTrue(publishDp(group1_device1_ssl, MODULE));
        assertTrue(publishDp(group1_device2_ssl, MODULE));
        assertTrue(publishDp(group2_device1_ssl, MODULE));
        assertTrue(publishDp(group2_device2_ssl, MODULE));

        assertTrue(pubWrongFormatDp(group1_device1_ssl, MODULE));
        assertTrue(pubWrongFormatDp(group1_device2_ssl, MODULE));
        assertTrue(pubWrongFormatDp(group2_device1_ssl, MODULE));
        assertTrue(pubWrongFormatDp(group2_device2_ssl, MODULE));

        // test command subscribe
        assertTrue(deviceSubCmd(group1_device1_ssl));
        assertTrue(deviceSubCmd(group1_device2_ssl));
        assertTrue(deviceSubCmd(group2_device1_ssl));
        assertTrue(deviceSubCmd(group2_device2_ssl));

        // send a command from http api by the same group id and key
        assertTrue(runCommandTest(group1_device1_ssl, group1_id, group1_key, "ipaddress", MODULE));
        assertTrue(runCommandTest(group1_device2_ssl, group1_id, group1_key, "test2", MODULE));
        assertTrue(runCommandTest(group2_device1_ssl, group2_id, group2_key, "test3", MODULE));
        assertTrue(runCommandTest(group2_device2_ssl, group2_id, group2_key, "test4", MODULE));
    }
}
