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
package langhua.mqtt.common;

import org.apache.ofbiz.base.util.Debug;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Timestamp;

public abstract class AbstractSandMqttDevice implements MqttCallback {

    private static final String MODULE = AbstractSandMqttDevice.class.getName();

    protected MqttAsyncClient client;

    private static final int BEGIN = 0;
    private static final int CONNECTED = 1;
    private static final int PUBLISHED = 2;
    private static final int SUBSCRIBED = 3;
    private static final int DISCONNECTED = 4;
    private static final int FINISH = 5;
    private static final int ERROR = 6;
    private static final int DISCONNECT = 7;
    private static final int CONNECTING = 8;
    private int state = BEGIN;
    private Throwable ex = null;
    private final Object caller = new Object();
    private boolean donext = false;

    private MqttConnector con;
    protected MqttConnectOptions conOpt;

    private static final int STATE_CHECK_INTERVAL_LONG = 10000;
    private static final int STATE_CHECK_INTERVAL_SHORT = 2000;

    public abstract void init();

    public void disconnect() {
//        Disconnector disc = new Disconnector();
//        disc.doDisconnect();
        try {
            client.disconnect(null, null);
        } catch (MqttException e) {
            Debug.logError(e, MODULE);
        }
    }

    public void connectionLost(Throwable cause) {
        Debug.logError("[" + client.getClientId() +"] Connection Lost from " + client.getServerURI() + ": " + cause.getMessage(), MODULE);
    }

    /**
     * Subscribe to a topic on an MQTT server Once subscribed this method waits for
     * the messages to arrive from the server that match the subscription. It
     * continues listening for messages until the enter key is pressed.
     *
     * @param topicName to subscribe to (can be wild carded)
     * @param qos       the maximum quality of service to receive messages at for
     *                  this subscription
     */
    public void subscribe(String topicName, int qos) throws Throwable {
        /*
         Use a state machine to decide which step to do next. State change occurs
         when a notification is received that an MQTT action has completed
        */
        state = BEGIN;
        while (state != FINISH) {
            switch (state) {
                case BEGIN -> {
                    // Connect using a non-blocking connect
                    if (con == null) {
                        con = new MqttConnector();
                    }
                    if (!isConnected()) {
                        con.doConnect();
                        state = CONNECTING;
                    } else {
                        state = CONNECTED;
                    }
                }
                case CONNECTING -> {
                    if (client.isConnected()) {
                        state = CONNECTED;
                    }
                }
                case CONNECTED -> {
                    // Subscribe using a non-blocking subscribe
                    Subscriber sub = new Subscriber();
                    sub.doSubscribe(topicName, qos);
                }
                case SUBSCRIBED, DISCONNECTED -> {
                    // Block until Enter is pressed allowing messages to arrive
                    state = FINISH;
                    donext = true;
                }
                case DISCONNECT -> {
                    Disconnector disc = new Disconnector();
                    disc.doDisconnect();
                }
                case ERROR -> throw ex;
                default -> {
                    throw new IllegalStateException("Unexpected value: " + state);
                }
            }

            if (BEGIN == state) {
                waitForStateChange(STATE_CHECK_INTERVAL_LONG);
            } else {
                waitForStateChange(STATE_CHECK_INTERVAL_SHORT);
            }
        }
    }

