package com.tfg.mentoring.model.Ids;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MensajeChatId implements Serializable{

	private static final long serialVersionUID = 1L;
	/*@Column(name="id_mentor")
	private String mentor;
	@Column(name="id_mentorizado")
	private String mentorizado;
	@Column(name="inicio")
	private Date inicio;*/
	@Column(name="id_sala")
	private long id;
	@Column(name="fecha_envio")
	private Date fenvio = new Date();
	@Column(name="dementor")
	private boolean deMentor;
	
	public MensajeChatId() {
		
	}
	
	public MensajeChatId(long id, boolean deMentor) {
		this.id=id;
		this.deMentor=deMentor;
	}

	

	public long getId() {
		return id;
	}

	public boolean isDeMentor() {
		return deMentor;
	}

	public void setDeMentor(boolean deMentor) {
		this.deMentor = deMentor;
	}

	public Date getFenvio() {
		return fenvio;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		MensajeChatId that = (MensajeChatId) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(deMentor, that.deMentor) && Objects.equals(fenvio, that.fenvio);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, deMentor, fenvio);
	}

	@Override
	public String toString() {
		return "MensajeChatId [id=" + id + ", fenvio=" + fenvio + ", deMentor=" + deMentor + "]";
	}

	
	
	
}
