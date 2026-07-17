-- Creates the analytics sink database alongside the odoo source database.
--
-- A single PostgreSQL instance hosts both, mirroring the deployed stack where ANALYTICS_DB_HOST
-- and CONNECT_ODOO_DB_HOSTNAME point at the same host.
--
-- The analytics tables themselves are NOT created here: they are owned by the Liquibase migration
-- in ozone-analytics-queries, applied by the `analytics-migration` service.
CREATE USER analytics WITH PASSWORD 'password';
CREATE DATABASE analytics OWNER analytics;
