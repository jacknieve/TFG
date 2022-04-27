package com.tfg.mentoring.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="puestos")
public class Puesto {
	@Id
	@Column(name = "puesto", nullable=false)
	private String puesto;

	public Puesto(String puesto) {
		super();
		this.puesto = puesto;
	}

	public Puesto() {
		super();
	}

	public String getPuesto() {
		return puesto;
	}

	public void setPuesto(String puesto) {
		this.puesto = puesto;
	}

	@Override
	public String toString() {
		return "Puesto [puesto=" + puesto + "]";
	}
	
	
}
