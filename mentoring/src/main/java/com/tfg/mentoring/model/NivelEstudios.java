package com.tfg.mentoring.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="nivelestudios")
public class NivelEstudios {

	@Id
	@Column(name = "nivelestudios", nullable=false)
	private String nivelestudios;

	public NivelEstudios(String nivelestudios) {
		super();
		this.nivelestudios = nivelestudios;
	}

	public NivelEstudios() {
		super();
	}

	public String getNivelestudios() {
		return nivelestudios;
	}

	public void setNivelestudios(String nivel) {
		this.nivelestudios = nivel;
	}

	@Override
	public String toString() {
		return "NivelEstudios [nivelestudios=" + nivelestudios + "]";
	}
	
	
}
