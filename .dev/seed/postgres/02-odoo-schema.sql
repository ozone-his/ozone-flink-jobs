-- These are the odoo source tables the flattening pipelines read, mirroring the Flink table
-- definitions column for column. Only the columns the pipelines actually use are present, so this
-- is not a full odoo schema. Hand-maintained: update alongside ANALYTICS_SOURCE_TABLES_PATH, or a
-- job will fail at submission on a missing column.
-- No foreign keys: Debezium does not need them, and they would impose an insert
-- order on the sample data for no benefit.

CREATE TABLE IF NOT EXISTS "ir_model_data" (
  "id" INTEGER,
  "module" VARCHAR,
  "name" VARCHAR,
  "model" VARCHAR,
  "res_id" INTEGER,
  "complete_name" VARCHAR,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "product_product" (
  "id" INTEGER,
  "default_code" VARCHAR,
  "name" VARCHAR,
  "active" BOOLEAN,
  "product_tmpl_id" INTEGER,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "product_template" (
  "id" INTEGER,
  "name" VARCHAR,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "res_partner" (
  "id" INTEGER,
  "ref" VARCHAR,
  "name" VARCHAR,
  "email" VARCHAR,
  "phone" VARCHAR,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "sale_order" (
  "id" INTEGER,
  "partner_id" INTEGER,
  "amount_total" NUMERIC,
  "invoice_status" VARCHAR,
  "name" VARCHAR,
  "note" VARCHAR,
  "state" VARCHAR,
  PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "sale_order_line" (
  "id" INTEGER,
  "order_id" INTEGER,
  "sequence" INTEGER,
  "product_id" INTEGER,
  "product_uom_qty" NUMERIC,
  "price_unit" NUMERIC,
  "price_subtotal" NUMERIC,
  "price_total" NUMERIC,
  "create_date" TIMESTAMP,
  "write_date" TIMESTAMP,
  PRIMARY KEY ("id")
);
