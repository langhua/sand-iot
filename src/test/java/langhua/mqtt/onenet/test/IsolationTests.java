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

    public IsolationTests(String name) {
        super(name);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(IsolationTests.class);
    }

    /**
     * Tests entrance.
     */
    public void testAll() throws Exception {
        initialTcpDevices(MODULE);
        tcpIsolationTests();
        tearDownTcpDevices(MODULE);

        initialSslDevices(MODULE);
        sslIsolationTests();
        tearDownSslDevices(MODULE);
        Debug.logInfo("=== IsolationTests has been finished successfully. ===", MODULE);
    }

    private void tcpIsolationTests() {
        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(getGroup1Device1Tcp(), GROUP2_ID, GROUP2_KEY));
        assertNull(getDeviceInfo(getGroup1Device2Tcp(), GROUP2_ID, GROUP2_KEY));

        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(getGroup2Device1Tcp(), GROUP1_ID, GROUP1_KEY));
        assertNull(getDeviceInfo(getGroup2Device2Tcp(), GROUP1_ID, GROUP1_KEY));

        // subscribe publish accepted topic and rejected topic to make tcp device online
        assertTrue(getGroup1Device1Tcp().subPubResponse());
        assertTrue(getGroup1Device2Tcp().subPubResponse());
        assertTrue(getGroup2Device1Tcp().subPubResponse());
        assertTrue(getGroup2Device2Tcp().subPubResponse());

        // subscribe topics always success no matter what the topic is, but only messages belong itself can be received
        Debug.logInfo("Test publish data point message isolation in the same group...", MODULE);
        assertTrue(getGroup1Device1Tcp().subPubResponse(getGroup1Device2Tcp()));
        assertTrue(getGroup1Device2Tcp().subPubResponse(getGroup1Device1Tcp()));
        assertTrue(getGroup2Device1Tcp().subPubResponse(getGroup2Device2Tcp()));
        assertTrue(getGroup2Device2Tcp().subPubResponse(getGroup2Device1Tcp()));

        // test publish right and wrong format data
        assertTrue(publishDp(getGroup1Device1Tcp(), MODULE));
        assertTrue(publishDp(getGroup1Device2Tcp(), MODULE));
        assertTrue(publishDp(getGroup2Device1Tcp(), MODULE));
        assertTrue(publishDp(getGroup2Device2Tcp(), MODULE));

        assertTrue(pubWrongFormatDp(getGroup1Device1Tcp(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup1Device2Tcp(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup2Device1Tcp(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup2Device2Tcp(), MODULE));
        Debug.logInfo("... End of Test publish isolation in the same group", MODULE);

        // subscribe topics always success no matter what the topic is, but only messages belong itself can be received
        Debug.logInfo("Test publish message isolation between groups...", MODULE);
        assertTrue(getGroup1Device1Tcp().subPubResponse(getGroup2Device1Tcp()));
        assertTrue(getGroup1Device2Tcp().subPubResponse(getGroup2Device2Tcp()));
        assertTrue(getGroup2Device1Tcp().subPubResponse(getGroup1Device1Tcp()));
        assertTrue(getGroup2Device2Tcp().subPubResponse(getGroup1Device2Tcp()));

        // test publish right and wrong format data
        assertTrue(publishDp(getGroup1Device1Tcp(), MODULE));
        assertTrue(publishDp(getGroup1Device2Tcp(), MODULE));
        assertTrue(publishDp(getGroup2Device1Tcp(), MODULE));
        assertTrue(publishDp(getGroup2Device2Tcp(), MODULE));

        assertTrue(pubWrongFormatDp(getGroup1Device1Tcp(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup1Device2Tcp(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup2Device1Tcp(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup2Device2Tcp(), MODULE));
        Debug.logInfo("... End of Test isolation between groups", MODULE);

        // test command subscribe
        assertTrue(deviceSubCmd(getGroup1Device1Tcp()));
        assertTrue(deviceSubCmd(getGroup1Device2Tcp()));
        assertTrue(deviceSubCmd(getGroup2Device1Tcp()));
        assertTrue(deviceSubCmd(getGroup2Device2Tcp()));

        // send a command from http api by the other group id and key, should fail
        assertFalse(runCommandTest(getGroup1Device1Tcp(), GROUP2_ID, GROUP2_KEY, "ipaddress", MODULE));
        assertFalse(runCommandTest(getGroup1Device2Tcp(), GROUP2_ID, GROUP2_KEY, "test2", MODULE));
        assertFalse(runCommandTest(getGroup2Device1Tcp(), GROUP1_ID, GROUP1_KEY, "test3", MODULE));
        assertFalse(runCommandTest(getGroup2Device2Tcp(), GROUP1_ID, GROUP1_KEY, "test4", MODULE));
    }

    private void sslIsolationTests() {
        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(getGroup1Device1Ssl(), GROUP2_ID, GROUP2_KEY));
        assertNull(getDeviceInfo(getGroup1Device2Ssl(), GROUP2_ID, GROUP2_KEY));

        // use group1 key to get info of group2 device1 and device2, should be NULL
        assertNull(getDeviceInfo(getGroup2Device1Ssl(), GROUP1_ID, GROUP1_KEY));
        assertNull(getDeviceInfo(getGroup2Device2Ssl(), GROUP1_ID, GROUP1_KEY));

        // subscribe publish accepted topic and rejected topic to make tcp device online
        assertTrue(getGroup1Device1Ssl().subPubResponse());
        assertTrue(getGroup1Device2Ssl().subPubResponse());
        assertTrue(getGroup2Device1Ssl().subPubResponse());
        assertTrue(getGroup2Device2Ssl().subPubResponse());

        // subscribe topics always success no matter what the topic is, but only messages belong itself can be received
        Debug.logInfo("Test publish data point message isolation in the same group...", MODULE);
        assertTrue(getGroup1Device1Ssl().subPubResponse(getGroup1Device2Ssl()));
        assertTrue(getGroup1Device2Ssl().subPubResponse(getGroup1Device1Ssl()));
        assertTrue(getGroup2Device1Ssl().subPubResponse(getGroup2Device2Ssl()));
        assertTrue(getGroup2Device2Ssl().subPubResponse(getGroup2Device1Ssl()));

        // test publish right and wrong format data
        assertTrue(publishDp(getGroup1Device1Ssl(), MODULE));
        assertTrue(publishDp(getGroup1Device2Ssl(), MODULE));
        assertTrue(publishDp(getGroup2Device1Ssl(), MODULE));
        assertTrue(publishDp(getGroup2Device2Ssl(), MODULE));

        assertTrue(pubWrongFormatDp(getGroup1Device1Ssl(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup1Device2Ssl(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup2Device1Ssl(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup2Device2Ssl(), MODULE));
        Debug.logInfo("... End of Test publish isolation in the same group", MODULE);

        // subscribe topics always success no matter what the topic is, but only messages belong itself can be received
        Debug.logInfo("Test publish message isolation between groups...", MODULE);
        assertTrue(getGroup1Device1Ssl().subPubResponse(getGroup2Device1Ssl()));
        assertTrue(getGroup1Device2Ssl().subPubResponse(getGroup2Device2Ssl()));
        assertTrue(getGroup2Device1Ssl().subPubResponse(getGroup1Device1Ssl()));
        assertTrue(getGroup2Device2Ssl().subPubResponse(getGroup1Device2Ssl()));

        // test publish right and wrong format data
        assertTrue(publishDp(getGroup1Device1Ssl(), MODULE));
        assertTrue(publishDp(getGroup1Device2Ssl(), MODULE));
        assertTrue(publishDp(getGroup2Device1Ssl(), MODULE));
        assertTrue(publishDp(getGroup2Device2Ssl(), MODULE));

        assertTrue(pubWrongFormatDp(getGroup1Device1Ssl(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup1Device2Ssl(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup2Device1Ssl(), MODULE));
        assertTrue(pubWrongFormatDp(getGroup2Device2Ssl(), MODULE));
        Debug.logInfo("... End of Test isolation between groups", MODULE);

        // command subscribe
        assertTrue(deviceSubCmd(getGroup1Device1Ssl()));
        assertTrue(deviceSubCmd(getGroup1Device2Ssl()));
        assertTrue(deviceSubCmd(getGroup2Device1Ssl()));
        assertTrue(deviceSubCmd(getGroup2Device2Ssl()));

        // send a command from http api by another group id and key and should be failed
        assertFalse(runCommandTest(getGroup1Device1Ssl(), GROUP2_ID, GROUP2_KEY, "test5", MODULE));
        assertFalse(runCommandTest(getGroup1Device2Ssl(), GROUP2_ID, GROUP2_KEY, "test6", MODULE));
        assertFalse(runCommandTest(getGroup2Device1Ssl(), GROUP1_ID, GROUP1_KEY, "test7", MODULE));
        assertFalse(runCommandTest(getGroup2Device2Ssl(), GROUP1_ID, GROUP1_KEY, "test8", MODULE));
    }
}
