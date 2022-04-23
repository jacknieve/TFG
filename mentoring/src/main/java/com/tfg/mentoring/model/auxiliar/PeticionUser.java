package com.tfg.mentoring.model.auxiliar;

import java.util.Date;

import com.tfg.mentoring.model.Peticion;

public class PeticionUser {

	private String mentorizado;
	private String nombre;
	private String motivo;
	private boolean nueva;
	private Date fechaenv;
	
	public PeticionUser(String mentorizado, String nombre, String motivo, boolean nueva, Date fechaenv) {
		super();
		this.mentorizado = mentorizado;
		this.nombre = nombre;
		this.motivo = motivo;
		this.nueva = nueva;
		this.fechaenv = fechaenv;
	}
	
	public PeticionUser(Peticion p) {
		super();
		this.mentorizado = p.getMentorizado().getCorreo();
		this.nombre = p.getMentorizado().getNombre()+" "+p.getMentorizado().getPapellido()+" "+p.getMentorizado().getSapellido();
		this.motivo = p.getMotivo();
		if(p.getEstado() == EstadosPeticion.ENVIADA) this.nueva = true;
		else this.nueva=false;
		this.fechaenv = p.getId().getCreadaEn();
	}

	public PeticionUser() {
		super();
	}

	public String getMentorizado() {
		return mentorizado;
	}

	public void setMentorizado(String mentorizado) {
		this.mentorizado = mentorizado;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public boolean isNueva() {
		return nueva;
	}

	public void setNueva(boolean nueva) {
		this.nueva = nueva;
	}

	public Date getFechaenv() {
		return fechaenv;
	}

	public void setFechaenv(Date fechaenv) {
		this.fechaenv = fechaenv;
	}

	@Override
	public String toString() {
		return "PeticionUser [mentorizado=" + mentorizado + ", nombre=" + nombre + ", motivo=" + motivo + ", nueva="
				+ nueva + ", fechaenv=" + fechaenv + "]";
	}
	
	
}
