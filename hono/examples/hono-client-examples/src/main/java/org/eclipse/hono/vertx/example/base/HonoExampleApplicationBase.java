/*******************************************************************************
 * Copyright (c) 2016, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.hono.vertx.example.base;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletionException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.apache.kafka.common.security.scram.internals.ScramMechanism;
import org.eclipse.hono.application.client.ApplicationClient;
import org.eclipse.hono.application.client.DownstreamMessage;
import org.eclipse.hono.application.client.MessageConsumer;
import org.eclipse.hono.application.client.MessageContext;
import org.eclipse.hono.application.client.kafka.impl.KafkaApplicationClientImpl;
import org.eclipse.hono.client.kafka.CommonKafkaClientConfigProperties;
import org.eclipse.hono.client.kafka.consumer.MessagingKafkaConsumerConfigProperties;
import org.eclipse.hono.client.kafka.producer.CachingKafkaProducerFactory;
import org.eclipse.hono.client.kafka.producer.MessagingKafkaProducerConfigProperties;
import org.eclipse.hono.config.FileFormat;
import org.eclipse.hono.vertx.example.base.model.TemperatureSensor;
import org.eclipse.hono.vertx.example.base.model.Button;
import org.eclipse.hono.vertx.example.base.model.BrightnessSensor;

import com.google.gson.Gson;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.quarkus.runtime.Quarkus;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;


/**
 * Example base class for consuming telemetry and event data from devices connected to Hono and sending commands to these devices.
 * <p>
 * This class implements all necessary code to get Hono's messaging consumer client and Hono's command client running.
 * <p>
 * The code consumes data until it receives any input on its console (which finishes it and closes vertx).
 */
@SuppressFBWarnings(
        value = { "HARD_CODE_PASSWORD", "PREDICTABLE_RANDOM" },
        justification = """
                We use the default passwords of the Hono Sandbox installation throughout this class
                for ease of use. The passwords are publicly documented and do not affect any
                private installations of Hono.
                The values returned by the Random are only used as arbitrary values in example message
                payload.
                """)
public class HonoExampleApplicationBase {

    public static final String HONO_CLIENT_USER = System.getProperty("username", "hono");
    public static final String HONO_CLIENT_PASSWORD = System.getProperty("password", "hono-secret");
    public static final Boolean USE_PLAIN_CONNECTION =
            Boolean.valueOf(System.getProperty("plain.connection", "false"));
    public static final Boolean SEND_ONE_WAY_COMMANDS =
            Boolean.valueOf(System.getProperty("sendOneWayCommands", "false"));
    public static final Boolean USE_KAFKA = Boolean.valueOf(System.getProperty("kafka", "true"));
    private static final String MESSAGE_TYPE_TELEMETRY = "telemetry";
    private static final String MESSAGE_TYPE_EVENT = "event";

    ApplicationClient<? extends MessageContext> client;
    private final Set<String> supportedMessageTypes = new HashSet<>();
    private final Vertx vertx = Vertx.vertx();
    private final List<String> brightnessSensors =  new ArrayList<>(Arrays.asList(
            "033b5620-60a9-49f9-8419-1e79c7c19b97",
            "5cdb8243-b0d3-480e-ac4c-729a3329b2fa",
            "43a50b16-34ca-42f0-998d-835efb291567",
            "94594bd0-4789-4258-981b-c5f8a517c47f",
            "514f5fac-1de6-4b9e-ae0d-069ce0658654",
            "5341e96b-f6ef-470d-8c96-4e3ec2c98f5d"
    ));
    private final List<String> temperatureSensors =  new ArrayList<>(Arrays.asList(
            "5167c837-c70b-4a1e-9e88-1d64446b9a01"
    ));
    private final List<String> buttons =  new ArrayList<>(Arrays.asList(
            "d564aab8-09d2-4885-95cd-24ff936d635c"
    ));


    /**
     * Helper method to set credentials.
     */
    private String scramJaasConfig(final String username, final String password) {
        return """
                %s required username="%s" password="%s";
                """.formatted(ScramLoginModule.class.getName(), username, password);
    }
    /**
     * Creates an application client for Kafka based messaging. Unlike with AMQP, the Kafka clients manage their
     * connections to the cluster internally.
     * <p>
     * NB: if you want to integrate this code with your own software, it might be necessary to copy the trust store to
     * your project as well and adopt the file path.
     */


    Future<KafkaApplicationClientImpl> createKafkaClient() {

        final var commonProps = new HashMap<String, String>();
        final String bootstrapServers;
        //change path to match location of truststore.pem.
        final String path = "truststore.pem";
        commonProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, path);

