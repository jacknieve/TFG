package com.tfg.mentoring.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SalaChatId implements Serializable{

	private static final long serialVersionUID = 1L;
	@Column(name="id_mentor")
	private String mentor;
	@Column(name="id_mentorizado")
	private String mentorizado;
	@Column(name="fecha_inicio")
	private Date inicio = new Date();
	
	public SalaChatId() {
		
	}
	
	public SalaChatId(String mentor, String mentorizado) {
		this.mentor=mentor;
		this.mentorizado=mentorizado;
	}
	
	public SalaChatId(String mentor, String mentorizado, Date inicio) {
		this.mentor=mentor;
		this.mentorizado=mentorizado;
		this.inicio = inicio;
	}

	public String getMentor() {
		return mentor;
	}

	public String getMentorizado() {
		return mentorizado;
	}
	
	
	
	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		SalaChatId that = (SalaChatId) o;
		return Objects.equals(mentor, that.mentor) && Objects.equals(mentorizado, that.mentorizado) && Objects.equals(inicio, that.inicio);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(mentor, mentorizado, inicio);
	}

	@Override
	public String toString() {
		return "SalaChatId [mentor=" + mentor + ", mentorizado=" + mentorizado + ", inicio=" + inicio + "]";
	}
	
	
}
