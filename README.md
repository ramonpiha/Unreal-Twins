# Unreal Twins

This repository contains the necessary components to run a digital twin simulation, including APIs, a digital representation in Unreal Engine, IoT platform integration, and data ingestion tools.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Running the APIs and MongoDB](#running-the-apis-and-mongodb)
3. [Setting Up Unreal Engine](#setting-up-unreal-engine)
4. [IoT Platform Installation](#iot-platform-installation)
5. [Data Ingestion Application](#data-ingestion-application)
6. [Simulation and Analysis](#simulation-and-analysis)

## Prerequisites

- **Docker**: Required to run the APIs and MongoDB.
- **Unreal Engine 5.3**: Needed to run the digital twin representation.
- **Maven**: Required to build and run the data ingestion application.
- **Python**: For running simulation scripts and Jupyter notebooks.
- **WSL (Windows Subsystem for Linux)**: Recommended if using Windows, especially for the IoT platform installation.

## Running the APIs and MongoDB

1. Navigate to the root folder of this repository.
2. Ensure that the `docker-compose.yml` file is present.
3. Run the following command in your terminal:

    ```bash
    docker-compose up
    ```

   This command will start three containers:
   - **Read API** (accessible at `http://localhost:8080/swagger-ui/index.html#`)
   - **Write API** (accessible at `http://localhost:8081/swagger-ui/index.html#`)
   - **MongoDB**

## Setting Up Unreal Engine

1. **Install Unreal Engine 5.3** from the [official website](https://www.unrealengine.com).
2. Install the following plugins:
   - **Datasmith**: [Install Datasmith](https://www.unrealengine.com/en-US/datasmith)
   - **Victory Plugin**: [Install Victory Plugin](https://forums.unrealengine.com/t/ramas-extra-blueprint-nodes-for-ue5-no-c-required/231476)
   - **HttpBlueprint**: Can be installed directly from Unreal Engine under `Edit -> Plugins`.

3. Ensure all the above plugins are activated.
4. To open the Unreal Twin project:
   - Navigate to the `UnrealTwins` folder.
   - Open the project in Unreal Engine.
   - In the Content Drawer, go to `All -> Content`.
   - Open the `Api` level.

5. To access the main logic:
   - Click the Blueprint symbol at the top left.
   - Select `Open Level Blueprint`.

6. **Customization**: Update the sensor IDs in the endpoint and the file concatenation logic for creating the CSV file for data analysis.

## IoT Platform Installation

1. Follow the [Getting Started Guide for Eclipse Hono](https://eclipse.dev/hono/docs/getting-started/).
2. If on Windows, it's recommended to use WSL and Minikube for the installation. If you decide to use PowerShell, [here](PowershellCommands.md) are some example commands.
3. Use the `truststore.pem` files provided in this repository for testing purposes. Ensure to secure these files and update their paths in your configuration.
4. After starting Minikube with `minikube start` and `minikube tunnel`, wait a few minutes before ingesting data.
   - Note: The first data ingestion might result in an error if not enough time has passed. Subsequent ingestions should work correctly.

## Data Ingestion Application

1. Navigate to the `hono` folder.
2. Run the following commands to install dependencies and build the project:

    ```bash
    mvn clean install package
    ```

    Perform this step for the parent project and each submodule with a `pom.xml` file.

3. **Customization**:
   - Modify `HonoExampleConstants` in `hono-client-examples/src/main/java/org/eclipse/hono/vertx/example/base` to match your IoT platform configuration.

4. To start the application, navigate to `hono-client-examples` and run:

    ```bash
    mvn exec:java -Dexec.mainClass=org.eclipse.hono.vertx.example.HonoExampleApplication
    ```

5. For message consumption, refer to the discussion in [this GitHub issue](https://github.com/eclipse-hono/hono/issues/3617) for an obsolete but informative application.

## Simulation and Analysis

1. **Simulation Scripts**:
   - `simulation.py`: Simulates data and sends it procedurally.
   - `simulation_multi_thread.py`: Simulates data using multithreading.
   - `simulation_real_data.py`: Fetches real-world data from an API and simulates ingestion with that data.

2. **Customization**: Update the environment variables at the start of each script.

3. **Statistical Analysis**:
   - Use the provided Jupyter notebook to analyze the CSV files generated during the simulation.
   - The notebook analyzes the following components:
     - Time for Unreal Engine data polling.
     - Time for updating values in the digital representation.
     - Time for simulation to execute POST API calls and save data into MongoDB.
     - Time for the IoT platform to write data into the Kafka broker.

---