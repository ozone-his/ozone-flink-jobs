-- Minimal Odoo source-DB schema for integration tests.
-- Mirrors the columns read by the Flink flattening jobs (dsl/flattening/tables/odoo)
-- and the columns present in the test-data INSERT scripts.

CREATE TABLE IF NOT EXISTS res_partner (
    id              INTEGER PRIMARY KEY,
    ref             VARCHAR,
    name            VARCHAR,
    email           VARCHAR,
    phone           VARCHAR
);

CREATE TABLE IF NOT EXISTS product_template (
    id                  INTEGER PRIMARY KEY,
    name                VARCHAR,
    type                INTEGER,
    categ_id            INTEGER,
    uom_id              INTEGER,
    uom_po_id           INTEGER,
    tracking            VARCHAR,
    purchase_line_warn  VARCHAR,
    sale_line_warn      VARCHAR
);

CREATE TABLE IF NOT EXISTS product_product (
    id              INTEGER PRIMARY KEY,
    default_code    VARCHAR,
    name            VARCHAR,
    active          BOOLEAN,
    product_tmpl_id INTEGER
);

CREATE TABLE IF NOT EXISTS ir_model_data (
    id              INTEGER PRIMARY KEY,
    module          VARCHAR,
    name            VARCHAR,
    model           VARCHAR,
    res_id          INTEGER,
    complete_name   VARCHAR
);

CREATE TABLE IF NOT EXISTS sale_order (
    id                  INTEGER PRIMARY KEY,
    name                VARCHAR,
    date_order          TIMESTAMP,
    partner_id          INTEGER,
    partner_invoice_id  INTEGER,
    partner_shipping_id INTEGER,
    pricelist_id        INTEGER,
    company_id          INTEGER,
    picking_policy      VARCHAR,
    warehouse_id        INTEGER,
    amount_total        NUMERIC(10, 2),
    invoice_status      VARCHAR,
    note                VARCHAR,
    state               VARCHAR
);

CREATE TABLE IF NOT EXISTS sale_order_line (
    id              INTEGER PRIMARY KEY,
    order_id        INTEGER,
    name            VARCHAR,
    sequence        INTEGER,
    product_id      INTEGER,
    product_uom     INTEGER,
    product_uom_qty NUMERIC,
    price_unit      NUMERIC,
    price_subtotal  NUMERIC,
    price_total     NUMERIC,
    customer_lead   NUMERIC,
    create_date     TIMESTAMP,
    write_date      TIMESTAMP
);

