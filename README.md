# Ozone Analytics ETL Pipelines

## Overview

A suite of ETL jobs crafted using Apache Flink, which are specifically designed for processing and flattening data originating from all Ozone HIS components.

[Apache Flink](https://ci.apache.org/projects/flink/flink-docs-master/) is a powerful framework that supports both batch and real-time data processing. These jobs are pivotal in converting raw OpenMRS data into a format that's more conducive for reporting and analytics.

## Features

This repository is equipped to handle various data operations, including:

- **Batch and Streaming**: Accommodates both batch processing and real-time streaming workflows.
  
- **Data Flattening**: Transforms complex data structures into a more analytics-friendly tabular format, targeting multiple OpenMRS entities such as:
  - Patients
  - Observations
  - Visits
  - Concepts
  - Encounters
  - Orders
  - Conditions
  - Diagnoses
  - Appointments
  - Patient programs

## Technologies

We utilize a robust stack of technologies:

- [Apache Flink](https://ci.apache.org/projects/flink/flink-docs-master/) for orchestrating the ETL jobs.
- [Kafka Connect](https://docs.confluent.io/platform/current/connect/index.html) for Change Data Capture (CDC).
- [Apache Kafka](https://kafka.apache.org/) for managing data streams.

## Development

### Domain-Specific Languages (DSLs)

Our ETL jobs rely on two main types of DSLs:

- **Flattening DSLs**: Used to simplify data from OpenMRS, which is intricately linked to the liquibase migration scripts for generating the destination tables. Access the scripts [here](https://github.com/ozone-his/ozonepro-distro/analytics_config/liquibase/analytics/).

- **Parquet Export DSLs**: These enable the export of data to Parquet files, an efficient file format for large-scale data storage.

### Getting Started

#### Step 1: Start Required Services

Before running ETL jobs, ensure that an Ozone HIS instance is active. Instructions to set this up can be found [here for docker deployment](https://github.com/ozone-his/ozone-docker) or [here for a more comprehensive setup](https://github.com/ozone-his/ozonepro-docker).

Next, you need to have the migration and table creation scripts ready. You can find them in the `analytics_config` directory of the main project repository [here](https://github.com/ozone-his/ozonepro-distro).

Set the environment variables to point to the required scripts and locations:

```bash
export ANALYTICS_SOURCE_TABLES_PATH=~/path/to/flattening/tables/;
export ANALYTICS_QUERIES_PATH=~/path/to/flattening/queries/;
export ANALYTICS_DESTINATION_TABLES_MIGRATIONS_PATH=~/path/to/liquibase/migrations/;
export EXPORT_DESTINATION_TABLES_PATH=~/path/to/export/tables/;
export EXPORT_SOURCE_QUERIES_PATH=~/path/to/export/queries;
```

Navigate to the `development` directory and load the environment variables:

```bash
cd development
source ./set_env_vars.sh
```

Use `docker-compose` to start the necessary services:

```bash
docker-compose up -d
cd ..
```

*Note*: For non-Mac or Windows users (e.g., Linux), replace `gateway.docker.internal` with the actual Docker host IP (often `172.17.0.1`).

#### Step 2: Compile

Compile the project using Maven:

```bash
mvn clean install compile
```

#### Step 3: Run Jobs

Each job requires a configuration file, located by default in `development/data/config.yaml`. An example of this file is provided within the development data directory.

- **To run the Streaming job:**

  ```bash
  export necessary environment variables specific to your streaming job
  mvn compile exec:java -Dexec.mainClass="com.ozonehis.data.pipelines.streaming.StreamingETLJob" -Dexec.classpathScope="compile"
  ```

- **To run the Batch job:**

  ```bash
  export necessary environment variables specific to your batch job
  mvn compile exec:java -Dexec.mainClass="com.ozonehis.data.pipelines.batch.BatchETLJob" -Dexec.classpathScope="compile"
  ```

- **To run the Parquet Export job:**

  First, ensure the output directory exists:

  ```bash
  mkdir -p development/data/parquet/
  ```

  Then, export the required environment variables and execute the job:

  ```bash
  export necessary environment variables specific to your parquet export job
  mvn compile exec:java -Dexec.mainClass="com.ozonehis.data.pipelines.export.BatchParquetExport" -Dexec.classpathScope="compile"
  ```

## Important Considerations

- **Streaming from Postgres**: If you're streaming data from PostgreSQL, consult the Apache Flink documentation on [Debezium PostgreSQL Connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/formats/debezium/#consuming-data-produced-by-debezium-postgres-connector) to understand the nuances of this process.
