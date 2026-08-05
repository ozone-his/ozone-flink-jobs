-- These are the openmrs source tables the flattening pipelines read, mirroring the Flink table
-- definitions column for column. Only the columns the pipelines actually use are present, so this
-- is not a full openmrs schema. Hand-maintained: update alongside ANALYTICS_SOURCE_TABLES_PATH, or
-- a job will fail at submission on a missing column.
-- No foreign keys: Debezium does not need them, and they would impose an insert
-- order on the sample data for no benefit.

CREATE TABLE IF NOT EXISTS `appointment_service` (
  `appointment_service_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `start_time` DATETIME,
  `end_time` DATETIME,
  `location_id` INT,
  `speciality_id` INT,
  `max_appointments_limit` INT,
  `duration_mins` INT,
  `color` VARCHAR(255),
  `date_created` DATETIME,
  `creator` INT,
  `date_changed` DATETIME,
  `changed_by` INT,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  `initial_appointment_status` VARCHAR(255),
  PRIMARY KEY (`appointment_service_id`)
);

CREATE TABLE IF NOT EXISTS `appointment_service_type` (
  `appointment_service_type_id` INT,
  `appointment_service_id` INT,
  `name` VARCHAR(255),
  `duration_mins` INT,
  `date_created` DATETIME,
  `creator` INT,
  `date_changed` DATETIME,
  `changed_by` INT,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  PRIMARY KEY (`appointment_service_type_id`)
);

CREATE TABLE IF NOT EXISTS `care_setting` (
  `care_setting_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `care_setting_type` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `changed_by` INT,
  `date_changed` DATETIME,
  `uuid` VARCHAR(255),
  PRIMARY KEY (`care_setting_id`)
);

CREATE TABLE IF NOT EXISTS `concept` (
  `concept_id` INT,
  `retired` TINYINT(1),
  `short_name` VARCHAR(255),
  `description` VARCHAR(255),
  `form_text` VARCHAR(255),
  `datatype_id` INT,
  `class_id` INT,
  `is_set` TINYINT(1),
  `creator` INT,
  `date_created` DATETIME,
  `version` VARCHAR(255),
  `changed_by` INT,
  `date_changed` DATETIME,
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  PRIMARY KEY (`concept_id`)
);

CREATE TABLE IF NOT EXISTS `concept_answer` (
  `concept_answer_id` INT,
  `concept_id` INT,
  `answer_concept` INT,
  `answer_drug` INT,
  `creator` INT,
  `date_created` DATETIME,
  `sort_weight` DOUBLE,
  `uuid` VARCHAR(255),
  PRIMARY KEY (`concept_answer_id`)
);

CREATE TABLE IF NOT EXISTS `concept_name` (
  `concept_name_id` INT,
  `concept_id` INT,
  `name` VARCHAR(255),
  `locale` VARCHAR(255),
  `locale_preferred` TINYINT(1),
  `creator` INT,
  `date_created` DATETIME,
  `concept_name_type` VARCHAR(255),
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` CHAR(38),
  `date_changed` DATETIME,
  `changed_by` INT,
  PRIMARY KEY (`concept_name_id`)
);

CREATE TABLE IF NOT EXISTS `concept_reference_map` (
  `concept_map_id` INT,
  `concept_reference_term_id` INT,
  `concept_map_type_id` INT,
  `creator` INT,
  `date_created` DATETIME,
  `concept_id` INT,
  `changed_by` INT,
  `date_changed` DATETIME,
  `uuid` CHAR(38),
  PRIMARY KEY (`concept_map_id`)
);

CREATE TABLE IF NOT EXISTS `concept_reference_source` (
  `concept_source_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `hl7_code` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` CHAR(38),
  `unique_id` VARCHAR(255),
  `date_changed` DATETIME,
  `changed_by` INT,
  PRIMARY KEY (`concept_source_id`)
);

CREATE TABLE IF NOT EXISTS `concept_reference_term` (
  `concept_reference_term_id` INT,
  `concept_source_id` INT,
  `name` VARCHAR(255),
  `code` VARCHAR(255),
  `version` VARCHAR(255),
  `description` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `date_changed` DATETIME,
  `changed_by` INT,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` CHAR(38),
  PRIMARY KEY (`concept_reference_term_id`)
);

