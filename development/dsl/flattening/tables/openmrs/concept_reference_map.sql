CREATE TABLE `concept_reference_map` (
  `concept_map_id` int,
  `concept_reference_term_id` int,
  `concept_map_type_id` int,
  `creator` int,
  `date_created` TIMESTAMP,
  `concept_id` int,
  `changed_by` int,
  `date_changed` TIMESTAMP ,
  `uuid` char,
  PRIMARY KEY (`concept_map_id`) NOT ENFORCED
)