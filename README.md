# Ozone Analytics ETL Pipelines

## Overview

This repository contains the ETL pipelines that are used to transform data from all Ozone components into a format that is easy to query and analyze. The pipelines are written in [Apache Flink](https://ci.apache.org/projects/flink/flink-docs-master/), a powerful framework that supports both batch and real-time data processing.

## Features

The project provides the following features:

- Support for [**Batch Analytics**](https://nightlies.apache.org/flink/flink-docs-master/docs/ops/batch/batch_shuffle/) and [**Streaming Analytics**](https://nightlies.apache.org/flink/flink-docs-master/docs/dev/table/concepts/overview/) ETL

- Flattening of data from Ozone HIS Components into a format that is easy to query and analyze.:
  The data that is flattened depends on project needs. For example, our Reference Distro provides flattening queries that produce the following tables:

  - patients

  - observations

  - visits

  - concepts

  - encounters

  - orders

  - conditions

  - diagnoses

  - appointments

  - patient programs

## Technologies

We utilize the following technologies to power our ETL pipelines:
- [Apache Flink](hhttps://ci.apache.org/projects/flink/flink-docs-master/) - For orchestrating the ETL jobs.
- [Kafka Connect](https://docs.confluent.io/platform/current/connect/index.html) - for Change Data Capture (CDC).
- [Apache Kafka](https://kafka.apache.org/) - For managing data streams.

### Development

#### Domain Specific Languages (DSLs)

. The project generates Flink jobs based on SQL DSLs. The DSLs are categorized into two:
- [Flattening DSLs](https://github.com/ozone-his/ozonepro-distro/analytics_config/dsl/flattening/README.md) - For flattening data from OpenMRS. Note that these are related to the Liquibase migration scripts that are used to create destination tables found [here](https://github.com/ozone-his/ozonepro-distro/analytics_config/liquibase/analytics/).
- [Parquet Export DSLs](https://github.com/ozone-his/ozonepro-distro/analytics_config/dsl/export/README.md) - For exporting data to parquet files

#### Step1:  Start Required Services

The project assumes you already have an Ozone HIS instance running. If not please follow the instructions [here](https://github.com/ozone-his/ozone-docker) or [here](https://github.com/ozone-his/ozonepro-docker) to get one up and running.

The project also assumes you have the required migration scripts and destination table creation scripts with their query scripts located somewhere you know. They can be downloaded as part of the project [here](https://github.com/ozone-his/ozonepro-distro) in the `analytics_config` directory, for example, the following `env` variable would be exported as below;

```bash
export ANALYTICS_SOURCE_TABLES_PATH=~/ozonepro-distro/analytics_config/dsl/flattening/tables/;
export ANALYTICS_QUERIES_PATH=~/ozonepro-distro/analytics_config/dsl/flattening/queries/;
export ANALYTICS_DESTINATION_TABLES_MIGRATIONS_PATH=~/ozonepro-distro/analytics_config/liquibase/analytics/;
export EXPORT_DESTINATION_TABLES_PATH=~/ozonepro-distro/analytics_config/dsl/export/tables/;
export EXPORT_SOURCE_QUERIES_PATH=~/ozonepro-distro/analytics_config/dsl/export/queries;
```

```cd development```

##### Export environment variables

```bash
export ANALYTICS_DESTINATION_TABLES_MIGRATIONS_PATH= path_to_folder_containing_liquibase_destination_tables_migrations;\
```

```bash
export ANALYTICS_DB_HOST=gateway.docker.internal; \
export ANALYTICS_DB_PORT=5432; \
export CONNECT_MYSQL_HOSTNAME=gateway.docker.internal; \
export CONNECT_MYSQL_PORT=3306; \
export CONNECT_MYSQL_USER=root; \
export CONNECT_MYSQL_PASSWORD=3cY8Kve4lGey; \
export CONNECT_ODOO_DB_HOSTNAME=gateway.docker.internal; \
export CONNECT_ODOO_DB_PORT=5432; \
export CONNECT_ODOO_DB_NAME=odoo; \
export CONNECT_ODOO_DB_USER=postgres; \
export CONNECT_ODOO_DB_PASSWORD=password
```

```docker-compose up -d```
```cd ../```
***Note***: The `gateway.docker.internal` is a special DNS name that resolves to the host machine from within containers. It is only available for Mac and Windows. For Linux, use the docker host IP by default ```172.17.0.1```

#### Step 2: Compile

```mvn clean install compile```

#### Step 3:

***Note***: The `ANALYTICS_CONFIG_FILE_PATH` env var provides the location of the configuration file required by all jobs. An example file is provided at `development/data/config.yaml`

##### Running in Streaming mode

```bash
export ANALYTICS_SOURCE_TABLES_PATH=path_to_folder_containing_source_tables_to_query_from;\
export ANALYTICS_QUERIES_PATH=path_to_folder_containing_sql_flattening_queries;\
```

```bash
export ANALYTICS_DB_USER=analytics;\
export ANALYTICS_DB_PASSWORD=password;\
export ANALYTICS_DB_HOST=localhost;\
export ANALYTICS_DB_PORT=5432;\
export ANALYTICS_DB_NAME=analytics;\
export OPENMRS_DB_NAME=openmrs;\
export OPENMRS_DB_USER=root;\
export OPENMRS_DB_PASSWORD=3cY8Kve4lGey;\
export OPENMRS_DB_HOST=localhost;\
export OPENMRS_DB_PORT=3306;\
export ODOO_DB_NAME=odoo;\
export ODOO_DB_USER=postgres;\
export ODOO_DB_PASSWORD=password;\
export ODOO_DB_HOST=localhost;\
export ODOO_DB_PORT=5432;\
export ZOOKEEPER_URL=localhost:2181;\
export ANALYTICS_CONFIG_FILE_PATH=$(pwd)/development/data/config.yaml;\
export ANALYTICS_KAFKA_URL=localhost:29092;\
export FLINK_REST_PORT=8082;
```

```mvn compile exec:java -Dexec.mainClass="com.ozonehis.data.pipelines.streaming.StreamingETLJob" -Dexec.classpathScope="compile"```

##### Runin Batch mode

```bash
export ANALYTICS_DB_USER=analytics;\
export ANALYTICS_DB_PASSWORD=password;\
export ANALYTICS_DB_HOST=localhost;\
export ANALYTICS_DB_PORT=5432;\
export ANALYTICS_DB_NAME=analytics;\
export OPENMRS_DB_NAME=openmrs;\
export OPENMRS_DB_USER=root;\
export OPENMRS_DB_PASSWORD=3cY8Kve4lGey;\
export OPENMRS_DB_HOST=localhost;\
export OPENMRS_DB_PORT=3306;\
export ODOO_DB_NAME=odoo;\
export ODOO_DB_USER=postgres;\
export ODOO_DB_PASSWORD=password;\
export ODOO_DB_HOST=localhost;\
export ODOO_DB_PORT=5432;
export ANALYTICS_CONFIG_FILE_PATH=$(pwd)/development/data/config.yaml;\
export FLINK_REST_PORT=8082;
```

```mvn compile exec:java -Dexec.mainClass="com.ozonehis.data.pipelines.batch.BatchETLJob" -Dexec.classpathScope="compile"```

##### Run Export job

```mkdir -p development/data/parquet/```

```bash
export EXPORT_DESTINATION_TABLES_PATH=path_to_folder_containing_parquet_destination_tables_to_query_to;
export EXPORT_SOURCE_QUERIES_PATH=path_to_folder_containing_sql_parquet_queries;
```

```bash
export ANALYTICS_DB_USER=analytics;\
export ANALYTICS_DB_PASSWORD=password;\
export ANALYTICS_DB_HOST=localhost;\
export ANALYTICS_DB_PORT=5432;\
export ANALYTICS_DB_NAME=analytics;\
export EXPORT_OUTPUT_PATH=$(pwd)/development/data/parquet/;\
export EXPORT_OUTPUT_TAG=h1;
export ANALYTICS_CONFIG_FILE_PATH=$(pwd)/development/data/config.yaml;\
export FLINK_REST_PORT=8082;
```

```mvn compile exec:java -Dexec.mainClass="com.ozonehis.data.pipelines.export.BatchExport" -Dexec.classpathScope="compile"```

## Gotchas

When streaming data from PostgreSQL See
[consuming-data-produced-by-debezium-postgres-connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/formats/debezium/#consuming-data-produced-by-debezium-postgres-connector)

## Code formatting

The project includes spotless-maven-plugin to enforce code formatting. To format the code, run the following command:

```bash
mvn clean install -Pspotless
```

