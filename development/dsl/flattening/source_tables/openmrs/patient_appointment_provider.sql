CREATE TABLE `patient_appointment_provider` (
  `patient_appointment_provider_id` int,
  `patient_appointment_id` int,
  `provider_id` int,
  `response` VARCHAR,
  `comments` VARCHAR,
  `date_created` TIMESTAMP,
  `creator` int,
  `date_changed` TIMESTAMP,
  `changed_by` int,
  `voided` BOOLEAN,
  `voided_by` int,
  `date_voided` TIMESTAMP,
  `void_reason` VARCHAR,
  `uuid` VARCHAR,
  PRIMARY KEY (`patient_appointment_provider_id`) NOT ENFORCED
)