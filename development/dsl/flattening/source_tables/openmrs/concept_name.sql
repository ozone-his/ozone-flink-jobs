CREATE TABLE `concept_name` (
  `concept_name_id` int,
  `concept_id` int,
  `name` VARCHAR,
  `locale` VARCHAR,
  `locale_preferred` BOOLEAN,
  `creator` int,
  `date_created` TIMESTAMP,
  `concept_name_type` VARCHAR,
  `voided` BOOLEAN,
  `voided_by` int,
  `date_voided` TIMESTAMP,
  `void_reason` VARCHAR,
  `uuid` char,
  `date_changed` TIMESTAMP,
  `changed_by` int,
  PRIMARY KEY (`concept_name_id`) NOT ENFORCED
)