CREATE TABLE `patient_identifier_type` (
`patient_identifier_type_id` int,
`name` VARCHAR,
`description` VARCHAR,
`format` VARCHAR,
`check_digit` BOOLEAN,
`creator` int,
`date_created` TIMESTAMP,
`required` BOOLEAN,
`format_description` VARCHAR,
`validator` VARCHAR,
`retired` BOOLEAN,
`retired_by` int,
`date_retired` TIMESTAMP,
`retire_reason` VARCHAR,
`uuid` char,
`location_behavior` VARCHAR,
`uniqueness_behavior` VARCHAR,
`date_changed` TIMESTAMP,
`changed_by` int,
PRIMARY KEY (`patient_identifier_type_id`) NOT ENFORCED
)