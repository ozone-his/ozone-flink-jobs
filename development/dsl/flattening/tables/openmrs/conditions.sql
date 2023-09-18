CREATE TABLE `conditions` (
  `condition_id` int,
  `additional_detail` VARCHAR,
  `previous_version` int,
  `condition_coded` int,
  `condition_non_coded` VARCHAR,
  `condition_coded_name` int,
  `clinical_status` VARCHAR,
  `verification_status` VARCHAR,
  `onset_date` TIMESTAMP,
  `date_created` TIMESTAMP,
  `voided` BOOLEAN,
  `date_voided` TIMESTAMP,
  `void_reason` VARCHAR,
  `uuid` VARCHAR,
  `creator` int,
  `voided_by` int,
  `changed_by` int,
  `patient_id` int,
  `end_date` TIMESTAMP,
  `date_changed` TIMESTAMP,
  `encounter_id` int,
  PRIMARY KEY (`condition_id`) NOT ENFORCED
)