    /**
     * Send a message to an MQTT server to reponse a command
     *
     * @param topicName the name of the topic to publish to
     * @param qos       the quality of service to delivery the message at (0,1,2)
     * @param payload   the set of bytes to send to the MQTT server
     * @throws Throwable
     */
    protected void cmdResponse(String topicName, int qos, byte[] payload) throws Throwable {
        // Use a state machine to decide which step to do next. State change occurs
        // when a notification is received that an MQTT action has completed
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        state = BEGIN;
        while (state != FINISH) {
            switch (state) {
                case BEGIN -> {
                    // Connect using a non-blocking connect
                    if (con == null) {
                        con = new MqttConnector();
                    }
                    if (!client.isConnected()) {
                        con.doConnect();
                        state = CONNECTING;
                    } else {
                        state = CONNECTED;
                    }
                }
                case CONNECTING -> {
                    if (client.isConnected()) {
                        state = CONNECTED;
                    }
                }
                case CONNECTED -> {
                    try {
                        // Publish the message
                        client.publish(topicName, message, null, null);
                    } catch (MqttException e) {
                        state = ERROR;
                        donext = true;
                        ex = e;
                    }
                    // Publish using a non-blocking publisher
                    state = PUBLISHED;
                    donext = true;
                }
                case PUBLISHED, DISCONNECTED -> {
                    state = FINISH;
                    donext = true;
                }
                case DISCONNECT -> {
                    new Disconnector().doDisconnect();
                }
                case ERROR -> throw ex;
            }

            if (state == BEGIN) {
                waitForStateChange(STATE_CHECK_INTERVAL_LONG);
            } else {
                waitForStateChange(STATE_CHECK_INTERVAL_SHORT);
            }
        }
    }

    /**
     * Wait for a maximum amount of time for a state change event to occur
     *
     * @param maxTTW maximum time to wait in milliseconds
     */
    private void waitForStateChange(int maxTTW) throws MqttException {
        synchronized (caller) {
            if (!donext) {
                try {
                    caller.wait(maxTTW);
                } catch (InterruptedException e) {
                    Debug.logError("Timed out." + e.getMessage(), MODULE);
                }

                if (ex != null) {
                    throw (MqttException) ex;
                }
            }
            donext = false;
        }
    }

