select
    encounter.voided AS encounter_voided,
    location.name AS location_name,
    encounter.encounter_datetime AS encounter_datetime,
    encounter_type.name AS encounter_type_name,
    visit_type.name AS visit_type_name,
    visit.date_started AS visit_date_started,
    visit.date_stopped AS visit_date_stopped,
    form.name AS form_name,
    form.uuid AS form_uuid,
    form.version AS form_version,
    encounter.uuid AS encounter_uuid,
    encounter_type.uuid AS encounter_type_uuid,
    visit.uuid AS visit_uuid,
    person.uuid AS patient_uuid,
    location.uuid AS location_uuid,
    creator.uuid AS creator_uuid
from
    encounter
    LEFT JOIN encounter_type encounter_type ON encounter.encounter_type = encounter_type.encounter_type_id
    LEFT JOIN location location ON encounter.location_id = location.location_id
    LEFT JOIN form form ON encounter.form_id = form.form_id
    LEFT JOIN visit visit ON encounter.visit_id = visit.visit_id
    LEFT JOIN person person ON encounter.patient_id = person.person_id
    LEFT JOIN visit_type visit_type ON visit.visit_type_id = visit_type.visit_type_id
    LEFT JOIN person creator ON encounter.creator = creator.person_id
