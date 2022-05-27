package com.tfg.mentoring.exceptions;

public class ExcepcionFichero extends Exception{

	private static final long serialVersionUID = 1L;
	
	private String titulo;

	public ExcepcionFichero(String titulo, String msg) {
		super(msg);
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	
	
}
