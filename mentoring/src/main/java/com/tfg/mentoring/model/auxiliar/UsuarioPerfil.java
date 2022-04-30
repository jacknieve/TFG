package com.tfg.mentoring.model.auxiliar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tfg.mentoring.model.AreaConocimiento;


//Clase que contiene los datos que se le devolverán a un usuario al acceder a su perfil
public class UsuarioPerfil {

	private String nombre;//
	private String papellido;//
	private String sapellido;//
	private String nivelEstudiosNivelestudios;//Este nonbre es tan raro y largo para el mapeo
	private String telefono;//
	private String descripcion;//
	private String linkedin;
	private float horaspormes;//-
	private Date fnacimiento;//
	private List<AreaConocimiento> areas = new ArrayList<>();
	private String puestoPuesto;//-
	private String institucionNombre;
	private boolean mentor;//
	private boolean notificar_correo;//
	private int edad;

	public UsuarioPerfil(String nombre, String papellido, String sapellido, String nivelEstudiosNivelestudios,
			String telefono, String descripcion, String linkedin, float horaspormes,
			Date fnacimiento, String puestoPuesto, String institucionNombre, boolean mentor, boolean notificar_correo) {
		super();
		this.nombre = nombre;
		this.papellido = papellido;
		this.sapellido = sapellido;
		this.nivelEstudiosNivelestudios = nivelEstudiosNivelestudios;
		this.telefono = telefono;
		this.descripcion = descripcion;
		this.linkedin = linkedin;
		this.horaspormes = horaspormes;
		this.fnacimiento = fnacimiento;
		this.puestoPuesto = puestoPuesto;
		this.institucionNombre = institucionNombre;
		this.mentor=mentor;
		this.notificar_correo=notificar_correo;
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

	public String getNivelEstudiosNivelestudios() {
		return nivelEstudiosNivelestudios;
	}

	public void setNivelEstudiosNivelestudios(String nivelEstudiosNivelestudios) {
		this.nivelEstudiosNivelestudios = nivelEstudiosNivelestudios;
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
	

	public String getPuestoPuesto() {
		return puestoPuesto;
	}

	public void setPuestoPuesto(String puestoPuesto) {
		this.puestoPuesto = puestoPuesto;
	}

	public String getInstitucionNombre() {
		return institucionNombre;
	}

	public void setInstitucionNombre(String institucionNombre) {
		this.institucionNombre = institucionNombre;
	}
	

	public boolean isMentor() {
		return mentor;
	}

	public void setMentor(boolean mentor) {
		this.mentor = mentor;
	}
	
	


	public int getEdad() {
		return edad;
	}


	public void setEdad(int edad) {
		this.edad = edad;
	}


	public boolean isNotificar_correo() {
		return notificar_correo;
	}


	public void setNotificar_correo(boolean notificar_correo) {
		this.notificar_correo = notificar_correo;
	}


	@Override
	public String toString() {
		return "UsuarioPerfil [nombre=" + nombre + ", papellido=" + papellido + ", sapellido=" + sapellido
				+ ", nivelEstudiosNivelestudios=" + nivelEstudiosNivelestudios + ", telefono=" + telefono
				+ ", descripcion=" + descripcion + ", linkedin=" + linkedin + ", horaspormes=" + horaspormes
				+ ", fnacimiento=" + fnacimiento + ", areas=" + areas + ", puestoPuesto=" + puestoPuesto
				+ ", institucionNombre=" + institucionNombre + ", mentor=" + mentor + ", notificar_correo="
				+ notificar_correo + ", edad=" + edad + "]";
	}


	


	
	

	
}
