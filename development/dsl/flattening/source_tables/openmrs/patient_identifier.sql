CREATE TABLE `patient_identifier` (
`patient_identifier_id` int,
`patient_id` int,
`identifier` VARCHAR,
`identifier_type` int,
`preferred` BOOLEAN,
`location_id` int,
`creator` int,
`date_created` TIMESTAMP,
`voided` BOOLEAN,
`voided_by` int ,
`date_voided` TIMESTAMP ,
`void_reason` VARCHAR ,
`uuid` char,
`date_changed` TIMESTAMP,
`changed_by` int,
PRIMARY KEY (`patient_identifier_id`) NOT ENFORCED
)