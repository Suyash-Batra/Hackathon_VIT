package com.wildguard.wildlife.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "species")
public class Species {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "common_name", nullable = false, length = 150)
    private String commonName;

    @Column(name = "scientific_name", nullable = false, length = 150)
    private String scientificName;

    @Column(name = "conservation_flag")
    private Boolean conservationFlag;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Species() {
        super();
    }

    public Species(Long id, String commonName, String scientificName, Boolean conservationFlag, String notes) {
        super();
        this.id = id;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.conservationFlag = conservationFlag;
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Species [id=" + id + ", commonName=" + commonName + ", scientificName=" + scientificName
                + ", conservationFlag=" + conservationFlag + ", notes=" + notes + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public Boolean getConservationFlag() {
        return conservationFlag;
    }

    public void setConservationFlag(Boolean conservationFlag) {
        this.conservationFlag = conservationFlag;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}