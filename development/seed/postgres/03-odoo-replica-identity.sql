-- Debezium needs the pre-image of an updated row to emit a complete change event. Under the
-- default replica identity PostgreSQL logs only the primary key, so Flink's debezium-json format
-- would never see an UPDATE_BEFORE and could not retract the superseded row.
--
-- Hand-written and deliberately separate from 02-odoo-schema.sql, which is generated and would
-- overwrite anything added to it.
ALTER TABLE ir_model_data REPLICA IDENTITY FULL;
ALTER TABLE product_product REPLICA IDENTITY FULL;
ALTER TABLE product_template REPLICA IDENTITY FULL;
ALTER TABLE res_partner REPLICA IDENTITY FULL;
ALTER TABLE sale_order REPLICA IDENTITY FULL;
ALTER TABLE sale_order_line REPLICA IDENTITY FULL;
