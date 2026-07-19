-- A small, hand-written Odoo sample: one customer, one product, one order with a line, plus the
-- ir_model_data row the sale_order_lines flattening query joins to resolve a product's external
-- identifier.
--
-- The columns here must match 02-odoo-schema.sql, which is generated from the Flink table
-- definitions and so carries ONLY the columns the pipelines read. It is not a full Odoo schema, so
-- an INSERT lifted from a real Odoo database will not apply.
INSERT INTO res_partner (id, ref, name, email, phone)
VALUES (1, 'MRN-0011', 'John K Mwangi', 'john@example.test', '+254700000001');

INSERT INTO product_template (id, name)
VALUES (1, 'Consultation');

INSERT INTO product_product (id, default_code, name, active, product_tmpl_id)
VALUES (1, 'CONSULT-01', 'Consultation', true, 1);

-- The sale_order_lines query joins this on (model = 'product.product' AND res_id = product.id) to
-- resolve the product's external identifier.
INSERT INTO ir_model_data (id, module, name, model, res_id, complete_name)
VALUES (1, 'ozone', 'consultation_service', 'product.product', 1, 'ozone.consultation_service');

INSERT INTO sale_order (id, partner_id, amount_total, invoice_status, name, note, state)
VALUES (1, 1, 500.00, 'to invoice', 'SO0001', 'Seed order', 'sale');

INSERT INTO sale_order_line (id, order_id, sequence, product_id, product_uom_qty, price_unit, price_subtotal, price_total, create_date, write_date)
VALUES (1, 1, 10, 1, 1, 500.00, 500.00, 500.00, NOW(), NOW());
