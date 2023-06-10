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

import java.io.ByteArrayInputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class OnenetMqttsDevice extends OnenetMqttDevice {

    private static final String MODULE = OnenetMqttsDevice.class.getName();

    // see https://open.iot.10086.cn/doc/mqtt/book/device-develop/manual.html
    public static String ONENET_SERVER_SSL_SCHEMA = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.server.ssl.schema");
    protected static String ONENET_SERVER_SSL_IP = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.server.ssl.ip");
    protected static String ONENET_SERVER_SSL_PORT = UtilProperties.getPropertyValue("mqtt", "mqtt.onenet.server.ssl.port");

    private static final String ONENET_SERVER_PERM = "-----BEGIN CERTIFICATE-----\r\n" +
        "MIIDOzCCAiOgAwIBAgIJAPCCNfxANtVEMA0GCSqGSIb3DQEBCwUAMDQxCzAJBgNV\r\n" +
        "BAYTAkNOMQ4wDAYDVQQKDAVDTUlPVDEVMBMGA1UEAwwMT25lTkVUIE1RVFRTMB4X\r\n" +
        "DTE5MDUyOTAxMDkyOFoXDTQ5MDUyMTAxMDkyOFowNDELMAkGA1UEBhMCQ04xDjAM\r\n" +
        "BgNVBAoMBUNNSU9UMRUwEwYDVQQDDAxPbmVORVQgTVFUVFMwggEiMA0GCSqGSIb3\r\n" +
        "DQEBAQUAA4IBDwAwggEKAoIBAQC/VvJ6lGWfy9PKdXKBdzY83OERB35AJhu+9jkx\r\n" +
        "5d4SOtZScTe93Xw9TSVRKrFwu5muGgPusyAlbQnFlZoTJBZY/745MG6aeli6plpR\r\n" +
        "r93G6qVN5VLoXAkvqKslLZlj6wXy70/e0GC0oMFzqSP0AY74icANk8dUFB2Q8usS\r\n" +
        "UseRafNBcYfqACzF/Wa+Fu/upBGwtl7wDLYZdCm3KNjZZZstvVB5DWGnqNX9HkTl\r\n" +
        "U9NBMS/7yph3XYU3mJqUZxryb8pHLVHazarNRppx1aoNroi+5/t3Fx/gEa6a5PoP\r\n" +
        "ouH35DbykmzvVE67GUGpAfZZtEFE1e0E/6IB84PE00llvy3pAgMBAAGjUDBOMB0G\r\n" +
        "A1UdDgQWBBTTi/q1F2iabqlS7yEoX1rbOsz5GDAfBgNVHSMEGDAWgBTTi/q1F2ia\r\n" +
        "bqlS7yEoX1rbOsz5GDAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQAL\r\n" +
        "aqJ2FgcKLBBHJ8VeNSuGV2cxVYH1JIaHnzL6SlE5q7MYVg+Ofbs2PRlTiWGMazC7\r\n" +
        "q5RKVj9zj0z/8i3ScWrWXFmyp85ZHfuo/DeK6HcbEXJEOfPDvyMPuhVBTzuBIRJb\r\n" +
        "41M27NdIVCdxP6562n6Vp0gbE8kN10q+ksw8YBoLFP0D1da7D5WnSV+nwEIP+F4a\r\n" +
        "3ZX80bNt6tRj9XY0gM68mI60WXrF/qYL+NUz+D3Lw9bgDSXxpSN8JGYBR85BxBvR\r\n" +
        "NNAhsJJ3yoAvbPUQ4m8J/CoVKKgcWymS1pvEHmF47pgzbbjm5bdthlIx+swdiGFa\r\n" +
        "WzdhzTYwVkxBaU+xf/2w\r\n" +
        "-----END CERTIFICATE-----\r\n";

    public OnenetMqttsDevice(String groupName, String groupId, String deviceName, String deviceId, String deviceKey) throws MqttException {
        super(groupName, groupId, deviceName, deviceId, deviceKey);
    }

    /**
     * Initialize OneNET uses groupId as username, deviceName as clientId.
     *
     * @throws Exception
     */
    public void init() {
        brokerUrl = ONENET_SERVER_SSL_SCHEMA + ONENET_SERVER_SSL_IP + ":" + ONENET_SERVER_SSL_PORT;

        try {
            // Construct the object that contains connection parameters
            // such as cleanSession and LWT
            conOpt = new MqttConnectOptions();
            conOpt.setCleanSession(clean);
            if (UtilValidate.isNotEmpty(password)) {
                conOpt.setPassword(password.toCharArray());
            }
            if (UtilValidate.isNotEmpty(groupId)) {
                conOpt.setUserName(groupId);
            }
            conOpt.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            conOpt.setConnectionTimeout(30);
            conOpt.setKeepAliveInterval(10);
            conOpt.setAutomaticReconnect(true);
            
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(null, getTrustManagers(), null);
            SSLSocketFactory socketFactory = context.getSocketFactory();
            
            conOpt.setSocketFactory(socketFactory);
            conOpt.setHttpsHostnameVerificationEnabled(false);
            // Construct the MqttClient instance
            client = new MqttAsyncClient(brokerUrl, deviceName, dataStore);

            // Set this wrapper as the callback handler
            client.setCallback(this);
        } catch (Exception e) {
            Debug.logError("Unable to set up client: " + e.getMessage(), MODULE);
            client = null;
        }
    }

    private static TrustManager[] getTrustManagers() throws Exception {
        return new TrustManager[] { new TrustOnenetManager() };
    }

    private static class TrustOnenetManager implements X509TrustManager {
        X509Certificate onenetServerCert;
        
        public TrustOnenetManager() throws Exception {
            super();
            CertificateFactory certf = CertificateFactory.getInstance("X.509");
            // InputStream caIn = TrustOnenetManager.class.getResourceAsStream("/serverCert.pem");
            // onenetServerCert = (X509Certificate) cAf.generateCertificate(caIn);
            onenetServerCert = (X509Certificate) certf.generateCertificate(new ByteArrayInputStream(ONENET_SERVER_PERM.getBytes()));
        }
         
        public void checkClientTrusted(X509Certificate[] certs, String string) throws CertificateException {
            Debug.logVerbose("Trusting (un-trusted) client certificate chain:", MODULE);
            for (X509Certificate cert: certs) {
                Debug.logVerbose("---- " + cert.getSubjectX500Principal().getName() + " valid: " + cert.getNotAfter(), MODULE);
            }
        }

        public void checkServerTrusted(X509Certificate[] certs, String string) throws CertificateException {
            Debug.logVerbose("Trusting (un-trusted) server certificate chain:", MODULE);
            for (X509Certificate cert: certs) {
                Debug.logVerbose("---- " + cert.getSubjectX500Principal().getName() + " valid: " + cert.getNotAfter(), MODULE);
                if (cert.equals(onenetServerCert)) {
                    Debug.logVerbose("---- Server cert trusted", MODULE);
                    return;
                }
            }
            throw new CertificateException("Onenet server cert NOT found.");
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}