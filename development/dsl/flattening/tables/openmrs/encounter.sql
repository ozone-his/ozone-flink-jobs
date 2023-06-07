CREATE TABLE `encounter` (
  `encounter_id` int,
  `encounter_type` int,
  `patient_id` int,
  `location_id` int,
  `form_id` int,
  `encounter_datetime` TIMESTAMP,
  `creator` int,
  `date_created` TIMESTAMP,
  `voided` BOOLEAN,
  `voided_by` int,
  `date_voided` TIMESTAMP,
  `void_reason` VARCHAR,
  `changed_by` int,
  `date_changed` TIMESTAMP,
  `visit_id` int,
  `uuid` VARCHAR,
  PRIMARY KEY (`encounter_id`) NOT ENFORCED
)