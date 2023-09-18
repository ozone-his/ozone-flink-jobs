CREATE TABLE `encounter_diagnoses` (
    `diagnosis_id` BIGINT,
    `diagnosis_coded` BIGINT,
    `diagnosis_non_coded` VARCHAR,
    `diagnosis_coded_name` BIGINT,
    `encounter_id` BIGINT,
    `patient_id` BIGINT,
    `certainty` VARCHAR,
    `uuid` VARCHAR,
    `creator` BIGINT,
    `date_created` TIMESTAMP,
    `voided` BOOLEAN,
    `voided_by` BIGINT,
    `date_voided` TIMESTAMP,
    `void_reason` VARCHAR
)