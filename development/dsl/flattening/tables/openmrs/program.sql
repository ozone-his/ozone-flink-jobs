CREATE TABLE `program` (
  `program_id` int,
  `concept_id` int,
  `outcomes_concept_id` int,
  `creator` int,
  `date_created` TIMESTAMP,
  `changed_by` int,
  `date_changed` TIMESTAMP,
  `retired` BOOLEAN,
  `name` VARCHAR,
  `description` VARCHAR,
  `uuid` VARCHAR,
  PRIMARY KEY (`program_id`) NOT ENFORCED
)