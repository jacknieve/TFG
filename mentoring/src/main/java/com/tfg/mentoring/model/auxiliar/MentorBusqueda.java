package com.tfg.mentoring.model.auxiliar;


public class MentorBusqueda {

	private String correo;
	private String nombre;
	private String papellido;
	private String sapellido;
	private String nivelEstudios;
	private float horaspormes;
	

	public MentorBusqueda(String correo, String nombre, String papellido, String sapellido, String nivelEstudios,
			float horaspormes) {
		super();
		this.correo = correo;
		this.nombre = nombre;
		this.papellido = papellido;
		this.sapellido = sapellido;
		this.nivelEstudios = nivelEstudios;
		this.horaspormes = horaspormes;
	}

	public MentorBusqueda() {
		super();
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPapellido() {
		return papellido;
	}

	public void setPapellido(String papellido) {
		this.papellido = papellido;
	}

	public String getSapellido() {
		return sapellido;
	}

	public void setSapellido(String sapellido) {
		this.sapellido = sapellido;
	}

	public String getNivelEstudios() {
		return nivelEstudios;
	}

	public void setNivelEstudios(String nivelEstudios) {
		this.nivelEstudios = nivelEstudios;
	}


	public float getHoraspormes() {
		return horaspormes;
	}

	public void setHoraspormes(float horaspormes) {
		this.horaspormes = horaspormes;
	}
	
	

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	@Override
	public String toString() {
		return "MentorBusqueda [correo=" + correo + ", nombre=" + nombre + ", papellido=" + papellido + ", sapellido="
				+ sapellido + ", nivelEstudios=" + nivelEstudios + ", horaspormes=" + horaspormes + "]";
	}

	
	
	
}
