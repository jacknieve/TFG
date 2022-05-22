package com.tfg.mentoring.model.auxiliar.requests;

import com.tfg.mentoring.model.auxiliar.enums.FasesMentorizacion;

public class CambioFase {

	private String correo;
	private FasesMentorizacion fase;
	
	public CambioFase(String correo, FasesMentorizacion fase) {
		super();
		this.correo = correo;
		this.fase = fase;
	}

	public CambioFase() {
		super();
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public FasesMentorizacion getFase() {
		return fase;
	}

	public void setFase(FasesMentorizacion fase) {
		this.fase = fase;
	}

	@Override
	public String toString() {
		return "CambioFase [correo=" + correo + ", fase=" + fase + "]";
	}
	
	
}
