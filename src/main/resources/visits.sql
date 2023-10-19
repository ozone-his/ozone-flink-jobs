SELECT
    visit.voided AS visit_voided,
    location.name AS location_name,
    visit.date_started AS date_started,
    visit.date_stopped AS date_stopped,
    visit_type.name AS visit_type,
    person.gender AS patient_gender,
    person.birthdate AS patient_birthdate,
    person.birthdate_estimated AS patient_birthdate_estimated,
    timestampdiff(day, person.birthdate, visit.date_started) / 365 AS patient_age_at_visit,
    person.dead AS patient_dead,
    person.death_date AS patient_death_date,
    person.cause_of_death AS patient_cause_of_death,
    visit.uuid AS visit_uuid,
    visit_type.uuid AS visit_type_uuid,
    location.uuid AS location_uuid,
    person.uuid AS patient_uuid,
    creator.uuid AS creator_uuid
FROM
    visit
    LEFT JOIN visit_type visit_type ON visit.visit_type_id = visit_type.visit_type_id
    LEFT JOIN person person ON visit.patient_id = person.person_id
    LEFT JOIN person creator ON visit.creator = creator.person_id
    LEFT JOIN location location ON visit.location_id = location.location_id