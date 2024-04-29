INSERT INTO product_template (id, name, type, categ_id, uom_id, uom_po_id, tracking, purchase_line_warn, sale_line_warn)
VALUES (1, 'Test Template', 1, 1, 1, 1, 'test', 'test', 'test');
INSERT INTO product_product (id, default_code, product_tmpl_id)
VALUES (1, 'AMP', 1),
       (2, 'ASP', 1);

INSERT INTO sale_order (id, name, date_order, partner_id, partner_invoice_id, partner_shipping_id, pricelist_id, company_id, picking_policy, warehouse_id)
VALUES (1, 'Initial Visit', '2024-04-22 07:15:00.000000', 1, 2, 2, 1, 1, 'Test picking policy', 1);

INSERT INTO sale_order_line (id, order_id, name, price_unit, product_uom, product_uom_qty, product_id, customer_lead)
VALUES (101, 1, 'Ampicillin', 1, 1, 1, 1, 1),
       (102, 1, 'Aspirin', 2, 1, 2, 2, 2);
