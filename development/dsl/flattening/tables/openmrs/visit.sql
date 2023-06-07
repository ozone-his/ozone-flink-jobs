CREATE TABLE `visit` (
  `visit_id` int,
  `patient_id` int,
  `visit_type_id` int,
  `date_started` TIMESTAMP,
  `date_stopped` TIMESTAMP,
  `indication_concept_id` int,
  `location_id` int,
  `creator` int,
  `date_created` TIMESTAMP,
  `changed_by` int,
  `date_changed` TIMESTAMP,
  `voided` BOOLEAN,
  `voided_by` int,
  `date_voided` TIMESTAMP,
  `void_reason` VARCHAR,
  `uuid` VARCHAR,
  PRIMARY KEY (`visit_id`) NOT ENFORCED
)