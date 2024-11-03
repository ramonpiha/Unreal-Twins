import csv
import os
import time
import random
import subprocess

# Setting environment variables (assuming these are already set in your environment)
env_vars = {
    "MY_TENANT": "70d85efc-a332-4cf2-8dba-c5b25e815a57",
    "REGISTRY_IP": "127.0.0.1",
    "HTTP_ADAPTER_IP": "127.0.0.1",
    "MQTT_ADAPTER_IP": "127.0.0.1",
    "KAFKA_IP": "127.0.0.1",
    "TRUSTSTORE_PATH": "truststore.pem",
    "APP_OPTIONS": 'app --host 127.0.0.1 --port 9094 -u hono -p hono-secret --ca-file truststore.pem --disable-hostname-verification',
    "CURL_OPTIONS": '--insecure',
    "MOSQUITTO_OPTIONS": '--cafile truststore.pem --insecure',
    "MY_DEVICE": '56629fcc-4d05-4d64-9d76-4e4728258550',
    "MY_PWD": "this-is-my-password",
    "TEMPERATURE_SENSOR1": "5167c837-c70b-4a1e-9e88-1d64446b9a01",
    "BRIGHTNESS_SENSOR1": "033b5620-60a9-49f9-8419-1e79c7c19b97",
    "BRIGHTNESS_SENSOR2": "5cdb8243-b0d3-480e-ac4c-729a3329b2fa",
    "BRIGHTNESS_SENSOR3": "43a50b16-34ca-42f0-998d-835efb291567",
    "BRIGHTNESS_SENSOR4": "94594bd0-4789-4258-981b-c5f8a517c47f",
    "BRIGHTNESS_SENSOR5": "514f5fac-1de6-4b9e-ae0d-069ce0658654",
    "BRIGHTNESS_SENSOR6": "5341e96b-f6ef-470d-8c96-4e3ec2c98f5d",
    "BUTTON1": "d564aab8-09d2-4885-95cd-24ff936d635c"
}

# Assigning environment variables
for key, value in env_vars.items():
    os.environ[key] = value

# Sensor types
temperature_sensors = ["TEMPERATURE_SENSOR1"]
brightness_sensors = ["BRIGHTNESS_SENSOR1", "BRIGHTNESS_SENSOR2", "BRIGHTNESS_SENSOR3", "BRIGHTNESS_SENSOR4", "BRIGHTNESS_SENSOR5", "BRIGHTNESS_SENSOR6"]
button_sensors = ["BUTTON1"]

def generate_sensor_data(sensor_type):
    if sensor_type == "temperature":
        return random.randint(-20, 50)
    elif sensor_type == "brightness":
        return random.randint(0, 100)
    elif sensor_type == "on":
        return random.choice([True, False])

def make_curl_call(sensor_id, sensor_type, value):
    tenant = os.getenv("MY_TENANT")
    pwd = os.getenv("MY_PWD")
    http_adapter_ip = os.getenv("HTTP_ADAPTER_IP")
    curl_options = os.getenv("CURL_OPTIONS")
    
    if sensor_type == "temperature":
        data = {"temperature": value}
    elif sensor_type == "brightness":
        data = {"brightness": value}
    elif sensor_type == "on":
        data = {"on": value}

    curl_command = [
        "curl", "-i", "-u", f"{sensor_id}@{tenant}:{pwd}",
        curl_options, "-H", "Content-Type: application/json",
        "--data-binary", str(data), f"https://{http_adapter_ip}:8443/telemetry"
    ]
    start_time = time.time()
    result = subprocess.run(curl_command, capture_output=True, text=True)
    end_time = time.time()
    time_taken = int((end_time - start_time) * 1000)

    with open('curl_times.csv', mode='a', newline='') as file:
        writer = csv.writer(file)
        writer.writerow([time_taken, sensor_id, value])

    print(result.stdout)

while True:
    for sensor in temperature_sensors:
        sensor_id = os.getenv(sensor)
        value = generate_sensor_data("temperature")
        make_curl_call(sensor_id, "temperature", value)

    for sensor in brightness_sensors:
        sensor_id = os.getenv(sensor)
        value = generate_sensor_data("brightness")
        make_curl_call(sensor_id, "brightness", value)

    for sensor in button_sensors:
        sensor_id = os.getenv(sensor)
        value = generate_sensor_data("on")
        make_curl_call(sensor_id, "on", value)

    time.sleep(5)
