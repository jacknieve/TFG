package com.tfg.mentoring.model.auxiliar.requests;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;

public class UserAux {

	@Email
	@NotEmpty(message = "El correo no puede estar vacío")
	@Size(max=240)
	@Pattern(regexp = "^[^/]*$", message = "El correo no puede contener el caracter /")
	private String correo;
	@NotEmpty(message = "La contraseña no puede estar vacía")
	@Size(max=255)
	private String password;
	@NotEmpty(message = "Es necesario introducir un nombre")
	@Size(max=255)
	@Pattern(regexp = "^([A-zÀ-ÿ\u00f1\u00d1\u0020]+)?$", message = "Por favor, introduzca un nombre válido")
	private String nombre;//
	@Size(max=255)
	@Pattern(regexp = "^([A-zÀ-ÿ\u00f1\u00d1\u0020]+)?$", message = "Por favor, introduzca un apellido válido")
	private String papellido;//
	@Size(max=255)
	@Pattern(regexp = "^([A-zÀ-ÿ\u00f1\u00d1\u0020]+)?$", message = "Por favor, introduzca un apellido válido")
	private String sapellido;//
	private String nivelEstudios;//
	@Pattern(regexp = "^(([+][0-9]{2})?[0-9]{9})?$", message = "Por favor, introduzca un formato de telefono válido")
	private String telefono;//
	private String descripcion;//
	//https://stackoverflow.com/questions/30256969/how-to-validate-linkedin-public-profile-url-regular-expression-in-python
	//@Pattern(regexp = "^(http[s]?://www.linkedin.com/in/[A-z0-9À-ÿ\\u00f1\\u00d1_-]+/?)?$", message = "Por favor, introduzca una url de linkedin valido")
	@URL(message = "Por favor, introduzca una url válida")
	private String linkedin;//
	private Integer horaspormes;//
	private String fnacimiento;//
	@Size(max=255)
	private String entidad;//
	private String institucion;//
	private Boolean mentor;//
	private String mensajeCambio;
	
	public UserAux(String nombre, String papellido, String sapellido, String nivelEstudios,
			String telefono, String descripcion, String linkedin, Integer horaspormes,
			String fnacimiento, String entidad, String institucion, Boolean mentor, String correo, String password, String mensajeCambio) {
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
		this.mensajeCambio=mensajeCambio;
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

	public Integer getHoraspormes() {
		return horaspormes;
	}

	public void setHoraspormes(Integer horaspormes) {
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
	
	

	public String getMensajeCambio() {
		return mensajeCambio;
	}

	public void setMensajeCambio(String mensajeCambio) {
		this.mensajeCambio = mensajeCambio;
	}

	@Override
	public String toString() {
		return "UserAux [correo=" + correo + ", password=" + password + ", nombre=" + nombre + ", papellido="
				+ papellido + ", sapellido=" + sapellido + ", nivelEstudios=" + nivelEstudios + ", telefono=" + telefono
				+ ", descripcion=" + descripcion + ", linkedin=" + linkedin + ", horaspormes=" + horaspormes
				+ ", fnacimiento=" + fnacimiento + ", entidad=" + entidad + ", institucion=" + institucion + ", mentor="
				+ mentor + ", mensajeCambio=" + mensajeCambio + "]";
	}

	

	

	
	
	
}
