SELECT
    patient_program.patient_program_id AS patient_program_id,
    patient_program.patient_id AS patient_id,
    patient_program.program_id AS program_id,
    patient_program.date_enrolled AS date_enrolled,
    patient_program.date_completed AS date_completed,
    patient_program.location_id AS location_id,
    patient_program.outcome_concept_id AS outcome_concept_id,
    patient_program.creator AS creator,
    patient_program.date_created AS date_created,
    patient_program.changed_by AS changed_by,
    patient_program.date_changed AS date_changed,
    patient_program.voided AS voided,
    patient_program.voided_by AS voided_by,
    patient_program.date_voided AS date_voided,
    patient_program.void_reason AS void_reason,
    patient_program.uuid as uuid,
    program.retired AS program_retired,
    program.name AS program_name,
    program.description AS program_description,
    program.uuid AS program_uuid,
    program.concept_id AS program_concept_id,
    concept_concept_name.name AS concept_name,
    concept_concept_name.uuid AS concept_uuid,
    program.outcomes_concept_id AS program_outcomes_concept_id,
    outcomes_concept.name AS outcomes_concept_name,
    outcomes_concept.uuid AS outcomes_concept_uuid
FROM
    patient_program
    LEFT JOIN program program ON patient_program.program_id = program.program_id
    LEFT JOIN concept_name outcomes_concept ON program.outcomes_concept_id = outcomes_concept.concept_id
    AND program.outcomes_concept_id IS NOT NULL
    AND outcomes_concept.locale_preferred = true
    AND outcomes_concept.locale = 'en'
    LEFT JOIN concept_name concept_concept_name ON program.concept_id = concept_concept_name.concept_id
    AND concept_concept_name.locale_preferred = true
    AND concept_concept_name.locale = 'en'
    