package com.tfg.mentoring.model.auxiliar;

public class MensajeError {

	private String titulo;
	private String mensaje;

	public MensajeError(String titulo, String mensaje) {
		super();
		this.titulo = titulo;
		this.mensaje = mensaje;
	}

	public MensajeError() {
		super();
	}
	
	

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	@Override
	public String toString() {
		return "MensajeError [titulo=" + titulo + ", mensaje=" + mensaje + "]";
	}

	
	
	
}