CREATE TABLE IF NOT EXISTS `concept_set` (
  `concept_set_id` INT,
  `concept_id` INT,
  `concept_set` INT,
  `sort_weight` DOUBLE,
  `creator` INT,
  `date_created` DATETIME,
  `uuid` VARCHAR(255),
  PRIMARY KEY (`concept_set_id`)
);

CREATE TABLE IF NOT EXISTS `conditions` (
  `condition_id` INT,
  `additional_detail` VARCHAR(255),
  `previous_version` INT,
  `condition_coded` INT,
  `condition_non_coded` VARCHAR(255),
  `condition_coded_name` INT,
  `clinical_status` VARCHAR(255),
  `verification_status` VARCHAR(255),
  `onset_date` DATETIME,
  `date_created` DATETIME,
  `voided` TINYINT(1),
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  `creator` INT,
  `voided_by` INT,
  `changed_by` INT,
  `patient_id` INT,
  `end_date` DATETIME,
  `date_changed` DATETIME,
  `encounter_id` INT,
  PRIMARY KEY (`condition_id`)
);

CREATE TABLE IF NOT EXISTS `drug` (
  `drug_id` INT,
  `concept_id` INT,
  `name` VARCHAR(255),
  `combination` TINYINT(1),
  `dosage_form` INT,
  `maximum_daily_dose` DOUBLE,
  `minimum_daily_dose` DOUBLE,
  `route` INT,
  `creator` INT,
  `date_created` DATETIME,
  `retired` TINYINT(1),
  `changed_by` INT,
  `date_changed` DATETIME,
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  `strength` VARCHAR(255),
  `dose_limit_units` INT,
  PRIMARY KEY (`drug_id`)
);

CREATE TABLE IF NOT EXISTS `drug_order` (
  `order_id` INT,
  `drug_inventory_id` INT,
  `dose` DOUBLE,
  `as_needed` TINYINT(1),
  `dosing_type` VARCHAR(255),
  `quantity` DOUBLE,
  `as_needed_condition` VARCHAR(255),
  `num_refills` INT,
  `dosing_instructions` VARCHAR(255),
  `duration` INT,
  `duration_units` INT,
  `quantity_units` INT,
  `route` INT,
  `dose_units` INT,
  `frequency` INT,
  `brand_name` VARCHAR(255),
  `dispense_as_written` TINYINT(1),
  `drug_non_coded` VARCHAR(255),
  PRIMARY KEY (`order_id`)
);

CREATE TABLE IF NOT EXISTS `encounter` (
  `encounter_id` INT,
  `encounter_type` INT,
  `patient_id` INT,
  `location_id` INT,
  `form_id` INT,
  `encounter_datetime` DATETIME,
  `creator` INT,
  `date_created` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `changed_by` INT,
  `date_changed` DATETIME,
  `visit_id` INT,
  `uuid` VARCHAR(255),
  PRIMARY KEY (`encounter_id`)
);

CREATE TABLE IF NOT EXISTS `encounter_diagnosis` (
  `diagnosis_id` INT,
  `diagnosis_coded` INT,
  `diagnosis_non_coded` VARCHAR(255),
  `diagnosis_coded_name` INT,
  `encounter_id` INT,
  `patient_id` INT,
  `condition_id` INT,
  `certainty` VARCHAR(255),
  `uuid` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  PRIMARY KEY (`diagnosis_id`)
);

