package com.tfg.mentoring.model.auxiliar;

public class EnvioPeticion {
	
	private String mentor;
	private String motivo;
	
	
	
	public EnvioPeticion(String mentor, String motivo) {
		super();
		this.mentor = mentor;
		this.motivo = motivo;
	}
	
	
	public EnvioPeticion() {
		super();
	}

	public String getMentor() {
		return mentor;
	}
	public void setMentor(String mentor) {
		this.mentor = mentor;
	}
	public String getMotivo() {
		return motivo;
	}
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}


	@Override
	public String toString() {
		return "EnvioPeticion [mentor=" + mentor + ", motivo=" + motivo + "]";
	}
	
	
	
	
}
