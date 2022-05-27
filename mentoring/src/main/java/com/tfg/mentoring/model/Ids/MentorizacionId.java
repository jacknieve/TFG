package com.tfg.mentoring.model.Ids;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MentorizacionId implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Column(name="id_mentor")
	private String mentor;
	@Column(name="id_mentorizado")
	private String mentorizado;
	@Column(name="creada_en")
	private Date creadaEn = new Date();
	
	public MentorizacionId() {
		
	}
	
	public MentorizacionId(String mentor, String mentorizado) {
		this.mentor=mentor;
		this.mentorizado=mentorizado;
	}

	public String getMentor() {
		return mentor;
	}

	public String getMentorizado() {
		return mentorizado;
	}
	
	public Date getCreadaEn() {
		return creadaEn;
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		MentorizacionId that = (MentorizacionId) o;
		return Objects.equals(mentor, that.mentor) && Objects.equals(mentorizado, that.mentorizado);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(mentor, mentorizado);
	}

	
	
	

}
