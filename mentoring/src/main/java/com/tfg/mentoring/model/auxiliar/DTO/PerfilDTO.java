package com.tfg.mentoring.model.auxiliar.DTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tfg.mentoring.model.AreaConocimiento;

public class PerfilDTO {

	private String nombre;//
	private String papellido;//
	private String sapellido;//
	private String nivelEstudiosNivelestudios;//Este nonbre es tan raro y largo para el mapeo
	private String telefono;//
	private String descripcion;//
	private String linkedin;
	private int horaspormes;//-
	private Date fnacimiento;//
	private List<AreaConocimiento> areas = new ArrayList<>();
	private String entidad;//-
	private String institucionNombre;
	private boolean mentor;//
	private boolean notificar_correo;//
	private boolean verificado;
	private String foto;
	private String correo;
	private List<String> ficheros = new ArrayList<>();

	public PerfilDTO(String nombre, String papellido, String sapellido, String nivelEstudiosNivelestudios,
			String telefono, String descripcion, String linkedin, int horaspormes,
			Date fnacimiento, String entidad, String institucionNombre, boolean mentor, boolean notificar_correo, boolean verificado,
			String correo) {
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
		this.entidad = entidad;
		this.institucionNombre = institucionNombre;
		this.mentor=mentor;
		this.notificar_correo=notificar_correo;
		this.verificado=verificado;
		this.foto = "";
		this.correo = correo;
	}
	

	public PerfilDTO() {
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

	public int getHoraspormes() {
		return horaspormes;
	}

	public void setHoraspormes(int horaspormes) {
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
	
	

	public String getEntidad() {
		return entidad;
	}


	public void setEntidad(String entidad) {
		this.entidad = entidad;
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


	public boolean isNotificar_correo() {
		return notificar_correo;
	}


	public void setNotificar_correo(boolean notificar_correo) {
		this.notificar_correo = notificar_correo;
	}
	
	


	public boolean isVerificado() {
		return verificado;
	}


	public void setVerificado(boolean verificado) {
		this.verificado = verificado;
	}

	

	public String getFoto() {
		return foto;
	}


	public void setFoto(String foto) {
		this.foto = foto;
	}

	

	public String getCorreo() {
		return correo;
	}


	public void setCorreo(String correo) {
		this.correo = correo;
	}

	

	public List<String> getFicheros() {
		return ficheros;
	}


	public void setFicheros(List<String> ficheros) {
		this.ficheros = ficheros;
	}


	@Override
	public String toString() {
		return "PerfilDTO [nombre=" + nombre + ", papellido=" + papellido + ", sapellido=" + sapellido
				+ ", nivelEstudiosNivelestudios=" + nivelEstudiosNivelestudios + ", telefono=" + telefono
				+ ", descripcion=" + descripcion + ", linkedin=" + linkedin + ", horaspormes=" + horaspormes
				+ ", fnacimiento=" + fnacimiento + ", areas=" + areas + ", entidad=" + entidad + ", institucionNombre="
				+ institucionNombre + ", mentor=" + mentor + ", notificar_correo=" + notificar_correo + ", verificado="
				+ verificado + ", foto=" + foto + ", correo=" + correo + ", ficheros=" + ficheros + "]";
	}


	
}
