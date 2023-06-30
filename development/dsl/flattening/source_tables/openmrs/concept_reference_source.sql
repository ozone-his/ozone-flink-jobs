CREATE TABLE `concept_reference_source` (
  `concept_source_id` int,
  `name` VARCHAR,
  `description` VARCHAR,
  `hl7_code` VARCHAR,
  `creator` int,
  `date_created` TIMESTAMP,
  `retired` BOOLEAN,
  `retired_by` int,
  `date_retired` TIMESTAMP,
  `retire_reason` VARCHAR,
  `uuid` char,
  `unique_id` VARCHAR,
  `date_changed` TIMESTAMP,
  `changed_by` int,
  PRIMARY KEY (`concept_source_id`) NOT ENFORCED
)