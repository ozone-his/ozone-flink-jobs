package com.ozonehis.data.pipelines;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Patient {

    @JsonProperty("patient_id")
    private int patientId;

    private String gender;

    private String birthdate;

    @JsonProperty("birthdate_estimated")
    private boolean birthdateEstimated;

    private boolean dead;

    @JsonProperty("death_date")
    private String deathDate;

    @JsonProperty("patient_uuid")
    private String patientUuid;

    /**
     * Gets the patientId
     *
     * @return the patientId
     */
    public int getPatientId() {
        return patientId;
    }

    /**
     * Sets the patientId
     *
     * @param patientId the patientId to set
     */
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    /**
     * Gets the gender
     *
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the gender
     *
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Gets the birthdate
     *
     * @return the birthdate
     */
    public String getBirthdate() {
        return birthdate;
    }

    /**
     * Sets the birthdate
     *
     * @param birthdate the birthdate to set
     */
    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    /**
     * Gets the birthdateEstimated
     *
     * @return the birthdateEstimated
     */
    public boolean isBirthdateEstimated() {
        return birthdateEstimated;
    }

    /**
     * Sets the birthdateEstimated
     *
     * @param birthdateEstimated the birthdateEstimated to set
     */
    public void setBirthdateEstimated(boolean birthdateEstimated) {
        this.birthdateEstimated = birthdateEstimated;
    }

    /**
     * Gets the dead
     *
     * @return the dead
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Sets the dead
     *
     * @param dead the dead to set
     */
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    /**
     * Gets the deathDate
     *
     * @return the deathDate
     */
    public String getDeathDate() {
        return deathDate;
    }

    /**
     * Sets the deathDate
     *
     * @param deathDate the deathDate to set
     */
    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }

    /**
     * Gets the patientUuid
     *
     * @return the patientUuid
     */
    public String getPatientUuid() {
        return patientUuid;
    }

    /**
     * Sets the patientUuid
     *
     * @param patientUuid the patientUuid to set
     */
    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }
}
