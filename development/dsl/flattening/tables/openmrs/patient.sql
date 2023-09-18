CREATE TABLE `patient` (
`patient_id` int,
`creator` int,
`date_created` TIMESTAMP,
`changed_by` int,
`date_changed` TIMESTAMP,
`voided` BOOLEAN,
`voided_by` int,
`date_voided` TIMESTAMP,
`void_reason` VARCHAR,
`allergy_status` VARCHAR,
PRIMARY KEY (`patient_id`) NOT ENFORCED
)