CREATE TABLE IF NOT EXISTS `encounter_type` (
  `encounter_type_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  `edit_privilege` VARCHAR(255),
  `view_privilege` VARCHAR(255),
  `changed_by` INT,
  `date_changed` DATETIME,
  PRIMARY KEY (`encounter_type_id`)
);

CREATE TABLE IF NOT EXISTS `form` (
  `form_id` INT,
  `name` VARCHAR(255),
  `version` VARCHAR(255),
  `build` INT,
  `published` TINYINT(1),
  `xslt` VARCHAR(255),
  `template` VARCHAR(255),
  `description` VARCHAR(255),
  `encounter_type` INT,
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retired_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  PRIMARY KEY (`form_id`)
);

CREATE TABLE IF NOT EXISTS `location` (
  `location_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `address1` VARCHAR(255),
  `address2` VARCHAR(255),
  `city_village` VARCHAR(255),
  `state_province` VARCHAR(255),
  `postal_code` VARCHAR(255),
  `country` VARCHAR(255),
  `latitude` VARCHAR(255),
  `longitude` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `county_district` VARCHAR(255),
  `address3` VARCHAR(255),
  `address4` VARCHAR(255),
  `address5` VARCHAR(255),
  `address6` VARCHAR(255),
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `parent_location` INT,
  `uuid` CHAR(38),
  `changed_by` INT,
  `date_changed` DATETIME,
  `address7` VARCHAR(255),
  `address8` VARCHAR(255),
  `address9` VARCHAR(255),
  `address10` VARCHAR(255),
  `address11` VARCHAR(255),
  `address12` VARCHAR(255),
  `address13` VARCHAR(255),
  `address14` VARCHAR(255),
  `address15` VARCHAR(255),
  PRIMARY KEY (`location_id`)
);

CREATE TABLE IF NOT EXISTS `location_tag` (
  `location_tag_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `retired` TINYINT,
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  `changed_by` INT,
  `date_changed` DATETIME,
  PRIMARY KEY (`location_tag_id`)
);

CREATE TABLE IF NOT EXISTS `location_tag_map` (
  `location_id` INT,
  `location_tag_id` INT,
  PRIMARY KEY (`location_id`, `location_tag_id`)
);

CREATE TABLE IF NOT EXISTS `obs` (
  `obs_id` INT,
  `person_id` INT,
  `concept_id` INT,
  `encounter_id` INT,
  `order_id` INT,
  `obs_datetime` DATETIME,
  `location_id` INT,
  `obs_group_id` INT,
  `accession_number` VARCHAR(255),
  `value_group_id` INT,
  `value_coded` INT,
  `value_coded_name_id` INT,
  `value_drug` INT,
  `value_datetime` DATETIME,
  `value_numeric` DOUBLE,
  `value_modifier` VARCHAR(255),
  `value_text` VARCHAR(255),
  `value_complex` VARCHAR(255),
  `comments` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  `previous_version` INT,
  `form_namespace_and_path` VARCHAR(255),
  `status` VARCHAR(255),
  `interpretation` VARCHAR(255),
  PRIMARY KEY (`obs_id`)
);

CREATE TABLE IF NOT EXISTS `order_frequency` (
  `order_frequency_id` INT,
  `concept_id` INT,
  `frequency_per_day` DOUBLE,
  `creator` INT,
  `date_created` DATETIME,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `changed_by` INT,
  `date_changed` DATETIME,
  `uuid` VARCHAR(255),
  PRIMARY KEY (`order_frequency_id`)
);

CREATE TABLE IF NOT EXISTS `order_type` (
  `order_type_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  `java_class_name` VARCHAR(255),
  `parent` INT,
  `changed_by` INT,
  `date_changed` DATETIME,
  PRIMARY KEY (`order_type_id`)
);

CREATE TABLE IF NOT EXISTS `orders` (
  `order_id` INT,
  `order_type_id` INT,
  `concept_id` INT,
  `orderer` INT,
  `encounter_id` INT,
  `instructions` VARCHAR(255),
  `date_activated` DATETIME,
  `auto_expire_date` DATETIME,
  `date_stopped` DATETIME,
  `order_reason` INT,
  `order_reason_non_coded` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `patient_id` INT,
  `accession_number` VARCHAR(255),
  `uuid` CHAR(38),
  `urgency` VARCHAR(255),
  `order_number` VARCHAR(255),
  `previous_order_id` INT,
  `order_action` VARCHAR(255),
  `comment_to_fulfiller` VARCHAR(255),
  `care_setting` INT,
  `scheduled_date` DATETIME,
  `order_group_id` INT,
  `sort_weight` DOUBLE,
  `fulfiller_comment` VARCHAR(255),
  `fulfiller_status` VARCHAR(255),
  `form_namespace_and_path` VARCHAR(255),
  PRIMARY KEY (`order_id`)
);

