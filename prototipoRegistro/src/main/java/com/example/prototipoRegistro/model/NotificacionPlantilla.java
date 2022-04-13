package com.example.prototipoRegistro.model;

public class NotificacionPlantilla {

	private long usuario;
	private String descripcion;
	
	
	
	public NotificacionPlantilla() {
		super();
	}

	public NotificacionPlantilla(long usuario, String descripcion) {
		super();
		this.usuario = usuario;
		this.descripcion = descripcion;
	}
	
	public long getUsuario() {
		return usuario;
	}
	public void setUsuario(long usuario) {
		this.usuario = usuario;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Usuario: "+this.usuario+", Descripcion: "+this.descripcion;
	}
	
	
	
	
}
