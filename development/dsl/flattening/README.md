This directory contains the DSL for flattening data from OpenMRS and Odoo the queries are shared  by the batch and streaming jobs. Currently the project supports read from OpenMRS and Odoo. The folder is divided into:
- `tables` - Contains the table definitions for the source tables in OpenMRS and Odoo. (Direct 1-1 mapping to the database tables)
- `queries` - Contains the queries for flattening the data.

***Note***: We don't have destination(SINK) tables for  the data is writted directly the analytics database via [JDBC Catalog](https://ci.apache.org/projects/flink/flink-docs-stable/dev/table/connectors/jdbc.html#jdbc-catalog).