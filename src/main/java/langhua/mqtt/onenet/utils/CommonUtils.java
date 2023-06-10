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
package langhua.mqtt.onenet.utils;

import langhua.mqtt.common.Utils;
import org.apache.ofbiz.base.util.Debug;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class CommonUtils {

    private static final String MODULE = CommonUtils.class.getName();

    private static final String ONENET_MQTT_VERSION = "2018-10-31";
    private static final String ONENET_MQTT_HASH_METHOD = "sha256";
    private static final long DEFAULT_EXPIRE_IN_SECONDS = 86400; // 24 hours

    public static String generateMqttPassword(String groupId, String deviceName, String deviceKey) {
        String result;
        // 1. Generate resource
        String resource = "products/" + groupId + "/devices/" + deviceName;
        Debug.logInfo("resource: " + resource, MODULE);

        try {
            // 2. Generate resource byte[] to sign
            long et = System.currentTimeMillis() / 1000 + DEFAULT_EXPIRE_IN_SECONDS;
            String stringToSign = et + "\n" + ONENET_MQTT_HASH_METHOD + "\n" + resource + "\n" + ONENET_MQTT_VERSION;

            // 3. get hmac
            String hmacStr = "Hmac" + ONENET_MQTT_HASH_METHOD.toUpperCase();
            Mac hmac = Mac.getInstance(hmacStr);
            byte[] key = Base64.getDecoder().decode(deviceKey);
            SecretKeySpec secret_key = new SecretKeySpec(key, hmacStr);
            hmac.init(secret_key);

            String hashString = URLEncoder.encode(Base64.getEncoder().encodeToString(hmac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
            result = "version=" + ONENET_MQTT_VERSION + "&res=" + URLEncoder.encode(resource, StandardCharsets.UTF_8) + "&et=" + et + "&method=" + ONENET_MQTT_HASH_METHOD
                   + "&sign=" + hashString;
            Debug.logInfo("groupId[" + groupId + "] device[" + deviceName + "] deviceKey[" + deviceKey + "]:" + result, MODULE);
            return result;
        } catch (Exception e) {
            Debug.logError(e, MODULE);
        }
        return null;
    }
    

    public static String generateApiToken(String groupId, String groupKey) {
        String result;
        // 1. Generate resource
        String resource = "products/" + groupId;

        try {
            // 2. Generate resource byte[] to sign
            long et = System.currentTimeMillis() / 1000 + DEFAULT_EXPIRE_IN_SECONDS;
            String stringToSign = et + "\n" + ONENET_MQTT_HASH_METHOD + "\n" + resource + "\n" + ONENET_MQTT_VERSION;

            // 3. 
            String hmacStr = "Hmac" + ONENET_MQTT_HASH_METHOD.toUpperCase();
            Mac hmac = Mac.getInstance(hmacStr);
            byte[] key = Base64.getDecoder().decode(groupKey);
            SecretKeySpec secret_key = new SecretKeySpec(key, hmacStr);
            hmac.init(secret_key);

            String hashString = URLEncoder.encode(Base64.getEncoder().encodeToString(hmac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
            result = "version=" + ONENET_MQTT_VERSION + "&res=" + URLEncoder.encode(resource, StandardCharsets.UTF_8) + "&et=" + et + "&method=" + ONENET_MQTT_HASH_METHOD
                   + "&sign=" + hashString;
            Debug.logInfo("groupId[" + groupId + "] with key[" + groupKey + "], generated api token[" + result + "]", MODULE);
            return result;
        } catch (Exception e) {
            Debug.logError(e, MODULE);
        }
        return null;
    }

    public static String getCmdRequestTopicPrefix(String groupId, String deviceName) {
        return "$sys/" + groupId + "/" + deviceName + "/cmd/request/";
    }

    /**
     * $sys/{pid}/{device-name}/cmd/request/+
     *
     */
    public static String getCmdRequestTopic(String groupId, String deviceName) {
        return getCmdRequestTopic(groupId, deviceName, "+");
    }

    /**
     * $sys/{pid}/{device-name}/cmd/request/{cmdid}
     *
     * @param groupId       pid, the product id in Onenet document
     * @param deviceName    device-name in Onenet document
     * @return
     */
    public static String getCmdRequestTopic(String groupId, String deviceName, String cmdId) {
        return "$sys/" + groupId + "/" + deviceName + "/cmd/request/" + cmdId;
    }

    public static String getCmdResponseTopic(String groupName, String groupId) {
        return getCmdResponseTopic(groupName, groupId, "#");
    }

    /**
     * $sys/{pid}/{device-name}/cmd/response/{cmdid}
     *
     * @param groupId
     * @param deviceName
     * @param cmdId
     * @return
     */
    public static String getCmdResponseTopic(String groupId, String deviceName, String cmdId) {
        return "$sys/" + groupId + "/" + deviceName + "/cmd/response/" + cmdId;
    }

    public static String getDataPubAcceptedTopic(String groupId, String deviceName) {
        return "$sys/" + groupId + "/" + deviceName + "/dp/post/json/accepted";
    }

    public static String getDataPubRejectedTopic(String groupId, String deviceName) {
        return "$sys/" + groupId + "/" + deviceName + "/dp/post/json/rejected";
    }

    public static String getDataPubTopic(String groupId, String deviceName) {
        return "$sys/" + groupId + "/" + deviceName + "/dp/post/json";
    }

    /**
     * Generate the data map to a json format like this:
     * {
     *     "id": 123,
     *     "dp": {
     *         "temperatrue": [{
     *             "v": 30,
     *         }],
     *         "power": [{
     *             "v": 4.5,
     *         }]
     *     }
     * }
     *
     * @param dataMap
     * @return
     */
    public static String generateDataPubContent(Map<String, String> dataMap) {
        StringBuffer sb = new StringBuffer(8);
        sb.append("{\"id\":");
        sb.append(Utils.getIdByNowTime());
        sb.append(",\"dp\":{");
        sb.append(generateDpContent(dataMap));
        sb.append("}}");
        return sb.toString();
    }

    private static StringBuffer generateDpContent(Map<String, String> dpMap) {
        long now = System.currentTimeMillis()/1000;
        boolean isFirstKey = true;
        StringBuffer sb = new StringBuffer(8);
        for (String key : dpMap.keySet()) {
            Object value = dpMap.get(key);
            if (value != null) {
                if (!isFirstKey) {
                    sb.append(",");
                }
                sb.append("\"");
                sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8).replaceAll(" ", "%20").replaceAll("\\+", "%20"));
                boolean isFirstValue = true;
                sb.append("\":[{\"v\":\"");
                sb.append(value);
                sb.append("\"}]");
                isFirstKey = false;
            }
        }
        return sb;
    }
}