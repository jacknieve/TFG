package com.tfg.mentoring.model.auxiliar.DTO;

import com.tfg.mentoring.model.auxiliar.CuerpoMensaje;

public class SalaChatDTO implements CuerpoMensaje{

	private long id;
	private String otroUsuario;
	private String nombreOtro;
	private boolean nuevos;
	private boolean cerrada;
	//Faltaria el path de la foto de perfil
	
	
	public SalaChatDTO(long id, String otroUsuario, String nombreOtro, boolean cerrada) {
		super();
		this.id = id;
		this.otroUsuario=otroUsuario;
		this.nombreOtro=nombreOtro;
		this.nuevos = false;
		this.cerrada = cerrada;
	}

	public SalaChatDTO() {
		super();
	}

	

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOtroUsuario() {
		return otroUsuario;
	}

	public void setOtroUsuario(String otroUsuario) {
		this.otroUsuario = otroUsuario;
	}

	public String getNombreOtro() {
		return nombreOtro;
	}

	public void setNombreOtro(String nombreOtro) {
		this.nombreOtro = nombreOtro;
	}
	
	

	public boolean isNuevos() {
		return nuevos;
	}

	public void setNuevos(boolean nuevos) {
		this.nuevos = nuevos;
	}
	
	

	public boolean isCerrada() {
		return cerrada;
	}

	public void setCerrada(boolean cerrada) {
		this.cerrada = cerrada;
	}

	@Override
	public String toString() {
		return "SalaChatDTO [id=" + id + ", otroUsuario=" + otroUsuario + ", nombreOtro=" + nombreOtro + ", nuevos="
				+ nuevos + ", cerrada=" + cerrada + "]";
	}

	

	

	

	

	
	
	

}
