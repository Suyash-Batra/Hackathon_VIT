package com.wildguard.wildlife.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "protected_areas")
public class ProtectedArea {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_code", nullable = false, unique = true, length = 50)
    private String areaCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String geometry;

	public ProtectedArea() {
		super();
	}

	public ProtectedArea(Long id, String areaCode, String name, String description, String geometry) {
		super();
		this.id = id;
		this.areaCode = areaCode;
		this.name = name;
		this.description = description;
		this.geometry = geometry;
	}

	@Override
	public String toString() {
		return "ProtectedArea [id=" + id + ", areaCode=" + areaCode + ", name=" + name + ", description=" + description
				+ ", geometry=" + geometry + "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGeometry() {
		return geometry;
	}

	public void setGeometry(String geometry) {
		this.geometry = geometry;
	}
}
