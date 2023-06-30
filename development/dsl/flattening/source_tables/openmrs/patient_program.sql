CREATE TABLE `patient_program` (
  `patient_program_id` int,
  `patient_id` int,
  `program_id` int,
  `date_enrolled` TIMESTAMP,
  `date_completed` TIMESTAMP,
  `location_id` int,
  `outcome_concept_id` int,
  `creator` int,
  `date_created` TIMESTAMP,
  `changed_by` int,
  `date_changed` TIMESTAMP,
  `voided` BOOLEAN,
  `voided_by` int,
  `date_voided` TIMESTAMP,
  `void_reason` VARCHAR,
  `uuid` VARCHAR,
  PRIMARY KEY (`patient_program_id`) NOT ENFORCED
)