CREATE TABLE IF NOT EXISTS `patient` (
  `patient_id` INT,
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `allergy_status` VARCHAR(255),
  PRIMARY KEY (`patient_id`)
);

CREATE TABLE IF NOT EXISTS `patient_appointment` (
  `patient_appointment_id` INT,
  `provider_id` INT,
  `appointment_number` VARCHAR(255),
  `patient_id` INT,
  `start_date_time` DATETIME,
  `end_date_time` DATETIME,
  `appointment_service_id` INT,
  `appointment_service_type_id` INT,
  `status` VARCHAR(255),
  `location_id` INT,
  `appointment_kind` VARCHAR(255),
  `comments` VARCHAR(255),
  `uuid` VARCHAR(255),
  `date_created` DATETIME,
  `creator` INT,
  `date_changed` DATETIME,
  `changed_by` INT,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `related_appointment_id` INT,
  `tele_health_video_link` VARCHAR(255),
  PRIMARY KEY (`patient_appointment_id`)
);

CREATE TABLE IF NOT EXISTS `patient_appointment_provider` (
  `patient_appointment_provider_id` INT,
  `patient_appointment_id` INT,
  `provider_id` INT,
  `response` VARCHAR(255),
  `comments` VARCHAR(255),
  `date_created` DATETIME,
  `creator` INT,
  `date_changed` DATETIME,
  `changed_by` INT,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  PRIMARY KEY (`patient_appointment_provider_id`)
);

CREATE TABLE IF NOT EXISTS `patient_identifier` (
  `patient_identifier_id` INT,
  `patient_id` INT,
  `identifier` VARCHAR(255),
  `identifier_type` INT,
  `preferred` TINYINT(1),
  `location_id` INT,
  `creator` INT,
  `date_created` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` CHAR(38),
  `date_changed` DATETIME,
  `changed_by` INT,
  PRIMARY KEY (`patient_identifier_id`)
);

CREATE TABLE IF NOT EXISTS `patient_identifier_type` (
  `patient_identifier_type_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `format` VARCHAR(255),
  `check_digit` TINYINT(1),
  `creator` INT,
  `date_created` DATETIME,
  `required` TINYINT(1),
  `format_description` VARCHAR(255),
  `validator` VARCHAR(255),
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` CHAR(38),
  `location_behavior` VARCHAR(255),
  `uniqueness_behavior` VARCHAR(255),
  `date_changed` DATETIME,
  `changed_by` INT,
  PRIMARY KEY (`patient_identifier_type_id`)
);

CREATE TABLE IF NOT EXISTS `patient_program` (
  `patient_program_id` INT,
  `patient_id` INT,
  `program_id` INT,
  `date_enrolled` DATETIME,
  `date_completed` DATETIME,
  `location_id` INT,
  `outcome_concept_id` INT,
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  PRIMARY KEY (`patient_program_id`)
);

CREATE TABLE IF NOT EXISTS `person` (
  `person_id` INT,
  `gender` VARCHAR(255),
  `birthdate` DATE,
  `birthdate_estimated` TINYINT(1),
  `dead` TINYINT(1),
  `death_date` DATETIME,
  `cause_of_death` INT,
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` CHAR(38),
  `deathdate_estimated` TINYINT(1),
  `birthtime` TIME,
  `cause_of_death_non_coded` VARCHAR(255),
  PRIMARY KEY (`person_id`)
);

