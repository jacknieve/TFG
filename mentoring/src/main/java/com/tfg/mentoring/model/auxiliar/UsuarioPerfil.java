package com.tfg.mentoring.model.auxiliar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tfg.mentoring.model.AreaConocimiento;
import com.tfg.mentoring.model.Mentor;
import com.tfg.mentoring.model.Mentorizado;


//Clase que contiene los datos que se le devolverán a un usuario al acceder a su perfil
public class UsuarioPerfil {

	private String nombre;
	private String papellido;
	private String sapellido;
	private String nivelEstudios;
	private String telefono;
	private String descripcion;
	private String linkedin;
	private float horaspormes;
	private Date fnacimiento;
	private List<AreaConocimiento> areas = new ArrayList<>();
	private String puesto;
	private String institucion;
	private boolean mentor;

	public UsuarioPerfil(String nombre, String papellido, String sapellido, String nivelEstudios,
			String telefono, String descripcion, String linkedin, float horaspormes,
			Date fnacimiento, String puesto, String institucion, boolean mentor) {
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
		this.mentor=mentor;
	}
	
	public UsuarioPerfil(Mentor m) {
		super();
		this.nombre = m.getNombre();
		this.papellido = m.getPapellido();
		this.sapellido = m.getSapellido();
		this.nivelEstudios = m.getNivelEstudios();
		this.telefono = m.getTelefono();
		this.descripcion = m.getDescripcion();
		this.linkedin = m.getLinkedin();
		this.horaspormes = m.getHoraspormes();
		this.fnacimiento=m.getFnacimiento();
		this.puesto = m.getPuesto();
		this.institucion = m.getInstitucion().getNombre();
		this.areas=m.getAreas();
		this.mentor=true;
	}
	
	public UsuarioPerfil(Mentorizado m) {
		super();
		this.nombre = m.getNombre();
		this.papellido = m.getPapellido();
		this.sapellido = m.getSapellido();
		this.nivelEstudios = m.getNivelEstudios();
		this.telefono = m.getTelefono();
		this.descripcion = m.getDescripcion();
		this.linkedin = m.getLinkedin();
		this.horaspormes = 0;
		this.fnacimiento=m.getFnacimiento();
		this.puesto = null;
		this.institucion = m.getInstitucion().getNombre();
		this.areas=m.getAreas();
		this.mentor=false;
	}

	public UsuarioPerfil() {
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

	public Date getFnacimiento() {
		return fnacimiento;
	}

	public void setFnacimiento(Date fnacimiento) {
		this.fnacimiento = fnacimiento;
	}
	
	public List<AreaConocimiento> getAreas() {
		return areas;
	}

	public void setAreas(List<AreaConocimiento> areas) {
		this.areas = areas;
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
	
	



	public boolean isMentor() {
		return mentor;
	}

	public void setMentor(boolean mentor) {
		this.mentor = mentor;
	}

	@Override
	public String toString() {
		return "UsuarioPerfil [nombre=" + nombre + ", papellido=" + papellido + ", sapellido=" + sapellido
				+ ", nivelEstudios=" + nivelEstudios + ", telefono=" + telefono + ", descripcion=" + descripcion
				+ ", linkedin=" + linkedin + ", horaspormes=" + horaspormes + ", fnacimiento=" + fnacimiento
				+ ", areas=" + areas + ", puesto=" + puesto + ", institucion=" + institucion + ", mentor=" + mentor
				+ "]";
	}

	
}
