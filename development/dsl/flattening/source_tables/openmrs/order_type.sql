CREATE TABLE `order_type` (
  `order_type_id` int,
  `name` VARCHAR,
  `description` VARCHAR,
  `creator` int,
  `date_created` TIMESTAMP,
  `retired` BOOLEAN,
  `retired_by` int,
  `date_retired` TIMESTAMP,
  `retire_reason` VARCHAR,
  `uuid` VARCHAR,
  `java_class_name` VARCHAR,
  `parent` int,
  `changed_by` int,
  `date_changed` TIMESTAMP,
  PRIMARY KEY (`order_type_id`) NOT ENFORCED
)