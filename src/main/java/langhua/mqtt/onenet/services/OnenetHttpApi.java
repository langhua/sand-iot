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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import com.google.gson.JsonSyntaxException;
import langhua.mqtt.onenet.utils.CommonUtils;
import okhttp3.*;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilProperties;

public class OnenetHttpApi {
    private static final String MODULE = OnenetHttpApi.class.getName();
    private static final String ONENET_API_URL = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.api.url");
    private static final String DEVICE_INFO_URI = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.device.info.uri");
    private static final String REGISTER_DEVICE_URI = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.register.device.uri");
    private static final String DELETE_DEVICE_URI = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.delete.device.uri");
    private static final String CHANGE_DEVICE_KEY_URI = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.change.device.key.uri");
    private static final String SEND_COMMAND_URI = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.send.command.uri");

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType PLAIN = MediaType.parse("application/plain; charset=utf-8");
    
    private final String token;
    
    public OnenetHttpApi(String groupId, String groupKey) {
        this.token = CommonUtils.generateApiToken(groupId, groupKey);
    }

    public String getApiToken() {
        return token;
    }

    @SuppressWarnings("rawtypes")
    public static String getDeviceInfo(String apiToken, OnenetMqttDevice device) {
        OkHttpClient client = new OkHttpClient.Builder()
                                              .readTimeout(100, TimeUnit.SECONDS)
                                              .connectTimeout(60, TimeUnit.SECONDS)
                                              .writeTimeout(60, TimeUnit.SECONDS)
                                              .connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES))
                                              .build();
        String url = ONENET_API_URL + DEVICE_INFO_URI.replaceFirst("device_name", device.getDeviceName());
        Debug.logInfo("Get device info url: " + url, MODULE);
        Request request = new Request.Builder()
                                     .url(url)
                                     .addHeader("Authorization", apiToken)
                                     .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String result = response.body().string();
                Gson gson = new Gson();
                HashMap deviceInfo = gson.fromJson(result, HashMap.class);
                Map data = (Map) deviceInfo.get("data");
                if (data != null && data.containsKey("device_id")) {
                    String deviceId = (String) data.get("device_id");
                    if (deviceId != null && !deviceId.isEmpty()) {
                        device.setDeviceId(deviceId);
                        return result;
                    }
                }
            }
        } catch (IOException e) {
            Debug.logError(e, MODULE);
        }
        return null;
    }

    public static String sendCommand(String apiToken, String deviceId, String command) {
        Debug.logInfo("Send command: [" + apiToken + ", " + deviceId + ", " + command + "]", MODULE);
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(100, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES))
                .build();
        String url = ONENET_API_URL + SEND_COMMAND_URI.replaceFirst("deviceId", deviceId);
        Debug.logInfo("Command Url: " + url, MODULE);
        RequestBody body = RequestBody.create(command, PLAIN);
        Request request = new Request.Builder()
                                     .url(url)
                                     .addHeader("Authorization", apiToken)
                                     .post(body)
                                     .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null)
                return response.body().string();
        } catch (IOException e) {
            Debug.logError(e, MODULE);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static boolean isSuccessResponse(String response) {
        if (response == null) return false;
        Gson gson = new Gson();
        HashMap<String, Object> result = gson.fromJson(response, HashMap.class);
        return result != null && !result.isEmpty() && "success".equals(result.get("error"));
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, ?> parseSuccessResponse(String response) {
        if (response == null)
            return null;
        Gson gson = new Gson();
        HashMap<String, ?> result = gson.fromJson(response, HashMap.class);
        if (result == null || result.isEmpty() || !"success".equals(result.get("error")))
            return null;
        Map<String, ?> data = (Map<String, ?>) result.get("data");
        if (data == null || data.isEmpty())
            return null;
        String cmdResponse = (String) data.get("cmd_resp");
        if (cmdResponse == null)
            return null;
        cmdResponse = new String(Base64.getDecoder().decode(cmdResponse), StandardCharsets.UTF_8);
        try {
            result = gson.fromJson(cmdResponse, HashMap.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
        return result;
    }
}
