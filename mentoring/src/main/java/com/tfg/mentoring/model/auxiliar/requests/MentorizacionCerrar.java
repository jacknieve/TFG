package com.tfg.mentoring.model.auxiliar.requests;


public class MentorizacionCerrar {
	
	private String mentor;
	private String comentario;
	private int puntuacion;
	private Long fechafin;
	
	public MentorizacionCerrar(String mentor, String comentario, int puntuacion, Long fechafin) {
		super();
		this.mentor = mentor;
		this.comentario = comentario;
		this.puntuacion = puntuacion;
		this.fechafin=fechafin;
	}

	public MentorizacionCerrar() {
		super();
	}

	public String getMentor() {
		return mentor;
	}

	public void setMentor(String mentor) {
		this.mentor = mentor;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public int getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion(int puntuacion) {
		this.puntuacion = puntuacion;
	}
	
	

	public Long getFechafin() {
		return fechafin;
	}

	public void setFechafin(Long fechafin) {
		this.fechafin = fechafin;
	}

	@Override
	public String toString() {
		return "MentorizacionCerrar [mentor=" + mentor + ", comentario=" + comentario + ", puntuacion=" + puntuacion
				+ ", fechafin=" + fechafin + "]";
	}

	
	
	
	
}