CREATE TABLE IF NOT EXISTS `person_address` (
  `person_address_id` INT,
  `person_id` INT,
  `preferred` TINYINT(1),
  `address1` VARCHAR(255),
  `address2` VARCHAR(255),
  `city_village` VARCHAR(255),
  `state_province` VARCHAR(255),
  `postal_code` VARCHAR(255),
  `country` VARCHAR(255),
  `latitude` VARCHAR(255),
  `longitude` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `county_district` VARCHAR(255),
  `address3` VARCHAR(255),
  `address6` VARCHAR(255),
  `address5` VARCHAR(255),
  `address4` VARCHAR(255),
  `uuid` CHAR(38),
  `date_changed` DATETIME,
  `changed_by` INT,
  `start_date` DATETIME,
  `end_date` DATETIME,
  `address7` VARCHAR(255),
  `address8` VARCHAR(255),
  `address9` VARCHAR(255),
  `address10` VARCHAR(255),
  `address11` VARCHAR(255),
  `address12` VARCHAR(255),
  `address13` VARCHAR(255),
  `address14` VARCHAR(255),
  `address15` VARCHAR(255),
  PRIMARY KEY (`person_address_id`)
);

CREATE TABLE IF NOT EXISTS `person_attribute` (
  `person_attribute_id` INT,
  `person_id` INT,
  `value` VARCHAR(255),
  `person_attribute_type_id` INT,
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  PRIMARY KEY (`person_attribute_id`)
);

CREATE TABLE IF NOT EXISTS `person_attribute_type` (
  `person_attribute_type_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `format` VARCHAR(255),
  `foreign_key` INT,
  `searchable` TINYINT(1),
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `edit_privilege` VARCHAR(255),
  `sort_weight` DOUBLE,
  `uuid` VARCHAR(255),
  PRIMARY KEY (`person_attribute_type_id`)
);

CREATE TABLE IF NOT EXISTS `person_name` (
  `person_name_id` INT,
  `preferred` TINYINT(1),
  `person_id` INT,
  `prefix` VARCHAR(255),
  `given_name` VARCHAR(255),
  `middle_name` VARCHAR(255),
  `family_name_prefix` VARCHAR(255),
  `family_name` VARCHAR(255),
  `family_name2` VARCHAR(255),
  `family_name_suffix` VARCHAR(255),
  `degree` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `changed_by` INT,
  `date_changed` DATETIME,
  `uuid` CHAR(38),
  PRIMARY KEY (`person_name_id`)
);

CREATE TABLE IF NOT EXISTS `program` (
  `program_id` INT,
  `concept_id` INT,
  `outcomes_concept_id` INT,
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `retired` TINYINT(1),
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `uuid` VARCHAR(255),
  PRIMARY KEY (`program_id`)
);

CREATE TABLE IF NOT EXISTS `visit` (
  `visit_id` INT,
  `patient_id` INT,
  `visit_type_id` INT,
  `date_started` DATETIME,
  `date_stopped` DATETIME,
  `indication_concept_id` INT,
  `location_id` INT,
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  PRIMARY KEY (`visit_id`)
);

CREATE TABLE IF NOT EXISTS `visit_attribute` (
  `visit_attribute_id` INT,
  `visit_id` INT,
  `attribute_type_id` INT,
  `value_reference` VARCHAR(255),
  `uuid` CHAR(38),
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `voided` TINYINT(1),
  `voided_by` INT,
  `date_voided` DATETIME,
  `void_reason` VARCHAR(255),
  PRIMARY KEY (`visit_attribute_id`)
);

CREATE TABLE IF NOT EXISTS `visit_attribute_type` (
  `visit_attribute_type_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `datatype` VARCHAR(255),
  `datatype_config` VARCHAR(255),
  `preferred_handler` VARCHAR(255),
  `handler_config` VARCHAR(255),
  `min_occurs` INT,
  `max_occurs` INT,
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` CHAR(38),
  PRIMARY KEY (`visit_attribute_type_id`)
);

CREATE TABLE IF NOT EXISTS `visit_type` (
  `visit_type_id` INT,
  `name` VARCHAR(255),
  `description` VARCHAR(255),
  `creator` INT,
  `date_created` DATETIME,
  `changed_by` INT,
  `date_changed` DATETIME,
  `retired` TINYINT(1),
  `retired_by` INT,
  `date_retired` DATETIME,
  `retire_reason` VARCHAR(255),
  `uuid` VARCHAR(255),
  PRIMARY KEY (`visit_type_id`)
);
