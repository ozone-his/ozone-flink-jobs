CREATE TABLE `orders` (
    `order_id` BIGINT,
    `patient_id` BIGINT,
    `order_type_id` BIGINT,
    `order_type_name` VARCHAR,
    `order_type_uuid` VARCHAR,
    `order_type_java_class_name` VARCHAR,
    `concept_id` BIGINT,
    `orderer` BIGINT,
    `encounter_id` BIGINT,
    `encounter_datetime` TIMESTAMP,
    `encounter_type_name` VARCHAR,
    `encounter_type_uuid` VARCHAR,
    `care_setting` BIGINT,
    `care_setting_name` VARCHAR,
    `care_setting_type` VARCHAR,
    `care_setting_uuid` VARCHAR,
    `instructions` VARCHAR,
    `date_activated` TIMESTAMP,
    `auto_expire_date` TIMESTAMP,
    `date_stopped` TIMESTAMP,
    `order_reason` BIGINT,
    `order_reason_non_coded` VARCHAR,
    `date_created` TIMESTAMP,
    `creator` BIGINT,
    `voided_by` BIGINT,
    `date_voided` TIMESTAMP,
    `void_reason` VARCHAR,
    `accession_number` VARCHAR,
    `uuid` VARCHAR,
    `order_number` VARCHAR,
    `previous_order_id` BIGINT,
    `order_action` VARCHAR,
    `comment_to_fulfiller` VARCHAR,
    `scheduled_date` TIMESTAMP,
    `order_group_id` BIGINT,
    `sort_weight` DOUBLE,
    `encounter_voided` BOOLEAN,
    `voided` BOOLEAN,
    `order_type_retired` BOOLEAN,
    `encounter_type_retired` BOOLEAN,
    `care_setting_retired` BOOLEAN
)