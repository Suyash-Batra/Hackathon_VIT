package com.wildguard.wildlife.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "zones")
public class Zone {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_code", nullable = false, unique = true, length = 50)
    private String zoneCode;

    @Column(name = "area_id", nullable = false)
    private Long areaId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String coordinates;

    @Column(columnDefinition = "TEXT")
    private String description;

	public Zone() {
		super();
	}

	public Zone(Long id, String zoneCode, Long areaId, String name, String coordinates, String description) {
		super();
		this.id = id;
		this.zoneCode = zoneCode;
		this.areaId = areaId;
		this.name = name;
		this.coordinates = coordinates;
		this.description = description;
	}

	@Override
	public String toString() {
		return "Zone [id=" + id + ", zoneCode=" + zoneCode + ", areaId=" + areaId + ", name=" + name + ", coordinates="
				+ coordinates + ", description=" + description + "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getZoneCode() {
		return zoneCode;
	}

	public void setZoneCode(String zoneCode) {
		this.zoneCode = zoneCode;
	}

	public Long getAreaId() {
		return areaId;
	}

	public void setAreaId(Long areaId) {
		this.areaId = areaId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(String coordinates) {
		this.coordinates = coordinates;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
