package com.tfg.mentoring.model.auxiliar;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserAux {

	@Email
	@NotEmpty(message = "El correo no puede estar vacío")
	@Size(max=255)
	private String correo;
	@NotEmpty(message = "La contraseña no puede estar vacía")
	@Size(max=255)
	private String password;
	@NotEmpty(message = "Es necesario introducir un nombre")
	@Size(max=255)
	private String nombre;//
	@Size(max=255)
	private String papellido;//
	@Size(max=255)
	private String sapellido;//
	private String nivelEstudios;//
	@Pattern(regexp = "([+][0-9]{2})?[0-9]{9}", message = "Por favor, introduzca un formato de telefono valido")
	private String telefono;//
	private String descripcion;//
	private String linkedin;//
	private float horaspormes;//
	private String fnacimiento;//
	@Size(max=255)
	private String entidad;//
	private String institucion;//
	private Boolean mentor;//
	
	public UserAux(String nombre, String papellido, String sapellido, String nivelEstudios,
			String telefono, String descripcion, String linkedin, float horaspormes,
			String fnacimiento, String entidad, String institucion, Boolean mentor, String correo, String password) {
		super();
		this.nombre = nombre;
		this.papellido = papellido;
		this.sapellido = sapellido;
		this.nivelEstudios = nivelEstudios;
		this.telefono = telefono;
		this.descripcion = descripcion;
		this.linkedin = linkedin;
		this.horaspormes = horaspormes;
		this.fnacimiento = fnacimiento;
		this.entidad = entidad;
		this.institucion = institucion;
		this.mentor = mentor;
		this.correo=correo;
		this.password=password;
	}

	public UserAux() {
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

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getLinkedin() {
		return linkedin;
	}

	public void setLinkedin(String linkedin) {
		this.linkedin = linkedin;
	}

	public float getHoraspormes() {
		return horaspormes;
	}

	public void setHoraspormes(float horaspormes) {
		this.horaspormes = horaspormes;
	}

	public String getFnacimiento() {
		return fnacimiento;
	}

	public void setFnacimiento(String fnacimiento) {
		this.fnacimiento = fnacimiento;
	}


	

	public String getEntidad() {
		return entidad;
	}

	public void setEntidad(String entidad) {
		this.entidad = entidad;
	}

	public String getInstitucion() {
		return institucion;
	}

	public void setInstitucion(String institucion) {
		this.institucion = institucion;
	}
	
	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getMentor() {
		return mentor;
	}

	public void setMentor(Boolean mentor) {
		this.mentor = mentor;
	}

	@Override
	public String toString() {
		return "UserAux [correo=" + correo + ", password=" + password + ", nombre=" + nombre + ", papellido="
				+ papellido + ", sapellido=" + sapellido + ", nivelEstudios=" + nivelEstudios + ", telefono=" + telefono
				+ ", descripcion=" + descripcion + ", linkedin=" + linkedin + ", horaspormes=" + horaspormes
				+ ", fnacimiento=" + fnacimiento + ", entidad=" + entidad + ", institucion=" + institucion + ", mentor="
				+ mentor + "]";
	}

	

	
	
	
}
