package com.tfg.mentoring.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="areas")
public class AreaConocimiento {
	@Id
	@Column(name = "area", nullable=false)
	private String area;

	public AreaConocimiento(String area) {
		super();
		this.area = area;
	}

	public AreaConocimiento() {
		super();
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	@Override
	public String toString() {
		return "AreaConocimiento [area=" + area + "]";
	}
	
	

	
	
	
}
