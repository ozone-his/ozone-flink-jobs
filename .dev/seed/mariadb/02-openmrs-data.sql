-- A small, hand-written OpenMRS sample so the pipelines have something to flatten on a fresh
-- start. Enough rows to exercise the joins the flattening queries perform (a patient needs a
-- person, a name, an identifier; an encounter needs a visit and a location), not a realistic
-- clinical dataset.
INSERT INTO location (location_id, name, description, city_village, country, retired, uuid, date_created, creator)
VALUES (1, 'Outpatient Clinic', 'Seed location', 'Nairobi', 'KE', 0, 'loc-0001-seed', NOW(), 1),
       (2, 'Inpatient Ward',    'Seed location', 'Mombasa', 'KE', 0, 'loc-0002-seed', NOW(), 1);

INSERT INTO person (person_id, gender, birthdate, birthdate_estimated, dead, deathdate_estimated, voided, creator, date_created, uuid)
VALUES (11, 'M', '1990-04-12', 0, 0, 0, 0, 1, NOW(), 'per-0011-seed'),
       (12, 'F', '1985-11-03', 0, 0, 0, 0, 1, NOW(), 'per-0012-seed');

INSERT INTO person_name (person_name_id, person_id, given_name, middle_name, family_name, preferred, voided, creator, date_created, uuid)
VALUES (21, 11, 'John',  'K', 'Mwangi',  1, 0, 1, NOW(), 'pnm-0021-seed'),
       (22, 12, 'Grace', 'A', 'Otieno',  1, 0, 1, NOW(), 'pnm-0022-seed');

INSERT INTO patient (patient_id, creator, date_created, voided, allergy_status)
VALUES (11, 1, NOW(), 0, 'Unknown'),
       (12, 1, NOW(), 0, 'Unknown');

INSERT INTO patient_identifier_type (patient_identifier_type_id, name, description, retired, uuid, date_created, creator)
VALUES (1, 'OpenMRS ID', 'Seed identifier type', 0, 'pit-0001-seed', NOW(), 1);

INSERT INTO patient_identifier (patient_identifier_id, patient_id, identifier, identifier_type, preferred, location_id, voided, creator, date_created, uuid)
VALUES (31, 11, 'MRN-0011', 1, 1, 1, 0, 1, NOW(), 'pid-0031-seed'),
       (32, 12, 'MRN-0012', 1, 1, 1, 0, 1, NOW(), 'pid-0032-seed');
