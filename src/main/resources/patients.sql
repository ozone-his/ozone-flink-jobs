SELECT
       patient.patient_id AS id,
       name.given_name AS given_name,
       name.middle_name AS middle_name,
       name.family_name AS family_name,
       GROUP_CONCAT(CONCAT_WS(":", identifier_type.name, identifier.identifier) SEPARATOR ", ") as identifiers,
       person.gender AS gender,
       person.birthdate AS birthdate,
       person.birthdate_estimated AS birthdate_estimated,
       address.city_village AS address_city,
       address.county_district AS address_county_district,
       address.state_province AS address_state_province,
       address.country AS address_country,
       address.address1 AS address_1,
       address.address2 AS address_2,
       address.address3 AS address_3,
       address.address4 AS address_4,
       address.address5 AS address_5,
       address.address6 AS address_6,
       address.address7 AS address_7,
       address.address8 AS address_8,
       address.address9 AS address_9,
       address.address10 AS address_10,
       address.address11 AS address_11,
       address.address12 AS address_12,
       address.address13 AS address_13,
       address.address14 AS address_14,
       address.address15 AS address_15,
       address.latitude AS address_latitude,
       address.longitude AS address_longitude,
       person.dead AS dead,
       person.death_date AS death_date,
       person.cause_of_death AS cause_of_death,
       person.creator AS creator,
       person.date_created AS date_created,
       person.voided AS person_voided,
       person.void_reason AS person_void_reason
FROM
       patient
       LEFT JOIN person person ON patient.patient_id = person.person_id
       LEFT JOIN person_name name ON person.person_id = name.person_id
       LEFT JOIN person_address address ON person.person_id = address.person_id
       LEFT JOIN patient_identifier identifier ON patient.patient_id = identifier.patient_id
       LEFT JOIN patient_identifier_type identifier_type ON identifier.identifier_type = identifier_type.patient_identifier_type_id
GROUP BY patient.patient_id