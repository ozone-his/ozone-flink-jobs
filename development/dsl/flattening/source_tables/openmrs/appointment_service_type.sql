CREATE TABLE `appointment_service_type` (
  `appointment_service_type_id` int,
  `appointment_service_id` int,
  `name` VARCHAR,
  `duration_mins` int,
  `date_created` TIMESTAMP,
  `creator` int,
  `date_changed` TIMESTAMP,
  `changed_by` int,
  `voided` BOOLEAN,
  `voided_by` int,
  `date_voided` TIMESTAMP,
  `void_reason` VARCHAR,
  `uuid` VARCHAR,
  PRIMARY KEY (`appointment_service_type_id`) NOT ENFORCED
)