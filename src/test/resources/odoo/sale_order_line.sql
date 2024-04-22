INSERT INTO res_partner (id, name)
VALUES (1, 'John Doe'),
       (2, 'Dealer');

INSERT INTO res_currency (id, name, symbol)
VALUES (1, 'UG Shilling', 'UGX');

INSERT INTO product_pricelist (id, name, currency_id,discount_policy)
VALUES (1, 'Test Price list', 1, 'Test discount policy');

INSERT INTO res_company (id, name, partner_id, currency_id, fiscalyear_last_day, fiscalyear_last_month, account_opening_date, manufacturing_lead, po_lead, security_lead)
VALUES (1, 'Test Company', 2, 1, 31, 12, '2024-01-01', 0, 0, 0);

INSERT INTO stock_location (id, name, usage)
VALUES (1, 'Test Stock Location', 'test');

INSERT INTO stock_warehouse (id, name, company_id, view_location_id, lot_stock_id, code, reception_steps, delivery_steps, manufacture_steps)
VALUES (1, 'Test warehouse', 1, 1, 1, 'test', 'test', 'test', 'test');

INSERT INTO uom_category (id, name)
VALUES (1, 'Test uom category');

INSERT INTO uom_uom (id, name, category_id, factor, rounding, uom_type)
VALUES (1, 'Tab', 1, 0, 0, 'test');

INSERT INTO product_category (id, name)
VALUES (1, 'Test product category');

INSERT INTO product_template (id, name, type, categ_id, uom_id, uom_po_id, tracking, purchase_line_warn, sale_line_warn)
VALUES (1, 'Test Template', 1, 1, 1, 1, 'test', 'test', 'test');

INSERT INTO product_product (id, default_code, product_tmpl_id)
VALUES (1, 'AMP', 1),
       (2, 'ASP', 1);

INSERT INTO sale_order (id, name, date_order, partner_id, partner_invoice_id, partner_shipping_id, pricelist_id, company_id, picking_policy, warehouse_id)
VALUES (1, 'Initial Visit', '2024-04-22 07:15:00.000000', 1, 2, 2, 1, 1, 'Test picking policy', 1);

INSERT INTO sale_order_line (id, order_id, name, price_unit, product_uom_qty, product_id, customer_lead)
VALUES (101, 1, 'Ampicillin', 1, 1, 1, 1),
       (102, 1, 'Aspirin', 2, 2, 2, 2);
