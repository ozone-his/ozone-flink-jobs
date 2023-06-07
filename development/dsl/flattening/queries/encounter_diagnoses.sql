select
    diagnosis_id,
    diagnosis_coded,
    diagnosis_non_coded,
    diagnosis_coded_name,
    encounter_id,
    patient_id,
    certainty,
    uuid,
    creator,
    date_created,
    voided,
    voided_by,
    date_voided,
    void_reason
from
    encounter_diagnosis
    