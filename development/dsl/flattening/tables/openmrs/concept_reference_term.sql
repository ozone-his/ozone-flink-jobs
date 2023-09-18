CREATE TABLE `concept_reference_term` (
  `concept_reference_term_id` int,
  `concept_source_id` int,
  `name` VARCHAR,
  `code` VARCHAR,
  `version` VARCHAR,
  `description` VARCHAR,
  `creator` int,
  `date_created` TIMESTAMP,
  `date_changed` TIMESTAMP,
  `changed_by` int,
  `retired` BOOLEAN,
  `retired_by` int,
  `date_retired` TIMESTAMP,
  `retire_reason` VARCHAR,
  `uuid` char,
  PRIMARY KEY (`concept_reference_term_id`) NOT ENFORCED
)