        Optional.ofNullable(FileFormat.detect(path))
            .ifPresent(fileFormat -> commonProps.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, fileFormat.name()));

        bootstrapServers = "127.0.0.1:9094";
        commonProps.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        commonProps.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
        commonProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
        commonProps.put(SaslConfigs.SASL_MECHANISM, ScramMechanism.SCRAM_SHA_512.mechanismName());
        commonProps.put(
                SaslConfigs.SASL_JAAS_CONFIG,
                scramJaasConfig("hono", "hono-secret"));

        final var commonClientConfig = new CommonKafkaClientConfigProperties();
        commonClientConfig.setCommonClientConfig(commonProps);

        final var consumerProps = new MessagingKafkaConsumerConfigProperties();
        consumerProps.setCommonClientConfig(commonClientConfig);
        final var producerProps = new MessagingKafkaProducerConfigProperties();
        producerProps.setCommonClientConfig(commonClientConfig);

        System.err.printf("Connecting to Kafka based messaging infrastructure [%s]%n", bootstrapServers);
        final var kafkaClient = new KafkaApplicationClientImpl(
                vertx,
                consumerProps,
                CachingKafkaProducerFactory.sharedFactory(vertx),
                producerProps);
        return startClientAndWaitForReadiness(kafkaClient)
                .map(ok -> {
                    this.client = kafkaClient;
                    return kafkaClient;
                }).onSuccess(ok -> System.out.println("client started"))
                .onFailure(err -> System.out.println("failed to create client " + err));
    }

    private Future<Void> startClientAndWaitForReadiness(final ApplicationClient<? extends MessageContext> client) {
        final Promise<Void> readyTracker = Promise.promise();
        client.addOnClientReadyHandler(result -> {
            if (result.succeeded()) {
                readyTracker.complete();
                System.out.println("completed"); // Client is ready, complete the promise
            } else {
                readyTracker.fail(result.cause());
                System.out.println("failed"); // Error occurred, fail the promise
            }
        });
        return client.start()
                .compose(ok -> readyTracker.future());
    }

    /**
     * Creates an application client for Kafka based messaging. Unlike with AMQP, the Kafka clients manage their
     * connections to the cluster internally.
     * <p>
     * NB: if you want to integrate this code with your own software, it might be necessary to copy the trust store to
     * your project as well and adopt the file path.
     * @return Application
     */
    public Future<ApplicationClient<? extends MessageContext>> getApplicationClient() {
        final Promise<ApplicationClient<? extends MessageContext>> result = Promise.promise();
        if (client != null) {
            result.complete(client);
        } else if (!USE_KAFKA) {
        } else {
            createKafkaClient()
                .onSuccess(result::complete)
                .onFailure(result::fail);
        }
        return result.future();
    }

    /**
     * Start the application client and set the message handling method to treat data that is received.
     */
    protected void consumeData() {
        supportedMessageTypes.add(MESSAGE_TYPE_EVENT);
        supportedMessageTypes.add(MESSAGE_TYPE_TELEMETRY);

        try {
            getApplicationClient()
                .compose(this::createConsumers)
                .onSuccess(ok -> System.err.println("""
                        Consuming messages for tenant [%s], ctrl-c to exit.
                        """.formatted(HonoExampleConstants.TENANT_ID)))
                .toCompletionStage()
                .toCompletableFuture()
                .join();
            Quarkus.waitForExit();
            return;
        } catch (final CompletionException e) {
            System.out.println(e.getCause());
            System.err.println("failed to create message consumer(s): %s".formatted(e.getMessage()));
            return;
        }
    }

    /**
     * Creates a consumer to consume messages.
     */

    //consumer
    private Future<Void> createConsumers(final ApplicationClient<? extends MessageContext> client) {

        final Handler<Throwable> closeHandler = cause -> {
            System.err.println("peer has closed message consumer(s) unexpectedly, trying to reopen ...");
            vertx.setTimer(1000L, reconnect -> {
                createConsumers(client);
            });
        };

        final List<Future<MessageConsumer>> consumerFutures = new ArrayList<>();
        if (supportedMessageTypes.contains(MESSAGE_TYPE_EVENT)) {
            consumerFutures.add(
                    client.createEventConsumer(
                            HonoExampleConstants.TENANT_ID,
                            msg -> {
                                printMessage(MESSAGE_TYPE_EVENT, msg);
                                processMessage(msg);
                            },
                            closeHandler));
        }

        if (supportedMessageTypes.contains(MESSAGE_TYPE_TELEMETRY)) {
            consumerFutures.add(
                    client.createTelemetryConsumer(
                            HonoExampleConstants.TENANT_ID,
                            msg -> {
                                printMessage(MESSAGE_TYPE_TELEMETRY, msg);
                                processMessage(msg);
                            },
                            closeHandler));
        }

        return Future.all(consumerFutures)
                .mapEmpty();
    }

    private void printMessage(final String endpoint, final DownstreamMessage<? extends MessageContext> message) {

        System.out.println("%s %s %s %s %s".formatted(
                endpoint.charAt(0),
                message.getDeviceId(),
                Optional.ofNullable(message.getContentType()).orElse("-"),
                Optional.ofNullable(message.getPayload())
                    .map(Buffer::toString)
                    .orElse("-"),
                message.getProperties().getPropertiesMap()));
    }

    private void processMessage(final DownstreamMessage<? extends MessageContext> message) {
        // Extract data from the message
        final Map<String, Object> propertiesMap = message.getProperties().getPropertiesMap();
        final String deviceId = message.getDeviceId();
        final String originAdapter = propertiesMap.containsKey("orig_adapter") ? propertiesMap.get("orig_adapter").toString() : "-";
        final long creationTime = message.getCreationTime().toEpochMilli();
        final String origAddress = propertiesMap.containsKey("orig_address") ? propertiesMap.get("orig_address").toString() : "-";
        String payload = Optional.ofNullable(message.getPayload())
                .map(Buffer::toString)
                .orElse("-");
        System.out.println("payload: " + payload);
        // Determine the appropriate endpoint based on the device ID
        String apiEndpoint = null;
        String jsonPayload;
        long startTime = System.currentTimeMillis();
        if (temperatureSensors.contains(deviceId)) {
            int temperature = 0;
            apiEndpoint = "http://localhost:8081/api/temperature-sensor";
            // Parse temperature value from payload
            try {
                JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject();
                temperature = jsonObject.get("temperature").getAsInt();
                System.out.println("jsonObject: "+ jsonObject);
            } catch (Exception e) {
                System.out.println("parsing error: " + e);
            }
            TemperatureSensor temperatureSensor = new TemperatureSensor(deviceId, creationTime, originAdapter, origAddress, temperature);
            jsonPayload = convertTemperatureSensorToJson(temperatureSensor);
        } else if (brightnessSensors.contains(deviceId)) {
            int brightness = 0;
            apiEndpoint = "http://localhost:8081/api/brightness-sensor";
            // Parse brightness value from payload
            try {
                JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject();
                brightness = jsonObject.get("brightness").getAsInt();
            } catch (Exception e) {
                System.out.println("parsing error: " + e);
            }
            BrightnessSensor brightnessSensor = new BrightnessSensor(deviceId, creationTime, originAdapter, origAddress, brightness);
            jsonPayload = convertBrightnessSensorToJson(brightnessSensor);
        } else if (buttons.contains(deviceId)) {
            boolean on = false;
            apiEndpoint = "http://localhost:8081/api/button";
            try {
                JsonObject jsonObject = JsonParser.parseString(payload).getAsJsonObject();
                on = jsonObject.get("on").getAsBoolean();
            } catch (Exception e) {
                System.out.println("parsing error: " + e);
            }
            Button button = new Button(deviceId, creationTime, originAdapter, origAddress, on);
            jsonPayload = convertButtonToJson(button);
        } else {
            jsonPayload = null;
            System.out.println("device doesn't exist");
        }
    
        // Perform API call
        if (apiEndpoint != null) {
            callApi(apiEndpoint, jsonPayload);
            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;
            // Write to CSV
            String csvFile = "output.csv";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile, true))) {
                writer.write(timeTaken + "," + deviceId + "," + payload);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertTemperatureSensorToJson(final TemperatureSensor temperatureSensor) {
        final Gson gson = new Gson();
        return gson.toJson(temperatureSensor);
    }

    private String convertBrightnessSensorToJson(final BrightnessSensor brightnessSensor) {
        final Gson gson = new Gson();
        return gson.toJson(brightnessSensor);
    }

    private String convertButtonToJson(final Button button) {
        final Gson gson = new Gson();
        return gson.toJson(button);
    }

    private void callApi(String apiEndpoint, final String payload) {
        try {
            final URL url = new URL(apiEndpoint);
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Write payload to the connection output stream
            try (OutputStream os = conn.getOutputStream()) {
                final byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Print response code
            System.out.println("Response code: " + conn.getResponseCode());

            // Close the connection
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
