
# Ozone ETL pipelines

## Flink

  

This repository contains ETL [Flink](hhttps://ci.apache.org/projects/flink/flink-docs-master/) [jobs](https://ci.apache.org/projects/flink/flink-docs-master/docs/internals/job_scheduling/#:~:text=A%20Flink%20job%20is%20first,it%20cancels%20all%20running%20tasks) for flattening [Ozone HIS](https://github.com/ozone-his) data.

## Features

  

- Provides both [batch]() and [streaming]() modes

- Currently flattens OpenMRS to output reporting friendly tables for:

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



## Tech

- [Flink](hhttps://ci.apache.org/projects/flink/flink-docs-master/) - For ETL
- [Kafka connect](https://docs.confluent.io/platform/current/connect/index.html) - For CDC
- [Kafka](https://kafka.apache.org/) - For streaming data

### Development

#### DSL

The project contains reference DSLs for defining the ETL jobs. The DSLs are located in the `development/dsl` directory.
- [Flattening DSL](development/dsl/flattening/README.md) - For flattening data from OpenMRS
- [Parquet Export DSL](development/dsl/parquet/README.md) - For exporting data to parquet files


#### Step1:  startup backing services

```cd development```
```docker-compose up -d```
```cd ../```

#### Step2: Configure ENV vars
#### Step3: Compile
```mvn clean install compile```

#### Step3:
##### Run Streaming job
```
export ANALYTICS_DB_USER=analytics;\
export ANALYTICS_DB_PASSWORD=password;\
export ANALYTICS_DB_HOST=localhost;\
export ANALYTICS_DB_PORT=5432;\
export ANALYTICS_DB_NAME=analytics;\
export ANALYTICS_SOURCE_TABLES_PATH=$(pwd)/development/dsl/flattening/tables/;\
export ANALYTICS_QUERIES_PATH=$(pwd)/development/dsl/flattening/queries/;\
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
```

```mvn compile exec:java -Dexec.mainClass="com.ozonehis.data.pipelines.streaming.StreamingETLJob" -Dexec.classpathScope="compile"```

##### Run Batch job
```
export ANALYTICS_DB_USER=analytics;\
export ANALYTICS_DB_PASSWORD=password;\
export ANALYTICS_DB_HOST=localhost;\
export ANALYTICS_DB_PORT=5432;\
export ANALYTICS_DB_NAME=analytics;\
export ANALYTICS_SOURCE_TABLES_PATH=$(pwd)/development/dsl/flattening/tables/;\
export ANALYTICS_QUERIES_PATH=$(pwd)/development/dsl/flattening/queries/;\
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
```
```mvn compile exec:java -Dexec.mainClass="com.ozonehis.data.pipelines.batch.BatchETLJob" -Dexec.classpathScope="compile"```

##### Run Parquet Export job
```mkdir -p development/data/parquet/```
```
export ANALYTICS_DB_USER=analytics;\
export ANALYTICS_DB_PASSWORD=password;\
export ANALYTICS_DB_HOST=localhost;\
export ANALYTICS_DB_PORT=5432;\
export ANALYTICS_DB_NAME=analytics;\
export EXPORT_DESTINATION_TABLES_PATH=$(pwd)/development/dsl/parquet/tables/;\
export EXPORT_SOURCE_QUERIES_PATH=$(pwd)/development/dsl/parquet/queries;\
export EXPORT_OUTPUT_PATH=$(pwd)/development/data/parquet/;\
export EXPORT_OUTPUT_TAG=h1;
```
```mvn compile exec:java -Dexec.mainClass="com.ozonehis.data.pipelines.export.BatchParquetExport" -Dexec.classpathScope="compile"```


## Gotchas
When streaming data from Postgres See
[consuming-data-produced-by-debezium-postgres-connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/formats/debezium/#consuming-data-produced-by-debezium-postgres-connector)