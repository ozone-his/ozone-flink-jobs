CREATE TABLE `encounter_type` (
  `encounter_type_id` int,
  `name` VARCHAR,
  `description` VARCHAR,
  `creator` int,
  `date_created` TIMESTAMP,
  `retired` BOOLEAN,
  `retired_by` int,
  `date_retired` TIMESTAMP,
  `retire_reason` VARCHAR,
  `uuid` VARCHAR,
  `edit_privilege` VARCHAR,
  `view_privilege` VARCHAR,
  `changed_by` int,
  `date_changed` TIMESTAMP,
  PRIMARY KEY (`encounter_type_id`) NOT ENFORCED
  )
