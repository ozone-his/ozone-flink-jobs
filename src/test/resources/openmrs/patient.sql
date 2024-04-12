INSERT INTO person (person_id,gender,birthdate_estimated,deathdate_estimated,dead,voided,creator,date_created,uuid)
VALUES (11, 'M', 0, 0, 0 , 0, 1, now(), 'a13c12e5-6c4f-235f-671b-a45a22137602'),
       (12, 'F', 0, 0, 0 , 0, 1, now(), 'b13c12e5-6c4f-235f-671b-a45a22137602');

INSERT INTO patient(patient_id,creator,date_created,voided,allergy_status)
values (11, 1, '2022-05-18 00:00:00', 0, 'Unknown'),
       (12, 1, '2022-05-18 00:00:00', 0, 'Unknown');