    /**
     * Disconnect in a non-blocking way and then sit back and wait to be notified
     * that the action has completed.
     */
    public class Disconnector {
        public void doDisconnect() {
            // Disconnect the client
            Debug.logInfo("MQTT Disconnecting clientId[" + client.getClientId() + "] of [" + client.getServerURI() + "] ... ", MODULE);

            IMqttActionListener discListener = new IMqttActionListener() {
                public void onSuccess(IMqttToken asyncActionToken) {
                    Debug.logInfo("Disconnect Completed", MODULE);
                    state = DISCONNECTED;
                    carryOn();
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    Debug.logError("Disconnect failed" + exception, MODULE);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (caller) {
                        donext = true;
                        caller.notifyAll();
                    }
                }
            };

            try {
                client.disconnect(null, null);
            } catch (MqttException e) {
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }

    /**
     * Connect in a non-blocking way and then sit back and wait to be notified that
     * the action has completed.
     */
    public class MqttConnector {

        public MqttConnector() {
        }

        public void doConnect() {
            // Connect to the server
            // Get a token and setup an asynchronous listener on the token which
            // will be notified once the connect completes
            Debug.logInfo("Connecting to " + client.getServerURI() + " with device name[" + client.getClientId() + "]", MODULE);

            IMqttActionListener conListener = new IMqttActionListener() {

                public void onSuccess(IMqttToken asyncActionToken) {
                    Debug.logInfo("Connected.", MODULE);
                    state = CONNECTED;
                    carryOn();
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    Debug.logError("connect failed" + exception.getMessage(), MODULE);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (caller) {
                        donext = true;
                        caller.notifyAll();
                    }
                }
            };

            try {
                // Connect using a non-blocking connect
                client.connect(conOpt, "Connect sample context", conListener);
            } catch (MqttException e) {
                // If though it is a non-blocking connect an exception can be
                // thrown if validation of parms fails or other checks such
                // as already connected fail.
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }

    /**
     * Publish / send a message to an MQTT server
     *
     * @param topicName the name of the topic to publish to
     * @param qos       the quality of service to delivery the message at (0,1,2)
     * @param payload   the set of bytes to send to the MQTT server
     */
    public void publish(String topicName, int qos, byte[] payload) throws Throwable {
        /*
         Use a state machine to decide which step to do next. State change occurs
         when a notification is received that an MQTT action has completed
        */
        state = BEGIN;
        while (state != FINISH) {
            switch (state) {
                case BEGIN -> {
                    // Connect using a non-blocking connect
                    if (con == null) {
                        con = new MqttConnector();
                    }
                    if (!client.isConnected()) {
                        con.doConnect();
                    } else {
                        state = CONNECTED;
                    }
                }
                case CONNECTED -> {
                    // Publish using a non-blocking publisher
                    Publisher pub = new Publisher();
                    pub.doPublish(topicName, qos, payload);
                }
                case PUBLISHED, DISCONNECTED -> {
                    state = FINISH;
                    donext = true;
                }
                case DISCONNECT -> {
                    Disconnector disc = new Disconnector();
                    disc.doDisconnect();
                }
                case ERROR -> throw ex;
            }

            if (state == BEGIN) {
                waitForStateChange(STATE_CHECK_INTERVAL_LONG);
            } else {
                waitForStateChange(STATE_CHECK_INTERVAL_SHORT);
            }
        }
    }

    /**
     * Publish in a non-blocking way and then sit back and wait to be notified that
     * the action has completed.
     */
    public class Publisher {
        public void doPublish(String topicName, int qos, byte[] payload) {
            // Send / publish a message to the server
            // Get a token and setup an asynchronous listener on the token which
            // will be notified once the message has been delivered
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos);

            String time = new Timestamp(System.currentTimeMillis()).toString();
            Debug.logInfo("Publishing at: " + time + " to topic \"" + topicName + "\" qos " + qos, MODULE);

            // Set up a listener object to be notified when the publish completes.
            IMqttActionListener pubListener = new IMqttActionListener() {
                public void onSuccess(IMqttToken asyncActionToken) {
                    Debug.logInfo("Publish Completed", MODULE);
                    state = PUBLISHED;
                    carryOn();
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    Debug.logError("-- publish error: " + asyncActionToken, MODULE);
                    Debug.logError("Publish failed" + exception.getMessage(), MODULE);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (caller) {
                        donext = true;
                        caller.notifyAll();
                    }
                }
            };

            try {
                // Publish the message
                client.publish(topicName, message, "Pub sample context", pubListener);
            } catch (MqttException e) {
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }

    /**
     * Subscribe in a non-blocking way and then sit back and wait to be notified
     * that the action has completed.
     */
    public class Subscriber {
        public void doSubscribe(String topicName, int qos) {
            // Make a subscription
            // Get a token and setup an asynchronous listener on the token which
            // will be notified once the subscription is in place.
            Debug.logInfo("[" + client.getClientId() + "] of server[" + client.getServerURI() + "] Subscribing to topic \"" + topicName + "\" qos " + qos, MODULE);

            IMqttActionListener subListener = new IMqttActionListener() {
                public void onSuccess(IMqttToken asyncActionToken) {
                    Debug.logInfo("Subscribe Completed", MODULE);
                    state = SUBSCRIBED;
                    carryOn();
                }

                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    ex = exception;
                    state = ERROR;
                    Debug.logError("Subscribe failed" + exception.getMessage(), MODULE);
                    carryOn();
                }

                public void carryOn() {
                    synchronized (caller) {
                        donext = true;
                        caller.notifyAll();
                    }
                }
            };

            try {
                client.subscribe(topicName, qos, "Subscribe sample context", subListener);
            } catch (MqttException e) {
                state = ERROR;
                donext = true;
                ex = e;
            }
        }
    }

    public boolean isConnected() {
        if (client == null) {
            return false;
        }
        return client.isConnected();
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            Debug.logInfo(new String(token.getMessage().getPayload()), MODULE);
        } catch (MqttException e) {
            Debug.logError(e.getMessage(), MODULE);
        }
    }
}

