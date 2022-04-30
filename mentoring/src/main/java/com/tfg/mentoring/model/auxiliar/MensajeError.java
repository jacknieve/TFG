package com.tfg.mentoring.model.auxiliar;

public class MensajeError {

	private String mensaje;

	public MensajeError(String mensaje) {
		super();
		this.mensaje = mensaje;
	}

	public MensajeError() {
		super();
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	@Override
	public String toString() {
		return "MensajeError [mensaje=" + mensaje + "]";
	}
	
	
}
