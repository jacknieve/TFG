package com.tfg.mentoring.model.auxiliar.DTO;


public class MentorDTO {

	private String correo;
	private String nombre;
	private String papellido;
	private String sapellido;
	private String nivelEstudios;
	private String entidad;
	private int horaspormes;
	private String institucionNombre;
	private boolean verificado;
	

	public MentorDTO(String correo, String nombre, String papellido, String sapellido, String nivelEstudios,
			int horaspormes, String entidad, String institucionNombre, boolean verificado) {
		super();
		this.correo = correo;
		this.nombre = nombre;
		this.papellido = papellido;
		this.sapellido = sapellido;
		this.nivelEstudios = nivelEstudios;
		this.horaspormes = horaspormes;
		this.entidad=entidad;
		this.verificado=verificado;
	}

	public MentorDTO() {
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


	public int getHoraspormes() {
		return horaspormes;
	}

	public void setHoraspormes(int horaspormes) {
		this.horaspormes = horaspormes;
	}
	
	

	public String getEntidad() {
		return entidad;
	}

	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}
	
	

	public String getInstitucionNombre() {
		return institucionNombre;
	}

	public void setInstitucionNombre(String institucionNombre) {
		this.institucionNombre = institucionNombre;
	}
	
	

	public boolean isVerificado() {
		return verificado;
	}

	public void setVerificado(boolean verificado) {
		this.verificado = verificado;
	}

	@Override
	public String toString() {
		return "MentorBusqueda [correo=" + correo + ", nombre=" + nombre + ", papellido=" + papellido + ", sapellido="
				+ sapellido + ", nivelEstudios=" + nivelEstudios + ", entidad=" + entidad + ", horaspormes="
				+ horaspormes + ", institucionNombre=" + institucionNombre + ", verificado=" + verificado + "]";
	}

	

	

	
	
	
	
}
