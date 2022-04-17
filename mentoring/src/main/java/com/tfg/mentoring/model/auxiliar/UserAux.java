package com.tfg.mentoring.model.auxiliar;




public class UserAux {

	private String nombre;//
	private String papellido;//
	private String sapellido;//
	private String nivelEstudios;//
	private String telefono;//
	private String descripcion;//
	private String linkedin;//
	private float horaspormes;//
	private String fnacimiento;//
	private String puesto;//
	private String institucion;//
	private Boolean mentor;//
	
	public UserAux(String nombre, String papellido, String sapellido, String nivelEstudios,
			String telefono, String descripcion, String linkedin, float horaspormes,
			String fnacimiento, String puesto, String institucion, Boolean mentor) {
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
		this.puesto = puesto;
		this.institucion = institucion;
		this.mentor = mentor;
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


	public String getPuesto() {
		return puesto;
	}

	public void setPuesto(String puesto) {
		this.puesto = puesto;
	}

	public String getInstitucion() {
		return institucion;
	}

	public void setInstitucion(String institucion) {
		this.institucion = institucion;
	}
	
	

	public Boolean getMentor() {
		return mentor;
	}

	public void setMentor(Boolean mentor) {
		this.mentor = mentor;
	}

	@Override
	public String toString() {
		return "UserAux [nombre=" + nombre + ", papellido=" + papellido + ", sapellido="
				+ sapellido + ", nivelEstudios=" + nivelEstudios + ", telefono=" + telefono + ", descripcion="
				+ descripcion + ", linkedin=" + linkedin + ", horaspormes=" + horaspormes + ", fnacimiento="
				+ fnacimiento + ", puesto=" + puesto + ", institucion=" + institucion + ", mentor=" + mentor + "]";
	}

	
	
	
}
