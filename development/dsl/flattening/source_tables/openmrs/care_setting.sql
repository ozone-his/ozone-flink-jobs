CREATE TABLE `care_setting` (
  `care_setting_id` int,
  `name` VARCHAR,
  `description` VARCHAR,
  `care_setting_type` VARCHAR,
  `creator` int,
  `date_created` TIMESTAMP,
  `retired` BOOLEAN,
  `retired_by` int,
  `date_retired` TIMESTAMP,
  `retire_reason` VARCHAR,
  `changed_by` int,
  `date_changed` TIMESTAMP,
  `uuid` VARCHAR,
  PRIMARY KEY (`care_setting_id`) NOT ENFORCED
)