# Hono

### to start jar file:

```powershell
$env:HOST = "127.0.0.1"
$env:PORT = "9094"
$env:USER = "hono"
$env:PASSWORD = "hono-secret"
$env:CA_FILE = "truststore.pem"
$env:DISABLE_HOSTNAME_VERIFICATION = "--disable-hostname-verification"
java -jar hono-cli-2.5.0-exec.jar app --host ${env:HOST} --port ${env:PORT} -u ${env:USER} -p ${env:PASSWORD} --ca-file ${env:CA_FILE} ${env:DISABLE_HOSTNAME_VERIFICATION} consume --tenant ${env:MY_TENANT}
```

### to send telemetry data via MQTT:

```powershell
mosquitto_pub -h "${env:MQTT_ADAPTER_IP}" -p "8883" -u "${env:MY_DEVICE}@${env:MY_TENANT}" -P "${env:MY_PWD}" --cafile "truststore.pem" --insecure -t "telemetry" -m '{"temp": 5}'
```

with: `--cafile "truststore.pem" --insecure` → `$env:MOSQUITTO_OPTIONS`

### to send an event via MQTT:

```powershell
mosquitto_pub -h "${env:MQTT_ADAPTER_IP}" -p "8883" -u "${env:MY_DEVICE}@${env:MY_TENANT}" -P "${env:MY_PWD}" --cafile "truststore.pem" --insecure -t event -q 1 -m '{"alarm": "fire"}'
```

### To send telemetry data via HTTP:

```bash
curl -i -u ${env:MY_DEVICE}@${env:MY_TENANT}:${env:MY_PWD} ${env:CURL_OPTIONS} -H 'Content-Type: application/json' --data-binary '{"temperature": -4}' https://${env:HTTP_ADAPTER_IP}:8443/telemetry
```

### to receive a command

```bash
ow --tenant 953805d9-4e6e-4709-a2b1-532768f318f6 --device cbfa66a4-1692-4bc4-9ba1-ce5eaa24a1ff -n setVolume --payload '{"level": 40}'
```

with:

- `953805d9-4e6e-4709-a2b1-532768f318f6` → `{env:MY_TENANT}`
- `cbfa66a4-1692-4bc4-9ba1-ce5eaa24a1ff` → `${env:MY_DEVICE}`

### to request a command:

```bash
req --tenant 953805d9-4e6e-4709-a2b1-532768f318f6 --device cbfa66a4-1692-4bc4-9ba1-ce5eaa24a1ff -n setBrightness --payload '{"level": 87}'
```

with:

- `953805d9-4e6e-4709-a2b1-532768f318f6` → `{env:MY_TENANT}`
- `cbfa66a4-1692-4bc4-9ba1-ce5eaa24a1ff` → `${env:MY_DEVICE}`

### Create a new Tenant:

```bash
curl -i -X POST --insecure -H "content-type: application/json" --data-binary '{
>>   "ext": {
>>     "messaging-type": "kafka"
>>   }
>> }' https://127.0.0.1:28443/v1/tenants
```

### To register a new device:

```powershell
curl -i -X PUT $ENV:CURL_OPTIONS -H "content-type: application/json" --data-binary @"
>> [{
>>   "type": "hashed-password",
>>   "auth-id": "$ENV:TEMPERATURE_SENSOR1",
>>   "secrets": [{
>>       "pwd-plain": "$ENV:MY_PWD"
>>   }]
>> }]
>> "@ https://${ENV:REGISTRY_IP}:28443/v1/credentials/${ENV:MY_TENANT}/${ENV:TEMPERATURE_SENSOR1}
```

output:

```bash
HTTP/1.1 201 Created
etag: 969729ed-6139-44b0-8924-f86a230a72d0
location: /v1/tenants/e82a672f-1790-4d4a-b66c-8fc4dfdec3e8
content-type: application/json; charset=utf-8
content-length: 45

{"id":"e82a672f-1790-4d4a-b66c-8fc4dfdec3e8"}
```

### Start consumer application:

```bash
mvn exec:java -Dexec.mainClass=org.eclipse.hono.vertx.example.HonoExampleApplication
```